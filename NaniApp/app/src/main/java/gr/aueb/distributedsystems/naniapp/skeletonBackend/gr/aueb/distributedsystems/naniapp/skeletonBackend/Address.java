package gr.aueb.distributedsystems.naniapp.skeletonBackend;
import java.io.Serializable;

public class Address implements Serializable {

    //the port that the device listens to
    private int port;

    //ip address of device
    private String ip;

    public Address(String ip,int port) {
        this.port = port;
        this.ip = ip;
    }

//ACESSORS METHODS FOR USEABILITY
    public int getPort() {
        return port;
    }

    public String getIp() {
        return ip;
    }
//Corelletion to see if we have the same port and same ip
    public boolean compare(Address address){
        return (this.getIp().equals(address.getIp()) && this.getPort()==address.getPort()) || (this == address);
    }


    @Override
    public String toString() {
        return "Port: " + this.port + " IP: " + this.ip;
    }



}
