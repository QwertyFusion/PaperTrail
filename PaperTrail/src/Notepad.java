import javax.swing.ImageIcon;
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

    JMenuBar menuBar = new JMenuBar();
    JMenu file = new JMenu("File");
    JMenu edit = new JMenu("Edit");
    JMenu view = new JMenu("View");
    JMenu help = new JMenu("Help");

    JMenuItem newTab = new JMenuItem("New tab");

    Notepad()
    {
        setTitle("PaperTrail");
        setSize(800,600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ImageIcon icon = new ImageIcon(getClass().getResource("notepad.jpg"));
        setIconImage(icon.getImage());

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

    }
    private void createStatusBar()
    {

    }
    private void addNewTab()
    {

    }
    private void loadPreferences()
    {

    }
}
