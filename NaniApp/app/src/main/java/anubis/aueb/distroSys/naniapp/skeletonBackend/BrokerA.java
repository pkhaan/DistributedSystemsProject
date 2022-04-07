package anubis.aueb.distroSys.naniapp.skeletonBackend;


public class BrokerA {
    public static void main(String[] args) {
        new Broker(Node.BROKER_ADDRESSES.get(0)).init();
    }
}




