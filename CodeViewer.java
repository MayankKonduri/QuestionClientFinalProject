import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

public class CodeViewer extends JPanel {
    private JFrame frame;
    private String username;
    private DatabaseManager databaseManager;
    private String questionSummary;
    private String consoleOutput;
    private String fileName;
    private byte[] fileData;
    private String tableName3;
    private String studentID;
    private String studentName;

    private JTabbedPane tabbedPane;

    public CodeViewer(JFrame frame, String userName, String studentID, String studentName, String questionSummary,
                      String consoleOutput, String fileName, byte[] fileData, String tableName3) {
        this.frame = frame;
        this.username = userName;
        this.databaseManager = new DatabaseManager(username);
        this.questionSummary = questionSummary;
        this.consoleOutput = consoleOutput;
        this.fileName = fileName;
        this.fileData = fileData;
        this.tableName3 = tableName3;
        this.studentID = studentID;
        this.studentName = studentName;

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(800, 700));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

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
            doc.insertString(doc.getLength(), questionSummary + "\n", regularStyle);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setPreferredSize(new Dimension(750, 75));
        infoPanel.add(scrollPane);

        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(infoPanel, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        unzipAndDisplayCodeFiles();

        JTextArea consoleTextArea = new JTextArea(consoleOutput);
        consoleTextArea.setEditable(false);
        consoleTextArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        JScrollPane consoleScrollPane = new JScrollPane(consoleTextArea);
        consoleScrollPane.setPreferredSize(new Dimension(750, 200));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tabbedPane, consoleScrollPane);
        splitPane.setResizeWeight(0.8);
        splitPane.setDividerLocation(600);
        add(splitPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton comingOverButton = createButton("Coming Over", e -> handleComingOver());
        JButton sendResponseButton = createButton("Send Response", e -> handleSendResponse());
        JButton homeButton = new JButton("Go Back");
        homeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.getContentPane().removeAll();
                frame.getContentPane().add(new QuestionViewer(frame, userName));
                frame.revalidate();
                frame.repaint();
                frame.setSize(400,400);
            }
        });
        comingOverButton.setFont(new Font("Georgia",Font.PLAIN, 14));
        sendResponseButton.setFont(new Font("Georgia",Font.PLAIN, 14));
        homeButton.setFont(new Font("Georgia", Font.PLAIN, 14));

        buttonPanel.add(comingOverButton);
        buttonPanel.add(sendResponseButton);
        buttonPanel.add(homeButton);
        add(buttonPanel, BorderLayout.PAGE_END);
    }

    private void unzipAndDisplayCodeFiles() {
        try {

            File tempDir = new File(System.getProperty("java.io.tmpdir"), "codeFiles");
            tempDir.mkdir();

            try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(fileData))) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    if (!entry.isDirectory()) {

                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = zis.read(buffer)) > 0) {
                            byteArrayOutputStream.write(buffer, 0, length);
                        }

                        String fileContent = byteArrayOutputStream.toString();
                        String fileName = entry.getName();
                        JTextPane textPane = new JTextPane();
                        textPane.setText(fileContent);
                        textPane.setFont(new Font("Monospaced", Font.PLAIN, 12));

                        JTextArea lineNumbers = new JTextArea(getLineNumbers(fileContent));
                        lineNumbers.setEditable(false);
                        lineNumbers.setFont(new Font("Monospaced", Font.PLAIN, 12));
                        lineNumbers.setBackground(Color.LIGHT_GRAY);
                        lineNumbers.setMargin(new Insets(0, 10, 0, 10));

                        JPanel codePanel = new JPanel(new BorderLayout());
                        codePanel.add(new JScrollPane(lineNumbers), BorderLayout.WEST);
                        codePanel.add(new JScrollPane(textPane), BorderLayout.CENTER);

                        JScrollPane codeScrollPane = new JScrollPane(codePanel);
                        tabbedPane.addTab(fileName, codeScrollPane);
                        tabbedPane.setFont(new Font("Georgia",Font.PLAIN,12));
                    }
                    zis.closeEntry();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getLineNumbers(String code) {
        String[] lines = code.split("\n");
        StringBuilder lineNumbers = new StringBuilder();
        for (int i = 1; i <= lines.length; i++) {
            lineNumbers.append(i).append("\n");
        }
        return lineNumbers.toString();
    }

    private JButton createButton(String text, ActionListener action) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        button.addActionListener(action);
        return button;
    }

    private void handleComingOver() {
        System.out.println("Coming Over selected");
        databaseManager.updateQuestionsTable(studentID, tableName3, "Went to Student's Desk", 1);
        frame.getContentPane().removeAll();
        frame.getContentPane().add(new QuestionViewer(frame, username));
        frame.revalidate();
        frame.repaint();
        frame.setSize(400, 400);
    }

    private void handleSendResponse() {
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
            databaseManager.updateQuestionsTable(studentID, tableName3, userResponse, 1);
            frame.getContentPane().removeAll();
            frame.getContentPane().add(new QuestionViewer(frame, username));
            frame.revalidate();
            frame.repaint();
            frame.setSize(400, 400);
        } else {
            System.out.println("Response input was canceled.");
        }
    }
}
