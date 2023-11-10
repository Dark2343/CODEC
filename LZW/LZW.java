import java.io.BufferedReader;
import java.io.FileReader;
import java.io.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class LZW{
    HashMap<String, Integer> dictionary = new HashMap<>();
    HashMap<Integer, String> reverseDictionary = new HashMap<>();
    ArrayList<Integer> compressedData = new ArrayList<>();

    public void compress(File textFile) throws Exception {
        try{
            String content = readTextFile(textFile);
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
            writeCompressedFile(compressedData, textFile.getName());
        }
        catch (Exception e) {throw e;}
    }

    public void decompress(File textFile) throws Exception {
        try{
            List<Integer> compressedData = readCompressedFile(textFile);
            createReverseDictionary();
            StringBuilder decompressedText = new StringBuilder();
            int initialCode = compressedData.remove(0);
            String current = reverseDictionary.get(initialCode);
            decompressedText.append(current);
            for (int code : compressedData) {
                String entry;
                if (reverseDictionary.containsKey(code)) {
                    entry = reverseDictionary.get(code);
                } else if (code == reverseDictionary.size()) {
                    entry = current + current.charAt(0);
                } else {
                    throw new IllegalArgumentException("Invalid compressed data");
                }
                decompressedText.append(entry);
                reverseDictionary.put(reverseDictionary.size(), current + entry.charAt(0));
                current = entry;
            }
            writeDecompressedFile(decompressedText.toString() , textFile.getName());

        } catch (Exception e) {throw e;}
    }

    private void createDictionary(){
        for (int i = 0; i < 256; i++) {
            Character c = (char)(i);
            dictionary.put(Character.toString(c), i);
        }
    }

    private void createReverseDictionary(){
        for (int i = 0; i < 256; i++) {
           reverseDictionary.put(i, String.valueOf((char) i));
        }
    }

    private String readTextFile(File textFile) throws Exception {
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

    private void writeCompressedFile(ArrayList<Integer> compressedData, String name) throws IOException{
        FileWriter output = new FileWriter("Compressed" + name);
        for (int data : compressedData) {
            output.write(data + " ");
        }
        output.close();
    }
    private  List<Integer> readCompressedFile(File textFile) throws Exception {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(textFile));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] codes = line.split(" ");
                for (String code : codes) {
                    if (!code.isEmpty()) {
                        compressedData.add(Integer.parseInt(code));
                    }
                }
            }
            reader.close();
            return compressedData;
        } catch (Exception e) {throw e;}

    }


    private void writeDecompressedFile(String decompressedData, String name) throws IOException{
        FileWriter output = new FileWriter("Decompressed" + name);
        output.write(decompressedData);
        output.close();
    }
}
