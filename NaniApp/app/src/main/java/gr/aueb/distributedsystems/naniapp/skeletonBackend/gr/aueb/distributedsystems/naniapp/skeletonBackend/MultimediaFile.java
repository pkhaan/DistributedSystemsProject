package gr.aueb.distributedsystems.naniapp.skeletonBackend;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;


public class MultimediaFile implements Serializable{

    //file of the Video
    File file;

    //byte array data: stores the read data from FileInputStream
    //for each chunk
    private byte[] data;

    //Metadata
    private ArrayList<String> metadata;

    //ID of this VideoFile chunk
    private int chunkID, data_bytes;

    /**
     * Constructor
     * @param file
     */
    public MultimediaFile(File file) {
        this.file = file;
    }


    /**
     * Constructor for chunk
     * @param data
     * @param metadata
     * @param chunkID
     * @param data_bytes
     */

    public MultimediaFile(byte[] data, ArrayList<String> metadata, int chunkID, int data_bytes) {
        this.data = data;
        this.metadata = metadata;
        this.chunkID = chunkID;
        this.data_bytes = data_bytes;
    }

    /**
     * GETTERS OF DATA
     * @return
     */
    public File getFile() {
        return file;
    }

    public byte[] getData() {
        return data;
    }

    public int getChunkID() {
        return chunkID;
    }



}