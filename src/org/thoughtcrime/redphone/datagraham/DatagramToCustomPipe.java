package org.thoughtcrime.redphone.datagraham;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Created by Julian on 3/20/2016.
 */
public class DatagramToCustomPipe extends SocketPipe {
    protected DatagramSocket datagramSocket;
    protected CustomSocket customSocket;

    public DatagramToCustomPipe(DatagramSocket datagramSocket,
                                CustomSocket customSocket){
        this.datagramSocket = datagramSocket;
        this.customSocket = customSocket;
    }

    @Override
    protected byte[] receive() throws IOException{
        DatagramPacket datagramPacket = new DatagramPacket(new byte[1000],1000);
        datagramSocket.receive(datagramPacket);
        return datagramPacket.getData();
    }

    @Override
    protected void send(byte[] data) throws IOException{
        customSocket.send(data);
    }
}
