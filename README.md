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



### RUN INSTRUCTIONS

Backend of event delivery system is implemented with some additions via console for both debugging reasons and examination.
- 3x Broker main run with: 

1. Port: 3000 ID: 0
2. Port: 4000 ID: 1
3. Port: 5000 ID: 2

- 3x Usernodes with 3 **different** usernames. 

When running Usernode: 
1. Specify topic name for consumer and publisher separately as they are 
different components.
2. After connecting to the required Broker: 
- Type anything for live chat messaging between users connected to the same topic
- Type "file" to initiate file upload. The file is also being uploaded to user's profile and shared to the live topic conversation
- Type "exit" to disconnect current user (both consumer and publisher)

3. Chat history is also implemented. When initially connecting to the broker, 
any user is being sent the chat history along with the files shared. Files shared are being downloaded automatically.

Note that download folder is hardcoded to:
USERDIR\DownloadedContent\

PS: Under folder testfiles you can find some lightweight files for testing
 




