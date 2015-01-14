package ch.dissem.mpj.gossip.messageboard.nodes;

import ch.dissem.mpj.gossip.messageboard.CID;
import ch.dissem.mpj.gossip.messageboard.LogRecord;
import ch.dissem.mpj.gossip.messageboard.Message;
import ch.dissem.mpj.gossip.messageboard.VT;
import ch.dissem.mpj.gossip.messageboard.deprecated.Receipt;
import ch.dissem.mpj.gossip.messageboard.gossipmessages.GossipMessage;
import ch.dissem.mpj.gossip.messageboard.gossipmessages.Query;
import ch.dissem.mpj.gossip.messageboard.gossipmessages.Update;
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

    protected final VT timestamp;

    /**
     * Hängt von der Reihenfolge der ch.dissem.mpj.gossip.messageboard.gossipmessages.Update-Operationen ab.
     */
    protected final List<Message> value;
    /**
     * Spiegelt die Aktualität des aktuellen Zustandes wider.
     * Wird mit jedem ch.dissem.mpj.gossip.messageboard.gossipmessages.Update aktualisiert.
     */
    protected final VT valueTS;
    /**
     * Enthält alle Updates, die entweder bereits bearbeitet,
     * aber noch nicht bestätigt wurden oder die noch nicht
     * bearbeitet sind.
     */
    protected final List<LogRecord> updateLog;
    /**
     * Berücksichtigt alle im ch.dissem.mpj.gossip.messageboard.gossipmessages.Update-Log enthaltenen Events.
     */
    protected final VT replicaTS;
    /**
     * Ausgeführte Events, die eindeutig anhand der ID
     * identifiziert und gefunden werden können.
     */
    protected final Set<CID> executedCalls;
    protected final Deque<Query> pendingQueue;

    private List<Thread> listenerThreads = new LinkedList<>();

    public GossipNode(int networkSize, int serverThreshold) {
        this.networkSize = networkSize;
        this.serverThreshold = serverThreshold;

        nodeId = MPI.COMM_WORLD.Rank();
        System.out.println("Node " + nodeId + " is a " + getClass().getSimpleName());

        timestamp = new VT(nodeId);

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

    protected void receiveMessage(Object message) {
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
        message.prev = replicaTS.clone(); // FIXME: oder doch valueTS?

        System.out.println("From: " + nodeId + "; To: " + node + "; " + message.getClass().getSimpleName() + ":\n\t" + message);
        MPI.COMM_WORLD.Isend(new GossipMessage[]{message}, 0, 1, MPI.OBJECT, node, 0);
    }

    protected void receive(Query q) {
        if (q.prev.happenedBeforeOrIs(valueTS)) {
            valueTS.increment();
            send(q.sender, new Update(nodeId, createCID(), value, valueTS));
        } else {
            pendingQueue.add(q);
        }
        timestamp.max(q.prev);
    }

    protected void receive(Update u) {
        if (executedCalls.contains(u.cid))
            return;

        replicaTS.increment();

        long[] ts = u.prev.getVector();
        ts[nodeId] = replicaTS.getTime(nodeId);
        u.timestamp = new VT(nodeId, ts);

        LogRecord logRecord = new LogRecord(nodeId, u.timestamp, u);
        updateLog.add(logRecord);

        // TODO: timestamp wird and die zugehörige FE zurückgegeben, welche ihren Timestamp entsprechend durch die Maximumsbildung anpasst.
        // TODO: Wie?
        send(u.sender, new Receipt(u));

        if (u.prev.happenedBefore(valueTS)) {
            apply(u);
            valueTS.max(u.timestamp); // TODO: stimmt das?
            executedCalls.add(u.cid);
        }
    }

    protected void apply(Update u) {
        value.addAll(u.value);
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

    private long messageId = 0;

    protected CID createCID() {
        return new CID(nodeId, messageId++);
    }
}