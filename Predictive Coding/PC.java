import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class PC {

    int WIDTH, HEIGHT, MAX = Integer.MIN_VALUE, MIN = Integer.MAX_VALUE;;
    int[][] pixelArray, differenceArray;
    int[][] predictedArray;
    int[] firstRow, firstColumn, levels;
    int quantizationLevel = 32;


    public void compress(File imageFile) throws Exception{
        try{
            ProcessGrayScaleImage(imageFile);
            Predict(pixelArray);
            int[][] quantizedArray = Quantize(differenceArray);
            String name = imageFile.getName().replaceFirst("[.][^.]+$", "");

            SaveCompressedFile(quantizedArray, name);
        }
        catch(Exception e){
            throw e;
        }
    }

    public void ProcessGrayScaleImage(File imageFile) throws Exception {
        try{
            BufferedImage img = ImageIO.read(imageFile);
            WIDTH = img.getWidth();
            HEIGHT = img.getHeight();

            pixelArray = new int[WIDTH][HEIGHT];

            for(int i = 0; i < WIDTH; i++){
                for(int j = 0; j < HEIGHT; j++){

                    // Get the RGB value of the pixel
                    int rgb = img.getRGB(i, j);

                    // Extract the grayscale value (assuming it's a single channel)
                    int grayscaleValue = (rgb >> 16) & 0xFF;

                    pixelArray[i][j] = grayscaleValue;
                }
            }
        }
        catch(Exception e){
            throw e;
        }
    }

    public void Predict(int[][] pixelArray) {
        differenceArray = new int[WIDTH][HEIGHT];
        predictedArray = new int[WIDTH][HEIGHT];
        firstColumn = new int[HEIGHT];
        firstRow = new int[WIDTH];

        // Loading all the first column data
        for(int i = 0; i < HEIGHT; i++){
            firstColumn[i] = pixelArray[i][0];
        }

        // Loading all the first row data
        for(int j = 0; j < WIDTH; j++){
            firstRow[j] = pixelArray[0][j];
        }

        // Loading all other data starting from 1, 1
        for(int i = 1; i < pixelArray.length; i++){
            for (int j = 1; j < pixelArray[0].length; j++) {
                int a = pixelArray[i - 1][j];
                int b = pixelArray[i - 1][j - 1];
                int c = pixelArray[i][j - 1];

                int prediction;

                if (b < Math.min(a, c)) {
                    prediction = Math.max(a, c);
                } else if (b > Math.max(a, c)) {
                    prediction = Math.min(a, c);
                } else {
                    prediction = a + c - b;
                }
                predictedArray[i][j] = prediction;

                int difference =  prediction - pixelArray[i][j];
                MAX = (difference > MAX) ? difference : MAX;
                MIN = (difference < MIN) ? difference : MIN;
                differenceArray[i][j] = difference;
            }
        }
    }

    public int[][] Quantize(int[][] differenceArray){

        levels = new int[quantizationLevel];
        int steps = (int) Math.ceil((double) (MAX - MIN) / quantizationLevel);

        while (steps % quantizationLevel != 0) {
            steps++;
        }

        levels[0] = MIN;
        for(int i = 1; i < quantizationLevel; i++){
            levels[i] = levels[i - 1] + steps;
        }

        for(int i = 1; i < differenceArray.length; i++){
            for(int j = 1; j < differenceArray[0].length; j++){
                int difference = differenceArray[i][j];
                int quantizedValue = 0;

                if(difference < levels[0]){
                    quantizedValue = 0;
                }
                else if(difference > levels[quantizationLevel - 1]){
                    quantizedValue = quantizationLevel;
                }
                else{
                    for(int k = 0; k < quantizationLevel - 2; k++){
                        if(difference >= levels[k] && difference <= levels[k + 1]){
                            quantizedValue = k + 1;
                            break;
                        }
                    }
                }
                differenceArray[i][j] = quantizedValue;
            }
        }
        return differenceArray;
    }

    // FOR BINARY FILE OUTPUT
    public void SaveCompressedFile(int[][] compressedImage, String name) throws Exception {
        String path = System.getProperty("user.dir") + "\\CP_" + name + ".bin";
        File compressedFile = new File(path);

        try (ObjectOutputStream dataOut = new ObjectOutputStream(new FileOutputStream(compressedFile)))
            {
            dataOut.writeInt(WIDTH);
            dataOut.writeInt(HEIGHT);

            for(int i = 0; i < quantizationLevel; i++){
                if (levels[i] > 0){
                    dataOut.write(1);
                } else {
                    dataOut.write(0);
                }
                dataOut.write(Math.abs(levels[i]));
            }

            // Loading all the first column data
            for(int i = 0; i < HEIGHT; i++){
                dataOut.write(firstColumn[i]);
            }

            // Loading all the first row data
            for(int j = 0; j < WIDTH; j++){
                dataOut.write(firstRow[j]);
            }

            for(int i = 1; i < WIDTH; i++){
                for (int j = 1; j < HEIGHT; j++) {
                    dataOut.write(compressedImage[i][j]);
                    dataOut.write(predictedArray[i][j]);
                }
            }

        } catch (Exception e) {
            throw e;
        }
    }

    public void decompress(File compressedFile) throws Exception {
        try (ObjectInputStream dataIn = new ObjectInputStream(new FileInputStream(compressedFile))) {
            WIDTH = dataIn.readInt();
            HEIGHT = dataIn.readInt();
            int[][] decompressedImage = new int[WIDTH][HEIGHT];
            int[][] predictedArray = new int[WIDTH][HEIGHT];
            int[] levels = new int[quantizationLevel];

            for(int i = 0; i < quantizationLevel; i++){
                int sign = dataIn.read();
                levels[i] = dataIn.read();
                if (sign == 0){
                    levels[i] = -levels[i];
                }

            }

            // Loading all the first column data
            for(int i = 0; i < HEIGHT; i++){
                decompressedImage[i][0] = dataIn.read();
            }

            // Loading all the first row data
            for(int j = 0; j < WIDTH; j++){
                decompressedImage[0][j] = dataIn.read();
            }

            // Read other data in array
            for(int i = 1; i < WIDTH; i++){
                for (int j = 1; j < HEIGHT; j++) {
                    decompressedImage[i][j] = dataIn.read();
                    predictedArray[i][j] = dataIn.read();

                }
            }

            // De-quantize array
            for(int i = 1; i < WIDTH; i++){
                for (int j = 1; j < HEIGHT; j++) {
                    int prediction = predictedArray[i][j];
                    int quantizedValue = decompressedImage[i][j];
                    int difference = 0;

                    if(quantizedValue < levels[0]){
                        difference = levels[0];
                    }
                    else if(quantizedValue > levels[7]){
                        difference = levels[7];
                    }
                    else{
                        for(int k = 0; k < 7; k++){
                            if(quantizedValue >= levels[k] && quantizedValue <= levels[k + 1]){
                                difference = (levels[k + 1] + levels[k]) / 2;
                                break;
                            }
                        }
                    }
                    decompressedImage[i][j] = difference + prediction;
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
        String path = System.getProperty("user.dir") + "\\DP_" + name + ".jpg";
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
            ImageIO.write(image, "jpg", ImageFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
