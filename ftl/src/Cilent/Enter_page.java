package Cilent;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.Socket;
import java.util.Random;

class Enter_Page {
    Socket socket;
    Font font = new Font(null, Font.PLAIN, 16);
    String username;
    String password;
    String testCode;
    PrintWriter fw = null;
    BufferedReader in = null;
    ImageIcon register_image = new ImageIcon("image/login/register_button.png");
    ImageIcon register_pressed = new ImageIcon("image\\login\\register_pressed.png");
    ImageIcon login_image = new ImageIcon("image\\login\\login_button.png");
    ImageIcon login_pressed = new ImageIcon("image\\login\\login_pressed.png");

    ImageIcon mission = new ImageIcon("image\\poker\\dizhu.png");

    public Enter_Page(Socket socket) throws IOException {
        this.socket = socket;
        fw = new PrintWriter(socket.getOutputStream(),true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        JFrame frame = new JFrame("欢乐斗地主");
        frame.setSize(633, 423);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setIconImage(mission.getImage());
        // 创建背景 JLabel
        JLabel background = new JLabel(new ImageIcon("image\\login\\background.png"));
        background.setBounds(0, 0, 633, 423);

        // 创建一个 JPanel 容器
        JPanel panel = new JPanel();
        panel.setLayout(null); // 使用绝对布局，确保 setBounds 生效
        panel.setBounds(0, 0, 633, 423);
        panel.setOpaque(false); //透明 panel

        // 创建用户标签
        JLabel userLabel = new JLabel("用户名:");
        userLabel.setForeground(Color.white);
        userLabel.setFont(font);
        userLabel.setBounds(170, 100, 80, 25);
        panel.add(userLabel);

        // 创建文本框
        JTextField userText = new JTextField(20);
        userText.setBounds(220, 100, 200, 25);
        panel.add(userText);

        // 创建密码标签
        JLabel passwordLabel = new JLabel("密码:");
        passwordLabel.setForeground(Color.white);
        passwordLabel.setFont(font);
        passwordLabel.setBounds(170, 140, 80, 25);
        panel.add(passwordLabel);

        // 创建密码框
        JPasswordField passwordText = new JPasswordField(20);
        passwordText.setBounds(220, 140, 200, 25);
        panel.add(passwordText);

        // 创建验证码
        testCode = generateCaptcha();

        JLabel testCodeLabel = new JLabel("请输入验证码:");
        testCodeLabel.setForeground(Color.white);
        testCodeLabel.setFont(font);
        testCodeLabel.setBounds(170, 180, 120, 25);
        panel.add(testCodeLabel);

        // 创建验证码文本框
        JTextField testCodeText = new JTextField(20);
        testCodeText.setBounds(280, 180, 60, 25);
        panel.add(testCodeText);

        //验证码显示
        JLabel showTestCodeLabel = new JLabel(testCode);
        showTestCodeLabel.setForeground(Color.red);
        showTestCodeLabel.setFont(font);
        showTestCodeLabel.setBounds(360, 180, 60, 25);
        panel.add(showTestCodeLabel);
        showTestCodeLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e){
                testCode = generateCaptcha();
                showTestCodeLabel.setText(testCode);
            }
        });

        // 创建登录按钮
        JButton loginButton = new JButton(login_image);
        loginButton.setPressedIcon(login_pressed);
        loginButton.setBorderPainted(false);
        loginButton.setContentAreaFilled(false);
        loginButton.setBounds(190, 230, 128, 47);
        panel.add(loginButton);

        // 创建注册按钮
        JButton registerButton = new JButton(register_image);
        registerButton.setPressedIcon(register_pressed);
        registerButton.setBorderPainted(false);
        registerButton.setContentAreaFilled(false);
        registerButton.setBounds(340, 230, 128, 47);
        panel.add(registerButton);

        // 登录按钮监听事件
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                username = userText.getText();
                password = new String(passwordText.getPassword());

                try {
                    if( true || testCode == showTestCodeLabel.getText() ){
                        if (!password.equals("") && !username.equals("") && login_to_server(username,password) ) {
                            JOptionPane.showMessageDialog(frame, "登录成功");
                            // xinchuangk
                            frame.dispose();
                            new Hall_page(socket,username);
                        } else {
                            JOptionPane.showMessageDialog(frame, "密码错误", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }else{
                        JOptionPane.showMessageDialog(frame, "验证码错误", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        // 注册按钮监听事件
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showRegisterDialog(frame);
            }
        });

        // 设置 JFrame 的布局
        frame.setLayout(null);
        frame.setContentPane(background);
        frame.add(panel);
        frame.setVisible(true);
    }

    private void showRegisterDialog(JFrame parent) {
        JDialog registerDialog = new JDialog(parent, "注册", true);
        registerDialog.setSize(400, 300);
        registerDialog.setLayout(null);
        registerDialog.setLocationRelativeTo(parent);

        JLabel userLabel = new JLabel("用户名:");
        userLabel.setBounds(50, 50, 80, 25);
        registerDialog.add(userLabel);

        JTextField userText = new JTextField(20);
        userText.setBounds(140, 50, 200, 25);
        registerDialog.add(userText);

        JLabel passwordLabel = new JLabel("密码:");
        passwordLabel.setBounds(50, 90, 80, 25);
        registerDialog.add(passwordLabel);

        JPasswordField passwordText = new JPasswordField(20);
        passwordText.setBounds(140, 90, 200, 25);
        registerDialog.add(passwordText);

        JLabel confirmPasswordLabel = new JLabel("确认密码:");
        confirmPasswordLabel.setBounds(50, 130, 80, 25);
        registerDialog.add(confirmPasswordLabel);

        JPasswordField confirmPasswordText = new JPasswordField(20);
        confirmPasswordText.setBounds(140, 130, 200, 25);
        registerDialog.add(confirmPasswordText);

        JButton registerButton = new JButton("注册");
        registerButton.setBounds(150, 180, 100, 30);
        registerDialog.add(registerButton);

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = userText.getText();
                String password = new String(passwordText.getPassword());
                String confirmPassword = confirmPasswordText.getText();
                if(!confirmPassword.equals(password)) {
                    JOptionPane.showMessageDialog(registerDialog,"两次密码输入不同");
                    return;
                }
                JsonObject json = new JsonObject();
                json.addProperty("type", "register");
                json.addProperty("username", username);
                json.addProperty("password", password);
                try {
                    fw.println(json);

                    System.out.println(json);

                    JsonObject response = new JsonParser().parse(in.readLine()).getAsJsonObject();
                    registerDialog.dispose();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

            }
        });

        registerDialog.setVisible(true);
    }

    private String generateCaptcha() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder captcha = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 4; i++) {
            captcha.append(chars.charAt(random.nextInt(chars.length())));
        }
        return captcha.toString();
    }

    private boolean login_to_server( String username, String password ) throws IOException {
//        PrintWriter fw = new PrintWriter(socket.getOutputStream(),true);
//        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        try{
            // 创建json
            JsonObject json = new JsonObject();
            json.addProperty("type","login_request");
            json.addProperty("username", username); // username 是字符串类型
            json.addProperty("password", password);// password 是字符串类型

            System.out.println(json.toString());

            // 上传json
            fw.write(json.toString()+'\n');
            fw.flush();
            // 下载解析json
            StringBuilder requestBuilder = new StringBuilder();
            String line;
            line = in.readLine();
                requestBuilder.append(line);

            String jsonStr = requestBuilder.toString();
            JsonObject jsonObj = new JsonParser().parse(jsonStr).getAsJsonObject();

            System.out.println(jsonObj.toString());

            if(jsonObj.get("loginstate").getAsString().equals("success")){
                System.out.println(11);
                return true;
            }else {
                return false;
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
}
