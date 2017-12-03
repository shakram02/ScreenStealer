package com.client;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ScreenSpyClient implements Runnable {

    private static String SERVER_IP = "127.0.0.1";
    private static int SERVER_PORT = 13267;
    private static int INT_SIZE_IN_BYTES = 4;
    private static int SLEEP_MS = 100;
    private Robot bot;
    private Rectangle screenRectangle;

    ScreenSpyClient() throws AWTException {
        bot = new Robot();
        screenRectangle = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());

    }

    @Override
    public void run() {
        OutputStream outputStream = null;
        InputStream inputStream = null;
        Socket socket = new Socket();

        try {
            socket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT));
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();
            // Sending
            while (!Thread.interrupted()) {

                if (inputStream.available() >= 2 * INT_SIZE_IN_BYTES) {
                    int xCoordinate = readInt(inputStream);
                    int yCoordinate = readInt(inputStream);
                    bot.mouseMove(xCoordinate, yCoordinate);

                    System.out.println("Move mouse to: " +
                            xCoordinate + ", " + yCoordinate);
                }

                sendScreenShot(outputStream);
                Thread.sleep(SLEEP_MS);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            e.printStackTrace();
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

        System.out.println("Sent: " + imageSizeBytes + " bytes");
    }

    private ByteArrayOutputStream captureImageToStream() throws IOException {
        BufferedImage image = bot.createScreenCapture(screenRectangle);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", byteArrayOutputStream);
        return byteArrayOutputStream;
    }

    private static int readInt(InputStream inputStream) throws IOException {
        byte[] bytes = new byte[4];
        for (int i = 0; i < INT_SIZE_IN_BYTES; i++) {
            bytes[i] = (byte) inputStream.read();  // Will block if needed

            if (bytes[i] == -1) {
                return -1;
            }
        }

        return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).asIntBuffer().get();
    }

    public static void main(String[] args) throws Exception {
        ScreenSpyClient client = new ScreenSpyClient();
        Thread th = new Thread(client);
        th.start();
        th.join();
    }
}