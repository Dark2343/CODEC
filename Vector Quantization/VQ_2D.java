import java.io.File;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.awt.image.BufferedImage;

public class VQ_2D {

    int KSIZE; // Number of clusters in codebook
    int BSIZE; // Block size
    int WIDTH, HEIGHT;
    ArrayList<float[][]> clusters = new ArrayList<float[][]>();
    ArrayList<float[][]> codeBook = new ArrayList<float[][]>();
    
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
    
    public void GenerateClusters(int[][] pixelArray){
        int width = pixelArray.length, height = pixelArray[0].length;
        
        for(int i = 0; i < width; i += BSIZE){
            for(int j = 0; j < height; j += BSIZE){
                
                float entry[][] = new float[BSIZE][BSIZE];
                
                for(int x = 0; x < BSIZE; x++){
                    for (int y = 0; y < BSIZE; y++) {
                        entry[x][y] = pixelArray[x + i][y + j]; 
                    }
                }
                clusters.add(entry);
            }
        }
    }
    
    public float[][] GetAverageEntry(ArrayList<float[][]> clusterGroup){
        
        float[][] averageEntry = new float[BSIZE][BSIZE];
        
        for(int i = 0; i < clusterGroup.size(); i++){
            for(int j = 0; j < clusterGroup.get(i).length; j++){
                for(int k = 0; k < clusterGroup.get(i)[j].length; k++){
                    averageEntry[j][k] += clusterGroup.get(i)[j][k];
                }
            }
        }
        
        for(int i = 0; i < averageEntry.length; i++){
            for(int j = 0; j < averageEntry[0].length; j++){
                averageEntry[i][j] /= clusterGroup.size();
            }
        }
        return averageEntry;
    }
    
    public void SplitClusters(float[][] averageEntry){
        codeBook.add(averageEntry);
        while (codeBook.size() < KSIZE) {
            ArrayList<float[][]> tempBook = new ArrayList<float[][]>();
            ArrayList<ArrayList<float[][]>> nearestVectors = new ArrayList<ArrayList<float[][]>>();
            
            for(float[][] cluster : codeBook){
                float[][] low = new float[BSIZE][BSIZE];
                float[][] high = new float[BSIZE][BSIZE];
                
                for(int i = 0; i < cluster.length; i++){
                    for(int j = 0; j < cluster[0].length; j++){
                        low[i][j] = (float) Math.floor(cluster[i][j]);
                        high[i][j] = (float) Math.ceil(cluster[i][j]);
                    }
                }
                tempBook.add(low);
                tempBook.add(high);
            }
            nearestVectors = CalculateDistances(tempBook);
            codeBook.clear();

            for (int i = 0; i < nearestVectors.size(); i++) {
                codeBook.add(GetAverageEntry(nearestVectors.get(i)));
            }
        }
    }
    
    public ArrayList<ArrayList<float[][]>> CalculateDistances(ArrayList<float[][]> tempBook){
        ArrayList<ArrayList<float[][]>> nearestVectors = new ArrayList<ArrayList<float[][]>>();

        for (int i = 0; i < tempBook.size(); i++) {
            nearestVectors.add(new ArrayList<float[][]>());
        }

        for(float[][] cluster : clusters){
            int minDistance = Integer.MAX_VALUE;
            int minDistanceCluster = -1;
            
            for(int i = 0; i < tempBook.size(); i++){
                int tempDistance = 0;

                for(int x = 0; x < BSIZE; x++){
                    for(int y = 0; y < BSIZE; y++){
                        tempDistance += Math.abs(cluster[x][y] - tempBook.get(i)[x][y]);
                    }
                }
                minDistanceCluster = (minDistance > tempDistance) ? i : minDistanceCluster;
                minDistance = Math.min(minDistance, tempDistance);
            }
            nearestVectors.get(minDistanceCluster).add(cluster);
        }
        return nearestVectors;
    }

    public int[][] OverwriteImage(int width, int height) {
        int[][] newPixelArray = new int[width][height];
    
        for (int i = 0; i < clusters.size(); i++) {
            float[][] cluster = clusters.get(i);
    
            // Find the closest codeBook entry
            int minDistance = Integer.MAX_VALUE;
            int minDistanceCluster = -1;
    
            for (int j = 0; j < codeBook.size(); j++) {
                int tempDistance = 0;

                for (int x = 0; x < BSIZE; x++) {
                    for (int y = 0; y < BSIZE; y++) {
                        tempDistance += Math.abs(cluster[x][y] - codeBook.get(j)[x][y]);
                    }
                }

                if (tempDistance < minDistance) {
                    minDistance = tempDistance;
                    minDistanceCluster = j;
                }
            }
    
            // Set new pixel array values based on the chosen codeBook entry
            float[][] chosenCodeBookEntry = codeBook.get(minDistanceCluster);
            int yOffset = (i % (width / BSIZE)) * BSIZE;  // Corrected calculation
            int xOffset = (i / (width / BSIZE)) * BSIZE;  // Corrected calculation
    
            for (int x = 0; x < BSIZE; x++) {
                for (int y = 0; y < BSIZE; y++) {
                    newPixelArray[y + xOffset][x + yOffset] = (int) chosenCodeBookEntry[x][y];
                }
            }
        }
    
        return newPixelArray;
    }

    public void compress(File imageFile, int kSize, int blockSize) throws Exception{
        try{
            KSIZE = kSize;
            BSIZE = blockSize;
            int[][] pixelArray = ProcessGrayScaleImage(imageFile);
            
            GenerateClusters(pixelArray);
            SplitClusters(GetAverageEntry(clusters));
            pixelArray = OverwriteImage(WIDTH, HEIGHT);

            String name = imageFile.getName().replaceFirst("[.][^.]+$", "");
            WriteCompressedImage(pixelArray, name);
        }
        catch(Exception e){
            throw e;
        }
    }

    // FOR BINARY FILE OUTPUT
    
    // public void saveCompressedFile(int[][] compressedImage) throws Exception{
    //     // saveCodeBook();
    //     try (DataOutputStream dataOut = new DataOutputStream(new FileOutputStream("compressedImage.bin"))){
    //         // size of the image
    //         dataOut.writeShort(compressedImage.length);
    //         dataOut.writeShort(compressedImage[0].length);
    //         for(int i = 0; i < compressedImage.length; i++){
    //             for(int j = 0; j < compressedImage[0].length; j++){
    //                 // Compressed image value
    //                 dataOut.writeByte(compressedImage[i][j]);
    //             }
    //         }
    //     }   catch (Exception e) {
    //         throw e;
    //     }
    // }


    // FOR COMPRESSED IMAGE OUTPUT
    public void WriteCompressedImage(int[][] pixelArray, String name) {
        String path = System.getProperty("user.dir") + "\\Compressed_"+ name + "_Grayscaled.png";
        BufferedImage image = new BufferedImage(pixelArray.length, pixelArray[0].length, BufferedImage.TYPE_BYTE_GRAY);
        for (int x = 0; x < pixelArray.length; x++) {
            for (int y = 0; y < pixelArray[0].length; y++) {
                int grayValue = pixelArray[x][y];
                int pixelValue = (grayValue << 16) | (grayValue << 8) | grayValue;
                image.setRGB(x, y, pixelValue);
            }
        }

        File ImageFile = new File(path);
        try {
            ImageIO.write(image, "png", ImageFile);
        } catch (IOException e) {
            e.printStackTrace();
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
        // int[][] compressedImage = readCompressedFile(compressedFile);
    }
}