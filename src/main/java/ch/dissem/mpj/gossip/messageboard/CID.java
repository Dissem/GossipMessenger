package ch.dissem.mpj.gossip.messageboard;

import java.io.Serializable;

/**
 * Created by chris on 08.01.15.
 */
public class CID implements Serializable {
    private final int n;
    private final long m;

    public CID(int n, long m) {
        this.n = n;
        this.m = m;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CID) {
            CID other = (CID) obj;
            return n == other.n && m == other.m;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (int) (n + m);
    }

    @Override
    public String toString() {
        return n + ":" + m;
    }
}
