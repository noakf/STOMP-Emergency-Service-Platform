#pragma once
#include <string>
#include <vector>
#include <unordered_map>
#include <memory> 
#include "ConnectionHandler.h" 
#include "event.h"
#include <mutex>
using namespace std;
class StompProtocol {
public:
    StompProtocol();
    vector<string> generteFrame(vector<string> args, string userName);
    void process(shared_ptr<ConnectionHandler> &connectionHandler, string serverResponse); 
    bool containsWord(const std::string &text, const std::string &word);
    void generateSummary(const std::string& user, const std::string& channel, const std::string& file);
    void processMessage(const std::string& serverResponse);
    std::string convertTimestampToDateTime(int timestamp);
    bool getShouldTerminate();
    bool getIsLoggedIn();
    void setShouldTerminate(bool terminate);
    bool getIsError();
    void deleteFromUserMap(string userName);

private: 
int subscriptionId = 0;
int receiptId = 0;
int logout = 0;
bool shouldTerminate = false;
bool isError = false;
bool isLoggedIn = false;
unordered_map<int, string> subscribeReceiptIdMap;
unordered_map<int, string> unsubscribeReceiptIdMap;
unordered_map<string,unordered_map<string, int>> userMap;
unordered_map<string, unordered_map<string, vector<Event>>> usersReportMap;
string userNameProt;
mutex mtx;



};