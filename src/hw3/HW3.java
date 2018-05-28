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

    private String SEARCH_CRITERIA = "OR";
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
        categoryList.setPreferredSize(new Dimension(200, categories.size() * 23));

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
                        // TODO Clear checked subcategories
                    }
                    System.out.println("Checked categories: " + Arrays.asList(checkedCategories).toString());

                    // Add to UI
                    JList subcategoryList = new JList();
                    subcategoryList.setLayout(new BoxLayout(subcategoryList, BoxLayout.PAGE_AXIS));

                    List<String> subcategories = querySubcategoriesOR(connection, checkedCategories);
                    subcategoryList.setPreferredSize(new Dimension(200, subcategories.size() * 23));

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
                                    // TODO Clear checked attributes
                                }
                                System.out.println("Checked subcategories: " + Arrays.asList(checkedSubcategories).toString());

                                // Add to UI
                                JList attributesList = new JList();
                                attributesList.setLayout(new BoxLayout(attributesList, BoxLayout.PAGE_AXIS));

                                List<String> attributes = queryAttributesOR(connection, checkedCategories, checkedSubcategories);
                                attributesList.setPreferredSize(new Dimension(200, attributes.size() * 23));

                                for (String attr : attributes) {
                                    JCheckBox checkBox = new JCheckBox(attr);
                                    checkBox.addItemListener(new ItemListener() {
                                        @Override
                                        public void itemStateChanged(ItemEvent e) {
                                            JCheckBox check = (JCheckBox) e.getSource();
                                            String attribute = check.getText();
                                            if (e.getStateChange() == 1) {
                                                checkedAttributes.add(attribute);
                                            } else {
                                                checkedAttributes.remove(attribute);
                                            }
                                            System.out.println("Checked attributes: " + Arrays.asList(checkedAttributes).toString());
                                        }
                                    });
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

        if (SEARCH_CRITERIA.equals("OR")) {
            // category search only
            if (checkedSubcategories.isEmpty() && checkedAttributes.isEmpty()) {
                categorySearchOR(conn);
            }
            // category and subcategory search
            else if (checkedAttributes.isEmpty()) {
                categoryAndSubcategorySearchOR(conn);
            }
            // category and subcategory and attribute search
            else {
                categoryAndSubcategoryAndAttributeSearchOR(conn);
            }
        }

    }

    private void onClear() {
        checkedCategories.clear();
        checkedSubcategories.clear();
        checkedAttributes.clear();
        // TODO Reset the UI to initial state
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

    private static List<String> querySubcategoriesOR(Connection conn, List<String> checkedCategories) {

        List<String> subcategories = new ArrayList<String>();

        if (!checkedCategories.isEmpty()) {

            String categoryClause = "";
            for (String checkedCategory : checkedCategories) {
                categoryClause += "A.CATEGORY = ? OR ";
            }
            categoryClause = categoryClause.substring(0, categoryClause.length() - 4); // remove the last ' OR '

            String sql = "SELECT DISTINCT B.SUBCATEGORY FROM BU_CATEGORY A, BU_SUBCATEGORY B WHERE " +
                    "((" + categoryClause + ") AND (A.BU_ID = B.BU_ID)) ORDER BY B.SUBCATEGORY";
            try {
                PreparedStatement ps = conn.prepareStatement(sql);
                for (int i = 1; i <= checkedCategories.size(); i++) {
                    ps.setString(i, checkedCategories.get(i - 1));
                }
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    subcategories.add(rs.getString("SUBCATEGORY"));
                }
                rs.close();

            } catch (SQLException e) {
                System.out.println("Exception while querying for subcategories: " + e.getMessage());
            }

        }

        return subcategories;

    }

    private static List<String> queryAttributesOR(Connection conn, List<String> checkedCategories, List<String> checkedSubcategories) {

        List<String> attributes = new ArrayList<String>();

        if (!checkedSubcategories.isEmpty()) {

            String categoryClause = "";
            for (int i = 0; i < checkedCategories.size(); i++) {
                categoryClause += "A.CATEGORY = ? OR ";
            }
            categoryClause = categoryClause.substring(0, categoryClause.length() - 4); // remove the last ' OR '

            String subcategoryClause = "";
            for (int i = 0; i < checkedSubcategories.size(); i++) {
                subcategoryClause += "B.SUBCATEGORY = ? OR ";
            }
            subcategoryClause = subcategoryClause.substring(0, subcategoryClause.length() - 4); // remove the last ' OR '

            String sql = "SELECT DISTINCT C.ATTR_NAME, C.ATTR_VALUE FROM BU_CATEGORY A, BU_SUBCATEGORY B, BU_ATTRIBUTE C WHERE " +
                    "((" + categoryClause + ") AND (" + subcategoryClause + ") AND (B.BU_ID = C.BU_ID))";
            try {
                PreparedStatement ps = conn.prepareStatement(sql);
                int id = 0;
                for (int i = 1; i <= checkedCategories.size(); i++) {
                    ps.setString((id += 1), checkedCategories.get(i - 1));
                }
                for (int i = 1; i <= checkedSubcategories.size(); i++) {
                    ps.setString((id += 1), checkedSubcategories.get(i - 1));
                }
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String attr = rs.getString("ATTR_NAME") + " = " + rs.getString("ATTR_VALUE");
                    attributes.add(attr);
                }
                rs.close();
            } catch (SQLException e) {
                System.out.println("Exception while querying for attributes: " + e.getMessage());
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

        } catch (SQLException e) {
            System.out.println("Exception while building data model for results table: " + e.getMessage());
        }

        return null;
    }

    private void categorySearchOR(Connection conn) {

        String categoryClause = "";
        for (int i = 0; i < checkedCategories.size(); i++) {
            categoryClause += "CATEGORY = ? OR ";
        }
        categoryClause = categoryClause.substring(0, categoryClause.length() - 4); // remove the last ' OR '

        String sql = "SELECT BU_ID FROM BU_CATEGORY WHERE (" + categoryClause + ")";

        businessSearchOR(conn, checkedCategories, sql);

    }

    private void categoryAndSubcategorySearchOR(Connection conn) {

        String categoryClause = "";
        for (int i = 0; i < checkedCategories.size(); i++) {
            categoryClause += "A.CATEGORY = ? OR ";
        }
        categoryClause = categoryClause.substring(0, categoryClause.length() - 4); // remove the last ' OR '

        String subcategoryClause = "";
        for (int i = 0; i < checkedSubcategories.size(); i++) {
            subcategoryClause += "B.SUBCATEGORY = ? OR ";
        }
        subcategoryClause = subcategoryClause.substring(0, subcategoryClause.length() - 4); // remove the last ' OR '

        String sql = "SELECT A.BU_ID FROM BU_CATEGORY A, BU_SUBCATEGORY B WHERE " +
                "((" + categoryClause + ") AND (" + subcategoryClause + ") AND (A.BU_ID = B.BU_ID))";

        List<String> items = new ArrayList<String>();
        items.addAll(checkedCategories);
        items.addAll(checkedSubcategories);

        businessSearchOR(conn, items, sql);

    }

    private void categoryAndSubcategoryAndAttributeSearchOR(Connection conn) {

        String categoryClause = "";
        for (int i = 0; i < checkedCategories.size(); i++) {
            categoryClause += "A.CATEGORY = ? OR ";
        }
        categoryClause = categoryClause.substring(0, categoryClause.length() - 4); // remove the last ' OR '

        String subcategoryClause = "";
        for (int i = 0; i < checkedSubcategories.size(); i++) {
            subcategoryClause += "B.SUBCATEGORY = ? OR ";
        }
        subcategoryClause = subcategoryClause.substring(0, subcategoryClause.length() - 4); // remove the last ' OR '

        String attributeClause = "";
        List<String> attrItems = new ArrayList<String>();
        for (String checkedAttribute : checkedAttributes) {
            attributeClause += "(C.ATTR_NAME = ? AND C.ATTR_VALUE = ?) OR ";
            String k = checkedAttribute.split(" = ")[0];
            String v = checkedAttribute.split(" = ")[1];
            attrItems.add(k);
            attrItems.add(v);
        }
        attributeClause = attributeClause.substring(0, attributeClause.length() - 4); // remove the last ' OR '

        String sql = "SELECT C.BU_ID FROM BU_CATEGORY A, BU_SUBCATEGORY B, BU_ATTRIBUTE C WHERE " +
                "((" + categoryClause + ") AND (" + subcategoryClause + ") AND (" + attributeClause + ") AND (A.BU_ID = B.BU_ID) AND (B.BU_ID = C.BU_ID))";

        List<String> items = new ArrayList<String>();
        items.addAll(checkedCategories);
        items.addAll(checkedSubcategories);
        items.addAll(attrItems);

        businessSearchOR(conn, items, sql);

    }

    private void businessSearchOR(Connection conn, List<String> items, String inClause) {

        System.out.print("Searching... ");
        try {
            // Get name, city, state, and stars of each bu_id
            String sql = "SELECT DISTINCT NAME, CITY, STATE, STARS FROM BUSINESS WHERE BU_ID IN (" + inClause + ") ORDER BY STARS DESC";
            System.out.println(sql);
            System.out.println(Arrays.asList(items).toString());
            PreparedStatement ps = conn.prepareStatement(sql);
            for (int i = 1; i <= items.size(); i++) {
                ps.setString(i, items.get(i - 1));
            }
            ResultSet rs = ps.executeQuery();
            DefaultTableModel model = buildTableModel(rs);
            rs.close();

            if (model != null) {
                JTable resultsTable = new JTable(model);
                resultsScrollPane.add(resultsTable);
                resultsScrollPane.setViewportView(resultsTable);
                resultsScrollPane.repaint();
            }

        } catch (SQLException e) {
            System.out.println("Exception while querying for businesses: " + e.getMessage());
        }
        System.out.print("Done.\n");
    }
}
