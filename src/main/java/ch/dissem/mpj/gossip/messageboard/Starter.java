package ch.dissem.mpj.gossip.messageboard;

import mpi.MPI;

/**
 * Created by chris on 12.01.15.
 */
public class Starter {
    public static void main(String[] args) {
        int size = MPI.COMM_WORLD.Size();
        int rank = MPI.COMM_WORLD.Rank();

        if (rank < size / 5) {
            new ReplicationManager().start();
        } else {
            new FrontEnd().start();
        }
    }
}
