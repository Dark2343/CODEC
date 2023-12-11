import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

// NOTE THE RGB DOES WORK AT ALL RIGHT NOW, THE VARIABLE IS JUST FOR DEMO PURPOSES


public class VQ_3D {
    
    int KSIZE; // Number of clusters in codeBook
    int BSIZE; // Block size
    int HEIGHT, WIDTH;
    ArrayList<float[][][]> clusters = new ArrayList<float[][][]>();
    ArrayList<float[][][]> codeBook = new ArrayList<float[][][]>();
    final int RGB = 3; 

    public int[][][] ProcessRGBImage(File imageFile) throws Exception {
        try{
            BufferedImage img = ImageIO.read(imageFile);
            WIDTH = img.getWidth();
            HEIGHT = img.getHeight();
            
            int[][][] pixelArray = new int[WIDTH][HEIGHT][RGB];
            
            for (int i = 0; i < WIDTH; i++) {
                for (int j = 0; j < HEIGHT; j++) {
                    int rgb = img.getRGB(i, j);
                    
                    // Extract R, G, B values
                    int r = (rgb >> 16) & 0xFF;
                    int g = (rgb >> 8) & 0xFF;
                    int b = rgb & 0xFF;
                    
                    pixelArray[i][j][0] = r;  // Red
                    pixelArray[i][j][1] = g;  // Green
                    pixelArray[i][j][2] = b;  // Blue
                }
            }
            
            return pixelArray;
        }
        catch(Exception e){
            throw e;
        }
    }
    
    public void GenerateClusters(int[][][] pixelArray) {
        int width = pixelArray.length, height = pixelArray[0].length;
        
        for(int i = 0; i < width; i += BSIZE){
            for(int j = 0; j < height; j += BSIZE){
                for (int k = 0; k < RGB; k++) {

                    float entry[][][] = new float[BSIZE][BSIZE][RGB];
                    
                    for (int x = 0; x < BSIZE; x++) {
                        for (int y = 0; y < BSIZE; y++) {
                            entry[x][y][k] = pixelArray[x + i][y + j][k];
                        }
                    }
                    clusters.add(entry);
                }
            }
        }
    }

    public float[][][] GetAverageEntry(ArrayList<float[][][]> clusterGroup){
        
        float[][][] averageEntry = new float[BSIZE][BSIZE][RGB];
        
        if (clusterGroup.size() > 0) {
            for(int i = 0; i < clusterGroup.size(); i++){
                for(int j = 0; j < clusterGroup.get(i).length; j++){
                    for(int k = 0; k < clusterGroup.get(i)[j].length; k++){
                        for(int l = 0; l < RGB; l++){
                            averageEntry[j][k][l] += clusterGroup.get(i)[j][k][l];
                        }
                    }
                }
            }
            
            for(int i = 0; i < averageEntry.length; i++){
                for(int j = 0; j < averageEntry[0].length; j++){
                    for(int k = 0; k < RGB; k++){
                        averageEntry[i][j][k] /= clusterGroup.size();
                    }
                }
            }
        }
        return averageEntry;
    }

