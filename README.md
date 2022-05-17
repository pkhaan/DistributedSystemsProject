# Distributed Systems Project 2021-2022

_This is a repository regarding the assignment of Distributed Systems Course in 2021-2022 class._

### Collaborators

* **Kostas Kostantios**
* **Nikolas Giannakopoulos**
* **Petros Chanas**

>The main Language used in the project is Java .


| Java     |Android App|
| ---      | ---       |
| IntelliJ | JetBrains |
| Implementation|Android Studio|    



## RUN INSTRUCTIONS

Backend of event delivery system is implemented with some additions via console for both debugging reasons and examination.
- 3x Broker main runs (manually edit port and ids before running under Broker main) with: 

1. Port: 3000 ID: 0
2. Port: 4000 ID: 1
3. Port: 5000 ID: 2

- 3x Usernode runs (manually edit username before running under Usernode main) with 3 **different** usernames. 

When running Usernode: 
1. Specify topic name for consumer and publisher separately as they are 
different components.
2. After connecting to the required Broker: 
- Type anything for live chat messaging between users connected to the same topic
- Type "file" to initiate file upload. The file is also being uploaded to user's profile and shared to the live topic conversation
- Type "exit" to disconnect current user (both consumer and publisher)

3. Chat history is also implemented. Any user (consumer), when initially connecting to the broker, 
is being sent the chat history along with the files shared. Files shared are being downloaded automatically.

Note that download folder is hardcoded to:
USERDIR\DownloadedContent\

### Additional information regarding implementation: 

- When any component is connected to the server, instead of returning a list with all brokers and available topics, 
broker is checking the topic and only returns the correct port and IP of the broker that usernode needs to
connect. User node is then switching connection to the correct broker.
- Consumer and publisher are designed as two different components. Downtime with one of the components will not affect the other.
- Due to the separation, one Usernode can connect to different brokers at the same time (e.g. Publisher and Consumer connected to two different topics under different Brokers)
- Available Topics, Broker IDs and IPs are read by both Brokers and Usernode via /config.txt file. 


PS: Under /testfiles/ folder you can find some lightweight files for testing.
 




