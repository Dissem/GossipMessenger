package ch.dissem.mpj.gossip.messageboard;

import mpi.MPI;

import java.io.Serializable;

public class VT implements Cloneable, Serializable {
    private final int nodeId;
    private final long[] vector;

    public VT(int node) {
        this.nodeId = node;
        this.vector = new long[MPI.COMM_WORLD.Size()];
    }

    public VT(int node, long[] vector) {
        this(node);
        System.arraycopy(vector, 0, this.vector, 0, vector.length);
    }

    public boolean happenedBefore(VT vt) {
        for (int i = 0; i < vector.length; i++) {
            if (vector[i] > vt.vector[i]) return false;
        }

        return nodeId != vt.nodeId;
    }

    public boolean happenedBeforeOrIs(VT vt) {
        for (int i = 0; i < vector.length; i++) {
            if (vector[i] > vt.vector[i]) return false;
        }
        return true;
    }

    public void max(VT other) {
        for (int i = 0; i < vector.length; i++) {
            vector[i] = (vector[i] > other.vector[i] ? vector[i] : other.vector[i]);
        }
    }

    public VT min(VT other) {
        VT v = new VT(nodeId);
        for (int i = 0; i < vector.length; i++) {
            v.vector[i] = (vector[i] < other.vector[i] ? vector[i] : other.vector[i]);
        }
        return v;
    }

    public CID generateCid() {
        return new CID(nodeId, ++vector[nodeId]);
    }

    public long[] getVector() {
        long[] v = new long[vector.length];
        System.arraycopy(vector, 0, v, 0, vector.length);
        return v;
    }

    public long getTime(int node) {
        return vector[node];
    }

    @Override
    protected VT clone() {
        try {
            return (VT) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public void increment() {
        vector[nodeId]++;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            result.append(i).append(':').append(vector[i]).append(';');
        }
        return result.replace(result.length() - 1, result.length(), "]").toString();
    }
}
