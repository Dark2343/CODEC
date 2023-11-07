import java.io.*;
import java.util.*;

public class LZW {

    public static void compress(String inputFileName, String outputFileName) throws IOException {
        //implement this function aboos edek yasta
    }

    public static void decompress(String inputFileName, String outputFileName) throws IOException {
        List<Integer> compressedData = readCompressedData(inputFileName);
        Map<Integer, String> dictionary = createReverseDictionary();
        StringBuilder decompressedText = new StringBuilder();
        int initialCode = compressedData.remove(0);
        String current = dictionary.get(initialCode);
        decompressedText.append(current);

        for (int code : compressedData) {
            String entry;
            if (dictionary.containsKey(code)) {
                entry = dictionary.get(code);
            } else if (code == dictionary.size()) {
                entry = current + current.charAt(0);
            } else {
                throw new IllegalArgumentException("Invalid compressed data");
            }
            decompressedText.append(entry);
            dictionary.put(dictionary.size(), current + entry.charAt(0));
            current = entry;
        }
        writeText(outputFileName, decompressedText.toString());
    }

    private static String readFile(String fileName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        StringBuilder text = new StringBuilder();
        int c;
        while ((c = br.read()) != -1) {
            text.append((char) c);
        }
        br.close();
        return text.toString();
    }

    private static void writeText(String fileName, String text) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
        bw.write(text);
        bw.close();
    }

    private static void writeCompressedData(String fileName, List<Integer> compressedData) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        for (Integer code : compressedData) {
            writer.write(code.toString()); // Write the code as a string
            writer.write(" "); // Separate codes with spaces
        }
        writer.close();
    }

    public static List<Integer> readCompressedData(String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        List<Integer> compressedData = new ArrayList<>();
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
    }
    private static Map<String, Integer> createDictionary() {
        Map<String, Integer> dictionary = new HashMap<>();
        for (int i = 0; i < 256; i++) {
            dictionary.put(String.valueOf((char) i), i);
        }
        return dictionary;
    }

    private static Map<Integer, String> createReverseDictionary() {
        Map<Integer, String> dictionary = new HashMap<>();
        for (int i = 0; i < 256; i++) {
            dictionary.put(i, String.valueOf((char) i));
        }
        return dictionary;
    }

    public static void main(String[] args) {
        try {
            String inputFileName = "input.txt";
            String compressedFileName = "compressed.txt";
            String decompressedFileName = "decompressed.txt";

            // Compression
            compress(inputFileName, compressedFileName);
            System.out.println("Compression completed.");

            // Decompression
            decompress(compressedFileName, decompressedFileName);
            System.out.println("Decompression completed.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
