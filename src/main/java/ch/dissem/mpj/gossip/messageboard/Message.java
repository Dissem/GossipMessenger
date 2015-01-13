package ch.dissem.mpj.gossip.messageboard;

import java.io.Serializable;

/**
 * Represents a message within the bulletin board.
 * <p/>
 * Created by Christian Basler on 2015-01-01.
 */
public class Message implements Serializable {
    private final long timestamp;
    private final String user;
    private final String title;
    private final String content;

    public Message(String user, String title, String content) {
        this.timestamp = System.currentTimeMillis();
        this.user = user;
        this.title = title;
        this.content = content;
    }

    public Message(String user, Message message, String content) {
        this.timestamp = System.currentTimeMillis();
        this.user = user;

        if (message.getTitle().startsWith("RE:")) {
            this.title = message.getTitle();
        } else {
            this.title = "RE: " + message.getTitle();
        }
        this.content = content;
    }


    public String getUser() {
        return user;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return user + ": " + title + "\n" + content + "\n";
    }
}
