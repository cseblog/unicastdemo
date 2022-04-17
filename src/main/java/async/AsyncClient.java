package async;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AsyncClient {

    public static void main(String[] args) {

        try {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress("localhost", 9995));
            System.out.println("Connected" + socketChannel.isConnected());


            Runnable runnable = () -> {
                ByteBuffer outBuf = ByteBuffer.allocate(48);
                String ping = "Client " + args[0] + ": Ping " + System.currentTimeMillis();
                outBuf.clear();
                outBuf.put(ping.getBytes(StandardCharsets.UTF_8));
                outBuf.flip();

                while (outBuf.hasRemaining()) {
                    try {
                        socketChannel.write(outBuf);
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.exit(-1);
                    }
                }

                ByteBuffer inBuf = ByteBuffer.allocate(48);
                try {
                    socketChannel.read(inBuf);
                    System.out.println(new String(inBuf.array(), StandardCharsets.UTF_8));
                    inBuf.clear();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
            };

            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(runnable, 0, 1, TimeUnit.SECONDS);

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
