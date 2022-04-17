import java.io.Serializable;
import java.util.Arrays;

public class Value implements Serializable {    //serializable object for all kinds of communication
                                                // with brokers as well as data passing

    private String message, username, filename;
    private byte[] chunk;
    private int remainingChunks;
    private final boolean fileSharing;

    public Value(String message){
        this.message = message;
        this.fileSharing = false;
    }

    public Value(String message, String username) {
        this.message = message;
        this.username = username;
        this.fileSharing = false;
    }

    public Value(Value value){
        this.message = value.message;
        this.username = value.username;
        this.filename = value.filename;
        this.remainingChunks = value.remainingChunks;
        this.chunk = Arrays.copyOf(value.chunk,value.chunk.length);
        this.fileSharing = false;
    }

    public Value(String message, String chunkName, int remainingChunks, byte[] chunk){
        this.message = message;
        this.chunk = Arrays.copyOf(chunk,chunk.length);
        this.remainingChunks = remainingChunks;
        this.filename = chunkName;
        this.fileSharing = true;
    }

    @Override //toString override for printing non data sharing attr of our custom object
    public String toString() {
        return "Value{" +
                "message='" + message + '\'' +
                ", fileName='" + filename + '\'' +
                ", profileName='" + username + '\'' +
                ", number of remaining Chunks='" + remainingChunks + '\'' +
                '}';
    }


    public String getMessage() {
        return message;
    }

    public boolean isFile(){
        return this.fileSharing;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUsername() {
        return this.username;
    }

    public int getRemainingChunks() {
        return remainingChunks;
    }

    public String getFilename(){
        return this.filename;
    }

    public void setChunk(byte[] chunk) {
        this.chunk = chunk;
    }

    public byte[] getChunk() {
        return chunk;
    }
}
