import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.awt.Font;
import java.awt.event.ActionEvent;
import javax.swing.filechooser.FileNameExtensionFilter;


public class GUI implements ActionListener {
    File textFile;
    JPanel panel= new JPanel();
    JLabel textLabel = new JLabel();
    JButton select = new JButton();
    JButton compress = new JButton();
    JButton decompress = new JButton();
    JFrame frame = new JFrame("Codec");
    JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir")); // GUI to select files
    FileNameExtensionFilter filter = new FileNameExtensionFilter("Text Files", "txt", "tx"); // Filter to choose specific files only
    
    public GUI(){
        panel.setLayout(null);
        select.setText("Select File");
        compress.setText("Compress");
        decompress.setText("Decompress");
        textLabel.setText("Select a file to proceed");
        
        textLabel.setBounds(150, 50, 250, 60);
        textLabel.setFont(new Font("", Font.BOLD, 16));     
        select.setBounds(30, 160, 100, 30);
        compress.setBounds(230, 160, 100, 30);
        decompress.setBounds(350, 160, 110, 30);

        // Listens for button press and calls actionPerformed()
        select.addActionListener(this);
        compress.addActionListener(this);
        decompress.addActionListener(this);
        
        panel.add(textLabel);
        panel.add(select);
        panel.add(compress);
        panel.add(decompress);
        
        // Hides buttons till you select a file
        compress.setVisible(false);
        decompress.setVisible(false);
        
        frame.add(panel);
        frame.setSize(500, 250);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        
    }
    
    public void actionPerformed(ActionEvent actionEvent){
        if (actionEvent.getActionCommand().equals("Select File")) {
            selectFile();
            compress.setVisible(true);
            decompress.setVisible(true);
            textLabel.setText("File selected: " + textFile.getName());
        }
        else if (actionEvent.getActionCommand().equals("Compress")) {
            
            textLabel.setBounds(145, 50, 250, 60);
            textLabel.setText("Compression completed");
        }
        else if (actionEvent.getActionCommand().equals("Decompress")) {
            
            textLabel.setBounds(140, 50, 250, 60);
            textLabel.setText("Decompression completed");
        }
    }

    public void selectFile(){
        fileChooser.setFileFilter(filter);
        int returnVal = fileChooser.showOpenDialog(fileChooser);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            textFile = fileChooser.getSelectedFile();
        }
    }

    public static void main(String[] args){
        new GUI();
    }
}
