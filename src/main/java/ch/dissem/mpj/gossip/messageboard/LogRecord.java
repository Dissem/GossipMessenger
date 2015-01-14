package ch.dissem.mpj.gossip.messageboard;

import ch.dissem.mpj.gossip.messageboard.gossipmessages.Update;

import java.io.Serializable;

/**
 * Created by chris on 08.01.15.
 */
public class LogRecord implements Serializable {
    public final int node;
    public final VT timestamp;
    public final Update update;

    public LogRecord(int node, VT timestamp, Update update) {
        this.node = node;
        this.timestamp = timestamp;
        this.update = update;
    }

    @Override
    public String toString() {
        return "Sender: " + node + "; Update: " + update;
    }
}
