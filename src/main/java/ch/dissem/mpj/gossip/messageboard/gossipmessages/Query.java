package ch.dissem.mpj.gossip.messageboard.gossipmessages;

import ch.dissem.mpj.gossip.messageboard.Message;
import ch.dissem.mpj.gossip.messageboard.VT;

import java.util.List;

/**
 * Created by chris on 08.01.15.
 */
public class Query extends GossipMessage {
    public List<Message> value;
    public VT valueTS;

    public Query(int sender, VT vt) {
        this.sender = sender;
        this.prev = vt;
    }
}
