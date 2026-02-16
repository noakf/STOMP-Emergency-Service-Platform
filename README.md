# Emergency Service Messaging Platform (STOMP Client & Server)
*Java • C++ • Networking • Multithreading • STOMP Protocol*

---

##  Overview
This project implements a full **Emergency Service Messaging Platform** that allows users to subscribe to and report on emergency channels such as **fire**, **police**, **medical**, and **natural disasters**.  
The system is based on the **STOMP (Simple Text Oriented Messaging Protocol)** and includes both a **Java server** and a **C++ client**, supporting concurrent communication between multiple users.

---

##  Components

###  Server (Java)
Implements a STOMP message broker that routes messages between clients subscribed to the same topics.

Two server modes are supported:
- **Thread-Per-Client (TPC)** – A dedicated thread for each client.
- **Reactor** – An event-driven model for efficient I/O handling.

#### Core Classes
- `Connections<T>` – Manages all active client connections, providing:
  - `send(int connectionId, T msg)`  
  - `send(String channel, T msg)`  
  - `disconnect(int connectionId)`
- `ConnectionHandler<T>` – Sends messages to a specific client.
- `StompMessagingProtocol<T>` – Handles STOMP frame parsing and processing.
- `MessageEncoderDecoder<T>` – Encodes and decodes frames.
- `StompServer` – Entry point, supports both **TPC** and **Reactor** modes.

#### Build & Run
```bash
mvn compile
mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.stomp.StompServer" -Dexec.args="7777 tpc"
# or
mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.stomp.StompServer" -Dexec.args="7777 reactor"
```

---

###  Client (C++)
A multithreaded STOMP client that allows users to:
- Log in and out
- Subscribe / unsubscribe to emergency channels
- Report emergency events via JSON files
- Receive live updates
- Generate summaries of reported events

The client runs **two threads**:
- **Keyboard thread** – Reads commands from stdin.  
- **Socket thread** – Listens for messages from the server.

#### Build & Run
```bash
make
./bin/StompEMIClient
```

---

##  STOMP Frames Overview

### Client Frames
- `CONNECT`, `SEND`, `SUBSCRIBE`, `UNSUBSCRIBE`, `DISCONNECT`

### Server Frames
- `CONNECTED`, `MESSAGE`, `RECEIPT`, `ERROR`

All frames terminate with a null character (`\0`).  
Example of a `SEND` frame:
```
SEND
destination:/topic/fire_dept
user:joe
city:Liberty City
event name: Fire
date time:1773279900
general information:
active:true
forces_arrival_at_scene:true
description:
A gas pipe leak caused a fire at the fabric factory.
^@
```

---

##  Client Commands

| Command | Description |
|----------|--------------|
| `login {host:port} {username} {password}` | Connects to the server via STOMP |
| `join {channel_name}` | Subscribes to an emergency topic |
| `exit {channel_name}` | Unsubscribes from a topic |
| `report {file}` | Reads and sends events from JSON |
| `summary {channel_name} {user} {file}` | Exports a summary report |
| `logout` | Gracefully disconnects from the server |

Example:
```
login 127.0.0.1:7777 meni films
join fire_dept
report events1_partial.json
summary fire_dept meni summary.txt
logout
```

---

##  Emergency Event Format
Emergency events are parsed from JSON files using the provided `Event` class and `parseEventsFile()` function.

Each event contains:
```json
{
  "channel_name": "police",
  "events": [
    {
      "event_name": "Grand Theft Auto",
      "city": "Liberty City",
      "date_time": "1762966800",
      "description": "Pink Lampadati Felon with license plate 'STOL3N1'",
      "general_information": {
        "active": true,
        "forces_arrival_at_scene": false
      }
    }
  ]
}
```

---

##  Summary Output Example
```
Channel fire_dept
Stats:
Total: 5
active: 3
forces arrival at scene: 2

Event Reports:
Report_1:
city: Liberty City
date time: 29/12/24 22:15
event name: Fire
summary: A gas pipe leak caused a f...

Report_2:
city: Raccoon City
date time: 30/12/24 09:30
event name: Explosion
summary: Gas tank exploded near th...
```

---

##  Testing & Tools
- **Server:** built with Maven  
- **Client:** compiled with GNU Make  
- **Protocol:** tested using both provided and custom event JSON files  
- **Examples:** provided echo and newsfeed demos

---

##  Technologies
- Java 17  
- C++11  
- Multithreading (`std::thread`, `std::mutex`)  
- STOMP Protocol v1.2  
- JSON Parsing  
