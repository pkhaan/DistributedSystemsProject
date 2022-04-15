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

    private ArrayList<String> Topics = new ArrayList<>();






































}
