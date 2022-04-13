import java.net.Socket;

public class Consumer extends UserNode implements Runnable{

    public Consumer(){
        super();
    }

    public Consumer(Profile profile){
        super(profile);
    }

    public Consumer(Socket socket, Profile profile) {
        super(socket, profile);
        connect(socket);
    }

    @Override
    public void run() {


    }
}
