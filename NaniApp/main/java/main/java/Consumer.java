package main.java;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Integer.parseInt;

public class Consumer extends UserNode implements Runnable,Serializable {


    public Consumer(Profile profile){
        super(profile);
        if (socket == null){
            connect(currentPort);
        }
        aliveConsumerConnections.add(this);
    }

    public Consumer(int port, Profile profile){
        super(port, profile);
        connect(currentPort);
        aliveConsumerConnections.add(this);
    }

    @Override
    public void run() {
        System.out.println("Consumer established connection with Broker on port: " + this.socket.getPort());
        String topic = consoleInput("Please enter consumer topic: ");
        if (topic != null) {
            while (true){
                int response = checkBroker(topic);
                if (response != socket.getPort()) {
                    System.out.println("SYSTEM: Switching Consumer connection to another broker on port: " + response);
                    connect(response);
                } else break;
            }
            List<Value> data = getConversationData(topic); //getting conversation data at first
            List<Value> chunkList = new ArrayList<>();
            for (Value message : data) {
                if (message.isFile()) {
                    chunkList.add(message);
                    writeFiles(chunkList);
                } else {
                    System.out.println(message.getProfile().getUsername() + ": " + message.getMessage());
                }
            }
            while (!socket.isClosed()) {
                listenForMessage(); //listening for messages
            }
        }
    }

    private void listenForMessage(){
        try {
            Object message = objectInputStream.readObject();
            System.out.println("Receiving live chat message:" + message);
            if (message instanceof Value && ((Value)message).getRequestType().equalsIgnoreCase("liveMessage")){
                System.out.println(((Value) message).getProfile().getUsername() +":" + ((Value) message).getMessage());
            }
            else{
                //need to implement broadcasting file
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
            disconnect();
        }

    }


    private synchronized List<Value> getConversationData(String topic){
        List<Value> data = new ArrayList<>();
        Value value = new Value("dataRequest", this.profile, topic, conRequest);
        try {
            objectOutputStream.writeObject(value);
            objectOutputStream.flush();
            int incomingTopicMessages = (Integer)objectInputStream.readObject();
            System.out.println("Need to receive: " + incomingTopicMessages);
            for(int i= 0; i < incomingTopicMessages; i++){
                data.add((Value)objectInputStream.readObject());
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            disconnect();
        }
        return data;
    }

    private synchronized int checkBroker(String topic){
        int response = 0;
        try {
            objectOutputStream.writeObject(topic);
            objectOutputStream.flush();
            response = (int)objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            disconnect();
        }
        return response;
    }

    private synchronized void writeFiles(List<Value> chunkList){ //withdrawal and writing of files from the
        List<String> filenames = new ArrayList<>(); //random chunkList that we received
        String temp = "";
        for (Value chunk : chunkList){ //getting all different filenames from the chunkList received
            System.out.println("----Received chunk: " + chunk);
            String chunkName = chunk.getFilename();
            String filename = chunkName.substring(0, chunkName.indexOf("_"));
            String fileExt = chunkName.substring(chunkName.indexOf("."));
            if(!temp.contains(filename)){
                temp = filename;
                filenames.add(filename + fileExt);
            }
        }
        for (String filename : filenames) { //for each filename create a file on the download path
            System.out.println("----Found name: " + filename);
            Value[] sortedChunks = sortChunks(filename, chunkList);
            System.out.println(Arrays.toString(sortedChunks));
            File download = new File(downloadPath + sortedChunks[0].getProfile().getUsername() + "_" + filename);
            try {
                FileOutputStream os = new FileOutputStream(download);
                for (Value chunk : sortedChunks) {
                    os.write(chunk.getChunk());
                }
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
                disconnect();
            }
        }
    }

    private Value[] sortChunks(String filename, List<Value> chunks){ //retrieving and sorting chunks for
        List<Value> filenameChunks = new ArrayList<>(); // a specific filename
        for (Value chunk : chunks) {
            if (chunk.getFilename().startsWith(filename.substring(0, filename.indexOf(".")))){
                filenameChunks.add(chunk);
            }
        }
        Value[] sortedChunks = new Value[filenameChunks.size()];
        for (Value chunk : filenameChunks){
            int index = parseInt(chunk.getFilename().substring(chunk.getFilename().indexOf("_") + 1,
                    chunk.getFilename().indexOf("_") + 2) );
            sortedChunks[index] = chunk;
        }
        return sortedChunks;
    }

}