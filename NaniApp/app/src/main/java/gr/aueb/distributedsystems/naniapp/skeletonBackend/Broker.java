
import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;





public class Broker extends Node {
    private static final int UPDATE_NODES = 0;
    private static final int UPDATE_ID = 2;
    private static final int UPDATE_ON_DELETE = 1;
    //private static final int UPDATE_PUBLISHERS = 3;
    private boolean updateID = true;
    private Address address;
    private BigInteger brokerID = BigInteger.valueOf(0);
    ServerSocket brokerServerSocket = null;
    private ArrayList<String> topicsAssociated = new ArrayList<>();
    private DataKeeper dataKeeper;
    private HashMap<Node, ArrayList<String>> registeredConsumers = new HashMap<>();
    private ArrayList<Node> registeredPublishers = new ArrayList<>();
    private HashMap<Node, ArrayList<String>> availablePublishers;

    public Broker(Address address) {
        this.address = address;
        System.out.println("[Broker]: Broker initialized as part of the System. " + address.toString());
    }

    public synchronized Address getAddress() {
        return address;
    }

    public synchronized HashMap<Node, ArrayList<String>> getRegisteredConsumers() {
        return registeredConsumers;
    }

    public synchronized ArrayList<Node> getRegisteredPublishers() {
        return registeredPublishers;
    }

    public synchronized DataKeeper getDataKeeper() {
        return dataKeeper;
    }

    public void setTopicsAssociated(ArrayList<String> topicsAssociated) {
        this.topicsAssociated = topicsAssociated;
    }

    public void setAvailablePublishers(HashMap<Node, ArrayList<String>> availablePublishers) {
        this.availablePublishers = availablePublishers;
    }

    /**
     * method setRegisteredPublishers updates the registeredPublishers list of the broker
     *        based on the topics assigned to the broker and the availablePublishers topics lists.
     *        If there is a match of at least one topic in the previously mentioned lists then
     *        the related publisher will be registered to this broker.
     */
    public synchronized void setRegisteredPublishers() {
        boolean nextPub = false;
        boolean pub_exists = false;
        for(Node publisher : availablePublishers.keySet()){
            for (String topicPublisher : availablePublishers.get(publisher)){
                System.out.println("This is my publisher topic:" + topicPublisher);
                for (String associatedTopic : topicsAssociated){
                    System.out.println("Broker topic: " + associatedTopic);
                    if (topicPublisher.equals(associatedTopic)) {
                        for (Node registeredPublisher: registeredPublishers){
                            if(registeredPublisher.compare(publisher)){
                                pub_exists = true;
                            }
                        }
                        if(!pub_exists){
                            registeredPublishers.add(publisher);
                            pub_exists = false;
                        }
                        nextPub = true;
                        break;
                    }
                }
                if(nextPub){
                    nextPub = false;
                    break;
                }
            }
        }
    }

    
    public void init(){
        calculateBrokerID();
        Thread zookeeperThread = new Thread(new Runnable() {
            @Override
            public void run() {
                updateID();
                updateID = false;
            }
        });
        zookeeperThread.start();
        openBrokerServer();
    }

