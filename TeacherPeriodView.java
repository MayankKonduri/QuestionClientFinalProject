import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TeacherPeriodView extends JPanel {
    public JFrame frame;
    public String period;
    public JButton addButton = new JButton("Add");
    public JButton importButton = new JButton("Import");
    public JButton removeButton = new JButton("Remove");
    public JButton editButton = new JButton("Edit");
    public JButton infoButton = new JButton("Info");
    DatabaseManager dbManager;
    String userName; // = System.getProperty("user.name");
    JTextField classNameField;
    JFormattedTextField startTimeField;
    JFormattedTextField endTimeField;
    JTable studentTable = new JTable();
    DefaultTableModel tableModel;

    String studentId;
    String firstName;
    String lastName;
    String nickname;
    public TeacherPeriodView(JFrame jFrame, int period, String userName) {

        this.userName = userName;
        this.frame = jFrame;
        dbManager = new DatabaseManager(userName);
        this.period = String.valueOf(period);
        setLayout(null);

        JPanel homePanel = new JPanel();
        homePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

        JButton homeButton = new JButton("Home");
        homeButton.setFont(new Font("Georgia", Font.BOLD, 10));
        homePanel.add(homeButton);

        homePanel.setBounds(10, 10, 100, 40);
        add(homePanel);

        homeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                boolean isClassNameEmpty = classNameField.getText().isEmpty();
                boolean isStartTimeEmpty = startTimeField.getText().trim().isEmpty() || startTimeField.getText().equals("  :  ");
                boolean isEndTimeEmpty = endTimeField.getText().trim().isEmpty() || endTimeField.getText().equals("  :  ");
                boolean isTableEmpty = tableModel.getRowCount() == 0;

                System.out.println(isClassNameEmpty);
                System.out.println(isStartTimeEmpty);
                System.out.println(isEndTimeEmpty);
                System.out.println(isTableEmpty);

                if ((isClassNameEmpty && isStartTimeEmpty && isEndTimeEmpty && isTableEmpty) ||
                        (!isClassNameEmpty && !isStartTimeEmpty && !isEndTimeEmpty && !isTableEmpty)) {

                    TeacherCourses teacherCourses = new TeacherCourses(frame, userName);
                    frame.getContentPane().removeAll();
                    frame.getContentPane().add(teacherCourses);
                    frame.revalidate();
                    frame.repaint();
                    System.out.println("New page loaded.");
                } else {

                    if (!isClassNameEmpty && !isStartTimeEmpty && !isEndTimeEmpty) {

                        TeacherCourses teacherCourses = new TeacherCourses(frame, userName);
                        frame.getContentPane().removeAll();
                        frame.getContentPane().add(teacherCourses);
                        frame.revalidate();
                        frame.repaint();
                        System.out.println("New page loaded.");
                    } else if (!isTableEmpty && !isClassNameEmpty && !isStartTimeEmpty && !isEndTimeEmpty) {

                        TeacherCourses teacherCourses = new TeacherCourses(frame, userName);
                        frame.getContentPane().removeAll();
                        frame.getContentPane().add(teacherCourses);
                        frame.revalidate();
                        frame.repaint();
                        System.out.println("New page loaded.");
                    } else {

                        System.out.println("Cannot proceed to new page. Fields are not valid.");
                    }
                }
            }
        });

        JLabel titleLabel = new JLabel("Period " + period + " - Class Name:");
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 12));
        titleLabel.setBounds(110, 25, 200, 30);
        add(titleLabel);

        classNameField = new JTextField(15);
        classNameField.setFont(new Font("Georgia", Font.BOLD, 8));
        classNameField.setBounds(260, 30, 95, 25);
        add(classNameField);

        JLabel durationLabel = new JLabel("Duration:");
        durationLabel.setFont(new Font("Georgia", Font.PLAIN, 12));
        durationLabel.setBounds(80, 60, 60, 25);
        add(durationLabel);

        startTimeField = createTimeField();
        startTimeField.setBounds(150, 60, 60, 25);
        add(startTimeField);

        JLabel toLabel = new JLabel("to");
        toLabel.setFont(new Font("Georgia", Font.PLAIN, 12));
        toLabel.setBounds(220, 60, 20, 25);
        add(toLabel);

        endTimeField = createTimeField();
        endTimeField.setBounds(250, 60, 60, 25);
        add(endTimeField);

        tableModel = new DefaultTableModel(new Object[]{"Student ID", "Nickname"}, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        studentTable.setModel(tableModel);

        studentTable.setFont(new Font("Georgia", Font.PLAIN, 12));

        studentTable.setRowHeight(20);

        studentTable.getTableHeader().setFont(new Font("Georgia", Font.BOLD, 12));
        studentTable.getTableHeader().setBackground(Color.LIGHT_GRAY);
        studentTable.getTableHeader().setForeground(Color.BLACK);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        studentTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        studentTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

        JScrollPane tableScrollPane = new JScrollPane(studentTable);
        tableScrollPane.setBounds(55, 100, 300, 125);
        add(tableScrollPane);

        String[] temp = dbManager.getTeacher(userName);
        String teacherName = temp[1];
        teacherName = teacherName.replaceAll("^(Mr\\.\\s*|Ms\\.\\s*|Mrs\\.\\s*)", "").trim();
        String studentTableName = teacherName + "_" + period + "_Students";

        try {

            String[][] students = dbManager.getTeacherStudents(studentTableName);

            if (students != null) {
                for (String[] student : students) {

                    String studentId = student[0];
                    System.out.println("Type" + student[3]);

                    tableModel.addRow(new Object[]{studentId, student[3]});
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Error fetching students: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        addButton.setFont(new Font("Georgia", Font.PLAIN, 12));
        addButton.setBounds(55, 240, 145, 30);
        add(addButton);

        importButton.setFont(new Font("Georgia", Font.PLAIN, 12));
        importButton.setBounds(210, 240, 145, 30);
        add(importButton);

        infoButton.setFont(new Font("Georgia", Font.PLAIN, 12));
        infoButton.setBounds(55, 240, 145, 30);
        add(infoButton);

        removeButton.setFont(new Font("Georgia", Font.PLAIN, 12));
        removeButton.setBounds(210, 240, 145, 30);
        removeButton.setVisible(false);
        add(removeButton);

        studentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                if (studentTable.getSelectedRow() == -1) {

                    addButton.setVisible(true);
                    importButton.setVisible(true);
                    editButton.setVisible(false);
                    removeButton.setVisible(false);
                } else {

                    addButton.setVisible(false);
                    importButton.setVisible(false);
                    infoButton.setVisible(true);
                    removeButton.setVisible(true);

                    int selectedRow = studentTable.getSelectedRow();
                    String studentId = (String) studentTable.getValueAt(selectedRow, 0);

                    for (ActionListener al : infoButton.getActionListeners()) {
                        infoButton.removeActionListener(al);
                    }

                    infoButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {

                            infoButtonPopUp(studentId, studentTableName);
                            studentTable.clearSelection();
                        }
                    });
                }
            }
        });

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String[] temp1 = dbManager.getTeacher(userName);
                String teacherName = temp1[1];
                teacherName = teacherName.replaceAll("^(Mr\\.\\s*|Ms\\.\\s*|Mrs\\.\\s*)", "").trim();
                String mainTable = teacherName + "_" + period + "_Students";

                JPanel panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

                Font georgiaFont = new Font("Georgia", Font.PLAIN, 12);
                Font boldGeorgiaFont = new Font("Georgia", Font.BOLD, 12);

                JLabel studentIdLabel = new JLabel("Enter Student ID:");
                studentIdLabel.setFont(boldGeorgiaFont);
                JTextField studentIdField = new JTextField(20);
                studentIdField.setFont(georgiaFont);

                JLabel firstNameLabel = new JLabel("Enter First Name:");
                firstNameLabel.setFont(boldGeorgiaFont);
                JTextField firstNameField = new JTextField(20);
                firstNameField.setFont(georgiaFont);

                JLabel lastNameLabel = new JLabel("Enter Last Name:");
                lastNameLabel.setFont(boldGeorgiaFont);
                JTextField lastNameField = new JTextField(20);
                lastNameField.setFont(georgiaFont);

                JLabel nicknameLabel = new JLabel("Enter Nickname:");
                nicknameLabel.setFont(boldGeorgiaFont);
                JTextField nicknameField = new JTextField(20);
                nicknameField.setFont(georgiaFont);

                panel.add(studentIdLabel);
                panel.add(studentIdField);
                panel.add(firstNameLabel);
                panel.add(firstNameField);
                panel.add(lastNameLabel);
                panel.add(lastNameField);
                panel.add(nicknameLabel);
                panel.add(nicknameField);

                int option = JOptionPane.showConfirmDialog(frame, panel, "Add Student", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (option == JOptionPane.OK_OPTION) {

                    String studentId = studentIdField.getText().trim();
                    String firstName = firstNameField.getText().trim();
                    String lastName = lastNameField.getText().trim();
                    String nickname = nicknameField.getText().trim();

                    if (!studentId.isEmpty() && !firstName.isEmpty() && !lastName.isEmpty() && !nickname.isEmpty()) {
                        try {

                            Boolean doubleStudent = dbManager.checkValueInTable(mainTable, studentId);
                            if (!doubleStudent) {

                                tableModel.addRow(new Object[]{studentId, nickname});

                                try {
                                    dbManager.updateTeacherStudents(mainTable, studentId, firstName, lastName, nickname);
                                } catch (SQLException ex) {
                                    throw new RuntimeException("Error inserting student into database", ex);
                                }
                            } else {

                                JOptionPane.showMessageDialog(frame, "Student ID Already Exists in Table", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (SQLException ex) {
                            throw new RuntimeException("Error checking student in database", ex);
                        }
                    } else {

                        JOptionPane.showMessageDialog(frame, "All fields must be filled.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        importButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<String[]> studentsImported = importStudents();

                for(String[] t :studentsImported){
                    tableModel.addRow(new Object[]{t[0].trim(), t[3].trim()});
                    String[] temp1 = dbManager.getTeacher(userName);
                    String teacherName = temp1[1];
                    teacherName = teacherName.replaceAll("^(Mr\\.\\s*|Ms\\.\\s*|Mrs\\.\\s*)", "").trim();
                    String mainTable = teacherName + "_" + period + "_Students";
                    try {
                        dbManager.updateTeacherStudents(mainTable, t[0].trim(), t[1].trim(), t[2].trim(), t[3].trim());
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });

        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = studentTable.getSelectedRow();
                if (selectedRow != -1) {
                    String studentId = studentTable.getValueAt(selectedRow,0).toString().trim();
                    tableModel.removeRow(selectedRow);
                    toggleButtonVisibility(false);
                    String[] temp2 = dbManager.getTeacher(userName);
                    String teacherName = temp2[1];
                    teacherName = teacherName.replaceAll("^(Mr\\.\\s*|Ms\\.\\s*|Mrs\\.\\s*)", "").trim();
                    String mainTable1 = teacherName + "_" + period + "_Students";
                    try {
                        dbManager.removeTeacherStudents(mainTable1, studentId);
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });

        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (!studentTable.getBounds().contains(evt.getPoint())) {
                    studentTable.clearSelection();
                    addButton.setVisible(true);
                    importButton.setVisible(true);
                    editButton.setVisible(false);
                    removeButton.setVisible(false);
                }
            }
        });

        String[] temp3 = dbManager.getTeacher(userName);
        String teacherName3 = temp3[1];
        teacherName3 = teacherName3.replaceAll("^(Mr\\.\\s*|Ms\\.\\s*|Mrs\\.\\s*)", "").trim();
        String mainTable = teacherName3 + "_" + period + "_Main";

        String[] mainData = dbManager.getTeacherMainData(mainTable);
        if (mainData != null) {
            classNameField.setText(mainData[0]);
            startTimeField.setText(mainData[1]);
            endTimeField.setText(mainData[2]);
            classNameField.setCaretPosition(0);
        }

        addUpdateListeners(classNameField, startTimeField, endTimeField, mainTable);
    }

    private void infoButtonPopUp(String studentId, String studentTableName) {
        try {

            String[][] students = dbManager.getTeacherStudents(studentTableName);

            if (students != null) {
                for (String[] student : students) {

                    String studentId1 = student[0];
                    if (studentId1.equals(studentId)) {

                        String lastName = student[2];
                        String firstName = student[1];
                        String nickname = student[3];

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

                        doc.insertString(doc.getLength(), "ID: ", boldStyle);
                        doc.insertString(doc.getLength(), studentId1 + "\n", regularStyle);

                        doc.insertString(doc.getLength(), "Full Name: ", boldStyle);
                        doc.insertString(doc.getLength(), lastName + ", " + firstName + "\n", regularStyle);

                        doc.insertString(doc.getLength(), "Nickname: ", boldStyle);
                        doc.insertString(doc.getLength(), nickname, regularStyle);

                        JOptionPane.showMessageDialog(frame, new JScrollPane(textPane), "Student Info", JOptionPane.INFORMATION_MESSAGE);

                        studentTable.clearSelection();

                        return;
                    }
                }

                JOptionPane.showMessageDialog(frame, "Student ID not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException | BadLocationException ex) {
            JOptionPane.showMessageDialog(frame, "Error fetching students: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void toggleButtonVisibility(boolean rowSelected) {
        if (rowSelected) {
            addButton.setVisible(false);
            importButton.setVisible(false);
            editButton.setVisible(true);
            removeButton.setVisible(true);
        } else {
            addButton.setVisible(true);
            importButton.setVisible(true);
            editButton.setVisible(false);
            removeButton.setVisible(false);
        }
    }

    private JFormattedTextField createTimeField() {
        JFormattedTextField timeField = null;
        try {
            timeField = new JFormattedTextField(new javax.swing.text.MaskFormatter("##:##"));
            timeField.setFont(new Font("Georgia", Font.PLAIN, 12));
            timeField.setHorizontalAlignment(JTextField.CENTER);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timeField;
    }

    private void addUpdateListeners(JTextField classNameField, JFormattedTextField startTimeField, JFormattedTextField endTimeField, String tableName) {
        classNameField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                dbManager.updateTeacherMain(tableName, classNameField.getText(), startTimeField.getText(), endTimeField.getText());
            }
        });

        startTimeField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String startTime = startTimeField.getText().trim();
                String endTime = endTimeField.getText().trim();

                if (!startTime.isEmpty() && !startTime.equals("  :  ") && !endTime.isEmpty() && !endTime.equals("  :  ")) {

                    if (isStartTimeAfterEndTime(startTime, endTime)) {

                        JOptionPane.showMessageDialog(null, "Start Time cannot be after End Time.", "Time Error", JOptionPane.ERROR_MESSAGE);
                        startTimeField.setText("");
                    } else {

                        dbManager.updateTeacherMain(tableName, classNameField.getText(), startTime, endTime);
                    }
                } else {
                    System.out.println("Start Time or End Time is empty or invalid. Not updating.");
                }
            }
        });

        endTimeField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String startTime = startTimeField.getText().trim();
                String endTime = endTimeField.getText().trim();

                if (!startTime.isEmpty() && !startTime.equals("  :  ") && !endTime.isEmpty() && !endTime.equals("  :  ")) {

                    if (isStartTimeAfterEndTime(startTime, endTime)) {

                        JOptionPane.showMessageDialog(null, "Start Time cannot be after End Time, Values Have Been Erased Try Again", "Time Error", JOptionPane.ERROR_MESSAGE);
                        endTimeField.setText("");
                    } else {

                        dbManager.updateTeacherMain(tableName, classNameField.getText(), startTime, endTime);
                    }
                } else {
                    System.out.println("Start Time or End Time is empty or invalid. Not updating.");
                }
            }
        });
    }

    private boolean isStartTimeAfterEndTime(String startTime, String endTime) {
        try {

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime start = LocalTime.parse(startTime, formatter);
            LocalTime end = LocalTime.parse(endTime, formatter);

            return start.isAfter(end);
        } catch (Exception e) {

            return false;
        }
    }

    private ArrayList<String[]> importStudents() {
        ArrayList<String[]> tempStudents = new ArrayList<>();
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a CSV File");

        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory() || file.getName().toLowerCase().endsWith(".csv");
            }

            @Override
            public String getDescription() {
                return "CSV Files (.csv)";
            }
        });

        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            if (selectedFile.getName().toLowerCase().endsWith(".csv")) {
                try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
                    List<List<String>> rows = new ArrayList<>();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] values = line.split(",");
                        rows.add(Arrays.asList(values));
                    }

                    List<Integer> selectedColumns = showColumnSelectionDialog(rows);

                    if (selectedColumns != null) {
                        boolean previewConfirmed = showPreviewDialog(rows, selectedColumns);
                        if (previewConfirmed) {
                            for (List<String> row : rows) {
                                List<String> selectedRow = new ArrayList<>();
                                for (int colIndex : selectedColumns) {
                                    if (colIndex < row.size()) {
                                        selectedRow.add(row.get(colIndex));
                                    }
                                }
                                tempStudents.add(selectedRow.toArray(new String[0]));
                            }
                            return tempStudents;
                        } else {

                            return importStudents();
                        }
                    }

                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "Error reading the file: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Please select a valid CSV file.",
                        "Invalid File", JOptionPane.ERROR_MESSAGE);
            }
        }
        return tempStudents;
    }

    private boolean showPreviewDialog(List<List<String>> rows, List<Integer> selectedColumns) {
        List<String[]> previewData = new ArrayList<>();
        int maxRows = Math.min(3, rows.size());
        for (int i = 0; i < maxRows; i++) {
            List<String> row = rows.get(i);
            String[] previewRow = new String[2];
            previewRow[0] = row.size() > 0 ? row.get(0) : "";
            previewRow[1] = row.size() > 3 ? row.get(3) : "";
            previewData.add(previewRow);
        }

        if (rows.size() > 3) {
            String[] previewRow = new String[2];
            previewRow[0] = "...";
            previewRow[1] = "...";
            previewData.add(previewRow);
        }

        String[] columnHeaders = {"Student ID", "Nickname"};
        JTable previewTable = new JTable(previewData.toArray(new String[0][]), columnHeaders);
        previewTable.setPreferredScrollableViewportSize(new java.awt.Dimension(400, 64));
        previewTable.setFillsViewportHeight(true);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        previewTable.setDefaultRenderer(Object.class, centerRenderer);
        previewTable.getTableHeader().setFont(new Font("Georgia", Font.BOLD, 12));
        previewTable.setFont(new Font("Georgia", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(previewTable);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(scrollPane);

        panel.add(Box.createVerticalStrut(10));

        JLabel disclaimer = new JLabel("<html><font color='red' face='Georgia'>Disclaimer: First Name and Last Name can be found using the 'Info' button.</font></html>");
        panel.add(disclaimer);

        int option = JOptionPane.showConfirmDialog(null, panel, "Preview the Data", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.CANCEL_OPTION) {

            return false;
        }

        return option == JOptionPane.OK_OPTION;
    }

    private List<Integer> showColumnSelectionDialog(List<List<String>> rows) {
        List<String> columnHeaders = rows.get(0);

        JCheckBox[] checkBoxes = new JCheckBox[columnHeaders.size()];
        for (int i = 0; i < columnHeaders.size(); i++) {
            checkBoxes[i] = new JCheckBox(columnHeaders.get(i));
        }

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        for (JCheckBox checkBox : checkBoxes) {
            panel.add(checkBox);
        }

        List<Integer> selectedColumns = null;

        while (selectedColumns == null || selectedColumns.size() != 4) {
            int option = JOptionPane.showConfirmDialog(null, panel, "Select Exactly 4 Columns to Import", JOptionPane.OK_CANCEL_OPTION);

            if (option == JOptionPane.OK_OPTION) {
                selectedColumns = new ArrayList<>();
                for (int i = 0; i < checkBoxes.length; i++) {
                    if (checkBoxes[i].isSelected()) {
                        selectedColumns.add(i);
                    }
                }

                if (selectedColumns.size() != 4) {
                    JOptionPane.showMessageDialog(null, "Please select exactly 4 columns.", "Invalid Selection", JOptionPane.ERROR_MESSAGE);
                }
            } else {

                return null;
            }
        }
        return selectedColumns;
    }

}
