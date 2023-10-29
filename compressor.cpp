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
    // Implement the compression logic here
    vector<LZ77Token> compressedData;
    // ...
    return compressedData;
}

string Compressor::Decompress(const vector<LZ77Token>& compressedData) {
    // Implement the decompression logic here
    string decompressed;
    // ...
    return decompressed;
}