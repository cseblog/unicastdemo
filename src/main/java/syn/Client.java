package syn;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Client {

    public static void main(String[] args) throws IOException {
        // Create a socket channel and connection to Server at 9995
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("localhost", 9995));


        ByteBuffer outBuf = ByteBuffer.allocate(48);
        Runnable runnable = () -> {

            // Sending data to server every second
            String newData = "Client: Ping " + System.currentTimeMillis();
            outBuf.clear();
            outBuf.put(newData.getBytes(StandardCharsets.UTF_8));
            outBuf.flip();

            while (outBuf.hasRemaining()) {
                try {
                    socketChannel.write(outBuf);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Trying to read data from Server
            ByteBuffer inBuf = ByteBuffer.allocate(48);
            try {
                socketChannel.read(inBuf);
                System.out.println(new String(inBuf.array(), StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(runnable, 0, 1, TimeUnit.SECONDS);
    }
}
