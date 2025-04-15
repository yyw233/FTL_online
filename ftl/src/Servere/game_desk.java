package Servere;

import com.google.gson.JsonObject;
import com.mysql.cj.conf.ConnectionUrlParser;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class game_desk {

    Vector<Vector<pair>> username_in_desk = new Vector<Vector<pair>>(9);

    HashMap<String, Integer> username_to_desk_no = new HashMap<>();
    public ConcurrentHashMap<String,FTL_game> user_game_desk = new ConcurrentHashMap<>();
    boolean[] is_desk_start = new boolean[9];

    private static game_desk instance = new game_desk();

    //Boolean is_game_started = false;


    public static game_desk getInstance() {
        return instance;
    }
    private game_desk() {
        for(var i = 0; i < 9; i++){
            username_in_desk.add(new Vector<pair>());
            is_desk_start[i] = false;
        }
    }
    boolean desk_add_people(String username,int desk_no){
        if(username_in_desk.get(desk_no).size() < 3){
            username_in_desk.get(desk_no).add(new pair(username,false));
            username_to_desk_no.put(username, desk_no);
            return true;
        }else return false;
    }

    boolean people_leave_desk(String username,int desk_no){
        if(desk_no>=0 && desk_no <9){
            Vector<pair> desk = username_in_desk.get(desk_no);
            for (int i = 0; i < desk.size(); i++) {
                if (desk.get(i).name.equals(username)) {
                    desk.remove(i);
                    username_to_desk_no.remove(username);
                    return true;
                }
            }
        }
        return false;
    }

    boolean people_ready(String username) {
        if (!username_to_desk_no.containsKey(username)) {
            return false;
        }
        int desk_no = username_to_desk_no.get(username);
        Vector<pair> desk = username_in_desk.get(desk_no);

        synchronized (desk) {
            for (pair p : desk) {
                if (p.name.equals(username)) {
                    p.is_ready = true;
                    if (check_game_start(desk_no)) {
                        Vector<String> n_on_s_d = new Vector<>();
                        for (var x : username_in_desk.get(desk_no)) {
                            n_on_s_d.add(x.name);
                        }
                        // 这里开始了游戏，因为创建了新的类
                        FTL_game ftl = new FTL_game(n_on_s_d.get(0), n_on_s_d.get(1), n_on_s_d.get(2), desk_no);
                        user_game_desk.putIfAbsent(n_on_s_d.get(0), ftl);
                        user_game_desk.putIfAbsent(n_on_s_d.get(1), ftl);
                        user_game_desk.putIfAbsent(n_on_s_d.get(2), ftl);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    boolean people_unready(String username) {
        if (!username_to_desk_no.containsKey(username)) {
            return false;
        }
        int desk_no = username_to_desk_no.get(username);
        Vector<pair> desk = username_in_desk.get(desk_no);
        for (pair p : desk) {
            if (p.name.equals(username)) {
                p.is_ready = false;
                return true;
            }
        }
        return false;
    }
    boolean check_game_start(int desk_no){
        if(is_desk_start[desk_no]){ return true; }
        Vector<pair> desk = username_in_desk.get(desk_no);
        if ( desk.isEmpty() || desk.size() != 3 ) return false;
        for (pair p : desk) {
            if (!p.is_ready) {
                return false;
            }
        }
//        is_game_started = true;
//        Vector<pair> users = username_in_desk.get(desk_no);
//        new FTL_game(users.get(0).name,users.get(1).name,users.get(2).name,desk_no);
        is_desk_start[desk_no] = true;
        return true;
    }

    public ArrayList<Integer> how_much_people_in_desk(){
        ArrayList<Integer> rt = new ArrayList<>();
        for(var i = 0; i < 9; i++){
            if(username_in_desk.isEmpty()) rt.add(0);
            else rt.add(username_in_desk.get(i).size());
        }
        return rt;
    }

    public int get_user_pos_on_desk(String username){
        for( var i :username_in_desk ){
            int pos = 0;
            for (var j : i){
                if( j.name.equals(username) ){
                    return pos;
                }
                pos++;
            }
        }
        return -1;
    }

    public JsonObject get_now_state(JsonObject json) throws SQLException {
        String username = json.get("username").getAsString();
        int desk_num = json.get("desk_num").getAsInt();

        JsonObject now_state = new JsonObject();
        now_state.addProperty("user_desk_no",get_user_pos_on_desk(username)); // 放入玩家位置信息

        //synchronized(username_in_desk.get(desk_num)){
            if(check_game_start(desk_num)){ // 开了

                FTL_game game = user_game_desk.get(username);
                if (game == null) {
                    // 返回明确的重试状态
                    now_state.addProperty("state", "initializing");
                    return now_state;
                }

                // 去用户所在的牌桌里面获取状态
                now_state = game.now_state(json);

            }else{ // 没开始

                now_state.addProperty("state","prepare");

                int[] temp_score = new int[3];
                temp_score[0] = temp_score[1] = temp_score[2] = -1000000;

                // 那么传输准备信息
                for(int i = 0; i < username_in_desk.get(desk_num).size(); i++){
                    now_state.addProperty("u"+i+"name", username_in_desk.get(desk_num).get(i).name);
                    if(temp_score[i] == -1000000 ) temp_score[i] =  database_manage.getInstance().query_user_score( username_in_desk.get(desk_num).get(i).name);
                    now_state.addProperty("u"+i+"score",temp_score[i]);
                    now_state.addProperty("u"+i+"prepare_state", username_in_desk.get(desk_num).get(i).is_ready);
                }
            }
        //}

        return now_state;
    }

    public void set_all_unready(int desk_no){
        username_in_desk.get(desk_no).get(0).is_ready = false;

        user_game_desk.remove(username_in_desk.get(desk_no).get(0).name);

        username_in_desk.get(desk_no).get(1).is_ready = false;
        user_game_desk.remove(username_in_desk.get(desk_no).get(1).name);

        username_in_desk.get(desk_no).get(2).is_ready = false;
        user_game_desk.remove(username_in_desk.get(desk_no).get(2).name);
    }

}

class pair{
    String name;
    Boolean is_ready;
    pair(String name, Boolean ready){
        this.name = name;
        this.is_ready = ready;
    }
}
