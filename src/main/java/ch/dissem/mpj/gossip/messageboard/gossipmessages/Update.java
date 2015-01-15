package ch.dissem.mpj.gossip.messageboard.gossipmessages;

import ch.dissem.mpj.gossip.messageboard.CID;
import ch.dissem.mpj.gossip.messageboard.Message;
import ch.dissem.mpj.gossip.messageboard.VT;

import java.util.List;

/**
 * Update is being sent from front end to replication manager.
 */
public class Update extends GossipMessage {
    public final CID cid;
    public final Message value;
    public VT timestamp;

    public Update(int nodeId, CID cid, Message value, VT valueTS) {
        this.sender = nodeId;
        this.cid = cid;
        this.value = value;
        this.prev = valueTS;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Update) {
            return cid.equals(((Update) obj).cid);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return cid.hashCode();
    }

    @Override
    public String toString() {
        return sender + ": prev=" + prev + "; Message: " + value;
    }
}
