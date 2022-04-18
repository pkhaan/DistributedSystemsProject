import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;

public class Broker implements Serializable {

    private final ServerSocket serverSocket;

    public Broker(ServerSocket serverSocket){
        this.serverSocket = serverSocket;
    }

    public void startBroker(){
        try {
            while (!serverSocket.isClosed()){
                Socket socket = serverSocket.accept();
                System.out.println("A new client has connected!");
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

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(3000);
        System.out.println("Initiating: " + serverSocket);
        Broker broker = new Broker(serverSocket);
        broker.startBroker();
    }
}
