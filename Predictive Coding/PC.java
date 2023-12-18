import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class PC {
    
    int WIDTH, HEIGHT, MAX = Integer.MIN_VALUE, MIN = Integer.MAX_VALUE;;
    int[][] pixelArray, differenceArray;
    int[] firstRow, firstColumn, levels;

    
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
    
    public void Predict(int[][] pixelArray){

        differenceArray = new int[WIDTH][HEIGHT];

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
                int difference, prediction;
                
                prediction = (pixelArray[i][j - 1] + pixelArray[i - 1][j]) / 2;
                difference = pixelArray[i][j] - prediction;
                MAX = (difference > MAX) ? difference : MAX;
                MIN = (difference < MIN) ? difference : MIN;
                differenceArray[i][j] = difference;
            }
        }
    }
    
    public int[][] Quantize(int[][] differenceArray){
        
        levels = new int[9];
        int steps = (int) Math.round((double) (MAX - MIN) / 8.0);
        
        levels[0] = MIN;
        for(int i = 1; i < 9; i++){
            levels[i] = levels[i - 1] + steps;
        }
        
        for(int i = 1; i < differenceArray.length; i++){
            for(int j = 1; j < differenceArray[0].length; j++){
                int difference = differenceArray[i][j];
                int quantizedValue = 0;
                
                if(difference < levels[0]){
                    quantizedValue = levels[0];
                }
                else if(difference > levels[7]){
                    quantizedValue = levels[7];
                }
                else{
                    for(int k = 0; k < 7; k++){
                        if(difference >= levels[k] && difference <= levels[k + 1]){
                            quantizedValue = levels[k + 1];
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
        
        try (DataOutputStream dataOut = new DataOutputStream(new FileOutputStream(compressedFile))) {
            dataOut.writeShort(WIDTH);
            dataOut.writeShort(HEIGHT);
            
            for(int i = 0; i < 9; i++){
                dataOut.writeShort(levels[i]);
            }
            
            // Loading all the first column data
            for(int i = 0; i < HEIGHT; i++){
                dataOut.writeShort(firstColumn[i]);
            }
            
            // Loading all the first row data
            for(int j = 0; j < WIDTH; j++){
                dataOut.writeShort(firstRow[j]);
            }
            
            for(int i = 1; i < WIDTH; i++){
                for (int j = 1; j < HEIGHT; j++) {                    
                    dataOut.writeShort(compressedImage[i][j]);
                }
            }
            
        } catch (Exception e) {
            throw e;
        }
    }
    
    public void decompress(File compressedFile) throws Exception {
        try (DataInputStream dataIn = new DataInputStream(new FileInputStream(compressedFile))) {
            WIDTH = dataIn.readShort();
            HEIGHT = dataIn.readShort();
            int[][] decompressedImage = new int[WIDTH][HEIGHT];
            int[] levels = new int[9];
            
            for(int i = 0; i < 9; i++){
                levels[i] = dataIn.readShort();
            }
            
            // Loading all the first column data
            for(int i = 0; i < HEIGHT; i++){
                decompressedImage[i][0] = dataIn.readShort(); 
            }
            
            // Loading all the first row data
            for(int j = 0; j < WIDTH; j++){
                decompressedImage[0][j] = dataIn.readShort(); 
            }
            
            // Read other data in array
            for(int i = 1; i < WIDTH; i++){
                for (int j = 1; j < HEIGHT; j++) {                    
                    decompressedImage[i][j] = dataIn.readShort();
                }
            }
            
            // De-quantize array
            for(int i = 1; i < WIDTH; i++){
                for (int j = 1; j < HEIGHT; j++) {                    
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
                    decompressedImage[i][j] = difference;
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