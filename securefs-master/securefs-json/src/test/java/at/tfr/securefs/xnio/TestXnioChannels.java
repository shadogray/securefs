/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.tfr.securefs.xnio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;
import org.junit.Assert;
import org.junit.Test;
import org.xnio.ChannelListener;
import org.xnio.ChannelPipe;
import org.xnio.IoUtils;
import org.xnio.OptionMap;
import org.xnio.Xnio;
import org.xnio.XnioWorker;
import org.xnio.channels.Channels;
import org.xnio.channels.StreamSinkChannel;
import org.xnio.channels.StreamSourceChannel;

/**
 *
 * @author Thomas Frühbeck
 */
public class TestXnioChannels {

    @Test
    public void testXnioSimplePipe() throws Exception {

        Xnio xnio = Xnio.getInstance();
        XnioWorker worker = xnio.createWorker(OptionMap.EMPTY);
        ByteBuffer result = ByteBuffer.allocate(10);

        ChannelPipe<StreamSourceChannel, StreamSinkChannel> pipe = worker.createHalfDuplexPipe();

        pipe.getLeftSide().getReadSetter().set(new ChannelListener<StreamSourceChannel>() {

            @Override
            public void handleEvent(StreamSourceChannel channel) {
                try {
                    final int readBlocking = Channels.readBlocking(channel, result);
                    if (readBlocking > 0) {
                        result.flip();
                        if (result.hasRemaining()) {
                            System.out.println("received: " + new String(Arrays.copyOfRange(result.array(), result.position(), result.limit())));
                        }
                    }
                    if (readBlocking < 0) {
                        IoUtils.safeClose(channel);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    IoUtils.safeClose(channel);
                }
            }
        });
        pipe.getLeftSide().getCloseSetter().set(new ChannelListener<StreamSourceChannel>() {
            @Override
            public void handleEvent(StreamSourceChannel channel) {
                System.out.println("read channel closed");
                synchronized (TestXnioChannels.this) {
                    TestXnioChannels.this.notifyAll();
                }
            }
        });

        pipe.getRightSide().getWriteSetter().set(new ChannelListener<StreamSinkChannel>() {
            private ByteBuffer buff = ByteBuffer.allocate(10);

            @Override
            public void handleEvent(StreamSinkChannel channel) {
                try {
                    System.out.println("write channel: "+new Date());
                    buff.clear();
                    buff.put("hello".getBytes());
                    buff.flip();
                    Channels.writeBlocking(channel, buff);
                    System.out.println("close channel: "+new Date());
                    IoUtils.safeClose(channel);
                } catch (Exception e) {
                    e.printStackTrace();
                    IoUtils.safeClose(channel);
                }
            }
        });

        pipe.getRightSide().getCloseSetter().set(new ChannelListener<StreamSinkChannel>() {
            @Override
            public void handleEvent(StreamSinkChannel channel) {
                System.out.println("write channel closed.");
            }
        });

        pipe.getLeftSide().resumeReads();
        pipe.getRightSide().resumeWrites();
        synchronized (this) {
            this.wait(10000);
        }

        Assert.assertArrayEquals("hello".getBytes(), Arrays.copyOfRange(result.array(), result.position(), result.limit()));
    }

}