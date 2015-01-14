package ch.dissem.mpj.gossip.messageboard.deprecated;

import ch.dissem.mpj.gossip.messageboard.CID;
import ch.dissem.mpj.gossip.messageboard.VT;
import ch.dissem.mpj.gossip.messageboard.gossipmessages.GossipMessage;
import ch.dissem.mpj.gossip.messageboard.gossipmessages.Update;

/**
 * Created by chris on 08.01.15.
 */
public class Receipt extends GossipMessage {
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
