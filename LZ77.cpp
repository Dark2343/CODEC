#include <iostream>
#include <string>
#include <vector>
#include <cstring>
#include <fstream>

using namespace std;

struct Token {
    int length;
    int offset;
    char nextChar;
};

vector<Token> compress(const string& input) {
    vector<Token> compressedData;
    int inputSize = input.size();
    int currentIndex = 0;

    while (currentIndex < inputSize) {
        Token token;
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

string decompress(const vector<Token>& compressedData) {
    string decompressed;
    for (const Token& token : compressedData) {
        if (token.length == 0) {
            decompressed += token.nextChar;
        } else {
            int startIndex = decompressed.size() - token.offset;
            for (int i = 0; i < token.length; i++) {
                decompressed += decompressed[startIndex + i];
            }
            decompressed += token.nextChar;
        }
    }
    return decompressed;
}

int main() {
    
    fstream file;
    string input ,inputText, decompressedText;
    char fileName[100];
    vector<Token> compressedData;

    while (true){
        cout << "Welcome the LZ77 Menu:\n";
        cout << "1- Compress Input\n";
        cout << "2- Decompress Input\n";
        cout << "3- Compress File\n";
        cout << "4- Decompress File\n";
        cout << "5- Exit\n";
        
        int choice;
        cin >> choice;
        cin.ignore();
        switch (choice) {
            case 1:
                cout << "Input text:" << endl;
                getline(cin, inputText);
                compressedData = compress(inputText);
                
                for (const Token& token : compressedData) {
                    cout << "<" << token.offset << "," << token.length << "," << token.nextChar << ">";
                }
                cout << endl;
                break;
            case 2:
                decompressedText = decompress(compressedData);
                cout << "Decompressed Text: " << decompressedText << endl;
                break;
            case 3:                
                cout << "Enter File Name: \n";
                cin.getline(fileName, 100);
                strcat(fileName, ".txt");
                file.open(fileName, ios::in);

                if(file.is_open()){
                    input.assign((istreambuf_iterator<char>(file)),(istreambuf_iterator<char>()));
                }
                
                file.close();
                compressedData = compress(input);

                file.open(fileName,ios::out); // write

                if(file.is_open()){
                    string output;
                    for (int i = 0; i < compressedData.size(); i++)
                    {
                        output = "<" + to_string(compressedData[i].offset) + "," + to_string(compressedData[i].length) + ">" + compressedData[i].nextChar;
                        file << output;
                    }
                    file.close();
                }

                break;
            case 4:
                cout << "Enter File Name: \n";

                strcat(fileName, "txt");
                cin.getline(fileName, 100);
                file.open(fileName, ios::in);

                if(file.is_open()){
                    input.assign((istreambuf_iterator<char>(file)),(istreambuf_iterator<char>()));
                }
                
                decompressedText = decompress(compressedData);

                file.open(fileName,ios::out); // write

                if(file.is_open()){
                    file << decompressedText;
                    file.close();
                }
                break;
                
            case 5:
                exit(0);
            default:
                cout << "Invalid input\n";
        }
        system("pause");
    }
}
