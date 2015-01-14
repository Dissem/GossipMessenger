package ch.dissem.mpj.gossip.messageboard.gossipmessages;

import ch.dissem.mpj.gossip.messageboard.LogRecord;
import ch.dissem.mpj.gossip.messageboard.VT;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.joining;

/**
 * Created by chris on 08.01.15.
 */
public class ReplicationMessage extends GossipMessage{
    public final VT timestamp;
    public final List<LogRecord> log;

    public ReplicationMessage(VT ts, List<LogRecord> log) {
        this.timestamp = ts;
        this.log = new ArrayList<>(log);
    }

    @Override
    public String toString() {
        return "ts=" + timestamp + "; log: " + log.stream().map(LogRecord::toString).collect(joining(", "));
    }
}
