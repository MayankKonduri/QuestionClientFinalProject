import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.synth.SynthTextAreaUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class StudentHome extends JPanel {
    private JFrame frame;
    private DatabaseManager databaseManager;
    private String userName; //= System.getProperty("user.name");
    private Timer countdownTimer;
    private Timer positionUpdateTimer;
    public String questionTableName;
    public String waitTimeOfClass;
    private int minutesUntilEndOfClass;
    private Timer timer;
    private JPanel topBar;
    public int periodNumber;
    JLabel positionLabel;
    JLabel classLabel;
    int labelWidth;
    int labelHeight;
    int xPosition;
    int yPosition;
    JPanel waitTimePanel = new JPanel();
    JLabel waitTimeLabel;
    int waitTimeLabelWidth;
    int waitTimeLabelHeight;
    int waitTimeXPosition;
    int waitTimeYPosition;
    JPanel positionPanel = new JPanel();
    int positionLabelWidth;
    int positionLabelHeight;
    int positionXPosition;
    int positionYPosition;
    JPanel buttonPanel = new JPanel();
    JButton addQuestionButton = new JButton("Add Question");
    JButton removeQuestionButton = new JButton("Remove Question");
    String[] columnNames;
    String[][] rowData;
    JTable questionTable;
    private Thread refreshThread;
    private volatile boolean running = true;
    private Thread refreshThread1;
    private volatile boolean running1 = false;
    public boolean successful_1;
    String className;
    private int position;
    public JDialog messagePopup;
    public JButton imageButton;
    public JPanel imagePanel;
    public ImageIcon imageIcon;
    public Image image;
    public boolean readMessage;
    JButton homeButton;
    ArrayList<Object[]> threeMessageStatus;
    ArrayList<Object[]> allButThreesMessageStatus;
    public StudentHome(JFrame frame, String userName) throws SQLException {
        this.frame = frame;
        this.userName = userName;
        this.databaseManager = new DatabaseManager(userName);

        this.setLayout(new BorderLayout());

        Font georgiaFont = new Font("Georgia", Font.BOLD, 16);
        if (databaseManager.checkTeacherExists(userName)) {
            frame.setSize(400, 225);
            System.out.println("Already a Teacher");

            JLabel messageLabel = new JLabel("Hello! You are a Teacher, not a Student", JLabel.CENTER);
            messageLabel.setFont(georgiaFont);
            messageLabel.setForeground(Color.RED);
            this.add(messageLabel, BorderLayout.CENTER);
        } else {

            String url = "jdbc:mysql://10.195.75.116/qclient1";
            String user = "root";
            String password = "password";
            String tableName = userName + "_waitTime";

            String createTableSQL = "CREATE TABLE IF NOT EXISTS " + tableName + " ("
                    + "ID INT AUTO_INCREMENT PRIMARY KEY, "
                    + "WaitTime_1 INT, "
                    + "WaitTime_2 INT, "
                    + "WaitTime_3 INT, "
                    + "WaitTime_4 INT, "
                    + "WaitTime_5 INT, "
                    + "WaitTime_6 INT, "
                    + "WaitTime_7 INT"
                    + ")";

            try (Connection connection = DriverManager.getConnection(url, user, password);
                 Statement statement = connection.createStatement()) {

                statement.executeUpdate(createTableSQL);
                System.out.println("Table '" + tableName + "' created successfully (if not exists).");

            } catch (SQLException e) {
                e.printStackTrace();
            }
            ArrayList<String[]> results = databaseManager.checkNameInStudentsTables(userName);

            LocalTime currentTime = LocalTime.now();

            int hour = currentTime.getHour();
            int minute = currentTime.getMinute();

            int temporary = 0;
            String bigToPrint = "";

            for(int i=0;i<results.size();i++){
                String timePreStart = (results.get(i))[2];
                String timePreEnd = (results.get(i))[3];
                int[] timePostStart = splitTime(timePreStart);
                int[] timePostEnd = splitTime(timePreEnd);

                int startHour = timePostStart[0];
                int startMinute = timePostStart[1];
                int endHour = timePostEnd[0];
                int endMinute = timePostEnd[1];

                boolean isInClassTime = (hour > startHour) || (hour == startHour && minute >= startMinute);
                boolean isBeforeEndTime = (hour < endHour) || (hour == endHour && minute < endMinute);

                if (isInClassTime && isBeforeEndTime) {

                    String formattedDisplayString = formatClassString(results.get(i)[0], results.get(i)[1]);

                    setLayout(null);

                    classLabel = new JLabel(formattedDisplayString);
                    classLabel.setFont(new Font("Georgia", Font.PLAIN, 16));
                    classLabel.setForeground(Color.BLACK);

                    labelWidth = classLabel.getPreferredSize().width;
                    labelHeight = classLabel.getPreferredSize().height;

                    xPosition = (400 - labelWidth) / 2;
                    yPosition = 50;

                    classLabel.setBounds(xPosition, yPosition, labelWidth, labelHeight);
                    add(classLabel);

                    waitTimePanel.setBackground(new Color(255, 182, 193));
                    waitTimePanel.setLayout(new BorderLayout());

                    waitTimeLabel = new JLabel("Wait Time: 0 seconds", SwingConstants.CENTER);
                    waitTimeLabel.setFont(new Font("Georgia", Font.PLAIN, 12));
                    waitTimeLabel.setForeground(Color.BLACK);
                    waitTimePanel.setBorder(new LineBorder(Color.BLACK, 2));

                    waitTimePanel.add(waitTimeLabel, BorderLayout.CENTER);

                    waitTimeLabelWidth = waitTimeLabel.getPreferredSize().width;
                    waitTimeLabelHeight = waitTimeLabel.getPreferredSize().height;

                    waitTimeXPosition = (400 - waitTimeLabelWidth) / 2;
                    waitTimeYPosition = yPosition + labelHeight + 10;

                    waitTimePanel.setBounds(waitTimeXPosition - 123, waitTimeYPosition + 20, waitTimeLabelWidth + 58, waitTimeLabelHeight + 20);

                    add(waitTimePanel);

                    positionPanel.setLayout(new BorderLayout());

                    positionLabel = new JLabel("Position: ", SwingConstants.CENTER);
                    positionLabel.setFont(new Font("Georgia", Font.PLAIN, 12));
                    positionLabel.setForeground(Color.BLACK);
                    positionPanel.setBorder(new LineBorder(Color.BLACK, 2));

                    positionPanel.add(positionLabel, BorderLayout.CENTER);

                    positionLabelWidth = waitTimePanel.getWidth();
                    positionLabelHeight = positionLabel.getPreferredSize().height;

                    positionXPosition = waitTimePanel.getX() + waitTimePanel.getWidth() + 4;
                    positionYPosition = waitTimePanel.getY();

                    positionPanel.setBounds(positionXPosition, positionYPosition, positionLabelWidth - 6, positionLabelHeight + 20);

                    add(positionPanel);

                    revalidate();
                    repaint();

                    buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
                    buttonPanel.setBounds(15, yPosition + labelHeight + 140, 360, 40);

                    addQuestionButton.setFont(new Font("Georgia", Font.PLAIN, 12));
                    removeQuestionButton.setFont(new Font("Georgia", Font.PLAIN, 12));
                    addQuestionButton.setBounds(15, yPosition + labelHeight + 140, 360, 25);
                    removeQuestionButton.setBounds(15, yPosition + labelHeight + 140, 360, 25);

                    add(addQuestionButton);
                    add(removeQuestionButton);

                    columnNames = new String[]{"Question Summary"};
                    rowData = new String[][]{
                            {""}
                    };

                    DefaultTableModel model = new DefaultTableModel(rowData, columnNames){
                        @Override
                        public boolean isCellEditable(int row, int column) {
                            return false;
                        }
                    };
                    questionTable = new JTable(model);
                    questionTable.setRowSelectionAllowed(false);
                    questionTable.setColumnSelectionAllowed(false);
                    questionTable.setCellSelectionEnabled(false);

                    questionTable.getTableHeader().setFont(new Font("Georgia", Font.BOLD, 14));

                    int rowHeight = 40;
                    questionTable.setRowHeight(rowHeight);

                    questionTable.setFont(new Font("Georgia", Font.PLAIN, 12));

                    DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
                    centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

                    for (int ii = 0; ii < questionTable.getColumnCount(); ii++) {
                        questionTable.getColumnModel().getColumn(ii).setCellRenderer(centerRenderer);
                    }

                    JScrollPane tableScrollPane = new JScrollPane(questionTable);
                    tableScrollPane.setBounds(15, yPosition + labelHeight + 70, 360, 70);

                    questionTable.setPreferredScrollableViewportSize(new Dimension(300, 100));

                    add(tableScrollPane);
                }}

            if (results.isEmpty()) {
                System.out.println("No records found for the student.");
            } else {
                for (String[] result : results) {
                    System.out.println("Main Table: " + result[0]);
                    System.out.println("Class Name: " + result[1]);
                    System.out.println("Start Time: " + result[2]);
                    System.out.println("End Time: " + result[3]);
                    System.out.println("Wait Time: " + result[4]);
                    System.out.println("-------------------------");
                }
            }

            currentTime = LocalTime.now();

            hour = currentTime.getHour();
            minute = currentTime.getMinute();

            temporary = 0;
            bigToPrint = "";

            for(int i=0;i<results.size();i++){
                String timePreStart = (results.get(i))[2];
                String timePreEnd = (results.get(i))[3];
                int[] timePostStart = splitTime(timePreStart);
                int[] timePostEnd = splitTime(timePreEnd);

                int startHour = timePostStart[0];
                int startMinute = timePostStart[1];
                int endHour = timePostEnd[0];
                int endMinute = timePostEnd[1];

                boolean isInClassTime = (hour > startHour) || (hour == startHour && minute >= startMinute);
                boolean isBeforeEndTime = (hour < endHour) || (hour == endHour && minute < endMinute);

                if (isInClassTime && isBeforeEndTime) {
                    className = results.get(i)[1];
                    System.out.println("Current time is within the class time: " + results.get(i)[1]);
                    int currentTimeInMinutes = hour * 60 + minute;

                    int endTimeInMinutes = endHour * 60 + endMinute;

                    minutesUntilEndOfClass = endTimeInMinutes - currentTimeInMinutes;
                    initializeStudentDashboard(results.get(i)[0], results.get(i)[1]);
                    initializeTimer(minutesUntilEndOfClass);

                    int finalI = i;
                    timer = new Timer(1000, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {

                            LocalTime currentTime1 = LocalTime.now();
                            int hour1 = currentTime1.getHour();
                            int minute1 = currentTime1.getMinute();

                            int currentTimeInMinutes = hour1 * 60 + minute1;

                            int endTimeInMinutes = endHour * 60 + endMinute;

                            minutesUntilEndOfClass = endTimeInMinutes - currentTimeInMinutes;

                            if (minutesUntilEndOfClass > 0) {

                                initializeTimer(minutesUntilEndOfClass);
                            } else {

                                timer.stop();
                                System.out.println(results.get(finalI)[0] + " " + results.get(finalI)[1] + " is not currently going on");
                                String msg =formatClassString(results.get(finalI)[0], results.get(finalI)[1]);
                                JOptionPane.showMessageDialog(frame, msg + " is Over");
                                StudentHome studentHome = null;
                                try {
                                    studentHome = new StudentHome(frame, userName);
                                } catch (SQLException ex) {
                                    throw new RuntimeException(ex);
                                }
                                frame.getContentPane().removeAll();
                                frame.revalidate();
                                frame.repaint();
                                frame.setSize(400, 325);
                                frame.add(studentHome);
                                frame.setVisible(true);
                            }
                        }
                    });
                    timer.start();

                }
                else{
                    int minutesToClassStart = calculateMinutesToClassStart(hour, minute, startHour, startMinute, endHour, endMinute);
                    if(minutesToClassStart == 1000000)
                    {
                        String smallToPrint = formatClassString(results.get(i)[0], results.get(i)[1]) + " is going to resume Tomorrow.";
                        System.out.println(smallToPrint);
                        temporary++;
                        bigToPrint = bigToPrint + smallToPrint + "~";
                        if(temporary == results.size()){
                            System.out.println(bigToPrint);

                            String[] splitArray = bigToPrint.split("~");

                            ArrayList<String> resultList = new ArrayList<>(Arrays.asList(splitArray));
                            resultList.removeIf(String::isEmpty);
                            initializeStudentWaitingScreen(resultList);
                        }
                    }
                    else {
                        String smallToPrint = formatClassString(results.get(i)[0], results.get(i)[1]) + " is going to resume in " + minutesToClassStart + " minutes.";
                        System.out.println(smallToPrint);
                        temporary++;
                        bigToPrint = bigToPrint + smallToPrint + "~";
                        if(temporary == results.size()){
                            System.out.println(bigToPrint);

                            String[] splitArray = bigToPrint.split("~");

                            ArrayList<String> resultList = new ArrayList<>(Arrays.asList(splitArray));
                            resultList.removeIf(String::isEmpty);
                            initializeStudentWaitingScreen(resultList);
                        }
                    }
                }
            }
        }

        JPanel homePanel = new JPanel();
        homePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

        homeButton = new JButton("Home");
        homeButton.setFont(new Font("Georgia", Font.BOLD, 10));
        homePanel.add(homeButton);

        homePanel.setBounds(10, 30, 100, 100);
        add(homePanel, BorderLayout.NORTH);

        imagePanel = new JPanel();
        imagePanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5,5));
        imagePanel.setBounds(345, 25, 50, 50);
        imageIcon = new ImageIcon("messages2.png");
        image = imageIcon.getImage();
        int newWidth = (int) (image.getWidth(null) * 0.05);
        int newHeight = (int) (image.getHeight(null) * 0.05);
        Image resizedImage = image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        ImageIcon resizedIcon = new ImageIcon(resizedImage);

        if(imageIcon.getImageLoadStatus() != MediaTracker.COMPLETE){
            System.out.println("Image Failed to Load.");
        }
        imageButton = new JButton(resizedIcon);
        imageButton.revalidate();
        imageButton.repaint();
        imageButton.setBorderPainted(false);
        imageButton.setFocusPainted(false);
        imageButton.setContentAreaFilled(false);
        imageButton.setBounds(90, 10, 80, 30);
        imagePanel.add(imageButton);
        add(imagePanel);

        imageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                threeMessageStatus = databaseManager.getThrees(questionTableName,userName);
                allButThreesMessageStatus = databaseManager.getAllButThrees(questionTableName, userName);
                updateImageButtonUI(true);
                showPopUp(frame, threeMessageStatus, allButThreesMessageStatus, questionTable, userName);
                startAutoRefreshThreadPopUp(frame, threeMessageStatus, allButThreesMessageStatus, questionTable, userName);
                databaseManager.resetSeenSample(questionTableName, userName, 0);
            }
        });

        homeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                HomePage homePage = null;
                try {
                    homePage = new HomePage(frame, userName);
                } catch (IOException ex) {

                }
                stopAutoRefreshThread();
                frame.getContentPane().removeAll();
                frame.getContentPane().add(homePage);
                frame.revalidate();
                frame.repaint();
                frame.setSize(400, 225);
            }
        });

        startAutoRefreshThread();
    }

    private void updateImageButtonUI(boolean readMessage) {
        if(readMessage){
            //imagePanel.remove(imageButton);
            imageIcon = new ImageIcon("messages2.png");
            image = imageIcon.getImage();
            int newWidth = (int) (image.getWidth(null) * 0.05);
            int newHeight = (int) (image.getHeight(null) * 0.05);
            Image resizedImage = image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            ImageIcon resizedIcon = new ImageIcon(resizedImage);

            if (imageIcon.getImageLoadStatus() != MediaTracker.COMPLETE) {
                System.out.println("Image Failed to Load.");
            }
            imageButton.setIcon(resizedIcon);
            imageButton.revalidate();
            imageButton.repaint();
            imageButton.setBorderPainted(false);
            imageButton.setFocusPainted(false);
            imageButton.setContentAreaFilled(false);
            imageButton.setBounds(90, 10, 80, 30);
        }else {
            imageIcon = new ImageIcon("unreadmessages2.png");
            image = imageIcon.getImage();
            int newWidth = (int) (image.getWidth(null) * 0.05);
            int newHeight = (int) (image.getHeight(null) * 0.05);
            Image resizedImage = image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            ImageIcon resizedIcon = new ImageIcon(resizedImage);

            if (imageIcon.getImageLoadStatus() != MediaTracker.COMPLETE) {
                System.out.println("Image Failed to Load.");
            }
            imageButton.setIcon(resizedIcon);
            imageButton.revalidate();
            imageButton.repaint();
            imageButton.setBorderPainted(false);
            imageButton.setFocusPainted(false);
            imageButton.setContentAreaFilled(false);
            imageButton.setBounds(90, 10, 80, 30);
        }
    }
    private void showPopUp(JFrame frame, ArrayList<Object[]> threeMessageStatus, ArrayList<Object[]> allButThreesMessageStatus, JTable questionTable, String userName) {
        System.out.println("Testing Thread Popup");
        ArrayList<Object[]> todayThreeMessageStatus = new ArrayList<>();
        ArrayList<Object[]> todayAllButThreeMessageStatus = new ArrayList<>();
        System.out.println("Number of Unread Threes: " + threeMessageStatus.size() + " and Number of Read Zeroes: " + allButThreesMessageStatus.size());
        System.out.println("Image Clicked");
        int countTodayThrees = 0;
        int countTodayZeroes = 0;

        LocalDate currentDate = LocalDate.now();
        for(int i=0;i<threeMessageStatus.size();i++){
            Timestamp timestamp = (Timestamp) threeMessageStatus.get(i)[4];
            LocalDate messageDate = timestamp.toLocalDateTime().toLocalDate();
            if (messageDate.equals(currentDate)) {
                countTodayThrees++;
                todayThreeMessageStatus.add(threeMessageStatus.get(i));
            }
        }
        for(int i=0;i<allButThreesMessageStatus.size();i++){
            Timestamp timestamp = (Timestamp) allButThreesMessageStatus.get(i)[4];
            LocalDate messageDate = timestamp.toLocalDateTime().toLocalDate();
            if (messageDate.equals(currentDate)) {
                countTodayZeroes++;
                todayAllButThreeMessageStatus.add(allButThreesMessageStatus.get(i));
            }
        }
        System.out.println("Number of Unread Threes from Today: " + countTodayThrees);
        System.out.println("Number of Read Zeroes from Today: " + countTodayZeroes);

        if(messagePopup != null && messagePopup.isVisible()){
            return;
        }

        messagePopup = new JDialog(frame, "Messages", false);
        messagePopup.setUndecorated(false);
        messagePopup.setResizable(false);
        messagePopup.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        //frame.setEnabled(false);
        //frame.setFocusableWindowState(false);
        addQuestionButton.setEnabled(false);
        homeButton.setEnabled(false);
        removeQuestionButton.setEnabled(false);

        messagePopup.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {

            }
        });

        int frameWidth = frame.getWidth();
        int frameHeight = frame.getHeight();
        int popupWidth = frameWidth/2;
        int popupHeight = frameHeight;

        int popupX = frame.getX() + frameWidth/2;
        int popupY = frame.getY();

        messagePopup.setBounds(popupX-30, popupY+ 60, popupWidth+20, popupHeight-80);
        messagePopup.setUndecorated(true);

        JPanel contentPanel = new JPanel();
        contentPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));  // Set a black border with thickness 2
        contentPanel.setLayout(null);

        JButton closeButton = new JButton("X");
        closeButton.setForeground(Color.BLACK);
        closeButton.setBackground(Color.WHITE);
        closeButton.setFont(new Font("Verdana",Font.BOLD,8));
        closeButton.setPreferredSize(new Dimension(40,20));
        closeButton.setBorder(BorderFactory.createLineBorder(Color.BLACK,1));
        closeButton.setFocusable(false);
        //closeButton.setBounds(contentPanel.getWidth() - 50, 8, 40, 20);  // Recalculate button position
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                closeButton.setBounds(contentPanel.getWidth() - 46, 7, 40, 20);  // Recalculate button position
            }
        });
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //frame.setEnabled(true);
                //frame.setFocusableWindowState(true);
                stopAutoRefreshThreadPopUp();
                addQuestionButton.setEnabled(true);
                homeButton.setEnabled(true);
                removeQuestionButton.setEnabled(true);
                messagePopup.dispose();
            }
        });

        JPanel shadowPanel = new JPanel();
        shadowPanel.setBackground(Color.LIGHT_GRAY);
        shadowPanel.setForeground(Color.BLACK);
        shadowPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        JLabel titleLabel = new JLabel("Question(s) History");
        titleLabel.setForeground(Color.BLACK);
        titleLabel.setFont(new Font("Georgia",Font.BOLD,11));

        shadowPanel.add(titleLabel);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                shadowPanel.setLayout(null);
                titleLabel.setBounds(10,5,120,20);
            }
        });
        //shadowPanel.setBounds(5, 5, contentPanel.getWidth()-10, 27);  // Recalculate button position

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                shadowPanel.setBounds(1, 1, contentPanel.getWidth()-1, 32);  // Recalculate button position
            }
        });

        String[] columns = {"Q. Summary", "Response"};
        DefaultTableModel tableModel = new DefaultTableModel(columns,0){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setFont(new Font("Georgia", Font.PLAIN, 10));
        table.setRowHeight(20);

        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setFont(new Font("Georgia",Font.BOLD,11));
        tableHeader.setReorderingAllowed(false);
        tableHeader.setPreferredSize(new Dimension(tableHeader.getWidth(), 25));
        ((DefaultTableCellRenderer) tableHeader.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(14, 0));
        scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 14));
        table.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        contentPanel.add(scrollPane);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                scrollPane.setBounds(3,34,contentPanel.getWidth()-4,contentPanel.getHeight()-35);
            }
        });

        todayThreeMessageStatus.sort((a, b) -> ((Timestamp) b[4]).compareTo((Timestamp) a[4]));
        for(Object[] row : todayThreeMessageStatus){
            if(((String) row[2]).equals("Teacher Manually Removed Question")){
                addRowToTable(tableModel, "<html><font color='blue'>" + (String) row[0] + "</font></html>", "<html><font color='blue'>Question Removed</font></html>");
            } else if(((String) row[2]).equals("Went to Student's Desk")){
                addRowToTable(tableModel, "<html><font color='blue'>" + (String) row[0] + "</font></html>", "<html><font color='blue'>Coming Over</font></html>");
            } else if(((String) row[2]).equals("Student Took Back Question")){
                addRowToTable(tableModel, "<html><font color='blue'>" + (String) row[0] + "</font></html>", "<html><font color='blue'>Question Removed</font></html>");
            } else{
                addRowToTable(tableModel, "<html><font color='blue'>" + (String) row[0] + "</font></html>", "<html><font color='blue'>" + (String) row[2] + "</font></html>");
            }
        }
        todayAllButThreeMessageStatus.sort((a, b) -> ((Timestamp) b[4]).compareTo((Timestamp) a[4]));
        for(Object[] row : todayAllButThreeMessageStatus){
            if(((String) row[2]).equals("Teacher Manually Removed Question")){
                addRowToTable(tableModel, "<html><font color='red'>" + (String) row[0] + "</font></html>", "<html><font color='red'>Question Removed</font></html>");
            } else if(((String) row[2]).equals("Went to Student's Desk")){
                addRowToTable(tableModel, (String) row[0], "Coming Over");
            } else if(((String) row[2]).equals("Student Took Back Question")){
                addRowToTable(tableModel, "<html><font color='red'>" + (String) row[0] + "</font></html>", "<html><font color='red'>Question Removed</font></html>");
            } else{
                addRowToTable(tableModel, (String) row[0], (String) row[2]);
            }
        }

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row != -1) {
                        String QuestionSummary = tableModel.getValueAt(row, 0).toString();
                        String replaceQSummary = QuestionSummary.replaceAll("<[^>]+>","");
                        String Response = tableModel.getValueAt(row, 1).toString();
                        String replaceResponse = Response.replaceAll("<[^>]+>","");
                        JTextPane textPane = new JTextPane();
                            textPane.setContentType("text/plain");
                            textPane.setEditable(false);
                            textPane.setFont(new Font("Georgia", Font.PLAIN, 12));

                            StyledDocument doc = textPane.getStyledDocument();

                            SimpleAttributeSet boldStyle = new SimpleAttributeSet();
                            StyleConstants.setBold(boldStyle, true);
                            StyleConstants.setFontFamily(boldStyle, "Georgia");

                            SimpleAttributeSet regularStyle = new SimpleAttributeSet();
                            StyleConstants.setBold(regularStyle, false);
                            StyleConstants.setFontFamily(regularStyle, "Georgia");

                            try {

                                doc.insertString(doc.getLength(), "Question Summary: ", boldStyle);
                                doc.insertString(doc.getLength(), replaceQSummary + "\n", regularStyle);

                                doc.insertString(doc.getLength(), "Response: ", boldStyle);
                                doc.insertString(doc.getLength(), replaceResponse, regularStyle);

                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            questionTable.setFocusable(false);
                            JOptionPane.showOptionDialog(
                                    frame,
                                    new JScrollPane(textPane),
                                    "Question Details",
                                    JOptionPane.DEFAULT_OPTION,
                                    JOptionPane.INFORMATION_MESSAGE,
                                    null,
                                    null,
                                    null
                            );

                            table.clearSelection();
                    }
                }
            }
        });















