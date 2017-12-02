package com.client;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.net.Socket;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class ScreenSpyClient extends JFrame {
    private static String SERVER_IP = "127.0.0.1";
    private static int SERVER_PORT = 13267;

    public ScreenSpyClient() {
        super("Team-Viewer");
        this.setSize(900, 900);
        this.setVisible(true);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);

        try {


            JLabel l = new JLabel();
            this.add(l, BorderLayout.CENTER);

            while (true) {
                try {
                    Socket socket = new Socket(SERVER_IP, SERVER_PORT);
                    BufferedImage image = ImageIO.read(socket.getInputStream());
                    if (image != null) {
                        System.out.println("image received!!!!");
                        ImageIcon icon = new ImageIcon(image);
                        l.setIcon(icon);
                        //l=new JLabel(new ImageIcon(image));
                        socket.close();
                        this.revalidate();
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    System.err.println(e);
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            System.err.println(e);
        }
    }

    public static void main(String[] args) {
        new ScreenSpyClient();
    }
}