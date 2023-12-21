package Codecs;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import src.EncodingAlgorithm;

public class LZW implements EncodingAlgorithm{
    
    @Override
    public void Compress(File textFile) throws Exception {
        try{
            ArrayList<Integer> compressedData = new ArrayList<>();
            // First read all file contents
            String content = readTextFile(textFile);
            // Then we create the 256 char dictionary
            HashMap<String, Integer> dictionary = createDictionary();
            // We initialize an empty string to store the current string of chars found in the dictionary
            String substring = "";

            // We loop over the file's content
            for(char c : content.toCharArray()){
                // We make a variable to store the current char + the string we built before
                String mix = substring + c;
                // if the dict contains the mix, then we store it as the new existing string
                if (dictionary.containsKey(mix)) {
                    substring = mix;
                }
                else{
                    // Otherwise, we store the value of that string in the compressed data
                    compressedData.add(dictionary.get(substring));
                    // And we store the mix we found in the dict along with the dict size
                    dictionary.put(mix, dictionary.size());
                    // Then we reset the string to our current char to compare it with the next char in the following iteration
                    substring = String.valueOf(c);
                }
            }

            // At the end of the loop, if the substring is not empty, that means that it contains a string found in the dict
            if (!substring.isEmpty()) {
                // So we store this string in the compressed data
                compressedData.add(dictionary.get(substring));
            }

            // Then we write all that to the file
            writeCompressedFile(compressedData, textFile.getName());
        }
        catch (Exception e) {throw e;}
    }

    @Override
    public void Decompress(File textFile) throws Exception {
        try{
            List<Integer> compressedData = readCompressedFile(textFile);
            HashMap<Integer, String> reverseDictionary = createReverseDictionary();
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
            writeDecompressedFile(decompressedText.toString());

        } catch (Exception e) {throw e;}
    }

    private HashMap<String, Integer> createDictionary(){
        HashMap<String, Integer> dictionary = new HashMap<>();
        for (int i = 0; i < 256; i++) {
            dictionary.put(String.valueOf((char)(i)), i);
        }
        return dictionary;
    }

    private HashMap<Integer, String> createReverseDictionary(){
        HashMap<Integer, String> reverseDictionary = new HashMap<>();
        for (int i = 0; i < 256; i++) {
           reverseDictionary.put(i, String.valueOf((char) i));
        }
        return reverseDictionary;
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
            ArrayList<Integer> compressedData = new ArrayList<>();
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

    private void writeDecompressedFile(String decompressedData) throws IOException{
        FileWriter output = new FileWriter("DecompressedData.txt");
        output.write(decompressedData);
        output.close();
    }
}
