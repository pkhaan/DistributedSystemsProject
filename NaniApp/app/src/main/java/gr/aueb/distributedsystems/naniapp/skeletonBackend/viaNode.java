
//package main.gr.aueb.distributedsystems.naniapp.skeletonBackend;


import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;


/*
This Class practically connects to a node
of the system exploiting the usages of a
regular node in order to create volatility
and practically assume the role of a user
 */






public class viaNode extends gr.aueb.distributedsystems.naniapp.skeletonBackend.Node {

    private HashMap<String, ArrayList<File>> subscribedTopics = new HashMap<>();
    private static DataKeeper dataKeeper;
    transient Socket connection = null;
    private Address address;
    private Channel channel;
    transient ServerSocket viaNodeServerSocket = null;
    private boolean publisherFound = false;
    private boolean subscriberFound = false;
    private string Dir = "";
    transient Scanner viaNodeInput;

    public viaNode(Address address) {
        this.address = address;
        viaNodeInput = new Scanner(System.in);
    }

    public Address getAddress() {
        return address;
    }

    public dataKeeper getDataKeeper() {
        return dataKeeper;
    }

    public void setDataKeeper(DataKeeper dataKeeper) {
        this.dataKeeper = dataKeeper;
    }


    public Scanner getViaNodeInput() {
        return viaNodeInput;
    }

    public boolean SubscriberFound() {
        return subscriberFound;
    }

    public void setSubscribers(boolean subscribed) {
        subscriberFound = subscribed;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public synchronized HashMap<String, ArrayList<File>> getSubscribedTopics() {
        return subscribedTopics;
    }

    public boolean compare(AppNode appNode) {
        return this.getAddress().compare(appNode.getAddress());
    }

    public boolean compare(viaNode viaNode) {
        return this.getAddress().compare(viaNode.getAddress());
    }










}
