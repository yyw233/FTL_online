package Cilent;

import javax.swing.*;
import java.awt.*;

public class ScoreChangeDialog extends JDialog {
    ImageIcon win = new ImageIcon("image/desk/lord_win.jpg");
    ImageIcon lose = new ImageIcon("image/desk/lord_lose.jpg");
    ImageIcon mission = new ImageIcon("image\\poker\\dizhu.png");

    public ScoreChangeDialog(String player1, int scoreChange1,
                             String player2, int scoreChange2,
                             String player3, int scoreChange3) {
        super((Frame) null, "积分变化", true);

        // 创建背景标签并设置图标
        JLabel win_lose = new JLabel();
        win_lose.setLayout(new BorderLayout()); // 使用BorderLayout布局

        // 根据积分变化设置背景
        if (scoreChange1 * scoreChange2 * scoreChange3 < 0) {
            win_lose.setIcon(win);
        } else {
            win_lose.setIcon(lose);
        }

        this.setIconImage(mission.getImage());

        // 创建玩家信息面板并设置为透明
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setOpaque(false); // 透明面板

        // 添加玩家信息到面板
        addPlayerInfo(panel, player1, scoreChange1, player2, scoreChange2, player3, scoreChange3);

        // 将玩家信息面板添加到背景标签的中央
        win_lose.add(panel, BorderLayout.CENTER);

        // 设置对话框的内容面板为背景标签
        setContentPane(win_lose);

        // 根据背景图片调整对话框大小
        setSize(win_lose.getIcon().getIconWidth(), win_lose.getIcon().getIconHeight());
        setLocationRelativeTo(null); // 居中显示
    }

    private void addPlayerInfo(JPanel panel, String p1, int s1, String p2, int s2, String p3, int s3) {
        // 玩家1
        JLabel labelPlayer1 = createCenteredLabel("玩家: " + p1);
        labelPlayer1.setForeground(Color.black);
        labelPlayer1.setFont(new Font("微软雅黑", Font.BOLD, 14));
        panel.add(labelPlayer1);
        JLabel labelScore1 = createCenteredLabel("积分变化: " + s1);
        labelScore1.setForeground(Color.black);
        labelScore1.setFont(new Font("微软雅黑", Font.BOLD, 14));
        panel.add(labelScore1);

        // 玩家2
        JLabel labelPlayer2 = createCenteredLabel("玩家: " + p2);
        labelPlayer2.setForeground(Color.black);
        labelPlayer2.setFont(new Font("微软雅黑", Font.BOLD, 14));
        panel.add(labelPlayer2);
        JLabel labelScore2 = createCenteredLabel("积分变化: " + s2);
        labelScore2.setForeground(Color.black);
        labelScore2.setFont(new Font("微软雅黑", Font.BOLD, 14));
        panel.add(labelScore2);

        // 玩家3
        JLabel labelPlayer3 = createCenteredLabel("玩家: " + p3);
        labelPlayer3.setForeground(Color.black);
        labelPlayer3.setFont(new Font("微软雅黑", Font.BOLD, 14));
        panel.add(labelPlayer3);
        JLabel labelScore3 = createCenteredLabel("积分变化: " + s3);
        labelScore3.setForeground(Color.black);
        labelScore3.setFont(new Font("微软雅黑", Font.BOLD, 14));
        panel.add(labelScore3);
    }

    private JLabel createCenteredLabel(String text) {
        JLabel label = new JLabel(text);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setOpaque(false); // 确保透明
        return label;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ScoreChangeDialog dialog = new ScoreChangeDialog("Alice", 10, "Bob", -5, "Charlie", -5);
            dialog.setVisible(true);
        });
    }
}