import java.io.File;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.filechooser.FileNameExtensionFilter;


public class GUI implements ActionListener {
    File imageFile;
    String fileExtension;
    JPanel panel= new JPanel();
    JButton select = new JButton();
    JLabel textLabel = new JLabel();
    JButton compress = new JButton();
    JButton decompress = new JButton();
    JFrame frame = new JFrame("Codec");
    JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir")); // GUI to select files
    FileNameExtensionFilter imageFilter = new FileNameExtensionFilter("Image Files", "png", "jpg", "jpeg"); // Filter to choose specific files only
    FileNameExtensionFilter binaryFilter = new FileNameExtensionFilter("Binary Files", "bin"); // Filter to choose specific files only
    
    public GUI(){
        panel.setLayout(null);
        select.setText("Select File");
        compress.setText("Compress");
        decompress.setText("Decompress");
        textLabel.setText("Select a file to proceed");
        
        textLabel.setBounds(199, 80, 300, 60);
        textLabel.setFont(new Font("", Font.BOLD, 16));
        select.setBounds(150, 210, 100, 30);
        compress.setBounds(300, 210, 100, 30);
        decompress.setBounds(300, 210, 110, 30);

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
        frame.setSize(570, 300);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        
    }
    
    public void actionPerformed(ActionEvent actionEvent){
        if (actionEvent.getActionCommand().equals("Select File")) {
            if (selectFile()) {
                fileExtension = imageFile.getName().substring(imageFile.getName().lastIndexOf(".") + 1);
                if (fileExtension.equals("bin")) {
                    decompress.setVisible(true);
                    compress.setVisible(false);
                }
                else{
                    compress.setVisible(true);
                    decompress.setVisible(false);
                }
                textLabel.setBounds(167, 60, 250, 60);
                textLabel.setText("File selected: " + imageFile.getName());
            }
        }
        else if (actionEvent.getActionCommand().equals("Compress")) {
            try{
                PC pc = new PC();
                pc.compress(imageFile);
                
                textLabel.setBounds(197, 60, 250, 60);
                textLabel.setText("Compression completed!");
            }
            catch(Exception e){
                textLabel.setBounds(195, 60, 250, 60);
                textLabel.setText("File error");
            }
        }
        else if (actionEvent.getActionCommand().equals("Decompress")) {
            try{
                PC pc = new PC();
                pc.decompress(imageFile);

                textLabel.setBounds(190, 60, 250, 60);
                textLabel.setText("Decompression completed!");
            }
            catch(Exception e){
                textLabel.setBounds(195, 60, 250, 60);
                textLabel.setText("File error");
            }
        }
    }

    public Boolean selectFile(){
        fileChooser.setFileFilter(binaryFilter);
        fileChooser.setFileFilter(imageFilter);
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
