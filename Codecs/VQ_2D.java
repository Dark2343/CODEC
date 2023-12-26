package Codecs;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import src.CodecType;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.awt.image.BufferedImage;

public class VQ_2D implements CodecType {

    int KSIZE; // Number of clusters in codeBook
    int BSIZE; // Block size
    int WIDTH, HEIGHT;
    ArrayList<float[][]> clusters = new ArrayList<float[][]>();
    ArrayList<float[][]> codeBook = new ArrayList<float[][]>();
    int [][] codedImage;
    
    public VQ_2D(int kSize, int blockSize){
        this.KSIZE = kSize;
        this.BSIZE = blockSize;
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
        codedImage = new int[width/BSIZE][height/BSIZE];
    
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
            codedImage[xOffset/BSIZE][yOffset/BSIZE] = minDistanceCluster;
            for (int x = 0; x < BSIZE; x++) {
                for (int y = 0; y < BSIZE; y++) {
                    newPixelArray[y + xOffset][x + yOffset] = (int) chosenCodeBookEntry[x][y];
                }
            }
        }
    
        return newPixelArray;
    }

    @Override
    public void Compress(File imageFile) throws Exception{
        try{
            int[][] pixelArray = ProcessGrayScaleImage(imageFile);
            
            GenerateClusters(pixelArray);
            SplitClusters(GetAverageEntry(clusters));
            pixelArray = OverwriteImage(WIDTH, HEIGHT);

            String name = imageFile.getName().replaceFirst("[.][^.]+$", "");
            String extension = imageFile.getName().substring(imageFile.getName().lastIndexOf(".") + 1);
            WriteCompressedImage(pixelArray, name, extension);
            SaveCompressedFile(pixelArray, name);
        }
        catch(Exception e){
            throw e;
        }
    }

    // FOR COMPRESSED IMAGE OUTPUT
    public void WriteCompressedImage(int[][] pixelArray, String name, String extension) throws Exception{
        String path = System.getProperty("user.dir") + "\\CP_"+ name + "." + extension;
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
            ImageIO.write(image, extension, ImageFile);
        } catch (IOException e) {
            throw e;
        }
    }

    // FOR BINARY FILE OUTPUT
    // Modify SaveCompressedFile method
    public void SaveCompressedFile(int[][] compressedImage, String name) throws Exception {
        String path = System.getProperty("user.dir") + "\\CP_" + name + ".bin";
        File compressedFile = new File(path);


        try (DataOutputStream dataOut = new DataOutputStream(new FileOutputStream(compressedFile))) {
            dataOut.writeShort(KSIZE);
            dataOut.writeShort(BSIZE);
            dataOut.writeShort(WIDTH);
            dataOut.writeShort(HEIGHT);

            for (int i = 0; i < KSIZE; i++) {
                dataOut.writeByte(0);
                float[][] entry = codeBook.get(i);

                for (int j = 0; j < BSIZE; j++) {
                    for (int k = 0; k < BSIZE; k++) {
                        // Convert float to short before writing
                        dataOut.writeShort((short) entry[j][k]);
                    }
                }
            }

            for (int i=0; i < codedImage.length; i++){
                for (int j = 0; j < codedImage[0].length; j++){
                    dataOut.writeShort(codedImage[i][j]);
                }
            }
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void Decompress(File compressedFile) throws Exception {
        try (DataInputStream dataIn = new DataInputStream(new FileInputStream(compressedFile))) {
            KSIZE = dataIn.readShort();
            BSIZE = dataIn.readShort();
            WIDTH = dataIn.readShort();
            HEIGHT = dataIn.readShort();
            int[][] decompressedImage = new int[WIDTH][HEIGHT];

            for (int i = 0; i < KSIZE; i++) {
                dataIn.readByte();
                float[][] entry = new float[BSIZE][BSIZE];

                for (int j = 0; j < BSIZE; j++) {
                    for (int k = 0; k < BSIZE; k++) {
                        // Convert short to float after reading
                        entry[j][k] = dataIn.readShort();
                    }
                }

                codeBook.add(entry);
            }

            codedImage = new int[WIDTH/BSIZE][HEIGHT/BSIZE];
            for (int i = 0; i < codedImage.length; i++){
                for (int j = 0; j < codedImage[0]. length; j++){
                    codedImage[i][j] = dataIn.readShort();
                }
            }

            // rebuild Image
            for (int i = 0; i < codedImage.length; i++){
                for (int j = 0; j < codedImage[0]. length; j++){
                    float[][] cluster = codeBook.get(codedImage[i][j]);
                    for (int x = 0; x < BSIZE; x++){
                        for (int y = 0; y < BSIZE; y++) {
                            decompressedImage[(i * BSIZE) + x][(j * BSIZE) + y] = (int) cluster[x][y];
                        }
                    }
                }
            }
            String name = compressedFile.getName().replaceFirst("[.][^.]+$", "");
            WriteDecompressedImage(decompressedImage, name);
        } catch (Exception e) {
            throw e;
        }
    }

    public void WriteDecompressedImage(int[][] decompressedImage, String Name) throws Exception{
        String name = Name.substring(Name.indexOf("CP_") + 3);
        String path = System.getProperty("user.dir") + "\\DP_" + name + ".png";
        BufferedImage image = new BufferedImage(decompressedImage.length, decompressedImage[0].length, BufferedImage.TYPE_BYTE_GRAY);
        for (int x = 0; x < decompressedImage.length; x++) {
            for (int y = 0; y < decompressedImage[0].length; y++) {
                int grayValue = decompressedImage[x][y];
                int pixelValue = (grayValue << 16) | (grayValue << 8) | grayValue;
                image.setRGB(x, y, pixelValue);
            }
        }

        File ImageFile = new File(path);
        try {
            ImageIO.write(image, "png", ImageFile);
        } catch (IOException e) {
            throw e;
        }
    }
}