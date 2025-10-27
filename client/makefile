CFLAGS:=-c -Wall -Weffc++ -g -std=c++11 -Iinclude
LDFLAGS:=-lboost_system -lpthread
# TODO change the makefile for StompEMIlient
all: StompEMIClient

StompEMIClient: bin/ConnectionHandler.o bin/event.o bin/StompClient.o bin/StompProtocol.o bin/KeyBoardInput.o
	g++ -o bin/StompEMIClient.o bin/ConnectionHandler.o bin/event.o bin/StompClient.o bin/StompProtocol.o bin/KeyBoardInput.o $(LDFLAGS)

EchoClient: bin/ConnectionHandler.o bin/echoClient.o
	g++ -o bin/EchoClient bin/ConnectionHandler.o bin/echoClient.o $(LDFLAGS)

bin/ConnectionHandler.o: src/ConnectionHandler.cpp
	g++ $(CFLAGS) -o bin/ConnectionHandler.o src/ConnectionHandler.cpp

bin/echoClient.o: src/echoClient.cpp
	g++ $(CFLAGS) -o bin/echoClient.o src/echoClient.cpp

bin/event.o: src/event.cpp
	g++ $(CFLAGS) -o bin/event.o src/event.cpp

bin/StompProtocol.o: src/StompProtocol.cpp
	g++ $(CFLAGS) -o bin/StompProtocol.o src/StompProtocol.cpp

bin/StompClient.o: src/StompClient.cpp
	g++ $(CFLAGS) -o bin/StompClient.o src/StompClient.cpp

bin/KeyBoardInput.o: src/KeyBoardInput.cpp
	g++ $(CFLAGS) -o bin/KeyBoardInput.o src/KeyBoardInput.cpp

.PHONY: clean
clean:
    
	rm -f bin/*