    public void SplitClusters(float[][][] averageEntry){
        codeBook.add(averageEntry);
        while (codeBook.size() < KSIZE) {
            ArrayList<float[][][]> tempBook = new ArrayList<float[][][]>();
            ArrayList<ArrayList<float[][][]>> nearestVectors = new ArrayList<ArrayList<float[][][]>>();
            
            for(float[][][] cluster : codeBook){
                float[][][] low = new float[BSIZE][BSIZE][RGB];
                float[][][] high = new float[BSIZE][BSIZE][RGB];
                
                for(int i = 0; i < cluster.length; i++){
                    for(int j = 0; j < cluster[0].length; j++){
                        for(int k = 0; k < RGB; k++){
                            low[i][j][k] = (float) Math.floor(cluster[i][j][k]);
                            high[i][j][k] = (float) Math.ceil(cluster[i][j][k]);
                        }
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
            System.out.println("LOL");
        }
    }

    public ArrayList<ArrayList<float[][][]>> CalculateDistances(ArrayList<float[][][]> tempBook){
        ArrayList<ArrayList<float[][][]>> nearestVectors = new ArrayList<ArrayList<float[][][]>>();

        for (int i = 0; i < tempBook.size(); i++) {
            nearestVectors.add(new ArrayList<float[][][]>());
        }

        for(float[][][] cluster : clusters){
            int minDistance = Integer.MAX_VALUE;
            int minDistanceCluster = -1;
            
            for(int i = 0; i < tempBook.size(); i++){
                int tempDistance = 0;

                for(int x = 0; x < BSIZE; x++){
                    for(int y = 0; y < BSIZE; y++){
                        for(int z = 0; z < RGB; z++){
                            tempDistance += Math.abs(cluster[x][y][z] - tempBook.get(i)[x][y][z]);
                        }
                    }
                }
                minDistanceCluster = (minDistance > tempDistance) ? i : minDistanceCluster;
                minDistance = Math.min(minDistance, tempDistance);
            }
            nearestVectors.get(minDistanceCluster).add(cluster);
        }
        return nearestVectors;
    }

    public int[][][] OverwriteImage(int width, int height) {
        int[][][] newPixelArray = new int[width][height][RGB];
    
        for (int i = 0; i < clusters.size(); i++) {
            float[][][] cluster = clusters.get(i);
    
            // Find the closest codeBook entry
            int minDistance = Integer.MAX_VALUE;
            int minDistanceCluster = -1;
    
            for (int j = 0; j < codeBook.size(); j++) {
                int tempDistance = 0;

                for (int x = 0; x < BSIZE; x++) {
                    for (int y = 0; y < BSIZE; y++) {
                        for(int z = 0; z < RGB; z++){
                            tempDistance += Math.abs(cluster[x][y][z] - codeBook.get(j)[x][y][z]);
                        }
                    }
                }

                if (tempDistance < minDistance) {
                    minDistance = tempDistance;
                    minDistanceCluster = j;
                }
            }
    
            // Set new pixel array values based on the chosen codeBook entry
            float[][][] chosenCodeBookEntry = codeBook.get(minDistanceCluster);
            int blockIndex = i / BSIZE;  // Calculate the current block index
            int xOffset = (blockIndex % (width / BSIZE)) * BSIZE;
            int yOffset = (blockIndex / (width / BSIZE)) * BSIZE;

    
            for (int x = 0; x < BSIZE; x++) {
                for (int y = 0; y < BSIZE; y++) {
                    for(int z = 0; z < RGB; z++){
                        newPixelArray[x + xOffset][y + yOffset][z] = (int) chosenCodeBookEntry[x][y][z];
                    }
                }
            }
        }
    
        return newPixelArray;
    }

    public void compress(File imageFile, int kSize, int blockSize) throws Exception{
        try{
            KSIZE = kSize;
            BSIZE = blockSize;
            int[][][] pixelArray = ProcessRGBImage(imageFile);

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

    // FOR COMPRESSED IMAGE OUTPUT
    public void WriteCompressedImage(int[][][] pixelArray, String name) {
        String path = System.getProperty("user.dir") + "\\Compressed_"+ name + "_RGB.png";
        BufferedImage image = new BufferedImage(pixelArray.length, pixelArray[0].length, BufferedImage.TYPE_INT_RGB);
        
        for (int x = 0; x < pixelArray.length; x++) {
            for (int y = 0; y < pixelArray[0].length; y++) {
                int r = pixelArray[x][y][0];
                int g = pixelArray[x][y][1];
                int b = pixelArray[x][y][2];

                int rgb = (r << 16) | (g << 8) | b;
                image.setRGB(x, y, rgb);
            }
        }

        File ImageFile = new File(path);
        try {
            ImageIO.write(image, "png", ImageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void decompress(File compressedFile) throws Exception{
        // int[][][] compressedImage = readCompressedFile(compressedFile);
    }
}
