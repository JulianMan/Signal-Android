package org.thoughtcrime.redphone.datagraham;

import java.io.IOException;

/**
 * Basic class for forwarding data from one socket to another
 * Created by Julian on 3/19/2016.
 */
public abstract class SocketPipe {

    protected boolean running = true;

    public void start() {
        Runnable run = new Runnable() {
            public void run() {
                while (running)
                {
                    try {
                        byte[] data = receive();
                        System.out.print("HEY HEY ");
                        send(data);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        new Thread(run).start();
    }

    public void stop(){
        running = false;
    }

    protected abstract byte[] receive() throws IOException, InterruptedException;

    protected abstract void send(byte[] data) throws IOException;
}
