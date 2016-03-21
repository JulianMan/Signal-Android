package org.thoughtcrime.redphone.datagraham;

import org.thoughtcrime.redphone.network.RtpPacket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by Julian on 3/20/2016.
 */
public class CustomToDatagramPipe extends SocketPipe {
    protected DatagramSocket datagramSocket;
    protected CustomSocket customSocket;
    protected InetAddress inetAddress;
    protected int remotePort;

    protected boolean muteEnabled = false;

    public CustomToDatagramPipe(DatagramSocket datagramSocket,
                                CustomSocket customSocket,
                                    String remoteHost,
                                    int remotePort) throws IOException{
        this(datagramSocket, customSocket, InetAddress.getByName(remoteHost),remotePort);
    }

    public CustomToDatagramPipe(DatagramSocket datagramSocket,
                                CustomSocket customSocket,
                                InetAddress inetAddress,
                                int remotePort){
        this.datagramSocket = datagramSocket;
        this.customSocket =  customSocket;
        this.inetAddress = inetAddress;
        this.remotePort = remotePort;
    }
    @Override
    protected byte[] receive() throws IOException {
        return customSocket.receive(RtpPacket.class).getPacket();
    }

    @Override
    protected void send(byte[] data) throws IOException {
        if(!muteEnabled) {
            DatagramPacket datagramPacket = new DatagramPacket(data, data.length, inetAddress, remotePort);
            datagramSocket.send(datagramPacket);
        }
    }

    // If muteEnabled is true then send(byte[] data) doesn't do anything
    // i.e. data is not send to the datagramSocket
    public void setMuteEnabled(boolean muteEnabled){
        this.muteEnabled = muteEnabled;
    }
}
