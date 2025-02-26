import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Arrays;

public class TeacherHome extends JPanel {
    private JFrame frame;
    DatabaseManager dbManager;
    String userName; //= System.getProperty("user.name");

    public TeacherHome(JFrame frame, String userName) {
        this.frame = frame;
        this.userName = userName;
        dbManager = new DatabaseManager(userName);
        setLayout(null);

        String[] temp = dbManager.getTeacher(userName);
        System.out.println(Arrays.toString(temp));

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
                HomePage homePage = null;
                try {
                    homePage = new HomePage(frame, userName);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                frame.getContentPane().removeAll();
                frame.getContentPane().add(homePage);
                frame.revalidate();
                frame.repaint();
                frame.setSize(400, 225);
            }
        });

        if (temp != null && temp.length > 0) {

            String teacherName = temp[1];

            JLabel titleLabel = new JLabel("Welcome " + teacherName);
            titleLabel.setFont(new Font("Georgia", Font.BOLD, 18));
            titleLabel.setBounds(110, 20, 300, 30);
            add(titleLabel);

            JButton questionViewerButton = new JButton("Question Viewer");
            questionViewerButton.setFont(new Font("Georgia", Font.BOLD, 16));
            questionViewerButton.setBounds(50, 70, 300, 40);
            add(questionViewerButton);
            questionViewerButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    QuestionViewer questionViewer = null;
                    questionViewer = new QuestionViewer(frame, userName);
                    frame.getContentPane().removeAll();
                    frame.getContentPane().add(questionViewer);
                    frame.revalidate();
                    frame.repaint();
                    frame.setSize(400, 400);
                }
            });

            JButton coursesButton = new JButton("Courses");
            coursesButton.setFont(new Font("Georgia", Font.BOLD, 16));
            coursesButton.setBounds(50, 120, 300, 40);
            add(coursesButton);
            coursesButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    TeacherCourses teacherCourses = new TeacherCourses(frame, userName);

                    frame.getContentPane().removeAll();
                    frame.revalidate();
                    frame.repaint();
                    frame.setSize(400, 325);
                    frame.add(teacherCourses);
                    frame.setVisible(true);
                }
            });

            JButton reportsButton = new JButton("Reports");
            reportsButton.setFont(new Font("Georgia", Font.BOLD, 16));
            reportsButton.setBounds(50, 170, 300, 40);
            add(reportsButton);

            reportsButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ReportsHome reportsHome = new ReportsHome(frame, userName);

                    frame.getContentPane().removeAll();
                    frame.revalidate();
                    frame.repaint();
                    frame.setSize(500, 500);
                    frame.add(reportsHome);
                    frame.setVisible(true);
                }
            });

            JButton settingsButton = new JButton("Settings");
            settingsButton.setFont(new Font("Georgia", Font.BOLD, 16));
            settingsButton.setBounds(50, 220, 300, 40);
            add(settingsButton);

            settingsButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    String[] temp1 = dbManager.getTeacher(userName);
                    String pID = temp1[0];
                    String pName = temp1[1];
                    String pSound = temp1[2];
                    int pWaitTime = Integer.parseInt(temp1[3]);

                    JDialog dialog = new JDialog(frame, "Settings", true);

                    dialog.setLayout(new GridBagLayout());
                    GridBagConstraints gbc = new GridBagConstraints();
                    gbc.insets = new Insets(10, 10, 10, 10);
                    gbc.fill = GridBagConstraints.HORIZONTAL;

                    Font font = new Font("Georgia", Font.PLAIN, 12);

                    gbc.gridx = 0;
                    gbc.gridy = 0;
                    JLabel idLabel = new JLabel("ID:", JLabel.RIGHT);
                    idLabel.setFont(font);
                    dialog.add(idLabel, gbc);

                    gbc.gridx = 1;
                    JTextField idField = new JTextField(pID);
                    idField.setEditable(false);
                    idField.setFont(font);
                    dialog.add(idField, gbc);

                    gbc.gridx = 0;
                    gbc.gridy = 1;
                    JLabel nameLabel = new JLabel("Name:", JLabel.RIGHT);
                    nameLabel.setFont(font);
                    dialog.add(nameLabel, gbc);

                    gbc.gridx = 1;
                    JTextField nameField = new JTextField(pName);
                    nameField.setEditable(false);
                    nameField.setFont(font);
                    dialog.add(nameField, gbc);

                    gbc.gridx = 0;
                    gbc.gridy = 2;
                    JLabel soundLabel = new JLabel("Sound:", JLabel.RIGHT);
                    soundLabel.setFont(font);
                    dialog.add(soundLabel, gbc);

                    gbc.gridx = 1;
                    JComboBox<String> soundComboBox = new JComboBox<>(new String[]{"Default", "SoundTemp1", "SoundTemp2", "SoundTemp3"});
                    soundComboBox.setSelectedItem(pSound);
                    soundComboBox.setFont(font);
                    dialog.add(soundComboBox, gbc);

                    gbc.gridx = 0;
                    gbc.gridy = 3;
                    JLabel waitTimeLabel = new JLabel("Wait Time (seconds):", JLabel.RIGHT);
                    waitTimeLabel.setFont(font);
                    dialog.add(waitTimeLabel, gbc);

                    gbc.gridx = 1;
                    JSlider waitTimeSlider = new JSlider(0, 600, pWaitTime);
                    waitTimeSlider.setMajorTickSpacing(60);
                    waitTimeSlider.setMinorTickSpacing(30);
                    waitTimeSlider.setPaintTicks(true);
                    waitTimeSlider.setPaintLabels(true);
                    waitTimeSlider.setFont(font);
                    dialog.add(waitTimeSlider, gbc);

                    Font smallFont = new Font("Georgia", Font.PLAIN, 8);
                    waitTimeSlider.setFont(smallFont);

                    gbc.gridy = 4;
                    JLabel currentWaitTimeLabel = new JLabel("Current Wait Time: " + waitTimeSlider.getValue() + " seconds");
                    currentWaitTimeLabel.setFont(font);

                    waitTimeSlider.addChangeListener(e1 -> {
                        currentWaitTimeLabel.setText("Current Wait Time: " + waitTimeSlider.getValue() + " seconds");
                    });
                    dialog.add(currentWaitTimeLabel, gbc);

                    gbc.gridx = 1;
                    gbc.gridy = 5;
                    JButton okButton = new JButton("OK");
                    okButton.setFont(new Font("Georgia", Font.BOLD, 12));
                    okButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {

                            String newName = nameField.getText();
                            String newSound = (String) soundComboBox.getSelectedItem();
                            int newWaitTime = waitTimeSlider.getValue();

                            dbManager.updateTeacher(pID, newName, newSound, newWaitTime);

                            dialog.dispose();
                        }
                    });
                    dialog.add(okButton, gbc);

                    gbc.gridy = 6;
                    JButton deleteButton = new JButton("Delete Teacher Account");
                    deleteButton.setFont(new Font("Georgia", Font.BOLD, 12));
                    deleteButton.setBackground(Color.WHITE);
                    deleteButton.setForeground(Color.RED);
                    deleteButton.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
                    deleteButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            int confirm = JOptionPane.showConfirmDialog(
                                    dialog,
                                    "Are you sure you want to delete the teacher account? This action cannot be undone.",
                                    "Confirm Deletion",
                                    JOptionPane.OK_CANCEL_OPTION,
                                    JOptionPane.WARNING_MESSAGE
                            );

                            if (confirm == JOptionPane.OK_OPTION) {

                                dbManager.deleteTeacherAndAssociatedTables(pName);
                                dialog.dispose();

                                HomePage homePage = null;
                                try {
                                    homePage = new HomePage(frame, userName);
                                } catch (IOException ex) {
                                    throw new RuntimeException(ex);
                                }
                                frame.getContentPane().removeAll();
                                frame.getContentPane().add(homePage);
                                frame.revalidate();
                                frame.repaint();
                                frame.setSize(400, 225);
                            }
                        }
                    });
                    dialog.add(deleteButton, gbc);

                    dialog.setSize(500, 450);
                    dialog.setLocationRelativeTo(frame);
                    dialog.setVisible(true);
                }
            });
        } else {
            System.err.println("Teacher not found or error occurred.");
        }
    }

}
