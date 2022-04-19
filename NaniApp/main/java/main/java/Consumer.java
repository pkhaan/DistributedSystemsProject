package main.java;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Consumer extends UserNode implements Runnable, Serializable {



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
        String topic = consoleInput("Please type conversation to check: ");
        if (topic != null) {
            int response = checkBroker(topic);
            if (response != socket.getPort()) {
                connect(response);
            } else {
                while (!socket.isClosed()) {
                    List<Value> data = getConversationData(topic);
                    List<Value> chunkList = new ArrayList<>();
                    for (Value message : data) {
                        if (message.isFile()) {
                            chunkList.add(message);
                            writeFiles(chunkList);
                        } else {
                            System.out.println(message.getProfile().getUsername() + ": " + message.getMessage());
                        }
                    }
                }
            }
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
        for (Value chunk : chunkList){
            String chunkName = chunk.getFilename();
            System.out.println(chunkName);
            String filename = chunkName.substring(0, chunkName.indexOf("_") - 1);
            String fileExt = chunkName.substring(chunkName.indexOf("."));
            if(!temp.contains(filename)){
                temp = filename;
                filenames.add(filename + fileExt);
            }
        }
        for (String filename : filenames){
            File download = new File(downloadPath + filename);
            FileOutputStream os = null;
            for (Value chunk : chunkList){
                if (chunk.getFilename().startsWith(filename.substring(0, filename.indexOf("_") - 1))){
                    try {
                        os = new FileOutputStream(download);
                        os.write(chunk.getChunk());
                    } catch (IOException e) {
                        e.printStackTrace();
                        disconnect();
                    }
                }
            }
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                disconnect();
            }
        }
    }

}


  /*
        Value brokerList = getBrokerList();
        if (brokerList != null){

        }
    }


    public synchronized Value getBrokerList(){
        Value value = new Value("Brokerlist.", "Consumer");
        try {
            objectOutputStream.writeObject(value);
            objectOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Value answer = null;
        try {
            if (objectInputStream.readObject() instanceof Value){
                answer = (Value)objectInputStream.readObject();
            }
        }catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
        return answer;
    }

    public synchronized void updateBrokerAndTopicInformation(){

    }
    */