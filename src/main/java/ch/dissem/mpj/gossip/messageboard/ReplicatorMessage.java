package ch.dissem.mpj.gossip.messageboard;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.joining;

/**
 * Created by chris on 08.01.15.
 */
public class ReplicatorMessage implements GossipMessage {
    protected final List<LogRecord> log;
    protected final VT timestamp;

    public ReplicatorMessage(List<LogRecord> log, VT timestamp) {
        this.log = new ArrayList<>(log);
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "ts=" + timestamp + "; log: " + log.stream().map(LogRecord::toString).collect(joining(", "));
    }
}
