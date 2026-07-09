import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.plaf.FontUIResource;

public class StudentCourseManagementGui extends JFrame {
    private static final String DEFAULT_FILE_NAME = "courses.txt";
    private static final Color BACKGROUND = new Color(246, 248, 251);
    private static final Color SURFACE = Color.WHITE;
    private static final Color TEXT = new Color(32, 41, 57);
    private static final Color MUTED = new Color(91, 103, 122);
    private static final Color PRIMARY = new Color(37, 99, 235);
    private static final Color PRIMARY_DARK = new Color(29, 78, 216);
    private static final Color DANGER = new Color(220, 38, 38);
    private static final Color SUCCESS = new Color(22, 163, 74);
    private static final String FONT_FAMILY = "SansSerif";
    private static final int BASE_WIDTH = 1280;
    private static final int BASE_HEIGHT = 820;
    private static final int MIN_WIDTH = 760;
    private static final int MIN_HEIGHT = 560;

    private final CourseManager courseManager = new CourseManager();
    private final double uiScale;
    private final Font titleFont;
    private final Font subtitleFont;
    private final Font sectionFont;
    private final Font labelFont;
    private final Font fieldFont;
    private final Font buttonFont;
    private final Font tableFont;
    private final Font tableHeaderFont;
    private final JTextField codeField = new JTextField();
    private final JTextField titleField = new JTextField();
    private final JTextField unitField = new JTextField();
    private final JTextField searchField = new JTextField();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Course Code", "Course Title", "Unit"},
            0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable courseTable = new JTable(tableModel);
    private final JLabel totalUnitsLabel = new JLabel("0");
    private final JLabel courseCountLabel = new JLabel("0");
    private final JLabel statusLabel = new JLabel("Ready");

    public StudentCourseManagementGui() {
        super("Student Course Management");
        uiScale = calculateUiScale();
        titleFont = scaledFont(Font.BOLD, 36);
        subtitleFont = scaledFont(Font.PLAIN, 18);
        sectionFont = scaledFont(Font.BOLD, 22);
        labelFont = scaledFont(Font.PLAIN, 16);
        fieldFont = scaledFont(Font.PLAIN, 18);
        buttonFont = scaledFont(Font.BOLD, 17);
        tableFont = scaledFont(Font.PLAIN, 17);
        tableHeaderFont = scaledFont(Font.BOLD, 16);
        configureWindow();
        buildInterface();
        refreshTable();
    }

    private void configureWindow() {
        configureLookAndFeel();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(calculateWindowSize());
        setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
        setLocationRelativeTo(null);
    }

