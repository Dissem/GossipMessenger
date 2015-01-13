package ch.dissem.mpj.gossip.messageboard;

import mpi.MPI;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.Serializable;
import java.util.*;

/**
 * Created by chris on 08.01.15.
 */
public class GossipNode implements Serializable {
    protected final int nodeId;
    /**
     * Hängt von der Reihenfolge der ch.dissem.mpj.gossip.messageboard.Update-Operationen ab.
     */
    protected final List<Message> value;
    /**
     * Spiegelt die Aktualität des aktuellen Zustandes wider.
     * Wird mit jedem ch.dissem.mpj.gossip.messageboard.Update aktualisiert.
     */
    protected final VT valueTS;
    /**
     * Enthält alle Updates, die entweder bereits bearbeitet,
     * aber noch nicht bestätigt wurden oder die noch nicht
     * bearbeitet sind.
     */
    protected final List<LogRecord> updateLog;
    /**
     * Berücksichtigt alle im ch.dissem.mpj.gossip.messageboard.Update-Log enthaltenen Events.
     */
    protected final VT replicaTS;
    /**
     * Ausgeführte Events, die eindeutig anhand der ID
     * identifiziert und gefunden werden können.
     */
    protected final Set<Update> executedCalls;
    protected final Deque<Query> pendingQueue;

    public GossipNode() {
        nodeId = MPI.COMM_WORLD.Rank();
        executedCalls = new HashSet<>();
        value = new LinkedList<>();
        replicaTS = new VT(nodeId);
        pendingQueue = new LinkedList<>();
        valueTS = new VT(nodeId);
        updateLog = new LinkedList<>();
    }

    public void start() {
        for (int n = 0; n < MPI.COMM_WORLD.Size(); n++) {
            startListening(n);
        }
    }

    protected void startListening(int node) {
        new Thread(() -> {
            while (true) {
                GossipMessage[] buf = new GossipMessage[1];
                MPI.COMM_WORLD.Recv(buf, 0, 1, MPI.OBJECT, node, 0);
                receiveMessage(buf[0]);
            }
        });
    }

    protected void receiveMessage(GossipMessage message) {
        if (message instanceof Update) {
            receive((Update) message);
        } else if (message instanceof Query) {
            receive((Query) message);
        } else {
            throw new NotImplementedException();
        }
    }

    protected void send(int node, GossipMessage message) {
        MPI.COMM_WORLD.Isend(new GossipMessage[]{message}, 0, 1, MPI.OBJECT, node, 0);
    }

    protected void receive(Query q) {
        if (q.previous.happenedBeforeOrIs(replicaTS)) {
            send(q.sender, new ReplicatorMessage(value, valueTS));
        } else {
            pendingQueue.add(q);
        }
    }

    protected void receive(Update u) {
        if (executedCalls.contains(u))
            return;

        replicaTS.increment();

        long[] ts = u.previous.getVector();
        ts[nodeId] = replicaTS.getTime(nodeId);
        u.timestamp = new VT(nodeId, ts);

        updateLog.add(new LogRecord(nodeId, u));

        send(u.sender, new Receipt(u));

        if (u.previous.happenedBefore(valueTS)) {
            value.add(u.value);
            valueTS.max(u.timestamp); // Is this correct?
            executedCalls.add(u);
        }
    }
}
