import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class VQ {

    public int[][] ProcessGrayScaleImage(File imageFile) throws Exception {
        try{
            BufferedImage img = ImageIO.read(imageFile);
            int width = img.getWidth(), height = img.getHeight();
            
            int[][] pixelArray = new int[width][height];
    
            for(int i = 0; i < width; i++){
                for(int j = 0; j < height; j++){

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

    public void compress(File imageFile) throws Exception{
        try{
            int[][] pixelArray = ProcessGrayScaleImage(imageFile);
            System.out.println("SUCCESS");

        }
        catch(Exception e){
            throw e;
        }
    }


    public void decompress(File imageFile) throws Exception{
        
    }
}