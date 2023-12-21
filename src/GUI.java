package src;

import java.io.File;
import java.util.Vector;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
    FileNameExtensionFilter binaryFilter = new FileNameExtensionFilter("Binary Files", "bin");
    FileNameExtensionFilter textFilter = new FileNameExtensionFilter("Text Files", "txt");
    // USE THE JComboBox to make a drop down menu    

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
                EncodingAlgorithm encoder = new PC();
                encoder.Compress(imageFile);
                
                textLabel.setBounds(197, 60, 250, 60);
                textLabel.setText("Compression completed!");
            }
            catch(Exception e){
                textLabel.setBounds(195, 60, 250, 60);
                textLabel.setText("File error");
                e.printStackTrace();
            }
        }
        else if (actionEvent.getActionCommand().equals("Decompress")) {
            try{
                EncodingAlgorithm encoder = new PC();
                encoder.Decompress(imageFile);

                textLabel.setBounds(190, 60, 250, 60);
                textLabel.setText("Decompression completed!");
            }
            catch(Exception e){
                textLabel.setBounds(195, 60, 250, 60);
                textLabel.setText("File error");
                e.printStackTrace();
            }
        }
    }

    public Boolean selectFile(){
        fileChooser.setFileFilter(textFilter);
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


// VQ GUI
/*
import java.io.File;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.JFileChooser;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import Codecs.VQ_2D;
import Codecs.VQ_3D;


public class GUI implements ActionListener {
    File imageFile;
    String fileExtension;
    JPanel panel= new JPanel();
    JButton select = new JButton();
    JLabel textLabel = new JLabel();
    JButton compress = new JButton();
    JLabel kSizeLabel = new JLabel();
    JLabel blockSizeLabel = new JLabel();
    JButton decompress = new JButton();
    JFrame frame = new JFrame("Codec");
    JTextField kSizeField = new JTextField();
    JTextField blockSizeField = new JTextField();
    JRadioButton rgbImage = new JRadioButton();
    ButtonGroup radioGroup = new ButtonGroup();
    JRadioButton grayImage = new JRadioButton();
    JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir")); // GUI to select files
    FileNameExtensionFilter imageFilter = new FileNameExtensionFilter("Image Files", "png", "jpg", "jpeg"); // Filter to choose specific files only
    FileNameExtensionFilter binaryFilter = new FileNameExtensionFilter("Binary Files", "bin"); // Filter to choose specific files only
    
    public GUI(){
        panel.setLayout(null);
        select.setText("Select File");
        compress.setText("Compress");
        decompress.setText("Decompress");
        kSizeLabel.setText("K-Size");
        blockSizeLabel.setText("B-Size");
        textLabel.setText("Select a file to proceed");
        grayImage.setText("Gray");
        rgbImage.setText("RGB");
        
        textLabel.setBounds(199, 80, 300, 60);
        textLabel.setFont(new Font("", Font.BOLD, 16));
        select.setBounds(150, 210, 100, 30);
        compress.setBounds(300, 210, 100, 30);
        decompress.setBounds(300, 210, 110, 30);
        kSizeLabel.setBounds(183, 150, 100, 25);
        kSizeField.setBounds(228, 150, 50, 25);
        blockSizeLabel.setBounds(285, 150, 110, 25);
        blockSizeField.setBounds(328, 150, 50, 25);
        grayImage.setBounds(210, 115, 55, 25);
        rgbImage.setBounds(279, 115, 55, 25);

        radioGroup.add(grayImage);
        radioGroup.add(rgbImage);

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
        panel.add(blockSizeLabel);
        panel.add(blockSizeField);
        panel.add(grayImage);
        panel.add(rgbImage);
        
        // Hides buttons till you select a file
        compress.setVisible(false);
        decompress.setVisible(false);
        kSizeLabel.setVisible(false);
        kSizeField.setVisible(false);
        blockSizeLabel.setVisible(false);
        blockSizeField.setVisible(false);
        grayImage.setVisible(false);
        rgbImage.setVisible(false);
        
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
                    kSizeLabel.setVisible(false);
                    kSizeField.setVisible(false);
                    blockSizeLabel.setVisible(false);
                    blockSizeField.setVisible(false);
                }
                else{
                    compress.setVisible(true);
                    kSizeLabel.setVisible(true);
                    kSizeField.setVisible(true);
                    blockSizeLabel.setVisible(true);
                    blockSizeField.setVisible(true);
                    decompress.setVisible(false);
                }
                grayImage.setVisible(true);
                rgbImage.setVisible(true);
                textLabel.setBounds(167, 60, 250, 60);
                textLabel.setText("File selected: " + imageFile.getName());
            }
        }
        else if (actionEvent.getActionCommand().equals("Compress")) {
            try{
                int kSize = 0;
                int blockSize = 0;

                if (kSizeField.getText().equals("")) {
                    // Throws exception if input is empty
                    throw new Exception();
                } else {
                    // Throws exception if input isn't int
                    kSize = Integer.parseInt(kSizeField.getText());
                }

                if (blockSizeField.getText().equals("")) {
                    // Throws exception if input is empty
                    throw new Exception();
                } else {
                    // Throws exception if input isn't int
                    blockSize = Integer.parseInt(blockSizeField.getText());
                }

                if (grayImage.isSelected()) {
                    VQ_2D vq = new VQ_2D();
                    vq.Compress(imageFile, kSize, blockSize);
                }
                else if (rgbImage.isSelected()) {
                    VQ_3D vq = new VQ_3D();
                    vq.Compress(imageFile, kSize, blockSize);
                }
                else{
                    throw new Exception();
                }
                
                textLabel.setBounds(197, 60, 250, 60);
                textLabel.setText("Compression completed!");
            }
            catch(Exception e){
                e.printStackTrace();
                textLabel.setBounds(195, 60, 250, 60);
                textLabel.setText("Invalid or missing inputs");
            }
        }
        else if (actionEvent.getActionCommand().equals("Decompress")) {
            try{
                if (grayImage.isSelected()) {
                    VQ_2D vq = new VQ_2D();
                    vq.Decompress(imageFile);
                }
                else if (rgbImage.isSelected()) {
                    VQ_3D vq = new VQ_3D();
                    vq.Decompress(imageFile);
                }
                else{
                    throw new Exception();
                }
                textLabel.setBounds(190, 60, 250, 60);
                textLabel.setText("Decompression completed!");
            }
            catch(Exception e){
                textLabel.setBounds(195, 60, 250, 60);
                textLabel.setText("File or input error");
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

 */