//        JPanel panelForScrool = new JPanel();
//        panelForScrool.setLayout(new BoxLayout(panelForScrool, BoxLayout.Y_AXIS));
//        scrollPane.setViewportView(panelForScrool);
        contentPanel.add(shadowPanel);


        contentPanel.add(closeButton);


        contentPanel.revalidate();
        contentPanel.repaint();

        table.getSelectionModel().addListSelectionListener(event ->{
            if(!event.getValueIsAdjusting() && table.getSelectedRow() != -1){
                int selectedRow = table.getSelectedRow();
                int responseColumnIndex = 1;
                Object responseValue = table.getValueAt(selectedRow, responseColumnIndex);
                System.out.println("Selected Response: " + responseValue);
            }
        });

        messagePopup.add(contentPanel, BorderLayout.CENTER);
        messagePopup.setVisible(true);
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent e) {
                messagePopup.setLocation((frame.getX() + frame.getWidth()/2)-30, frame.getY()+60);
            }
        });
        messagePopup.setVisible(true);
    }

    private void addRowToTable(DefaultTableModel tableModel, String questionSummary, String response) {
        tableModel.addRow(new Object[]{questionSummary, response});
    }

    private void startAutoRefreshThread() {
        refreshThread = new Thread(() -> {
            while (running) {
                try {
                    updateStudentDashboard();
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        refreshThread.start();
    }

    private void stopAutoRefreshThread() {
        running = false;
        if (refreshThread != null) {
            refreshThread.interrupt();
        }
    }

    private void startAutoRefreshThreadPopUp(JFrame frame, ArrayList<Object[]> threeMessageStatus, ArrayList<Object[]> allButThreesMessageStatus, JTable questionTable, String userName) {
        running1 = true;
        refreshThread1 = new Thread(() -> {
            while (running1) {
                try {
                    ArrayList<Object[]> threeMessageStatus1 = databaseManager.getThrees(questionTableName, userName);
                    ArrayList<Object[]> allButThreesMessageStatus1 = databaseManager.getAllButThrees(questionTableName, userName);
                    showPopUp(frame, threeMessageStatus1, allButThreesMessageStatus1, questionTable, userName);
                    System.out.println("ThreeMessageStatus " + threeMessageStatus1.size());
                    System.out.println("AllButThreeMessageStatus " + allButThreesMessageStatus1.size());
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        refreshThread1.start();
    }

    private void stopAutoRefreshThreadPopUp() {
        running1 = false;
        if (refreshThread1 != null) {
            refreshThread1.interrupt();
        }
    }

    public void updateStudentDashboard() {

        ArrayList<String[]> results = null;
        try {
            results = databaseManager.checkNameInStudentsTables(userName);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }

        if (results.isEmpty()) {
            System.out.println("No records found for the student.");
        } else {
            for (int i = 0; i < results.size(); i++) {
                try {

                    LocalTime startTime = LocalTime.parse((String) results.get(i)[2]);
                    LocalTime endTime = LocalTime.parse((String) results.get(i)[3]);

                    if (LocalTime.now().isAfter(startTime) && LocalTime.now().isBefore(endTime)) {
                        String result = (String) results.get(i)[0];
                        String waitTime = (String) results.get(i)[4];
                        waitTimeOfClass = waitTime;
                        periodNumber = extractNumberFromTableName(result);
                        questionTableName = result.replace("_main", "_questions");
                        System.out.println(running1);
                        position = databaseManager.getQuestionPosition(questionTableName, userName);

                        Object[] seenMessageStatus = databaseManager.getSeenMessageStatus(questionTableName, userName);
                        ArrayList<Object[]> threeMessageStatus = databaseManager.getThrees(questionTableName,userName);
                        System.out.println("ThreesStatus" + threeMessageStatus.size());
                        if(seenMessageStatus!= null) {
                            if ((int) seenMessageStatus[3] == 2) {
                                String message = "Teacher Removed Your Question";
                                JTextPane textPane = new JTextPane();
                                textPane.setEditable(false);
                                textPane.setFont(new Font("Georgia", Font.PLAIN, 12));
                                textPane.setBorder(new LineBorder(Color.BLACK, 1));
                                StyledDocument doc = textPane.getStyledDocument();
                                SimpleAttributeSet boldStyle = new SimpleAttributeSet();
                                StyleConstants.setBold(boldStyle, true);
                                StyleConstants.setFontFamily(boldStyle, "Georgia");
                                SimpleAttributeSet regularStyleSet = new SimpleAttributeSet();
                                StyleConstants.setBold(regularStyleSet, false);
                                StyleConstants.setFontFamily(regularStyleSet, "Georgia");
                                try{
                                    doc.insertString(doc.getLength(), message, boldStyle);
                                } catch (Exception ex){
                                    ex.printStackTrace();
                                }
                                //updateImageButtonUI(false);
                                System.out.println("1" + running1);
                                if(!running1) {
                                    JOptionPane.showMessageDialog(null, textPane, "Notification", JOptionPane.INFORMATION_MESSAGE);
                                }
                                databaseManager.resetSeenSample(questionTableName, userName, 0);
                            } else if ((int) seenMessageStatus[3] == 1) {
                                String teacherResponse = (String) seenMessageStatus[2];
                                String message;

                                if(teacherResponse.equals("Went to Student's Desk")){
                                    message = "Coming Over to Your Desk";
                                }else{
                                    message = teacherResponse;
                                }
                                JTextPane textPane = new JTextPane();
                                textPane.setEditable(false);
                                textPane.setFont(new Font("Georgia", Font.PLAIN, 12));
                                textPane.setBorder(new LineBorder(Color.BLACK, 1));
                                StyledDocument doc = textPane.getStyledDocument();
                                SimpleAttributeSet boldStyle = new SimpleAttributeSet();
                                StyleConstants.setBold(boldStyle, true);
                                StyleConstants.setFontFamily(boldStyle, "Georgia");
                                SimpleAttributeSet regularStyleSet = new SimpleAttributeSet();
                                StyleConstants.setBold(regularStyleSet, false);
                                StyleConstants.setFontFamily(regularStyleSet, "Georgia");

                                try{
                                    doc.insertString(doc.getLength(), "Teacher Responded to Your Question", boldStyle);
                                    //doc.insertString(doc.getLength(), message, regularStyleSet);
                                } catch (Exception ex){
                                    ex.printStackTrace();
                                }
                                updateImageButtonUI(false);
                                System.out.println("2" + running1);
                                if(!running1) {
                                    JOptionPane.showMessageDialog(null, textPane, "Notification", JOptionPane.INFORMATION_MESSAGE);
                                }
                                databaseManager.resetSeenSample(questionTableName, userName, 3);
                            }
                        } else if (threeMessageStatus.size()>0){
                            updateImageButtonUI(false);
                            System.out.println("Number of Unread: " + threeMessageStatus.size());
                        }
                        positionLabel.setText("Position: " + position);
                        if(position==-1)
                        {
                            positionLabel.setText("Position: N/A");
                        }

                        String input = databaseManager.getQuestionStudent(questionTableName, userName);
                        if (input.equals("")) {

                            questionTable.setValueAt("No Active Question", 0, 0);
                            removeQuestionButton.setVisible(false);
                            addQuestionButton.setVisible(true);

                        } else {

                            questionTable.setValueAt(input, 0, 0);
                            removeQuestionButton.setVisible(true);
                            addQuestionButton.setVisible(false);

                            waitTimePanel.setBackground(new Color(144, 238, 144));
                        }

                        revalidate();
                        repaint();
                    }
                } catch (DateTimeParseException e) {
                    System.out.println("Invalid time format in results: " + e.getMessage());
                }
            }
        }
    }

    private void initializeTimer(int minutesTillEndOfClass) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                if (topBar == null) {
                    topBar = new JPanel();
                    topBar.setLayout(new FlowLayout(FlowLayout.CENTER));
                    topBar.setBounds(0, 0, 400, 25);
                    Border blackBorder = BorderFactory.createLineBorder(Color.BLACK, 2);
                    topBar.setBorder(blackBorder);
                    add(topBar);
                }

                if (minutesTillEndOfClass > 15) {
                    topBar.setBackground(Color.GREEN);
                } else if (minutesTillEndOfClass > 5) {
                    topBar.setBackground(Color.YELLOW);
                } else {
                    topBar.setBackground(Color.RED);
                }

                JLabel minutesLeftLabel = new JLabel("Minutes left: " + minutesTillEndOfClass);

                minutesLeftLabel.setFont(new Font("Georgia", Font.PLAIN, 12));
                minutesLeftLabel.setForeground(Color.BLACK);

                topBar.removeAll();
                topBar.add(minutesLeftLabel);

                topBar.revalidate();
                topBar.repaint();
            }
        });
    }

    private void initializeStudentWaitingScreen(ArrayList<String> resultList) {
        resultList.add("");
        int yPosition = 65;

        for (String tempResults : resultList) {
            JLabel messageLabel = new JLabel(tempResults, JLabel.CENTER);
            messageLabel.setForeground(Color.BLACK);
            messageLabel.setFont(new Font("Georgia", Font.PLAIN, 12));

            messageLabel.setBounds(20, yPosition, 360, 20);

            this.add(messageLabel);

            yPosition += 25;
        }

        frame.revalidate();
        frame.repaint();
    }

    public static int calculateMinutesToClassStart(int currentHour, int currentMinute, int startHour, int startMinute, int endHour, int endMinute) {

        int currentTotalMinutes = currentHour * 60 + currentMinute;
        int startTotalMinutes = startHour * 60 + startMinute;
        int endTotalMinutes = endHour * 60 + endMinute;

        int minutesRemainingToStart = startTotalMinutes - currentTotalMinutes;

        if (minutesRemainingToStart > 0) {
            return minutesRemainingToStart;
        } else {

            int minutesRemainingToEnd = endTotalMinutes - currentTotalMinutes;

            if (minutesRemainingToEnd < 0) {
                return 1000000;
            }

            return 1000000;
        }
    }
    private void initializeStudentDashboard(String classInfo, String className) {

        ArrayList<String[]> results1 = null;
        try {
            results1 = databaseManager.checkNameInStudentsTables(userName);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        if (results1.isEmpty()) {
            System.out.println("No records found for the student.");
        }

        ArrayList<String[]> results = null;
        try {
            results = databaseManager.checkNameInStudentsTables(userName);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        if (results.isEmpty()) {
            System.out.println("No records found for the student.");
        }
        for (int i = 0; i < results.size(); i++) {
            try {

                LocalTime startTime = LocalTime.parse((String) results.get(i)[2]);
                LocalTime endTime = LocalTime.parse((String) results.get(i)[3]);

                if (LocalTime.now().isAfter(startTime) && LocalTime.now().isBefore(endTime)) {
                    String result = (String) results.get(i)[0];
                    String waitTime = (String) results.get(i)[4];
                    waitTimeOfClass = waitTime;
                    periodNumber = extractNumberFromTableName(result);

                    if (result.matches(".*_\\d+_main")) {
                        questionTableName = result.replace("_main", "_questions");
                    }

                }
            } catch (DateTimeParseException e1) {

                System.out.println("Invalid time format in results: " + e1.getMessage());
            }
        }
        int position = databaseManager.getQuestionPosition(questionTableName, userName);
        positionLabel.setText("Position: " + position);
        if(position==-1)
        {
            positionLabel.setText("Position: N/A");
        }
        waitTimeLabel.setText("Wait Time: " + 0 + " seconds");
        waitTimePanel.setBackground(new Color(144, 238, 144));
        System.out.println("Test: " + waitTimeOfClass);
        String input = databaseManager.getQuestionStudent(questionTableName, userName);
        if(input.equals("")){
            positionLabel.setText("Position: N/A");
            waitTimeLabel.setText("Wait Time: ## seconds");

            rowData[0][0] = "";
            addQuestionButton.setVisible(true);
            removeQuestionButton.setVisible(false);
            String tableString = userName + "_" + "waittime";
            String columnName = "WaitTime_" + periodNumber;
            int loadWaitTime = databaseManager.getWaitTimeFromStudent(tableString,columnName);
            System.out.println("Wait Time Received: " + loadWaitTime);
            if(!(loadWaitTime==0)) {
                addQuestionButton.setEnabled(false);
                if(loadWaitTime<=10){
                    waitTimePanel.setBackground(new Color(144, 238, 144));
                }
                else{
                    waitTimePanel.setBackground(new Color(255, 182, 193));
                }
                startCountdown(waitTimeLabel, loadWaitTime, waitTimePanel, addQuestionButton);
            }
            else{
                addQuestionButton.setEnabled(true);
                waitTimeLabel.setText("Wait Time: 0 seconds");
                waitTimePanel.setBackground(new Color(144, 238, 144));
            }
        }
        else{
            rowData[0][0] = input;
            addQuestionButton.setVisible(false);
            removeQuestionButton.setVisible(true);
            positionLabel.setText("Position: " + databaseManager.getQuestionPosition(questionTableName, userName));
        }

        if(questionTable.getValueAt(0,0).equals("")){
            questionTable.setValueAt("No Active Question", 0, 0);
        }

        ArrayList<String[]> finalResults = results;
        addQuestionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                JPanel panel = new JPanel(new GridBagLayout());
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(5, 10, 5, 10);
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.weightx = 1;

                Font font = new Font("Georgia", Font.PLAIN, 14);

                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.anchor = GridBagConstraints.NORTHWEST;
                panel.add(createStyledLabel("Enter Question Summary:", font), gbc);

                gbc.gridx = 1;
                gbc.gridwidth = 2;
                gbc.fill = GridBagConstraints.BOTH;
                JTextArea questionSummaryArea = new JTextArea(3, 30);
                questionSummaryArea.setFont(font);
                questionSummaryArea.setLineWrap(true);
                questionSummaryArea.setWrapStyleWord(true);
                JScrollPane questionScrollPane = new JScrollPane(questionSummaryArea);
                questionScrollPane.setPreferredSize(new Dimension(400,60));
                questionScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                panel.add(questionScrollPane, gbc);

                gbc.gridwidth = 1;
                gbc.fill = GridBagConstraints.HORIZONTAL;

                gbc.gridx = 0;
                gbc.gridy = 1;
                panel.add(createStyledLabel("Code File:", font), gbc);

                gbc.gridx = 1;
                JButton uploadButton = new JButton("Upload Code File");
                uploadButton.setFont(font);
                uploadButton.setEnabled(false);
                panel.add(uploadButton, gbc);

                gbc.gridy = 2;
                JLabel fileLabel = new JLabel("No file selected");
                fileLabel.setForeground(Color.RED);
                fileLabel.setFont(font);
                fileLabel.setFont(font.deriveFont(Font.BOLD));
                panel.add(fileLabel, gbc);

                final File[] selectedFile = new File[1];
                ArrayList<File> selectedFilesList = new ArrayList<>();
                uploadButton.setText("Upload File");

                gbc.gridx = 0;
                gbc.gridy = 3;
                panel.add(createStyledLabel("Console (Error) Output:", font), gbc);

                gbc.gridx = 1;
                gbc.gridwidth = 2;
                gbc.fill = GridBagConstraints.BOTH;
                JTextArea consoleErrorArea = new JTextArea(5, 30);
                consoleErrorArea.setLineWrap(true);
                consoleErrorArea.setWrapStyleWord(true);
                consoleErrorArea.setFont(font);
                JScrollPane consoleScrollPane = new JScrollPane(consoleErrorArea);
                consoleScrollPane.setPreferredSize(new Dimension(400, 100));
                consoleScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                panel.add(consoleScrollPane, gbc);
                consoleErrorArea.setForeground(Color.RED);
                consoleErrorArea.setEnabled(false);
                consoleErrorArea.setEditable(false);
                consoleErrorArea.setBackground(Color.lightGray);

                questionSummaryArea.setText("You Will Lose Inputted Data Attempting to Add Question Without Question Summary");
                questionSummaryArea.setForeground(Color.GRAY);

                questionSummaryArea.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusGained(FocusEvent e) {

                        if (questionSummaryArea.getText().equals("You Will Lose Inputted Data Attempting to Add Question Without Question Summary")) {
                            questionSummaryArea.setText("");
                            questionSummaryArea.setForeground(Color.BLACK);
                        }
                    }

                    @Override
                    public void focusLost(FocusEvent e) {

                        if (questionSummaryArea.getText().trim().isEmpty()) {
                            questionSummaryArea.setText("You Will Lose Inputted Data Attempting to Add Question Without Question Summary");
                            questionSummaryArea.setForeground(Color.GRAY);
                        }
                    }
                });

                frame.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {

                        if (!questionSummaryArea.contains(e.getPoint())) {
                            System.out.println("Clicked outside the questionSummaryArea");
                            if (questionSummaryArea.getText().trim().isEmpty()) {
                                questionSummaryArea.setText("You Will Lose Inputted Data Attempting to Add Question Without Question Summary");
                                questionSummaryArea.setForeground(Color.RED);
                            }
                        } else {
                            System.out.println("Clicked inside the questionSummaryArea");
                        }
                    }
                });

                questionSummaryArea.getDocument().addDocumentListener(new DocumentListener() {
                    private void updateButtonState() {
                        boolean isEmpty = questionSummaryArea.getText().trim().isEmpty();
                        uploadButton.setEnabled(!isEmpty);
                        consoleErrorArea.setEnabled(!isEmpty);
                        consoleErrorArea.setEditable(true);

                        if (isEmpty) {
                            questionSummaryArea.setForeground(Color.RED);
                        } else {
                            questionSummaryArea.setForeground(Color.BLACK);
                        }
                    }

                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        updateButtonState();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        updateButtonState();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        updateButtonState();
                    }
                });

                uploadButton.addActionListener(e1 -> {
                    JFileChooser fileChooser = new JFileChooser();
                    if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                        File chosenFile = fileChooser.getSelectedFile();
                        selectedFilesList.add(chosenFile);

                        try (BufferedReader reader = new BufferedReader(new FileReader(chosenFile))) {
                            StringBuilder previewText = new StringBuilder("<html><body style='background-color:#1e1e1e; color:#00ff00; font-family:Courier New; padding:10px;'>");
                            String line;
                            int maxLineLength = 0;

                            while ((line = reader.readLine()) != null) {
                                previewText.append("&nbsp;&nbsp;").append(line.replace(" ", "&nbsp;")).append("<br>");
                                maxLineLength = Math.max(maxLineLength, line.length());
                            }
                            previewText.append("</body></html>");

                            JLabel previewLabel = new JLabel(previewText.toString());
                            JScrollPane previewScrollPane = new JScrollPane(previewLabel);
                            previewScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                            previewScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

                            int panelWidth = Math.min(800, maxLineLength * 8 + 50);
                            int panelHeight = 400;

                            previewScrollPane.setPreferredSize(new Dimension(panelWidth, panelHeight));
                            previewScrollPane.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));

                            JPanel previewPanel = new JPanel(new BorderLayout());
                            previewPanel.setBackground(Color.BLACK);
                            previewPanel.add(previewScrollPane, BorderLayout.CENTER);

                            JOptionPane.showMessageDialog(null, previewPanel, "Code Preview", JOptionPane.PLAIN_MESSAGE);

                            successful_1 = true;

                        } catch (IOException ex) {
                            JOptionPane.showMessageDialog(null, "Error reading file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                            successful_1 = false;
                            selectedFilesList.remove(selectedFilesList.size()-1);
                        }

                        if(successful_1) {

                            String fileNames = selectedFilesList.stream()
                                    .map(File::getName)
                                    .collect(Collectors.joining(", "));
                            fileLabel.setText("File Name(s): " + fileNames);

                            uploadButton.setText("Upload Another File");
                            consoleErrorArea.setForeground(Color.BLACK);
                            consoleErrorArea.setEnabled(true);
                            consoleErrorArea.setBackground(Color.WHITE);
                        }
                    }
                });

                int result = JOptionPane.showConfirmDialog(null, panel, "Add Question", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (result == JOptionPane.OK_OPTION) {
                    String questionSummary = questionSummaryArea.getText().trim();
                    String consoleErrorOutput = consoleErrorArea.getText().trim();
                    byte[] fileBytes = null;

                    if (questionSummary.isEmpty() || questionSummary.equals("You Will Lose Inputted Data Attempting to Add Question Without Question Summary")) {

                        JOptionPane.showMessageDialog(null, "Question summary cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);

                        questionSummaryArea.setText(questionSummary);
                        consoleErrorArea.setText(consoleErrorOutput);
                        return;
                    }
                    else{
                        addQuestionButton.setVisible(false);
                        removeQuestionButton.setVisible(true);
                    }

                    try {
                        Pattern pattern1 = Pattern.compile("\\b(\\d+)(st|nd|rd|th)\\b");
                        Matcher matcher1 = pattern1.matcher(className);

                        if (matcher1.find()) {
                            periodNumber = Integer.parseInt(matcher1.group(1));
                            System.out.println("Period Number: " + periodNumber);
                        } else {
                            System.out.println("Period number not found.");
                        }
                        String tableName = "Test";
                        for (int i = 0; i < finalResults.size(); i++) {
                            try {
                                LocalTime startTime = LocalTime.parse(finalResults.get(i)[2]);
                                LocalTime endTime = LocalTime.parse(finalResults.get(i)[3]);

                                if (LocalTime.now().isAfter(startTime) && LocalTime.now().isBefore(endTime)) {
                                    tableName = finalResults.get(i)[0];

                                    if (tableName.matches(".*_\\d+_main")) {
                                        tableName = tableName.replace("_main", "_students");
                                    }
                                }
                            } catch (DateTimeParseException e1) {
                                System.out.println("Invalid time format in results: " + e1.getMessage());
                            }
                        }

                        String nName = databaseManager.getStudentName(userName, tableName);

                        File zipFile = File.createTempFile(nName + "_", ".zip");

                        try (FileOutputStream fos = new FileOutputStream(zipFile);
                             ZipOutputStream zipOut = new ZipOutputStream(fos)) {

                            for (File file : selectedFilesList) {
                                try (FileInputStream fis = new FileInputStream(file)) {
                                    ZipEntry zipEntry = new ZipEntry(file.getName());
                                    zipOut.putNextEntry(zipEntry);

                                    byte[] bytes = new byte[1024];
                                    int length;
                                    while ((length = fis.read(bytes)) >= 0) {
                                        zipOut.write(bytes, 0, length);
                                    }
                                    zipOut.closeEntry();
                                }
                            }
                        }

                        selectedFile[0] = zipFile;

                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null, "Error creating ZIP file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }

                    if (selectedFile[0] != null) {
                        try {
                            fileBytes = Files.readAllBytes(selectedFile[0].toPath());
                        } catch (IOException ex) {
                            JOptionPane.showMessageDialog(null, "Failed to read file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }

                    if (!questionSummary.isEmpty()) {
                        questionTable.setValueAt(questionSummary, 0, 0);
                        System.out.println("Question Added: " + questionSummary);

                        ArrayList<String[]> results = null;
                        try {
                            results = databaseManager.checkNameInStudentsTables(userName);
                        } catch (SQLException ex) {
                            throw new RuntimeException(ex);
                        }

                        if (results.isEmpty()) {
                            System.out.println("No records found for the student.");
                        }

                        for (int i = 0; i < results.size(); i++) {
                            try {
                                LocalTime startTime = LocalTime.parse(results.get(i)[2]);
                                LocalTime endTime = LocalTime.parse(results.get(i)[3]);

                                if (LocalTime.now().isAfter(startTime) && LocalTime.now().isBefore(endTime)) {
                                    String tableName = results.get(i)[0];

                                    if (tableName.matches(".*_\\d+_main")) {
                                        tableName = tableName.replace("_main", "_questions");
                                    }

                                    if (selectedFilesList.isEmpty()) {

                                        DatabaseManager.addRecordToTable(tableName, userName, questionSummary, null, consoleErrorOutput, "No File(s) Attached");
                                        positionLabel.setText("Position: " + databaseManager.getQuestionPosition(tableName, userName));
                                    } else{

                                        DatabaseManager.addRecordToTable(tableName, userName, questionSummary, fileBytes, consoleErrorOutput, selectedFile[0].getName());
                                        positionLabel.setText("Position: " + databaseManager.getQuestionPosition(tableName, userName));
                                    }
                                }
                            } catch (DateTimeParseException e1) {
                                System.out.println("Invalid time format in results: " + e1.getMessage());
                            }
                        }
                    }
                }
            }
        });

        removeQuestionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<String[]> results = null;
                int position1 = 0;
                try {
                    results = databaseManager.checkNameInStudentsTables(userName);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
                if (results.isEmpty()) {
                    System.out.println("No records found for the student.");
                } else {
                    for (int i = 0; i < results.size(); i++) {
                        try {

                            LocalTime startTime = LocalTime.parse((String) results.get(i)[2]);
                            LocalTime endTime = LocalTime.parse((String) results.get(i)[3]);

                            if (LocalTime.now().isAfter(startTime) && LocalTime.now().isBefore(endTime)) {
                                String result = (String) results.get(i)[0];
                                String waitTime = (String) results.get(i)[4];
                                waitTimeOfClass = waitTime;
                                periodNumber = extractNumberFromTableName(result);
                                questionTableName = result.replace("_main", "_questions");

                                position1 = databaseManager.getQuestionPosition(questionTableName, userName);
                            }
                        }catch (DateTimeParseException e1) {
                            System.out.println("Invalid time format in results: " + e1.getMessage());
                        }
                    }
                }
                if(position1 == 1) {
                    JPanel panel = new JPanel();
                    JLabel label = new JLabel("Teacher is Reviewing Your Question Right Now, Cannot Remove Question", JLabel.CENTER);
                    label.setFont(new Font("Georgia", Font.BOLD, 14));
                    panel.add(label);

                    JOptionPane.showMessageDialog(null, panel, "Error", JOptionPane.ERROR_MESSAGE);
                }
                else{
                    String currentQuestion = (String) questionTable.getValueAt(0, 0);
                    if ("No Active Question".equals(currentQuestion)) {

                        JPanel panel = new JPanel();
                        JLabel label = new JLabel("No Question to Remove.", JLabel.CENTER);
                        label.setFont(new Font("Georgia", Font.BOLD, 14));
                        panel.add(label);

                        JOptionPane.showMessageDialog(null, panel, "Error", JOptionPane.ERROR_MESSAGE);
                    } else {

                        ArrayList<String[]> results2 = null;
                        try {
                            results2 = databaseManager.checkNameInStudentsTables(userName);
                        } catch (SQLException ex) {
                            throw new RuntimeException(ex);
                        }
                        if (results2.isEmpty()) {
                            System.out.println("No records found for the student.");
                        }
                        for (int i = 0; i < results2.size(); i++) {
                            try {

                                LocalTime startTime = LocalTime.parse((String) results2.get(i)[2]);
                                LocalTime endTime = LocalTime.parse((String) results2.get(i)[3]);

                                if (LocalTime.now().isAfter(startTime) && LocalTime.now().isBefore(endTime)) {
                                    String result = (String) results2.get(i)[0];

                                    if (result.matches(".*_\\d+_main")) {
                                        result = result.replace("_main", "_questions");
                                    }

                                    databaseManager.updateQuestionsTable(userName, result, "Student Took Back Question", 0);
                                    positionLabel.setText("Position: N/A");
                                }
                            } catch (DateTimeParseException e1) {

                                System.out.println("Invalid time format in results: " + e1.getMessage());
                            }
                        }

                        System.out.println("Remove Question button clicked");

                        questionTable.setValueAt("No Active Question", 0, 0);

                        JPanel panel = new JPanel();
                        JLabel label = new JLabel("Question has been removed.", JLabel.CENTER);
                        System.out.println("Clicked" + position);
                        label.setFont(new Font("Georgia", Font.BOLD, 14));
                        panel.add(label);

                        JOptionPane.showMessageDialog(null, panel, "Success", JOptionPane.INFORMATION_MESSAGE);
                        addQuestionButton.setVisible(true);
                        removeQuestionButton.setVisible(false);
                        startCountdown(waitTimeLabel, Integer.parseInt(waitTimeOfClass), waitTimePanel, addQuestionButton);
                        addQuestionButton.setEnabled(false);
                    }
                }
            }
        });

        revalidate();
        repaint();

        questionTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = questionTable.getSelectedRow();
                    if (selectedRow >= 0) {

                        Color selectionBackground = UIManager.getColor("Table.selectionBackground");
                        questionTable.setSelectionBackground(selectionBackground);
                        questionTable.setSelectionBackground(selectionBackground);
                    } else {

                        questionTable.setSelectionBackground(Color.WHITE);
                    }
                }
            }
        });

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {

                if (questionTable.getSelectedRow() != -1) {
                    questionTable.clearSelection();
                    questionTable.setSelectionBackground(Color.WHITE);
                }
            }
        });

    }

    private static JLabel createStyledLabel(String text, Font font) {
        JLabel label = new JLabel(text);
        label.setFont(font.deriveFont(Font.BOLD));
        return label;
    }

    private void startCountdown(JLabel waitTimeLabel, int startTimeInSeconds, JPanel waitTimePanel, JButton addQuestionButton) {

        countdownTimer = new Timer(1000, new ActionListener() {
            private int remainingTime = startTimeInSeconds;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (remainingTime >= 0) {
                    if (remainingTime > 10) {
                        waitTimePanel.setBackground(new Color(255, 182, 193));
                    }

                    waitTimeLabel.setText("Wait Time: " + remainingTime + " seconds");

                    if (remainingTime == 10) {
                        waitTimePanel.setBackground(new Color(144, 238, 144));
                    }

                    remainingTime--;
                    String tableString = userName + "_" + "waittime";
                    String columnName = "WaitTime_" + periodNumber;
                    databaseManager.insertOrUpdateWaitTime(tableString, columnName, remainingTime);
                } else {

                    ((Timer) e.getSource()).stop();
                    waitTimeLabel.setText("Wait Time: 0 seconds");
                    String tableString = userName + "_" + "waittime";
                    String columnName = "WaitTime_" + periodNumber;
                    databaseManager.insertOrUpdateWaitTime(tableString, columnName, 0);

                    waitTimeLabel.setForeground(Color.BLACK);
                    addQuestionButton.setEnabled(true);
                }
            }
        });

        countdownTimer.start();
    }

    private void stopCountdown() {
        if (countdownTimer != null && countdownTimer.isRunning()) {
            countdownTimer.stop();
        }
    }

    public static String formatClassString(String classInfo, String className) {

        String[] nameClassParts = classInfo.split("_");
        String name = nameClassParts[0];
        int period = Integer.parseInt(nameClassParts[1]);

        String suffix = getPeriodSuffix(period);

        name = capitalize(name);
        className = capitalize(className);

        return String.format("%s' %s Period %s", name, suffix, className);
    }

    private static String getPeriodSuffix(int period) {
        if (10 <= period % 100 && period % 100 <= 20) {
            return "th";
        } else {
            switch (period % 10) {
                case 1: return "1st";
                case 2: return "2nd";
                case 3: return "3rd";
                case 4: return "4th";
                case 5: return "5th";
                case 6: return "6th";
                case 7: return "7th";
                default: return "";
            }
        }
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static int[] splitTime(String timeString) {

        String[] timeParts = timeString.split(":");

        return new int[]{Integer.parseInt(timeParts[0]), Integer.parseInt(timeParts[1])};
    }

    public static int extractNumberFromTableName(String tableName) {

        String[] parts = tableName.split("_");

        if (parts.length >= 3) {
            try {
                return Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                System.out.println("Error: The value between underscores is not a valid number.");
                return -1;
            }
        } else {
            System.out.println("Invalid table name format: " + tableName);
            return -1;
        }
    }
}
