package com.server;

import com.sun.istack.internal.NotNull;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class ScreenSpyServer extends JFrame implements Runnable {
    private static int SERVER_PORT = 13267;
    private static int INT_SIZE_IN_BYTES = 4;
    private final JLabel jLabel;
    LinkedBlockingQueue<Integer> userInput;
    Thread inputReader;

    private ScreenSpyServer() {
        super("Team-Viewer");
        this.setSize(900, 768);
        this.setVisible(true);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);

        this.jLabel = new JLabel();
        this.add(jLabel, BorderLayout.CENTER);
        userInput = new LinkedBlockingQueue<>(2);

        inputReader = new Thread(() -> {
            Scanner s = new Scanner(System.in);
            while (!Thread.interrupted()) {
                runInputReader(s);
            }
        });

        inputReader.start();
    }


    private void runInputReader(Scanner s) {
        System.out.println("Enter coordinates separated by a space i.e [250 333] ");
        String input = s.nextLine().trim();

        if (input.length() == 0) {
            inputReader.interrupt();
            return;
        }

        String[] coordinates = input.split(" ");


        for (String coordinate : coordinates) {
            try {
                userInput.put(Integer.parseInt(coordinate));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(SERVER_PORT);

            Socket socket = serverSocket.accept();
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            while (!Thread.interrupted()) {

                if (userInput.size() > 0 && userInput.size() % 2 == 0) {
                    int xCoordinate = userInput.poll();
                    int yCoordinate = userInput.poll();

                    outputStream.write(wrapInt(xCoordinate), 0, INT_SIZE_IN_BYTES);
                    outputStream.write(wrapInt(yCoordinate), 0, INT_SIZE_IN_BYTES);
                }

                int size = readImageSize(inputStream);

                if (size == -1) {
                    System.err.println("Socket closed");
                    return;
                }

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

    /**
     * Reads the size of the image from socket
     *
     * @param inputStream Socket input stream to read data from
     * @return Size of the image or -1 if the socket was closed
     * @throws IOException Socket stream read error
     */
    private static int readImageSize(InputStream inputStream) throws IOException {
        byte[] sizeAr = new byte[4];

        for (int i = 0; i < INT_SIZE_IN_BYTES; i++) {
            sizeAr[i] = (byte) inputStream.read();  // Will block if needed

            if (sizeAr[i] == -1) {
                return -1;
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

    private static byte[] wrapInt(int number) {
        return ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN)
                .putInt(number)
                .array();
    }

    public static void main(String[] args) throws InterruptedException {
        ScreenSpyServer server = new ScreenSpyServer();

        Thread th = new Thread(server);
        th.run();
        th.join();
    }
}