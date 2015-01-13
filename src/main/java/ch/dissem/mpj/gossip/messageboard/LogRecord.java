package ch.dissem.mpj.gossip.messageboard;

/**
 * Created by chris on 08.01.15.
 */
public class LogRecord {
    public final int node;
    public final Update update;

    public LogRecord(int node, Update update) {
        this.node = node;
        this.update = update;
    }
}
