import java.io.Serializable;

public class Value implements Serializable {    //serializable object for all kinds of communication
                                                // with brokers as well as data passing

    private String message, topic, name;
    private MultimediaFile multimediaFile;
    private Profile profile;
    private final boolean dataSharing;

    private byte[] chunk;

    public Value(String message){
        this.message = message;
        this.dataSharing = true;
    }

    public Value(String message, String topic){
        this.message = message;
        this.topic = topic;
        this.dataSharing = true;
    }

    public Value(MultimediaFile file){
        this.multimediaFile = file;
        this.dataSharing = true;
        this.name = file.getFileName();
    }

    public Value(String message, MultimediaFile file){
        this.message = message;
        this.multimediaFile = file;
        this.dataSharing = true;
        this.name = file.getFileName();
    }

    public Value(Profile profile){
        this.profile = profile;
        this.dataSharing = false;
    }

    public Value(byte[] chunk){
        this.chunk = chunk;
        this.dataSharing = true;
    }

    public Value(String name, byte[] chunk){
        this.chunk = chunk;
        this.name = name;
        this.dataSharing = true;
    }

    public Value(String message, Profile profile){
        this.message = message;
        this.profile = profile;
        this.dataSharing = false;
    }

    public Value(String message, String name, String topic, byte[] chunk){
        this.message = message;
        this.chunk = chunk;
        this.dataSharing = true;
        this.name = name;
        this.topic = topic;
    }

    public Value(String message, String name, Profile profile, byte[] chunk){
        this.message = message;
        this.profile = profile;
        this.chunk = chunk;
        this.name = name;
        this.dataSharing = true;
    }

    public Value(String message, Profile profile, MultimediaFile file){
        this.message = message;
        this.profile = profile;
        this.multimediaFile = file;
        this.dataSharing = true;
        this.name = file.getFileName();
    }

    @Override //toString override for printing non data sharing attr of our custom object
    public String toString() {
        return "Value{" +
                "message='" + message + '\'' +
                ", fileName='" + name + '\'' +
                ", profileName='" + profile.getUsername() + '\'' +
                ", dataSharing=" + dataSharing +
                ", topic=" + topic +
                '}';
    }


    public String getMessage() {
        return message;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFileName() {
        return multimediaFile.getFileName();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name= name;
    }

    public String getProfileName() {
        return profile.getUsername();
    }

    public MultimediaFile getMultimediaFile() {
        return multimediaFile;
    }

    public void setMultimediaFile(MultimediaFile multimediaFile) {
        this.multimediaFile = multimediaFile;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public void setChunk(byte[] chunk) {
        this.chunk = chunk;
    }


    public boolean isDataSharing() {
        return dataSharing;
    }

    public byte[] getChunk() {
        return chunk;
    }
}
