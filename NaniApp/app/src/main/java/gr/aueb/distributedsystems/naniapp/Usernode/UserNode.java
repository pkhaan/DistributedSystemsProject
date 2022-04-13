import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

public class UserNode {

    protected Socket socket;
    protected Profile profile;

    protected ObjectOutputStream objectOutputStream;
    protected ObjectInputStream objectInputStream;


    protected static final int[] socketList = new int[]{3000, 4000, 5000};

    public UserNode(){
        this(getRandomSocket(),createProfile());
    }

    public UserNode(Profile profileName){
        this(getRandomSocket(),profileName);
    }

    public UserNode(Socket socket, Profile profile) { //user node initialization
        this.socket = socket;
        this.profile = profile;
    }

    private static Socket getRandomSocket(){ //generates a random port for initial communication with a random Broker
        Socket socket = null;
        int rnd = new Random().nextInt(socketList.length);
        try{
            socket = new Socket("localhost", socketList[rnd]);
        } catch (UnknownHostException uh) {
            System.out.println("Could not find host.");
            uh.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return socket;
    }

    public Socket getSocket(){
        return this.socket;
    }

    private static Profile createProfile(){ //creates a noUsername empty profile
        return new Profile("NoUsername");
    } //creates a noUsername prof


    protected void connect(Socket socket){
        try{
            this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            this.objectInputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            disconnect();
        }
    }

    protected void disconnect(){
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
        } catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    protected void switchConnection(Socket socket){
        disconnect();
        this.socket = socket;
        connect(socket);
    }



    public static void main(String[] args) throws IOException { //running UserNode

        Profile profile = new Profile("Kostas");
        UserNode user = new UserNode(profile);
        Socket initSocket = user.getSocket();

        Publisher publisher = new Publisher(initSocket,profile);
        Consumer consumer = new Consumer(initSocket,profile);

        new Thread(publisher).start();
        new Thread(consumer).start();
    }
}


