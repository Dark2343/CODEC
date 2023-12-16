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
    
    ArrayList<Integer> differenceArray = new ArrayList<Integer>();
    int WIDTH, HEIGHT;

    
    public void compress(File imageFile) throws Exception{
        try{
            int[][] pixelArray = ProcessGrayScaleImage(imageFile);
            Predict(pixelArray);
            

            String name = imageFile.getName().replaceFirst("[.][^.]+$", "");

            // WriteCompressedImage(pixelArray, name);
            // SaveCompressedFile(pixelArray, name);
        }
        catch(Exception e){
            throw e;
        }
    }

    public int[][] ProcessGrayScaleImage(File imageFile) throws Exception {
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
            
            return pixelArray;
        }
        catch(Exception e){
            throw e;
        }
    }
    
    public void Predict(int[][] pixelArray){

        for(int i = 0; i < pixelArray.length; i++){
            for (int j = 0; j < pixelArray[0].length; j++) {
                
                if (i == 0 || i == pixelArray.length - 1 || j == 0 || j == pixelArray[0].length) {
                    differenceArray.add(pixelArray[i][j]);
                }
                else{
                    int prediction = (pixelArray[i][j - 1] + pixelArray[i - 1][j]) / 2;
                    int difference = pixelArray[i][j] - prediction;
                    differenceArray.add(difference);
                }
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