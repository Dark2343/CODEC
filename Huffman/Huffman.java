import javax.xml.stream.events.EndDocument;
import java.io.*;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Scanner;

public class Huffman {
    BitReader bitReader;
    Node root;
    ArrayList<Node> frequencyTable = new ArrayList<Node>();
    HashMap<Character, String> codedTable = new HashMap<>();
    HashMap<String, Character> invertedCodedTable = new HashMap<>();

    public void buildFrequencyTable(File textFile){
        try{
            Scanner scanner = new Scanner(textFile);
            while(scanner.hasNextLine()){
                String line = scanner.nextLine();
                
                for(int i = 0; i < line.length(); i++){
                    String character = Character.toString(line.charAt(i));
                    
                    if(frequencyTable.isEmpty()){
                        frequencyTable.add(new Node(character, 1));
                    }
                    else{
                        boolean found = false;
                        for(int j = 0; j < frequencyTable.size(); j++){
                            if(frequencyTable.get(j).character.equals(character)){
                                frequencyTable.get(j).frequency++;
                                found = true;
                                break;
                            }
                        }
                        if(!found){
                            frequencyTable.add(new Node(character, 1));
                        }
                    }
                }
            }
            scanner.close();
        }
        catch(Exception e){
            System.out.println("Error: " + e);
        }

        frequencyTable.sort((Node n1, Node n2) -> n1.frequency - n2.frequency);
    }

    public void buildHuffmanTree(){
        while (frequencyTable.size() != 1) {
            Node n1 = frequencyTable.get(0);
            Node n2 = frequencyTable.get(1);

            Node sumNode = new Node(n1.character + " " + n2.character, n1.frequency + n2.frequency, n1, n2);

            frequencyTable.remove(1);
            frequencyTable.remove(0);
            frequencyTable.add(sumNode);
            frequencyTable.sort((Node nA, Node nB) -> nA.frequency - nB.frequency);
        }

        root = frequencyTable.get(0);
    }

    public void buildCodedTable(Node root, String code){
        if(root.left == null && root.right == null){
            codedTable.put(root.character.charAt(0), code);
        }
        else{
            buildCodedTable(root.left, code + "0");
            buildCodedTable(root.right, code + "1");
        }
    }

    public void readTextFile(File textFile){
        try{
            Scanner scanner = new Scanner(textFile);
            StringBuilder data = new StringBuilder();
            while(scanner.hasNextLine()){
                data.append(scanner.nextLine());
            }
            writeCodedFile(data.toString());
            scanner.close();
        }
        catch(Exception e){
            System.out.println("Error: " + e);
        }
    }

    public void writeCodedFile(String text) throws IOException {
        // TODO: Add tree to beginning of file
        File file = new File("CompressedData.txt");
        FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
        BufferedWriter bw = new BufferedWriter(fw);
        StringBuilder data = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            data.append(codedTable.get(text.charAt(i)));
        }

        bw.write(data.toString());

        bw.close();
        fw.close();
    }

    public void compress(File textFile){
        buildFrequencyTable(textFile);
        buildHuffmanTree();
        buildCodedTable(root, "");
        readTextFile(textFile);
    }


    public void decompress(File textFile){
        try {
            BitReader bitReader = new BitReader(new FileInputStream(textFile));
        } catch (FileNotFoundException e) {
            System.out.println("Error: " + e);
        }
        readCodedFile(textFile);
        String textString = decompressCode();
        writeTextFile(textString);
        
    }

    private void writeTextFile(String textString) {
        try{
            File file = new File("DecompressedData.txt");
            FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(textString);
            bw.close();
            fw.close();
        }
        catch(Exception e){
            System.out.println("Error: " + e);
        }
    }

    private String decompressCode() {
        StringBuilder textString = new StringBuilder();
        StringBuilder code = new StringBuilder();
        while(bitReader.hasNextBit()){
            code.append(bitReader.readBit());
            if(invertedCodedTable.containsKey(code.toString())){
                textString.append(invertedCodedTable.get(code.toString()));
                code = new StringBuilder();
            }
        }
        return textString.toString();
    }

    private void readCodedFile(File codedFile) {
        // Builds tree
        rebuildHuffmanTree(codedFile);
        // then codedTable
        buildCodedTable(root, "");
        this.invertedCodedTable = invertCodedTable();
    }

    private HashMap<String, Character> invertCodedTable() {
        HashMap<String, Character> invertedTable = new HashMap<>();
        for (Character key : codedTable.keySet()) {
            invertedTable.put(codedTable.get(key), key);
        }
        return invertedTable;
    }

    private void rebuildHuffmanTree(File codedFile) {
        this.root = readNode();
    }

    private Node readNode() {
        // If byte is 1 it means it's a leaf node
        if(bitReader.readBit()){

            String character = String.valueOf((char) bitReader.readByte());

            return new Node(character, 1);
        }
        else{

            Node left = readNode();
            Node right = readNode();
            return new Node("", 0, left, right);
        }
    }
}

class Node{
    String character;
    int frequency;
    Node left;
    Node right;

    public Node(String character, int frequency){
        this.character = character;
        this.frequency = frequency;
    }

    public Node(String character, int frequency, Node left, Node right){
        this.character = character;
        this.frequency = frequency;
        this.left = left;
        this.right = right;
    }
}

class BitReader{
    InputStream inputStream;
    BitSet bitSet = new BitSet();
    int index = 0;
    int size = 0;

    public BitReader(InputStream inputStream){
        this.inputStream = inputStream;
    }

    public boolean readBit() throws IndexOutOfBoundsException {
        if(index == size){
            try{
                // Read next byte
                int data = inputStream.read();
                // If end of file, return 0
                if(data == -1){
                    throw new IndexOutOfBoundsException("End of file in BitReader");
                }
                bitSet = BitSet.valueOf(new byte[]{(byte)data});
                size = bitSet.length();
                index = 0;
            }
            catch(Exception e){
                System.out.println("Error: " + e);
            }
        }
        return bitSet.get(index++);
    }

    public boolean hasNextBit(){
        return index < size;
    }

    public byte readByte(){
        BitSet bits = new BitSet();
        for(int i = 0; i < 8; i++){
            bits.set(i, bitSet.get(index));
            index++;
        }
        return bits.toByteArray()[0];
    }
}