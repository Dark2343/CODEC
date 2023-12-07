import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class VQ {

    ArrayList<int[][]> clusters = new ArrayList<int[][]>();
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

    public int GetOptimalKSize(int[][] pixelArray){
        int width = pixelArray.length, height = pixelArray[0].length;
        int kSize = 0;

        if(width == height){
            kSize = width / 2;
        }
        else if(width > height){
            kSize = height / 2;
        }
        else{
            kSize = width / 2;
        }
        return kSize;
    }

    public void GenerateClusters(int[][] pixelArray, int kSize){
        int width = pixelArray.length, height = pixelArray[0].length;

        for(int i = 0; i < width; i += kSize){
            for(int j = 0; j < height; j += kSize){
                
                int entry[][] = new int[kSize][kSize];

                for(int x = 0; x < kSize; x++){
                    for (int y = 0; y < kSize; y++) {
                        entry[x][y] = pixelArray[x + i][y + j]; 
                    }
                }

                clusters.add(entry);
            }
        }
        
        GetAverageEntry(kSize);
    }

    public void GetAverageEntry(int kSize){
        
        float[][] averageEntry = new float[kSize][kSize];

        for(int i = 0; i < clusters.size(); i++){
            for(int j = 0; j < clusters.get(i).length; j++){
                for(int k = 0; k < clusters.get(i)[j].length; k++){
                    averageEntry[j][k] += clusters.get(i)[j][k];
                }
            }
        }

        for(int i = 0; i < averageEntry.length; i++){
            for(int j = 0; j < averageEntry[0].length; j++){
                averageEntry[i][j] /= kSize;
            }
        }

        SplitClusters(averageEntry, kSize);
    }


    public void SplitClusters(float[][] averageEntry, int kSize){
        if (codeBook != null){

        }
        else{
            int[][] low = new int[kSize][kSize];
            int[][] high = new int[kSize][kSize];
    
            for(int i = 0; i < kSize; i++){
                for(int j = 0; j < kSize; j++){
                    low[i][j] = (int) Math.floor(averageEntry[i][j]);
                    high[i][j] = (int) Math.ceil(averageEntry[i][j]);
                }
            }
    
            codeBook.add(low);
            codeBook.add(high);
        }
    }

    public void compress(File imageFile) throws Exception{
        try{
            int[][] pixelArray = ProcessGrayScaleImage(imageFile);
            int kSize = GetOptimalKSize(pixelArray);
            GenerateClusters(pixelArray, kSize);
        }
        catch(Exception e){
            throw e;
        }
    }

    public void saveCompressedFile(int[][] compressedImage) throws Exception{
        // saveCodeBook();
        try (DataOutputStream dataOut = new DataOutputStream(new FileOutputStream("compressedImage.bin"))){
            // size of the image
            dataOut.writeShort(compressedImage.length);
            dataOut.writeShort(compressedImage[0].length);
            for(int i = 0; i < compressedImage.length; i++){
                for(int j = 0; j < compressedImage[0].length; j++){
                    // Compressed image value
                    dataOut.writeByte(compressedImage[i][j]);
                }
            }
        }   catch (Exception e) {
            throw e;
        }
    }

    // private void saveCodeBook() throws Exception {
    //     try (DataOutputStream dataOut = new DataOutputStream(new FileOutputStream("compressedImage.bin"))){
    //         // Number of entries in the code book
    //         dataOut.writeShort(codeBook.size());
    //         for(int i = 0; i < codeBook.size(); i++){
    //             // Code book entry number
    //             dataOut.writeByte(i);
    //             for(int j = 0; j < codeBook.get(i).length; j++){
    //                 for(int k = 0; k < codeBook.get(i)[j].length; k++){
    //                     // Code book entry value
    //                     dataOut.writeByte(codeBook.get(i)[j][k]);
    //                 }
    //             }
    //         }
    //     }   catch (Exception e) {
    //         throw e;
    //     }
    // }

    public void decompress(File compressedFile) throws Exception{
    //     int[][] compressedImage = readCompressedFile(compressedFile);
    }
}