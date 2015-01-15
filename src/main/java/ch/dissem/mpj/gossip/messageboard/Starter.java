package ch.dissem.mpj.gossip.messageboard;

import ch.dissem.mpj.gossip.messageboard.nodes.FrontEnd;
import ch.dissem.mpj.gossip.messageboard.nodes.ReplicationManager;
import mpi.MPI;

/**
 * Created by chris on 12.01.15.
 */
public class Starter {
    public static int numberOfServers;

    public static void main(String[] args) {
        MPI.Init(args);


        int size = MPI.COMM_WORLD.Size();
        int rank = MPI.COMM_WORLD.Rank();

        numberOfServers = (size / 4) + 1;

        if (rank < numberOfServers) {
            new ReplicationManager(size, numberOfServers).start();
        } else {
            new FrontEnd(size, numberOfServers).start();
        }
        MPI.Finalize();
    }
}
