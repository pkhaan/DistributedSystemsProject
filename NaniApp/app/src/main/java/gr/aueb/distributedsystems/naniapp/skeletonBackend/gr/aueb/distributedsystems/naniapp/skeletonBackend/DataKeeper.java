package gr.aueb.distributedsystems.naniapp.skeletonBackend;
import java.io.File;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

public class DataKeeper implements Serializable{

 /*
 This is the most important part of the program
 as it contains all the crucial data and relation
 so that the system can function properly
  */

    /*
    Every broker Address is listed as a key to
    the array list and the value is assigned relevantly
    to the broker with the aid of the hashing function
     */


    private HashMap<Address, ArrayList<String>> topicsToBrokers = new HashMap<>();

    /*Stores the hash ID of the broker address
    into the hashing function
     */

    private HashMap<Address, BigInteger> hashIdBroker = new HashMap<>();


   /*List of topics for the mentioned
   publisher each time the fun is called
    */
    private HashMap<userNode, ArrayList<String>> availablePublishers = new HashMap<>();

    //Multimedia Fiels
    private HashMap<String, ArrayList<File>> topicsMultimediaFiles = new HashMap<>();

    private ArrayList<String> availableTopics = new ArrayList<>();


    public DataKeeper() {
    }


    public synchronized HashMap<Address, ArrayList<String>> getTopicsAssociatedWithBrokers() {

        return topicsToBrokers;
    }

    public synchronized ArrayList<String> getAvailableTopics() {
        return availableTopics;
    }

    public synchronized HashMap<Address, BigInteger> getHashingIDAssociatedWithBrokers() {
        return hashIdBroker;
    }

    public synchronized HashMap<userNode, ArrayList<String>> getAvailablePublishers() {
        return availablePublishers;
    }

   // public synchronized HashMap<String, ArrayList<File>> getAllVideosByTopic() {
       // return allVideosByTopic;
    //}



    public synchronized HashMap<Address, ArrayList<String>> getTopicsToBrokers() {
        return topicsToBrokers;
    }


    public synchronized HashMap<Address, ArrayList<String>> getHashIdBroker() {
        return hashIdBroker;
    }





















}
