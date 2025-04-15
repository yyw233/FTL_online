package Cilent;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class client {
    public static void main(String[] args) throws IOException {

        int port = 1024;
        try( Socket socket = new Socket("127.0.0.1",port); ){
            PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
            Scanner sc = new Scanner(System.in);
            while(true){
                String line = sc.nextLine();
                out.println(line);
            }

        }catch(Exception e){
            e.printStackTrace();
        }

    }
}
