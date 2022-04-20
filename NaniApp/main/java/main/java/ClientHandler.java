package main.java;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class ClientHandler implements Runnable,Serializable {

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    public static ArrayList<ClientHandler> connectedPublishers = new ArrayList<>();
    public static ArrayList<ClientHandler> connectedConsumers = new ArrayList<>();


    public static Multimap<Profile,String> knownPublishers = ArrayListMultimap.create();
    public static Multimap<Profile,String> registeredConsumers = ArrayListMultimap.create();
    public static Multimap<String,Value> messagesMap = LinkedListMultimap.create();


    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username;


    public ClientHandler(Socket socket){
        try {
            this.socket = socket;
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
            clientHandlers.add(this);
            connectedPublishers.add(this);
            connectedConsumers.add(this);
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
                    //------------------CHECK TOPIC HERE
                    //------------------
                    int correctPort = sendCorrectBroker(topic);
                    if (correctPort == this.socket.getLocalPort()){
                        Value value = (Value) readStream();
                        if (value != null) {
                            if (this.username == null) { //we set the username for the client handler on first value object we receive
                                this.username = value.getProfile().getUsername();
                            }
                            System.out.println(value);
                            if (value.getRequestType().equalsIgnoreCase("Publisher")
                                    && value.getMessage().equalsIgnoreCase("search")) {  //initial Search case
                                Profile userProfile = value.getProfile();
                                checkPublisher(userProfile, topic);
                            } else if (value.getRequestType().equalsIgnoreCase("Publisher")) {
                                if (!value.isFile()) { //usual data passing case
                                    messagesMap.put(topic,value);
                                    broadcastMessage(topic,value);
                                } else {
                                    messagesMap.put(topic,value);
                                    broadcastFile(topic,value);
                                }
                            } else if (value.getRequestType().equalsIgnoreCase("Consumer")
                                    && value.getMessage().equalsIgnoreCase("dataRequest")) { //initial case
                                checkConsumer(value.getProfile(), value.getTopic());
                                pull(value.getTopic());
                            }
                        }
                    }
                    else {
                        System.out.println("SYSTEM: Redirecting component to broker on port: " + correctPort);
                    }
                }
            }
        }
    }

    private void broadcastFile(String topic, Value value){
        //need to implement this
    }

    private void broadcastMessage(String topic, Value value){
        value.setRequestType("liveMessage");
        for (ClientHandler consumer : connectedConsumers){
            System.out.println(this.username + " " + consumer.getUsername());
            if (!consumer.getUsername().equalsIgnoreCase(this.username)){
                System.out.println("Broadcasting to topic: " + topic.toUpperCase() +
                        "for: " + this.username + " and value: " + value);
                try {
                    consumer.out.writeObject(value);
                    consumer.out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private synchronized void pull(String topic){ //main pull function
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
                    System.out.println("SYSTEM: Pulling: "  + entry.getValue());
                    out.writeObject(entry.getValue());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private synchronized int checkValueCount(String topic){ //checks how many messages we have for the specific topic
        int count = 0;
        for (Map.Entry<String,Value> entry : messagesMap.entries()){
            if (entry.getKey().equalsIgnoreCase(topic)){
                count++;
            }
        }
        return count;
    }

    private synchronized int sendCorrectBroker(String topic){
        try {
            out.writeObject(3000); //NEED TO UPDATE THIS TO CHECK FOR THE CORRECT BROKER PORT BASED
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 3000;
    }

    public void checkConsumer(Profile profile, String topic){
        if (!(registeredConsumers.containsEntry(profile,topic))){
            System.out.println("SYSTEM: New consumer registered to topic: " + topic
                    + " with username: " +profile.getUsername());
            registeredConsumers.put(profile, topic);
        }
    }


    public void checkPublisher(Profile profile, String topic){ //checks if publisher is known and adds them along with the topic
        if (!(knownPublishers.containsEntry(profile,topic))){
            System.out.println("SYSTEM: New publisher added to known Publishers for topic: " + topic
                    + " with username: " +profile.getUsername());
            knownPublishers.put(profile, topic);
        }
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
            System.out.println("SYSTEM: Received in following order, topic: " + entry.getKey()
                    + " and value: " + entry.getValue());
        }
    }

    public String getUsername(){
        return this.username;
    }

    public void removeClientHandler(){ //disconnects clients
        clientHandlers.remove(this);
        System.out.println("SYSTEM: A component has disconnected!");
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
