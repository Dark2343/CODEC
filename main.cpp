//
// Created by Yahya Ehab on 10/30/2023.
//
#include <iostream>
#include <fstream>
#include "Compressor.h"
using namespace std;

int main() {
    const int windowSize = 256; // Adjust as needed
    const int lookAheadBufferSize = 16; // Adjust as needed

    Compressor compressor(windowSize, lookAheadBufferSize);

    // Reading input from a file
    ifstream inputFile("input.txt");
    if (!inputFile) {
        cerr << "Error: Cannot open input file." << endl;
        return 1;
    }

    std::string inputText((istreambuf_iterator<char>(inputFile)), istreambuf_iterator<char>());
    inputFile.close();

    // Compression
    vector<LZ77Token> compressedData = compressor.Compress(inputText);

    // Writing compressed data to an output file
    std::ofstream compressedFile("compressed.lz77");
    for (const LZ77Token& token : compressedData) {
        compressedFile << token.offset << " " << token.length << " " << token.nextChar << " ";
    }
    compressedFile.close();

    // Reading compressed data from the file
    ifstream compressedInput("compressed.lz77");
    string compressedText((istreambuf_iterator<char>(compressedInput)), istreambuf_iterator<char>());
    compressedInput.close();

    // Decompression
    vector<LZ77Token> compressedTokens;
    // Parse the compressed data from the file into the compressedTokens vector

    string decompressedText = compressor.Decompress(compressedTokens);

    // Writing decompressed data to an output file
    ofstream outputDecompressedFile("output.txt");
    outputDecompressedFile << decompressedText;
    outputDecompressedFile.close();

    return 0;
}