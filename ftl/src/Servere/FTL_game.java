package Servere;

import com.google.gson.JsonObject;

import java.sql.SQLException;
import java.util.*;

public class FTL_game {
    int desk_num;

    // 用户名
    String[] user = new String[3];

    //谁是地主
    String landlord = null;

    // 地主倍率
    int game_rate = 0;

    // 用户的牌
    HashMap<String,Vector<String>> user_poke = new HashMap<>();

    HashMap<String,Integer> username_to_no = new HashMap<>();

    // 现在正处于什么游戏阶段
    String state = "loot_landlord"; // loot_landlord gaming gameover
    // 该谁操作
    int now_turn = who_first_call();
    // 现在场上要被管的牌
    ArrayList<String> now_pokes = new ArrayList<>();
    // 012 玩家分别出了的在场上的牌 "null" 代表不出 没有代表没出
    ArrayList<String> u0_out = new ArrayList<>();
    ArrayList<String> u1_out = new ArrayList<>();
    ArrayList<String> u2_out = new ArrayList<>();

    // 不出牌的次数，达到两次，说明现在出牌的玩家要再次出牌
    int un_response_count = 0;

    String[] poke_set = new String[54]; // 全部牌

    // 用户的牌
    Vector<String> u0 = new Vector<>();
    Vector<String> u1 = new Vector<>();
    Vector<String> u2 = new Vector<>();

    // 地主的牌
    Vector<String> top = new Vector<>();

    // 谁赢了
    String winner = null;
    String loser = null;

    Set<String> game_over_received = new HashSet<>();

    void init() // 洗牌发牌
    {
        int oo = 0;
        for(int i=1;i<=13;i++){
            for(int j=1;j<=4;j++){
                poke_set[oo++]=""+j+"-"+i;
            }
        }
        poke_set[oo++] = "5-1";
        poke_set[oo++] = "5-2";

        // 洗牌
        Random rand = new Random();
        for(int i=0;i<54;i++){
            int y = i + rand.nextInt(54 - i);
            var t = poke_set[y];
            poke_set[y] = poke_set[i];
            poke_set[i] = t;
        }

        // 发牌
        for(int i=0;i<17;i++){
            u0.add(poke_set[i]);
        }
        for (int i=17;i<34;i++){
            u1.add(poke_set[i]);
        }
        for(int i=34;i<51;i++){
            u2.add(poke_set[i]);
        }
        // 放置顶牌
        for (int i = 51; i < 54; i++) {
            top.add(poke_set[i]);
        }

    }

    public FTL_game(String user0, String user1, String user2,int desk_num) {
        init();
        this.user[0] = user0;
        this.user[1] = user1;
        this.user[2] = user2;

        username_to_no.put(user0, 0);
        username_to_no.put(user1, 1);
        username_to_no.put(user2, 2);

        user_poke.put(user0, u0);
        user_poke.put(user1, u1);
        user_poke.put(user2, u2);

        this.desk_num = desk_num;
    }


    public void put_king_poke(String user__){
        user_poke.get(user__).addAll(top);
    }   // 发地主牌


    public JsonObject out_poke(String user,Vector<String> pokes ) throws SQLException { // 有人赢了在result里面返回赢得玩家名称

        JsonObject rt = new JsonObject();

        if(pokes.size()==0){ // 不出牌    // 不出也要返回
            un_response_count++;
            if(un_response_count == 2){
                now_pokes = new ArrayList<>();
                now_pokes.add("buchu");
            }
            switch(username_to_no.get(user)){
                case 0:
                    u0_out.clear();
                    u0_out.add("buchu");
                    break;
                case 1:
                    u1_out.clear();
                    u1_out.add("buchu");
                    break;
                case 2:
                    u2_out.clear();
                    u2_out.add("buchu");
                    break;
            }

        }else { // 出牌了

            un_response_count = 0;

            // 设置当前应该被管的牌
            now_pokes = new ArrayList<>(pokes);
            //出牌

            user_poke.get(user).removeAll(pokes);

            switch(username_to_no.get(user)){
                case 0:
                    u0_out.clear();
                    u0_out.addAll(pokes);
                    break;
                case 1:
                    u1_out.clear();
                    u1_out.addAll(pokes);
                    break;
                case 2:
                    u2_out.clear();
                    u2_out.addAll(pokes);
                    break;
            }
        }

        // 胜利判断
        String res = win_state_check();

        // 这里好像不用管
        if(res.equals("")){
            rt.addProperty("win_state",false);
        }
        else{
            rt.addProperty("win_state",true);
            rt.addProperty("result",res); // 谁赢了
        }

        now_turn++;
        now_turn %= 3;

        return rt;
    } // 玩家出牌

    public String win_state_check() throws SQLException {
        for (String user : user_poke.keySet()){
            // 游戏结束
            if(user_poke.get(user).isEmpty()){
                state = "gameover";
                if (landlord.equals(user)) {
                    database_manage.getInstance().alter_user_score(user,2*game_rate);
                    winner = user;

                    for (String userrr : user_poke.keySet()){
                        if(!userrr.equals(user)){
                            database_manage.getInstance().alter_user_score(userrr,-1*game_rate);
                            loser += userrr + " ";
                        }
                    }

                    return user;
                }else{
                    String other_winner = null;
                    database_manage.getInstance().alter_user_score(landlord,-2*game_rate);
                    for (String userrr : user_poke.keySet()){
                        if(!userrr.equals(user) && !userrr.equals(landlord)){
                            database_manage.getInstance().alter_user_score(userrr,game_rate);
                            database_manage.getInstance().alter_user_score(user,game_rate);
                            other_winner = userrr;
                        }
                    }
                    winner = user + " " + other_winner;
                    loser = landlord;
                    return user + " " + other_winner;
                }

            }
        }
        return "";
    } // 检查有没有人赢 同时操作数据库 修改积分 同时游戏进入下一个状态

