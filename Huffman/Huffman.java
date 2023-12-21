import java.io.*;
import java.util.*;

public class Huffman {
    Node root;
    ArrayList<Node> frequencyTable = new ArrayList<>();
    HashMap<Character, String> codedTable = new HashMap<>();
    HashMap<String, Character> invertedCodedTable = new HashMap<>();

    public void BuildFrequencyTable(File textFile){
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
                        for (Node node : frequencyTable) {
                            if (node.character.equals(character)) {
                                node.frequency++;
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

    public void BuildHuffmanTree(){
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

    public void BuildCodedTable(Node root, String code){
        if(root.left == null && root.right == null){
            codedTable.put(root.character.charAt(0), code);
        }
        else{
            BuildCodedTable(root.left, code + "0");
            BuildCodedTable(root.right, code + "1");
        }
    }

    public void ReadToWriteText(File textFile){
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
        writeHuffmanTree();
        try(DataOutputStream dataOut = new DataOutputStream(new FileOutputStream("CompressedData.bin", true))){
                StringBuilder data = new StringBuilder();
                for (int i = 0; i < text.length(); i++) {
                data.append(codedTable.get(text.charAt(i)));
            }
            String dataStr = data.toString();
            dataOut.writeShort(dataStr.length());

            for (int i = 0; i < dataStr.length(); i+=8) {
                String byteStr = dataStr.substring(i, Math.min(i + 8, dataStr.length()));

                while (byteStr.length() < 8) {
                    byteStr += "0";
                }
                int dataByte = Integer.parseInt(byteStr, 2);
                dataOut.write(dataByte);
            }
        }
    }
    private void writeHuffmanTree() {
        try(DataOutputStream dataOut = new DataOutputStream(new FileOutputStream("CompressedData.bin"))){
            int tableLength = codedTable.size();
            dataOut.writeByte(tableLength);
            for (Character key : codedTable.keySet()) {
                dataOut.writeByte(key);
                int codeSize = codedTable.get(key).length();
                dataOut.writeByte(codeSize);
                String code = codedTable.get(key);
                while (code.length() < 8) {
                    code += "0";
                }
                int data = Integer.parseInt(code, 2);
                dataOut.write(data);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }

    public void compress(File textFile){
        BuildFrequencyTable(textFile);
        BuildHuffmanTree();
        BuildCodedTable(root, "");
        ReadToWriteText(textFile);
    }


    public void decompress(File textFile) {
        try(DataInputStream dataIn = new DataInputStream(new FileInputStream(textFile))){
            int tableLength = dataIn.readByte();
            for (int i = 0; i < tableLength; i++) {
                char key = (char) dataIn.read();
                int codeLength = dataIn.read();
                int data = dataIn.read();
                String dataBinStr = Integer.toBinaryString(data);
                while (dataBinStr.length() < 8) {
                    dataBinStr = "0" + dataBinStr;
                }
                String dataStr = dataBinStr.substring(0, codeLength);
                invertedCodedTable.put(dataStr, key);
            }
            int codedLength = dataIn.readShort();
            int codedLengthBytes = (int) Math.ceil((double)codedLength/8);
            StringBuilder codedData = new StringBuilder();
            for (int i = 0; i < codedLengthBytes; i++) {
                int data = dataIn.read();
                String dataStr = Integer.toBinaryString(data);
                while (dataStr.length() < 8) {
                    dataStr = "0" + dataStr;
                }
                codedData.append(dataStr);
            }
            String codedDataStr = codedData.toString();
            StringBuilder decodedData = new StringBuilder();
            StringBuilder temp = new StringBuilder();
            for (int i = 0; i < codedLength; i++) {
                temp.append(codedDataStr.charAt(i));
                if(invertedCodedTable.containsKey(temp.toString())){
                    decodedData.append(invertedCodedTable.get(temp.toString()));
                    temp = new StringBuilder();
                }
            }
            FileWriter writer = new FileWriter("DecompressedData.txt");

            writer.write(decodedData.toString());
            writer.close();
        }catch (Exception e) {
            System.out.println("Error: " + e);
            e.printStackTrace();
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