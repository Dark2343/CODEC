import java.io.File;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.filechooser.FileNameExtensionFilter;


public class GUI implements ActionListener {
    File imageFile;
    VQ vq = new VQ();
    JPanel panel= new JPanel();
    JButton select = new JButton();
    JLabel textLabel = new JLabel();
    JButton compress = new JButton();
    JLabel kSizeLabel = new JLabel();
    JButton decompress = new JButton();
    JFrame frame = new JFrame("Codec");
    JTextField kSizeField = new JTextField();
    JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir")); // GUI to select files
    FileNameExtensionFilter filter = new FileNameExtensionFilter("Image Files", "png", "jpg", "jpeg"); // Filter to choose specific files only
    
    public GUI(){
        panel.setLayout(null);
        select.setText("Select File");
        compress.setText("Compress");
        decompress.setText("Decompress");
        kSizeLabel.setText("K-Size");
        textLabel.setText("Select a file to proceed");
        
        textLabel.setBounds(199, 80, 270, 60);
        textLabel.setFont(new Font("", Font.BOLD, 16));
        select.setBounds(30, 210, 100, 30);
        compress.setBounds(330, 210, 100, 30);
        decompress.setBounds(450, 210, 110, 30);
        kSizeLabel.setBounds(238, 120, 100, 25);
        kSizeField.setBounds(278, 120, 50, 25);

        // Listens for button press and calls actionPerformed()
        select.addActionListener(this);
        compress.addActionListener(this);
        decompress.addActionListener(this);
        
        panel.add(textLabel);
        panel.add(select);
        panel.add(compress);
        panel.add(decompress);
        panel.add(kSizeLabel);
        panel.add(kSizeField);
        
        // Hides buttons till you select a file
        compress.setVisible(false);
        decompress.setVisible(false);
        kSizeLabel.setVisible(false);
        kSizeField.setVisible(false);
        
        frame.add(panel);
        frame.setSize(600, 300);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        
    }
    
    public void actionPerformed(ActionEvent actionEvent){
        if (actionEvent.getActionCommand().equals("Select File")) {
            if (selectFile()) {
                compress.setVisible(true);
                decompress.setVisible(true);
                kSizeLabel.setVisible(true);
                kSizeField.setVisible(true);
                textLabel.setBounds(190, 60, 250, 60);
                textLabel.setText("File selected: " + imageFile.getName());
            }
        }
        else if (actionEvent.getActionCommand().equals("Compress")) {
            try{
                int kSize = 0;
                if (kSizeField.getText().equals("")) {
                    // Throws exception if input is empty
                    throw new Exception();
                } else {
                    // Throws exception if input isn't int
                    kSize = Integer.parseInt(kSizeField.getText());
                }
                vq.compress(imageFile, kSize);
                textLabel.setBounds(200, 60, 250, 60);
                textLabel.setText("Compression completed");
            }
            catch(Exception e){
                textLabel.setBounds(190, 60, 250, 60);
                textLabel.setText("Error, Please check inputs");
            }
        }
        else if (actionEvent.getActionCommand().equals("Decompress")) {
            try{
                vq.decompress(imageFile);
                textLabel.setBounds(190, 60, 250, 60);
                textLabel.setText("Decompression completed");
            }
            catch(Exception e){
                e.printStackTrace();
                textLabel.setBounds(185, 60, 250, 60);
                textLabel.setText("File Error");
            }
        }
    }

    public Boolean selectFile(){
        fileChooser.setFileFilter(filter);
        fileChooser.setApproveButtonText("Select");
        int returnVal = fileChooser.showOpenDialog(fileChooser);

        // If you press "Select"
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            imageFile = fileChooser.getSelectedFile();
            return true;
        }
        return false;
    }

    public static void main(String[] args){
        new GUI();
    }
}
