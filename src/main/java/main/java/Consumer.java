package main.java;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import static java.lang.Integer.parseInt;

public class Consumer extends UserNode implements Runnable,Serializable {


    public Consumer(Profile profile){
        super(profile);
        connect(currentPort, conRequest);
        aliveConsumerConnections.add(this);
    }

    public Consumer(int port, Profile profile){
        super(port, profile);
        connect(currentPort, conRequest);
        aliveConsumerConnections.add(this);
    }

    @Override
    public void run() {
        if (this.socket != null) {
            System.out.println("SYSTEM: Consumer established connection with Broker on port: " + this.socket.getPort());
            String topic;
            synchronized (lock) { // consumer's part will run first asking for topic, locking and notifying publisher once we received the topic successfully
                topic = consoleInput("SYSTEM: Please enter consumer topic: ");
                if (topic != null) {
                    while (true) {
                        int response = checkBroker(topic);
                        if (response == 0) { //non-existing topic case
                            System.out.println("SYSTEM: There is no existing topic named: " + topic + ". Here are available ones: " + availableTopics);
                            topic = consoleInput("SYSTEM: Please enter consumer topic: ");
                        } else if (response != socket.getPort()) { //incorrect port
                            System.out.println("SYSTEM: Switching Consumer connection to another broker on port: " + response);
                            connect(response, conRequest);
                        } else break; //correct port
                    }
                }
                lock.notifyAll();
            }
            List<Value> data = getConversationData(topic); //getting conversation data at first
            List<Value> chunkList = new ArrayList<>(); //separating chunks from live messages
            for (Value message : data) {
                if (message.isFile()) {
                    chunkList.add(message); //if its a file add it to the chunklist
                } else {
                    System.out.println(message.getProfile().getUsername() + ": " + message.getMessage()); //if not, print the message to console
                }
            }
            writeFilesByID(chunkList); //sorting chunk list and writing files
            while (!socket.isClosed()) {
                listenForMessage(); //listening for messages while we are connected
            }
        } else {
            System.out.println("SYSTEM: Consumer exiting...");
        }
    }

    private void listenForMessage(){ //main consumer functionality,listening for messages and files while connected as consumer to a specific topic
        try {
            Object message = objectInputStream.readObject();
            if (message instanceof Value && ((Value)message).getRequestType().equalsIgnoreCase("liveMessage")){ //live message case
                System.out.println("SYSTEM: Receiving live chat message:" + message);
                System.out.println(((Value) message).getProfile().getUsername() +":" + ((Value) message).getMessage());
            }
            else if (message instanceof Value && ((Value)message).getRequestType().equalsIgnoreCase("liveFile")){ //live file case
                System.out.println("SYSTEM: " + ((Value) message).getUsername() + " has started file sharing. Filename: " + ((Value) message).getFilename());
                List<Value> chunkList = new ArrayList<>();
                int incomingChunks = ((Value) message).getRemainingChunks();
                for (;incomingChunks >= 0; incomingChunks--){
                    chunkList.add((Value)message);
                    if (incomingChunks == 0){break;}
                    message = objectInputStream.readObject();
                }
                System.out.println(chunkList);
                writeFilesByID(chunkList);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
            disconnect();
        }
    }


    private synchronized List<Value> getConversationData(String topic){ //getting conversation history once we connect to the topic
        List<Value> data = new ArrayList<>();
        Value value = new Value("datareq", this.profile, topic, conRequest);
        try {
            objectOutputStream.writeObject(value);
            objectOutputStream.flush();
            int incomingTopicMessages = (Integer)objectInputStream.readObject(); //asking how many messages (chunks + livechat) to read
            System.out.println("SYSTEM: Number of conversation history messages and files: " + incomingTopicMessages);
            for(int i= 0; i < incomingTopicMessages; i++){
                data.add((Value)objectInputStream.readObject());
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
            disconnect();
        }
        return data;
    }

    private synchronized int checkBroker(String topic){ //asking if we are on the correct broker for the topic
        int response = 0;
        try {
            objectOutputStream.writeObject(topic);
            objectOutputStream.flush();
            response = (int)objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
            disconnect();
        }
        return response;
    }

    private synchronized void writeFilesByID(List<Value> chunkList){ //withdrawal and writing of all files received
        String temp ="";
        List<String> fileIDs = new ArrayList<>();
        for (Value chunk : chunkList) { //separating chunks by file id
            if (!chunk.getFileID().equalsIgnoreCase(temp)) {
                fileIDs.add(chunk.getFileID());
                temp = chunk.getFileID();
            }
        }
        for (String id : fileIDs){ //for each id we keep the chunks in a list
            List <Value> fileList = new ArrayList<>();
            for (Value chunk : chunkList){
                if (id.equalsIgnoreCase(chunk.getFileID())){
                    fileList.add(chunk);
                }
            }
            System.out.println(fileList);
            Value[] sortedChunks = new Value[fileList.size()];
            for (Value chunk : fileList){ //and sorting them according to the number on the chunk name
                int index = parseInt(chunk.getFilename().substring
                        (chunk.getFilename().indexOf("_") + 1, chunk.getFilename().indexOf("_") + 2));
                sortedChunks[index] = chunk;
            }
            String filename = sortedChunks[0].getFilename().substring(0, sortedChunks[0].getFilename().indexOf("_"));
            String fileExt = sortedChunks[0].getFilename().substring(sortedChunks[0].getFilename().indexOf("."));
            Path path = Paths.get(downloadPath + filename + fileExt);
            int counter = 1;
            String existString;
            while (Files.exists(path)){ //if file exists loop with a counter and change filename to filename%counter%.ext
                System.out.println(path);
                existString = String.format("(%s)", counter);
                path = Paths.get(downloadPath + filename + existString + fileExt);
                counter++;
            }
            File download = new File(String.valueOf(path)); //writing file
            System.out.println("SYSTEM: Downloading file at: " + path);
            try {
                FileOutputStream os = new FileOutputStream(download);
                for (Value chunk : sortedChunks) {
                    os.write(chunk.getChunk());
                }
                os.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
                disconnect();
            }
        }
    }
}