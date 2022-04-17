package async;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncServer {

    public static void main(String[] arg) {
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(9995));

            Selector selector = Selector.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            ExecutorService threadPool = Executors.newFixedThreadPool(10);


            while (true) {
                int readyChannels = 0;
                try {
                    readyChannels = selector.select();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (readyChannels == 0) {
                    continue;
                }

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    if (key.isAcceptable()) {
                        try {
                            register(selector, serverSocketChannel);
                        } catch (IOException e) {
                            e.printStackTrace();
                            break;
                        }
                    }

                    if (key.isReadable() | key.isWritable()) {
                        SocketChannel channel = (SocketChannel) key.channel();

//                        Runnable runnable = () -> {
                            ByteBuffer inBuf = ByteBuffer.allocate(48);
                            ByteBuffer outBuf = ByteBuffer.allocate(48);
                            if (key.isReadable()) {
                                try {
                                    inBuf.clear();
                                    channel.read(inBuf);
                                    inBuf.flip();
                                    System.out.println(new String(inBuf.array(), StandardCharsets.UTF_8));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    break;
                                }

                                // Write data back to the socket channel
                                if (key.isWritable()) {
                                    String pong = "Server: Pong " + System.currentTimeMillis();
                                    outBuf.clear();
                                    outBuf.put(pong.getBytes(StandardCharsets.UTF_8));
                                    outBuf.flip();
                                    while (outBuf.hasRemaining()) {
                                        try {
                                            channel.write(outBuf);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            break;
                                        }
                                    }
                                }
                            }
//                        };

//                        threadPool.execute(runnable);
                    }
                    keyIterator.remove();
                }
            }
        } catch (ClosedChannelException closedChannelException) {
            closedChannelException.printStackTrace();
        } catch (IOException ioException) {
            ioException.printStackTrace();
            System.exit(-1);
        }


    }

    private static void register(Selector selector, ServerSocketChannel serverSocket)
            throws IOException {
        SocketChannel client = serverSocket.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }

}
