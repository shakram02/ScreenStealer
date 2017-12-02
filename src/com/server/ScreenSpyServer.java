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

public class ScreenSpyServer extends JFrame {
    private static int SERVER_PORT = 13267;

    public ScreenSpyServer() throws IOException, InterruptedException {
        super("Team-Viewer");
        this.setSize(900, 768);
        this.setVisible(true);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);

        JLabel l = new JLabel();
        this.add(l, BorderLayout.CENTER);

        ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
        Socket socket = serverSocket.accept();
        InputStream inputStream = socket.getInputStream();

        while (true) {
            int size;

            try {
                size = readImageSize(inputStream);
            } catch (IllegalStateException e) {
                break;
            }

            System.out.println("Will receive: " + size + " bytes");
            BufferedImage image = readImage(size, inputStream);

            if (image == null) {
                throw new IllegalStateException("Incomplete image received");
            }

            ImageIcon icon = new ImageIcon(image);
            l.setIcon(icon);
            this.revalidate();
        }

        serverSocket.close();
    }

    private static int readImageSize(InputStream inputStream) throws IOException, InterruptedException {
        byte[] sizeAr = new byte[4];

        if (inputStream.available() < 4) {
            Thread.sleep(10);   // Wait a bit
        }
        for (int i = 0; i < 4; i++) {
            sizeAr[i] = (byte) inputStream.read();

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

    public static void main(String[] args) throws IOException, InterruptedException {
        new ScreenSpyServer();
    }
}