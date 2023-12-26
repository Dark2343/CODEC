package src;
import java.io.File;

public interface CodecType {
    public void Compress(File file) throws Exception; 
    public void Decompress(File compressedFile) throws Exception;
}