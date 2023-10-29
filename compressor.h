//
// Created by Yahya Ehab on 10/30/2023.
//

#ifndef COMPRESSION_ALGORITHMS_COMPRESSOR_H
#define COMPRESSION_ALGORITHMS_COMPRESSOR_H

#include <string>
#include <vector>
using namespace std;

struct LZ77Token {
    int offset;
    int length;
    char nextChar;
};

class Compressor {
public:
    Compressor(int windowSize, int lookAheadBufferSize);

    vector<LZ77Token> Compress(const string& input);
    string Decompress(const vector<LZ77Token>& compressedData);

private:
    int windowSize;
    int lookAheadBufferSize;
};


#endif //COMPRESSION_ALGORITHMS_COMPRESSOR_H
