import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class LZW{
    HashMap<String, Integer> dictionary = new HashMap<>();
    HashMap<Integer, String> reverseDictionary = new HashMap<>();
    ArrayList<Integer> compressedData = new ArrayList<>();

    public void compress(File textFile) throws Exception {
        try{
            String content = readFile(textFile);
            createDictionary();
            StringBuilder substring = new StringBuilder();

            for(char c : content.toCharArray()){
                if (dictionary.containsKey(substring.toString())) {
                    substring.append(c);
                }
                else{
                    dictionary.put(substring.toString(), dictionary.size());
                    substring.deleteCharAt(substring.length() - 1);
                    compressedData.add(dictionary.get(substring.toString()));
                    substring.delete(0, substring.length() - 1);
                    substring.append(c);
                }
            }
            writeFile(compressedData, textFile.getName());
        }
        catch (Exception e) {throw e;}
    }

    public void decompress(File textFile) throws Exception {
        try{
            readFile(textFile);
            createReverseDictionary();
        }
        catch (Exception e) {throw e;}
    }
    
    private void createDictionary(){
        for (int i = 0; i < 256; i++) {
            Character c = (char)(i);
            dictionary.put(Character.toString(c), i);
        }
    }

    private void createReverseDictionary(){
        for (int i = 0; i < 256; i++) {
            Character c = (char)(i);
            reverseDictionary.put(i, Character.toString(c));
        }
    }
    
    private String readFile(File textFile) throws Exception {
        try {
            String content = new String();
            Scanner reader = new Scanner(textFile);
            while (reader.hasNextLine()) {
                content += reader.nextLine();
            }
            reader.close();
            return content;
        } catch (Exception e) {throw e;}
    }

    private void writeFile(ArrayList<Integer> compressedData, String name) throws IOException{
        FileWriter output = new FileWriter("Compressed" + name);
        for (int data : compressedData) {
            output.write(data + " ");
        }
        output.close();
    }
}
