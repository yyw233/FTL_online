package Cilent;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class state_refresh extends Thread {
    PrintWriter fw;
    BufferedReader in;

    String username;
    int desk_num;

    Poker_desk poker_desk = null;
    Timer timer = null;

    JsonObject now_json; // 最新的json数据包


    public state_refresh(PrintWriter fw, BufferedReader in, String username, int desk_num, Poker_desk poker_desk) {
        this.fw = fw;
        this.in = in;
        this.username = username;
        this.desk_num = desk_num;
        this.poker_desk = poker_desk;
        num_to_img.put(0,poker_desk.zero);
        num_to_img.put(1,poker_desk.one);
        num_to_img.put(2,poker_desk.two);
        num_to_img.put(3,poker_desk.three);
    }

    HashMap<Integer,ImageIcon> num_to_img = new HashMap<>();
    boolean gamovered = false;

    @Override
    public void run() {
        timer = new Timer(300, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JsonObject sent = new JsonObject();
                sent.addProperty("type", "get_state");
                sent.addProperty("username", username);
                sent.addProperty("desk_num", desk_num);

                fw.println(sent);


                System.out.println(sent);

                JsonObject response = null;
                try {
                    response = JsonParser.parseString(in.readLine()).getAsJsonObject();


                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

                if (now_json != response) {
                    now_json = response;

                    System.out.println(now_json);

                    String state = response.get("state").getAsString();

                    if (state.equals("prepare")) {
                        // 获取自己位置信息和左右位置信息
                        poker_desk.my_pos = response.get("user_desk_no").getAsInt();
                        poker_desk.left_pos = (poker_desk.my_pos - 1 + 3) % 3;
                        poker_desk.right_pos = (poker_desk.my_pos + 1 + 3) % 3;
                        // 获取场上玩家 积分 信息
                        poker_desk.username = username;
                        poker_desk.my_score = response.get("u" + poker_desk.my_pos+"score").getAsInt();
                        poker_desk.my_data.setText("积分 "+poker_desk.my_score);

                        // 获取左右玩家姓名信息 存在姓名则获得积分 准备信息
                        if (response.has("u" + poker_desk.left_pos + "name")) {
                            poker_desk.left_username = response.get("u" + poker_desk.left_pos + "name").getAsString();
                            poker_desk.left_score = response.get("u" + poker_desk.left_pos + "score").getAsInt();
                            poker_desk.is_left_ok = response.get("u" + poker_desk.left_pos + "prepare_state").getAsBoolean();
                        }
                        if (response.has("u" + poker_desk.right_pos + "name")) {
                            poker_desk.right_username = response.get("u" + poker_desk.right_pos + "name").getAsString();
                            poker_desk.right_score = response.get("u" + poker_desk.right_pos + "score").getAsInt();
                            poker_desk.is_right_ok = response.get("u" + poker_desk.right_pos + "prepare_state").getAsBoolean();
                        }
                        poker_desk.refresh_prepare_state();


                    }
                    // 抢地主时期
                    else if (state.equals("loot_landlord")) {

                        // 读取我的卡牌信息
                        ArrayList<String> my_cards = poke_from_string(response.get("my_card").getAsString());
                        poker_desk.my_pokes = my_cards;

                        // 关闭 准备时期的组件
                        if (!is_prepare_component_closed) {
                            try {
                                close_prepare_component();
                            } catch (InterruptedException ex) {
                                throw new RuntimeException(ex);
                            }
                            is_prepare_component_closed = true;
                        }

                        System.out.println("我的位置："+poker_desk.my_pos);
                        System.out.println("当前正在出牌："+response.get("now_turn").getAsInt());

                        // 读取场上的叫分信息
                        if (!response.get("u" + poker_desk.left_pos + "loot_score").getAsString().equals("meijiao")) {
                            poker_desk.left_call_score_label.setIcon(num_to_img.get(response.get("u" + poker_desk.left_pos + "loot_score").getAsInt()));
                            poker_desk.left_call_score = response.get("u" + poker_desk.left_pos + "loot_score").getAsInt();
                        }
                        if (!response.get("u" + poker_desk.right_pos + "loot_score").getAsString().equals("meijiao")) {
                            poker_desk.right_call_score_label.setIcon(num_to_img.get(response.get("u" + poker_desk.right_pos + "loot_score").getAsInt()));
                            poker_desk.right_call_score = response.get("u" + poker_desk.right_pos + "loot_score").getAsInt();
                        }

                        // 获取回合信息
                        // 如果是我的回合，那么显示按钮
                        if (response.get("now_turn").getAsInt() == poker_desk.my_pos) {
                            System.out.println("11111111111111111111111111111111111");
                            poker_desk.show_lood_button();
                        }

                        if (!is_loaded) {
                            load_for_loot_landlord();
                            is_loaded = true;
                        }

                    }
                    // 打牌时期
                    else if (state.equals("gaming")) {
                        // 关闭抢地主时期的组件 设置地主牌 设置叫地主后的更新信息
                        if (!is_loot_landlord_component_closed) {
                            poker_desk.landlord = response.get("landlord").getAsInt();
                            poker_desk.my_pokes = poke_from_string(response.get("my_card").getAsString());
                            poker_desk.sortPokes();
                            close_loot_landlord_component();
                            is_loot_landlord_component_closed = true;
                        }
                        // 显示顶牌
                        if (!is_show_top) {
                            poker_desk.set_top_card(poke_from_string(response.get("top_card").getAsString()));
                            is_show_top = true;
                        }

                        // 录入要被管的牌
                        if(response.has("now_should_beat_pokes") && response.get("now_should_beat_pokes").getAsString() != null){
                            if(response.get("now_should_beat_pokes").getAsString().equals("buchu ")){
                                poker_desk.now_should_beat_pokes.clear();
                                poker_desk.left_chupai.clear();
                                poker_desk.clear_out_container(poker_desk.left_chupai_container);
                                poker_desk.right_chupai.clear();
                                poker_desk.clear_out_container(poker_desk.right_chupai_container);
                                poker_desk.my_chupai.clear();
                                poker_desk.clear_out_container(poker_desk.my_chupai_container);
                            }else{
                                poker_desk.now_should_beat_pokes = poke_from_string(response.get("now_should_beat_pokes").getAsString());
                            }
                        }

                        // 录入其他玩家剩余的牌数信息
                        poker_desk.lf_card_count = response.get("lf_card_count").getAsInt();
                        poker_desk.rt_card_count = response.get("rt_card_count").getAsInt();

                        poker_desk.lf_re_pk.setText(""+poker_desk.lf_card_count);
                        poker_desk.rt_re_pk.setText(""+poker_desk.rt_card_count);

                        // 录入其他玩家出的牌
                        poker_desk.my_chupai = poke_from_string(response.get("u" + poker_desk.my_pos +  "_out").getAsString());
                        poker_desk.left_chupai = poke_from_string(response.get("u" + poker_desk.left_pos + "_out").getAsString());
                        poker_desk.right_chupai = poke_from_string(response.get("u" + poker_desk.right_pos + "_out").getAsString());

                        // 其他玩家的牌数信息
                        poker_desk.lf_card_count = response.get("u"+poker_desk.left_pos+"_card_num").getAsInt();
                        poker_desk.rt_card_count = response.get("u"+poker_desk.right_pos+"_card_num").getAsInt();
                        poker_desk.lf_re_pk.setText(""+poker_desk.lf_card_count);
                        poker_desk.rt_re_pk.setText(""+poker_desk.rt_card_count);
                        poker_desk.lf_re_pk.repaint();
                        poker_desk.rt_re_pk.repaint();


//                        System.out.println("======");
//                        System.out.println(poker_desk.my_chupai);
//                        System.out.println(poker_desk.left_chupai);
//                        System.out.println(poker_desk.right_chupai);
//                        System.out.println("======");


                        // 左边出牌区管理
                        if( !response.get("u" + poker_desk.left_pos + "_out").getAsString().equals("buchu ")){
                            poker_desk.lf_lord_mark_pass.setVisible(false);
                            poker_desk.set_left_out_card_on_desk(poker_desk.left_chupai);
                        }
                        else {
                            poker_desk.lf_lord_mark_pass.setVisible(true);
                            poker_desk.set_left_out_card_on_desk(null);
                            poker_desk.left_chupai.clear();
                            poker_desk.clear_out_container(poker_desk.left_chupai_container);
                        }

                        // 右边出牌区管理
                        if( !response.get("u" + poker_desk.right_pos + "_out").getAsString().equals("buchu ") ){
                            poker_desk.rt_lord_mark_pass.setVisible(false);
                            poker_desk.set_right_out_card_on_desk(poker_desk.right_chupai);
                        }
                        else {
                            System.out.println("ssssssssssssssssssssssssssss");
                            poker_desk.rt_lord_mark_pass.setVisible(true);
                            poker_desk.set_right_out_card_on_desk(null);
                            poker_desk.right_chupai.clear();
                            poker_desk.clear_out_container(poker_desk.right_chupai_container);
                        }



                        // 录入回合信息
                        int now_turn = response.get("now_turn").getAsInt();
                        if( now_turn == poker_desk.my_pos ){
                            poker_desk.chupaiButton.setVisible(true);
                            poker_desk.buchuButton.setVisible(true);
                            poker_desk.chupai_judge.running = true;
                            if(!poker_desk.chupai_judge.isAlive()){
                                poker_desk.chupai_judge =  new if_can_beat_light_button(poker_desk); // 重新创建线程对象
                                poker_desk.chupai_judge.start();
                            }
                        }

                        // 获取回合信息
                        // 如果是我的回合，那么显示按钮
                        if (response.get("now_turn").getAsInt() == poker_desk.my_pos) {
                            // 清空我的出牌区
                            poker_desk.my_chupai.clear();
                            poker_desk.clear_out_container(poker_desk.my_chupai_container);

                            poker_desk.chupai_judge.running = true;
                            if(!poker_desk.chupai_judge.isAlive()){
                                poker_desk.chupai_judge.start();
                            }
                            poker_desk.chupaiButton.setVisible(true);
                            poker_desk.buchuButton.setVisible(true);

                            if(poker_desk.now_should_beat_pokes.isEmpty()){
                                // 如果要我来管
                                poker_desk.my_chupai.clear();
                                poker_desk.clear_out_container(poker_desk.my_chupai_container);
                                poker_desk.buchuButton.setVisible(false);
                            }
                        }
                    } else if (state.equals("gameover")) {
                        // 关闭打牌时期的组件
                        String winner = response.get("winner").getAsString();
                        String loser = response.get("loser").getAsString();
                        int score_change = response.get("game_rate").getAsInt();


                        JsonObject sent1 = new JsonObject();
                        sent1.addProperty("type", "rec_gameover");
                        sent1.addProperty("username", username);
                        fw.println(sent1);

                        JsonObject res = null;
                        try {
                            res = JsonParser.parseString(in.readLine()).getAsJsonObject();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }

                        if(!gamovered){
                            gamovered = true;
                            if(winner.split(" ").length == 2){
                                String Winner1 = winner.split(" ")[0];
                                String Winner2 = winner.split(" ")[1];
                                (new ScoreChangeDialog(Winner1, score_change, Winner2, score_change, loser, -2 * score_change)).setVisible(true);
                            }else{
                                String loser1 = loser.split(" ")[0];
                                String loser2 = loser.split(" ")[1];
                                (new ScoreChangeDialog(winner, 2 * score_change, loser1, -1 * score_change, loser2, -1 * score_change)).setVisible(true);
                            }
                        }

                        close_gaming_component();
                        timer.stop();
                        poker_desk.new_game();
                    }

                    //System.out.println(response);
                }

            }
        });
        timer.start();
    }


    // 加载开始的信息
    boolean is_loaded = false;
    void load_for_loot_landlord(){
        poker_desk.top1.setVisible(true);
        poker_desk.top2.setVisible(true);
        poker_desk.top3.setVisible(true);

        poker_desk.lf_card_count = poker_desk.rt_card_count = 17;

        poker_desk.lf_re_pk.setText(poker_desk.lf_card_count + "");
        poker_desk.rt_re_pk.setText(poker_desk.rt_card_count + "");

        poker_desk.lf_re_pk.setHorizontalTextPosition(JLabel.CENTER);
        poker_desk.lf_re_pk.setVerticalTextPosition(JLabel.CENTER);

        poker_desk.rt_re_pk.setHorizontalTextPosition(JLabel.CENTER);
        poker_desk.rt_re_pk.setVerticalTextPosition(JLabel.CENTER);

        poker_desk.lf_re_pk.setVisible(true);
        poker_desk.rt_re_pk.setVisible(true);
    }

    // 关闭准备时期的组件
    boolean is_prepare_component_closed = false;
    void close_prepare_component() throws InterruptedException {

        poker_desk.ready_button.setVisible(false);
        poker_desk.oj_lbl_me.setVisible(false);
        poker_desk.ok_lbl_left.setVisible(false);
        poker_desk.ok_lbl_right.setVisible(false);
        poker_desk.sortPokes();

        Timer cardTimer = new Timer(30, null);
        final int[] index = {1}; // 用数组来保存索引，使其可以在匿名内部类中被修改
        cardTimer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (index[0] <= poker_desk.my_pokes.size()) {
                    poker_desk.sortPokes();
                    poker_desk.clear_out_container(poker_desk.my_pokes_container);
                    poker_desk.show_my_hand_card(new ArrayList<>(poker_desk.my_pokes.subList(0, index[0])));
                    poker_desk.frame.repaint();
                    index[0]++;
                } else {
                    // 当所有牌都已加载完成后停止 Timer
                    ((Timer)e.getSource()).stop();
                    // 隐藏准备组件
                    poker_desk.ready_button.setVisible(false);
                    poker_desk.oj_lbl_me.setVisible(false);
                    poker_desk.ok_lbl_left.setVisible(false);
                    poker_desk.ok_lbl_right.setVisible(false);
                }
            }
        });
        cardTimer.start();

        poker_desk.sortPokes();
        poker_desk.show_my_hand_card(poker_desk.my_pokes);
        poker_desk.frame.repaint();
    }
    // 关闭抢地主时期的组件 设置地主牌
    boolean is_loot_landlord_component_closed = false;
    void close_loot_landlord_component(){

        if(poker_desk.landlord == poker_desk.my_pos){
            poker_desk.my_lord_mark.setVisible(true);
            poker_desk.sortPokes();
            poker_desk.show_my_hand_card(poker_desk.my_pokes);

        }else{
            if(poker_desk.landlord == poker_desk.left_pos){
                poker_desk.lf_lord_mark.setVisible(true);
                poker_desk.lf_card_count = 20;
                poker_desk.lf_re_pk.setText(poker_desk.lf_card_count + "");
            }else{
                poker_desk.rt_lord_mark.setVisible(true);
                poker_desk.rt_card_count = 20;
                poker_desk.rt_re_pk.setText(poker_desk.rt_card_count + "");
            }
        }

        poker_desk.zeroButton.setVisible(false);
        poker_desk.oneButton.setVisible(false);
        poker_desk.twoButton.setVisible(false);
        poker_desk.threeButton.setVisible(false);

        poker_desk.my_call_score_label.setVisible(false);
        poker_desk.left_call_score_label.setVisible(false);
        poker_desk.right_call_score_label.setVisible(false);

        poker_desk.ok_lbl_left.setVisible(false);
        poker_desk.ok_lbl_right.setVisible(false);

    }
    boolean is_show_top = false;

    void close_gaming_component(){
        poker_desk.chupaiButton.setVisible(false);
        poker_desk.buchuButton.setVisible(false);
        poker_desk.my_lord_mark.setVisible(false);
        poker_desk.lf_lord_mark.setVisible(false);
        poker_desk.rt_lord_mark.setVisible(false);
        poker_desk.my_lord_mark_pass.setVisible(false);
        poker_desk.lf_lord_mark_pass.setVisible(false);
        poker_desk.rt_lord_mark_pass.setVisible(false);
        poker_desk.my_chupai.clear();
        poker_desk.left_chupai.clear();
        poker_desk.right_chupai.clear();

        poker_desk.clear_out_container(poker_desk.my_chupai_container);
        poker_desk.clear_out_container(poker_desk.left_chupai_container);
        poker_desk.clear_out_container(poker_desk.right_chupai_container);

        poker_desk.my_chupai.clear();
        poker_desk.clear_out_container(poker_desk.my_pokes_container);

        poker_desk.top1.setVisible(false);
        poker_desk.top2.setVisible(false);
        poker_desk.top3.setVisible(false);

        poker_desk.rt_re_pk.setVisible(false);
        poker_desk.lf_re_pk.setVisible(false);

        poker_desk.ready_button_state = false;
        poker_desk.ready_button.setVisible(true);
    }




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
        ArrayList<String> res = new ArrayList<>();
        String[] pokes = poke.split(" ");
        for(String poke1 : pokes){
            if(!poke1.equals("")){
                res.add(poke1);
            }
        }
        return res;
    }

}