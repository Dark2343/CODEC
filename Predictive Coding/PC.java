import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.awt.image.BufferedImage;

public class PC {
    
    int WIDTH, HEIGHT, MAX = Integer.MIN_VALUE, MIN = Integer.MAX_VALUE;;
    int[][] pixelArray, differenceArray;

    
    public void compress(File imageFile) throws Exception{
        try{
            ProcessGrayScaleImage(imageFile);
            Predict(pixelArray);
            Quantize(differenceArray);
            

            String name = imageFile.getName().replaceFirst("[.][^.]+$", "");

            // WriteCompressedImage(pixelArray, name);
            // SaveCompressedFile(pixelArray, name);
        }
        catch(Exception e){
            throw e;
        }
    }

    public void ProcessGrayScaleImage(File imageFile) throws Exception {
        try{
            BufferedImage img = ImageIO.read(imageFile);
            WIDTH = img.getWidth();
            HEIGHT = img.getHeight();
            
            int[][] pixelArray = new int[WIDTH][HEIGHT];
            
            for(int i = 0; i < WIDTH; i++){
                for(int j = 0; j < HEIGHT; j++){
                    
                    // Get the RGB value of the pixel
                    int rgb = img.getRGB(i, j);
                    
                    // Extract the grayscale value (assuming it's a single channel)
                    int grayscaleValue = (rgb >> 16) & 0xFF;
                    
                    pixelArray[i][j] = grayscaleValue;
                }
            }
        }
        catch(Exception e){
            throw e;
        }
    }
    
    public void Predict(int[][] pixelArray){

        differenceArray = new int[WIDTH][HEIGHT];

        for(int i = 1; i < pixelArray.length; i++){
            for (int j = 1; j < pixelArray[0].length; j++) {
                int difference, prediction;

                prediction = (pixelArray[i][j - 1] + pixelArray[i - 1][j]) / 2;
                difference = pixelArray[i][j] - prediction;
                MAX = (difference > MAX) ? difference : MAX;
                MIN = (difference < MIN) ? difference : MIN;
                differenceArray[i][j] = difference;
            }
        }
    }

    public void Quantize(int[][] differenceArray){
        
        int[] levels = new int[9];
        int steps = (MAX - MIN) / 8;

        levels[0] = 0;
        
        for(int i = 1; i < 8; i++){
            levels[i] = levels[i - 1] + steps;
        }

        for(int i = 1; i < differenceArray.length; i++){
            for(int j = 1; j < differenceArray[0].length; j++){
                int difference = differenceArray[i][j];
                int quantizedValue = 0;

                if(difference < levels[0]){
                    quantizedValue = levels[0];
                }
                else if(difference > levels[7]){
                    quantizedValue = levels[7];
                }
                else{
                    for(int k = 0; k < 7; k++){
                        if(difference >= levels[k] && difference <= levels[k + 1]){
                            quantizedValue = levels[k + 1];
                            break;
                        }
                    }
                }
                differenceArray[i][j] = quantizedValue;
            }
        }
    }

    // FOR COMPRESSED IMAGE OUTPUT
    // public void WriteCompressedImage(int[][] pixelArray, String name) {
    //     String path = System.getProperty("user.dir") + "\\CP_"+ name + ".png";
    //     BufferedImage image = new BufferedImage(pixelArray.length, pixelArray[0].length, BufferedImage.TYPE_BYTE_GRAY);
    //     for (int x = 0; x < pixelArray.length; x++) {
    //         for (int y = 0; y < pixelArray[0].length; y++) {
    //             int grayValue = pixelArray[x][y];
    //             int pixelValue = (grayValue << 16) | (grayValue << 8) | grayValue;
    //             image.setRGB(x, y, pixelValue);
    //         }
    //     }
        
    //     File ImageFile = new File(path);
    //     try {
    //         ImageIO.write(image, "png", ImageFile);
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }
    // }

    // FOR BINARY FILE OUTPUT
    // public void SaveCompressedFile(int[][] compressedImage, String name) throws Exception {
    //     String path = System.getProperty("user.dir") + "\\CP_" + name + ".bin";
    //     File compressedFile = new File(path);

    //     try (DataOutputStream dataOut = new DataOutputStream(new FileOutputStream(compressedFile))) {
    //         dataOut.writeShort(WIDTH);
    //         dataOut.writeShort(HEIGHT);


    //     } catch (Exception e) {
    //         throw e;
    //     }
    // }

    public void decompress(File compressedFile) throws Exception {
        try (DataInputStream dataIn = new DataInputStream(new FileInputStream(compressedFile))) {
            WIDTH = dataIn.readShort();
            HEIGHT = dataIn.readShort();
            int[][] decompressedImage = new int[WIDTH][HEIGHT];


            String name = compressedFile.getName().replaceFirst("[.][^.]+$", "");
            // WriteDecompressedImage(decompressedImage, name);
        } catch (Exception e) {
            throw e;
        }
    }


    // public void WriteDecompressedImage(int[][] decompressedImage, String Name) throws Exception{
    //     String name = Name.substring(Name.indexOf("CP_") + 3);
    //     String path = System.getProperty("user.dir") + "\\DP_" + name + ".png";
    //     BufferedImage image = new BufferedImage(decompressedImage.length, decompressedImage[0].length, BufferedImage.TYPE_BYTE_GRAY);
    //     for (int x = 0; x < decompressedImage.length; x++) {
    //         for (int y = 0; y < decompressedImage[0].length; y++) {
    //             int grayValue = decompressedImage[x][y];
    //             int pixelValue = (grayValue << 16) | (grayValue << 8) | grayValue;
    //             image.setRGB(x, y, pixelValue);
    //         }
    //     }

    //     File ImageFile = new File(path);
    //     try {
    //         ImageIO.write(image, "png", ImageFile);
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }
    // }
}