    HashMap<String,Integer> user_val = new HashMap<>(); // 谁叫了多少分
    public JsonObject loot_landlord(String user,int val){
        game_rate = Math.max(game_rate,val);

        JsonObject rt = new JsonObject();

        // 直接当地主
        if(val == 3) {
            set_landlord(user);
            rt.addProperty("have_landlord",user);
            return rt;
        }
        else user_val.put(user,val);

        // 最大的当地主
        String mx_user = null; int mx_val = 0;
        if(user_val.size() == 3){
            for(String user2 : user_val.keySet()){
                if (user_val.get(user2) > mx_val){
                    mx_user = user2;
                    mx_val = user_val.get(user2);
                }
            }
            set_landlord(mx_user);
            rt.addProperty("have_landlord",mx_user);
        }

        now_turn++;
        now_turn %= 3;

        return rt;
    } // 叫分 抢地主

    private void set_landlord(String user){
        landlord = user;
        put_king_poke(user);
        now_turn = username_to_no.get(user);
        state = "gaming";
    } // 让玩家当地主，并给他发地主牌 同时让游戏状态变化

    public JsonObject now_state(JsonObject data){
        String username = data.get("username").getAsString();
        JsonObject rt = new JsonObject();
        rt.addProperty("state",state); // 游戏状态

        // 牌数
        rt.addProperty("u0_card_num",u0.size());
        rt.addProperty("u1_card_num",u1.size());
        rt.addProperty("u2_card_num",u2.size());



        // 现在是谁的回合
        rt.addProperty("now_turn",now_turn);

        //现在在抢地主
        if(state.equals("loot_landlord")){

            // 发送请求的玩家的牌
            rt.addProperty("my_card",poke_to_string(user_poke.get(username)));

            // 那么发送分数状态
            if(user_val.containsKey(this.user[0])){
                rt.addProperty("u"+0+"loot_score",user_val.get(this.user[0]));
            }else rt.addProperty("u"+0+"loot_score","meijiao");
            if(user_val.containsKey(this.user[1])){
                rt.addProperty("u"+1+"loot_score",user_val.get(this.user[1]));
            }else rt.addProperty("u"+1+"loot_score","meijiao");
            if(user_val.containsKey(this.user[2])){
                rt.addProperty("u"+2+"loot_score",user_val.get(this.user[2]));
            }else rt.addProperty("u"+2+"loot_score","meijiao");

            // 顶牌不显示
            rt.addProperty("top_card","null");

        }
        // 现在在打牌
        else if(state.equals("gaming")){

            // 发送请求的玩家的牌
            rt.addProperty("my_card",poke_to_string(user_poke.get(username)));

            // 录入地主信息
            rt.addProperty("landlord",username_to_no.get(landlord));

            // 显示顶牌
            rt.addProperty("top_card",poke_to_string(top));

            // 玩家们分别出的牌
            rt.addProperty("u0_out",poke_to_string(u0_out));
            rt.addProperty("u1_out",poke_to_string(u1_out));
            rt.addProperty("u2_out",poke_to_string(u2_out));

            // 现在要被管的牌
            rt.addProperty("now_should_beat_pokes",poke_to_string(new Vector<String>(now_pokes)));

            // 录入其他玩家剩余的牌数信息
            rt.addProperty("lf_card_count",u0.size());
            rt.addProperty("rt_card_count",u1.size());



        }
        else if(state.equals("gameover")){
            rt.addProperty("winner",winner); // 可能有两个人，被空格分开
            rt.addProperty("loser",loser); // 可能有两个人，被空格分开
            rt.addProperty("game_rate",game_rate); // 积分倍率
        }


        return rt;
    }  // 返回现在的游戏状态， 根据不同的游戏状态返回不同种类的状态信息


    public void receive_gameover(String user){ // 接收客户端确认收到游戏结束信息，然后记录以便进入准备判断阶段
        game_over_received.add(user);
        if (game_over_received.size() == 3) {
            //去游戏桌面把几个人的状态设置成未准备，然后就不会进入到游戏的状态了
            game_desk.getInstance().set_all_unready(desk_num);
            game_desk.getInstance().is_desk_start[desk_num] = false;
        }
    } // 开放接口接收用户 收到游戏结束状态的确认

    private int who_first_call(){
        Random rand = new Random();
        return rand.nextInt(3);
    } // 随机数确定谁先叫




    private String poke_to_string(Vector<String> pokes ){
        String res = "";
        for(String poke: pokes){
            res += poke;
            res += " ";
        }
        return res;
    }

    private String poke_to_string(ArrayList<String> pokes ){
        if(pokes == null) return "";
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
            if(!poke1.equals("")) {
                res.add(poke1);
            }
        }
        return res;
    }

}
