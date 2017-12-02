package com.server;

import com.sun.istack.internal.NotNull;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class ScreenSpyServer extends JFrame implements Runnable {
    private static int SERVER_PORT = 13267;
    private static int INT_SIZE_IN_BYTES = 4;
    private final JLabel jLabel;

    private ScreenSpyServer() throws IOException {
        super("Team-Viewer");
        this.setSize(900, 768);
        this.setVisible(true);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);

        this.jLabel = new JLabel();
        this.add(jLabel, BorderLayout.CENTER);


    }


    @Override
    public void run() {
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(SERVER_PORT);

            Socket socket = serverSocket.accept();
            InputStream inputStream = socket.getInputStream();

            while (!Thread.interrupted()) {
                int size = readImageSize(inputStream);

                System.out.println("Will receive: " + size + " bytes");
                BufferedImage image = readImage(size, inputStream);

                if (image == null) {
                    throw new IllegalStateException("Incomplete image received");
                }

                ImageIcon icon = new ImageIcon(image);
                jLabel.setIcon(icon);
                this.revalidate();
            }

            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int readImageSize(InputStream inputStream) throws IOException {
        byte[] sizeAr = new byte[4];

        for (int i = 0; i < INT_SIZE_IN_BYTES; i++) {
            sizeAr[i] = (byte) inputStream.read();  // Will block if needed

            if (sizeAr[i] == -1) {
                throw new IllegalStateException("End of stream");
            }
        }

        return ByteBuffer.wrap(sizeAr).order(ByteOrder.BIG_ENDIAN).asIntBuffer().get();
    }

    @NotNull
    private static BufferedImage readImage(int imageSize, InputStream inputStream) throws IOException {
        byte[] imageAr = new byte[imageSize];

        for (int i = 0; i < imageSize; i++) {
            imageAr[i] = (byte) inputStream.read();
        }

        return ImageIO.read(new ByteArrayInputStream(imageAr));
    }

    public static void main(String[] args) throws IOException {
        ScreenSpyServer server = new ScreenSpyServer();
        server.run();
    }
}