package Servere;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import java.util.stream.Collectors;

public class data_type_manage implements AutoCloseable {
    Socket socket;
    JsonObject json;
    String type;
    String result;



    public data_type_manage(Socket socket, JsonObject json)  {
        this.socket = socket;
        this.json = json;
        this.type = json.get("type").getAsString();
    }

    JsonObject mangae() throws SQLException {
        JsonObject return_json = new JsonObject();
        System.out.println("dtm start");
        if(type.equals("login_request")){
            String username=json.get("username").getAsString();
            String password=json.get("password").getAsString();


                JsonObject database_result = database_manage.getInstance().user_search(username,password);
                boolean userExist = database_result.get("user_exist").getAsBoolean();
                boolean passwordRight = database_result.get("password_right").getAsBoolean();
                if( passwordRight ){
                    return_json.addProperty("loginstate", "success");
                }else {
                    return_json.addProperty("loginstate", "fail");
                }

        }
        else if( type.equals("register") ){
            System.out.println("dtm do the register request");
            String username=json.get("username").getAsString();
            String password=json.get("password").getAsString();


                database_manage.getInstance().register_user(username,password);
                return_json.addProperty("register", "success");

        }
        else if( type.equals("desk_people_query") ){

            ArrayList<Integer> data = game_desk.getInstance().how_much_people_in_desk();
            for(int i=0; i<data.size(); i++){
                return_json.addProperty("desk_"+i, data.get(i));
            }
        }
        else if( type.equals("join_desk") ){
            String username=json.get("username").getAsString();
            int desk_id=json.get("desk_num").getAsInt();
            Boolean is_success =  game_desk.getInstance().desk_add_people(username,desk_id);
            return_json.addProperty("is_success", is_success);
        }
        else if( type.equals("leave_desk") ){
            String username=json.get("username").getAsString();
            int desk_num=json.get("desk_num").getAsInt();
            game_desk.getInstance().people_leave_desk(username,desk_num);
            return_json.addProperty("leave_desk", "success");
        }
        else if( type.equals("exit") ){
            String username=json.get("username").getAsString();
            int desk_num=json.get("desk_num").getAsInt();
            game_desk.getInstance().people_leave_desk(username,desk_num);
            return_json.addProperty("exit", "success");
        }
        else if(type.equals("ready")){
            String username=json.get("username").getAsString();
            int desk_num=json.get("desk_num").getAsInt();
            synchronized (json.get("desk_num")){
                game_desk.getInstance().people_ready(username);
            }
            return_json.addProperty("ready", "success");

            boolean is_start =  game_desk.getInstance().check_game_start(desk_num);
            return_json.addProperty("is_start", is_start);


        }else if( type.equals("unready") ){
            String username=json.get("username").getAsString();
            int desk_num=json.get("desk_num").getAsInt();
            game_desk.getInstance().people_unready(username);
            return_json.addProperty("unready", "success");

        }
        else if( type.equals("get_state") ){
            int desk_num = json.get("desk_num").getAsInt();
            // 获取桌面对应的同步锁
            var lock = game_desk.getInstance().username_in_desk.get(desk_num);
            //synchronized (lock) {
                return_json = game_desk.getInstance().get_now_state(json);
            //}
        }
        else if( type.equals("call") ){
            String username=json.get("username").getAsString();
            int desk_num=json.get("desk_num").getAsInt();
            int score=json.get("score").getAsInt();
            game_desk.getInstance().user_game_desk.get(username).loot_landlord(username,score);
            return_json.addProperty("call", "success");
        }
        else if( type.equals("pass") ){
            String username=json.get("username").getAsString();
            int desk_num=json.get("desk_num").getAsInt();
            game_desk.getInstance().user_game_desk.get(username).out_poke(username,new Vector<>());
            return_json.addProperty("pass", "success");
        }
        else if( type.equals("chupai") ){
            String username=json.get("username").getAsString();
            int desk_num=json.get("desk_num").getAsInt();
            ArrayList<String> cards= poke_from_string(json.get("cards").getAsString()); 
            game_desk.getInstance().user_game_desk.get(username).out_poke(username,new Vector<String>(cards));
            return_json.addProperty("chupai", "success");
        } else if( type.equals("rec_gameover")){
            String username=json.get("username").getAsString();
            game_desk.getInstance().user_game_desk.get(username).receive_gameover(username);
        }
        return return_json;
    }


    @Override
    public void close() throws Exception {

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
