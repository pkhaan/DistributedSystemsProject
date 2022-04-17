
import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;


public class Publisher extends UserNode implements Runnable, Serializable {


    public Publisher(){
        super();
    }

    public Publisher(Profile profile){
        super(profile);
    }

    public Publisher(Socket socket, Profile profile) {
        super(socket, profile);
        connect(socket);
    }

    @Override
    public void run() {
        System.out.println("Publisher established connection with Broker on port: " + this.socket.getPort());
        String topic = searchTopic();
        while(!socket.isClosed()) {
            if (this.inputScanner.hasNextLine()){
                String messageToSend = this.inputScanner.nextLine();
                if (messageToSend.equalsIgnoreCase("file")) { //file to initiate file upload
                    System.out.println("Please give full file path: \n");
                    String path = this.inputScanner.nextLine();
                    MultimediaFile file = new MultimediaFile(path);
                    this.profile.addFileToProfile(file.getFileName(),file);

                    List<byte[]> chunkList = file.splitInChunks();
                    Value chunk;
                    for (int i = 0; i < chunkList.size(); i++) { //get all byte arrays, create chunk name and value obj
                        StringBuilder strB = new StringBuilder(file.getFileName());
                        String chunkName = strB.insert(file.getFileName().lastIndexOf("."), String.format("_%s", i)).toString();
                        chunk = new Value("Sending file chunk", chunkName, file.getNumberOfChunks() - i - 1, chunkList.get(i));
                        push(topic, chunk);
                    }
                }
                else if(messageToSend.equalsIgnoreCase("exit")) { //exit for dc
                    disconnect();
                }
                else {
                    Value messageValue = new Value(messageToSend);
                    push(topic, messageValue);
                }
            }

            //----------NOT TESTED THREAD
            Thread autoCheck = new Thread(new Runnable() { //while taking input from clients' consoles above
                // we automatically check for new Profile file uploads from Upload queue with a new thread
                @Override
                public synchronized void run() {
                    if (checkForNewContent()){
                        MultimediaFile uploadedFile = getNewContent();
                        List<byte[]> chunkList = uploadedFile.splitInChunks();
                        Value chunk;
                        for (int i=0; i < chunkList.size(); i++){ //get all byte arrays, create chunk name and value obj
                            String chunkName = uploadedFile.getFileName().concat(String.format("_%s", i));
                            chunk = new Value("Sending file chunk", chunkName, uploadedFile.getNumberOfChunks()-i-1, chunkList.get(i));
                            push(topic, chunk); //if we find new profile content we split it we send it to all users
                        }  //this is probably not needed as profile page will be different on final implementation
                    }
                }
            });
            autoCheck.start();
        }
    }

    public synchronized String searchTopic(){
        System.out.print("Please enter topic: ");
        String topic = this.inputScanner.nextLine();
        if(!profile.checkSub(topic)){          //check if subbed
            String hash = hashTopic(topic);   //if not, hash and add to profile hashmap
            if (hash!=null){
                profile.sub(hash,topic); //we sub to the topic as well
                System.out.printf("Subbed to topic:%s %n\n", topic);
            }
        }
        Value value = new Value("search",this.profile.getUsername());
        try {
            push(topic,value);
            int answer = (int)objectInputStream.readObject(); //asking and receiving port number for correct Broker based on the topic
            System.out.printf("Correct broker on port: %s\n", answer);
            if (answer != socket.getPort()){ //if we are not connected to the right one, switch conn
                switchConnection(new Socket("localhost", answer));
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            disconnect();
        }
        return topic;
    }



    private boolean checkForNewContent(){
        return this.profile.checkUploadQueueCount() > 0; //check if there are any items under upload Q 
    }

    private synchronized MultimediaFile getNewContent(){ //gets first item in upload Q
        return this.profile.getFileFromUploadQueue();
    }


    public String hashTopic(String topic){ //hash topic with MD5
        try{
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte [] hashBytes = md.digest(topic.getBytes(StandardCharsets.UTF_8));
            StringBuilder hash = new StringBuilder();
            for (byte b : hashBytes) {
                hash.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            return hash.toString();
        } catch (NoSuchAlgorithmException e){
            System.out.println(e.getMessage());
            disconnect();
        }
        return null;
    }


    public synchronized void push(String topic, Value value){ //initial push

        try {
            System.out.printf("Trying to push to topic: %s with value: %s%n\n", topic , value);
            if (value.getMessage() != null){
                objectOutputStream.writeObject(topic); // if value is not null write to stream
                objectOutputStream.writeObject(value); // if value is not null write to stream
                objectOutputStream.flush();
            }
            else throw new RuntimeException("File or filechunk corrupted.\n"); //else throw exc
        } catch (IOException e){
            System.out.println(e.getMessage());
            disconnect();
        }
    }
}
