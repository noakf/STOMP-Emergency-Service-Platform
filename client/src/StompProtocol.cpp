#include "../include/StompProtocol.h" 
#include "../include/event.h" 
#include "StompProtocol.h"
#include <sstream>
#include <fstream>
#include <algorithm>
#include <iostream>
#include <sstream>
#include <iostream>
#include <chrono>
#include <iostream>
#include <ctime>
#include <iomanip>
using namespace std;

StompProtocol::StompProtocol(): subscriptionId(0), receiptId(0), logout(0), shouldTerminate(false), isError(false), subscribeReceiptIdMap(), unsubscribeReceiptIdMap() ,userMap(), usersReportMap(), userNameProt(""), mtx() {}

vector<string> StompProtocol::generteFrame(vector<string> args, string userName){
    userNameProt = userName;
    vector<string> frames;
    string frame = "";
    if (!args.empty() && args.at(0) == "join"){
        receiptId++;
        subscriptionId++;
        subscribeReceiptIdMap.insert({receiptId, args.at(1)});
        userMap[userName].insert({args.at(1), subscriptionId});
        frame = "SUBSCRIBE\ndestination:" + args.at(1) + "\nid:" + to_string(subscriptionId) + "\n" +"receipt:" + to_string(receiptId) + "\n" +"\n\0";
        frames.push_back(frame);
    }
    else if (!args.empty() && args.at(0) == "exit"){
        receiptId++;
        unsubscribeReceiptIdMap.insert({receiptId, args.at(1)});
        int id = userMap[userName][args.at(1)];
        userMap[userName].erase(args.at(1));
        frame = "UNSUBSCRIBE\nid:" + to_string(id) + "\n" +"receipt:" + to_string(receiptId) + "\n" + "\n\0";
        frames.push_back(frame);
    }
    else if (!args.empty() && args.at(0) == "logout")
    {
        receiptId++;
        string frame = "DISCONNECT\nreceipt:" + to_string(receiptId) + "\n\n\0";
        logout= receiptId;
        frames.push_back(frame);
    }
    else if (!args.empty() && args.at(0) == "report")
    {
        names_and_events eventsAndNames = parseEventsFile(args.at(1));
        vector<Event> newEvents = eventsAndNames.events;
        for(Event &event : newEvents)
        {
            event.setEventOwnerUser(userName);
        }
        string channel = eventsAndNames.channel_name;
        map<string, vector<Event>> eventsMap;
        if ( usersReportMap.find(userName) == usersReportMap.end())
        {
            usersReportMap[userName] = unordered_map<string, vector<Event>>();
        }
        sort(newEvents.begin(), newEvents.end(), [](const Event &firstEvent, const Event &secondEvent)
             { return firstEvent.get_date_time() < secondEvent.get_date_time(); });
        usersReportMap[userName][channel] = newEvents;
        for (const auto &currentEvent : newEvents)
        {
            stringstream frame;
            frame << "SEND\n"
                  << "destination:/" << currentEvent.get_channel_name() << "\n\n"
                  << "user:" << userName << "\n"
                  << "channel name:/" << currentEvent.get_channel_name() << "\n"
                  << "city:" << currentEvent.get_city() << "\n"
                  << "event name:" << currentEvent.get_name() << "\n"
                  << "date time:" << currentEvent.get_date_time() << "\n"
                  << "general information:\n";

            for (const auto &information : currentEvent.get_general_information())
            {
                frame << "\t" << information.first << ":" << information.second << "\n";
            }

            frame << "description:\n"
                  << currentEvent.get_description() << "\n\n\0";
            frames.push_back(frame.str());
        }
    }
     else if (!args.empty() && args.at(0) == "summary") {
        string channel = args.at(1);
        string user = args.at(2);
        string file = args.at(3);
        generateSummary(user, channel, file);
    }
    else {
        std::cout << "[Error] Invalid command.\n";
    }
    
    return frames;
}
void StompProtocol::process(shared_ptr<ConnectionHandler> &connectionHandler, string serverResponse)
{
    {
            // Parse server response
            std::istringstream responseStream(serverResponse);
            std::string line;
            std::map<std::string, std::string> headers;

            // Read the first line to determine the STOMP frame type
            if (std::getline(responseStream, line))
            {
                std::string command = line;
                if(command == "CONNECTED")
                {
                    shouldTerminate = false;
                    std::cout << "Login successful" << std::endl;
                    isLoggedIn = true;
                }
                if(command == "MESSAGE")
                {
                    processMessage(serverResponse);
                }
                // Only proceed if the frame type is RECEIPT
                if (command == "RECEIPT")
                {
                    // Parse the headers
                    while (std::getline(responseStream, line) && !line.empty())
                    {
                        size_t delimiterPos = line.find(':');
                        if (delimiterPos != std::string::npos)
                        {
                            std::string key = line.substr(0, delimiterPos);
                            std::string value = line.substr(delimiterPos + 1);
                            headers[key] = value;
                        }
                    }

                    // Check for the receipt-id header
                    auto receiptIdIt = headers.find("receipt-id");
                    if (receiptIdIt != headers.end())
                    {
                        int receiptId = stoi(receiptIdIt->second);

                        if (receiptId == logout)
                        {
                            std::cout << "Logout confirmed. Terminating connection." << std::endl;
                            setShouldTerminate(true); 
                        }
                        else if (subscribeReceiptIdMap.find(receiptId) != subscribeReceiptIdMap.end())
                        {
                                std::string channel = subscribeReceiptIdMap[receiptId];
                                std::cout << "Joined channel " << channel  << std::endl;
                            
                        }
                        else if(unsubscribeReceiptIdMap.find(receiptId) != unsubscribeReceiptIdMap.end())
                        {
                            std::string channel = unsubscribeReceiptIdMap[receiptId];
                            std::cout << "Exited channel " << channel << std::endl;
                        }
                    
                        else
                        {
                            std::cout << "Received receipt-id: " << receiptId << ", but it does not match logoutId: " << logout << std::endl;
                        }
                    }
                    else
                    {
                        std::cerr << "RECEIPT frame received but no receipt-id header found." << std::endl;
                    }
                }
                else if (command == "ERROR")
                
                {
                    if(containsWord(serverResponse, "message:User already logged in"))
                    {
                        cout<<"User already logged in" <<endl;
                    }
                    else if(containsWord(serverResponse, "password"))
                    {
                        cout<<"Wrong password" <<endl;
                    }
                    else
                    {
                        std::cerr << "Error: " << serverResponse << std::endl;
                    }
                    isError = true;
                    shouldTerminate = true;
                }
            }
            else
            {
                std::cerr << "Failed to read server response or empty response received." << std::endl;
            }
        }
      }
