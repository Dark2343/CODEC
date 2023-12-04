import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class VQ {

    ArrayList<int[][]> codeBook = new ArrayList<int[][]>();

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

    // public void GenerateKSize(){

    // }

    public void GenerateCodeBook(int[][] pixelArray, int kSize){
        int width = pixelArray.length, height = pixelArray[0].length;

        for(int i = 0; i < width; i += kSize){
            for(int j = 0; j < height; j += kSize){
                
                int entry[][] = new int[kSize][kSize];

                for(int x = 0; x < kSize; x++){
                    for (int y = 0; y < kSize; y++) {
                        entry[x][y] = pixelArray[x + i][y + j]; 
                    }
                }

                codeBook.add(entry);
            }
        }
    }

    public void GetAverageEntry(int kSize){
        
        float[][] averageEntry = new float[kSize][kSize];

        for(int i = 0; i < codeBook.size(); i++){
            for(int j = 0; j < codeBook.get(i).length; j++){
                for(int k = 0; k < codeBook.get(i)[j].length; k++){
                    averageEntry[j][k] += codeBook.get(i)[j][k];
                }
            }
        }

        for(int i = 0; i < averageEntry.length; i++){
            for(int j = 0; j < averageEntry[0].length; j++){
                averageEntry[i][j] /= kSize;
            }
        }
    }
    
    public void SplitCodeBook(int[][] averageEntry, int kSize){
        int[][] low = new int[kSize][kSize], high = low;
        int averageLow = 0, averageHigh = 0;

        for(int i = 0; i < averageEntry.length; i++){
            for(int j = 0; j < averageEntry[0].length; j++){
                low[i][j] = (int) Math.floor(averageEntry[i][j]);
                averageLow += low[i][j]; 
                high[i][j] = (int) Math.ceil(averageEntry[i][j]);
                averageHigh += high[i][j]; 
            }
        }

    }

    public void compress(File imageFile) throws Exception{
        try{
            int[][] pixelArray = ProcessGrayScaleImage(imageFile);
            GenerateCodeBook(pixelArray, 2);

        }
        catch(Exception e){
            throw e;
        }
    }


    public void decompress(File imageFile) throws Exception{
        
    }
}