import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import javax.swing.*;
import java.awt.event.*;
import java.sql.*;

public class HomePage extends JPanel {
    private JFrame frame;
    public String fullName;
    private String userName; // = System.getProperty("user.name"); //UNCOMMENT WHEN DEPLOYING

    DatabaseManager dbManager = new DatabaseManager(userName);
    public HomePage(JFrame frame, String userName) throws IOException {
        this.userName = userName;
        if (dbManager.isConnected()) {
            System.out.println("Database connection is active!");
        } else {
            System.err.println("Database connection failed!");
        }

        this.frame = frame;

        setLayout(null);

        JLabel titleLabel = new JLabel("Question-Client");
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 18));
        titleLabel.setBounds(130, 20, 200, 30);
        add(titleLabel);

        JButton teacherButton = new JButton("Teacher");
        teacherButton.setFont(new Font("Georgia",Font.BOLD, 15));
        teacherButton.setBounds(140, 70, 120, 30);
        add(teacherButton);

        teacherButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (dbManager.checkTeacherExists(userName)) {
                    System.out.println("Teacher Exists");

                    TeacherHome teacherHome = new TeacherHome(frame, userName);

                    frame.getContentPane().removeAll();
                    frame.revalidate();
                    frame.repaint();
                    frame.setSize(400, 325);
                    frame.add(teacherHome);
                    frame.setVisible(true);
                } else {
                    JPanel panel = new JPanel();
                    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

                    Font georgiaFont = new Font("Georgia", Font.BOLD, 14);

                    JTextField nameField = new JTextField(20);
                    JComboBox<String> titleComboBox = new JComboBox<>(new String[] {"Mr.", "Ms.", "Mrs."});

                    JLabel nameLabel = new JLabel("Enter Teacher Name:");
                    JLabel titleLabel = new JLabel("Select Title:");

                    nameLabel.setFont(georgiaFont);
                    nameField.setFont(georgiaFont);
                    titleLabel.setFont(georgiaFont);
                    titleComboBox.setFont(georgiaFont);

                    panel.add(nameLabel);
                    panel.add(nameField);
                    panel.add(titleLabel);
                    panel.add(titleComboBox);

                    int option = JOptionPane.showConfirmDialog(null, panel, "Add New Teacher", JOptionPane.OK_CANCEL_OPTION);

                    if (option == JOptionPane.OK_OPTION) {
                        String name = nameField.getText();
                        String title = (String) titleComboBox.getSelectedItem();
                        fullName = title + " " + name;

                        if (!isValidName(name) || isReservedKeyword(name)) {
                            JOptionPane.showMessageDialog(null, "Name does not follow Naming Conventions. Please try again.", "Invalid Name", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        String normalizedName = normalizeFullName(fullName);
                        if (dbManager.checkTeacherExistsByName(normalizedName)) {
                            JOptionPane.showMessageDialog(null, "A teacher with the name " + toPascalCase(name) + " already exists.", "Teacher Exists", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        dbManager.addToTeachers(fullName, userName, "Default", 60);
                        JOptionPane.showMessageDialog(null, "You have been added as a teacher " + fullName);

                        for (int i = 1; i <= 7; i++) {
                            String tableCreation1 = name + "_" + i + "_Main";
                            String tableCreation2 = name + "_" + i + "_Students";
                            String tableCreation3 = name + "_" + i + "_Questions";
                            createTableMain(tableCreation1);
                            createTableStudents(tableCreation2);
                            createTableQuestions(tableCreation3);
                        }

                        TeacherHome teacherHome = new TeacherHome(frame, userName);

                        frame.getContentPane().removeAll();
                        frame.revalidate();
                        frame.repaint();
                        frame.setSize(400, 325);
                        frame.add(teacherHome);
                        frame.setVisible(true);
                    }
                }
            }
        });

        JLabel andLabel = new JLabel("and");
        andLabel.setFont(new Font("Serif", Font.ITALIC, 16));
        andLabel.setBounds(185, 105, 40, 20);
        add(andLabel);

        JButton studentButton = new JButton("Student");
        studentButton.setFont(new Font("Georgia",Font.BOLD, 15));
        studentButton.setBounds(140, 130, 120, 30);
        add(studentButton);

        studentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                StudentHome studentHome = null;
                try {
                    studentHome = new StudentHome(frame, userName);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
                frame.getContentPane().removeAll();
                frame.revalidate();
                frame.repaint();
                frame.setSize(400, 300);
                frame.add(studentHome);
                frame.setVisible(true);
            }
        });
    }

    private void createTableMain(String name) {
        Connection connection = null;
        Statement statement = null;

        try {

            Class.forName("com.mysql.cj.jdbc.Driver");

            connection = DriverManager.getConnection("jdbc:mysql://10.195.75.116/qclient1", "root", "password");

            statement = connection.createStatement();
            String checkDatabaseSQL = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = 'qclient1'";
            ResultSet rs = statement.executeQuery(checkDatabaseSQL);

            String createTableSQL = "CREATE TABLE IF NOT EXISTS " + name + " (" +
                    "ClassName VARCHAR(100), " +
                    "StartTime TIME NULL, " +
                    "EndTime TIME NULL" +
                    ")";

            statement.executeUpdate(createTableSQL);
            System.out.println("Table " + name + " created successfully.");

        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {

            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void createTableStudents(String name) {
        Connection connection = null;
        Statement statement = null;

        try {

            Class.forName("com.mysql.cj.jdbc.Driver");

            connection = DriverManager.getConnection("jdbc:mysql://10.195.75.116/qclient1", "root", "password");

            statement = connection.createStatement();
            String checkDatabaseSQL = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = 'qclient1'";
            ResultSet rs = statement.executeQuery(checkDatabaseSQL);

            String createTableSQL = "CREATE TABLE IF NOT EXISTS " + name + " (" +
                    "StudentID VARCHAR(100), " +
                    "FirstName VARCHAR(100), " +
                    "LastName VARCHAR(100), " +
                    "Nickname VARCHAR(100)" +
                    ")";

            statement.executeUpdate(createTableSQL);
            System.out.println("Table " + name + " created successfully.");

        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {

            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void createTableQuestions(String tableCreation3) {
        Connection connection = null;
        Statement statement = null;

        try {

            Class.forName("com.mysql.cj.jdbc.Driver");

            connection = DriverManager.getConnection("jdbc:mysql://10.195.75.116/qclient1", "root", "password");

            statement = connection.createStatement();
            String checkDatabaseSQL = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = 'qclient1'";
            ResultSet rs = statement.executeQuery(checkDatabaseSQL);

            String createTableSQL = "CREATE TABLE IF NOT EXISTS " + tableCreation3 + " (" +
                    "StudentID VARCHAR(100), " +
                    "QuestionSummary LONGTEXT, " +
                    "TimeStamp TIME, " +
                    "IsQuestionActive BOOLEAN, " +
                    "Response LONGTEXT," +
                    "FileName VARCHAR(200)," +
                    "AttachedCodeFile LONGBLOB," +
                    "ConsoleOutput LONGTEXT" +
                    ")";

            statement.executeUpdate(createTableSQL);
            System.out.println("Table " + tableCreation3 + " created successfully.");

        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {

            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private boolean isValidName(String name) {
        String regex = "^[a-zA-Z_]+$";
        return name.matches(regex);
    }

    private boolean isReservedKeyword(String name) {
        String[] reservedKeywords = {
                "SELECT", "INSERT", "UPDATE", "DELETE", "DROP", "TABLE", "WHERE", "FROM", "JOIN", "AND", "OR", "GROUP", "ORDER", "BY", "HAVING"

        };
        for (String keyword : reservedKeywords) {
            if (name.equalsIgnoreCase(keyword)) {
                return true;
            }
        }
        return false;
    }

    public String getFullName(){
        return fullName;
    }

    public String normalizeFullName(String fullName) {

        String[] nameParts = fullName.split(" ", 2);

        if (nameParts.length < 2) {
            return fullName.toLowerCase();
        }

        String title = nameParts[0].toLowerCase();
        String name = nameParts[1].toLowerCase();

        System.out.println("Test Name: " + title + name);
        return name;
    }

    public String toPascalCase(String input) {

        if (input == null || input.isEmpty()) {
            return input;
        }

        String result = input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();

        return result;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Question-Client");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.setSize(400, 225);

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String name;
            System.out.print("Enter your name: ");
            try {
                name = reader.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            HomePage homePage = null;
            try {
                homePage = new HomePage(frame, name);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            frame.add(homePage);

            frame.setVisible(true);
        });
    }
}
