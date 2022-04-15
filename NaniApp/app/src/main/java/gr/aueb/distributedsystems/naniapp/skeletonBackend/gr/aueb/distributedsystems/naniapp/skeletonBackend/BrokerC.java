
package gr.aueb.distributedsystems.naniapp.skeletonBackend;
public class BrokerC {
    public static void main(String[] args) {
        new Broker(Node.BROKER_ADDRESSES.get(2)).init();
    }
}


