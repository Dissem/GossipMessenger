package ch.dissem.mpj.gossip.messageboard.gossipmessages;

import ch.dissem.mpj.gossip.messageboard.VT;

import java.io.Serializable;

/**
 * Base type for any gossip messages, update or query
 */
public class GossipMessage implements Serializable {
    public int sender;
    public VT prev;
}
