
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Scanner;


public class Publisher extends UserNode implements Runnable{


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
        Scanner scanner = new Scanner(System.in);
        while(socket.isConnected()) {
            String messageToSend = scanner.nextLine();
            if (!messageToSend.equalsIgnoreCase("file")) { //for testing, we give the option to write
                Value messageValue = new Value(messageToSend);        //file in console to initiate file upload
                push(topic, messageValue);                            //which we also add to profile files
            } else {
                System.out.println("Please give full file path: ");
                String path = scanner.nextLine();
                MultimediaFile file = new MultimediaFile(path);
                this.profile.addFileToProfile(file.getFileName(),file);

                List<byte[]> chunkList = file.splitInChunks();
                Value chunk;
                for (int i = 0; i < chunkList.size(); i++) { //get all byte arrays, create chunk name and value obj
                    String chunkName = file.getFileName().concat(String.format("_%s", i));
                    chunk = new Value("Sending file chunk", chunkName, topic, chunkList.get(i));
                    push(topic, chunk);
                }
            }

            Thread autoCheck = new Thread(new Runnable() { //while taking input from clients' consoles above
                                                //we automatically check for new Profile file uploads from Upload queue
                @Override
                public synchronized void run() { //sync? probably
                    if (checkForNewContent()){
                        MultimediaFile uploadedFile = getNewContent();
                        List<byte[]> chunkList = uploadedFile.splitInChunks();
                        Value chunk;
                        for (int i=0; i < chunkList.size(); i++){ //get all byte arrays, create chunk name and value obj
                            String chunkName = uploadedFile.getFileName().concat(String.format("_%s", i));
                            chunk = new Value("Sending file chunk", chunkName, topic, chunkList.get(i));
                            push(topic, chunk); //if we find new profile content we split it we send it to all users
                        }  //this is probably not needed as profile page will be different on final implementation
                    }
                }
            });
            autoCheck.start();
        }
    }

    public synchronized String searchTopic(){
        System.out.println("Please enter topic: ");
        Scanner scanner = new Scanner(System.in);
        String topic = scanner.nextLine();
        if(!profile.checkSub(topic)){          //check if subbed
            String hash = hashTopic(topic);   //if not, hash and add to profile hashmap
            if (hash!=null){
                profile.sub(hash,topic);
            }
        }
        Value value = new Value("search",topic);
        try {
            objectOutputStream.writeObject(value);
            objectOutputStream.flush();
            Value answer = (Value)objectInputStream.readObject(); //asking and receiving port number for correct Broker based on the topic
            if (Integer.parseInt(answer.getMessage()) != socket.getPort()){ //if we are not connected to the right one, switch conn
                switchConnection( new Socket("localhost",Integer.parseInt(answer.getMessage())));
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        scanner.close();
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
        }
        return null;
    }


    public synchronized void push(String topic, Value value){ //main push function

        try {
            if (value != null){
                objectOutputStream.writeObject(value); // if value is not null write to stream
                objectOutputStream.flush();
            }
            else throw new RuntimeException("File or file chunk corrupted"); //else throw exc
        } catch (IOException e){
            System.out.println(e.getMessage());
        }
    }
}
