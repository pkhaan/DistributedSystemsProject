package main.java;
import java.io.IOException;
import java.io.Serializable;
import java.net.*;

public class Broker implements Serializable {

    private final int id;
    protected static final int[] portList = new int[]{3000,4000,5000};
    private final InetAddress address;



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
                System.out.println("A new component connected!");
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
            e.printStackTrace();
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




    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(3000);
        Broker broker = new Broker(serverSocket, InetAddress.getByName("localhost"), 1);
        System.out.println("Broker_" + broker.getBrokerID()+" connected at: " + serverSocket + "with address: " +  broker.getBrokerAddress()
        + " and hashcode: " + broker.getBrokerHash());
        broker.startBroker();
    }
}
