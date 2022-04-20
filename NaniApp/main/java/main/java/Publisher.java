package main.java;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;


public class Publisher extends UserNode implements Runnable,Serializable {


    public Publisher(Profile profile){
        super(profile);
        connect(currentPort);
        alivePublisherConnections.add(this);
    }

    public Publisher(int port, Profile profile){
        super(port, profile);
        connect(currentPort);
        alivePublisherConnections.add(this);
    }

    @Override
    public void run() {
        System.out.println("Publisher established connection with Broker on port: " + this.socket.getPort());
        String topic = searchTopic();
        while(!socket.isClosed()) {
            String messageToSend = consoleInput();
            if (messageToSend.equalsIgnoreCase("file")) { //type file to initiate file upload
            System.out.println("Please give full file path: \n");
            String path = this.inputScanner.nextLine();
            MultimediaFile file = new MultimediaFile(path);
            this.profile.addFileToProfile(file.getFileName(),file); //adding file to profile
            }
            else if(messageToSend.equalsIgnoreCase("exit")) { //exit for dc
                disconnectComponents(this.currentPort);
            }
            else {
                Value messageValue = new Value(messageToSend, this.profile ,topic, pubRequest);
                push(topic, messageValue);
            }
            if (checkForNewContent()){ //if a new file is added to profile we also push it
                MultimediaFile uploadedFile = getNewContent();
                pushChunks(topic,uploadedFile);
            }
        }
    }

    public synchronized void pushChunks(String topic, MultimediaFile file){ //splitting in chunks and pushing each one
        List<byte[]> chunkList = file.splitInChunks();
        Value chunk;
        for (int i = 0; i < chunkList.size(); i++) { //get all byte arrays, create chunk name and value obj
            StringBuilder strB = new StringBuilder(file.getFileName());
            String chunkName = strB.insert(file.getFileName().lastIndexOf("."), String.format("_%s", i)).toString();
            chunk = new Value("Sending file chunk", chunkName, this.profile, topic,
                    file.getNumberOfChunks() - i - 1, chunkList.get(i), pubRequest);
            push(topic, chunk);
        }
    }

    public synchronized String searchTopic(){ //initial search topic function
        String topic = consoleInput("Please enter publisher topic: ");
        if(!profile.checkSub(topic)){          //check if subbed
            int hash = hashTopic(topic);   //if not, hash and add to profile hashmap
            if (hash!=0){
                profile.sub(hash,topic); //we sub to the topic as well
                System.out.printf("Subbed to topic:%s with hash:%s %n\n", topic, hash);
            }
        }
        Value value = new Value("search", this.profile, pubRequest);
        try {
            push(topic, value);
            int response = (int)objectInputStream.readObject(); //asking and receiving port number for correct Broker based on the topic
            if (response != socket.getPort()){ //if we are not connected to the right one, switch conn
                System.out.println("SYSTEM: Switching Publisher connection to another broker on port: " + response);
                connect(response);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
            disconnect();
        }
        return topic;
    }


    private boolean checkForNewContent(){
        return this.profile.checkUploadQueueCount() > 0; //check if there are any items under upload Q 
    }

    private MultimediaFile getNewContent(){ //gets first item in upload Q
        return this.profile.getFileFromUploadQueue();
    }


    public int hashTopic(String topic){ //hash topic
        return topic.hashCode();
    }


    public synchronized void push(String topic, Value value){ //initial push

        try {
            System.out.printf("Trying to push to topic: %s with value: %s%n\n", topic , value);
            if (value.getMessage() != null){
                objectOutputStream.writeObject(topic); // if value is not null write to stream
                objectOutputStream.flush();
                objectOutputStream.writeObject(value); // if value is not null write to stream
                objectOutputStream.flush();
            }
            else throw new RuntimeException("Could not write to stream. Message corrupted.\n"); //else throw exc
        } catch (IOException e){
            System.out.println(e.getMessage());
            disconnect();
        }
    }
}
