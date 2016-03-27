package org.thoughtcrime.redphone.datagraham;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;

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
        DatagramPacket datagramPacket = new DatagramPacket(new byte[5000],5000);
        datagramSocket.receive(datagramPacket);
        return ByteBuffer.allocate(datagramPacket.getLength())
                .put(datagramPacket.getData(), 0, datagramPacket.getLength())
                .array();
    }

    @Override
    protected void send(byte[] data) throws IOException{
        customSocket.send(data);
    }
}
