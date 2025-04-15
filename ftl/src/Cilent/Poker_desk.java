package Cilent;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;


public class Poker_desk {
//    public static void main(String[] args) throws IOException {
//        int port = 1024;
//        Socket socket = new Socket("localhost",port);
//        PrintWriter fw = new PrintWriter(socket.getOutputStream(),true);
//        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//
//
//        new Poker_desk(socket,"aa",fw,in,0);
//    }

    if_can_beat_light_button chupai_judge = new if_can_beat_light_button(this);
    state_refresh s_f = null;
    Socket socket;
    int desk_num; // 构造传入
    JFrame frame;
    int landlord = 0;

    // 用户名信息
    String username = null;
    String left_username = null;
    String right_username = null;

    // 用户积分信息
    int my_score = 0;
    int left_score = 0;
    int right_score = 0;

    // 用户准备信息
    boolean is_left_ok = false;
    boolean is_right_ok = false;

    // 叫分按钮
    JButton zeroButton = null;
    JButton oneButton = null;
    JButton twoButton = null;
    JButton threeButton = null;

    // 叫了多少分的标签
    JLabel my_call_score_label = null;
    JLabel left_call_score_label = null;
    JLabel right_call_score_label = null;

    // 过牌标记
    JLabel my_lord_mark_pass = null;
    JLabel lf_lord_mark_pass = null;
    JLabel rt_lord_mark_pass = null;

    // 地主标记
    JLabel my_lord_mark = null;
    JLabel lf_lord_mark = null;
    JLabel rt_lord_mark = null;

    // 我的卡牌有哪些
    ArrayList<String> my_pokes = new ArrayList<>();

    // 我现在选中的卡牌
    ArrayList<String> now_select_cards = new ArrayList<>();
    ArrayList<Poker> now_select_pokers_label = new ArrayList();

    // 我现在需要击败的牌
    ArrayList<String> now_should_beat_pokes = new ArrayList<>();

    // 装我的卡牌的容器
    ArrayList<Poker> my_pokes_container = new ArrayList<>();

    // 出牌容器
    // 我的出牌容器
    ArrayList<String> my_chupai = new ArrayList<>();
    ArrayList<Poker> my_chupai_container = new ArrayList<>();
    //左边玩家的出牌容器
    ArrayList<String> left_chupai = new ArrayList<>();
    ArrayList<Poker> left_chupai_container = new ArrayList<>();
    //右边玩家的出牌容器
    ArrayList<String> right_chupai = new ArrayList<>();
    ArrayList<Poker> right_chupai_container = new ArrayList<>();

    // 上面的地主牌
    JLabel top1 = null;
    JLabel top2 = null;
    JLabel top3 = null;


    // 用户信息标签
    JLabel left_data = null;
    JLabel left_data2 = null;
    JLabel right_data = null;
    JLabel right_data2 = null;
    JLabel my_data = null;

    // 翻面扑克
    JLabel lf_re_pk = null;
    JLabel rt_re_pk = null;

    // 出牌按钮
    JButton buchuButton = null;
    JButton chupaiButton = null;

    // 准备按钮
    JButton ready_button = null;

    // 用户牌数
    int lf_card_count = 0;
    int rt_card_count = 0;


    PrintWriter fw = null;
    BufferedReader in = null;
    boolean ready_button_state = false;


    // Icon
    ImageIcon mission = new ImageIcon("image\\poker\\dizhu.png");
    ImageIcon background = new ImageIcon("image/login/desk.jpg");
    ImageIcon ready = new ImageIcon("image/desk/ready.png");
    ImageIcon exit = new ImageIcon("image/desk/mexit.png");
    ImageIcon unready = new ImageIcon("image/desk/unready.png");
    ImageIcon ok = new ImageIcon("image/desk/ok.png");
    ImageIcon chupai = new ImageIcon("image/desk/chupai.png");
    ImageIcon buchu = new ImageIcon("image/desk/buchu.png");
    ImageIcon rear_pic = new ImageIcon("image/poker/rear.png");
    ImageIcon zero = new ImageIcon("image/desk/0.png");
    ImageIcon one = new ImageIcon("image/desk/1.png");
    ImageIcon two = new ImageIcon("image/desk/2.png");
    ImageIcon three = new ImageIcon("image/desk/3.png");
    String rear_path = "image/poker/rear.png";


