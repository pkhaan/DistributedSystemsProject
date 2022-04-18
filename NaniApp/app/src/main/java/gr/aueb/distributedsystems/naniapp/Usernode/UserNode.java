import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class UserNode implements Serializable {

    protected Socket socket;
    protected Profile profile;

    protected ObjectOutputStream objectOutputStream;
    protected ObjectInputStream objectInputStream;
    protected Scanner inputScanner;

    protected static final int[] portList = new int[]{3000};
    protected static ArrayList<Publisher> alivePublisherConnections;
    protected static ArrayList<Consumer> aliveConsumerConnections;

    public UserNode(){
        this(getRandomSocket(),createProfile());
    }

    public UserNode(Profile profileName){
        this(getRandomSocket(),profileName);
    }

    public UserNode(Socket socket, Profile profile) { //user node initialization
        this.socket = socket;
        this.profile = profile;
        alivePublisherConnections = new ArrayList<>();
        aliveConsumerConnections = new ArrayList<>();
    }

    private static Socket getRandomSocket(){ //generates a random port for initial communication with a random Broker
        Socket socket = null;
        int rnd = new Random().nextInt(portList.length);
        try{
            socket = new Socket("localhost", portList[rnd]);;
        } catch (UnknownHostException uh) {
            System.out.println("Could not find host. ");
            uh.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return socket;
    }


    private static Profile createProfile(){ //creates a noUsername empty profile
        return new Profile("NoUsername");
    } //creates a noUsername prof


    protected void connect(Socket socket){
        try{
            this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            this.objectInputStream = new ObjectInputStream(socket.getInputStream());
            this.inputScanner = new Scanner(System.in);
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
            if (this.inputScanner != null) {
                this.inputScanner.close();
            }
        } catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    protected void disconnectPublishers() {
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
    protected void disconnectConsumers(){
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

    protected void disconnectAll(){
        disconnectPublishers();
        disconnectConsumers();
    }

    protected void switchConnection(int port) {
        try {
            this.socket = new Socket("localhost", port);

        } catch (IOException e){
            System.out.println(e.getMessage());
            disconnectAll();
        }
        connect(this.socket);
    }


    public static void main(String[] args) throws IOException { //running UserNode

        Profile profile = new Profile("Kostas");
        Publisher kostaspub = new Publisher(profile);
        Consumer kostascon = new Consumer(profile);
        Thread pub = new Thread(kostaspub); //initiating both on random port
        Thread con = new Thread(kostascon);
        MultimediaFile upload = new MultimediaFile("C:\\Users\\kosta\\Desktop\\test.png");
        kostaspub.profile.addFileToProfile(upload.getFileName(),upload);
        pub.start();
        con.start();
    }
}


