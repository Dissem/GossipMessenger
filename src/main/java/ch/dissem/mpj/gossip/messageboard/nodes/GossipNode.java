package ch.dissem.mpj.gossip.messageboard.nodes;

import ch.dissem.mpj.gossip.messageboard.CID;
import ch.dissem.mpj.gossip.messageboard.LogRecord;
import ch.dissem.mpj.gossip.messageboard.Message;
import ch.dissem.mpj.gossip.messageboard.VT;
import ch.dissem.mpj.gossip.messageboard.gossipmessages.GossipMessage;
import ch.dissem.mpj.gossip.messageboard.gossipmessages.Query;
import ch.dissem.mpj.gossip.messageboard.gossipmessages.Update;
import mpi.MPI;

import java.io.Serializable;
import java.util.*;

/**
 * Created by chris on 08.01.15.
 */
public abstract class GossipNode implements Serializable {
    protected final int nodeId;
    protected final int networkSize;
    protected final int numberOfServers;

    protected final VT timestamp;

    /**
     * Hängt von der Reihenfolge der Update-Operationen ab.
     */
    protected List<Message> value;
    /**
     * Spiegelt die Aktualität des aktuellen Zustandes wider.
     * Wird mit jedem Update aktualisiert.
     */
    protected VT valueTS;
    /**
     * Enthält alle Updates, die entweder bereits bearbeitet,
     * aber noch nicht bestätigt wurden oder die noch nicht
     * bearbeitet sind.
     */
    protected final List<LogRecord> updateLog;
    /**
     * Berücksichtigt alle im Update-Log enthaltenen Events.
     */
    protected final VT replicaTS;
    /**
     * Ausgeführte Events, die eindeutig anhand der ID
     * identifiziert und gefunden werden können.
     */
    protected final Set<CID> executedCalls;
    protected final Deque<Query> pendingQueue;

    private List<Thread> listenerThreads = new LinkedList<>();

    public GossipNode(int networkSize, int numberOfServers) {
        this.networkSize = networkSize;
        this.numberOfServers = numberOfServers;

        nodeId = MPI.COMM_WORLD.Rank();

        log(getClass().getSimpleName());

        timestamp = new VT(nodeId);

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

    protected synchronized void receiveMessage(Object message) {
        log("Received", message);
        if (message instanceof Update) {
            receive((Update) message);
        } else if (message instanceof Query) {
            receive((Query) message);
        } else {
            throw new RuntimeException(message.getClass().getSimpleName() + " not handled in " + getClass().getSimpleName());
        }
    }

    protected void send(int node, GossipMessage message) {
        logSizes();
        message.prev = timestamp.clone();

        log("Sent to: " + node, message);

        MPI.COMM_WORLD.Isend(new GossipMessage[]{message}, 0, 1, MPI.OBJECT, node, 0);
    }

    protected void receive(Query q) {
        if (q.value == null) {
            if (q.prev.happenedBeforeOrIs(valueTS)) {
                valueTS.increment();
                Query response = new Query(nodeId, null);
                response.value = value;
                response.valueTS = valueTS;
                send(q.sender, response);
            } else {
                pendingQueue.add(q);
            }
            timestamp.max(q.prev);
        }
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

        send(u.sender, u);

        if (u.prev.happenedBefore(valueTS)) {
            apply(u);
        }
    }

    protected void apply(Update u) {
        executedCalls.add(u.cid);
        value.add(u.value);
        valueTS.max(u.timestamp); // TODO: stimmt das?
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

    protected void log(String tag, Object... objects) {
//        System.out.println("N" + nodeId + ": " + tag + "; " + Stream.of(objects).map(o -> o.getClass().getSimpleName() + ": " + o).collect(joining("; ")));
    }

    protected void logSizes() {
        if (updateLog.size() > 0) {
            System.out.println(getClass().getSimpleName() + " " + nodeId);
            System.out.println("value:\t" + value.size());
            System.out.println("updateLog:\t" + updateLog.size());
            System.out.println("pendingQueue:\t" + pendingQueue.size());
            System.out.println("executedCalls:\t" + executedCalls.size());
        }
    }
}
