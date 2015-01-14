package ch.dissem.mpj.gossip.messageboard.gossipmessages;

import ch.dissem.mpj.gossip.messageboard.VT;

/**
 * Created by chris on 08.01.15.
 */
public class Query extends GossipMessage {

    public Query(int sender, VT vt) {
        this.sender = sender;
        this.prev = vt.clone();
    }
}
