package main.java;

import java.io.*;
import java.net.Socket;
import java.util.*;
import static java.lang.Integer.parseInt;

public class UserNode implements Serializable {

    protected Socket socket;
    protected Profile profile;
    protected int currentPort;
    protected String currentAddress;
    protected final String pubRequest = "Publisher";
    protected final String conRequest = "Consumer";
    protected final String downloadPath = System.getProperty("user.dir")
            .concat("\\DownloadedContent\\");

    protected ObjectOutputStream objectOutputStream;
    protected ObjectInputStream objectInputStream;
    protected Scanner inputScanner;

    protected static final Object lock = new Object();

    protected static final int[] portNumbers = new int[]{3000,4000,5000}; //for testing 1 broker only please keep 1 port and run the broker on the same
    protected static HashMap<Integer,String> portsAndAddresses = new HashMap<>(); //ports and addresses
    protected static HashMap<Integer,Integer> availableBrokers =  new HashMap<>(); //ids, ports
    protected static List<String> availableTopics = new ArrayList<>();

    protected ArrayList<Publisher> alivePublisherConnections; //keeping alive publisher connections
    protected ArrayList<Consumer> aliveConsumerConnections; //keeping alive consumer connections


    public UserNode(){
        this(getRandomSocketPort(),createProfile());
    }

    public UserNode(Profile profileName){
        this(getRandomSocketPort(),profileName);
    }

    public UserNode(int port, Profile profile) { //user node initialization
        this.currentPort = port;
        this.currentAddress = portsAndAddresses.get(port);
        this.profile = profile;
        alivePublisherConnections = new ArrayList<>();
        aliveConsumerConnections = new ArrayList<>();
    }

    private static int getRandomSocketPort(){ //generates a random port for initial communication with a random Broker

        return portNumbers[new Random().nextInt(portNumbers.length)];

    }
    private static Profile createProfile(){ //creates a noUsername empty profile
        return new Profile("NoUsername");
    } //creates a noUsername prof

    protected synchronized String consoleInput(String message){ //synchronized method to be used with the lock
        System.out.println(message);
        String input = null;
        if (this.inputScanner.hasNextLine()) {
            input = this.inputScanner.nextLine();
        }
        return input;
    }

    protected String consoleInput(){
        String input = null;
        if (this.inputScanner.hasNextLine()) {
            input = this.inputScanner.nextLine();
        }
        return input;
    }

    protected void connect(int port, String address, String type){ //initial connection method, initializes socket, streams and scanner as well as passes an
        try{ //initial connection message to the broker when connecting (this is also used when switching connections between brokers)
            this.socket = new Socket(address, port);
            this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            this.objectInputStream = new ObjectInputStream(socket.getInputStream());
            this.inputScanner = new Scanner(System.in);
            try { //initial connection request for both publisher and consumer
                Value initMessage = new Value("Connection", this.profile, type);
                objectOutputStream.writeObject(initMessage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            if (e.getMessage().equalsIgnoreCase("Connection refused: connect")){
                System.out.println( type + " connection failed. Broker at port: "
                        + port + " is currently unavailable.");
                disconnectAll();
            }
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

    private static void readConfig(String path){ //reading ports, hostnames and topics from config file
        File file = new File(path); //same method on both brokers and user node
        try {
            Scanner reader = new Scanner(file);
            reader.useDelimiter(",");
            String id, hostname, port;
            id = reader.next();
            while(reader.hasNext() && !id.equalsIgnoreCase("#")){
                hostname = reader.next();
                port = reader.next();
                portsAndAddresses.put(parseInt(port),hostname);
                availableBrokers.put(parseInt(id),parseInt(port));
                id = reader.next();
            }
            while(reader.hasNext()){
                String topic = reader.next();
                availableTopics.add(topic);
            }
        } catch (FileNotFoundException e){
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) { //running UserNode

        UserNode.readConfig(System.getProperty("user.dir").concat("\\src\\main\\java\\main\\java\\config.txt"));
        Profile profile = new Profile("mitsos");
        Publisher kostaspub = new Publisher(profile);
        Consumer kostascon = new Consumer(profile);
        Thread pub = new Thread(kostaspub); //initiating both on random port
        Thread con = new Thread(kostascon);// both components ask a random broker at first and then getting redirected to the correct one if needed
        pub.start();
        con.start();
    }
}

