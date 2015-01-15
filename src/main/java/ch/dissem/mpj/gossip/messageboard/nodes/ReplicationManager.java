package ch.dissem.mpj.gossip.messageboard.nodes;

import ch.dissem.mpj.gossip.messageboard.VT;
import ch.dissem.mpj.gossip.messageboard.gossipmessages.ReplicationMessage;
import ch.dissem.mpj.gossip.messageboard.gossipmessages.Update;
import mpi.MPI;

import static java.lang.Thread.currentThread;

/**
 * Created by Christian Basler on 2015-01-01.
 */
public class ReplicationManager extends GossipNode {
    private final static long MIN_TIME_BETWEEN_MESSAGES = 1_000; // ms
    private final static long MAX_TIME_BETWEEN_MESSAGES = 2_000; // ms

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
                // don't send to self
                if (recipient != nodeId) {
                    send(recipient, new ReplicationMessage(replicaTS, updateLog));
                }
            } else {
                log("update log is empty!");
            }
            wait(MIN_TIME_BETWEEN_MESSAGES, MAX_TIME_BETWEEN_MESSAGES);
        }
    }

    private void receive(ReplicationMessage m) {
        updateLog.addAll(m.log);
        replicaTS.max(m.timestamp);

        updateLog.stream()
                .filter(r -> !executedCalls.contains(r.update.cid))
                .sorted((r1, r2) -> {
                    if (r1.update.prev.happenedBefore(r2.update.prev))
                        return -1;
                    if (r2.update.prev.happenedBefore(r1.update.prev))
                        return 1;
                    return 0;
                })
                .forEach(r -> {
                    if (r.update.prev.happenedBefore(valueTS)) {
                        apply(r.update);
                    }
                });
        VT minTS = valueTS.getMin(m.timestamp);
        int before = updateLog.size();
        updateLog.removeIf(l -> l.update.timestamp.happenedBeforeOrIs(minTS));
        int after = updateLog.size();
        if (after < before) System.out.println("Removed " + (before - after) + " entries!!!!!!!!!!!");
    }
}
