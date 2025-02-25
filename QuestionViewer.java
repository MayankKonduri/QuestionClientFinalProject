import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuestionViewer extends JPanel {
    private JFrame frame;
    private String userName;
    private JLabel titleLabel;
    private JLabel pageLabel;
    private JPanel contentPanel;
    private JTable questionTable;
    private DefaultTableModel tableModel;
    private String[] classPeriods;
    public int periodNumber;
    private int currentIndex = 0;
    private String teacherName;
    private JButton removeQuestionButton = new JButton();
    private JButton clearQuestionListButton = new JButton();
    private DatabaseManager databaseManager = new DatabaseManager(userName);
    private Thread refreshThread;
    private volatile boolean running = true;
    String studentName;

    public QuestionViewer(JFrame frame, String userName) {
        this.frame = frame;
        this.userName = userName;
        this.classPeriods = new String[7];
        this.databaseManager = new DatabaseManager(userName);

        setLayout(null);

        JLabel panelTitle = new JLabel("Questions-Viewer");
        panelTitle.setFont(new Font("Georgia", Font.BOLD, 18));
        panelTitle.setHorizontalAlignment(SwingConstants.CENTER);
        panelTitle.setBounds(50, 20, 300, 30);
        add(panelTitle);

        JButton homeButton = new JButton("Home");
        homeButton.setFont(new Font("Georgia", Font.BOLD, 10));
        homeButton.setBounds(15, 10, 65, 20);
        homeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopAutoRefreshThread();
                frame.getContentPane().removeAll();
                frame.getContentPane().add(new TeacherHome(frame, userName));
                frame.revalidate();
                frame.repaint();
                frame.setSize(400,325);
            }
        });
        add(homeButton);

        JPanel titleBarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(200, 200, 200));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        titleBarPanel.setLayout(null);
        titleBarPanel.setBounds(50, 60, 300, 35);
        titleBarPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        add(titleBarPanel);

        titleLabel = new JLabel("Loading...");
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 14));
        titleLabel.setBounds(0, 7, 310, 20);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleBarPanel.add(titleLabel);

        JButton prevButton = new JButton("<");
        JButton nextButton = new JButton(">");
        pageLabel = new JLabel("1");
        prevButton.setFont(new Font("Georgia", Font.BOLD, 16));
        nextButton.setFont(new Font("Georgia", Font.BOLD, 16));

        prevButton.setBounds(5, 8, 50, 20);
        nextButton.setBounds(245, 8, 50, 20);
        titleBarPanel.add(prevButton);
        titleBarPanel.add(nextButton);

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBounds(50, 105, 300, 200);
        contentPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        add(contentPanel);

        tableModel = new DefaultTableModel(new String[]{"Student ID", "Question Summary"}, 0);
        questionTable = new JTable(tableModel);
        questionTable.setFont(new Font("Georgia", Font.PLAIN, 10));
        questionTable.setRowHeight(30);
        questionTable.getTableHeader().setFont(new Font("Georgia", Font.BOLD, 14));
        questionTable.setDefaultEditor(Object.class, null);
        contentPanel.add(new JScrollPane(questionTable), BorderLayout.CENTER);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        for (int i = 0; i < questionTable.getColumnCount(); i++) {
            questionTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        questionTable.setDefaultEditor(Object.class, null);

        JScrollPane scrollPane = new JScrollPane(questionTable);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        removeQuestionButton.setText("Remove Question");
        removeQuestionButton.setFont(new Font("Georgia", Font.PLAIN, 12));
        removeQuestionButton.setBounds(50,310, 300,30);
        add(removeQuestionButton);
        removeQuestionButton.setVisible(false);

        removeQuestionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = questionTable.getSelectedRow();
                if (selectedRow != -1) {
                    String studentID = tableModel.getValueAt(selectedRow, 0).toString();
                    String questionSummary = tableModel.getValueAt(selectedRow, 1).toString();

                    System.out.println("Remove Requested: " + questionSummary + " " + titleLabel.getText() + " " + teacherName);

                    Pattern pattern = Pattern.compile("\\b(\\d+)(st|nd|rd|th)\\b");
                    Matcher matcher = pattern.matcher(titleLabel.getText());

                    if (matcher.find()) {
                        periodNumber = Integer.parseInt(matcher.group(1));
                        System.out.println("Period Number: " + periodNumber);
                    } else {
                        System.out.println("Period number not found.");
                    }
                    String tableName = teacherName + "_" + periodNumber + "_questions";

                    removeQuestionButton.setVisible(false);
                    clearQuestionListButton.setVisible(true);
                    questionTable.clearSelection();
                    String tableName4 = teacherName + "_" + periodNumber + "_questions";
                    databaseManager.updateQuestionsTable(studentID, tableName4, "Teacher Manually Removed Question");
                    loadTeacherAndClasses();
                }
            }
        });

        clearQuestionListButton.setText("Clear List");
        clearQuestionListButton.setFont(new Font("Georgia", Font.PLAIN, 12));
        clearQuestionListButton.setBounds(50,310, 300,30);
        add(clearQuestionListButton);
        clearQuestionListButton.setVisible(true);

        clearQuestionListButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Pattern pattern = Pattern.compile("\\b(\\d+)(st|nd|rd|th)\\b");
                Matcher matcher = pattern.matcher(titleLabel.getText());

                if (matcher.find()) {
                    periodNumber = Integer.parseInt(matcher.group(1));
                    System.out.println("Period Number: " + periodNumber);
                } else {
                    System.out.println("Period number not found.");
                }
                String tableName1 = teacherName + "_" + periodNumber + "_questions";
                databaseManager.clearQuestionsList(tableName1);
                String tableName4 = teacherName + "_" + periodNumber + "_questions";
                loadTeacherAndClasses();
            }

        });

        questionTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = questionTable.getSelectedRow();
            if (selectedRow != -1) {
                String studentID = tableModel.getValueAt(selectedRow, 0).toString();
                String questionSummary = tableModel.getValueAt(selectedRow, 1).toString();
                System.out.println(titleLabel.getText() + " | " + studentID + " | " + questionSummary);

                contentPanel.setBounds(50, 105, 300, 200);
                removeQuestionButton.setVisible(true);
                clearQuestionListButton.setVisible(false);
            }
        });

        questionTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = questionTable.getSelectedRow();
                    if (row != -1 && row == 0) {
                        String studentID = tableModel.getValueAt(row, 0).toString();
                        String questionSummary = tableModel.getValueAt(row, 1).toString();
                        String consoleOutput = "";
                        String FileName = "";
                        Pattern pattern = Pattern.compile("\\b(\\d+)(st|nd|rd|th)\\b");
                        Matcher matcher = pattern.matcher(titleLabel.getText());
                        if (matcher.find()) {
                            periodNumber = Integer.parseInt(matcher.group(1));
                            System.out.println("Period Number: " + periodNumber);
                        } else {
                            System.out.println("Period number not found.");
                        }
                        String tableName5 = teacherName + "_" + periodNumber + "_questions";

                        Object[] studentInputValues = databaseManager.getQuestionDetails(studentID, tableName5);
                        questionSummary = (String) studentInputValues[0];
                        FileName = (String) studentInputValues[1];
                        if(FileName.equals("No File(s) Attached")) {
                            System.out.println("No Filesss");
                            Pattern pattern1 = Pattern.compile("\\b(\\d+)(st|nd|rd|th)\\b");
                            Matcher matcher1 = pattern1.matcher(titleLabel.getText());

                            if (matcher1.find()) {
                                periodNumber = Integer.parseInt(matcher1.group(1));
                                System.out.println("Period Number: " + periodNumber);
                            } else {
                                System.out.println("Period number not found.");
                            }
                            String tableName2 = teacherName + "_" + periodNumber + "_students";
                            studentName = databaseManager.getStudentName(studentID, tableName2);
                            String tableName3 = teacherName + "_" + periodNumber + "_questions";

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

                                doc.insertString(doc.getLength(), "Student ID: ", boldStyle);
                                doc.insertString(doc.getLength(), studentID + "\n", regularStyle);

                                doc.insertString(doc.getLength(), "Nickname: ", boldStyle);
                                doc.insertString(doc.getLength(), studentName + "\n", regularStyle);

                                doc.insertString(doc.getLength(), "Question Summary: ", boldStyle);
                                doc.insertString(doc.getLength(), questionSummary, regularStyle);

                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }

                            JButton comingOverButton = new JButton("Coming Over");
                            JButton sendResponseButton = new JButton("Send Response");
                            JButton cancelButton = new JButton("Cancel");

                            Font buttonFont = new Font("Georgia", Font.PLAIN, 12);
                            comingOverButton.setFont(buttonFont);
                            sendResponseButton.setFont(buttonFont);
                            cancelButton.setFont(buttonFont);

                            JPanel buttonPanel = new JPanel();
                            buttonPanel.add(comingOverButton);
                            buttonPanel.add(sendResponseButton);
                            buttonPanel.add(cancelButton);

                            comingOverButton.addActionListener(e1 -> {
                                System.out.println("Coming Over selected");
                                databaseManager.updateQuestionsTable(studentID, tableName3, "Went to Student's Desk");
                                loadTeacherAndClasses();
                                SwingUtilities.getWindowAncestor(cancelButton).dispose();

                            });

                            sendResponseButton.addActionListener(e1 -> {
                                System.out.println("Send Response selected");

                                JTextField responseField = new JTextField(20);
                                responseField.setFont(new Font("Georgia", Font.PLAIN, 12));

                                JLabel promptLabel = new JLabel("Type your response:");
                                promptLabel.setFont(new Font("Georgia", Font.PLAIN, 12));

                                JPanel panel = new JPanel();
                                panel.add(promptLabel);
                                panel.add(responseField);

                                int option = JOptionPane.showConfirmDialog(frame, panel, "Enter Response", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                                if (option == JOptionPane.OK_OPTION) {

                                    String userResponse = responseField.getText();

                                    System.out.println("User response: " + userResponse);

                                    databaseManager.updateQuestionsTable(studentID, tableName3, userResponse);
                                    loadTeacherAndClasses();
                                    SwingUtilities.getWindowAncestor(cancelButton).dispose();
                                } else {
                                    System.out.println("Response input was canceled.");
                                }
                            });

                            cancelButton.addActionListener(e1 -> {
                                System.out.println("Cancel selected");
                                SwingUtilities.getWindowAncestor(cancelButton).dispose();
                            });

                            JOptionPane.showOptionDialog(
                                    frame,
                                    new JScrollPane(textPane),
                                    "Question Details",
                                    JOptionPane.DEFAULT_OPTION,
                                    JOptionPane.INFORMATION_MESSAGE,
                                    null,
                                    new Object[]{comingOverButton, sendResponseButton, cancelButton},
                                    null
                            );

                            questionTable.clearSelection();

                            contentPanel.setBounds(50, 105, 300, 200);
                            removeQuestionButton.setVisible(false);
                            clearQuestionListButton.setVisible(true);
                        }
                        else{
                            byte[] fileData = (byte[]) studentInputValues[2];
                            consoleOutput = (String) studentInputValues[3];

                            System.out.println("Question Summary: " + questionSummary);
                            System.out.println("Console Output: " + consoleOutput);
                            System.out.println("File Name: " + FileName);

                            if (!(questionSummary.equals("No Active Questions"))) {
                                Pattern pattern1 = Pattern.compile("\\b(\\d+)(st|nd|rd|th)\\b");
                                Matcher matcher1 = pattern1.matcher(titleLabel.getText());

                                if (matcher1.find()) {
                                    periodNumber = Integer.parseInt(matcher1.group(1));
                                    System.out.println("Period Number: " + periodNumber);
                                } else {
                                    System.out.println("Period number not found.");
                                }
                                String tableName2 = teacherName + "_" + periodNumber + "_students";
                                studentName = databaseManager.getStudentName(studentID, tableName2);
                                String tableName3 = teacherName + "_" + periodNumber + "_questions";

                                stopAutoRefreshThread();
                                frame.getContentPane().removeAll();
                                frame.getContentPane().add(new CodeViewer(frame, userName, studentID, studentName, questionSummary, consoleOutput, FileName, fileData, tableName3));
                                frame.revalidate();
                                frame.repaint();
                                frame.setSize(750,675);
                            }

                        }
                    }
                }
            }
        });

        prevButton.addActionListener(e -> navigate(false));
        nextButton.addActionListener(e -> navigate(true));

        loadTeacherAndClasses();
        startAutoRefreshThread();

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                if (!questionTable.getBounds().contains(e.getPoint())) {
                    questionTable.clearSelection();

                    contentPanel.setBounds(50, 105, 300, 200);
                    removeQuestionButton.setVisible(false);
                    clearQuestionListButton.setVisible(true);

                }
            }
        });
    }

    private void startAutoRefreshThread() {
        refreshThread = new Thread(() -> {
            while (running) {
                try {
                    loadTeacherAndClasses();
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

    private void loadTeacherAndClasses() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://10.195.75.116/qclient1", "root", "password")) {
            PreparedStatement stmt = conn.prepareStatement("SELECT teacher_name FROM teacher WHERE teacher_id = ?");
            stmt.setString(1, userName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) teacherName = rs.getString("teacher_name");
            teacherName = teacherName.replaceAll("^(Mr\\.\\s*|Ms\\.\\s*|Mrs\\.\\s*)", "").trim();

            for (int i = 1; i <= 7; i++) {
                stmt = conn.prepareStatement("SELECT ClassName FROM " + teacherName + "_" + i + "_Main");
                rs = stmt.executeQuery();
                if(i==1) {
                    classPeriods[i - 1] = rs.next() ? i + "st Period " + rs.getString("ClassName") : "N/A";
                }
                else if (i==2){
                    classPeriods[i - 1] = rs.next() ? i + "nd Period " + rs.getString("ClassName") : "N/A";
                } else if (i==3){
                    classPeriods[i - 1] = rs.next() ? i + "rd Period " + rs.getString("ClassName") : "N/A";
                } else{
                    classPeriods[i - 1] = rs.next() ? i + "th Period " + rs.getString("ClassName") : "N/A";
                }

            }

            updateTitle();
            loadQuestionsForCurrentPeriod(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateTitle() {
        while (classPeriods[currentIndex].equals("N/A")) {
            currentIndex = (currentIndex + 1) % 7;
        }
        titleLabel.setText(classPeriods[currentIndex]);
    }

    private void navigate(boolean forward) {
        questionTable.clearSelection();
        removeQuestionButton.setVisible(false);
        clearQuestionListButton.setVisible(true);
        int step = forward ? 1 : -1;
        do {
            currentIndex = (currentIndex + step + 7) % 7;
        } while (classPeriods[currentIndex].equals("N/A"));

        titleLabel.setText(classPeriods[currentIndex]);
        loadQuestionsForCurrentPeriod(null);
    }

    private void loadQuestionsForCurrentPeriod(Connection existingConn) {
        try (Connection conn = existingConn != null ? existingConn :
                DriverManager.getConnection("jdbc:mysql://10.195.75.116/qclient1", "root", "password")) {

            String period = classPeriods[currentIndex].split(" ")[0];
            PreparedStatement stmt = conn.prepareStatement("SELECT StudentID, QuestionSummary FROM " +
                    teacherName + "_" + period.charAt(0) + "_questions WHERE IsQuestionActive = 1 ORDER BY TimeStamp ASC");

            ResultSet rs = stmt.executeQuery();
            tableModel.setRowCount(0);

            boolean hasData = false;
            while (rs.next()) {
                tableModel.addRow(new Object[]{rs.getString("StudentID"), rs.getString("QuestionSummary")});
                hasData = true;
            }

            if (!hasData) {
                tableModel.addRow(new Object[]{"N/A", "No Active Questions"});
                questionTable.setForeground(Color.RED);
                questionTable.setFont(new Font("Georgia", Font.BOLD, 12));
            } else {
                questionTable.setForeground(Color.BLACK);
                questionTable.setFont(new Font("Georgia", Font.PLAIN, 12));
                DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
                centerRenderer.setHorizontalAlignment(JLabel.CENTER);
                questionTable.setDefaultRenderer(Object.class, centerRenderer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
