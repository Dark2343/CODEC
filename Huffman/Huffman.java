import java.io.File;

public class Huffman {

    public void compress(File textFile){
        
    }

    /**
     * @param root Root of the tree
     * @param compressedText Compressed text
     * @return Decompressed text
     * @author Ziad Karson
     */
    public String decompress(Node root, String compressedText){
        Node temp = root;
        StringBuilder decompressedText = new StringBuilder();
        for (int i = 0; i < compressedText.length(); i++) {
            if(compressedText.charAt(i) == '0'){
                temp = temp.left;
            }
            else{
                temp = temp.right;
            }
            if(temp.left == null && temp.right == null){
                decompressedText.append(temp.character);
                temp = root;
            }
        }
        return decompressedText.toString();
    }
    
}

class Node{
    char character;
    float frequency;
    Node left;
    Node right;
    Node(char character, float frequency){
        this.character = character;
        this.frequency = frequency;
    }
    Node(char character, float frequency, Node left, Node right){
        this.character = character;
        this.frequency = frequency;
        this.left = left;
        this.right = right;
    }
}