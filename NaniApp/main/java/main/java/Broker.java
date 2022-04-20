package main.java;
import java.io.IOException;
import java.io.Serializable;
import java.net.*;
import java.util.HashMap;

public class Broker implements Serializable {

    private final int id;
    private final InetAddress address;

    private HashMap<Integer,String> portsAndAddresses; //ports and addresses
    private HashMap<Integer,HashMap<Integer,String>> availableBrokers; //ids, ports and addresses
    private HashMap<Integer,String> hashTopics; //hash and topics
    private HashMap<Integer,Integer> hashedTopicsToBrokers; //topic hashes their corresponding Broker

    private final ServerSocket serverSocket;

    public Broker(ServerSocket serverSocket , InetAddress address, int id){
        this.serverSocket = serverSocket;
        this.address = address;
        this.id = id;
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

    public int getBrokerHash(){
        String brokerHash = getBrokerAddress() + getBrokerPort();
        return brokerHash.hashCode();
    }

    public static int getBrokerHash(String address, String port){
        String brokerHash = address + port;
        return brokerHash.hashCode();
    }

    public int getBrokerID(){
        return this.id;
    }

    private void readConfigurationFile(String file){
        //read config to retrieve all relevant information regarding brokers and topics and then assign topics to brokers
    }



    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(4000);
        Broker broker = new Broker(serverSocket, InetAddress.getByName("localhost"), 2);
        System.out.println("SYSTEM: Broker_" + broker.getBrokerID()+" connected at: " + serverSocket + "with address: " +  broker.getBrokerAddress()
        + " and hashcode: " + broker.getBrokerHash());
        broker.startBroker();
    }
}
