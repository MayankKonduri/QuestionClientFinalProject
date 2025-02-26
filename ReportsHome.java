import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ReportsHome extends JPanel {
    public JFrame jFrame;
    public String username;
    private DatabaseManager databaseManager;

    public ReportsHome(JFrame frame, String username){
        this.jFrame = frame;
        this.username = username;
        this.databaseManager = new DatabaseManager(username);

        setLayout(null);

        JButton homeButton = new JButton("Home");
        homeButton.setFont(new Font("Georgia", Font.BOLD, 10));
        homeButton.setBounds(15, 10, 70, 20);
        homeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.getContentPane().removeAll();
                frame.getContentPane().add(new TeacherHome(frame, username));
                frame.revalidate();
                frame.repaint();
                frame.setSize(400,325);
            }
        });
        add(homeButton);

        JLabel titleLabel = new JLabel("Reports");
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 25));
        titleLabel.setBounds(195, 25, 200, 40);
        add(titleLabel);
    }
}
