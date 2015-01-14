package ch.dissem.mpj.gossip.messageboard.nodes;

import ch.dissem.mpj.gossip.messageboard.VT;
import ch.dissem.mpj.gossip.messageboard.gossipmessages.ReplicationMessage;
import mpi.MPI;

import static java.lang.Thread.currentThread;

/**
 * Created by Christian Basler on 2015-01-01.
 */
public class ReplicationManager extends GossipNode {
    private final static long MIN_TIME_BETWEEN_MESSAGES = 10000; // ms
    private final static long MAX_TIME_BETWEEN_MESSAGES = 20000; // ms

    public ReplicationManager(int networkSize, int serverThreshold) {
        super(networkSize, serverThreshold);
    }

    @Override
    protected void receiveMessage(Object message) {
        if (message instanceof ReplicationMessage) {
            receive((ReplicationMessage) message);
        } else {
            super.receiveMessage(message);
        }
    }

    @Override
    public void start() {
        super.start();

        while (!currentThread().isInterrupted()) {
            if (!updateLog.isEmpty()) {
                int recipient = (int) (Math.random() * MPI.COMM_WORLD.Size() / 5);
                if (recipient != nodeId) {
                    // don't send to self
                    send(recipient, new ReplicationMessage(timestamp, updateLog));
                }
            } else {
                System.out.println("Node " + nodeId + ": update log is empty!");
            }
            wait(MIN_TIME_BETWEEN_MESSAGES, MAX_TIME_BETWEEN_MESSAGES);
        }
    }

    private void receive(ReplicationMessage m) {
        updateLog.addAll(m.log);
        replicaTS.max(m.timestamp);

        updateLog.stream().filter(r -> !executedCalls.contains(r.update) && r.update.prev.happenedBefore(valueTS)).forEach(r -> {
            executedCalls.add(r.update.cid);
            apply(r.update);
        });
        VT minTS = valueTS.min(m.timestamp);
        updateLog.removeIf(l->l.update.timestamp.happenedBefore(minTS));
    }
}
