package ch.dissem.mpj.gossip.messageboard;

import mpi.MPI;

/**
 * Created by Christian Basler on 2015-01-01.
 */
public class FrontEnd extends GossipNode {
    private final static int NUMBER_OF_MESSAGES_TO_SEND = 3;
    private final static long MIN_TIME_BETWEEN_MESSAGES = 1000; // ms
    private final static long MAX_TIME_BETWEEN_MESSAGES = 1000; // ms
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
    protected void receiveMessage(GossipMessage message) {
        if (message instanceof Receipt) {
            receive((Receipt) message);
        } else {
            super.receiveMessage(message);
        }
    }

    @Override
    public void start() {
        super.start();
        for (int i = 0; i < NUMBER_OF_MESSAGES_TO_SEND; i++) {
            wait(MIN_TIME_BETWEEN_MESSAGES, MAX_TIME_BETWEEN_MESSAGES);
            send(server, new Update(timestamp, nodeId, createCID(), new Message(user, "Topic " + messageId, "Message " + messageId)));
        }
    }

    @Override
    protected void receive(Update u) {
        timestamp.max(u.previous);
        super.receive(u);
    }

    protected void receive(Receipt r) {
        timestamp.max(r.timestamp);
    }

    private long messageId = 0;

    private CID createCID() {
        return new CID(nodeId, messageId++);
    }
}
