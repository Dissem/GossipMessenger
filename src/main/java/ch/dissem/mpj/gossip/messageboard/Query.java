package ch.dissem.mpj.gossip.messageboard;

/**
 * Created by chris on 08.01.15.
 */
public class Query implements GossipMessage {
    public final int sender;
    public final VT previous;

    public Query(VT vt, int sender) {
        this.sender = sender;
        this.previous = vt.clone();
    }
}
