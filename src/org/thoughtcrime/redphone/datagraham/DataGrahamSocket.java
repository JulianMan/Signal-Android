package org.thoughtcrime.redphone.datagraham;

/**
 * Created by Julian on 3/19/2016.
 */
public interface DataGrahamSocket {


    public void send(byte[] data);

    public byte[] receive();

    public void close();
}
