import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import javax.swing.Scrollable;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.JTableHeader;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.plaf.FontUIResource;

public class StudentCourseManagementGui extends JFrame {
    private static final String DEFAULT_FILE_NAME = "courses.txt";
    private static final Color BACKGROUND = new Color(246, 248, 251);
    private static final Color SURFACE = Color.WHITE;
    private static final Color SURFACE_MUTED = new Color(249, 250, 252);
    private static final Color BORDER = new Color(226, 232, 240);
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
    private static final int MIN_HEIGHT = 680;
    private static final int STACKED_HEADER_WIDTH = 920;
    private static final int STACKED_MAIN_WIDTH = 520;

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
    private JPanel headerPanel;
    private JPanel titleBlock;
    private JPanel statsPanel;
    private JPanel mainContent;
    private JScrollPane formScrollPane;
    private JPanel tablePanel;
    private boolean stackedLayout;
    private boolean stackedHeader;
    private boolean hasUnsavedChanges;
    private String selectedCourseCode;

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
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(calculateWindowSize());
        setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                requestExit();
            }
        });
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
        mainContent = createMainContent();
        root.add(mainContent, BorderLayout.CENTER);
        root.add(createStatusBar(), BorderLayout.SOUTH);
        setContentPane(root);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                updateResponsiveLayout();
            }
        });
        updateResponsiveLayout();
    }

    private JPanel createHeader() {
        headerPanel = new JPanel();
        headerPanel.setOpaque(false);

        titleBlock = new JPanel();
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

        statsPanel = new JPanel();
        statsPanel.setOpaque(false);
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.X_AXIS));
        statsPanel.add(createStatBox("Courses", courseCountLabel));
        statsPanel.add(Box.createHorizontalStrut(scaled(14)));
        statsPanel.add(createStatBox("Total Units", totalUnitsLabel));

        return headerPanel;
    }

    private JPanel createMainContent() {
        JPanel content = new JPanel();
        content.setOpaque(false);
        formScrollPane = createFormScrollPane();
        tablePanel = createTablePanel();
        return content;
    }

    private JScrollPane createFormScrollPane() {
        JScrollPane scrollPane = new JScrollPane(createFormPanel());
        styleScrollPane(scrollPane, BACKGROUND);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setPreferredSize(new Dimension(scaled(460), 10));
        scrollPane.setMinimumSize(new Dimension(scaled(280), scaled(180)));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(scaled(18));
        return scrollPane;
    }

    private JPanel createFormPanel() {
        JPanel panel = new ScrollablePanel();
        panel.setLayout(new GridBagLayout());
        styleSurfacePanel(panel);

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
        clearButton.addActionListener(_ -> clearInputs());
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

    private void updateResponsiveLayout() {
        int availableWidth = getContentPane().getWidth();
        updateHeaderLayout(availableWidth);
        updateMainContentLayout(availableWidth);
    }

    private void updateHeaderLayout(int availableWidth) {
        if (headerPanel == null || titleBlock == null || statsPanel == null) {
            return;
        }

        boolean shouldStack = availableWidth > 0 && availableWidth < scaled(STACKED_HEADER_WIDTH);
        if (shouldStack == stackedHeader && headerPanel.getComponentCount() > 0) {
            return;
        }

        stackedHeader = shouldStack;
        headerPanel.removeAll();
        headerPanel.setLayout(new BorderLayout(scaled(16), scaled(10)));

        if (stackedHeader) {
            headerPanel.add(titleBlock, BorderLayout.NORTH);
            headerPanel.add(statsPanel, BorderLayout.SOUTH);
        } else {
            headerPanel.add(titleBlock, BorderLayout.WEST);
            headerPanel.add(statsPanel, BorderLayout.EAST);
        }

        headerPanel.revalidate();
        headerPanel.repaint();
    }

    private void updateMainContentLayout(int availableWidth) {
        if (mainContent == null || formScrollPane == null || tablePanel == null) {
            return;
        }

        boolean shouldStack = availableWidth > 0 && availableWidth < scaled(STACKED_MAIN_WIDTH);
        if (shouldStack == stackedLayout && mainContent.getComponentCount() > 0 && !stackedLayout) {
            return;
        }

        stackedLayout = shouldStack;
        mainContent.removeAll();
        mainContent.setLayout(new BorderLayout(scaled(26), scaled(26)));

        if (stackedLayout) {
            int formHeight = Math.clamp(scaled(430), scaled(260), getHeight() / 2);
            formScrollPane.setPreferredSize(new Dimension(10, formHeight));
            mainContent.add(formScrollPane, BorderLayout.NORTH);
        } else {
            int formWidth = Math.clamp(scaled(280), availableWidth / 3, scaled(460));
            formScrollPane.setPreferredSize(new Dimension(formWidth, 10));
            mainContent.add(formScrollPane, BorderLayout.WEST);
        }

        mainContent.add(tablePanel, BorderLayout.CENTER);
        mainContent.revalidate();
        mainContent.repaint();
    }

    private JPanel createTablePanel() {
        JPanel panel = createSurfacePanel();
        panel.setLayout(new BorderLayout(0, scaled(18)));

        JLabel title = new JLabel("Recorded Courses");
        title.setForeground(TEXT);
        title.setFont(sectionFont);

        configureTable();
        JScrollPane scrollPane = new JScrollPane(courseTable);
        styleScrollPane(scrollPane, SURFACE);

        panel.add(title, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setOpaque(false);

        JButton exitButton = createButton("Exit", TEXT);
        exitButton.addActionListener(_ -> requestExit());
        exitButton.setMaximumSize(new Dimension(scaled(180), exitButton.getPreferredSize().height));

        statusLabel.setForeground(MUTED);
        statusLabel.setFont(scaledFont(Font.PLAIN, 16));

        statusBar.add(statusLabel, BorderLayout.WEST);
        statusBar.add(exitButton, BorderLayout.EAST);
        return statusBar;
    }

    private void configureTable() {
        courseTable.setRowHeight(scaled(48));
        courseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        courseTable.setFont(tableFont);
        courseTable.setForeground(TEXT);
        courseTable.setSelectionBackground(new Color(219, 234, 254));
        courseTable.setSelectionForeground(TEXT);
        courseTable.setIntercellSpacing(new Dimension(0, 0));
        courseTable.setGridColor(BORDER);
        courseTable.setShowVerticalLines(false);
        courseTable.setShowHorizontalLines(true);
        courseTable.setFillsViewportHeight(true);
        courseTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JTableHeader header = courseTable.getTableHeader();
        header.setFont(tableHeaderFont);
        header.setForeground(TEXT);
        header.setBackground(new Color(241, 245, 249));
        header.setPreferredSize(new Dimension(0, scaled(44)));
        header.setReorderingAllowed(false);
        header.setResizingAllowed(true);

        setColumnStyle(0, scaled(150), SwingConstants.CENTER);
        setColumnStyle(1, scaled(560), SwingConstants.LEFT);
        setColumnStyle(2, scaled(100), SwingConstants.CENTER);

        courseTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                fillSelectedCourse();
            }
        });
    }

    private void setColumnStyle(int columnIndex, int preferredWidth, int alignment) {
        courseTable.getColumnModel().getColumn(columnIndex).setPreferredWidth(preferredWidth);
        courseTable.getColumnModel().getColumn(columnIndex).setCellRenderer(new CourseTableCellRenderer(alignment));
        courseTable.getColumnModel().getColumn(columnIndex).setHeaderRenderer(new CourseTableHeaderRenderer(alignment));
    }

    private JPanel createSurfacePanel() {
        JPanel panel = new JPanel();
        styleSurfacePanel(panel);
        return panel;
    }

    private void styleSurfacePanel(JPanel panel) {
        panel.setBackground(SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(scaled(26), scaled(26), scaled(26), scaled(26))
        ));
    }

    private JPanel createStatBox(String label, JLabel valueLabel) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
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
        field.setForeground(TEXT);
        field.setBackground(SURFACE);
        field.setCaretColor(PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                new EmptyBorder(scaled(12), scaled(14), scaled(12), scaled(14))
        ));
        gbc.gridy++;
        panel.add(field, gbc);
    }

    private JButton createButton(String text, Color color) {
        JButton button = new FlatButton(text, color);
        button.setFocusPainted(false);
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFont(buttonFont);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.setBorder(new EmptyBorder(scaled(14), scaled(18), scaled(14), scaled(18)));
        return button;
    }

    private void styleScrollPane(JScrollPane scrollPane, Color viewportColor) {
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER));
        scrollPane.getViewport().setBackground(viewportColor);
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(scaled(10), 0));
        scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, scaled(10)));
        scrollPane.getVerticalScrollBar().setUnitIncrement(scaled(18));
        scrollPane.getHorizontalScrollBar().setUnitIncrement(scaled(18));
    }

    private double calculateUiScale() {
        Rectangle bounds = getScreenBounds();
        double widthScale = bounds.getWidth() / 1440.0;
        double heightScale = bounds.getHeight() / 900.0;
        return clamp(Math.min(widthScale, heightScale));
    }

    private Dimension calculateWindowSize() {
        Rectangle bounds = getScreenBounds();
        int width = Math.clamp(bounds.width - scaled(80), MIN_WIDTH, scaled(BASE_WIDTH));
        int height = Math.clamp(bounds.height - scaled(80), MIN_HEIGHT, scaled(BASE_HEIGHT));
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

    private double clamp(double value) {
        return Math.clamp(value, 0.82, 1.18);
    }

    private Color blendColor(Color base, Color overlay, double amount) {
        double baseAmount = 1.0 - amount;
        int red = (int) Math.round(base.getRed() * baseAmount + overlay.getRed() * amount);
        int green = (int) Math.round(base.getGreen() * baseAmount + overlay.getGreen() * amount);
        int blue = (int) Math.round(base.getBlue() * baseAmount + overlay.getBlue() * amount);
        return new Color(red, green, blue);
    }

    private class ScrollablePanel extends JPanel implements Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return scaled(18);
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return Math.max(scaled(80), visibleRect.height - scaled(36));
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    private class CourseTableCellRenderer extends DefaultTableCellRenderer {
        CourseTableCellRenderer(int alignment) {
            setHorizontalAlignment(alignment);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(new EmptyBorder(0, scaled(14), 0, scaled(14)));
            if (!isSelected) {
                setBackground(row % 2 == 0 ? SURFACE : SURFACE_MUTED);
                setForeground(TEXT);
            }
            return this;
        }
    }

    private class CourseTableHeaderRenderer extends DefaultTableCellRenderer {
        CourseTableHeaderRenderer(int alignment) {
            setHorizontalAlignment(alignment);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setFont(tableHeaderFont);
            setForeground(TEXT);
            setBackground(new Color(241, 245, 249));
            setBorder(new EmptyBorder(0, scaled(14), 0, scaled(14)));
            return this;
        }
    }

    private class ModernScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            thumbColor = new Color(148, 163, 184);
            trackColor = new Color(241, 245, 249);
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createInvisibleButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createInvisibleButton();
        }

        @Override
        protected void paintTrack(Graphics graphics, JComponent component, Rectangle trackBounds) {
            Graphics2D graphics2D = (Graphics2D) graphics.create();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics2D.setColor(trackColor);
            graphics2D.fillRoundRect(
                    trackBounds.x,
                    trackBounds.y,
                    trackBounds.width,
                    trackBounds.height,
                    scaled(10),
                    scaled(10)
            );
            graphics2D.dispose();
        }

        @Override
        protected void paintThumb(Graphics graphics, JComponent component, Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
                return;
            }

            Graphics2D graphics2D = (Graphics2D) graphics.create();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics2D.setColor(isDragging ? new Color(100, 116, 139) : thumbColor);
            graphics2D.fillRoundRect(
                    thumbBounds.x + scaled(2),
                    thumbBounds.y + scaled(2),
                    thumbBounds.width - scaled(4),
                    thumbBounds.height - scaled(4),
                    scaled(10),
                    scaled(10)
            );
            graphics2D.dispose();
        }

        private JButton createInvisibleButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }
    }

    private class FlatButton extends JButton {
        private final Color baseColor;

        FlatButton(String text, Color baseColor) {
            super(text);
            this.baseColor = baseColor;
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D graphics2D = (Graphics2D) graphics.create();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int arc = scaled(8);
            int shadowOffset = scaled(2);
            Color fillColor = baseColor;
            if (!isEnabled()) {
                fillColor = new Color(148, 163, 184);
            } else if (getModel().isPressed()) {
                fillColor = blendColor(baseColor, Color.BLACK, 0.14);
            } else if (getModel().isRollover()) {
                fillColor = blendColor(baseColor, Color.WHITE, 0.10);
            }

            graphics2D.setColor(new Color(15, 23, 42, getModel().isPressed() ? 18 : 32));
            graphics2D.fillRoundRect(0, shadowOffset, getWidth(), getHeight() - shadowOffset, arc, arc);
            graphics2D.setColor(fillColor);
            graphics2D.fillRoundRect(0, 0, getWidth(), getHeight() - shadowOffset, arc, arc);
            graphics2D.setColor(new Color(255, 255, 255, 42));
            graphics2D.drawRoundRect(0, 0, getWidth() - 1, getHeight() - shadowOffset - 1, arc, arc);
            graphics2D.dispose();
            super.paintComponent(graphics);
        }
    }

    private void addCourse(ActionEvent event) {
        String code = codeField.getText();
        String title = titleField.getText();
        int unit;

        try {
            unit = Integer.parseInt(unitField.getText().trim());
            if (selectedCourseCode != null) {
                if (!code.trim().equalsIgnoreCase(selectedCourseCode)) {
                    showMessage("Course code cannot be changed while editing a selected course.", JOptionPane.WARNING_MESSAGE);
                    codeField.setText(selectedCourseCode);
                    return;
                }

                courseManager.updateCourse(selectedCourseCode, title, unit);
                hasUnsavedChanges = true;
                refreshTable();
                selectCourseInTable(selectedCourseCode);
                setStatus("Course updated successfully.");
                return;
            }

            boolean added = courseManager.addCourse(code, title, unit);
            if (!added) {
                showMessage("A course with that code already exists. Select it from the table to edit it.", JOptionPane.WARNING_MESSAGE);
                return;
            }
            clearInputs();
            hasUnsavedChanges = true;
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

        selectCourseInTable(course.courseCode());
        showMessage(
                "Code: " + course.courseCode()
                        + "\nTitle: " + course.courseTitle()
                        + "\nUnit: " + course.unit(),
                JOptionPane.INFORMATION_MESSAGE
        );
        setStatus("Course found: " + course.courseCode());
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
            hasUnsavedChanges = true;
            refreshTable();
            setStatus("Course deleted successfully.");
        } else {
            showMessage("Course not found.", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void saveCourses(ActionEvent event) {
        saveCoursesToFile();
    }

    private boolean saveCoursesToFile() {
        try {
            courseManager.saveToFile(DEFAULT_FILE_NAME);
            hasUnsavedChanges = false;
            setStatus("Courses saved to " + DEFAULT_FILE_NAME + ".");
            return true;
        } catch (IOException exception) {
            showMessage("Could not save courses: " + exception.getMessage(), JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void loadCourses(ActionEvent event) {
        try {
            int skippedRecords = courseManager.loadFromFile(DEFAULT_FILE_NAME);
            refreshTable();
            hasUnsavedChanges = false;
            String message = "Courses loaded from " + DEFAULT_FILE_NAME + ".";
            if (skippedRecords > 0) {
                message += " Skipped invalid or duplicate records: " + skippedRecords + ".";
            }
            setStatus(message);
        } catch (IOException exception) {
            showMessage("Could not load courses: " + exception.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void requestExit() {
        if (hasUnsavedChanges) {
            int answer = JOptionPane.showConfirmDialog(
                    this,
                    "You have unsaved changes. Save before exiting?",
                    "Unsaved Changes",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (answer == JOptionPane.CANCEL_OPTION || answer == JOptionPane.CLOSED_OPTION) {
                return;
            }

            if (answer == JOptionPane.YES_OPTION && !saveCoursesToFile()) {
                return;
            }
        }

        exitProgram();
    }

    private void exitProgram() {
        dispose();
        System.exit(0);
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        List<Course> courses = courseManager.getCourses();
        for (Course course : courses) {
            tableModel.addRow(new Object[]{
                    course.courseCode(),
                    course.courseTitle(),
                    course.unit()
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
        selectedCourseCode = codeField.getText();
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
        selectedCourseCode = null;
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
