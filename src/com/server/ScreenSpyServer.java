package com.server;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ScreenSpyServer {

    private final static int SOCKET_PORT = 13267;


    public static void main(String[] args) throws Exception {
        OutputStream outputStream = null;
        ServerSocket serverSocket = null;
        Socket sock = null;
        InputStream inputStream = null;

        try {
            serverSocket = new ServerSocket(SOCKET_PORT);
            while (true) {
                System.out.println("Waiting...");
                try {
                    sock = serverSocket.accept();
                    System.out.println("Accepted connection, sending realtime view to this peer: " + sock);

                    BufferedImage image = new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    ImageIO.write(image, "jpg", byteArrayOutputStream);

                    byte[] bytes = byteArrayOutputStream.toByteArray();
                    outputStream = sock.getOutputStream();

                    //System.out.println("Sending " + FILE_TO_SEND + "(" + mybytearray.length + " bytes)");
                    System.out.println("sending image with len: " + bytes.length);

                    outputStream.write(bytes, 0, bytes.length);
                    outputStream.flush();
                    //TimeUnit.SECONDS.sleep(1);

                    //System.out.println("Done.");
                } finally {
                    if (inputStream != null) inputStream.close();
                    if (outputStream != null) outputStream.close();
                    if (sock != null) sock.close();
                }
            }
        } finally {
            if (serverSocket != null) serverSocket.close();
        }
    }
}