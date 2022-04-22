
package gr.aueb.distributedsystems.naniapp.skeletonBackend;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.Channel;
import java.util.*;


/*
This Class practically connects to a node
of the system exploiting the usages of a
regular node in order to create volatility
and practically assume the role of a user
 */






public class userNode extends gr.aueb.distributedsystems.naniapp.skeletonBackend.Node {

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

    public userNode(Address address) {
        this.address = address;
        viaNodeInput = new Scanner(System.in);
    }

    public Address getAddress() {
        return address;
    }

    public DataKeeper getDataKeeper() {
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

    public boolean compare(userNode UserNode) {
        return this.getAddress().compare(userNode.getAddress());
    }

    public synchronized void downloadVideo (@NotNull File video) throws IOException {

        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        Socket userNodeRequestSocket = null;
        String path = video.getPath();
        Address connectedBroker = find(path.substring(path.lastIndexOf("$")+1));


    try{
        out = new ObjectOutputStream(userNodeRequestSocket.getOutputStream());
        in = new ObjectInputStream(userNodeRequestSocket.getInputStream());
        userNodeRequestSocket =  new Socket(connectedBroker.getIp(), connectedBroker.getPort());
        String videoAdded = video.getPath();
        videoAdded = videoAdded.substring (videoAdded.indexOf("$") + 1, videoAdded.lastIndexOf("$")) + "-" + videoAdded.substring(videoAdded.lastIndexOf("$")+1);
        out.writeObject(new MultimediaFile(media));
        out.flush();
        System.out.println(in.readObject());
        out.writeObject(this);
        out.flush();
        ArrayList<MultimediaFile> chunks = new ArrayList<>();
        while (true) {
            Object response = in.readObject();
            if (response.equals("NO MORE CHUNKS")) break;
            chunks.add((MultimediaFile) response);
            System.out.println("Received chunk");
            out.writeObject("RECEIVED");
            out.flush();
        }
        out.writeObject("EXIT");
        out.flush();
        System.out.println("Broker(): " + in.readObject());
        in.close();
        out.close();
        userNodeRequestSocket.close();

        //String videoPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        System.out.println(mediaPath + mediaChosen.toLowerCase() + ".mp4");
        FileOutputStream fos = new FileOutputStream(mediaPath + mediaChosen.toLowerCase() + ".mp4");
        int i = 0;
        for (MultimediaFile chunk : chunks) {
            i++;
            fos.write(chunk.getData());
        }
        fos.close();
    } catch (IOException | ClassNotFoundException e) {
        e.printStackTrace();
    }
    }

    public synchronized void uploadMedia(String directory, ArrayList<String> hashtags) {

        File videoFile = new File(directory);


        if (getChannel().getAllVideosPublished().contains(videoFile)) {
            System.out.println("[Broker()]:Video has been uploaded. Please check again if you want to upload another one");
            return;
        }


        HashMap<String, ArrayList<File>> userVideosByHashtag = getChannel().getUserVideosByHashtag();
        if (hashtags == null) hashtags = new ArrayList<>();
        for (String hashtag : hashtags) {
            if (!getChannel().getAllHashtagsPublished().contains(hashtag)) {
                getChannel().getAllHashtagsPublished().add(hashtag);
            }
            if (userVideosByHashtag.containsKey(hashtag)) {
                ArrayList<File> mediaFilesByHashtag = userMediaFilesByHashtag.get(hashtag);
                mediaFilesByHashtag.add(mediaFile);
            } else {
                ArrayList<File> videosByHashtag = new ArrayList<>();
                videosByHashtag.add(videoFile);
                userVideosByHashtag.put(hashtag, videosByHashtag);
            }
        }
        getChannel().getAllVideosPublished().add(videoFile);
        getChannel().getUserHashtagsPerVideo().put(videoFile, hashtags);

    }

    }

    public synchronized void delete



























    }




}