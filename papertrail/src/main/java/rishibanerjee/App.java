package rishibanerjee;

import java.io.File;

import javax.swing.JOptionPane;

public class App 
{
    public static void main(String[] args) throws Exception 
    {
        if (args.length > 0) 
        {
            File file = new File(args[0]);
            if (file.exists() && file.isFile()) {
                PaperTrail app = new PaperTrail();
                app.setVisible(true);
                app.openFile(file);
            } 
            else 
            {
                JOptionPane.showMessageDialog(null, "Invalid file: " + args[0]);
            }
        } 
        else 
        {
            new PaperTrail().setVisible(true);
        }
    }
}