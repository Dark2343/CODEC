import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class VQ_3D {
    
    int KSIZE; // Number of clusters in codeBook
    int BSIZE; // Block size
    int HEIGHT, WIDTH;
    ArrayList<float[][]> clustersR = new ArrayList<float[][]>();
    ArrayList<float[][]> clustersG = new ArrayList<float[][]>();
    ArrayList<float[][]> clustersB = new ArrayList<float[][]>();
    
    ArrayList<float[][]> codeBookR = new ArrayList<float[][]>();
    ArrayList<float[][]> codeBookG = new ArrayList<float[][]>();
    ArrayList<float[][]> codeBookB = new ArrayList<float[][]>();

    public ArrayList<int[][]> ProcessRGBImage(File imageFile) throws Exception {
        try{
            BufferedImage img = ImageIO.read(imageFile);
            WIDTH = img.getWidth();
            HEIGHT = img.getHeight();
            
            int[][] pixelArrayR = new int[WIDTH][HEIGHT];
            int[][] pixelArrayG = new int[WIDTH][HEIGHT];
            int[][] pixelArrayB = new int[WIDTH][HEIGHT];
            
            for (int i = 0; i < WIDTH; i++) {
                for (int j = 0; j < HEIGHT; j++) {
                    int rgb = img.getRGB(i, j);
                    
                    // Extract R, G, B values
                    int r = (rgb >> 16) & 0xFF;
                    int g = (rgb >> 8) & 0xFF;
                    int b = rgb & 0xFF;
                    
                    pixelArrayR[i][j] = r;  // Red
                    pixelArrayG[i][j] = g;  // Green
                    pixelArrayB[i][j] = b;  // Blue
                }
            }
            
            ArrayList<int[][]> rgbArrayList = new ArrayList<int[][]>();
            rgbArrayList.add(pixelArrayR);
            rgbArrayList.add(pixelArrayG);
            rgbArrayList.add(pixelArrayB);
            return rgbArrayList;
        }
        catch(Exception e){
            throw e;
        }
    }
    
    public void GenerateClusters(int[][] pixelArray, int color){
        int width = pixelArray.length, height = pixelArray[0].length;
        
        for(int i = 0; i < width; i += BSIZE){
            for(int j = 0; j < height; j += BSIZE){
                
                float entry[][] = new float[BSIZE][BSIZE];
                
                for(int x = 0; x < BSIZE; x++){
                    for (int y = 0; y < BSIZE; y++) {
                        entry[x][y] = pixelArray[x + i][y + j]; 
                    }
                }

                switch (color) {
                    case 0:
                        clustersR.add(entry);
                        break;
                    case 1:
                        clustersG.add(entry);
                        break;
                    case 2:
                        clustersB.add(entry);
                        break;
                    default:
                        break;
                }
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

    public void SplitClusters(float[][] averageEntry, ArrayList<float[][]> codeBook, ArrayList<float[][]> clusters){
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
            nearestVectors = CalculateDistances(tempBook, clusters);
            codeBook.clear();

            for (int i = 0; i < nearestVectors.size(); i++) {
                codeBook.add(GetAverageEntry(nearestVectors.get(i)));
            }
        }
    }

    public ArrayList<ArrayList<float[][]>> CalculateDistances(ArrayList<float[][]> tempBook, ArrayList<float[][]> clusters){
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

    public int[][] OverwriteImage(int width, int height, ArrayList<float[][]> codeBook, ArrayList<float[][]> clusters) {
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
            int yOffset = (i % (width / BSIZE)) * BSIZE;
            int xOffset = (i / (width / BSIZE)) * BSIZE;

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
            ArrayList<int[][]> rgbArrayList = ProcessRGBImage(imageFile); 
            int[][] pixelArrayR = rgbArrayList.get(0);
            int[][] pixelArrayG = rgbArrayList.get(1);
            int[][] pixelArrayB = rgbArrayList.get(2);

            GenerateClusters(pixelArrayR, 0);
            GenerateClusters(pixelArrayG, 1);
            GenerateClusters(pixelArrayB, 2);

            SplitClusters(GetAverageEntry(clustersR), codeBookR, clustersR);
            SplitClusters(GetAverageEntry(clustersG), codeBookG, clustersG);
            SplitClusters(GetAverageEntry(clustersB), codeBookB, clustersB);

            pixelArrayR = OverwriteImage(WIDTH, HEIGHT, codeBookR, clustersR);
            pixelArrayG = OverwriteImage(WIDTH, HEIGHT, codeBookG, clustersG);
            pixelArrayB = OverwriteImage(WIDTH, HEIGHT, codeBookB, clustersB);
            
            String name = imageFile.getName().replaceFirst("[.][^.]+$", "");
            WriteCompressedImage(pixelArrayR, pixelArrayG, pixelArrayB, name);
        }
        catch(Exception e){
            throw e;
        }
    }

    // FOR COMPRESSED IMAGE OUTPUT
    public void WriteCompressedImage(int[][] pixelArrayR, int[][] pixelArrayG, int[][] pixelArrayB, String name) {
        String path = System.getProperty("user.dir") + "\\Compressed_"+ name + "_RGB.png";
        BufferedImage image = new BufferedImage(pixelArrayG.length, pixelArrayG[0].length, BufferedImage.TYPE_INT_RGB);
        
        for (int x = 0; x < pixelArrayG.length; x++) {
            for (int y = 0; y < pixelArrayG[0].length; y++) {
                int r = pixelArrayR[x][y];
                int g = pixelArrayG[x][y];
                int b = pixelArrayB[x][y];

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

    void decompress(){
        
    }
}
