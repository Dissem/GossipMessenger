package ch.dissem.mpj.gossip.messageboard;

/**
 * ch.dissem.mpj.gossip.messageboard.Update is being sent from front end to replication manager.
 */
public class Update implements GossipMessage {
    public final int sender;
    public final VT previous;

    public final CID cid;
    public VT timestamp;
    public final Message value;

    public Update(VT vt, int sender, CID cid, Message value) {
        this.sender = sender;
        this.previous = vt.clone();
        this.cid = cid;
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Update){
            return cid.equals(((Update) obj).cid);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return cid.hashCode();
    }
}
