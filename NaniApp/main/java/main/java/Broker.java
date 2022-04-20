package main.java;
import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import static java.lang.Integer.parseInt;

public class Broker implements Serializable {

    private final int id;
    private final InetAddress address;

    private static HashMap<Integer,String> portsAndAddresses = new HashMap<>(); //ports and addresses
    private static HashMap<Integer,Integer> availableBrokers =  new HashMap<>(); //ids, ports
    private static HashMap<BigInteger,String> hashedTopics = new HashMap<>();//hash and topics
    private static HashMap<String,Integer> topicsToBrokers = new HashMap<>(); //topic and broker ids
    private static List<String> availableTopics = new ArrayList<>();

    private final ServerSocket serverSocket;

    public Broker(ServerSocket serverSocket, InetAddress address, int id){
        this.serverSocket = serverSocket;
        this.address = address;
        this.id = id;
        readConfig(System.getProperty("user.dir").concat("\\src\\main\\java\\main\\java\\config.txt"));
        hashTopics();
        assignTopicsToBrokers();
    }

    public void startBroker(){
        try {
            while (!serverSocket.isClosed()){
                Socket socket = serverSocket.accept();
                System.out.println("SYSTEM: A new component connected!");
                ClientHandler clientHandler = new ClientHandler(socket);
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e){
            closeServerSocket();
        }
    }


    public void closeServerSocket(){
        try {
            if (serverSocket != null){
                serverSocket.close();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public String getBrokerAddress(){
        return this.address.toString();
    }

    public String getBrokerPort(){
        return Integer.toString(serverSocket.getLocalPort());
    }


    public int getBrokerID(){
        return this.id;
    }

    private void readConfig(String path){ //reading ports, hostnames and topics from config file
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

    //public int getBrokerHash(){}

    private static void hashTopics(){ //hashing topics with SHA-1 and getting their decimal value
        for (String topic : availableTopics){
            BigInteger hash = new BigInteger(encryptThisString(topic),16);
            hashedTopics.put(hash, topic);
        }
    }

    private static void assignTopicsToBrokers(){ //hash(topic) mod N to assign brokers
        for (Map.Entry<BigInteger, String> entry : hashedTopics.entrySet()){
            topicsToBrokers.put(entry.getValue(), entry.getKey().mod(BigInteger.valueOf(3)).intValue());
        }
        System.out.println(topicsToBrokers);
    }

    public static String encryptThisString(String input){ //SHA-1 encryption method
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            StringBuilder hashText = new StringBuilder(no.toString(16));
            while (hashText.length() < 32) {
                hashText.insert(0, "0");
            }
            return hashText.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static int searchBroker(String topic){ //searching for the correct broker given the topic
        int port = 0;
        int id = -1;
        for (Map.Entry<String, Integer> entry : topicsToBrokers.entrySet()){
            if (entry.getKey().equalsIgnoreCase(topic)){
                id = entry.getValue();
            }
        }
        for (Map.Entry<Integer, Integer> entry : availableBrokers.entrySet()){
            if (entry.getKey().equals(id)){
                port = entry.getValue();
            }
        }
        return port;
    }



    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(5000);
        Broker broker = new Broker(serverSocket, InetAddress.getByName("127.0.0.1"), 3);
        System.out.println("SYSTEM: Broker_" + broker.getBrokerID()+" initialized at: "
                + serverSocket + "with address: " +  broker.getBrokerAddress());
        broker.startBroker();
    }
}
