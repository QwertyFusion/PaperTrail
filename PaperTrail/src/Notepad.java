import javax.swing.ImageIcon;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.undo.UndoManager;

import java.awt.BorderLayout;
import java.util.prefs.Preferences;

import javax.swing.*;

public class Notepad extends JFrame
{
    JTabbedPane tabbedPane;
    JFileChooser fileChooser;
    String currentFindText = "";
    UndoManager undoManager;
    JTextArea currentTextArea;
    JCheckBoxMenuItem wordWrapItem;
    JLabel statusLabel;
    Preferences preferences;

    Notepad()
    {
        setTitle("PaperTrail");
        setSize(800,600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
        loadPreferences();      
    }

    private void createMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();

        // File menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem newFile = new JMenuItem("New");
        JMenuItem openFile = new JMenuItem("Open");
        JMenuItem saveFile = new JMenuItem("Save");
        JMenuItem saveAsFile = new JMenuItem("Save As");
        JMenuItem printFile = new JMenuItem("Print");
        fileMenu.add(newFile);
        fileMenu.add(openFile);
        fileMenu.add(saveFile);
        fileMenu.add(saveAsFile);
        fileMenu.add(printFile);

        // Edit menu
        JMenu editMenu = new JMenu("Edit");
        JMenuItem cut = new JMenuItem("Cut");
        JMenuItem copy = new JMenuItem("Copy");
        JMenuItem paste = new JMenuItem("Paste");
        JMenuItem undo = new JMenuItem("Undo");
        JMenuItem redo = new JMenuItem("Redo");
        JMenuItem find = new JMenuItem("Find");
        JMenuItem findNext = new JMenuItem("Find Next");
        JMenuItem replace = new JMenuItem("Replace");
        JMenuItem insertDate = new JMenuItem("Insert Date/Time");
        wordWrapItem = new JCheckBoxMenuItem("Word Wrap");
        wordWrapItem.setSelected(true);
        editMenu.add(cut);
        editMenu.add(copy);
        editMenu.add(paste);
        editMenu.add(undo);
        editMenu.add(redo);
        editMenu.add(find);
        editMenu.add(findNext);
        editMenu.add(replace);
        editMenu.add(insertDate);
        editMenu.add(wordWrapItem);

        // Settings menu
        JMenu settingsMenu = new JMenu("Settings");
        JMenuItem lightTheme = new JMenuItem("Light Theme");
        JMenuItem darkTheme = new JMenuItem("Dark Theme");
        JMenuItem changeFont = new JMenuItem("Change Font");
        settingsMenu.add(lightTheme);
        settingsMenu.add(darkTheme);
        settingsMenu.add(changeFont);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(settingsMenu);

        setJMenuBar(menuBar);

        // Action listeners
        newFile.addActionListener(e -> addNewTab());
        openFile.addActionListener(e -> openFile());
        saveFile.addActionListener(e -> saveFile(false));
        saveAsFile.addActionListener(e -> saveFile(true));
        printFile.addActionListener(e -> printFile());
        cut.addActionListener(e -> getCurrentTextArea().cut());
        copy.addActionListener(e -> getCurrentTextArea().copy());
        paste.addActionListener(e -> getCurrentTextArea().paste());
        undo.addActionListener(e -> undoAction());
        redo.addActionListener(e -> redoAction());
        find.addActionListener(e -> findText());
        findNext.addActionListener(e -> findNextText());
        replace.addActionListener(e -> replaceText());
        insertDate.addActionListener(e -> insertDateTime());
        lightTheme.addActionListener(e -> setLightTheme());
        darkTheme.addActionListener(e -> setDarkTheme());
        wordWrapItem.addActionListener(e -> toggleWordWrap());
        changeFont.addActionListener(e -> changeFont());
    }
    private void createStatusBar()
    {
        statusLabel = new JLabel("Line: 1, Column: 1");
        add(statusLabel, BorderLayout.SOUTH);
    }
    private void addNewTab()
    {
        JTextArea textArea = new JTextArea();
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
                if(!title.endsWith("*"))
                {
                    tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), title + "*");
                }
            }
        });
        textArea.addCaretListener(e->updateStatusBar());
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);

        JScrollPane scrollPane = new JScrollPane(textArea);
        tabbedPane.addTab("Untitled*", scrollPane);
        tabbedPane.setSelectedComponent(scrollPane);
    }
    private void updateStatusBar()
    {
        JTextArea textArea = getCurrentTextArea();
        int caretPos = textArea.getCaretPosition();
        int lineNum=0;
        int colNum=0;

        try
        {
            lineNum = textArea.getLineOfOffset(caretPos);
            colNum = caretPos - textArea.getLineStartOffset(lineNum);
        }
        catch (BadLocationException e)
        {
            e.printStackTrace();
        }
        statusLabel.setText("Line: "+(lineNum+1) + ", Column: "+(colNum+1));
    }
    private JTextArea getCurrentTextArea()
    {
        JScrollPane scrollPane = (JScrollPane) tabbedPane.getSelectedComponent();
        return (JTextArea) scrollPane.getViewport().getView();
    }
    private void openFile()
    {

    }
    private void saveFile(boolean saveAs)
    {

    }
    private void printFile()
    {

    }
    private void findText()
    {

    }
    private void findNextText()
    {

    }
    private void replaceText()
    {

    }
    private void insertDateTime()
    {

    }
    private void setLightTheme()
    {

    }
    private void setDarkTheme()
    {

    }
    private void setLookAndFeel()
    {

    }
    private void toggleWordWrap()
    {

    }
    private void changeFont()
    {

    }
    private void undoAction()
    {

    }
    private void redoAction()
    {

    }
    private void loadPreferences()
    {

    }
    
}
