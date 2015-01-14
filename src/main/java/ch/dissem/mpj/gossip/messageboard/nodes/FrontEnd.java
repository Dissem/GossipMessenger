package ch.dissem.mpj.gossip.messageboard.nodes;

import ch.dissem.mpj.gossip.messageboard.CID;
import ch.dissem.mpj.gossip.messageboard.Message;
import ch.dissem.mpj.gossip.messageboard.VT;
import ch.dissem.mpj.gossip.messageboard.deprecated.Receipt;
import ch.dissem.mpj.gossip.messageboard.gossipmessages.Query;
import ch.dissem.mpj.gossip.messageboard.gossipmessages.Update;
import mpi.MPI;

import java.util.Arrays;

/**
 * Created by Christian Basler on 2015-01-01.
 */
public class FrontEnd extends GossipNode {
    private final static int NUMBER_OF_MESSAGES_TO_SEND = Integer.MAX_VALUE;
    private final static long MIN_TIME_BETWEEN_MESSAGES = 10_000; // ms
    private final static long MAX_TIME_BETWEEN_MESSAGES = 60_000; // ms
    private String user;

    private final VT timestamp;

    private int server;

    public FrontEnd(int networkSize, int serverThreshold) {
        super(networkSize, serverThreshold);
        timestamp = new VT(nodeId);
        user = "User " + nodeId;
        server = (int) (Math.random() * MPI.COMM_WORLD.Size() / 4);
    }

    @Override
    public void start() {
        super.start();
        for (int i = 0; i < NUMBER_OF_MESSAGES_TO_SEND; i++) {
            wait(MIN_TIME_BETWEEN_MESSAGES, MAX_TIME_BETWEEN_MESSAGES);
            if (Math.random() < 0.5) {
                CID cid = createCID();
                send(server, new Update(nodeId, cid, Arrays.asList(new Message(user, "Topic " + cid, "Message " + cid)), valueTS));
                System.out.println("FE" + nodeId + " sent message " + cid + " to RM" + server);
            } else {
                send(server, new Query(nodeId, timestamp));
                System.out.println("FE" + nodeId + " sent query to RM" + server);
            }
        }
    }

    @Override
    protected void receive(Update u) {
        System.out.println("FE" + nodeId + " received update " + u);
        for (Message m : u.value) {
            System.out.println("\t" + m);
        }
        System.out.println();

        timestamp.max(u.prev);
        super.receive(u);
    }
}
