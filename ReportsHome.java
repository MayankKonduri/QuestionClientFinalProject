import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ReportsHome extends JPanel {
    public JFrame jFrame;
    public String username;
    private DatabaseManager databaseManager;

    private List<String> classNames = new ArrayList<>();
    private JComboBox<String> filterDropdown;
    private JComboBox<String> periodDropdown;
    private JPanel classNamePanel;

    public ReportsHome(JFrame frame, String username) {
        this.jFrame = frame;
        this.username = username;
        this.databaseManager = new DatabaseManager(username);

        setLayout(null);

        classNames.addAll(Arrays.asList("K1823180", "K1823182", "K1823183", "K1823200", "M2034221", "M2034222", "M2034230", "M2041233"));

        JButton homeButton = new JButton("Home");
        homeButton.setFont(new Font("Georgia", Font.BOLD, 10));
        homeButton.setBounds(15, 10, 70, 20);
        homeButton.addActionListener(e -> {
            frame.getContentPane().removeAll();
            frame.getContentPane().add(new TeacherHome(frame, username));
            frame.revalidate();
            frame.repaint();
            frame.setSize(400, 325);
        });
        add(homeButton);

        JLabel titleLabel = new JLabel("Reports");
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 25));
        titleLabel.setBounds(195, 25, 200, 40);
        add(titleLabel);

        JLabel filterLabel = new JLabel("Filter By:");
        filterLabel.setFont(new Font("Georgia", Font.BOLD, 12));
        filterLabel.setBounds(30, 80, 100, 30);
        add(filterLabel);

        String[] filterOptions = {"Select Option", "Class Period", "Class Name", "Student ID", "Date Range"};
        filterDropdown = new JComboBox<>(filterOptions);
        filterDropdown.setFont(new Font("Georgia", Font.PLAIN, 12));
        filterDropdown.setBounds(100, 80, 150, 30);
        add(filterDropdown);

        String[] periodOptions = {"Select Period", "Period 1", "Period 2", "Period 3", "Period 4", "Period 5", "Period 6", "Period 7"};
        periodDropdown = new JComboBox<>(periodOptions);
        periodDropdown.setFont(new Font("Georgia", Font.PLAIN, 12));
        periodDropdown.setBounds(270, 80, 120, 30);
        periodDropdown.setVisible(false);
        add(periodDropdown);

        classNamePanel = createClassNamePanel();
        classNamePanel.setBounds(270, 80, 120, 60);
        classNamePanel.setVisible(false);
        add(classNamePanel);

        filterDropdown.addActionListener(e -> {
            String selectedOption = (String) filterDropdown.getSelectedItem();
            if ("Class Period".equals(selectedOption)) {
                periodDropdown.setVisible(true);
                classNamePanel.setVisible(false);
            } else if ("Class Name".equals(selectedOption)) {
                classNamePanel.setVisible(true);
                periodDropdown.setVisible(false);
            } else {
                periodDropdown.setVisible(false);
                classNamePanel.setVisible(false);
            }
        });
    }

    private JPanel createClassNamePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextField classNameField = new JTextField();
        classNameField.setFont(new Font("Georgia", Font.PLAIN, 12));

        JComboBox<String> classNameComboBox = new JComboBox<>();
        classNameComboBox.setFont(new Font("Georgia", Font.PLAIN, 12));
        classNameComboBox.setEditable(false);

        classNameField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (classNameComboBox.getItemCount() == 0) {
                    classNameComboBox.setModel(new DefaultComboBoxModel<>(classNames.toArray(new String[0])));
                }
                classNameComboBox.showPopup();
            }
        });

        classNameField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateDropdown();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateDropdown();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateDropdown();
            }

            private void updateDropdown() {
                String input = classNameField.getText().trim();

                if (input.isEmpty()) {
                    classNameComboBox.setModel(new DefaultComboBoxModel<>(classNames.toArray(new String[0])));
                    classNameComboBox.showPopup();
                    return;
                }

                List<String> filteredClasses = classNames.stream()
                        .filter(className -> className.toLowerCase().contains(input.toLowerCase()))
                        .collect(Collectors.toList());

                classNameComboBox.setModel(new DefaultComboBoxModel<>(filteredClasses.toArray(new String[0])));

                if (!filteredClasses.isEmpty()) {
                    classNameComboBox.showPopup();
                } else {
                    classNameComboBox.hidePopup();
                }
            }
        });

        classNameComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedValue = (String) classNameComboBox.getSelectedItem();
                if (selectedValue != null) {
                    classNameField.setText(selectedValue);
                    classNameComboBox.hidePopup();
                }
            }
        });

        classNameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_UP) {
                    e.consume();
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String selectedValue = (String) classNameComboBox.getSelectedItem();
                    if (selectedValue != null) {
                        classNameField.setText(selectedValue);
                        classNameComboBox.hidePopup();
                    }
                }
            }
        });

        classNameComboBox.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String selectedValue = (String) classNameComboBox.getSelectedItem();
                if (selectedValue != null) {
                    classNameField.setText(selectedValue);
                }
            }
        });

        classNameField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String selectedValue = (String) classNameComboBox.getSelectedItem();
                if (selectedValue != null) {
                    classNameField.setText(selectedValue);
                }
            }
        });

        panel.add(classNameField, BorderLayout.NORTH);
        panel.add(classNameComboBox, BorderLayout.CENTER);

        classNameComboBox.setModel(new DefaultComboBoxModel<>(classNames.toArray(new String[0])));

        return panel;
    }





    private void filterClassNames(String input, JComboBox<String> comboBox) {
        List<String> filteredClasses = classNames.stream()
                .filter(className -> className.toLowerCase().contains(input.toLowerCase()))
                .collect(Collectors.toList());
        comboBox.setModel(new DefaultComboBoxModel<>(filteredClasses.toArray(new String[0])));
    }
}
