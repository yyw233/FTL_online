package Servere;
import com.google.gson.JsonObject;

import java.sql.*;

public class database_manage implements AutoCloseable {
    String url="jdbc:mysql://localhost:3306/";
    String user="123456";
    String password="123456";
    Connection con = DriverManager.getConnection(url, user, password);

    private database_manage() throws SQLException {
    }
    private static database_manage instance;

    static {
        try {
            instance = new database_manage();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static database_manage getInstance() throws SQLException {return instance;}

    public JsonObject user_search(String username, String password) throws SQLException {
        JsonObject judge_result = new JsonObject();
        boolean user_exist = false;
        boolean password_right = false;
        String query = "select * from ftl.ftl_user where username = " + "'"+ username + "'";
        Statement stm = con.createStatement();
        ResultSet rs = stm.executeQuery(query);
        if(rs.next()){  // 移动到第一行
            user_exist = true;
            if(password.equals(rs.getString("password"))){
                password_right = true;
            }
        }
        judge_result.addProperty("user_exist", user_exist);
        judge_result.addProperty("password_right", password_right);
        return judge_result;
    }

    public void alter_user_score(String username, int score) throws SQLException {
        int user_score = query_user_score(username);
        int new_user_score = user_score + score;
        String new_sql = "UPDATE ftl.ftl_user SET score = ? WHERE username = ?";
        PreparedStatement pst = con.prepareStatement(new_sql);
        pst.setInt(1, new_user_score);
        pst.setString(2, username);
        // 执行更新
        pst.executeUpdate();
    }
    public int query_user_score(String username) throws SQLException {
        String query = "select * from ftl.ftl_user where username = " + "'"+ username + "'";
        Statement stm = con.createStatement();
        ResultSet rs = stm.executeQuery(query);
        rs.next();
        int user_score = rs.getInt("score");
        return user_score;
    }

    public void register_user(String username, String password) throws SQLException {
        String query = "INSERT INTO ftl.ftl_user (username, password) VALUES (?, ?)";
        PreparedStatement pst = con.prepareStatement(query);
        pst.setString(1, username);
        pst.setString(2, password);
        pst.executeUpdate();
    }

    @Override
    public void close() throws Exception {
        con.close();
    }
}
