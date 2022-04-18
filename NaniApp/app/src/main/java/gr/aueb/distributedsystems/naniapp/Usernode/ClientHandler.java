
import java.io.*;
import java.net.Socket;
import java.util.*;

import static java.lang.Integer.parseInt;

public class ClientHandler implements Runnable, Serializable{

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    public static HashMap<String,byte[]> FileHashMap = new HashMap<>();


    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String clientUsername;
    private static final int CHUNK_KB_SIZE = 512 * 1024;


    public ClientHandler(Socket socket){
        try {
            this.socket = socket;
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
            String topic = (String)readStream(); //I know first one is string
            out.writeObject(socket.getLocalPort());
            out.flush();
            // -------------------------here we need to check if we are the correct broker
            //IT'S IMPORTANT TO DISCONNECT HERE IF WE ARE NOT ON THE CORRECT ONE
            Value initialMessage = (Value)readStream(); //I know second one is value
            this.clientUsername = initialMessage.getUsername();
            clientHandlers.add(this);
            System.out.println("SERVER: " + clientUsername + " has connected!");
        } catch (IOException e) {
            closeEverything(socket, out, in);
        }
    }

    @Override
    public void run() {
        Object streamObject;
        while(!socket.isClosed()){
            streamObject = readStream();
            System.out.println(streamObject);
            if(streamObject!=null){
                if (streamObject instanceof String) {
                    // String -> topic
                    // -------------------------here we check if we are the correct broker based on the topic
                    // if we are not the correct one it's important to disconnect here
                } else if (streamObject instanceof Value value){
                    if (!value.isFile()) {
                        broadcastMessage(value); //if it's not a file just broadcast the message
                    } else {
                        FileHashMap.put(value.getFilename(), value.getChunk()); //if its a chunk add it to the Hashmap
                    }
                }
            }
        }
    }

    public synchronized Object readStream(){ //main reading object method
        try {
            return in.readObject();
        } catch (ClassNotFoundException | IOException e){
            writeFile(); //on exception (client dc) we write the file as a test
            closeEverything(socket, out, in);
            System.out.println(e.getMessage());
        }
        return null;
    }

    //testing method to see if data passing is done correctly.
    public synchronized void writeFile(){
        System.out.println("Writing to file...\n");
        File downloadedFile = new File("C:\\Users\\kosta\\Desktop\\new_download.png");
        try{
            int chunkNumber = 0;
            for (Map.Entry<String, byte[]> entry : FileHashMap.entrySet()) {
                chunkNumber++;
            }
            byte[][] chunkList = new byte[chunkNumber][];
            String filename = "test";
            for (Map.Entry<String, byte[]> entry : FileHashMap.entrySet()){
                if (entry.getKey().contains(filename)){
                    String chunkname = entry.getKey();
                    int index = parseInt(chunkname.substring(chunkname.indexOf(".") - 1, chunkname.indexOf(".")));
                    System.out.println(index);
                    chunkList[index] = entry.getValue();
                }
            }
            FileOutputStream os = new FileOutputStream(downloadedFile);
            for (byte[] b: chunkList){
                os.write(b);
            }
            os.close();
        } catch (IOException e){
            closeEverything(socket, out, in);
            System.out.println("Could not get file!");
        }
    }

    /*public void broadcastFile() { //broadcasts file to all clients
        System.out.println("Broadcasting file initialization...\n");
        String fileName = null;
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (!clientHandler.clientUsername.equals(clientUsername)) {
                    bufferedWriter.write("Heads up! A file is being sent by: " + this.clientUsername);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }


        try{
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            int fileNameLength = dis.readInt();
            if (fileNameLength>0){
                System.out.println("Reading filename....\n");
                byte[] fileNameBytes = new byte[fileNameLength];
                dis.readFully(fileNameBytes,0,fileNameBytes.length);
                fileName = new String(fileNameBytes);
                System.out.println("Reading file " + fileName + "....\n");
                int chunks = dis.readInt();
                int totalSize = chunks*CHUNK_KB_SIZE;
                byte[] fullFile = new byte[totalSize];

                if (chunks > 0) {
                    System.out.println("Reading file with " + chunks + " number of chunks and " + totalSize +" total byte size....\n");
                    for (int i = 1; i <= chunks; i++) {
                        byte[] currentChunk = new byte[CHUNK_KB_SIZE];
                        int readBytes = dis.read(currentChunk,0,currentChunk.length);
                        System.out.println("Read "+ readBytes + " number of bytes\n");
                        System.arraycopy(currentChunk, 0, fullFile, (i-1)*CHUNK_KB_SIZE, currentChunk.length);
                    }
                    FileHashMap.put(fileName,fullFile);
                    System.out.println("File hashed successfully!");
                }
            }
        } catch (IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }

        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (!clientHandler.clientUsername.equals(clientUsername)) {
                    System.out.println("Sending file to client: "+ clientHandler.clientUsername +"...\n");
                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                    if (fileName!= null){
                        byte[] fileNameData = FileHashMap.get(fileName);
                        dos.writeInt(fileNameData.length);
                        dos.write(fileNameData);
                        dos.flush();
                        System.out.println("Wrote bytes: " + fileNameData.length + "...\n");
                    }
                    else{
                        System.out.println("File not found!");
                    }
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }*/



    public void broadcastMessage(Value messageToSend){ //broadcasts message as object to all clients
        for (ClientHandler clientHandler : clientHandlers){
            try {
                if (!clientHandler.clientUsername.equals(clientUsername)){
                    clientHandler.out.writeObject(messageToSend);
                    clientHandler.out.flush();
                }
            } catch (IOException e){
                closeEverything(socket, out, in);
            }
        }
    }

    public void removeClientHandler(){ //disconnects clients
        clientHandlers.remove(this);
        System.out.println("SERVER: " + clientUsername + " has left the chat!");
    }

    public void closeEverything(Socket socket, ObjectOutputStream out, ObjectInputStream in){
        removeClientHandler(); //removes clients and closes everything
        try {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
