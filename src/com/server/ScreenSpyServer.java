package com.server;

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
        this.setSize(900, 900);
        this.setVisible(true);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);

        JLabel l = new JLabel();
        this.add(l, BorderLayout.CENTER);

        ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
        Socket socket = serverSocket.accept();
        InputStream inputStream = socket.getInputStream();
        BufferedImage image;
        byte[] imageAr;
        byte[] sizeAr;

        while (true) {


            sizeAr = new byte[4];

            if (inputStream.available() < 4) {
                Thread.sleep(10);   // Wait a bit
            }

            if (inputStream.read(sizeAr, 0, 4) == -1) {
                break;
            }

            int size = ByteBuffer.wrap(sizeAr).order(ByteOrder.BIG_ENDIAN).asIntBuffer().get();
            System.out.println("Will receive: " + size + " bytes");

            imageAr = new byte[size];
            for (int i = 0; i < size; i++) {
                imageAr[i] = (byte) inputStream.read();
            }
            image = ImageIO.read(new ByteArrayInputStream(imageAr));

            if (image == null) {
                throw new IllegalStateException("Incomplete image received");
            }

            System.out.println("Received " + image.getHeight() + "x" + image.getWidth());
            ImageIcon icon = new ImageIcon(image);
            l.setIcon(icon);
            this.revalidate();
        }

        serverSocket.close();
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        new ScreenSpyServer();
    }
}