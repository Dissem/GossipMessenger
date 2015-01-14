package ch.dissem.mpj.gossip.messageboard;

import ch.dissem.mpj.gossip.messageboard.nodes.FrontEnd;
import ch.dissem.mpj.gossip.messageboard.nodes.ReplicationManager;
import mpi.MPI;

/**
 * Created by chris on 12.01.15.
 */
public class Starter {
    public static void main(String[] args) {
        MPI.Init(args);


        int size = MPI.COMM_WORLD.Size();
        int rank = MPI.COMM_WORLD.Rank();

        int serverThreshold = size / 5;

        if (rank < serverThreshold) {
            new ReplicationManager(size, serverThreshold).start();
        } else {
            new FrontEnd(size, serverThreshold).start();
        }
        MPI.Finalize();
    }
}
