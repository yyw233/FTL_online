package Cilent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class Poker extends JLabel implements MouseListener {
    String path = "image/poker/";
    String name;
    boolean front_or_back;
    boolean can_click;
    boolean clicked;
    int pk_with = 71;
    int pk_height = 96;
    Poker_desk poker_desk = null;

    Double size;

    Poker(String name, boolean can_click, Double size, Poker_desk poker_desk) {  // 牌的名称 是否能被点击 大小 叫null的牌是反面
        this.name = name;
        //this.front_or_back = front_or_back;
        this.can_click = can_click;
        this.size = size;
        this.poker_desk = poker_desk;

        path += name + ".png"; // 设置路径

        this.setSize((int) (pk_with * size), (int) (pk_height * size));
        new testImage(path, this, (int) (pk_with * size), (int) (pk_height * size));

        this.addMouseListener((MouseListener) this);
    }

    public void re_set(String name, boolean can_click, Double size, Poker_desk poker_desk) {  // 牌的名称 是否能被点击 大小 叫null的牌是反面
        this.name = name;
        //this.front_or_back = front_or_back;
        this.can_click = can_click;
        this.size = size;
        this.poker_desk = poker_desk;

        path += name + ".png"; // 设置路径

        this.setSize((int) (pk_with * size), (int) (pk_height * size));
        new testImage(path, this, (int) (pk_with * size), (int) (pk_height * size));

        this.addMouseListener((MouseListener) this);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if(!can_click) return;
        System.out.println("mouseClicked" + e.paramString());
        int step = 20;
        Point location = this.getLocation();
        Point newLocation = null;
//            if(clicked){
//                newLocation = new Point(location.x, location.y+step);
//                poker_desk.now_select_cards.add(name);
//            }else{
//                newLocation = new Point(location.x, location.y-step);
//                poker_desk.now_select_cards.remove(name);
//            }
        boolean clicked = false;
        for (String nowSelectCard : poker_desk.now_select_cards) {
            if (nowSelectCard.equals(name)) {
                clicked = true;
                break;
            }
        }
        if (clicked) {
            newLocation = new Point(location.x, location.y + step);
            poker_desk.now_select_cards.remove(name);
        } else {
            newLocation = new Point(location.x, location.y - step);
            poker_desk.now_select_cards.add(name);
        }
        this.setLocation(newLocation);
    }

    @Override
    public void mousePressed(MouseEvent e) {
//        System.out.println("mousePressed" + e.paramString());
    }

    @Override
    public void mouseReleased(MouseEvent e) {
//        System.out.println("mouseReleased" + e.paramString());
    }

    @Override
    public void mouseEntered(MouseEvent e) {
//        System.out.println("mouseEntered" + e.paramString());
    }

    @Override
    public void mouseExited(MouseEvent e) {
//        System.out.println("mouseExited" + e.paramString());
    }

}
