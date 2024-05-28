package rishibanerjee;

import com.formdev.flatlaf.intellijthemes.FlatArcDarkOrangeIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatArcOrangeIJTheme;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
//import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.print.PrinterException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.UnsupportedLookAndFeelException;


public class PaperTrail extends JFrame
{
    private static int openWindowsCount = 0;
    JTabbedPane tabbedPane;
    JFileChooser fileChooser;
    String currentFindText = "";
    UndoManager undoManager;
    JTextArea currentTextArea;
    JCheckBoxMenuItem wordWrapItem;
    JLabel statusLabel;
    Preferences preferences;
    private float zoomFactor;
    private JMenuItem zoomIn, zoomOut, restoreZoom;
    private JCheckBoxMenuItem statusBarToggle;
    private String currentFilePath;
    JPanel settingsPanel;
    private int defaultTextSize = 16;
    private String defaultFontName = "Consolas";

    PaperTrail() throws UnsupportedLookAndFeelException
    {
        openWindowsCount++;
        setTitle("PaperTrail");
        setSize(800,600);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() 
        {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) 
            {
                closeWindow(); 
            }
        });
        //ImageIcon icon = new ImageIcon(getClass().getResource("notepad.jpg"));
        //setIconImage(icon.getImage());

        tabbedPane = new JTabbedPane();
        fileChooser = new JFileChooser();
        preferences = Preferences.userRoot().node(this.getClass().getName());

        add(tabbedPane, BorderLayout.CENTER);
        createMenuBar();
        createStatusBar();

        // Adding first tab by default
        addNewTab();
        updateCurrentTextArea();
        loadPreferences(); 
    }

    private void closeWindow() 
    {
        openWindowsCount--; 
        if (openWindowsCount <= 0) 
        { 
            dispose();
            System.exit(0);
        } 
        else 
        {
            dispose();
        }
    }

    private void createMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();

        // File menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem newFile = new JMenuItem("New");
        JMenuItem newWindow = new JMenuItem("New Window");
        JMenuItem openFile = new JMenuItem("Open");
        JMenuItem saveFile = new JMenuItem("Save");
        JMenuItem saveAsFile = new JMenuItem("Save As");
        JMenuItem printFile = new JMenuItem("Print");
        JMenuItem closeTab = new JMenuItem("Close Tab");
        JMenuItem closeWindow = new JMenuItem("Close Window");
        fileMenu.add(newFile);
        fileMenu.add(newWindow);
        fileMenu.add(openFile);
        fileMenu.add(saveFile);
        fileMenu.add(saveAsFile);
        fileMenu.add(printFile);
        fileMenu.add(closeTab);
        fileMenu.add(closeWindow);

        // Edit menu
        JMenu editMenu = new JMenu("Edit");
        JMenuItem cut = new JMenuItem("Cut");
        JMenuItem copy = new JMenuItem("Copy");
        JMenuItem paste = new JMenuItem("Paste");
        JMenuItem undo = new JMenuItem("Undo");
        JMenuItem redo = new JMenuItem("Redo");
        JMenuItem find = new JMenuItem("Find");
        JMenuItem findNext = new JMenuItem("Find Next");
        JMenuItem findPrevious = new JMenuItem("Find Previous");
        JMenuItem replace = new JMenuItem("Replace");
        JMenuItem insertDate = new JMenuItem("Insert Date/Time");
        JMenuItem selectAll = new JMenuItem("Select All");
        JMenuItem delete = new JMenuItem("Delete");
        JMenuItem fontSettings = new JMenuItem("Font");

        editMenu.add(undo);
        editMenu.add(redo);
        editMenu.add(cut);
        editMenu.add(copy);
        editMenu.add(paste);
        editMenu.add(delete);
        editMenu.add(find);
        editMenu.add(findNext);
        editMenu.add(findPrevious);
        editMenu.add(replace);
        editMenu.add(selectAll);
        editMenu.add(insertDate);
        editMenu.add(fontSettings);

        // View menu
        JMenu viewMenu = new JMenu("View");
        zoomIn = new JMenuItem("Zoom In");
        zoomOut = new JMenuItem("Zoom Out");
        restoreZoom = new JMenuItem("Restore Default Zoom");
        statusBarToggle = new JCheckBoxMenuItem("Status Bar");
        statusBarToggle.setSelected(true);
        wordWrapItem = new JCheckBoxMenuItem("Word Wrap");
        wordWrapItem.setSelected(true);
        viewMenu.add(zoomIn);
        viewMenu.add(zoomOut);
        viewMenu.add(restoreZoom);
        viewMenu.add(statusBarToggle);
        viewMenu.add(wordWrapItem);

        // Settings menu
        JButton settingsMenu = new JButton("Settings");
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.X_AXIS));
        menuPanel.add(Box.createHorizontalGlue());
        menuPanel.add(settingsMenu);
        
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        menuBar.add(menuPanel);

        setJMenuBar(menuBar);

        // Action listeners
        newFile.addActionListener(e -> addNewTab());
        newWindow.addActionListener(e -> {
            try {
                new PaperTrail().setVisible(true);
            } catch (UnsupportedLookAndFeelException ex) {
                Logger.getLogger(PaperTrail.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        openFile.addActionListener(e -> openFile());
        saveFile.addActionListener(e -> saveFile(false));
        saveAsFile.addActionListener(e -> saveFile(true));
        printFile.addActionListener(e -> printFile());
        closeTab.addActionListener(e -> closeCurrentTab());
        closeWindow.addActionListener(e -> dispose());
        cut.addActionListener(e -> getCurrentTextArea().cut());
        copy.addActionListener(e -> getCurrentTextArea().copy());
        paste.addActionListener(e -> getCurrentTextArea().paste());
        undo.addActionListener(e -> undoAction());
        redo.addActionListener(e -> redoAction());
        find.addActionListener(e -> findText());
        findNext.addActionListener(e -> findNextText());
        findPrevious.addActionListener(e -> findPreviousText());
        replace.addActionListener(e -> replaceText());
        insertDate.addActionListener(e -> insertDateTime());
        selectAll.addActionListener(e -> getCurrentTextArea().selectAll());
        delete.addActionListener(e -> getCurrentTextArea().replaceSelection(""));
        fontSettings.addActionListener(e -> openSettingsPage());
        zoomIn.addActionListener(e -> zoomIn());
        zoomOut.addActionListener(e -> zoomOut());
        restoreZoom.addActionListener(e -> restoreDefaultZoom());
        statusBarToggle.addActionListener(e -> toggleStatusBar());
        wordWrapItem.addActionListener(e -> toggleWordWrap());
        settingsMenu.addActionListener(e -> openSettingsPage());

        newFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));
        newWindow.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));
        openFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
        saveFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
        saveAsFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));
        printFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK));
        closeTab.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_DOWN_MASK));
        closeWindow.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));
        cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK));
        copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK));
        paste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK));
        undo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK));
        redo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK));
        find.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK));
        findNext.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
        findPrevious.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, KeyEvent.SHIFT_DOWN_MASK));
        replace.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK));
        insertDate.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        zoomIn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, KeyEvent.CTRL_DOWN_MASK));
        zoomOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, KeyEvent.CTRL_DOWN_MASK));
        restoreZoom.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, KeyEvent.CTRL_DOWN_MASK));
        selectAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK));
        delete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
    }

    private void createStatusBar()
    {
        statusLabel = new JLabel("Line: 1, Column: 1");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        add(statusLabel, BorderLayout.SOUTH);
    }

    private void addNewTab()
    {
        JTextArea textArea = createTextArea();
        JScrollPane scrollPane = new JScrollPane(textArea);
        tabbedPane.addTab("Untitled*", scrollPane);
        tabbedPane.setTabComponentAt(tabbedPane.getTabCount() - 1, new TabComponent(tabbedPane));
        tabbedPane.setSelectedComponent(scrollPane);

        TabComponent tabComponent = (TabComponent) tabbedPane.getTabComponentAt(tabbedPane.getTabCount() - 1);
        tabComponent.setPreferredSize(new Dimension(150, 30));
    }

    private JTextArea createTextArea()
    {
        JTextArea textArea = new JTextArea();
        textArea.setFont(new Font("Arial", Font.PLAIN, 11));
        undoManager = new UndoManager();
        textArea.getDocument().addUndoableEditListener(undoManager);
        textArea.getDocument().addDocumentListener(new DocumentListener()
        {
            @Override
            public void insertUpdate(DocumentEvent e)
            {
                markAsModified();
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                markAsModified();
            }

            @Override
            public void changedUpdate(DocumentEvent e)
            {
                markAsModified();
            }

            private void markAsModified()
            {
                String title = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());
                if (!title.endsWith("*"))
                {
                    tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), title + "*");
                }
            }
        });
        textArea.addCaretListener(e -> updateStatusBar());
        textArea.setWrapStyleWord(wordWrapItem.isSelected());
        textArea.setLineWrap(wordWrapItem.isSelected());
        return textArea;
    }

    private void updateStatusBar()
    {
        JTextArea textArea = getCurrentTextArea();
        int caretPos = textArea.getCaretPosition();
        int lineNum = 0;
        int colNum = 0;

        try
        {
            lineNum = textArea.getLineOfOffset(caretPos);
            colNum = caretPos - textArea.getLineStartOffset(lineNum);
        }
        catch (BadLocationException e)
        {
        }
        statusLabel.setText("Line: " + (lineNum + 1) + ", Column: " + (colNum + 1));
    }

    private JTextArea getCurrentTextArea()
    {
        JScrollPane scrollPane = (JScrollPane) tabbedPane.getSelectedComponent();
        return (JTextArea) scrollPane.getViewport().getView();
    }

    private void openFile()
    {
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION)
        {
            File file = fileChooser.getSelectedFile();
            try (BufferedReader br = new BufferedReader(new FileReader(file)))
            {
                JTextArea textArea = createTextArea();
                textArea.read(br, null);

                JScrollPane scrollPane = new JScrollPane(textArea);
                tabbedPane.addTab(file.getName(), scrollPane);
                tabbedPane.setTabComponentAt(tabbedPane.getTabCount() - 1, new TabComponent(tabbedPane));
                tabbedPane.setSelectedComponent(scrollPane);

                TabComponent tabComponent = (TabComponent) tabbedPane.getTabComponentAt(tabbedPane.getTabCount() - 1);
                tabComponent.setPreferredSize(new Dimension(150, 30));

                currentFilePath = file.getAbsolutePath();

                textArea.getDocument().addDocumentListener(new DocumentListener() 
                {
                    @Override
                    public void insertUpdate(DocumentEvent e) 
                    {
                        markAsModified();
                    }
    
                    @Override
                    public void removeUpdate(DocumentEvent e) 
                    {
                        markAsModified();
                    }
    
                    @Override
                    public void changedUpdate(DocumentEvent e) 
                    {
                        markAsModified();
                    }
    
                    private void markAsModified() 
                    {
                        String title = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());
                        if (!title.endsWith("*")) 
                        {
                            tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), title + "*");
                        }
                    }
                });
            }
            catch (IOException e)
            {
                JOptionPane.showMessageDialog(this, "Error opening file: " + e.getMessage());
            }
        }
    }

    private void saveFile(boolean saveAs) 
    {
        JTextArea textArea = getCurrentTextArea();
        String title = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());
        File file = saveAs || title.startsWith("Untitled*") ? null : new File(title.replace("*", ""));
    
        if (file == null || saveAs) 
        {
            int result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) 
            {
                file = fileChooser.getSelectedFile();
                currentFilePath = file.getAbsolutePath();
            } 
            else 
            {
                return;
            }
        }
    
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentFilePath))) 
        {
            textArea.write(writer);
            tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), file.getName());
        } catch (IOException e) 
        {
            JOptionPane.showMessageDialog(this, "Error saving file: " + e.getMessage());
        }
    }

    private void printFile()
    {
        try
        {
            getCurrentTextArea().print();
        }
        catch (PrinterException e)
        {
            JOptionPane.showMessageDialog(this, "Error printing file: " + e.getMessage());
        }
    }

    private void findText()
    {
        currentFindText = JOptionPane.showInputDialog(this, "Find:");
        findInDocument(0);
    }

    private void findNextText()
    {
        currentFindText = JOptionPane.showInputDialog(this, "Find Next:");
        JTextArea textArea = getCurrentTextArea();
        int startIndex = textArea.getCaretPosition();
        findInDocument(startIndex);
    }

    private void findPreviousText()
    {
        currentFindText = JOptionPane.showInputDialog(this, "Find Previous:");
        JTextArea textArea = getCurrentTextArea();
        int startIndex = textArea.getCaretPosition() - 1;
        String content = textArea.getText();
        int index = content.lastIndexOf(currentFindText, startIndex);
        if (index == -1)
        {
            JOptionPane.showMessageDialog(this, "Text not found");
        }
        else
        {
            textArea.setCaretPosition(index);
            textArea.moveCaretPosition(index + currentFindText.length());
        }
    }

    private void findInDocument(int startIndex)
    {
        JTextArea textArea = getCurrentTextArea();
        String content = textArea.getText();
        int index = content.indexOf(currentFindText, startIndex);
        if (index == -1)
        {
            JOptionPane.showMessageDialog(this, "Text not found");
        }
        else
        {
            textArea.setCaretPosition(index);
            textArea.moveCaretPosition(index + currentFindText.length());
        }
    }

    private void replaceText()
    {
        JTextArea textArea = getCurrentTextArea();
        String findText = JOptionPane.showInputDialog(this, "Find:");
        String replaceText = JOptionPane.showInputDialog(this, "Replace with:");
        if (findText != null && replaceText != null)
        {
            textArea.setText(textArea.getText().replace(findText, replaceText));
        }
    }

    private void insertDateTime()
    {
        JTextArea textArea = getCurrentTextArea();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(new Date());
        textArea.insert(dateString, textArea.getCaretPosition());
    }

    private void setLightTheme() throws UnsupportedLookAndFeelException
    {
        preferences.put("theme", "light");
        FlatArcOrangeIJTheme.setup();
        SwingUtilities.updateComponentTreeUI(this);
        UIManager.put( "Button.arc", 10 );
        UIManager.put( "Component.arc", 10 );
        UIManager.put( "ProgressBar.arc", 10 );
        UIManager.put( "TextComponent.arc", 10 );
        UIManager.put( "TabbedPane.showTabSeparators", true );
    }

    private void setDarkTheme()
    {
        preferences.put("theme", "dark");
        FlatArcDarkOrangeIJTheme.setup();
        SwingUtilities.updateComponentTreeUI(this);
        UIManager.put( "Button.arc", 10 );
        UIManager.put( "Component.arc", 10 );
        UIManager.put( "ProgressBar.arc", 10 );
        UIManager.put( "TextComponent.arc", 10 );
        UIManager.put( "TabbedPane.showTabSeparators", true );
    }


    private void toggleWordWrap()
    {
        boolean wordWrap = wordWrapItem.isSelected();
        for (int i = 0; i < tabbedPane.getTabCount(); i++)
        {
            JScrollPane scrollPane = (JScrollPane) tabbedPane.getComponentAt(i);
            JTextArea textArea = (JTextArea) scrollPane.getViewport().getView();
            textArea.setLineWrap(wordWrap);
            textArea.setWrapStyleWord(wordWrap);
        }
    }
    
    private int findSettingsTabIndex() 
    {
        for (int i = 0; i < tabbedPane.getTabCount(); i++) 
        {
            if (tabbedPane.getComponentAt(i) == settingsPanel) 
            {
                return i;
            }
        }
        return -1;
    }
    
    private void openSettingsPage() 
    {
        int settingsTabIndex = findSettingsTabIndex();
        if (settingsTabIndex == -1) 
        {
            settingsPanel = createSettingsPanel();
            tabbedPane.addTab("Settings", settingsPanel);
            int index = tabbedPane.indexOfComponent(settingsPanel);
            tabbedPane.setTabComponentAt(index, new TabComponent(tabbedPane));
            tabbedPane.setSelectedIndex(index);
            tabbedPane.getTabComponentAt(index).setPreferredSize(new Dimension(150, 30));
        } 
        else 
        {
            // If settings page already exists, select that tab
            tabbedPane.setSelectedIndex(settingsTabIndex);
        }
    }
    
    private JPanel createSettingsPanel() 
    {
        settingsPanel = new JPanel();
        JPanel settingsInnerPanel = new JPanel();
        settingsInnerPanel.setLayout(new GridLayout(3,1));
        
        JPanel fontPanel = new JPanel();
        fontPanel.setBorder(new TitledBorder(null, "Font", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        fontPanel.setLayout(new GridLayout(2,1));
        JPanel fontTopPanel = new JPanel();
        fontTopPanel.setLayout(new GridLayout(3,2));
        String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        JComboBox<String> fontComboBox = new JComboBox<>(fonts);
        fontComboBox.setSelectedItem(currentTextArea.getFont().getFamily());
        fontTopPanel.add(new JLabel("Font:"));
        fontTopPanel.add(fontComboBox);

        SpinnerNumberModel fontSizeModel = new SpinnerNumberModel(currentTextArea.getFont().getSize(), 8, 72, 1);
        JSpinner fontSizeSpinner = new JSpinner(fontSizeModel);
        JComponent editor = fontSizeSpinner.getEditor();
        JSpinner.DefaultEditor spinnerEditor = (JSpinner.DefaultEditor) editor;
        spinnerEditor.getTextField().setColumns(2); 
        fontTopPanel.add(new JLabel("Size:"));
        fontTopPanel.add(fontSizeSpinner);

        JCheckBox boldCheckBox = new JCheckBox("Bold");
        JCheckBox italicCheckBox = new JCheckBox("Italic");
        fontTopPanel.add(boldCheckBox);
        fontTopPanel.add(italicCheckBox);

        
        JPanel fontDownPanel = new JPanel();
        fontDownPanel.setLayout(new BorderLayout());
        JTextArea fontSample = new JTextArea("Sample Text");
        fontSample.setFont(currentTextArea.getFont());
        fontDownPanel.add(new JLabel("Sample:"), BorderLayout.NORTH);
        fontDownPanel.add(new JScrollPane(fontSample), BorderLayout.CENTER);

        JPanel themePanel = new JPanel();
        themePanel.setBorder(new TitledBorder(null, "Theme", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        themePanel.setLayout(new GridLayout(1,2));
        
        ButtonGroup themeGroup = new ButtonGroup();
        JRadioButton lightThemeButton = new JRadioButton("Light Theme");
        JRadioButton darkThemeButton = new JRadioButton("Dark Theme");
        themeGroup.add(lightThemeButton);
        themeGroup.add(darkThemeButton);

        String theme = preferences.get("theme", "light");
        if (theme.equals("dark")) 
        {
            darkThemeButton.setSelected(true);
        } 
        else 
        {
            lightThemeButton.setSelected(true);
        }
        themePanel.add(lightThemeButton);
        themePanel.add(darkThemeButton);

        JPanel bottomPanel = new JPanel();
        JButton applyButton = new JButton("Apply");
        JButton closeButton = new JButton("Close");
        Dimension buttonSize = new Dimension(70, closeButton.getPreferredSize().height);
        applyButton.setPreferredSize(buttonSize);
        closeButton.setPreferredSize(buttonSize);
        bottomPanel.add(applyButton);
        bottomPanel.add(closeButton);

        applyButton.addActionListener(e -> {
            String selectedFont = (String) fontComboBox.getSelectedItem();
            int fontSize = (Integer) fontSizeSpinner.getValue();
            int fontStyle = Font.PLAIN;
            if (boldCheckBox.isSelected())
            { 
                fontStyle |= Font.BOLD;
            }
            if (italicCheckBox.isSelected()) 
            {
                fontStyle |= Font.ITALIC;
            }
            Font newFont = new Font(selectedFont, fontStyle, fontSize);
            currentTextArea.setFont(newFont);
            fontSample.setFont(newFont);

            if (darkThemeButton.isSelected()) 
            {
                setDarkTheme();
            } 
            else 
            {
                try {
                    setLightTheme();
                } catch (UnsupportedLookAndFeelException ex) {
                    Logger.getLogger(PaperTrail.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        fontPanel.add(fontTopPanel);
        fontPanel.add(fontDownPanel);
        settingsInnerPanel.add(fontPanel);
        settingsInnerPanel.add(themePanel);
        settingsInnerPanel.add(bottomPanel);
        settingsPanel.add(settingsInnerPanel);

        return settingsPanel;
    }    

    private void zoomIn()
    {
        zoomFactor = 1;
        updateZoom();
    }

    private void zoomOut()
    {
        zoomFactor = -1;
        updateZoom();
    }

    private void restoreDefaultZoom()
    {
        for (int i = 0; i < tabbedPane.getTabCount(); i++)
        {
            JScrollPane scrollPane = (JScrollPane) tabbedPane.getComponentAt(i);
            JTextArea textArea = (JTextArea) scrollPane.getViewport().getView();
            Font currentFont = textArea.getFont();
            float newSize = (float) defaultTextSize;
            textArea.setFont(currentFont.deriveFont(newSize));
        }
    }

    private void updateZoom()
    {
        for (int i = 0; i < tabbedPane.getTabCount(); i++)
        {
            JScrollPane scrollPane = (JScrollPane) tabbedPane.getComponentAt(i);
            JTextArea textArea = (JTextArea) scrollPane.getViewport().getView();
            Font currentFont = textArea.getFont();
            float newSize = currentFont.getSize() + zoomFactor;
            textArea.setFont(currentFont.deriveFont(newSize));
        }
    }

    private void toggleStatusBar()
    {
        statusLabel.setVisible(statusBarToggle.isSelected());
    }

    private void closeCurrentTab()
    {
        int selectedIndex = tabbedPane.getSelectedIndex();
        if (selectedIndex != -1)
        {
            tabbedPane.remove(selectedIndex);
        }
    }

    private void undoAction()
    {
        try
        {
            if (undoManager.canUndo())
            {
                undoManager.undo();
            }
        }
        catch (CannotUndoException e)
        {
            JOptionPane.showMessageDialog(this, "Cannot undo: " + e.getMessage());
        }
    }

    private void redoAction()
    {
        try
        {
            if (undoManager.canRedo())
            {
                undoManager.redo();
            }
        }
        catch (CannotRedoException e)
        {
            JOptionPane.showMessageDialog(this, "Cannot redo: " + e.getMessage());
        }
    }

    private void updateCurrentTextArea()
    {
        JScrollPane scrollPane = (JScrollPane) tabbedPane.getSelectedComponent();
        if (scrollPane != null)
        {
            currentTextArea = (JTextArea) scrollPane.getViewport().getView();
        }
    }

    private void loadPreferences() throws UnsupportedLookAndFeelException
    {
        String theme = preferences.get("theme", "light");
        if (theme.equals("dark"))
        {
            setDarkTheme();
        }
        else
        {
            setLightTheme();
        }

        String fontName = preferences.get("fontName", defaultFontName);
        int fontSize = preferences.getInt("fontSize", defaultTextSize);
        int fontStyle = preferences.getInt("fontStyle", Font.PLAIN);
        Font font = new Font(fontName, fontStyle, fontSize);
        currentTextArea.setFont(font);
    }

    class TabComponent extends JPanel 
    {
        private final JTabbedPane pane;
        private final JButton closeButton;
        private final JLabel label;
    
        public TabComponent(final JTabbedPane pane) 
        {
            this.pane = pane;
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            setOpaque(false);
    
            label = new JLabel() 
            {
                @Override
                public String getText() 
                {
                    int i = pane.indexOfTabComponent(TabComponent.this);
                    if (i != -1) 
                    {
                        return pane.getTitleAt(i);
                    }
                    return null;
                }
            };
    
            label.setPreferredSize(new Dimension(110, label.getPreferredSize().height));
            add(label);
    
            add(Box.createHorizontalGlue());
            closeButton = new JButton("x");
            closeButton.setOpaque(false);
            closeButton.setBorderPainted(false);
            closeButton.setContentAreaFilled(false);
            closeButton.addActionListener(e -> closeTab());
            add(closeButton, BorderLayout.EAST);
        }
    
        private void closeTab() 
        {
            int i = pane.indexOfTabComponent(TabComponent.this);
            if (i != -1) 
            {
                pane.remove(i);
            }
        }
    }
}

