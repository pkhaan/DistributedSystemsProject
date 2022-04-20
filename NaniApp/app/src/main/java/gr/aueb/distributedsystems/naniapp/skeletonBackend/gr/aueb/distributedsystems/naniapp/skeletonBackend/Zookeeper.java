package gr.aueb.distributedsystems.naniapp.skeletonBackend;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


/*
ZooKeeper is used in distributed systems for service synchronization and as a naming registry.
 When working with Apache Kafka, ZooKeeper is primarily used to track the status of nodes in
  the Kafka cluster and maintain a list of Kafka topics and messages.
*/
//##############################################################################################
/*
It is a project of the Apache Software Foundation.
 ZooKeeper is essentially a service for distributed 
 systems offering a hierarchical key-value store,
  which is used to provide a distributed configuration
  service, synchronization service, and naming registry 
  for large distributed systems (see Use cases).
*/

/*So our zookeeper will be tasked as to update
and keep track of any event that takes place in
a our current Data Keeper*/

public class Zookeeper extends Node{

ServerSocket zkServerSocket = null;
Address zkAddr = Node.ZK_ADDRESS;
transient DataKeeper dataKeeper;


//Initially we have the constructor of the Zookeeper
public Zookeeper(){
        //create new InfoTable obj for the system
        dataKeeper = new DataKeeper();
        System.out.println("{ZK_(All)}\\:Booted..........." + zkAddr.toString());
    }

/*In here a new server socket for the zookeeper 
is created and awaits any broker requests.
Whilst the signal is given green the zookeeper proceeds
to run the new thread as per broker request*/

public void initZKSocket(){


try{
    zkServerSocket = new ServerSocket(zkAddr.getPort(), Node.BACKLOG);
    Socket brockerSocket;
    System.out.println("{ZK_(requesting())}\\:Ready to accept new requests..........");
}
while (true) {
                brockerSocket = zkServerSocket.accept();
                Thread brokerThread = new ZooToBrokerConnection(brockerSocket, this);
                brokerThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                zookeeperServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized DataKeeper getDataKeeper() {
        return dataKeeper;
    }

    /**
     * main: creates new Zookeeper obj and calls openZookeeperServer()
     *       so that the Zookeeper starts functioning as it should
     * @param args not used
     */
    public static void main(String[] args) {
        new Zookeeper().initZKSocket();
    }

}


































