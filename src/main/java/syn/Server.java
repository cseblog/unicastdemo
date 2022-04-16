package syn;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class Server {

    public static void main(String[] arg) throws IOException {
        // Create socket channel and listening on port 9995
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(9995));
        SocketChannel socketChannel = serverSocketChannel.accept();

        ByteBuffer inBuf = ByteBuffer.allocate(48);
        ByteBuffer outBuf = ByteBuffer.allocate(48);

        while (true) {
            // Trying to check any messages from clients
            socketChannel.read(inBuf);
            System.out.println(new String(inBuf.array(), StandardCharsets.UTF_8));
            inBuf.clear();

            // Write an message back to the client
            String pong = "Server: Pong " + System.currentTimeMillis();
            outBuf.clear();
            outBuf.put(pong.getBytes(StandardCharsets.UTF_8));
            outBuf.flip();
            while (outBuf.hasRemaining()) {
                socketChannel.write(outBuf);
            }
        }
    }
}
