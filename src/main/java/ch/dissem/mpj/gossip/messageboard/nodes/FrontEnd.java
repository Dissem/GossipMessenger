package ch.dissem.mpj.gossip.messageboard.nodes;

import ch.dissem.mpj.gossip.messageboard.CID;
import ch.dissem.mpj.gossip.messageboard.Message;
import ch.dissem.mpj.gossip.messageboard.VT;
import ch.dissem.mpj.gossip.messageboard.gossipmessages.Query;
import ch.dissem.mpj.gossip.messageboard.gossipmessages.Update;
import mpi.MPI;

/**
 * Created by Christian Basler on 2015-01-01.
 */
public class FrontEnd extends GossipNode {
    private final static int NUMBER_OF_MESSAGES_TO_SEND = Integer.MAX_VALUE;
    private final static long MIN_TIME_BETWEEN_MESSAGES = 1_000; // ms
    private final static long MAX_TIME_BETWEEN_MESSAGES = 6_000; // ms
    private String user;

    private final VT timestamp;

    private int server;

    public FrontEnd(int networkSize, int serverThreshold) {
        super(networkSize, serverThreshold);
        timestamp = new VT(nodeId);
        user = "User " + nodeId;
        server = (int) (Math.random() * numberOfServers);
    }

    @Override
    public void start() {
        super.start();
        for (int i = 0; i < NUMBER_OF_MESSAGES_TO_SEND; i++) {
            wait(MIN_TIME_BETWEEN_MESSAGES, MAX_TIME_BETWEEN_MESSAGES);
            if (Math.random() < 0.5) {
                CID cid = createCID();
                if (value.isEmpty() || Math.random() < 0.2) {
                    send(server, new Update(nodeId, cid, new Message(user, "Topic " + cid, "Message " + cid), timestamp));
                    System.out.println("FE" + nodeId + " sent message " + cid + " to RM" + server);
                } else {
                    Message msg = value.get((int) (value.size() * Math.random()));
                    send(server, new Update(nodeId, cid, new Message(user, msg, "Answer to " + msg.getUser() + " with message " + cid), timestamp));
                }
            } else {
                send(server, new Query(nodeId, timestamp));
                System.out.println("FE" + nodeId + " sent query to RM" + server);
            }
        }
    }

    protected void receive(Query q) {
        if (q.value != null) {
            value = q.value;
            valueTS = q.valueTS;
            timestamp.max(q.valueTS);
            System.out.println("FE" + nodeId + " received reply");
            for (Message m : q.value) {
                System.out.println("\t" + m);
            }
        } else {
            super.receive(q);
        }
    }

    @Override
    protected void receive(Update u) {
        if (u.timestamp != null) {
            timestamp.max(u.timestamp);
        } else {
            super.receive(u);
        }
    }
}
