import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.IOException;
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
    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 36);
    private static final Font SUBTITLE_FONT = new Font("SansSerif", Font.PLAIN, 18);
    private static final Font SECTION_FONT = new Font("SansSerif", Font.BOLD, 22);
    private static final Font LABEL_FONT = new Font("SansSerif", Font.PLAIN, 16);
    private static final Font FIELD_FONT = new Font("SansSerif", Font.PLAIN, 18);
    private static final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 17);
    private static final Font TABLE_FONT = new Font("SansSerif", Font.PLAIN, 17);
    private static final Font TABLE_HEADER_FONT = new Font("SansSerif", Font.BOLD, 16);

    private final CourseManager courseManager = new CourseManager();
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
        configureWindow();
        buildInterface();
        refreshTable();
    }

    private void configureWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 820);
        setMinimumSize(new Dimension(1100, 720));
        setLocationRelativeTo(null);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // The default Swing look and feel is acceptable if the system one is unavailable.
        }
    }

    private void buildInterface() {
        JPanel root = new JPanel(new BorderLayout(26, 26));
        root.setBackground(BACKGROUND);
        root.setBorder(new EmptyBorder(32, 36, 28, 36));
        setContentPane(root);

        root.add(createHeader(), BorderLayout.NORTH);
        root.add(createMainContent(), BorderLayout.CENTER);
        root.add(createStatusBar(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 10));
        header.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel title = new JLabel("Student Course Management");
        title.setForeground(TEXT);
        title.setFont(TITLE_FONT);

        JLabel subtitle = new JLabel("Add, find, save, load, and delete semester courses.");
        subtitle.setForeground(MUTED);
        subtitle.setFont(SUBTITLE_FONT);

        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(8));
        titleBlock.add(subtitle);

        JPanel stats = new JPanel();
        stats.setOpaque(false);
        stats.setLayout(new BoxLayout(stats, BoxLayout.X_AXIS));
        stats.add(createStatBox("Courses", courseCountLabel));
        stats.add(Box.createHorizontalStrut(14));
        stats.add(createStatBox("Total Units", totalUnitsLabel));

        header.add(titleBlock, BorderLayout.WEST);
        header.add(stats, BorderLayout.EAST);
        return header;
    }

    private JPanel createMainContent() {
        JPanel content = new JPanel(new BorderLayout(26, 26));
        content.setOpaque(false);
        content.add(createFormPanel(), BorderLayout.WEST);
        content.add(createTablePanel(), BorderLayout.CENTER);
        return content;
    }

    private JPanel createFormPanel() {
        JPanel panel = createSurfacePanel();
        panel.setPreferredSize(new Dimension(390, 10));
        panel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(0, 0, 16, 0);

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

        gbc.insets = new Insets(24, 0, 16, 0);
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
        panel.setLayout(new BorderLayout(0, 18));

        JLabel title = new JLabel("Recorded Courses");
        title.setForeground(TEXT);
        title.setFont(SECTION_FONT);

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
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        statusBar.add(statusLabel, BorderLayout.WEST);
        return statusBar;
    }

    private void configureTable() {
        courseTable.setRowHeight(50);
        courseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        courseTable.setFont(TABLE_FONT);
        courseTable.getTableHeader().setFont(TABLE_HEADER_FONT);
        courseTable.getTableHeader().setForeground(TEXT);
        courseTable.getTableHeader().setBackground(new Color(241, 245, 249));
        courseTable.getTableHeader().setPreferredSize(new Dimension(0, 46));
        courseTable.setGridColor(new Color(226, 232, 240));
        courseTable.setShowVerticalLines(false);
        courseTable.setFillsViewportHeight(true);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setBorder(new EmptyBorder(0, 16, 0, 16));
        courseTable.setDefaultRenderer(Object.class, renderer);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        courseTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        courseTable.getColumnModel().getColumn(0).setPreferredWidth(180);
        courseTable.getColumnModel().getColumn(1).setPreferredWidth(560);
        courseTable.getColumnModel().getColumn(2).setPreferredWidth(100);

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
                new EmptyBorder(26, 26, 26, 26)
        ));
        return panel;
    }

    private JPanel createStatBox(String label, JLabel valueLabel) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(14, 22, 14, 22)
        ));

        JLabel labelView = new JLabel(label);
        labelView.setForeground(MUTED);
        labelView.setFont(new Font("SansSerif", Font.PLAIN, 15));
        valueLabel.setForeground(TEXT);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 30));
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        labelView.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(labelView);
        panel.add(Box.createVerticalStrut(2));
        panel.add(valueLabel);
        return panel;
    }

    private void addFormTitle(JPanel panel, GridBagConstraints gbc, String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT);
        label.setFont(SECTION_FONT);
        gbc.gridy++;
        panel.add(label, gbc);
        gbc.insets = new Insets(0, 0, 16, 0);
    }

    private void addLabeledField(JPanel panel, GridBagConstraints gbc, String labelText, JTextField field) {
        JLabel label = new JLabel(labelText);
        label.setForeground(MUTED);
        label.setFont(LABEL_FONT);
        gbc.gridy++;
        panel.add(label, gbc);

        field.setFont(FIELD_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                new EmptyBorder(12, 14, 12, 14)
        ));
        gbc.gridy++;
        panel.add(field, gbc);
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFont(BUTTON_FONT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(14, 18, 14, 18));
        return button;
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
