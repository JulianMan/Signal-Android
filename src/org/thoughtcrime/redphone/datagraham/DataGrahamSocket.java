package org.thoughtcrime.redphone.datagraham;

/**
 * Created by Julian on 3/19/2016.
 */
public class DataGrahamSocket {
    public void send(byte[] data) {

    }

    public byte[] receive() {
        try {
            wait(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void close()
    {
    }
}