    // 准备标签
    JLabel oj_lbl_me = null;
    JLabel ok_lbl_left = null;
    JLabel ok_lbl_right = null;

    // 我 左右玩家 在服务器桌面中对应的位置
    int my_pos = 0;
    int left_pos = 0;
    int right_pos = 0;


    JButton exit_button = null;

    Boolean can_beat = false;

    public Poker_desk(Socket socket, String username, PrintWriter fw, BufferedReader in, int desk_num) {
        this.socket = socket;
        this.username = username;
        this.fw = fw;
        this.in = in;
        this.desk_num = desk_num;

        s_f = new state_refresh(fw,in,username,desk_num,this);
        s_f.start();

        frame = new JFrame("欢乐斗地主");
        frame.setSize(633, 423);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setIconImage(mission.getImage());
        JLabel backgroundLabel = new JLabel(background);
        frame.setContentPane(backgroundLabel);
        frame.setLayout(null);
        frame.setResizable(false);

        // 准备和取消准备
        ready_button = new JButton(ready);
        ready_button.setBorderPainted(false);
        ready_button.setContentAreaFilled(false);
        ready_button.setFocusPainted(false);
        ready_button.setBounds(210,250,187,83);
        ready_button_state = false;
        frame.add(ready_button);

        // 按下准备
        ready_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                JsonObject sent = new JsonObject();
                sent.addProperty("username", username);
                sent.addProperty("desk_num", desk_num);
                ready_button_state = !ready_button_state;
                if (ready_button_state) {
                    sent.addProperty("type", "ready");
                    ready_button.setIcon(unready);
                    oj_lbl_me.setVisible(true);
                }else {
                    sent.addProperty("type", "unready");
                    ready_button.setIcon(ready);
                    oj_lbl_me.setVisible(false);
                }
                fw.println(sent);
                try {
                    JsonObject response = JsonParser.parseString(in.readLine()).getAsJsonObject();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        // 退出
        exit_button = new JButton(exit);
        exit_button.setBorderPainted(false);
        exit_button.setContentAreaFilled(false);
        exit_button.setFocusPainted(false);
        exit_button.setBounds(0,0,41,40);
        frame.add(exit_button);
        exit_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JsonObject sent = new JsonObject();
                sent.addProperty("username", username);
                sent.addProperty("type", "exit");
                sent.addProperty("desk_num",desk_num);
                fw.println(sent);

                System.out.println(sent);

                try {
                    JsonObject response = JsonParser.parseString(in.readLine()).getAsJsonObject();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                try {
                    new Hall_page(socket,username);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                frame.dispose();
            }
        });

        // okkk

        // 1 我
        oj_lbl_me = new JLabel(ok);
        oj_lbl_me.setBounds(280,220,40,40);
        oj_lbl_me.setVisible(false);

        // 2 左边玩家
        ok_lbl_left = new JLabel(ok);
        ok_lbl_left.setBounds(90,160,40,40);
        ok_lbl_left.setVisible(false);

        // 3 右边玩家
        ok_lbl_right = new JLabel(ok);
        ok_lbl_right.setBounds(473,160,40,40);
        ok_lbl_right.setVisible(false);

        frame.add(oj_lbl_me);
        frame.add(ok_lbl_left);
        frame.add(ok_lbl_right);


        // 个人资料
        // 下面的个人资料
        my_data = new JLabel(username+"   "+my_score);
        my_data.setForeground(Color.white);
        my_data.setFont(new Font(null, Font.BOLD, 14));
        my_data.setBounds(40, 350, 150, 40);

        frame.add(my_data);

        //左边的个人资料
        left_data = new JLabel(left_username);
        left_data2 = new JLabel("积分："+left_score);
        left_data.setForeground(Color.white);
        left_data2.setForeground(Color.white);
        left_data.setFont(new Font(null, Font.BOLD, 14));
        left_data2.setFont(new Font(null, Font.BOLD, 14));
        left_data.setBounds(0, 100, 100, 20);
        left_data2.setBounds(0,120, 100, 20);

        frame.add(left_data);
        frame.add(left_data2);


        //left_data.setAu


        // 右边的个人资料

        right_data = new JLabel(right_username);
        right_data.setHorizontalAlignment(SwingConstants.RIGHT);
        right_data2 = new JLabel("积分："+right_score);
        right_data2.setHorizontalAlignment(SwingConstants.RIGHT);
        right_data.setForeground(Color.white);
        right_data2.setForeground(Color.white);
        right_data.setFont(new Font(null, Font.BOLD, 14));
        right_data2.setFont(new Font(null, Font.BOLD, 14));
        right_data.setBounds(500, 100, 100, 20);
        right_data2.setBounds(500, 120, 100, 20);

        frame.add(right_data);
        frame.add(right_data2);


        // 翻面扑克
        lf_re_pk = new JLabel();
        new testImage(rear_path, lf_re_pk,(int) (61*0.8),(int)(96*0.8));
        rt_re_pk = new JLabel();
        new testImage(rear_path, rt_re_pk,(int) (61*0.8),(int)(96*0.8));
        lf_re_pk.setBounds(1,150,(int) (61*0.8),(int)(96*0.8));
        rt_re_pk.setBounds(570,150,(int) (61*0.8),(int)(96*0.8));

        lf_re_pk.setForeground(Color.white);
        rt_re_pk.setForeground(Color.white);

        lf_re_pk.setVisible(false);
        rt_re_pk.setVisible(false);

        frame.add(lf_re_pk);
        frame.add(rt_re_pk);


        // 出牌按钮
        buchuButton = new JButton(buchu);
        chupaiButton = new JButton(chupai);
        chupaiButton.setBounds(310, 200, 85, 37);
        chupaiButton.setContentAreaFilled(false);
        chupaiButton.setFocusPainted(false);
        chupaiButton.setBorderPainted(false);

        chupaiButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO: 处理出牌按钮点击事件
                if(can_beat && !now_select_cards.isEmpty()){
                    JsonObject sent = new JsonObject();
                    sent.addProperty("username", username);
                    sent.addProperty("desk_num", desk_num);
                    sent.addProperty("type", "chupai");
                    sent.addProperty("cards", poke_to_string(now_select_cards));
                    
                    // 把出的牌从我的牌里移除
                    for(String poke : now_select_cards){
                        my_pokes.remove(poke);
                    }
                    // 出牌容器显示出牌
                    set_my_out_card_on_desk(now_select_cards);

                    // 我的牌容器显示我的牌
                    show_my_hand_card(my_pokes);

                    // 清空现在选中的牌
                    now_select_cards.clear();

                    fw.println(sent);

                    try {
                        JsonObject response = JsonParser.parseString(in.readLine()).getAsJsonObject();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }

                    chupai_judge.running = false;
                    chupaiButton.setVisible(false);
                    buchuButton.setVisible(false);

                }else{
                    // do nothing
                }
            }
        });

        buchuButton.setBounds(190, 200, 85, 37);
        buchuButton.setContentAreaFilled(false);
        buchuButton.setFocusPainted(false);
        buchuButton.setBorderPainted(false);

        buchuButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO: 处理不出牌按钮点击事件
                JsonObject sent = new JsonObject();
                sent.addProperty("username", username);
                sent.addProperty("desk_num", desk_num);
                sent.addProperty("type", "pass");
                fw.println(sent);

                try {
                    JsonObject response = JsonParser.parseString(in.readLine()).getAsJsonObject();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

                chupai_judge.running = false;

                chupaiButton.setVisible(false);
                buchuButton.setVisible(false);

            }

        });

        frame.add(chupaiButton);
        frame.add(buchuButton);

        chupaiButton.setVisible(false);
        buchuButton.setVisible(false);
        

        // 上面的地主牌

        top1 = new JLabel();
        new testImage(rear_path,top1, (int) (71*0.8),(int)(96*0.8));
        top1.setBounds(213,0,(int) (71*0.8),(int)(96*0.8));
        frame.add(top1);
        top1.setVisible(false);

        top2 = new JLabel();
        new testImage(rear_path,top2, (int) (71*0.8),(int)(96*0.8));
        top2.setBounds(270,0,(int) (71*0.8),(int)(96*0.8));
        frame.add(top2);
        top2.setVisible(false);

        top3 = new JLabel();
        new testImage(rear_path,top3, (int) (71*0.8),(int)(96*0.8));
        top3.setBounds(327,0,(int) (71*0.8),(int)(96*0.8));
        frame.add(top3);
        top3.setVisible(false);

        // 地主标记
        ImageIcon lord_mark = new ImageIcon("image/desk/lord_mark.png");
        my_lord_mark = new JLabel(lord_mark);
        lf_lord_mark = new JLabel(lord_mark);
        rt_lord_mark = new JLabel(lord_mark);

        my_lord_mark.setBounds(0,320,(29),(66));
        lf_lord_mark.setBounds(49,140,(29),(66));
        rt_lord_mark.setBounds(545,140,(29),(66));

        frame.add(my_lord_mark);
        frame.add(lf_lord_mark);
        frame.add(rt_lord_mark);

        my_lord_mark.setVisible(false);
        lf_lord_mark.setVisible(false);
        rt_lord_mark.setVisible(false);

        // 过牌标记

        ImageIcon lord_mark_pass = new ImageIcon("image/desk/pass.png");
        my_lord_mark_pass = new JLabel(lord_mark_pass);
        lf_lord_mark_pass = new JLabel(lord_mark_pass);
        rt_lord_mark_pass = new JLabel(lord_mark_pass);

        my_lord_mark_pass.setBounds(275,240,(56),(58));
        lf_lord_mark_pass.setBounds(95,150,(56),(58));
        rt_lord_mark_pass.setBounds(480,150,(56),(58));

        frame.add(my_lord_mark_pass);
        frame.add(lf_lord_mark_pass);
        frame.add(rt_lord_mark_pass);

        my_lord_mark_pass.setVisible(false);
        lf_lord_mark_pass.setVisible(false);
        rt_lord_mark_pass.setVisible(false);

        // 选分叫地主
        zeroButton = new JButton(zero);
        zeroButton.setBorderPainted(false);
        zeroButton.setContentAreaFilled(false);
        zeroButton.setFocusPainted(false);
        zeroButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO: 处理0分按钮点击事件
                JsonObject sent = new JsonObject();
                sent.addProperty("type", "call");
                sent.addProperty("username", username);
                sent.addProperty("desk_num", desk_num);
                sent.addProperty("score", 0);
                fw.println(sent);

                my_call_score_label.setIcon(zero);
                close_lood_button();

                try {
                    JsonObject response = JsonParser.parseString(in.readLine()).getAsJsonObject();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        oneButton = new JButton(one);
        oneButton.setBorderPainted(false);
        oneButton.setContentAreaFilled(false);
        oneButton.setFocusPainted(false);
        oneButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO: 处理1分按钮点击事件
                JsonObject sent = new JsonObject();
                sent.addProperty("type", "call");
                sent.addProperty("username", username);
                sent.addProperty("desk_num", desk_num);
                sent.addProperty("score", 1);
                fw.println(sent);

                my_call_score_label.setIcon(one);
                close_lood_button();

                try {
                    JsonObject response = JsonParser.parseString(in.readLine()).getAsJsonObject();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        twoButton = new JButton(two);
        twoButton.setBorderPainted(false);
        twoButton.setContentAreaFilled(false);
        twoButton.setFocusPainted(false);
        twoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {    
                // TODO: 处理2分按钮点击事件
                JsonObject sent = new JsonObject();
                sent.addProperty("type", "call");
                sent.addProperty("username", username);
                sent.addProperty("desk_num", desk_num);
                sent.addProperty("score", 2);
                fw.println(sent);

                my_call_score_label.setIcon(two);
                close_lood_button();

                try {
                    JsonObject response = JsonParser.parseString(in.readLine()).getAsJsonObject();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        threeButton = new JButton(three);
        threeButton.setBorderPainted(false);
        threeButton.setContentAreaFilled(false);
        threeButton.setFocusPainted(false);
        threeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO: 处理3分按钮点击事件
                JsonObject sent = new JsonObject();
                sent.addProperty("type", "call");
                sent.addProperty("username", username);
                sent.addProperty("desk_num", desk_num);
                sent.addProperty("score", 3);
                fw.println(sent);

                my_call_score_label.setIcon(three);
                close_lood_button();

                try {
                    JsonObject response = JsonParser.parseString(in.readLine()).getAsJsonObject();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }); 


        int x_pos = 74; int start_pos = 184;
        zeroButton.setBounds(160, 225, 44, 26);
        oneButton.setBounds(160+x_pos, 225, 44, 26);
        twoButton.setBounds(160+2*x_pos, 225, 44, 26);
        threeButton.setBounds(160+3*x_pos, 225, 44, 26);
        zeroButton.setVisible(false);
        oneButton.setVisible(false);
        twoButton.setVisible(false);
        threeButton.setVisible(false);

        frame.add(zeroButton);
        frame.add(oneButton);
        frame.add(twoButton);
        frame.add(threeButton);

        // 叫了多少分的标签
        my_call_score_label = new JLabel();
        left_call_score_label = new JLabel();
        right_call_score_label = new JLabel();

        my_call_score_label.setBounds(275,240,45,26);
        left_call_score_label.setBounds(95,150,45,26);
        right_call_score_label.setBounds(480,150,45,26);

        frame.add(my_call_score_label);
        frame.add(left_call_score_label);
        frame.add(right_call_score_label);

//        ArrayList<String> a = new ArrayList<>();
//        a.add("1-1");
//        a.add("2-1");
//        set_my_out_card_on_desk(a);

        //clear_out_container(my_chupai_container);


        frame.setVisible(true);
    }

    // 显示我的手牌
    void show_my_hand_card(ArrayList<String> pokes) {
        // 移除并清空原有组件
        for (Poker p : my_pokes_container) {
            frame.remove(p);
        }
        my_pokes_container.clear();

        int dis = 17;
        int start_pos = (633 - 71 - dis * pokes.size()) / 2;
        int y = 265;
        int i = pokes.size() - 1;

        // 倒序遍历 pokes
        for (int j = pokes.size() - 1; j >= 0; j--) {
            String poke = pokes.get(j);
            Poker pk = new Poker(poke, true,1.0,this); // 缩放倍数 1
            my_pokes_container.add(pk);
            pk.setBounds(start_pos + i * dis, y, 71, 96);
            i--;
            frame.add(pk);
        }

        frame.revalidate();
        frame.repaint();
    }

    int left_call_score = 0;
    int right_call_score = 0;

    // 显示抢分按钮
    void show_lood_button(){
        if((left_call_score == right_call_score) && left_call_score == 0){
            oneButton.setVisible(true);
            twoButton.setVisible(true);
            threeButton.setVisible(true);
            return;
        }
        int mx = Math.max(left_call_score,right_call_score);
        if( mx == 1 ){
            zeroButton.setVisible(true);
            twoButton.setVisible(true); 
            threeButton.setVisible(true);
        }
        if( mx == 2 ){
            zeroButton.setVisible(true);
            threeButton.setVisible(true);
        }
    }
    void close_lood_button(){
        zeroButton.setVisible(false);
        oneButton.setVisible(false);
        twoButton.setVisible(false);
        threeButton.setVisible(false);
    }

    // 新游戏
    void new_game(){
        frame.dispose();
        s_f.timer.stop();
        new Poker_desk(socket,username,fw,in,desk_num);
    }

    private static class CardComparator implements Comparator<String> {
        private final Map<String, Integer> cardRank;

        public CardComparator(Map<String, Integer> cardRank) {
            this.cardRank = cardRank;
        }

        @Override
        public int compare(String card1, String card2) {
            // 提取牌的等级键：王牌用整个字符串，普通牌用"-"后的部分
            String key1 = card1.startsWith("5") ? card1 : card1.split("-")[1];
            String key2 = card2.startsWith("5") ? card2 : card2.split("-")[1];

            // 获取等级值
            int rank1 = cardRank.get(key1);
            int rank2 = cardRank.get(key2);

            // 反着排序

            // 比较等级
            if (rank1 != rank2) {
                return Integer.compare(rank2, rank1); // 等级从小到大
            }

            // 等级相同，比较花色
            int suit1 = Integer.parseInt(card1.split("-")[0]);
            int suit2 = Integer.parseInt(card2.split("-")[0]);
            return Integer.compare(suit2, suit1); // 花色从小到大
        }
    }

    // 添加排序方法
    public void sortPokes() {
        Map<String, Integer> cardRank = PokerComparator.getCardRank();
        my_pokes.sort((Comparator<? super String>) new CardComparator(cardRank));
    }



    // 刷新出牌区


    // 放出牌的
    double out_size = 0.6;
    public void clear_out_container(ArrayList<Poker> pokers) {
        if (pokers.size() == 0) {return;}
        for (Poker p : pokers) {
            frame.remove(p);
        }
        pokers.clear();
        frame.revalidate();
        frame.repaint();
    }
    public void set_my_out_card_on_desk(ArrayList<String> pokes) {
        // 我出牌
        for(String poke : pokes) {
            Poker pk = new Poker(poke, false,out_size,this);
            my_chupai_container.add(pk);
        }

        int dis = (int) (17 * out_size);
        int start_pos = (633 - (int)(71 * out_size) - dis * pokes.size()) / 2;
        int y = 240;
        int i = pokes.size() - 1;

        // 倒序遍历 pokes
        for (int j = pokes.size() - 1; j >= 0; j--) {
            Poker pk = my_chupai_container.get(j); // 缩放倍数 0.6
            pk.setBounds(start_pos + i * dis, y, (int)(71*out_size), (int)(96*out_size));
            i--;
            frame.add(pk);
        }

        frame.revalidate();
        frame.repaint();
    }
    public void set_left_out_card_on_desk(ArrayList<String> pokes){
        clear_out_container(left_chupai_container);

        if( pokes != null ){
            // 左边出牌
            for(String poke : pokes) {
                Poker pk = new Poker(poke, false,out_size,this);
                left_chupai_container.add(pk);
            }

            int dis = (int) (17 * out_size);
            int start_pos = 95;
            int y = 150;
            int i = pokes.size() - 1;

            // 倒序遍历 pokes
            for (int j = pokes.size() - 1; j >= 0; j--) {
                Poker pk = left_chupai_container.get(j);
                pk.setBounds(start_pos + i * dis, y, (int)(71*out_size), (int)(96*out_size));
                i--;
                frame.add(pk);
            }
        }

        frame.revalidate();
        frame.repaint();
    }
    public void set_right_out_card_on_desk(ArrayList<String> pokes){
        clear_out_container(right_chupai_container);
        if (pokes == null) return;
        for(String poke : pokes) {
            Poker pk = new Poker(poke, false,out_size,this);
            right_chupai_container.add(pk);
        }

        int dis = (int) (17 * out_size);
        int start_pos = 480;
        int y = 150;
        int i = 0;

        // 倒序遍历 pokes
        for (int j = pokes.size() - 1; j >= 0; j--) {
            Poker pk = right_chupai_container.get(j);
            pk.setBounds(start_pos - i * dis, y, (int)(71*out_size), (int)(96*out_size));
            i++;
            frame.add(pk);
        }

        frame.revalidate();
        frame.repaint();
    }

    // 放顶部地主牌
    void set_top_card(ArrayList<String> pokes){
        top1.setIcon(new ImageIcon("image/poker/"+ pokes.get(0) + ".png"));
        new testImage("image/poker/"+ pokes.get(0) + ".png",top1,(int) (71*0.8),(int)(96*0.8));

        top2.setIcon(new ImageIcon("image/poker/"+ pokes.get(1) + ".png"));
        new testImage("image/poker/"+ pokes.get(1) + ".png",top2,(int) (71*0.8),(int)(96*0.8));

        top3.setIcon(new ImageIcon("image/poker/"+ pokes.get(2) + ".png"));
        new testImage("image/poker/"+ pokes.get(2) + ".png",top3,(int) (71*0.8),(int)(96*0.8));
    }

    public void refresh_prepare_state(){
        // 准备状态
        if(is_left_ok) ok_lbl_left.setVisible(true);
        else ok_lbl_left.setVisible(false);
        if(is_right_ok) ok_lbl_right.setVisible(true);
        else ok_lbl_right.setVisible(false);

        // 用户信息
        left_data.setText(left_username != null ? left_username : "等待玩家");
        right_data.setText(right_username != null ? right_username : "等待玩家");
        left_data2.setText("积分："+left_score);
        right_data2.setText("积分："+right_score);

    }

    public void refresh_lood_state(){

    }




    // 工具函数
    private String poke_to_string(Vector<String> pokes ){
        String res = "";
        for(String poke: pokes){
            res += poke;
            res += " ";
        }
        return res;
    }

    private String poke_to_string(ArrayList<String> pokes ){
        String res = "";
        for(String poke: pokes){
            res += poke;
            res += " ";
        }
        return res;
    }

    private ArrayList<String> poke_from_string(String poke){
        ArrayList<String> res = new ArrayList();
        String[] pokes = poke.split(" ");
        for(String poke1 : pokes){
            res.add(poke1);
        }
        return res;
    }



}



class if_can_beat_light_button extends Thread {
    private String light_path = "image/desk/chupai.png";
    private String unlight_path = "image/desk/chupai_false.png";
    private Poker_desk poker_desk = null;
    private PokerComparator cmp = null;

    if_can_beat_light_button(Poker_desk poker_desk) {
        this.poker_desk = poker_desk;
        cmp = new PokerComparator();
    }
    public Boolean running = true;

    @Override
    public void run() {
        while (running) {
            if(cmp.canBeat(poker_desk.now_select_cards,poker_desk.now_should_beat_pokes)){
                poker_desk.can_beat = true;
                poker_desk.chupaiButton.setIcon(new ImageIcon(light_path));
            }else{
                poker_desk.can_beat = false;
                poker_desk.chupaiButton.setIcon(new ImageIcon(unlight_path));
            }
        }
    }
}

class testImage extends JFrame {

    public testImage(String path,JLabel lable1,int width,int height) {
        ImageIcon image;
        image = new ImageIcon(path);
        // image.setImage(image.getImage().getScaledInstance(width, height,Image.SCALE_DEFAULT));
        Image img = image.getImage();
        img = img.getScaledInstance(width,height, Image.SCALE_DEFAULT);
        image.setImage(img);
        lable1.setIcon(image);
    }

}
