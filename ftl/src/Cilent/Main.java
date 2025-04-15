package Cilent;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {

        int port = 1024;
        Socket socket = new Socket("localhost",port);
        new Enter_Page(socket);
//        new Hall_page(socket,"root");
//        var fw = new PrintWriter(socket.getOutputStream(),true);
//        var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//        new Enter_Page(socket);
    }
}