    /**
     * method openBrokerServer creates new ServerSocket for the Broker to accept Node requests
     *                         which will be handled by the BrokerActionsForNodes
     */
    public void openBrokerServer(){
        try{
            brokerServerSocket = new ServerSocket(address.getPort(), Node.BACKLOG);
            System.out.println("[Broker]: INIT==True........... ___READY TO ACCEPT REQUESTS");
            Socket NodeSocket;
            while (true){
                NodeSocket = brokerServerSocket.accept();
                Thread NodeThread = new BrokerActionsForNodes(NodeSocket, this);
                appNodeThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                brokerServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * method calculateBrokerID uses SHA-1 encoding to assign a BigInteger ID to this broker
     */
    public void calculateBrokerID(){
        System.out.println("[Broker]: ID CALCULATING......");
        //Start of hashing of ip+port
        String hash = address.getIp()+ address.getPort();
        byte[] bytesOfMessage=null;
        MessageDigest md=null;
        try {
            bytesOfMessage = hash.getBytes("UTF-8");
            md = MessageDigest.getInstance("SHA-1");
        } catch (UnsupportedEncodingException ex){
            System.out.println("Unsupported encoding");
        } catch (NoSuchAlgorithmException ex){
            System.out.println("Unsupported hashing");
        }
        byte[] digest = md.digest(bytesOfMessage);
        brokerID = new BigInteger(1, digest);
    }


    /**
     * method updateID() makes a connection with the Zookeeper, in which case the broker requests to update its ID
     *                   and address at the InfoTable
     */
    public void updateID(){
        Socket brokerSocket = null;
        ObjectOutputStream brokerSocketOut = null;
        ObjectInputStream brokerSocketIn = null;
        try{
            brokerSocket = new Socket(Node.ZOOKEEPER_ADDRESS.getIp(), Node.ZOOKEEPER_ADDRESS.getPort());
            brokerSocketOut = new ObjectOutputStream(brokerSocket.getOutputStream());
            brokerSocketIn = new ObjectInputStream(brokerSocket.getInputStream());
            brokerSocketOut.writeInt(UPDATE_ID);
            brokerSocketOut.flush();
            brokerSocketOut.writeObject(address);
            brokerSocketOut.flush();
            brokerSocketOut.writeObject(brokerID);
            brokerSocketOut.flush();
            System.out.println(brokerSocketIn.readObject());
//            System.out.println(this.getInfoTable());
//            System.out.println("Topics associated with brokers:" + this.getInfoTable().getTopicsAssociatedWithBrokers());
//            System.out.println("Hashing id associated with brokers:" + this.getInfoTable().getHashingIDAssociatedWithBrokers());
//            System.out.println("All videos by topic: " + this.getInfoTable().getAllVideosByTopic());
//            System.out.println("Available publishers: " + this.getInfoTable().getAvailablePublishers());
//            System.out.println("Available topics: "+ this.getInfoTable().getAvailableTopics());
//            System.out.println("Registered publishers: " + this.getRegisteredPublishers());
//            System.out.println("Registered consumers: " + this.getRegisteredConsumers());
            brokerSocketIn.close();
            brokerSocketOut.close();
            brokerSocket.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                brokerSocketIn.close();
                brokerSocketOut.close();
                brokerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * method updateInfoTable makes a connection with the Zookeeper, in which case the broker requests to update the
     *                        InfoTable with content read from an Node (such as the hashTagsPublished of this
     *                        Node - Publisher etc).
     *                        Then it receives the UPDATED InfoTable obj from the Zookeeper.
     * @param Node Node obj
     * @param allHashtagsPublished the hashtags published by this Node publisher
     * @param allVideosPublished the videos published by this Node publisher
     * @param userVideosByHashtag the videos by hashtag published by this Node publisher
     * @param isPublisher boolean variable to check if the Node is a Publisher or not
     */
    public void updateInfoTable(Node Node, ArrayList<String> allHashtagsPublished, ArrayList<File> allVideosPublished, HashMap<String, ArrayList<File>> userVideosByHashtag, boolean isPublisher){
        Socket brokerSocket = null;
        ObjectOutputStream brokerSocketOut = null;
        ObjectInputStream brokerSocketIn = null;
        try{
            brokerSocket = new Socket(Node.ZOOKEEPER_ADDRESS.getIp(), Node.ZOOKEEPER_ADDRESS.getPort());
            brokerSocketOut = new ObjectOutputStream(brokerSocket.getOutputStream());
            brokerSocketIn = new ObjectInputStream(brokerSocket.getInputStream());
            brokerSocketOut.writeInt(UPDATE_NODES);
            brokerSocketOut.flush();
            brokerSocketOut.writeObject(Node);
            brokerSocketOut.flush();
            brokerSocketOut.writeObject(address);
            brokerSocketOut.flush();
            brokerSocketOut.writeObject(allHashtagsPublished);
            brokerSocketOut.flush();
            brokerSocketOut.writeObject(allVideosPublished);
            brokerSocketOut.flush();
            brokerSocketOut.writeObject(userVideosByHashtag);
            brokerSocketOut.flush();
            brokerSocketOut.writeBoolean(isPublisher);
            brokerSocketOut.flush();
            System.out.println(brokerSocketIn.readObject());
            dataKeeper = (DataKeeper) brokerSocketIn.readObject();
            for (Address broker :dataKeeper.getTopicsAssociatedWithBrokers().keySet()){
                if (broker.compare(address))
                    setTopicsAssociated(dataKeeper.getTopicsAssociatedWithBrokers().get(broker));
            }
            setAvailablePublishers(dataKeeper.getAvailablePublishers());
            setRegisteredPublishers();
            System.out.println(brokerSocketIn.readObject());
            System.out.println(this.getDataKeeper());
            System.out.println("Topics associated with brokers:" + this.getDataKeeper().getTopicsAssociatedWithBrokers());
            System.out.println("Hashing id associated with brokers:" + this.getDataKeeper().getHashingIDAssociatedWithBrokers());
            System.out.println("All videos by topic: " + this.getDataKeeper().getAllVideosByTopic());
            System.out.println("Available publishers: " + this.getDataKeeper().getAvailablePublishers());
            System.out.println("Available topics: "+ this.getDataKeeper().getAvailableTopics());
            System.out.println("Registered publishers: " + this.getRegisteredPublishers());
            System.out.println("Registered consumers: " + this.getRegisteredConsumers());
            brokerSocketIn.close();
            brokerSocketOut.close();
            brokerSocket.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                brokerSocketIn.close();
                brokerSocketOut.close();
                brokerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * method updateOnDelete used each time there is a delete request from the Publisher Node
     *                       BROKER makes a connection with the Zookeeper, in this case the broker requests to update the
     *                       InfoTable (delete every instance of the video to be deleted, as well as hashtags that were
     *                       only associated with this video)
     * @param Node the Publisher Node obj that made the request
     * @param toBeDeleted the video File obj that the Node requested deletion of
     * @param allHashtagsPublished the list of the hashtagsPublished from the Publisher that made the request
     */
    public void updateOnDelete(Node Node, File toBeDeleted, ArrayList<String> allHashtagsPublished){
        Socket brokerSocket = null;
        ObjectOutputStream brokerSocketOut = null;
        ObjectInputStream brokerSocketIn = null;
        try{
            brokerSocket = new Socket(Node.ZOOKEEPER_ADDRESS.getIp(), Node.ZOOKEEPER_ADDRESS.getPort());
            brokerSocketOut = new ObjectOutputStream(brokerSocket.getOutputStream());
            brokerSocketIn = new ObjectInputStream(brokerSocket.getInputStream());
            brokerSocketOut.writeInt(UPDATE_ON_DELETE);
            brokerSocketOut.flush();
            brokerSocketOut.writeObject(Node);
            brokerSocketOut.flush();
            brokerSocketOut.writeObject(toBeDeleted);
            brokerSocketOut.flush();
            brokerSocketOut.writeObject(allHashtagsPublished);
            brokerSocketOut.flush();
            System.out.println(brokerSocketIn.readObject());
            dataKeeper = (DataKeeper) brokerSocketIn.readObject();
            for (Address broker :dataKeeper.getTopicsAssociatedWithBrokers().keySet()){
                if (broker.compare(address))
                    setTopicsAssociated(dataKeeper.getTopicsAssociatedWithBrokers().get(broker));
            }
            setAvailablePublishers(dataKeeper.getAvailablePublishers());
            setRegisteredPublishers();
            System.out.println(brokerSocketIn.readObject());

            System.out.println(this.getDataKeeper());
            System.out.println("Topics associated with brokers:" + this.getDataKeeper().getTopicsAssociatedWithBrokers());
            System.out.println("Hashing id associated with brokers:" + this.getDataKeeper().getHashingIDAssociatedWithBrokers());
            System.out.println("All videos by topic: " + this.getDataKeeper().getAllVideosByTopic());
            System.out.println("Available publishers: " + this.getDataKeeper().getAvailablePublishers());
            System.out.println("Available topics: "+ this.getDataKeeper().getAvailableTopics());
            System.out.println("Registered publishers: " + this.getRegisteredPublishers());
            System.out.println("Registered consumers: " + this.getRegisteredConsumers());
            brokerSocketIn.close();
            brokerSocketOut.close();
            brokerSocket.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                brokerSocketIn.close();
                brokerSocketOut.close();
                brokerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
