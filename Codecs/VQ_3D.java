package Codecs;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import src.EncodingAlgorithm;

public class VQ_3D implements EncodingAlgorithm{
    
    int KSIZE; // Number of clusters in codeBook
    int BSIZE; // Block size
    int HEIGHT, WIDTH;
    int[][] codedRImage, codedGImage, codedBImage;

    ArrayList<float[][]> clustersR = new ArrayList<float[][]>();
    ArrayList<float[][]> clustersG = new ArrayList<float[][]>();
    ArrayList<float[][]> clustersB = new ArrayList<float[][]>();
    
    ArrayList<float[][]> codeBookR = new ArrayList<float[][]>();
    ArrayList<float[][]> codeBookG = new ArrayList<float[][]>();
    ArrayList<float[][]> codeBookB = new ArrayList<float[][]>();


    public VQ_3D(int kSize, int blockSize){
        this.KSIZE = kSize;
        this.BSIZE = blockSize;
    }

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

    public int[][] OverwriteImage(int width, int height, ArrayList<float[][]> codeBook, ArrayList<float[][]> clusters, char color) {
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
            switch (color){
                case 'r':
                    codedRImage[xOffset/BSIZE][yOffset/BSIZE] = minDistanceCluster;
                    break;
                case 'b':
                    codedBImage [xOffset/BSIZE][yOffset/BSIZE] = minDistanceCluster;
                    break;
                case 'g':
                    codedGImage [xOffset/BSIZE][yOffset/BSIZE] = minDistanceCluster;
                    break;
            }
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

            codedRImage = new int[WIDTH/BSIZE][HEIGHT/BSIZE];
            codedGImage = new int[WIDTH/BSIZE][HEIGHT/BSIZE];
            codedBImage = new int[WIDTH/BSIZE][HEIGHT/BSIZE];

            pixelArrayR = OverwriteImage(WIDTH, HEIGHT, codeBookR, clustersR, 'r');
            pixelArrayG = OverwriteImage(WIDTH, HEIGHT, codeBookG, clustersG, 'g');
            pixelArrayB = OverwriteImage(WIDTH, HEIGHT, codeBookB, clustersB, 'b');
            
            String name = imageFile.getName().replaceFirst("[.][^.]+$", "");
            String extension = imageFile.getName().substring(imageFile.getName().lastIndexOf(".") + 1);
            WriteCompressedImage(pixelArrayR, pixelArrayG, pixelArrayB, name, extension);
            SaveCompressedFile(pixelArrayR, pixelArrayG, pixelArrayB, name);
        }
        catch(Exception e){
            throw e;
        }
    }

    // FOR COMPRESSED IMAGE OUTPUT
    public void WriteCompressedImage(int[][] pixelArrayR, int[][] pixelArrayG, int[][] pixelArrayB, String name, String extension) throws Exception {
        String path = System.getProperty("user.dir") + "\\CP_"+ name + "." + extension;
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
            ImageIO.write(image, extension, ImageFile);
        } catch (IOException e) {
            throw e;
        }
    }

    public void SaveCompressedFile(int[][] pixelArrayR, int[][] pixelArrayG, int[][] pixelArrayB, String name) throws Exception {
        String path = System.getProperty("user.dir") + "\\CP_" + name + ".bin";
        File compressedFile = new File(path);

        try (DataOutputStream dataOut = new DataOutputStream(new FileOutputStream(compressedFile))) {
            dataOut.writeShort((short)KSIZE);
            dataOut.writeShort((short)BSIZE);
            dataOut.writeShort((short)WIDTH);
            dataOut.writeShort((short)HEIGHT);

            for (int i = 0; i < KSIZE; i++) {
                dataOut.writeByte(0);
                float[][] entry = codeBookR.get(i);

                for (int j = 0; j < BSIZE; j++) {
                    for (int k = 0; k < BSIZE; k++) {
                        // Convert float to short before writing
                        dataOut.writeShort((short) entry[j][k]);
                    }
                }
            }
            for (int i = 0; i < KSIZE; i++) {
                dataOut.writeByte(0);
                float[][] entry = codeBookG.get(i);

                for (int j = 0; j < BSIZE; j++) {
                    for (int k = 0; k < BSIZE; k++) {
                        // Convert float to short before writing
                        dataOut.writeShort((short) entry[j][k]);
                    }
                }
            }
            for (int i = 0; i < KSIZE; i++) {
                dataOut.writeByte(0);
                float[][] entry = codeBookB.get(i);

                for (int j = 0; j < BSIZE; j++) {
                    for (int k = 0; k < BSIZE; k++) {
                        // Convert float to short before writing
                        dataOut.writeShort((short) entry[j][k]);
                    }
                }
            }

            for (int i=0; i < codedRImage.length; i++){
                for (int j = 0; j < codedRImage[0].length; j++){
                    dataOut.writeShort((short)codedRImage[i][j]);
                    dataOut.writeShort((short)codedBImage[i][j]);
                    dataOut.writeShort((short)codedGImage[i][j]);
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
            int[][] decompressedImageR = new int[WIDTH][HEIGHT];
            int[][] decompressedImageG = new int[WIDTH][HEIGHT];
            int[][] decompressedImageB = new int[WIDTH][HEIGHT];

            for (int i = 0; i < KSIZE; i++) {
                dataIn.readByte();
                float[][] entry = new float[BSIZE][BSIZE];

                for (int j = 0; j < BSIZE; j++) {
                    for (int k = 0; k < BSIZE; k++) {
                        // Convert short to float after reading
                        entry[j][k] = dataIn.readShort();
                    }
                }

                codeBookR.add(entry);
            }
            for (int i = 0; i < KSIZE; i++) {
                dataIn.readByte();
                float[][] entry = new float[BSIZE][BSIZE];

                for (int j = 0; j < BSIZE; j++) {
                    for (int k = 0; k < BSIZE; k++) {
                        // Convert short to float after reading
                        entry[j][k] = dataIn.readShort();
                    }
                }

                codeBookG.add(entry);
            }
            for (int i = 0; i < KSIZE; i++) {
                dataIn.readByte();
                float[][] entry = new float[BSIZE][BSIZE];

                for (int j = 0; j < BSIZE; j++) {
                    for (int k = 0; k < BSIZE; k++) {
                        // Convert short to float after reading
                        entry[j][k] = dataIn.readShort();
                    }
                }

                codeBookB.add(entry);
            }

            codedRImage = new int[WIDTH/BSIZE][HEIGHT/BSIZE];
            codedGImage = new int[WIDTH/BSIZE][HEIGHT/BSIZE];
            codedBImage = new int[WIDTH/BSIZE][HEIGHT/BSIZE];
            for (int i = 0; i < codedRImage.length; i++){
                for (int j = 0; j < codedRImage[0]. length; j++){
                    codedRImage[i][j] = dataIn.readShort();
                }
            }
            for (int i = 0; i < codedGImage.length; i++){
                for (int j = 0; j < codedGImage[0]. length; j++){
                    codedGImage[i][j] = dataIn.readShort();
                }
            }
            for (int i = 0; i < codedBImage.length; i++){
                for (int j = 0; j < codedBImage[0]. length; j++){
                    codedBImage[i][j] = dataIn.readShort();
                }
            }

            // rebuild Image
            for (int i = 0; i < codedRImage.length; i++){
                for (int j = 0; j < codedRImage[0]. length; j++){
                    float[][] clusterR = codeBookR.get(codedRImage[i][j]);
                    float[][] clusterG = codeBookG.get(codedGImage[i][j]);
                    float[][] clusterB= codeBookB.get(codedBImage[i][j]);
                    for (int x = 0; x < BSIZE; x++){
                        for (int y = 0; y < BSIZE; y++) {
                            decompressedImageR[(i * BSIZE) + x][(j * BSIZE) + y] = (int) clusterR[x][y];
                            decompressedImageG[(i * BSIZE) + x][(j * BSIZE) + y] = (int) clusterG[x][y];
                            decompressedImageB[(i * BSIZE) + x][(j * BSIZE) + y] = (int) clusterB[x][y];
                        }
                    }
                }
            }
            String name = compressedFile.getName().replaceFirst("[.][^.]+$", "");
            WriteDecompressedImage(decompressedImageR,decompressedImageG,decompressedImageB, name);
        } catch (Exception e) {
            throw e;
        }
    }

    public void WriteDecompressedImage(int[][] decompressedImageR,int[][] decompressedImageG,int[][] decompressedImageB, String Name) throws Exception{
        String name = Name.substring(Name.indexOf("CP_") + 3);
        String path = System.getProperty("user.dir") + "\\DP_" + name + ".png";
        BufferedImage image = new BufferedImage(decompressedImageR.length, decompressedImageR[0].length, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < decompressedImageR.length; x++) {
            for (int y = 0; y < decompressedImageR[0].length; y++) {
                int rValue = decompressedImageR[x][y];
                int gValue = decompressedImageG[x][y];
                int bValue = decompressedImageB[x][y];
                int pixelValue = (rValue << 16) | (gValue << 8) | bValue;
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
