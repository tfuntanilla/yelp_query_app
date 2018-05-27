package hw3;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
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
    private JButton buttonExecute;
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
    private JScrollPane resultsScrollPane;
    private JComboBox businessANDORSelect;
    private JButton buttonClear;

    private List<String> checkedCategories = new ArrayList<String>();
    private List<String> checkedSubcategories = new ArrayList<String>();
    private List<String> checkedAttributes = new ArrayList<String>();

    public HW3(Connection connection, List<String> categories) {

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonExecute);

        buttonExecute.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onExecute(connection);
            }
        });

        buttonClear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onClear();
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
            checkBox.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    JCheckBox check = (JCheckBox) e.getSource();
                    String categoryName = check.getText();
                    if (e.getStateChange() == 1) {
                        checkedCategories.add(categoryName);
                    } else {
                        checkedCategories.remove(categoryName);
                    }
                    System.out.println("Checked categories: "  + Arrays.asList(checkedCategories).toString());

                    // Add to UI
                    JList subcategoryList = new JList();
                    subcategoryList.setLayout(new BoxLayout(subcategoryList, BoxLayout.PAGE_AXIS));
                    subcategoryList.setPreferredSize(new Dimension(200, 650));

                    List<String> subcategories = querySubcategoriesOR(connection, checkedCategories);
                    for (String subcategory : subcategories) {
                        JCheckBox checkBox = new JCheckBox(subcategory);
                        checkBox.addItemListener(new ItemListener() {
                            @Override
                            public void itemStateChanged(ItemEvent e) {
                                JCheckBox check = (JCheckBox) e.getSource();
                                String subcategoryName = check.getText();
                                if (e.getStateChange() == 1) {
                                    checkedSubcategories.add(subcategoryName);
                                } else {
                                    checkedSubcategories.remove(subcategoryName);
                                }
                                System.out.println("Checked subcategories: " + Arrays.asList(checkedSubcategories).toString());

                                // Add to UI
                                JList attributesList = new JList();
                                attributesList.setLayout(new BoxLayout(attributesList, BoxLayout.PAGE_AXIS));
                                attributesList.setPreferredSize(new Dimension(200, 650));

                                List<String> attributes = queryAttributesOR(connection, checkedSubcategories);
                                for (String attr : attributes) {
                                    JCheckBox checkBox = new JCheckBox(attr);
                                    attributesList.add(checkBox);
                                    attributesList.repaint();
                                }

                                attributesPane.setLayout(new ScrollPaneLayout());
                                attributesPane.add(attributesList);
                                attributesPane.setViewportView(attributesList);
                                attributesPane.repaint();

                            }
                        });
                        subcategoryList.add(checkBox);
                        subcategoryList.repaint();
                    }

                    subcategoryPane.setLayout(new ScrollPaneLayout());
                    subcategoryPane.add(subcategoryList);
                    subcategoryPane.setViewportView(subcategoryList);
                    subcategoryPane.repaint();

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

    private void onExecute(Connection conn) {

        // category search only
        if (checkedSubcategories.isEmpty() && checkedAttributes.isEmpty()) {
            if (!checkedCategories.isEmpty()) {
                String categoryClause = "";
                for (String checkedCategory : checkedCategories) {
                    categoryClause += "CATEGORY = ? OR ";
                }
                categoryClause = categoryClause.substring(0, categoryClause.length() - 4); // remove the last ' OR '

                String sql1 = "SELECT BU_ID FROM BU_CATEGORY WHERE (" + categoryClause + ")";
                try {
                    PreparedStatement ps1 = conn.prepareStatement(sql1);
                    for (int i = 1; i <= checkedCategories.size(); i++) {
                        ps1.setString(i, checkedCategories.get(i - 1));
                    }
                    ResultSet rs1 = ps1.executeQuery();

                    List<String> buIds = new ArrayList<String>();
                    String buIdClause = "";
                    while (rs1.next()) {
                        buIdClause += "BU_ID = ? OR ";
                        buIds.add(rs1.getString("BU_ID"));
                    }
                    buIdClause = buIdClause.substring(0, buIdClause.length() - 4); // remove the last ' OR '
                    rs1.close();

                    String sql2 = "SELECT DISTINCT NAME, CITY, STATE, STARS FROM BUSINESS WHERE (" + buIdClause + ")";
                    PreparedStatement ps2 = conn.prepareStatement(sql2);
                    for (int i = 1; i <= buIds.size(); i++) {
                        ps2.setString(i, buIds.get(i - 1));
                    }
                    ResultSet rs2 = ps2.executeQuery();
                    DefaultTableModel model = buildTableModel(rs2);
                    rs2.close();

                    if (model != null) {
                        JTable resultsTable = new JTable(model);
                        resultsScrollPane.add(resultsTable);
                        resultsScrollPane.setViewportView(resultsTable);
                        resultsScrollPane.repaint();
                    }

                } catch (SQLException e) {
                    System.out.println("Exception while querying for businesses: " + e.getMessage());
                }

            }
        }

        if (checkedAttributes.isEmpty()) {
            if (!checkedCategories.isEmpty() && !checkedSubcategories.isEmpty()) {
                String categoryClause = "";
                for (String checkedCategory : checkedCategories) {
                    categoryClause += "CATEGORY = ? OR ";
                }
                categoryClause = categoryClause.substring(0, categoryClause.length() - 4); // remove the last ' OR '

                String sql1 = "SELECT BU_ID FROM BU_CATEGORY WHERE (" + categoryClause + ")";
                try {
                    PreparedStatement ps1 = conn.prepareStatement(sql1);
                    for (int i = 1; i <= checkedCategories.size(); i++) {
                        ps1.setString(i, checkedCategories.get(i - 1));
                    }
                    ResultSet rs1 = ps1.executeQuery();

                    Set<String> buIds = new HashSet<String>();
                    String buIdClause1 = "";
                    while (rs1.next()) {
                        buIdClause1 += "BU_ID = ? OR ";
                        buIds.add(rs1.getString("BU_ID"));
                    }
                    buIdClause1 = buIdClause1.substring(0, buIdClause1.length() - 4); // remove the last ' OR '
                    rs1.close();

                    String sql2 = "SELECT BU_ID FROM BU_SUBCATEGORY WHERE (" + categoryClause + ")";
                    PreparedStatement ps2 = conn.prepareStatement(sql1);
                    for (int i = 1; i <= checkedSubcategories.size(); i++) {
                        ps2.setString(i, checkedSubcategories.get(i - 1));
                    }
                    ResultSet rs2 = ps1.executeQuery();

                    String buIdClause2 = "";
                    while (rs2.next()) {
                        buIdClause2 += "BU_ID = ? OR ";
                        buIds.add(rs2.getString("BU_ID"));
                    }
                    buIdClause2 = buIdClause2.substring(0, buIdClause2.length() - 4); // remove the last ' OR '
                    rs2.close();

                    String buIdClause3 = "";
                    for (String buId : buIds) {
                        buIdClause3 += "BU_ID = ? OR ";
                    }
                    buIdClause3 = buIdClause3.substring(0, buIdClause3.length() - 4); // remove the last ' OR '
                    String sql3 = "SELECT DISTINCT NAME, CITY, STATE, STARS FROM BUSINESS WHERE (" + buIdClause3 + ")";
                    PreparedStatement ps3 = conn.prepareStatement(sql3);
                    Iterator<String> it = buIds.iterator();
                    int i = 1;
                    while (it.hasNext()) {
                        ps3.setString(i, it.next());
                        i++;
                    }
                    ResultSet rs3 = ps3.executeQuery();
                    DefaultTableModel model = buildTableModel(rs3);
                    rs3.close();

                    if (model != null) {
                        JTable resultsTable = new JTable(model);
                        resultsScrollPane.add(resultsTable);
                        resultsScrollPane.setViewportView(resultsTable);
                        resultsScrollPane.repaint();
                    }


                } catch (SQLException e) {
                    System.out.println("Exception while querying for businesses: " + e.getMessage());
                }

            }
        }

    }

    private void onClear() {
        checkedCategories.clear();
        checkedSubcategories.clear();
        checkedAttributes.clear();
        attributesPane.repaint();
        subcategoryPane.repaint();
        categoryPane.repaint();
    }

    private void onCancel() {
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

            HW3 dialog = new HW3(connection, categories);
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
            while (rs.next()) {
                categories.add(rs.getString("CATEGORY"));
            }
            rs.close();
        } catch (SQLException e) {
            System.out.println("Exception while querying for categories: " + e.getMessage());
        }

        return categories;

    }

    private static List<String> queryAllBusinessesUnderCategory(Connection conn, String category) {

        List<String> businesses = new ArrayList<String>();

        String sql = "SELECT BU_ID FROM BU_CATEGORY WHERE CATEGORY = '" + category + "'";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                businesses.add(rs.getString("BU_ID"));
            }
            rs.close();
        } catch (SQLException e) {
            System.out.println("Exception while querying for businesses: " + e.getMessage());
        }

        return businesses;

    }

