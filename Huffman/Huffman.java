import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Huffman {

    Node root;
    ArrayList<Node> frequencyTable = new ArrayList<Node>();
    HashMap<Character, Integer> codedTable = new HashMap<Character, Integer>();

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
            codedTable.put(root.character.charAt(0), Integer.parseInt(code));
        }
        else{
            buildCodedTable(root.left, code + "0");
            buildCodedTable(root.right, code + "1");
        }
    }

    public void readTextFile(File textFile){
        try{
            Scanner scanner = new Scanner(textFile);
            while(scanner.hasNextLine()){
                String line = scanner.nextLine();
                writeCodedFile(line);
            }
            scanner.close();
        }
        catch(Exception e){
            System.out.println("Error: " + e);
        }
    }

    public void writeCodedFile(String line) throws IOException {
        File file = new File("CompressedData.txt");
        FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
        BufferedWriter bw = new BufferedWriter(fw);
        StringBuilder data = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            data.append(codedTable.get(line.charAt(i)));
        }

        bw.write(data.toString());
        bw.newLine();

        bw.close();
        fw.close();
    }

    public void compress(File textFile){
        buildFrequencyTable(textFile);
        buildHuffmanTree();
        buildCodedTable(root, "");
        readTextFile(textFile);
    }

    /**
     * @param root Root of the tree
     * @param compressedText Compressed text
     * @return Decompressed text
     * @author Ziad Karson
     */
    public void decompress(File textFile){
        // Node temp = root;
        // StringBuilder decompressedText = new StringBuilder();
        // for (int i = 0; i < compressedText.length(); i++) {
        //     if(compressedText.charAt(i) == '0'){
        //         temp = temp.left;
        //     }
        //     else{
        //         temp = temp.right;
        //     }
        //     if(temp.left == null && temp.right == null){
        //         decompressedText.append(temp.character);
        //         temp = root;
        //     }
        // }
        // return decompressedText.toString();
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