// }
bool StompProtocol::containsWord(const std::string &text, const std::string &word) {
    return text.find(word) != std::string::npos;  // Returns true if found
}

void StompProtocol::generateSummary(const std::string& user, const std::string& channel, const std::string& file) {
    string thisChannel = "/"+channel;
    if (usersReportMap.find(user) == usersReportMap.end()) { 
        std::cerr << "[Error] No received events found for user: " << user << std::endl;
        return;
        }   
        if(usersReportMap[user].find(thisChannel) == usersReportMap[user].end()) {
        std::cerr << "[Error] No received events found for user: " << user << " in channel: " << channel << std::endl;
        return;
    }

    std::vector<Event> events = usersReportMap[user][thisChannel];

    std::ofstream outputFile(file);
    if (!outputFile) {
        std::cerr << "[Error] Could not open file: " << file << " for writing.\n";
        return;
    }

    int totalReports = events.size();
    int activeCount = 0;
    int forcesArrivalCount = 0;

    for (const auto& event : events) {
        if (event.isActive()) {
            activeCount++;
        }
        if (event.isForcesArrival()) {
            forcesArrivalCount++;
        }
    }
    
    outputFile << "Channel " << channel << "\n";
    outputFile << "Stats:\n";
    outputFile << "Total: " << totalReports << "\n";
    outputFile << "active: " << activeCount << "\n";
    outputFile << "forces arrival at scene: " << forcesArrivalCount << "\n";
    outputFile << "Event Reports:\n";

    for (const auto& event : events) {
        std::string summary = event.get_description().substr(0, 27) + (event.get_description().size() > 27 ? "..." : "");
        string dateTime = convertTimestampToDateTime(event.get_date_time());

        outputFile << "Report:\n";
        outputFile << "city: " << event.get_city() << "\n";
        outputFile << "date time: " << dateTime << "\n";
        outputFile << "event name: " << event.get_name() << "\n";
        outputFile << "summary: " << summary << "\n\n";
    }

    outputFile.close();
    std::cout << "[Summary] Successfully written summary to " << file << std::endl;
}


void StompProtocol::processMessage (const std::string& serverResponse){
   mtx.lock();
    Event newEvent = Event(serverResponse) ;
    string channel = newEvent.get_channel_name();
    string user = newEvent.getEventOwnerUser();
       if(userNameProt == user){
        cout<<"reported"<<endl;
        userNameProt = "";
       }
     if (usersReportMap.find(user) == usersReportMap.end())
            {
                usersReportMap[user].emplace(channel, std::vector<Event>{newEvent});
                mtx.unlock();
            }
    else if(usersReportMap.find(user) != usersReportMap.end()){
          if (usersReportMap[user].find(channel) == usersReportMap[user].end())
            {
                 vector<Event> events;
                events.push_back(newEvent);
                usersReportMap[user][channel] = events;
                mtx.unlock();

            } 
            else
            {
                usersReportMap[user][channel].push_back(newEvent);
                mtx.unlock();

            }   
    }    
}
std::string StompProtocol::convertTimestampToDateTime(int timestamp) {
    std::time_t time = static_cast<std::time_t>(timestamp);
    std::tm *tm_info = std::gmtime(&time);  // Convert to UTC time

    std::ostringstream oss;
    oss << std::put_time(tm_info, "%d/%m/%Y %H:%M:%S");  // Format as DD/MM/YYYY HH:MM:SS

    return oss.str();
}
bool StompProtocol::getShouldTerminate()
{
    return shouldTerminate;
}

bool StompProtocol::getIsLoggedIn()
{
    return isLoggedIn;
}

void StompProtocol::setShouldTerminate(bool terminate)
{
    shouldTerminate = terminate;
}
bool StompProtocol::getIsError()
{
    return isError;
}

void StompProtocol::deleteFromUserMap(string userName)
{
    userMap.erase(userName);
}