//
// Created by Yahya Ehab on 10/30/2023.
//

#include "compressor.h"
#include <string>
#include <vector>
using namespace std;

Compressor::Compressor(int windowSize, int lookAheadBufferSize)
    : windowSize(windowSize), lookAheadBufferSize(lookAheadBufferSize) {
}

vector<LZ77Token> Compressor::Compress(const string& input) {
    vector<LZ77Token> compressedData;
    int inputSize = input.size();
    int currentIndex = 0;

    while (currentIndex < inputSize) {
        LZ77Token token;
        token.length = 0;
        token.offset = 0;
        token.nextChar = input[currentIndex];

        for (int searchIndex = currentIndex - 1; searchIndex >= 0; searchIndex--) {
            int matchLength = 0;
            while (currentIndex + matchLength < inputSize && input[searchIndex + matchLength] == input[currentIndex + matchLength]) {
                matchLength++;
            }

            if (matchLength > token.length) {
                token.length = matchLength;
                token.offset = currentIndex - searchIndex;
                token.nextChar = input[currentIndex + matchLength];
            }
        }

        compressedData.push_back(token);
        currentIndex += token.length + 1;
    }

    return compressedData;
}

string Compressor::Decompress(const vector<LZ77Token>& compressedData) {
    // Implement the decompression logic here
    string decompressedData;
    for (const LZ77Token& token : compressedData){
        if (token.length == 0){
            decompressedData += token.nextChar;
        }
        decompressedData.substr(decompressedData.size()-token.offset,decompressedData.size()-token.offset+token.length);
        decompressedData += token.nextChar;
    }
    return decompressedData;
}