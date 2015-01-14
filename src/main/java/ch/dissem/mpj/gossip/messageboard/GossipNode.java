package ch.dissem.mpj.gossip.messageboard;

import mpi.MPI;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by chris on 08.01.15.
 */
public class GossipNode implements Serializable {
    protected final int nodeId;
    protected final int networkSize;
    protected final int serverThreshold;
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

    private List<Thread> listenerThreads = new LinkedList<>();

    public GossipNode(int networkSize, int serverThreshold) {
        this.networkSize = networkSize;
        this.serverThreshold = serverThreshold;

        nodeId = MPI.COMM_WORLD.Rank();
        executedCalls = new HashSet<>();
        value = new CopyOnWriteArrayList<>();
        replicaTS = new VT(nodeId);
        pendingQueue = new LinkedList<>();
        valueTS = new VT(nodeId);
        updateLog = new CopyOnWriteArrayList<>();
    }

    public void start() {
        for (int n = 0; n < MPI.COMM_WORLD.Size(); n++) {
            startListening(n);
        }
    }

    public void interrupt() {
        listenerThreads.stream().forEach(Thread::interrupt);
    }

    public void waitForThreads() {
        listenerThreads.stream().forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                // Ignore, we're kinda waiting for this...
            }
        });
    }

    protected void startListening(int node) {
        Thread t = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                GossipMessage[] buf = new GossipMessage[1];
                MPI.COMM_WORLD.Recv(buf, 0, 1, MPI.OBJECT, node, 0);
                receiveMessage(buf[0]);
            }
        });
        listenerThreads.add(t);
        t.start();
    }

    protected void receiveMessage(GossipMessage message) {
        System.out.println("Node " + nodeId + " received message:\n" + message);
        if (message instanceof Update) {
            receive((Update) message);
        } else if (message instanceof Query) {
            receive((Query) message);
        } else {
            throw new NotImplementedException();
        }
    }

    protected void send(int node, GossipMessage message) {
        System.out.println("Node " + nodeId + " sending message:\n" + message);
        MPI.COMM_WORLD.Isend(new GossipMessage[]{message}, 0, 1, MPI.OBJECT, node, 0);
    }

    protected void receive(Query q) {
        if (q.previous.happenedBeforeOrIs(replicaTS)) {
            send(q.sender, new ReplicatorMessage(updateLog, replicaTS));
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

    /**
     * Waits for a random time between min and max milliseconds.
     */
    protected void wait(long min, long max) {
        try {
            Thread.sleep(min + (long) (Math.random() * (max - min)));
        } catch (InterruptedException e) {
            e.printStackTrace();
            interrupt();
        }
    }
}
