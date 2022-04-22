package gr.aueb.distributedsystems.naniapp.skeletonBackend;
import java.io.Serializable;

public class Address implements Serializable {

    //the port that the device listens to
    private int PORT;

    //ip address of device
    private String IP;

    public Address(String ip,int port) {
        this.port = PORT;
        this.ip = IP;
    }

//ACESSORS METHODS FOR USEABILITY
    public int getPort() {
        return PORT;
    }

    public String getIp() {
        return IP;
    }
//Corelletion to see if we have the same port and same ip
    public boolean compare(Address address){
        return (this.getIp().equals(address.getIp()) && this.getPort()==address.getPort()) || (this == address);
    }


    @Override
    public String toString() {
        return "Port: " + this.PORT + " IP: " + this.IP;
    }



}
