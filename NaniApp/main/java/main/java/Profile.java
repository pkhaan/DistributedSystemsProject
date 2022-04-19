package main.java;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;


public class Profile implements Serializable {

    private String username;
    public static Multimap<String,MultimediaFile> userMultimediaFileMap;
    private final Queue<MultimediaFile> pendingUpload;
    private final HashMap<Integer,String> userSubscribedConversations;

    public Profile(String username){
        this.username = username;
        this.userSubscribedConversations = new HashMap<>();
        this.pendingUpload = new LinkedList<>();
        userMultimediaFileMap = ArrayListMultimap.create();
    }

    public void addFileToProfile(String fileName, MultimediaFile file){

        userMultimediaFileMap.put(fileName,file);
        addFileToUploadQueue(file);
    }

    public void addFileToUploadQueue(MultimediaFile file){
        pendingUpload.add(file);
    }

    public MultimediaFile getFileFromUploadQueue(){
        return pendingUpload.poll();
    }

    public boolean checkSub(String topic){
        return userSubscribedConversations.containsValue(topic);
    }

    public void sub(int id, String topic){
        userSubscribedConversations.put(id,topic);
    }

    public void removeFile(String name, MultimediaFile file){
        userMultimediaFileMap.remove(name,file);
    }

    public void unSub(String conversationName){
        userSubscribedConversations.remove(conversationName.hashCode());
    }

    public String getUsername(){
        return this.username;
    }

    public void setUserName(String username){
        this.username = username;
    }

    public int checkUploadQueueCount(){
        return pendingUpload.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Profile profile = (Profile) o;
        return Objects.equals(username, profile.username) && Objects.equals(pendingUpload, profile.pendingUpload)
                && Objects.equals(userSubscribedConversations, profile.userSubscribedConversations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, pendingUpload, userSubscribedConversations);
    }
}
