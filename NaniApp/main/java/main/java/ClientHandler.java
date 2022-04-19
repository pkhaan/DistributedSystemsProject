package main.java;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class ClientHandler implements Runnable, Serializable{

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    public static Multimap<Profile,String> connectedPublishers = ArrayListMultimap.create();
    public static Multimap<Profile,String> registeredConsumers = ArrayListMultimap.create();


    public static Multimap<String,Value> messagesMap = ArrayListMultimap.create();


    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private static final int CHUNK_KB_SIZE = 512 * 1024;


    public ClientHandler(Socket socket){
        try {
            this.socket = socket;
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
            clientHandlers.add(this);
        } catch (IOException e) {
            closeEverything(socket, out, in);
        }
    }


    @Override
    public void run() {
        Object streamObject;
        while(!socket.isClosed()){
            streamObject = readStream();
            System.out.println(streamObject);
            if(streamObject!=null){
                if (streamObject instanceof String topic) {
                    //------------------CHECK TOPIC HERE AND DISCONNECT IF NEEDED
                    sendCorrectBroker(topic);
                    Value value = (Value)readStream();
                    System.out.println(value);
                    if (value.getRequestType().equalsIgnoreCase("Publisher")
                            && value.getMessage().equalsIgnoreCase("search")){  //initial Search case
                        Profile userProfile = value.getProfile();
                        checkPublisher(userProfile,topic);
                    }
                    else if (value.getRequestType().equalsIgnoreCase("Publisher")){
                        if (!value.isFile()) { //usual data passing case
                            messagesMap.put(topic,value);
                        } else {
                            messagesMap.put(topic,value);
                        }
                    } else if (value.getRequestType().equalsIgnoreCase("Consumer")
                            && value.getMessage().equalsIgnoreCase("dataRequest")) { //initial case
                            checkConsumer(value.getProfile(), value.getTopic());
                            pull(value.getTopic());
                    }
                }
            }
        }
    }


    private synchronized void pull(String topic){
        int count = checkValueCount(topic);
        try {
            out.writeObject(count);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (Map.Entry<String,Value> entry : messagesMap.entries()){
            if (entry.getKey().equalsIgnoreCase(topic)){
                try {
                    System.out.println("Pulling: "  + entry.getValue());
                    out.writeObject(entry.getValue());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private synchronized int checkValueCount(String topic){
        int count = 0;
        System.out.println("-----------" + topic);
        for (Map.Entry<String,Value> entry : messagesMap.entries()){
            if (entry.getKey().equalsIgnoreCase(topic)){
                count++;
            }
        }
        return count;
    }

    private synchronized void sendCorrectBroker(String topic){
        try {
            out.writeObject(socket.getLocalPort()); //NEED TO UPDATE THIS TO CHECK FOR THE CORRECT BROKER PORT BASED
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void checkConsumer(Profile profile, String topic){
        if (!(registeredConsumers.containsEntry(profile,topic))){
            System.out.println("New consumer registered to topic: " + topic
                    + " with username: " +profile.getUsername());
            registeredConsumers.put(profile, topic);
        }
    }


    public void checkPublisher(Profile profile, String topic){ //checks if publisher is known and adds them along with the topic
        if (!(connectedPublishers.containsEntry(profile,topic))){
            System.out.println("New publisher added to known Publishers for topic: " + topic
                    + " with username: " +profile.getUsername());
            connectedPublishers.put(profile, topic);
        }
    }

    public void addPublisher(Profile profile, String topic){

    }


        public synchronized Object readStream(){ //main reading object method
        try {
            return in.readObject();
        } catch (ClassNotFoundException | IOException e){
            printHashMap(); //on exception (client dc) we write the file as a test
            closeEverything(socket, out, in);
            System.out.println(e.getMessage());
        }
        return null;
    }

    public void printHashMap(){
        for (Map.Entry<String,Value> entry : messagesMap.entries()){
            System.out.println("Received in following order, topic: " + entry.getKey()
                    + " and value: " + entry.getValue());
        }
    }

    public void removeClientHandler(){ //disconnects clients
        clientHandlers.remove(this);
        System.out.println("A Client has left the chat!");
    }

    public void closeEverything(Socket socket, ObjectOutputStream out, ObjectInputStream in){
        removeClientHandler(); //removes clients and closes everything
        try {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
