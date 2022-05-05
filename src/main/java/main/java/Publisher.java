package main.java;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;


public class Publisher extends UserNode implements Runnable,Serializable{


    public Publisher(Profile profile){
        super(profile);
        connect(currentPort, currentAddress, pubRequest);
        alivePublisherConnections.add(this);
    }

    public Publisher(int port, Profile profile){
        super(port, profile);
        connect(currentPort, currentAddress, pubRequest);
        alivePublisherConnections.add(this);
    }

    @Override
    public void run() {
        if (this.socket != null) {
            String topic;
            synchronized(lock){ //locking and waiting for consumer to receive its input first from the user
                try {  //this will not be needed later with the Android app
                    lock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.println("SYSTEM: Publisher established connection with Broker on port: " + this.socket.getPort());
            topic = consoleInput("SYSTEM: Please enter publisher topic: ");
            topic = searchTopic(topic); //finding topic
            System.out.println("--------- YOU CAN START CHATTING -----------");
            while (!socket.isClosed()) {
                String messageToSend = consoleInput();
                if (messageToSend.equalsIgnoreCase("file")) { //type file to initiate file upload
                    System.out.println("SYSTEM: Please give full file path: \n");
                    String path = this.inputScanner.nextLine();
                    MultimediaFile file = new MultimediaFile(path);
                    this.profile.addFileToProfile(file.getFileName(), file); //adding file to profile (later we will check if there are any new uploads and push the file)
                } else if (messageToSend.equalsIgnoreCase("exit")) { //exit for dc
                    disconnectComponents(this.currentPort); //disconnects both consumer and publisher (this can be changed to disconnect only publisher if needed)
                } else {
                    Value messageValue = new Value(messageToSend, this.profile, topic, pubRequest);
                    push(messageValue); //if it's a live chat message we push it
                }
                if (checkForNewContent()) { //if a new file is added to profile we also push it
                    MultimediaFile uploadedFile = getNewContent();
                    pushChunks(topic, uploadedFile);
                }
            }
        } else {
            System.out.println("SYSTEM: Publisher exiting...");
        }
    }

    public synchronized void pushChunks(String topic, MultimediaFile file){ //splitting in chunks and pushing each one
        List<byte[]> chunkList = file.splitInChunks();
        String fileID = file.getFileID();
        Value chunk;
        for (int i = 0; i < chunkList.size(); i++) { //get all byte arrays, create chunk name and value obj
            StringBuilder strB = new StringBuilder(file.getFileName());
            String chunkName = strB.insert(file.getFileName().lastIndexOf("."), String.format("_%s", i)).toString();
            chunk = new Value("SYSTEM: Sending file chunk", chunkName, this.profile, topic, fileID,
                    file.getNumberOfChunks() - i - 1, chunkList.get(i), pubRequest);
            push(chunk);
        }
    }
    private synchronized int checkBrokerPort(String topic){ //checking if we are on the correct broker
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

    private synchronized String checkBrokerAddress(){ //checking if we are on the correct broker
        String response = null;
        try {
            response = (String)objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
            disconnect();
        }
        return response;
    }


    public String searchTopic(String topic) { //initial search
        while(true) {
            int portResponse = checkBrokerPort(topic); //asking and receiving port number for correct Broker based on the topic
            String addressResponse = checkBrokerAddress();
            if (portResponse == 0 || addressResponse == null) {
                System.out.println("SYSTEM: There is no existing topic named: " + topic +". Here are available ones: " + availableTopics);
                topic = consoleInput("SYSTEM: Please enter publisher topic: ");
            } else if (portResponse != socket.getPort() || !addressResponse.equalsIgnoreCase(this.socket.getInetAddress().toString().substring(1))) { //if we are not connected to the right one, switch conn
                System.out.println("SYSTEM: Switching Publisher connection to another broker on port: " + portResponse + " and hostname: " + addressResponse);
                connect(portResponse, addressResponse, pubRequest);
            } else {
                if (!profile.checkSub(topic)) { //check if subbed
                    profile.sub(topic);
                    System.out.printf("SYSTEM: Subbed to topic:%s %n\n", topic);
                }
                break;
            }
        }
        return topic;
    }


    private boolean checkForNewContent(){
        return this.profile.checkUploadQueueCount() > 0; //check if there are any items under upload Q 
    }

    private MultimediaFile getNewContent(){ //gets first item in upload Q
        return this.profile.getFileFromUploadQueue();
    }
    public synchronized void push(Value value){ //initial push

        try {
            System.out.printf("SYSTEM: Trying to push to topic: %s with value: %s%n\n", value.getTopic() , value);
            if (value.getMessage() != null){
                objectOutputStream.writeObject(value); // if value is not null write to stream
                objectOutputStream.flush();
            }
            else throw new RuntimeException("SYSTEM: Could not write to stream. Message corrupted.\n"); //else throw exc
        } catch (IOException e){
            System.out.println(e.getMessage());
            disconnect();
        }
    }
}
