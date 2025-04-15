package Cilent;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.ArrayList;

public class Hall_page {
    public static void main(String[] args) throws IOException {
        int port = 1024;
        Socket socket = new Socket("localhost",port);

//        new Hall_page(socket,"demo3");
//        new Hall_page(socket,"demo2");
//        new Hall_page(socket,"demo1");
        new Thread(() -> {
            try {
                new Hall_page(socket, "demo3");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
        new Thread(() -> {
            try {
                new Hall_page(socket, "demo2");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
        new Thread(() -> {
            try {
                new Hall_page(socket, "demo1");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    ImageIcon mission = new ImageIcon("image\\poker\\dizhu.png");
    ImageIcon groundimg = new ImageIcon("image/login/halldesk.jpg");

    Socket socket;
    PrintWriter fw = null;
    BufferedReader in = null;
    String username;
    JFrame frame = null;
    desk_people_refresh refresh_people = null;
    Hall_page(Socket socket,String username) throws IOException {

        this.socket = socket;
        this.username = username;
        fw = new PrintWriter(socket.getOutputStream(),true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        frame = new JFrame("欢乐斗地主");
        JLabel background = new JLabel(groundimg);
        frame.setSize(633, 423);
        frame.setLocationRelativeTo(null);
        frame.setIconImage(mission.getImage());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(background);
        frame.setResizable(false);

        // 设置背景颜色
        frame.getContentPane().setBackground(Color.black);
        frame.setLayout(new GridLayout(3,3));

        System.out.println(11);

        ArrayList<Desk> desks = new ArrayList<Desk>();
        for(int i=0;i<9;i++){
            desks.add(new Desk(i,username,fw,in,this));
            frame.add(desks.get(i));
        }

        System.out.println(22);

        refresh_people = new desk_people_refresh(fw,in,desks);
        refresh_people.start();

        System.out.println(33);

        frame.setVisible(true);
    }

    void newdesk(int deskno){
        refresh_people.timer.stop();
        frame.dispose();
        new Poker_desk(socket,username,fw,in,deskno);
    }

    void Show_join_fail(){
        JOptionPane.showMessageDialog(frame, "加入失败");
    }

}



class Desk extends JPanel {
    int Dest_num;
    JLabel l4 = null;
    ImageIcon icon = new ImageIcon("image\\login\\newhouse.png");
    String username;
    PrintWriter fw = null;
    BufferedReader in = null;

    Hall_page hp = null;

    public void set_people(int num) {
        //l4.setText("桌"+Dest_num+'\n'+"当前人数："+num);
        l4.setText("<html>桌" + Dest_num + "<br>人数：" + num + "</html>");
    }

    Desk(int Dest_num,String username,PrintWriter fw,BufferedReader in,Hall_page hp) {
        this.Dest_num = Dest_num;
        this.username = username;
        this.fw = fw;
        this.in = in;
        this.hp = hp;
        this.setLayout(null);
        this.setSize(633/3, 423/3);
        setOpaque(false);

        JButton b1 = new JButton(icon);
        b1.setBounds(46, 28, 64, 64);
        b1.setOpaque(false);           // 设置为非不透明
        b1.setContentAreaFilled(false); // 不填充内容区域
        b1.setBorder(null);
        b1.addActionListener(e);

        l4 = new JLabel();
        l4.setBounds(66,90,80,30);
        l4.setOpaque(false);
        l4.setText("桌"+Dest_num);
        l4.setForeground(Color.white);

        // Add components to the panel
        this.add(l4);
        this.add(b1);
    }

    ActionListener e = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            // TODO: Implement action if needed
            JsonObject sent = new JsonObject();
            sent.addProperty("type","join_desk");
            sent.addProperty("desk_num",Dest_num);
            sent.addProperty("username",username);

            //System.out.println(Dest_num);

            fw.println(sent);

            try {
                JsonObject response = JsonParser.parseString(in.readLine()).getAsJsonObject();

                System.out.println(response);

                boolean success = response.get("is_success").getAsBoolean();
                if( success ){
                    hp.newdesk(Dest_num);
                }else{
                    hp.Show_join_fail();
                }

            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }


        }
    };
}

class desk_people_refresh extends Thread{
    Timer timer = null;
    PrintWriter fw;
    BufferedReader in;
    ArrayList<Desk> desks;
    String username;
    desk_people_refresh(PrintWriter fw, BufferedReader in, ArrayList<Desk> desks) {
        this.fw = fw;
        this.in = in;
        this.desks = desks;
    }

    @Override
    public void run() {
        int delay=100;    //时间间隔，单位为毫秒
        ActionListener taskPerformer=new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e){
                JsonObject sent = new JsonObject();
                sent.addProperty("type","desk_people_query");
                fw.println(sent);

                System.out.println(sent);

                try {
                    JsonObject received = JsonParser.parseString(in.readLine()).getAsJsonObject();

                    System.out.println(received);

                    // 把接受的人数信息放到这个里面
                    for(int i=0;i<9;i++){
                        desks.get(i).set_people(received.get("desk_"+i).getAsInt());
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
        timer = new Timer(delay,taskPerformer);
        timer.start();
    }
}