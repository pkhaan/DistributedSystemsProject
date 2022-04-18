import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

public class Profile{

    private String username;
    private final HashMap<String,MultimediaFile> userMultimediaFileMap;
    private final Queue<MultimediaFile> pendingUpload;
    private final HashMap<String,String> userSubscribedConversations;

    public Profile(String username){
        this.username = username;
        this.userSubscribedConversations = new HashMap<>();
        this.pendingUpload = new LinkedList<>();
        this.userMultimediaFileMap = new HashMap<>();
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

    public void sub(String id, String topic){
        userSubscribedConversations.put(id,topic);
    }

    public void removeFile(String name){
        userMultimediaFileMap.remove(name);
    }

    public void unSub(String conversationName){
        userSubscribedConversations.remove(conversationName);
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

}
