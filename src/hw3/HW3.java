package hw3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HW3 extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField textField1;
    private JTextField textField2;
    private JTextField textField3;
    private JTextField textField4;
    private JTextField textField5;
    private JComboBox comboBox1;
    private JComboBox comboBox2;
    private JComboBox comboBox3;
    private JComboBox comboBox4;
    private JComboBox comboBox5;
    private JButton executeButton;
    private JPanel footer;
    private JPanel buttons;
    private JPanel topPanel;
    private JPanel reviewPanel;
    private JPanel businessPanel;
    private JPanel lastPanel;
    private JPanel queryPanel;
    private JPanel resultsPanel;
    private JPanel middlePanel;
    private JPanel labelsPanel;
    private JPanel textFieldsPanel;
    private JPanel selectsPanel;
    private JPanel attributesPanel;
    private JPanel subcategoryPanel;
    private JScrollPane categoryPane;
    private JPanel categoryPanel;
    private JScrollPane subcategoryPane;
    private JScrollPane attributesPane;

    public HW3(List<String> categories) {

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // Set preferred sizes
        categoryPane.setPreferredSize(new Dimension(250, 200));
        subcategoryPane.setPreferredSize(new Dimension(250, 200));
        attributesPane.setPreferredSize(new Dimension(250, 200));

        reviewPanel.setPreferredSize(new Dimension(500, 200));
        resultsPanel.setPreferredSize(new Dimension(750, 250));
        queryPanel.setPreferredSize(new Dimension(500, 250));

        // Add to UI
        JList categoryList = new JList();
        categoryList.setLayout(new BoxLayout(categoryList, BoxLayout.PAGE_AXIS));
        categoryList.setPreferredSize(new Dimension(200, 650));

        for (String category : categories) {
            JCheckBox checkBox = new JCheckBox(category);
            checkBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println(e.getID() == ActionEvent.ACTION_PERFORMED
                            ? "ACTION_PERFORMED" : e.getID());
                }
            });
            checkBox.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    System.out.println(e.getStateChange() == ItemEvent.SELECTED
                            ? "SELECTED" : "DESELECTED");
                    JCheckBox check = (JCheckBox) e.getSource();
                    String categoryName = check.getText();

                }
            });
            categoryList.add(checkBox);
            categoryList.repaint();
        }

        categoryPane.setLayout(new ScrollPaneLayout());
        categoryPane.add(categoryList);
        categoryPane.setViewportView(categoryList);
        categoryPane.repaint();

    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {

        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            System.out.println("Exception while loading oracle jdbc driver: " + e.getMessage());
            System.out.println("Terminated program.");
            System.exit(-1);
        }


        Connection connection = null;
        try {

            connection = DriverManager
                    .getConnection(Constants.ORACLE_URL, Constants.USERNAME, Constants.PASSWORD);

            // Fetch category from DB
            List<String> categories = new ArrayList<String>();
            categories = queryAllCategories(connection);

            HW3 dialog = new HW3(categories);
            dialog.pack();
            dialog.setVisible(true);

        } catch (SQLException e) {
            System.out.println("Exception while establishing connection: " + e.getMessage());
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                System.out.println("Exception while closing connection: " + e.getMessage());
                System.exit(-1);
            }
        }

        System.exit(0);

    }

    private static List<String> queryAllCategories(Connection conn) {

        List<String> categories = new ArrayList<String>();

        String sql = "SELECT C.CATEGORY FROM CATEGORY C";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()) {
                categories.add(rs.getString("CATEGORY"));
            }
            rs.close();
        } catch(SQLException e) {
            System.out.println("Exception while querying for categories: " + e.getMessage());
        }

        return categories;

    }
}
