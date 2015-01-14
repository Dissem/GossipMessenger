package ch.dissem.mpj.gossip.messageboard;

import mpi.MPI;

import static java.lang.Thread.currentThread;

/**
 * Created by Christian Basler on 2015-01-01.
 */
public class ReplicationManager extends GossipNode {
    private final static long MIN_TIME_BETWEEN_MESSAGES = 1000; // ms
    private final static long MAX_TIME_BETWEEN_MESSAGES = 1000; // ms

    public ReplicationManager(int networkSize, int serverThreshold) {
        super(networkSize, serverThreshold);
    }

    @Override
    protected void receiveMessage(GossipMessage message) {
        if (message instanceof ReplicatorMessage) {
            receive((ReplicatorMessage) message);
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
                    send(recipient, new ReplicatorMessage(updateLog, replicaTS));
                }
            } else {
                System.out.println("Node " + nodeId + ": update log is empty!");
            }
            try {
                Thread.sleep((long) (500 + 10000 * Math.random()));
            } catch (InterruptedException e) {
                currentThread().interrupt();
            }
        }
    }

    private void receive(ReplicatorMessage m) {
        updateLog.addAll(m.log);
        replicaTS.max(m.timestamp);

        updateLog.stream().filter(r -> !executedCalls.contains(r.update) && r.update.previous.happenedBefore(valueTS)).forEach(r -> {
            executedCalls.add(r.update);
            value.add(r.update.value);
        });
        VT minTS = valueTS.min(m.timestamp);
        executedCalls.removeIf(u -> u.timestamp.happenedBefore(minTS));
    }
}
