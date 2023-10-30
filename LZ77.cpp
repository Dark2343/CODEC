#include <iostream>
#include <string>
#include <vector>

using namespace std;

struct LZ77Token {
    int length;
    int offset;
    char nextChar;
};

vector<LZ77Token> compress(const string& input) {
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

string decompress(const vector<LZ77Token>& compressedData) {
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

int main() {
    
    string inputText;
    vector<LZ77Token> compressedData;
    string decompressedText;
    while (true){
        cout << "Welcome the LZ77 Compression Algorithm Menu : "<<endl;
        cout << "1-Compress"<<endl;
        cout << "2-Decompress"<<endl;
        cout << "3-Exit"<<endl;
        
        int choice;
        cin >> choice;
        switch (choice) {
            case 1:
                cout << "Input text:\n" << endl;
                getline(cin,inputText);
                compressedData = compress(inputText);
                
                for (const LZ77Token& token : compressedData) {
                    cout << "<" << token.offset << "," << token.length << "," << token.nextChar << ">";
                }
                break;
            case 2:
                cout << "Input text:\n" << endl;
                getline(cin,inputText);
                decompressedText = decompress(compressedData);
                cout << "Decompressed Text: " << decompressedText << endl;
                break;
            case 3:
                exit(0);
            default:
                cout << "Invalid input\n";
        }
    }
}
