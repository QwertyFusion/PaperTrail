import javax.swing.ImageIcon;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
        updateCurrentTextArea();
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

        newFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));
        openFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
        saveFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
        saveAsFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));
        printFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK));
        //exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_DOWN_MASK));
        cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK));
        copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK));
        paste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK));
        undo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK));
        redo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK));
        find.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK));
        findNext.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
        replace.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK));
        insertDate.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        //selectall.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK));
        //about.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_J, KeyEvent.CTRL_DOWN_MASK));
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
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION)
        {
            File file = fileChooser.getSelectedFile();
            try(BufferedReader br = new BufferedReader(new FileReader(file)))
            {
                JTextArea textArea = new JTextArea();
                textArea.read(br, null);
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
            catch (IOException e)
            {
                JOptionPane.showMessageDialog(this, "Error opening file: "+e.getMessage());
            }
        }
    }
    private void saveFile(boolean saveAs)
    {
        JTextArea textArea = getCurrentTextArea();
        String title = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());
        File file = saveAs || title.equals("Untitled*") ? null : new File(title.replace("*", ""));
        
        if(file == null)
        {
            int result = fileChooser.showSaveDialog(this);
            if(result == JFileChooser.APPROVE_OPTION)
            {
                file = fileChooser.getSelectedFile();
            }
            else
            {
                return;
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file)))
        {
            textArea.write(writer);
            tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), file.getName());
        }
        catch (IOException e)
        {
            JOptionPane.showMessageDialog(this, "Error saving file: "+e.getMessage());
        }
    }
    private void printFile()
    {
        try
        {
            getCurrentTextArea().print();
        }
        catch(Exception e)
        {
            JOptionPane.showMessageDialog(this, "Error printing file: " +e.getMessage());
        }
        
    }
    private void findText()
    {
        currentFindText = JOptionPane.showInputDialog(this, "Find:");
        findNextText();
    }
    private void findNextText()
    {
        JTextArea textArea = getCurrentTextArea();
        if(currentFindText != null && !currentFindText.isEmpty())
        {
            int startIndex = textArea.getCaretPosition();
            int index = textArea.getText().indexOf(currentFindText, startIndex);
            if(index == -1)
            {
                JOptionPane.showMessageDialog(this, "Text not found");
                return;
            }
            textArea.setCaretPosition(index);
            textArea.moveCaretPosition(index+currentFindText.length());
        }
    }
    private void replaceText()
    {
        JTextArea textArea = getCurrentTextArea();
        String findText = JOptionPane.showInputDialog(this, "Find:");
        String replaceText = JOptionPane.showInputDialog(this, "Replace with:");
        if(findText!=null && replaceText!=null)
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
    private void setLightTheme()
    {
        setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
    }
    private void setDarkTheme()
    {
        setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
    }
    private void setLookAndFeel(String lookAndFeel)
    {
        try
        {
            UIManager.setLookAndFeel(lookAndFeel);
            SwingUtilities.updateComponentTreeUI(this);
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(this, "Error setting theme: "+e.getMessage());
        }
    }
    private void toggleWordWrap()
    {
        boolean wordWrap = wordWrapItem.isSelected();
        JTextArea textArea = getCurrentTextArea();
        textArea.setLineWrap(wordWrap);
        textArea.setWrapStyleWord(wordWrap);
    }
    private void changeFont()
    {
        String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        String font = (String) JOptionPane.showInputDialog(this, "Choose font:", "Font",
                JOptionPane.PLAIN_MESSAGE, null, fonts, currentTextArea.getFont().getFamily());

        if (font != null) 
        {
            int size = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter size:", currentTextArea.getFont().getSize()));
            currentTextArea.setFont(new Font(font, Font.PLAIN, size));
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
        } catch (CannotUndoException e) 
        {
            e.printStackTrace();
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
        } catch (CannotRedoException e) 
        {
            e.printStackTrace();
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
    private void loadPreferences()
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

        String fontName = preferences.get("fontName", "Arial");
        int fontSize = preferences.getInt("fontSize", 12);
        currentTextArea.setFont(new Font(fontName, Font.PLAIN, fontSize));
    }
}
