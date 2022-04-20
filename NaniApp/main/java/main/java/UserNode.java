package main.java;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class UserNode implements Serializable {

    protected Socket socket;
    protected Profile profile;
    protected int currentPort;
    protected final String pubRequest = "Publisher";
    protected final String conRequest = "Consumer";
    protected final String downloadPath = "C:\\Users\\kosta\\Desktop\\DownloadedContent\\";

    protected ObjectOutputStream objectOutputStream;
    protected ObjectInputStream objectInputStream;
    protected Scanner inputScanner;

    protected static final int[] portList = new int[]{3000};
    protected static ArrayList<String> topicList = new ArrayList<>(
            Arrays.asList("DS1", "DS2", "DS3"));


    protected static ArrayList<Publisher> alivePublisherConnections;
    protected static ArrayList<Consumer> aliveConsumerConnections;


    public UserNode(){
        this(getRandomSocketPort(),createProfile());
    }

    public UserNode(Profile profileName){
        this(getRandomSocketPort(),profileName);
    }

    public UserNode(int port, Profile profile) { //user node initialization
        this.currentPort = port;
        this.profile = profile;
        alivePublisherConnections = new ArrayList<>();
        aliveConsumerConnections = new ArrayList<>();
    }

    private static int getRandomSocketPort(){ //generates a random port for initial communication with a random Broker
        return portList[new Random().nextInt(portList.length)];
    }


    private static Profile createProfile(){ //creates a noUsername empty profile
        return new Profile("NoUsername");
    } //creates a noUsername prof

    protected synchronized String consoleInput(String message){
        System.out.println(message);
        String input = null;
        if (this.inputScanner.hasNextLine()) {
            input = this.inputScanner.nextLine();
        }
        return input;
    }

    protected synchronized String consoleInput(){
        String input = null;
        if (this.inputScanner.hasNextLine()) {
            input = this.inputScanner.nextLine();
        }
        return input;
    }

    protected void connect(int port){
        try{
            this.socket = new Socket("localhost", port);
            this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            this.objectInputStream = new ObjectInputStream(socket.getInputStream());
            this.inputScanner = new Scanner(System.in);
        } catch (IOException e) {
            disconnect();
        }
    }

    protected void disconnect(){ //this disconnects a single component (consumer/publisher) but not both
        try {
            if (this.objectInputStream != null) {
                this.objectInputStream.close();
            }
            if (this.objectOutputStream != null) {
                this.objectOutputStream.close();
            }
            if (this.socket != null) {
                this.socket.close();
            }
            if (this.inputScanner != null) {
                this.inputScanner.close();
            }
        } catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    protected void disconnectComponents(int port){ //this disconnects both as consumer and as publisher from a broker
        for (Consumer consumer : aliveConsumerConnections){
            if (consumer.currentPort == port){
                try {
                    if (consumer.objectInputStream != null) {
                        consumer.objectInputStream.close();
                    }
                    if (consumer.objectOutputStream != null) {
                        consumer.objectOutputStream.close();
                    }
                    if (consumer.socket != null) {
                        consumer.socket.close();
                    }
                    if (consumer.inputScanner != null) {
                        consumer.inputScanner.close();
                    }
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
        for (Publisher publisher : alivePublisherConnections){
            if (publisher.currentPort == port){
                try {
                    if (publisher.objectInputStream != null) {
                        publisher.objectInputStream.close();
                    }
                    if (publisher.objectOutputStream != null) {
                        publisher.objectOutputStream.close();
                    }
                    if (publisher.socket != null) {
                        publisher.socket.close();
                    }
                    if (publisher.inputScanner != null) {
                        publisher.inputScanner.close();
                    }
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    protected void disconnectPublishers(){ //this disconnects userNode from all brokers as publisher
        for (Publisher pub : alivePublisherConnections) {
            try {
                if (pub.objectInputStream != null) {
                    pub.objectInputStream.close();
                }
                if (pub.objectOutputStream != null) {
                    pub.objectOutputStream.close();
                }
                if (pub.socket != null) {
                    pub.socket.close();
                }
                if (pub.inputScanner != null) {
                    pub.inputScanner.close();
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }
    protected void disconnectConsumers(){ //this disconnects userNode from all brokers as consumer
        for (Consumer con : aliveConsumerConnections){
            try {
                if (con.objectInputStream != null) {
                    con.objectInputStream.close();
                }
                if (con.objectOutputStream != null) {
                    con.objectOutputStream.close();
                }
                if (con.socket != null) {
                    con.socket.close();
                }
                if (con.inputScanner != null) {
                    con.inputScanner.close();
                }
            } catch (IOException e){
                System.out.println(e.getMessage());
            }
        }
    }

    protected void disconnectAll(){ //this disconnects everything
        disconnectPublishers();
        disconnectConsumers();
    }


    public static void main(String[] args) { //running UserNode

        Profile profile = new Profile("Kostas");
        Publisher kostaspub = new Publisher(profile);
        Consumer kostascon = new Consumer(profile);
        Thread pub = new Thread(kostaspub); //initiating both on random port
        Thread con = new Thread(kostascon);
        pub.start();
        con.start();
    }
}

