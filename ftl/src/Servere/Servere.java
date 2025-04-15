package Servere;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.*;



public class Servere {

    public static HashMap<String,PrintWriter> user_out = new HashMap<>();

    public static void main(String[] args) {
        int port = 1024;

        // 创建一个线程池，核心线程数和最大线程数均为2，队列大小为5
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
                3,
                5,
                0L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(5),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        // 使用try-with-resources确保ServerSocket在退出时关闭
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("服务器启动，等待客户端连接...");
            while (true) {

                Socket clientSocket = serverSocket.accept();

                System.out.println("新客户端连接：" + clientSocket.getRemoteSocketAddress());
                // 为每个客户端连接创建一个任务，并提交给线程池处理
                threadPool.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭线程池
            threadPool.shutdown();
        }
    }

    // 内部类，用于处理客户端请求
    static class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            String username = null;
            int desk_num = -1;
            // 使用try-with-resources自动关闭输入输出流和Socket
            try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            ) {

//                System.out.println(111);

                    String temp;
                    while ((temp = in.readLine()) != null) {
                        System.out.println(333);
                        JsonObject json = new JsonParser().parse(temp.toString()).getAsJsonObject();


                        System.out.println(json.toString());

                        data_type_manage dtm = new data_type_manage(clientSocket,json);
                        JsonObject response = dtm.mangae();
                        response.addProperty("null","null");
                        out.println(response);

                        if(json.has("username")) {
                            username = json.get("username").getAsString();
                            Servere.user_out.put(username,out);
                        }

                        if(json.has("desk_num")) {
                            desk_num = json.get("desk_num").getAsInt();
                        }

                        System.out.println(response);
                    }


            } catch (IOException e) {
                System.err.println("处理客户端[" + clientSocket.getRemoteSocketAddress() + "]时出错: " + e.getMessage());

            } catch (SQLException e) {
                throw new RuntimeException(e);
            } finally {
                System.out.println(username);
                System.out.println(desk_num);

                game_desk.getInstance().people_leave_desk(username,desk_num);

                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("关闭客户端Socket时出错: " + e.getMessage());
                }
            }
        }
    }
}
