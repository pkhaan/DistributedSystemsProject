import java.net.Socket;

public class Consumer extends UserNode implements Runnable{


    public Consumer(Profile profile){
        super(profile);
        if (!socket.isConnected()){
            connect(socket);
            aliveConsumerConnections.add(this);
        }
    }

    public Consumer(Socket socket, Profile profile){
        super(socket, profile);
        connect(socket);
        aliveConsumerConnections.add(this);
    }

    @Override
    public void run() {
        System.out.println("Consumer established connection with Broker on port: " + this.socket.getPort());
        while(!socket.isClosed()) {
        }
    }



}
