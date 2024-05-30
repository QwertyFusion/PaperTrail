package rishibanerjee;

import com.formdev.flatlaf.intellijthemes.FlatArcDarkOrangeIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatArcOrangeIJTheme;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
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
import javax.swing.JTextField;
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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Image;
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
import java.util.HashMap;
import java.util.Map;
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
    JPanel aboutPanel;
    private int defaultTextSize = 16;
    private String defaultFontName = "Consolas";
    private Map<JTextArea, UndoManager> undoManagers = new HashMap<>();

    PaperTrail() throws UnsupportedLookAndFeelException
    {
        openWindowsCount++;
        setTitle("PaperTrail");
        setSize(1000,600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() 
        {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) 
            {
                closeWindow(); 
            }
        });
        ImageIcon icon = new ImageIcon(getClass().getResource("images/logo.png"));
        setIconImage(icon.getImage());

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
        if (tabbedPane.getTabCount()>0)
        {
            for (int i = 0; i < tabbedPane.getTabCount(); i++)
            {
                closeCurrentTab(i);
            }
        }

        if(tabbedPane.getTabCount()==0)
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
        JMenuItem closeWindowButton = new JMenuItem("Close Window");
        fileMenu.add(newFile);
        fileMenu.add(newWindow);
        fileMenu.add(openFile);
        fileMenu.add(saveFile);
        fileMenu.add(saveAsFile);
        fileMenu.addSeparator();
        fileMenu.add(printFile);
        fileMenu.addSeparator();
        fileMenu.add(closeTab);
        fileMenu.add(closeWindowButton);
        

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
        JMenuItem replaceAll = new JMenuItem("Replace All");
        JMenuItem insertDate = new JMenuItem("Insert Date/Time");
        JMenuItem selectAll = new JMenuItem("Select All");
        JMenuItem delete = new JMenuItem("Delete");
        JMenuItem fontSettings = new JMenuItem("Font");

        editMenu.add(undo);
        editMenu.add(redo);
        editMenu.addSeparator();
        editMenu.add(cut);
        editMenu.add(copy);
        editMenu.add(paste);
        editMenu.add(delete);
        editMenu.addSeparator();
        editMenu.add(find);
        editMenu.add(findNext);
        editMenu.add(findPrevious);
        editMenu.add(replaceAll);
        editMenu.addSeparator();
        editMenu.add(selectAll);
        editMenu.add(insertDate);
        editMenu.addSeparator();
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
        editMenu.addSeparator();
        viewMenu.add(statusBarToggle);
        viewMenu.add(wordWrapItem);

        // Settings menu
        JButton settingsButton = new JButton("Settings");
        JButton aboutButton = new JButton("About");
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.X_AXIS));
        menuPanel.add(Box.createHorizontalGlue());
        menuPanel.add(settingsButton);
        menuPanel.add(aboutButton);
        
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        menuBar.add(menuPanel);

        setJMenuBar(menuBar);

        // Action listeners
        newFile.addActionListener(e -> addNewTab());
        newWindow.addActionListener(e -> {
            try 
            {
                new PaperTrail().setVisible(true);
            } 
            catch (UnsupportedLookAndFeelException ex) 
            {
                Logger.getLogger(PaperTrail.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        openFile.addActionListener(e -> openFile());
        saveFile.addActionListener(e -> saveFile(false));
        saveAsFile.addActionListener(e -> saveFile(true));
        printFile.addActionListener(e -> printFile());
        closeTab.addActionListener(e -> closeCurrentTab());
        closeWindowButton.addActionListener(e -> closeWindow());
        cut.addActionListener(e -> getCurrentTextArea().cut());
        copy.addActionListener(e -> getCurrentTextArea().copy());
        paste.addActionListener(e -> getCurrentTextArea().paste());
        undo.addActionListener(e -> undoAction());
        redo.addActionListener(e -> redoAction());
        find.addActionListener(e -> findText());
        findNext.addActionListener(e -> findNextText());
        findPrevious.addActionListener(e -> findPreviousText());
        replaceAll.addActionListener(e -> replaceText());
        insertDate.addActionListener(e -> insertDateTime());
        selectAll.addActionListener(e -> getCurrentTextArea().selectAll());
        delete.addActionListener(e -> getCurrentTextArea().replaceSelection(""));
        fontSettings.addActionListener(e -> openSettingsPage());
        zoomIn.addActionListener(e -> zoomIn());
        zoomOut.addActionListener(e -> zoomOut());
        restoreZoom.addActionListener(e -> restoreDefaultZoom());
        statusBarToggle.addActionListener(e -> toggleStatusBar());
        wordWrapItem.addActionListener(e -> toggleWordWrap());
        settingsButton.addActionListener(e -> openSettingsPage());
        aboutButton.addActionListener(e -> openAboutPage());

        newFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));
        newWindow.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));
        openFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
        saveFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
        saveAsFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));
        printFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK));
        closeTab.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_DOWN_MASK));
        closeWindowButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));
        cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK));
        copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK));
        paste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK));
        undo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK));
        redo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK));
        find.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK));
        findNext.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
        findPrevious.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, KeyEvent.SHIFT_DOWN_MASK));
        replaceAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK));
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
        int tabIndex = tabbedPane.getTabCount() - 1;
        tabbedPane.setSelectedIndex(tabIndex);

        TabComponent tabComponent = (TabComponent) tabbedPane.getTabComponentAt(tabbedPane.getTabCount() - 1);
        tabComponent.setPreferredSize(new Dimension(150, 30));
        tabbedPane.requestFocus();
        tabbedPane.requestFocusInWindow();
    }

    private JTextArea createTextArea()
    {
        JTextArea textArea = new JTextArea();
        textArea.setFont(new Font(defaultFontName, Font.PLAIN, defaultTextSize));
        undoManager = new UndoManager();
        textArea.getDocument().addUndoableEditListener(undoManager);
        undoManagers.put(textArea, undoManager);
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
        Component component = tabbedPane.getSelectedComponent();
        if (component instanceof JScrollPane) 
        {
            JScrollPane scrollPane = (JScrollPane) component;
            Component view = scrollPane.getViewport().getView();
            if (view instanceof JTextArea) {
                return (JTextArea) view;
            }
        }
        return null;
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

    public void openFile(File file) 
    {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            JTextArea textArea = createTextArea();
            textArea.read(br, null);
    
            JScrollPane scrollPane = new JScrollPane(textArea);
            tabbedPane.addTab(file.getName(), scrollPane);
            tabbedPane.setTabComponentAt(tabbedPane.getTabCount() - 1, new TabComponent(tabbedPane));
            tabbedPane.setSelectedComponent(scrollPane);
    
            TabComponent tabComponent = (TabComponent) tabbedPane.getTabComponentAt(tabbedPane.getTabCount() - 1);
            tabComponent.setPreferredSize(new Dimension(150, 30));
    
            currentFilePath = file.getAbsolutePath();
    
            textArea.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    markAsModified();
                }
    
                @Override
                public void removeUpdate(DocumentEvent e) {
                    markAsModified();
                }
    
                @Override
                public void changedUpdate(DocumentEvent e) {
                    markAsModified();
                }
    
                private void markAsModified() {
                    String title = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());
                    if (!title.endsWith("*")) {
                        tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), title + "*");
                    }
                }
            });
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error opening file: " + e.getMessage());
        }
    }

    private void saveFile(boolean saveAs) 
    {
        if (!tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals("Settings") &&
        !tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals("About")) 
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
            } 
            catch (IOException e) 
            {
                JOptionPane.showMessageDialog(this, "Error saving file: " + e.getMessage());
            }
        } 
    }

    private void printFile()
    {
        if (!tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals("Settings") &&
            !tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals("About")) 
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
    }

    private void findText()
    {
        String input = JOptionPane.showInputDialog(this, "Find:");
        if (input != null)
        {
            currentFindText = input;
            findInDocument(0);
        }
    }

    private void findNextText()
    {
        String input = JOptionPane.showInputDialog(this, "Find Next:");
        if (input != null)
        {
            currentFindText = input;
            JTextArea textArea = getCurrentTextArea();
            int startIndex = textArea.getCaretPosition();
            findInDocument(startIndex);
        }
    }

    private void findPreviousText()
    {
        String input = JOptionPane.showInputDialog(this, "Find Previous:");
        if (input != null)
        {
            currentFindText = input;
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
        String findText = "";
        String replaceText = "";

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 2));
        panel.add(new JLabel("Find:"));
        JTextField findField = new JTextField();
        panel.add(findField);
        panel.add(new JLabel("Replace with:"));
        JTextField replaceField = new JTextField();
        panel.add(replaceField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Replace All", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION)
        {
            findText = findField.getText();
            replaceText = replaceField.getText();
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
        SwingUtilities.updateComponentTreeUI(fileChooser);
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
        SwingUtilities.updateComponentTreeUI(fileChooser);
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
            tabbedPane.requestFocusInWindow();
        } 
        else 
        {
            // If settings page already exists, select that tab
            tabbedPane.setSelectedIndex(settingsTabIndex);
            tabbedPane.requestFocusInWindow();
        }
    }
    
    private JPanel createSettingsPanel() 
    {
        settingsPanel = new JPanel();
        JPanel settingsInnerPanel = new JPanel();
        settingsInnerPanel.setLayout(new BoxLayout(settingsInnerPanel, BoxLayout.Y_AXIS));
        JPanel fontPanel = new JPanel();
        fontPanel.setBorder(new TitledBorder(null, "Font", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        fontPanel.setBorder(BorderFactory.createCompoundBorder(fontPanel.getBorder(), BorderFactory.createEmptyBorder(10, 10, 10, 10)));
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
        themePanel.setBorder(BorderFactory.createCompoundBorder(fontPanel.getBorder(), BorderFactory.createEmptyBorder(10, 10, 10, 10)));
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
        applyButton.setPreferredSize(new Dimension(100, closeButton.getPreferredSize().height));
        closeButton.setPreferredSize(new Dimension(100, closeButton.getPreferredSize().height));
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
                try 
                {
                    setLightTheme();
                } 
                catch (UnsupportedLookAndFeelException ex) 
                {
                    Logger.getLogger(PaperTrail.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        closeButton.addActionListener(e -> closeCurrentTab());

        fontPanel.add(fontTopPanel);
        fontPanel.add(fontDownPanel);
        settingsInnerPanel.add(fontPanel);
        settingsInnerPanel.add(themePanel);
        settingsInnerPanel.add(bottomPanel);

        settingsPanel.add(settingsInnerPanel);

        return settingsPanel;
    }    

    private int findAboutTabIndex() 
    {
        for (int i = 0; i < tabbedPane.getTabCount(); i++) 
        {
            if (tabbedPane.getComponentAt(i) == aboutPanel) 
            {
                return i;
            }
        }
        return -1;
    }

    private void openAboutPage()
    {
        int aboutTabIndex = findAboutTabIndex();
        if (aboutTabIndex == -1) 
        {
            aboutPanel = createAboutPanel();
            tabbedPane.addTab("About", aboutPanel);
            int index = tabbedPane.indexOfComponent(aboutPanel);
            tabbedPane.setTabComponentAt(index, new TabComponent(tabbedPane));
            tabbedPane.setSelectedIndex(index);
            tabbedPane.getTabComponentAt(index).setPreferredSize(new Dimension(150, 30));
            tabbedPane.requestFocusInWindow();      
        } 
        else 
        {
            // If about page already exists, select that tab
            tabbedPane.setSelectedIndex(aboutTabIndex);
            tabbedPane.requestFocusInWindow();
        }
    }

    private JPanel createAboutPanel() 
    {
        aboutPanel = new JPanel();

        JPanel aboutInnerPanel = new JPanel();
        aboutInnerPanel.setLayout(new GridLayout(3,1));
        aboutInnerPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        String theme = preferences.get("theme", "light");
        String logoPath = theme.equals("dark") ? "images/logo_dark.png" : "images/logo_light.png";
        ImageIcon logoIcon = new ImageIcon(getClass().getResource(logoPath));
        Image logoImage = logoIcon.getImage();
        int height = 180;
        int width = (int) (logoImage.getWidth(null) * (height / (double) logoImage.getHeight(null)));

        Image newimg = logoImage.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH); 
        logoIcon = new ImageIcon(newimg);
        JLabel logoLabel = new JLabel(logoIcon);

        logoLabel.setHorizontalAlignment(JLabel.CENTER);

        JTextArea descriptionArea = new JTextArea("PaperTrail: A powerful, user-friendly text editor designed for simplicity and efficiency. Built with Java, Maven and FlatLaf IJ theme, it offers a clean interface, syntax highlighting, and support for multiple tabs. \n\nPaperTrail is open-source and available on GitHub.");
        descriptionArea.setSize(500, descriptionArea.getPreferredSize().height);
        descriptionArea.setFocusable(false);
        descriptionArea.setEditable(false);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setLineWrap(true);
        descriptionArea.setFont(new Font(defaultFontName, Font.PLAIN, defaultTextSize));
        descriptionArea.setBackground(aboutPanel.getBackground());
        descriptionArea.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        
        JPanel authorPanel = new JPanel();
        authorPanel.setLayout(new GridLayout(1,2));
        JLabel authorLabel = new JLabel("Developer: Rishi Banerjee");
        authorLabel.setFont(new Font(defaultFontName, Font.PLAIN, defaultTextSize));
        JLabel githubLinkLabel = new JLabel("<html><a href='https://github.com/QwertyFusion'>https://github.com/QwertyFusion</a></html>");
        githubLinkLabel.setFont(new Font(defaultFontName, Font.PLAIN, defaultTextSize));
        authorPanel.add(authorLabel);
        authorPanel.add(githubLinkLabel);
        authorPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        aboutInnerPanel.add(logoLabel);
        aboutInnerPanel.add(new JScrollPane(descriptionArea));
        aboutInnerPanel.add(authorPanel);

        aboutPanel.add(aboutInnerPanel);
    
        return aboutPanel;
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
        if (selectedIndex!= -1) 
        {
            JTextArea currentTextArea = getCurrentTextArea();
            if (currentTextArea!= null) 
            {
                String title = currentTextArea.getText();
                if (tabbedPane.getTitleAt(selectedIndex).endsWith("*") &&!title.isEmpty() &&!tabbedPane.getTitleAt(selectedIndex).equals("Settings") &&!tabbedPane.getTitleAt(selectedIndex).equals("About")) 
                {
                    int result = JOptionPane.showOptionDialog(
                            SwingUtilities.getWindowAncestor(currentTextArea), // Show the dialog relative to the text area's window ancestor
                            "The tab \""+tabbedPane.getTitleAt(selectedIndex)+"\" has unsaved changes. What would you like to do?",
                            "Unsaved Changes",
                            JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            new Object[]{"Save", "Don't Save", "Cancel"},
                            "Save"
                    );
    
                    if (result == JOptionPane.YES_OPTION) 
                    {
                        saveFile(false);
                        tabbedPane.remove(selectedIndex);
                    } 
                    else if (result == JOptionPane.NO_OPTION) 
                    {
                        tabbedPane.remove(selectedIndex);
                    }
                } 
                else 
                {
                    tabbedPane.remove(selectedIndex);
                }
            } 
            else 
            {
                tabbedPane.remove(selectedIndex);
            }
        }
    }

    protected void closeCurrentTab(int index) 
    {
        if (index!= -1) 
        {
            JTextArea currentTextArea = getCurrentTextArea();
            if (currentTextArea!= null) 
            {
                String title = currentTextArea.getText();
                if (tabbedPane.getTitleAt(index).endsWith("*") &&!title.isEmpty() &&!tabbedPane.getTitleAt(index).equals("Settings") &&!tabbedPane.getTitleAt(index).equals("About")) 
                {
                    int result = JOptionPane.showOptionDialog(
                            SwingUtilities.getWindowAncestor(currentTextArea), // Show the dialog relative to the text area's window ancestor
                            "The tab \""+tabbedPane.getTitleAt(index)+"\" has unsaved changes. What would you like to do?",
                            "Unsaved Changes",
                            JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            new Object[]{"Save", "Don't Save", "Cancel"},
                            "Save"
                    );
    
                    if (result == JOptionPane.YES_OPTION) 
                    {
                        saveFile(false);
                        tabbedPane.remove(index);
                    } 
                    else if (result == JOptionPane.NO_OPTION) 
                    {
                        tabbedPane.remove(index);
                    }
                } 
                else 
                {
                    tabbedPane.remove(index);
                }
            } 
            else 
            {
                tabbedPane.remove(index);
            }
        }
    }

    private void undoAction()
    {
        JTextArea currentTextArea = getCurrentTextArea();
        UndoManager undoManager = undoManagers.get(currentTextArea);
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
        JTextArea currentTextArea = getCurrentTextArea();
        UndoManager undoManager = undoManagers.get(currentTextArea);
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
                    if (i != -1) {
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
    
        public void closeTab() 
        {
            int i = pane.indexOfTabComponent(TabComponent.this);
            if (i!= -1) {
                JTextArea textArea = getCurrentTextArea();
                if (textArea!= null) 
                {
                    String title = textArea.getText();
                    if (pane.getTitleAt(i).endsWith("*") &&!title.isEmpty() &&!pane.getTitleAt(i).equals("Settings") &&!pane.getTitleAt(i).equals("About")) {
                        int result = JOptionPane.showOptionDialog(
                                SwingUtilities.getWindowAncestor(textArea), // Show the dialog relative to the text area's window ancestor
                                "The tab \""+tabbedPane.getTitleAt(i)+"\" has unsaved changes. What would you like to do?",
                                "Unsaved Changes",
                                JOptionPane.YES_NO_CANCEL_OPTION,
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                new Object[]{"Save", "Don't Save", "Cancel"},
                                "Save"
                        );
        
                        if (result == JOptionPane.YES_OPTION)
                        {
                            saveFile(false);
                            pane.remove(i);
                        } 
                        else if (result == JOptionPane.NO_OPTION) 
                        {
                            pane.remove(i);
                        }
                    } else {
                        pane.remove(i);
                    }
                } else {
                    pane.remove(i);
                }
            }
        }
    }
}

