package ch.dissem.mpj.gossip.messageboard;

import java.io.Serializable;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

public class VT implements Cloneable, Serializable {
    private final int nodeId;
    private final long[] vector;

    public VT(int node) {
        this.nodeId = node;
        this.vector = new long[Starter.numberOfServers];
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

    public VT getMin(VT other) {
        VT v = new VT(nodeId);
        for (int i = 0; i < vector.length; i++) {
            v.vector[i] = (vector[i] < other.vector[i] ? vector[i] : other.vector[i]);
        }
        return v;
    }

    public long[] getVector() {
        long[] v = new long[vector.length];
        System.arraycopy(vector, 0, v, 0, vector.length);
        return v;
    }

    public synchronized long getTime(int node) {
        return vector[node];
    }

    @Override
    public VT clone() {
        try {
            return (VT) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void increment() {
        if (nodeId<vector.length) {
            vector[nodeId]++;
        }else{
            throw new RuntimeException("Increment from FrontEnd");
        }
    }

    @Override
    public String toString() {
        return "[" + LongStream.of(vector).mapToObj(String::valueOf).collect(joining(":"))+ "]";
    }
}
