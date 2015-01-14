package ch.dissem.mpj.gossip.messageboard;

/**
 * Created by chris on 08.01.15.
 */
public class Receipt implements GossipMessage {
    public final CID messageId;
    public final VT timestamp;

    public Receipt(Update u) {
        this.messageId = u.cid;
        this.timestamp = u.timestamp;
    }

    @Override
    public String toString() {
        return messageId + ": ts=" + timestamp;
    }
}
