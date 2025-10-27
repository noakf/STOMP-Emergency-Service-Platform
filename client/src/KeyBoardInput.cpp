#include "../include/KeyBoardInput.h"
#include <vector>
#include <string>


void split_str(const string& line, char sign, vector<string>& lineArgs)
{
    lineArgs.clear(); 
    std::string args = "";
    for (char c : line) {
        if (c == sign) {
            lineArgs.push_back(args); 
            args.clear();        
        } else {
            args += c;               
        }
    }
    if (!args.empty()) {
        lineArgs.push_back(args); 
    }
}