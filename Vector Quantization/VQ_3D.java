import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import javax.imageio.ImageIO;

// NOTE THE RGB DOES WORK AT ALL RIGHT NOW, THE VARIABLE IS JUST FOR DEMO PURPOSES


public class VQ_3D {
    
    ArrayList<int[][][]> clusters = new ArrayList<int[][][]>();
    ArrayList<int[][][]> codeBook = new ArrayList<int[][][]>();
    final int RGB = 3; 

    public int[][][] ProcessRGBImage(File imageFile) throws Exception {
        try{
            BufferedImage img = ImageIO.read(imageFile);
            int width = img.getWidth(), height = img.getHeight();
            
            int[][][] pixelArray = new int[width][height][RGB];
            
            for(int i = 0; i < width; i++){
                for(int j = 0; j < height; j++){
                    for(int k = 0; k < RGB; k++){
                        // Get the RGB value of the pixel
                        int rgb = img.getRGB(i, j);
                                                                    
                        pixelArray[i][j][k] = rgb;
                    }
                }
            }
            
            return pixelArray;
        }
        catch(Exception e){
            throw e;
        }
    }

    public void GenerateClusters(int[][][] pixelArray, int kSize){
        int width = pixelArray.length, height = pixelArray[0].length;

        for(int i = 0; i < width; i += kSize){
            for(int j = 0; j < height; j += kSize){
                for (int k = 0; k < RGB; k++) {
                    int entry[][][] = new int[kSize][kSize][RGB];
                    
                    for(int x = 0; x < kSize; x++){
                        for (int y = 0; y < kSize; y++) {
                            entry[x][y][k] = pixelArray[x + i][y + j][k]; 
                        }
                    }
                    clusters.add(entry);
                }
            }
        }
        
        GetAverageEntry(kSize);
    }

    public void GetAverageEntry(int kSize){
        
        float[][][] averageEntry = new float[kSize][kSize][RGB];

        for(int i = 0; i < clusters.size(); i++){
            for(int j = 0; j < clusters.get(i).length; j++){
                for(int k = 0; k < clusters.get(i)[j].length; k++){
                    for(int l = 0; l < RGB; l++){
                        averageEntry[j][k][l] += clusters.get(i)[i][k][l];
                    }
                }
            }
        }

        for(int i = 0; i < averageEntry.length; i++){
            for(int j = 0; j < averageEntry[0].length; j++){
                for(int k = 0; k < RGB; k++){
                    averageEntry[i][j][k] /= kSize;
                }
            }
        }

        SplitClusters(averageEntry, kSize);
    }


    public void SplitClusters(float[][][] averageEntry, int kSize){
        
    }

    public void compress(File imageFile, int kSize, int blockSize) throws Exception{
        try{
            // int[][][] pixelArray = ProcessRGBImage(imageFile);
        }
        catch(Exception e){
            throw e;
        }
    }

    public void decompress(File compressedFile) throws Exception{
        // int[][][] compressedImage = readCompressedFile(compressedFile);
    }
}