    private void configureLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo lookAndFeel : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(lookAndFeel.getName())) {
                    UIManager.setLookAndFeel(lookAndFeel.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception fallbackIgnored) {
                // The default Swing look and feel is acceptable if bundled options are unavailable.
            }
        }

        FontUIResource defaultFont = new FontUIResource(FONT_FAMILY, Font.PLAIN, scaled(16));
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                UIManager.put(key, defaultFont);
            }
        }
    }

    private void buildInterface() {
        JPanel root = new JPanel(new BorderLayout(scaled(26), scaled(26)));
        root.setBackground(BACKGROUND);
        root.setBorder(new EmptyBorder(scaled(32), scaled(36), scaled(28), scaled(36)));

        root.add(createHeader(), BorderLayout.NORTH);
        root.add(createMainContent(), BorderLayout.CENTER);
        root.add(createStatusBar(), BorderLayout.SOUTH);

        JScrollPane pageScrollPane = new JScrollPane(root);
        pageScrollPane.setBorder(BorderFactory.createEmptyBorder());
        pageScrollPane.getViewport().setBackground(BACKGROUND);
        pageScrollPane.getVerticalScrollBar().setUnitIncrement(scaled(18));
        pageScrollPane.getHorizontalScrollBar().setUnitIncrement(scaled(18));
        setContentPane(pageScrollPane);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(scaled(16), scaled(10)));
        header.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel title = new JLabel("Student Course Management");
        title.setForeground(TEXT);
        title.setFont(titleFont);

        JLabel subtitle = new JLabel("Add, find, save, load, and delete semester courses.");
        subtitle.setForeground(MUTED);
        subtitle.setFont(subtitleFont);

        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(scaled(8)));
        titleBlock.add(subtitle);

        JPanel stats = new JPanel();
        stats.setOpaque(false);
        stats.setLayout(new BoxLayout(stats, BoxLayout.X_AXIS));
        stats.add(createStatBox("Courses", courseCountLabel));
        stats.add(Box.createHorizontalStrut(scaled(14)));
        stats.add(createStatBox("Total Units", totalUnitsLabel));

        header.add(titleBlock, BorderLayout.WEST);
        header.add(stats, BorderLayout.EAST);
        return header;
    }

    private JPanel createMainContent() {
        JPanel content = new JPanel(new BorderLayout(scaled(26), scaled(26)));
        content.setOpaque(false);
        content.add(createFormPanel(), BorderLayout.WEST);
        content.add(createTablePanel(), BorderLayout.CENTER);
        return content;
    }

    private JPanel createFormPanel() {
        JPanel panel = createSurfacePanel();
        panel.setPreferredSize(new Dimension(scaled(390), 10));
        panel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(0, 0, scaled(16), 0);

        addFormTitle(panel, gbc, "Course Details");
        addLabeledField(panel, gbc, "Course Code", codeField);
        addLabeledField(panel, gbc, "Course Title", titleField);
        addLabeledField(panel, gbc, "Unit", unitField);

        JButton addButton = createButton("Add Course", PRIMARY);
        addButton.addActionListener(this::addCourse);
        gbc.gridy++;
        panel.add(addButton, gbc);

        JButton clearButton = createButton("Clear Fields", MUTED);
        clearButton.addActionListener(event -> clearInputs());
        gbc.gridy++;
        panel.add(clearButton, gbc);

        gbc.insets = new Insets(scaled(24), 0, scaled(16), 0);
        addFormTitle(panel, gbc, "Search and Actions");
        addLabeledField(panel, gbc, "Course Code", searchField);

        JButton searchButton = createButton("Search Course", PRIMARY_DARK);
        searchButton.addActionListener(this::searchCourse);
        gbc.gridy++;
        panel.add(searchButton, gbc);

        JButton deleteButton = createButton("Delete Course", DANGER);
        deleteButton.addActionListener(this::deleteCourse);
        gbc.gridy++;
        panel.add(deleteButton, gbc);

        JButton saveButton = createButton("Save to File", SUCCESS);
        saveButton.addActionListener(this::saveCourses);
        gbc.gridy++;
        panel.add(saveButton, gbc);

        JButton loadButton = createButton("Load from File", MUTED);
        loadButton.addActionListener(this::loadCourses);
        gbc.gridy++;
        panel.add(loadButton, gbc);

        gbc.gridy++;
        gbc.weighty = 1;
        panel.add(Box.createVerticalGlue(), gbc);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = createSurfacePanel();
        panel.setLayout(new BorderLayout(0, scaled(18)));

        JLabel title = new JLabel("Recorded Courses");
        title.setForeground(TEXT);
        title.setFont(sectionFont);

        configureTable();
        JScrollPane scrollPane = new JScrollPane(courseTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        scrollPane.getViewport().setBackground(SURFACE);

        panel.add(title, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setOpaque(false);
        statusLabel.setForeground(MUTED);
        statusLabel.setFont(scaledFont(Font.PLAIN, 16));
        statusBar.add(statusLabel, BorderLayout.WEST);
        return statusBar;
    }

    private void configureTable() {
        courseTable.setRowHeight(scaled(50));
        courseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        courseTable.setFont(tableFont);
        courseTable.getTableHeader().setFont(tableHeaderFont);
        courseTable.getTableHeader().setForeground(TEXT);
        courseTable.getTableHeader().setBackground(new Color(241, 245, 249));
        courseTable.getTableHeader().setPreferredSize(new Dimension(0, scaled(46)));
        courseTable.setGridColor(new Color(226, 232, 240));
        courseTable.setShowVerticalLines(false);
        courseTable.setFillsViewportHeight(true);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setBorder(new EmptyBorder(0, scaled(16), 0, scaled(16)));
        courseTable.setDefaultRenderer(Object.class, renderer);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        courseTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        courseTable.getColumnModel().getColumn(0).setPreferredWidth(scaled(180));
        courseTable.getColumnModel().getColumn(1).setPreferredWidth(scaled(560));
        courseTable.getColumnModel().getColumn(2).setPreferredWidth(scaled(100));

        courseTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                fillSelectedCourse();
            }
        });
    }

    private JPanel createSurfacePanel() {
        JPanel panel = new JPanel();
        panel.setBackground(SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(scaled(26), scaled(26), scaled(26), scaled(26))
        ));
        return panel;
    }

    private JPanel createStatBox(String label, JLabel valueLabel) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(scaled(14), scaled(22), scaled(14), scaled(22))
        ));

        JLabel labelView = new JLabel(label);
        labelView.setForeground(MUTED);
        labelView.setFont(scaledFont(Font.PLAIN, 15));
        valueLabel.setForeground(TEXT);
        valueLabel.setFont(scaledFont(Font.BOLD, 30));
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        labelView.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(labelView);
        panel.add(Box.createVerticalStrut(scaled(2)));
        panel.add(valueLabel);
        return panel;
    }

    private void addFormTitle(JPanel panel, GridBagConstraints gbc, String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT);
        label.setFont(sectionFont);
        gbc.gridy++;
        panel.add(label, gbc);
        gbc.insets = new Insets(0, 0, scaled(16), 0);
    }

    private void addLabeledField(JPanel panel, GridBagConstraints gbc, String labelText, JTextField field) {
        JLabel label = new JLabel(labelText);
        label.setForeground(MUTED);
        label.setFont(labelFont);
        gbc.gridy++;
        panel.add(label, gbc);

        field.setFont(fieldFont);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                new EmptyBorder(scaled(12), scaled(14), scaled(12), scaled(14))
        ));
        gbc.gridy++;
        panel.add(field, gbc);
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFont(buttonFont);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(scaled(14), scaled(18), scaled(14), scaled(18)));
        return button;
    }

    private double calculateUiScale() {
        Rectangle bounds = getScreenBounds();
        double widthScale = bounds.getWidth() / 1440.0;
        double heightScale = bounds.getHeight() / 900.0;
        return clamp(Math.min(widthScale, heightScale), 0.85, 1.2);
    }

    private Dimension calculateWindowSize() {
        Rectangle bounds = getScreenBounds();
        int width = Math.min(scaled(BASE_WIDTH), Math.max(MIN_WIDTH, bounds.width - scaled(80)));
        int height = Math.min(scaled(BASE_HEIGHT), Math.max(MIN_HEIGHT, bounds.height - scaled(80)));
        return new Dimension(width, height);
    }

    private Rectangle getScreenBounds() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
    }

    private int scaled(int value) {
        return Math.max(1, (int) Math.round(value * uiScale));
    }

    private Font scaledFont(int style, int size) {
        return new Font(FONT_FAMILY, style, scaled(size));
    }

    private double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private void addCourse(ActionEvent event) {
        String code = codeField.getText();
        String title = titleField.getText();
        int unit;

        try {
            unit = Integer.parseInt(unitField.getText().trim());
            boolean added = courseManager.addCourse(code, title, unit);
            if (!added) {
                showMessage("A course with that code already exists.", JOptionPane.WARNING_MESSAGE);
                return;
            }
            clearInputs();
            refreshTable();
            setStatus("Course added successfully.");
        } catch (NumberFormatException exception) {
            showMessage("Unit must be a valid number.", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException exception) {
            showMessage(exception.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchCourse(ActionEvent event) {
        String code = searchField.getText().trim();
        if (code.isEmpty()) {
            showMessage("Enter a course code to search.", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Course course = courseManager.searchCourse(code);
        if (course == null) {
            showMessage("Course not found.", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        selectCourseInTable(course.getCourseCode());
        showMessage(
                "Code: " + course.getCourseCode()
                        + "\nTitle: " + course.getCourseTitle()
                        + "\nUnit: " + course.getUnit(),
                JOptionPane.INFORMATION_MESSAGE
        );
        setStatus("Course found: " + course.getCourseCode());
    }

    private void deleteCourse(ActionEvent event) {
        String code = searchField.getText().trim();
        if (code.isEmpty()) {
            int selectedRow = courseTable.getSelectedRow();
            if (selectedRow >= 0) {
                code = tableModel.getValueAt(selectedRow, 0).toString();
            }
        }

        if (code.isEmpty()) {
            showMessage("Enter or select a course to delete.", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int answer = JOptionPane.showConfirmDialog(
                this,
                "Delete course " + code.toUpperCase() + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        if (courseManager.deleteCourse(code)) {
            clearInputs();
            refreshTable();
            setStatus("Course deleted successfully.");
        } else {
            showMessage("Course not found.", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void saveCourses(ActionEvent event) {
        try {
            courseManager.saveToFile(DEFAULT_FILE_NAME);
            setStatus("Courses saved to " + DEFAULT_FILE_NAME + ".");
        } catch (IOException exception) {
            showMessage("Could not save courses: " + exception.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCourses(ActionEvent event) {
        try {
            int skippedRecords = courseManager.loadFromFile(DEFAULT_FILE_NAME);
            refreshTable();
            String message = "Courses loaded from " + DEFAULT_FILE_NAME + ".";
            if (skippedRecords > 0) {
                message += " Skipped invalid or duplicate records: " + skippedRecords + ".";
            }
            setStatus(message);
        } catch (IOException exception) {
            showMessage("Could not load courses: " + exception.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        List<Course> courses = courseManager.getCourses();
        for (Course course : courses) {
            tableModel.addRow(new Object[]{
                    course.getCourseCode(),
                    course.getCourseTitle(),
                    course.getUnit()
            });
        }
        courseCountLabel.setText(String.valueOf(courses.size()));
        totalUnitsLabel.setText(String.valueOf(courseManager.computeTotalUnits()));
    }

    private void fillSelectedCourse() {
        int selectedRow = courseTable.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }

        codeField.setText(tableModel.getValueAt(selectedRow, 0).toString());
        titleField.setText(tableModel.getValueAt(selectedRow, 1).toString());
        unitField.setText(tableModel.getValueAt(selectedRow, 2).toString());
        searchField.setText(tableModel.getValueAt(selectedRow, 0).toString());
    }

    private void selectCourseInTable(String courseCode) {
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            if (tableModel.getValueAt(row, 0).toString().equalsIgnoreCase(courseCode)) {
                courseTable.setRowSelectionInterval(row, row);
                courseTable.scrollRectToVisible(courseTable.getCellRect(row, 0, true));
                return;
            }
        }
    }

    private void clearInputs() {
        codeField.setText("");
        titleField.setText("");
        unitField.setText("");
        searchField.setText("");
        courseTable.clearSelection();
        codeField.requestFocusInWindow();
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
    }

    private void showMessage(String message, int messageType) {
        JOptionPane.showMessageDialog(this, message, "Student Course Management", messageType);
    }
}
