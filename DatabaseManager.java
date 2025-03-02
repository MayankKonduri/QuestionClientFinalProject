import java.sql.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private static final String DATABASE_URL = "jdbc:mysql://192.168.1.14/qclient1";
    private static final String DATABASE_USER = "root";
    private static final String DATABASE_PASSWORD = "password";
    private String userName;

    private Connection connection;

    public DatabaseManager(String userName) {

        this.userName = userName;

        try {

            connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
            System.out.println("Database connected successfully!");
        } catch (SQLException e) {
            System.err.println("Failed to connect to the database.");
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            System.err.println("Error while checking the connection.");
            e.printStackTrace();
            return false;
        }
    }

    public void addToTeachers(String teacherName, String teacherID, String sound, int waitTime) {
        String query = "INSERT INTO Teacher (teacher_name, teacher_id, sound, wait_time) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, teacherName);
            stmt.setString(2, teacherID);
            stmt.setString(3, sound);
            stmt.setInt(4, waitTime);
            stmt.executeUpdate();
            System.out.println("Teacher added successfully!");
        } catch (SQLException e) {
            System.err.println("Error adding teacher.");
            e.printStackTrace();
        }
    }

    public void deleteTeacher(String teacherID) {
        String query = "DELETE FROM Teacher WHERE teacher_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, teacherID);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Teacher with ID " + teacherID + " deleted successfully!");
            } else {
                System.out.println("No teacher found with the given ID.");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting teacher.");
            e.printStackTrace();
        }
    }

    public void updateTeacher(String teacherID, String newName, String newSound, Integer newWaitTime) {
        StringBuilder query = new StringBuilder("UPDATE Teacher SET ");
        boolean firstUpdate = true;

        if (newName != null && !newName.isEmpty()) {
            if (!firstUpdate) {
                query.append(", ");
            }
            query.append("teacher_name = ?");
            firstUpdate = false;
        }

        if (newSound != null && !newSound.isEmpty()) {
            if (!firstUpdate) {
                query.append(", ");
            }
            query.append("sound = ?");
            firstUpdate = false;
        }

        if (newWaitTime != null) {
            if (!firstUpdate) {
                query.append(", ");
            }
            query.append("wait_time = ?");
        }

        query.append(" WHERE teacher_id = ?");

        try (PreparedStatement stmt = connection.prepareStatement(query.toString())) {
            int parameterIndex = 1;

            if (newName != null && !newName.isEmpty()) {
                stmt.setString(parameterIndex++, newName);
            }
            if (newSound != null && !newSound.isEmpty()) {
                stmt.setString(parameterIndex++, newSound);
            }
            if (newWaitTime != null) {
                stmt.setInt(parameterIndex++, newWaitTime);
            }

            stmt.setString(parameterIndex, teacherID);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Teacher details updated successfully!");
            } else {
                System.out.println("No teacher found with the given ID.");
            }
        } catch (SQLException e) {
            System.err.println("Error updating teacher.");
            e.printStackTrace();
        }
    }

    public boolean checkTeacherExists(String teacherID) {
        String query = "SELECT COUNT(*) FROM Teacher WHERE teacher_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, teacherID);
            ResultSet rs = stmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error checking teacher existence.");
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkTeacherExistsByName(String teacherName) {

        String normalizedTeacherName = teacherName.toLowerCase();

        String query = "SELECT teacher_name FROM Teacher";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String dbTeacherName = rs.getString("teacher_name");

                String normalizedDbTeacherName = normalizeFullName(dbTeacherName);

                if (normalizedTeacherName.equals(normalizedDbTeacherName)) {
                    return true;
                }
            }

            return false;
        } catch (SQLException e) {
            System.err.println("Error checking teacher existence by name.");
            e.printStackTrace();
            return false;
        }
    }

    private String normalizeFullName(String fullName) {

        String[] nameParts = fullName.split(" ", 2);

        if (nameParts.length < 2) {
            return fullName.toLowerCase();
        }

        String title = nameParts[0].toLowerCase().replace(".", "");
        String name = nameParts[1].toLowerCase();

        return name;
    }

    public String[] getTeacher(String teacherID) {
        String query = "SELECT * FROM Teacher WHERE teacher_id = ?";
        String[] teacherDetails = new String[4];

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, teacherID);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                teacherDetails[0] = rs.getString("teacher_id");
                teacherDetails[1] = rs.getString("teacher_name");
                teacherDetails[2] = rs.getString("sound");
                teacherDetails[3] = String.valueOf(rs.getInt("wait_time"));
            } else {

                teacherDetails = new String[] {"No teacher found with ID: " + teacherID};
            }
        } catch (SQLException e) {
            System.err.println("Error fetching teacher details.");
            e.printStackTrace();
            teacherDetails = new String[] {"Error fetching data"};
        }

        return teacherDetails;
    }

    public void addToStudents(String studentID) {
        String query = "INSERT INTO Student (student_id) VALUES (?)";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, studentID);
            stmt.executeUpdate();
            System.out.println("Student added successfully!");
        } catch (SQLException e) {
            System.err.println("Error adding student.");
            e.printStackTrace();
        }
    }

    public void deleteStudent(String studentID) {
        String query = "DELETE FROM Student WHERE student_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, studentID);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Student with ID " + studentID + " deleted successfully.");
            } else {
                System.out.println("Student with ID " + studentID + " not found.");
            }
        } catch (SQLException e) {
            System.err.println("Error while deleting student.");
            e.printStackTrace();
        }
    }

    public boolean checkStudentExists(String studentID) {
        String query = "SELECT COUNT(*) FROM Student WHERE student_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, studentID);
            ResultSet rs = stmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error checking student existence.");
            e.printStackTrace();
            return false;
        }
    }

    public String[] getStudent(String studentID) {
        String query = "SELECT * FROM Student WHERE student_id = ?";
        String[] studentDetails = new String[1];

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, studentID);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                studentDetails[0] = rs.getString("student_id");
            } else {

                studentDetails = new String[] {"No student found with ID: " + studentID};
            }
        } catch (SQLException e) {
            System.err.println("Error fetching student details.");
            e.printStackTrace();
            studentDetails = new String[] {"Error fetching data"};
        }

        return studentDetails;
    }

    public void updateTeacherMain(String tableName, String className, String startTime, String endTime) {
        String deleteSQL = "DELETE FROM " + tableName;
        String insertSQL = "INSERT INTO " + tableName + " (ClassName, StartTime, EndTime) VALUES (?, ?, ?)";

        try {

            try (PreparedStatement deleteStatement = connection.prepareStatement(deleteSQL)) {
                deleteStatement.executeUpdate();
                System.out.println("All rows cleared from " + tableName);
            }

            try (PreparedStatement insertStatement = connection.prepareStatement(insertSQL)) {
                insertStatement.setString(1, className);

                if (startTime == null || startTime.isEmpty()) {
                    insertStatement.setNull(2, java.sql.Types.TIME);
                } else {
                    insertStatement.setString(2, startTime);
                }

                if (endTime == null || endTime.isEmpty()) {
                    insertStatement.setNull(3, java.sql.Types.TIME);
                } else {
                    insertStatement.setString(3, endTime);
                }

                insertStatement.executeUpdate();
                System.out.println("Class details added successfully to " + tableName);
            }

        } catch (SQLException e) {
            System.err.println("Error updating class details in table: " + tableName);
            e.printStackTrace();
        }
    }
    public String[] getTeacherMainData(String tableName) {
        String selectSQL = "SELECT ClassName, StartTime, EndTime FROM " + tableName;
        String[] data = new String[3];

        try (PreparedStatement statement = connection.prepareStatement(selectSQL);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                data[0] = resultSet.getString("ClassName") != null ? resultSet.getString("ClassName") : "";
                data[1] = resultSet.getTime("StartTime") != null ? resultSet.getTime("StartTime").toString() : "";
                data[2] = resultSet.getTime("EndTime") != null ? resultSet.getTime("EndTime").toString() : "";
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving data from table: " + tableName);
            e.printStackTrace();
        }

        return data;
    }
    public void updateTeacherStudents(String tableName, String studentID, String firstN, String lastN, String nickN) throws SQLException {

        if (!tableName.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("Invalid table name.");
        }

        String insertSQL = "INSERT INTO " + tableName + " (StudentID, FirstName, LastName, Nickname) VALUES (?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {

            preparedStatement.setString(1, studentID);
            preparedStatement.setString(2, firstN);
            preparedStatement.setString(3, lastN);
            preparedStatement.setString(4, nickN);

            int rowsAffected = preparedStatement.executeUpdate();
            System.out.println(rowsAffected + " row(s) inserted into table " + tableName);
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public boolean checkValueInTable(String tableName, String valueToCheck) throws SQLException {

        if (!tableName.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("Invalid table name.");
        }

        String checkSQL = "SELECT COUNT(*) FROM " + tableName + " WHERE StudentID = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(checkSQL)) {

            preparedStatement.setString(1, valueToCheck);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    if (count > 0) {
                        System.out.println("Value '" + valueToCheck + "' found in table " + tableName);
                        return true;
                    } else {
                        System.out.println("Value '" + valueToCheck + "' not found in table " + tableName);
                        return false;
                    }
                }
            }
        }
        return false;
    }

    public void removeTeacherStudents(String tableName, String studentID) throws SQLException {

        if (!tableName.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("Invalid table name.");
        }

        String deleteSQL = "DELETE FROM " + tableName + " WHERE StudentID = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(deleteSQL)) {

            preparedStatement.setString(1, studentID);

            int rowsAffected = preparedStatement.executeUpdate();
            System.out.println(rowsAffected + " row(s) deleted from table " + tableName);
        }
    }

    public ArrayList<String[]> checkNameInStudentsTables(String nameToCheck) throws SQLException {

        ArrayList<String[]> resultList = new ArrayList<>();

        String fetchTablesQuery = "SELECT table_name FROM information_schema.tables WHERE table_name LIKE '%_students';";

        try (Statement tableStmt = connection.createStatement();
             ResultSet tableResultSet = tableStmt.executeQuery(fetchTablesQuery)) {

            while (tableResultSet.next()) {
                String tableName = tableResultSet.getString("table_name");
                System.out.println("Found _students table: " + tableName);

                String checkNameQuery = "SELECT COUNT(*) FROM " + tableName + " WHERE StudentID = ?";
                try (PreparedStatement nameStmt = connection.prepareStatement(checkNameQuery)) {
                    nameStmt.setString(1, nameToCheck);
                    try (ResultSet nameResultSet = nameStmt.executeQuery()) {
                        if (nameResultSet.next() && nameResultSet.getInt(1) > 0) {
                            System.out.println("Found student '" + nameToCheck + "' in table: " + tableName);

                            String baseName = tableName.split("_students")[0];
                            String mainTableName = tableName.replace("_students", "_main");
                            System.out.println("Main table corresponding to _students table: " + mainTableName);

                            String getClassDetailsQuery = "SELECT ClassName, StartTime, EndTime FROM " + mainTableName;
                            try (Statement mainTableStmt = connection.createStatement();
                                 ResultSet classResultSet = mainTableStmt.executeQuery(getClassDetailsQuery)) {

                                if (classResultSet.next()) {
                                    String className = classResultSet.getString("ClassName");
                                    String startTime = classResultSet.getString("StartTime");
                                    String endTime = classResultSet.getString("EndTime");

                                    String teacherLastName = baseName.split("_")[0];
                                    String checkTeacherQuery = "SELECT wait_time FROM Teacher WHERE teacher_name LIKE ?";
                                    try (PreparedStatement teacherStmt = connection.prepareStatement(checkTeacherQuery)) {
                                        teacherStmt.setString(1, "% " + teacherLastName);
                                        try (ResultSet teacherResultSet = teacherStmt.executeQuery()) {
                                            String waitTime = "Not Found";
                                            if (teacherResultSet.next()) {
                                                waitTime = teacherResultSet.getString("wait_time");
                                                System.out.println("Found teacher: " + teacherLastName + " with wait_time: " + waitTime);
                                            } else {
                                                System.out.println("Teacher with last name '" + teacherLastName + "' not found.");
                                            }

                                            resultList.add(new String[]{
                                                    mainTableName,
                                                    className,
                                                    startTime,
                                                    endTime,
                                                    waitTime
                                            });
                                        }
                                    }
                                } else {

                                    System.out.println("No class details found in main table for " + mainTableName);
                                }
                            }
                        }
                    }
                }
            }
        }

        return resultList;
    }

    public String[][] getTeacherStudents(String tableName) throws SQLException {

        if (!tableName.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("Invalid table name.");
        }

        String selectSQL = "SELECT StudentID, FirstName, LastName, Nickname FROM " + tableName;

        List<String[]> students = new ArrayList<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(selectSQL)) {

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {

                    String studentId = resultSet.getString("StudentID");
                    String firstName = resultSet.getString("FirstName");
                    String lastName = resultSet.getString("LastName");
                    String nickname = resultSet.getString("Nickname");
                    students.add(new String[]{studentId, firstName, lastName, nickname});
                }
            }
        }

        return students.toArray(new String[0][0]);
    }

    public void deleteTeacherAndAssociatedTables(String teacherName) {
        String deleteTeacherQuery = "DELETE FROM Teacher WHERE teacher_name = ?";

        String baseName = teacherName.replaceAll("^(Mr\\.|Mrs\\.|Ms\\.)\\s*", "").trim();

        try (PreparedStatement stmt = connection.prepareStatement(deleteTeacherQuery)) {

            stmt.setString(1, teacherName);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Teacher '" + teacherName + "' deleted successfully from Teacher table.");
            } else {
                System.out.println("No teacher found with the name '" + teacherName + "'.");
                return;
            }

            for (int grade = 1; grade <= 7; grade++) {
                String mainTable = baseName + "_" + grade + "_main";
                String studentsTable = baseName + "_" + grade + "_students";
                String questionsTable = baseName + "_" + grade + "_questions";

                try (Statement tableDeletionStmt = connection.createStatement()) {

                    String dropMainTableQuery = "DROP TABLE IF EXISTS " + mainTable;
                    tableDeletionStmt.executeUpdate(dropMainTableQuery);
                    System.out.println("Deleted table: " + mainTable);

                    String dropStudentsTableQuery = "DROP TABLE IF EXISTS " + studentsTable;
                    tableDeletionStmt.executeUpdate(dropStudentsTableQuery);
                    System.out.println("Deleted table: " + studentsTable);

                    String dropQuestionsTableQuery = "DROP TABLE IF EXISTS " + questionsTable;
                    tableDeletionStmt.executeUpdate(dropQuestionsTableQuery);
                    System.out.println("Deleted table: " + questionsTable);
                } catch (SQLException e) {
                    System.err.println("Error deleting table: " + mainTable + " or " + studentsTable + " or " + questionsTable);
                    e.printStackTrace();
                }
            }

        } catch (SQLException e) {
            System.err.println("Error deleting teacher and associated tables.");
            e.printStackTrace();
        }
    }

    public static void addRecordToTable(String tableName, String studentID, String questionSummary, byte[] fileBytes, String consoleErrorOutput, String FileName) {
        if (!tableName.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("Invalid table name.");
        }

        PreparedStatement preparedStatement = null;
        Connection connection = null;

        try {
            String insertSQL = "INSERT INTO " + tableName +
                    " (StudentID, QuestionSummary, TimeStamp, IsQuestionActive, Response, AttachedCodeFile, ConsoleOutput, FileName) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            String DATABASE_URL = "jdbc:mysql://192.168.1.14/qclient1";
            String DATABASE_USER = "root";
            String DATABASE_PASSWORD = "password";

            connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
            preparedStatement = connection.prepareStatement(insertSQL);

            preparedStatement.setString(1, studentID);
            preparedStatement.setString(2, questionSummary);
            preparedStatement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            preparedStatement.setBoolean(4, true);
            preparedStatement.setString(5, null);

            if (fileBytes != null) {
                preparedStatement.setBytes(6, fileBytes);
            } else {
                preparedStatement.setNull(6, java.sql.Types.BLOB);
            }

            preparedStatement.setString(7, consoleErrorOutput);
            preparedStatement.setString(8, FileName);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Record successfully added to the table.");
            } else {
                System.out.println("Failed to add record to the table.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getQuestionStudent(String questionTableName, String studentID) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {

            String selectSQL = "SELECT QuestionSummary FROM " + questionTableName + " WHERE StudentID = ? AND IsQuestionActive = 1";

            String DATABASE_URL = "jdbc:mysql://192.168.1.14/qclient1";
            String DATABASE_USER = "root";
            String DATABASE_PASSWORD = "password";

            connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);

            preparedStatement = connection.prepareStatement(selectSQL);

            preparedStatement.setString(1, studentID);

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                System.out.println("Find Successful");
                return resultSet.getString("QuestionSummary");
            } else {
                return "";
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return "";
        } finally {

            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void deactivateQuestion(String tableName, String studentID, String questionSummary) {

        if (!tableName.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("Invalid table name.");
        }

        PreparedStatement preparedStatement = null;
        Connection connection = null;

        try {

            String updateSQL = "UPDATE " + tableName + " SET IsQuestionActive = ? WHERE StudentID = ? AND QuestionSummary = ?";

            String DATABASE_URL = "jdbc:mysql://192.168.1.14/qclient1";
            String DATABASE_USER = "root";
            String DATABASE_PASSWORD = "password";

            connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);

            preparedStatement = connection.prepareStatement(updateSQL);

            preparedStatement.setBoolean(1, false);
            preparedStatement.setString(2, studentID);
            preparedStatement.setString(3, questionSummary);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Question successfully deactivated.");
            } else {
                System.out.println("Failed to deactivate question. No matching record found.");
            }

        } catch (SQLException e) {

            e.printStackTrace();
        } finally {

            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean doesTableExist(String tableName) {
        String query = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?";
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD)) {
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, tableName);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getInt(1) > 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean doesColumnExist(String tableName, String columnName) {
        String query = "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?";
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD)) {
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, tableName);
                statement.setString(2, columnName);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getInt(1) > 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void insertOrUpdateWaitTime(String tableString, String columnName, int remainingTime) {

        tableString = tableString.toLowerCase();

        if (!doesTableExist(tableString)) {

            return;
        } else {

        }

        if (!doesColumnExist(tableString, columnName)) {

            return;
        } else {

        }

        String deleteQuery = "DELETE FROM " + tableString;

        try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD)) {

            try (PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery)) {

                int rowsDeleted = deleteStatement.executeUpdate();

                if (rowsDeleted > 0) {

                } else {

                }
            }

            String insertQuery = "INSERT INTO " + tableString + " (" + columnName + ") VALUES (?)";

            try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {

                insertStatement.setInt(1, remainingTime);

                int rowsInserted = insertStatement.executeUpdate();

                if (rowsInserted > 0) {

                } else {

                }
            }

        } catch (SQLException e) {

            System.err.println("SQL Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public int getWaitTimeFromStudent(String tableString, String columnName) {
        int result = -1;

        String selectQuery = "SELECT " + columnName + " FROM " + tableString;

        try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD)) {

            try (PreparedStatement statement = connection.prepareStatement(selectQuery)) {

                try (ResultSet resultSet = statement.executeQuery()) {

                    if (resultSet.next()) {

                        result = resultSet.getInt(columnName);
                    } else {

                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static int getQuestionPosition(String tableName, String studentID) {
        int position = -1;
        String query = "SELECT StudentID, QuestionSummary, TimeStamp, IsQuestionActive " +
                "FROM " + tableName + " " +
                "WHERE IsQuestionActive = 1 " +
                "ORDER BY TimeStamp";

        try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            int rowNumber = 0;

            while (rs.next()) {
                String currentStudentID = rs.getString("StudentID");
                if (currentStudentID.equals(studentID)) {
                    rowNumber++;
                    position = rowNumber;
                    break;
                }
                rowNumber++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return position;
    }

    public void removeActiveQuestion(String studentID, String tableName) {
        String query = "UPDATE " + tableName + " SET ConsoleOutput = NULL, AttachedCodeFile = NULL, isQuestionActive = 0 WHERE studentID = ? AND isQuestionActive = 1";

        try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setString(1, studentID);

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Successfully deactivated the active question for student ID: " + studentID);
            } else {
                System.out.println("No active question found for student ID: " + studentID);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clearQuestionsList(String tableName1) {
        String sql = "UPDATE " + tableName1 + " SET ConsoleOutput = NULL, AttachedCodeFile = NULL, isQuestionActive = 0, response = 'Teacher Manually Removed Question', SeenResponse = 2 WHERE isQuestionActive = 1";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int affectedRows = pstmt.executeUpdate();
            System.out.println("Cleared questions and updated responses. Rows affected: " + affectedRows);

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to clear questions and update responses in table: " + tableName1);
        }
    }

    public String getStudentName(String studentID, String tableName) {
        String studentName = null;
        String query = "SELECT nickname FROM " + tableName + " WHERE studentID = ?";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, studentID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                studentName = rs.getString("nickname");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return studentName;
    }

    public void updateQuestionsTable(String studentID, String tableName3, String s, int SeenResponse) {
        System.out.println(s + " Message Received for Student " + studentID + " in Table " + tableName3);
        String query = "UPDATE " + tableName3 + " SET Response = ?, isQuestionActive = 0, ConsoleOutput = NULL, AttachedCodeFile = NULL, SeenResponse = ? WHERE studentID = ? AND isQuestionActive = 1";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, s);
            pstmt.setInt(2, SeenResponse);
            pstmt.setString(3, studentID);

            int rowsUpdated = pstmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Successfully updated the question for student ID: " + studentID);
            } else {
                System.out.println("No active question found for student ID: " + studentID);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public Object[] getQuestionDetails(String studentID, String tableName) {
        String query = "SELECT QuestionSummary, FileName, AttachedCodeFile, ConsoleOutput FROM " + tableName + " WHERE studentID = ? AND isQuestionActive = 1";
        Object[] result = new Object[4];

        try (Connection conn = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, studentID);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    result[0] = rs.getString("QuestionSummary");
                    result[1] = rs.getString("FileName");
                    result[2] = rs.getBytes("AttachedCodeFile");
                    result[3] = rs.getString("ConsoleOutput");
                } else {
                    System.out.println("No active question found for student ID: " + studentID);
                    result[0] = result[1] = result[2] = null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }
    public Object[] getSeenMessageStatus(String questionTableName, String userName) {
        Object[] result = new Object[4];
            String query = "SELECT StudentID, QuestionSummary, TimeStamp, Response, SeenResponse " + "FROM " + questionTableName + " " + "WHERE StudentID = ? AND SeenResponse != 0";

        try(Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
            PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, userName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    result[0] = rs.getString("QuestionSummary");
                    Time timeStamp = rs.getTime("TimeStamp");
                    result[1] = timeStamp != null ? timeStamp.toString() : null;
                    result[2] = rs.getString("Response");
                    result[3] = rs.getInt("SeenResponse");
                } else {
                    result = null;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return result;
    }
    public void resetSeenSample(String questionTableName, String studentID){
        String query = "UPDATE " + questionTableName + " SET SeenResponse = 0 WHERE StudentID = ? AND SeenResponse != 0";
        try(Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
            PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, studentID);
            stmt.executeUpdate();

        } catch (SQLException e){
            e.printStackTrace();
        }
    }
}
