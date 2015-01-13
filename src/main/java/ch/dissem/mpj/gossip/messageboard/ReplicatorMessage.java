package ch.dissem.mpj.gossip.messageboard;

import java.util.List;

/**
 * Created by chris on 08.01.15.
 */
public class ReplicatorMessage implements GossipMessage {
    protected final List<Message> value;
    protected final VT valueTS;

    public ReplicatorMessage(List<Message> value, VT valueTS) {
        this.value = value;
        this.valueTS = valueTS;
    }
}
