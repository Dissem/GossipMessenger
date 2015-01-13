package ch.dissem.mpj.gossip.messageboard;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Created by Christian Basler on 2015-01-01.
 */
public class FrontEnd extends GossipNode {
    private String user;

    private final VT timestamp;

    public FrontEnd() {
        timestamp = new VT(nodeId);
    }

    @Override
    protected void receiveMessage(GossipMessage message) {
        if (message instanceof Receipt) {
            receive((Receipt) message);
        } else {
            super.receiveMessage(message);
        }
    }

    @Override
    public void start() {
        GUI.launch();
        super.start();
    }

    @Override
    protected void receive(Update u) {
        timestamp.max(u.previous);
        super.receive(u);
    }

    protected void receive(Receipt r) {
        timestamp.max(r.timestamp);
    }

    private long messageId = 0;

    private CID createCID() {
        return new CID(nodeId, messageId++);
    }

    private class GUI extends Application {

        @Override
        public void start(Stage primaryStage) throws Exception {
            primaryStage.setTitle("Gossip");

            try {
                // Load root layout from fxml file.
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(GUI.class.getResource("chat.fxml"));
                BorderPane rootLayout = loader.load();

                // Show the scene containing the root layout.
                Scene scene = new Scene(rootLayout);
                primaryStage.setScene(scene);
                primaryStage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
