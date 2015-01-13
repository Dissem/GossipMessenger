package ch.dissem.mpj.gossip.messageboard;

/**
 * Created by Christian Basler on 2015-01-01.
 */
public class ReplicationManager extends GossipNode {

    public ReplicationManager() {
        super();
    }

    private void receive(ReplicatorMessage m) {
        updateLog.add(m.log);
        replicaTS.max(m.ts);

        updateLog.stream().filter(r -> !executedCalls.contains(r.update) && r.update.previous.happenedBefore(valueTS)).forEach(r -> {
            executedCalls.add(r.update);
            value.add(r.update.value);
        });
        VT minTS = valueTS;//FIXME: .min(something);
        executedCalls.removeIf(u -> u.timestamp.happenedBefore(minTS));
    }

    private static long max(long a, long b) {
        return (a > b ? a : b);
    }
}