//    private static List<String> querySubcategoriesOfCheckedCategoriesAND(Connection conn, List<String> checkedCategories) {
//
//        List<String> subcategories = new ArrayList<String>();
//
//        if (checkedCategories.size() > 0) {
//
//            String whereClause = "";
//            for (String checkedCategory : checkedCategories) {
//                whereClause += "CATEGORY = ? AND ";
//            }
//            whereClause = whereClause.substring(0, whereClause.length() - 5); // remove the last ' AND '
//
//            String sql = "SELECT SUBCATEGORY FROM CAT_TO_SUBCAT WHERE (" + whereClause + ")";
//            System.out.println(sql);
//            try {
//                PreparedStatement stmt = conn.prepareStatement(sql);
//                for (int i = 1; i <= checkedCategories.size(); i++) {
//                    stmt.setString(i, checkedCategories.get(i - 1));
//                }
//                ResultSet rs = stmt.executeQuery();
//                while (rs.next()) {
//                    subcategories.add(rs.getString("SUBCATEGORY"));
//                }
//                rs.close();
//            } catch (SQLException e) {
//                System.out.println("Exception while querying for subcategories: " + e.getMessage());
//            }
//
//        }
//        return subcategories;
//
//    }

    private static List<String> querySubcategoriesOR(Connection conn, List<String> checkedCategories) {

        List<String> subcategories = new ArrayList<String>();

        if (!checkedCategories.isEmpty()) {

            String categoryClause = "";
            for (String checkedCategory : checkedCategories) {
                categoryClause += "CATEGORY = ? OR ";
            }
            categoryClause = categoryClause.substring(0, categoryClause.length() - 4); // remove the last ' OR '

            String sql1 = "SELECT BU_ID FROM BU_CATEGORY WHERE (" + categoryClause + ")";
            try {
                PreparedStatement ps1 = conn.prepareStatement(sql1);
                for (int i = 1; i <= checkedCategories.size(); i++) {
                    ps1.setString(i, checkedCategories.get(i - 1));
                }
                ResultSet rs1 = ps1.executeQuery();

                List<String> buIds = new ArrayList<String>();
                String buIdClause = "";
                while (rs1.next()) {
                    buIdClause += "BU_ID = ? OR ";
                    buIds.add(rs1.getString("BU_ID"));
                }
                buIdClause = buIdClause.substring(0, buIdClause.length() - 4); // remove the last ' OR '
                rs1.close();

                String sql2 = "SELECT DISTINCT SUBCATEGORY FROM BU_SUBCATEGORY WHERE (" + buIdClause + ")";
                PreparedStatement ps2 = conn.prepareStatement(sql2);
                for (int i = 1; i <= buIds.size(); i++) {
                    ps2.setString(i, buIds.get(i - 1));
                }
                ResultSet rs2 = ps2.executeQuery();
                while (rs2.next()) {
                    subcategories.add(rs2.getString("SUBCATEGORY"));
                }
                rs2.close();
            } catch (SQLException e) {
                System.out.println("Exception while querying for subcategories: " + e.getMessage());
            }

        }
        return subcategories;

    }

    private static List<String> queryBuIdOR(Connection conn, List<String> checkedSubcategories) {

        List<String> buIds = new ArrayList<String>();

        if (!checkedSubcategories.isEmpty()) {

            String categoryClause = "";
            for (String checkedCategory : checkedSubcategories) {
                categoryClause += "SUBCATEGORY = ? OR ";
            }
            categoryClause = categoryClause.substring(0, categoryClause.length() - 4); // remove the last ' OR '

            String sql1 = "SELECT BU_ID FROM BU_SUBCATEGORY WHERE (" + categoryClause + ")";
            try {
                PreparedStatement ps1 = conn.prepareStatement(sql1);
                for (int i = 1; i <= checkedSubcategories.size(); i++) {
                    ps1.setString(i, checkedSubcategories.get(i - 1));
                }
                ResultSet rs1 = ps1.executeQuery();
                while (rs1.next()) {
                    buIds.add(rs1.getString("BU_ID"));
                }
                rs1.close();
            } catch (SQLException e) {
                System.out.println("Exception while querying for subcategories: " + e.getMessage());
            }

        }
        return buIds;

    }

    private static List<String> queryAttributesOR(Connection conn, List<String> checkedSubcategories) {

        List<String> attributes = new ArrayList<String>();

        List<String> buIds = queryBuIdOR(conn, checkedSubcategories);
        if (!buIds.isEmpty()) {

            String buIdClause = "";
            for (String buId : buIds) {
                buIdClause += "BU_ID = ? OR ";
            }
            buIdClause = buIdClause.substring(0, buIdClause.length() - 4); // remove the last ' OR '

            String sql1 = "SELECT DISTINCT ATTR_NAME, ATTR_VALUE FROM BU_ATTRIBUTE WHERE ((" + buIdClause + ") AND ATTR_VALUE IS NOT NULL)";
            try {
                PreparedStatement ps1 = conn.prepareStatement(sql1);
                for (int i = 1; i <= buIds.size(); i++) {
                    ps1.setString(i, buIds.get(i - 1));
                }
                ResultSet rs1 = ps1.executeQuery();
                while (rs1.next()) {
                    String attr = rs1.getString("ATTR_NAME") + " = " + rs1.getString("ATTR_VALUE");
                    attributes.add(attr);
                }
                rs1.close();
            } catch (SQLException e) {
                System.out.println("Exception while querying for subcategories: " + e.getMessage());
            }

        }
        return attributes;

    }

    private static DefaultTableModel buildTableModel(ResultSet rs) {

        try {

            ResultSetMetaData metaData = rs.getMetaData();

            // names of columns
            Vector<String> columnNames = new Vector<String>();
            int columnCount = metaData.getColumnCount();
            for (int column = 1; column <= columnCount; column++) {
                columnNames.add(metaData.getColumnName(column));
            }

            // data of the table
            Vector<Vector<Object>> data = new Vector<Vector<Object>>();
            while (rs.next()) {
                Vector<Object> vector = new Vector<Object>();
                for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                    vector.add(rs.getObject(columnIndex));
                }
                data.add(vector);
            }

            return new DefaultTableModel(data, columnNames);

        } catch(SQLException e) {
            System.out.println("Exception while building data model for results table: " + e.getMessage());
        }

        return null;
    }

}
