
package gr.aueb.distributedsystems.naniapp.skeletonBackend;

public class BrokerB {
    public static void main(String[] args) {
        new Broker(Node.BROKER_ADDRESSES.get(1)).init();
    }
}