package Cilent;

import java.util.*;

public class PokerComparator {
    private static final Map<String, Integer> CARD_RANK = new HashMap<>();

    static {
        // 初始化牌面等级 3-13, 14(A), 15(2), 16(小王), 17(大王)
        for (int i = 3; i <= 13; i++) CARD_RANK.put(i + "", i);
        CARD_RANK.put("1", 14);  // A
        CARD_RANK.put("2", 15);  // 2
        CARD_RANK.put("5-1", 16); // 小王
        CARD_RANK.put("5-2", 17); // 大王
    }
    public static Map<String, Integer> getCardRank() {
        return CARD_RANK;
    }



    // 转换卡牌为等级列表（已排序）
    private List<Integer> getSortedRanks(List<String> cards) {
        List<Integer> ranks = new ArrayList<>();
        for (String card : cards) {
            if(card.isEmpty()) continue;
            System.out.println(card);
            String key = card.startsWith("5") ? card : card.split("-")[1];
            Integer rank = CARD_RANK.get(key);
            if (rank == null) throw new IllegalArgumentException("Invalid card: " + card);
            ranks.add(rank);
        }
        Collections.sort(ranks);
        return ranks;
    }

    // 牌型判断入口
    public String getType(List<String> cards) {
        List<Integer> ranks = getSortedRanks(cards);
        int size = ranks.size();

        if (isRocket(ranks)) return "rocket";
        if (isBomb(ranks)) return "bomb";
        if (size == 1) return "single";
        if (isPair(ranks)) return "pair";
        if (isTriple(ranks)) return "triple";
        if (isTripleWithSingle(ranks)) return "triple+single";
        if (isTripleWithPair(ranks)) return "triple+pair";
        if (isFourWithTwo(ranks)) return "four+two";
        if (isAirplane(ranks)) return "airplane";
        if (isAirplaneWithWings(ranks)) return "airplane+wings";
        if (isDoubleStraight(ranks)) return "double_straight";
        if (isStraight(ranks)) return "straight";

        return "unknown";
    }

    // 以下是各牌型判断方法
    private boolean isRocket(List<Integer> ranks) {
        return ranks.size() == 2 && ranks.contains(16) && ranks.contains(17);
    }

    private boolean isBomb(List<Integer> ranks) {
        return ranks.size() == 4 && allEqual(ranks);
    }

    private boolean isPair(List<Integer> ranks) {
        return ranks.size() == 2 && allEqual(ranks);
    }

    private boolean isTriple(List<Integer> ranks) {
        return ranks.size() == 3 && allEqual(ranks);
    }

    // 三带一判断
    private boolean isTripleWithSingle(List<Integer> ranks) {
        if (ranks.size() != 4) return false;
        Map<Integer, Integer> freq = getFrequency(ranks);
        return freq.containsValue(3) && freq.containsValue(1);
    }

    // 三带对判断
    private boolean isTripleWithPair(List<Integer> ranks) {
        if (ranks.size() != 5) return false;
        Map<Integer, Integer> freq = getFrequency(ranks);
        return freq.containsValue(3) && freq.containsValue(2);
    }

    // 顺子判断
    private boolean isStraight(List<Integer> ranks) {
        if (ranks.size() < 5) return false;
        Map<Integer, Integer> freq = getFrequency(ranks);
        if (freq.size() != ranks.size()) return false; // 不能有重复牌
        if (ranks.get(ranks.size() - 1) >= 15) return false; // 不能包含2和王
        for (int i = 1; i < ranks.size(); i++) {
            if (ranks.get(i) - ranks.get(i - 1) != 1) return false;
        }
        return true;
    }

    // 双顺判断
    private boolean isDoubleStraight(List<Integer> ranks) {
        if (ranks.size() < 6 || ranks.size() % 2 != 0) return false;
        Map<Integer, Integer> freq = getFrequency(ranks);
        List<Integer> pairs = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : freq.entrySet()) {
            if (entry.getValue() != 2) return false;
            int rank = entry.getKey();
            if (rank >= 15) return false; // 排除2和王
            pairs.add(rank);
        }
        Collections.sort(pairs);
        for (int i = 1; i < pairs.size(); i++) {
            if (pairs.get(i) - pairs.get(i - 1) != 1) return false;
        }
        return true;
    }

    // 飞机（不带翅膀）
    private boolean isAirplane(List<Integer> ranks) {
        if (ranks.size() % 3 != 0) return false;
        Map<Integer, Integer> freq = getFrequency(ranks);
        List<Integer> triples = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : freq.entrySet()) {
            if (entry.getValue() != 3) return false;
            int rank = entry.getKey();
            if (rank >= 15) return false; // 排除2和王
            triples.add(rank);
        }
        Collections.sort(triples);
        for (int i = 1; i < triples.size(); i++) {
            if (triples.get(i) - triples.get(i - 1) != 1) return false;
        }
        return true;
    }

    // 飞机带翅膀
    private boolean isAirplaneWithWings(List<Integer> ranks) {
        Map<Integer, Integer> freq = getFrequency(ranks);
        List<Integer> triples = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : freq.entrySet()) {
            if (entry.getValue() == 3) {
                int rank = entry.getKey();
                if (rank >= 15) return false; // 排除2和王
                triples.add(rank);
            }
        }
        if (triples.size() < 2) return false;
        Collections.sort(triples);
        for (int i = 1; i < triples.size(); i++) {
            if (triples.get(i) - triples.get(i - 1) != 1) return false;
        }
        int wingCount = ranks.size() - triples.size() * 3;
        Map<Integer, Integer> wingsFreq = new HashMap<>(freq);
        for (int triple : triples) wingsFreq.remove(triple);
        if (wingCount == triples.size()) { // 带单牌
            for (int count : wingsFreq.values()) {
                if (count != 1) return false;
            }
        } else if (wingCount == triples.size() * 2) { // 带对子
            for (int count : wingsFreq.values()) {
                if (count != 2) return false;
            }
        } else {
            return false;
        }
        return true;
    }

    // 四带二
    private boolean isFourWithTwo(List<Integer> ranks) {
        if (ranks.size() != 6 && ranks.size() != 8) return false;
        Map<Integer, Integer> freq = getFrequency(ranks);
        if (!freq.containsValue(4)) return false;
        int otherCount = ranks.size() - 4;
        if (otherCount == 2) { // 四带两单牌
            return freq.values().stream().filter(c -> c == 1).count() == 2;
        } else if (otherCount == 4) { // 四带两对子
            return freq.values().stream().filter(c -> c == 2).count() == 2;
        }
        return false;
    }

    // 比较逻辑
    public boolean canBeat(List<String> current, List<String> previous) {
        String type1 = getType(current);
        String type2 = getType(previous);
//        System.out.println(current);
//        System.out.println("lcm");
//        System.out.println(previous);
        // 特殊牌型比较
        if (type1.equals("rocket")) return true;
        if (type2.equals("rocket")) return false;
        if (type1.equals("bomb") && !type2.equals("bomb")) return true;
        if (type1.equals("bomb") && type2.equals("bomb")) {
            return getSortedRanks(current).get(0) > getSortedRanks(previous).get(0);
        }
        List<Integer> r1 = getSortedRanks(current);
        List<Integer> r2 = getSortedRanks(previous);

//        System.out.println("aaaaaa");
//
//        System.out.println(r1);
//        System.out.println(r2);
//
//        System.out.println("aaaaaa");

        // 开局空牌情况
        if(r1.isEmpty()) return false;
        if(r2.isEmpty() || previous.get(0).equals("buchu")  || previous.get(0).equals("buchu ")) return !type1.equals("unknown");

        // 同类型比较
        if (!type1.equals(type2)) return false;


        return compareSameType(r1, r2, type1);
    }

    private boolean compareSameType(List<Integer> r1, List<Integer> r2, String type) {

        switch (type) {
            case "single": case "pair": case "triple":
                return r1.get(0) > r2.get(0);
            case "straight": case "double_straight": case "airplane":
                return r1.get(0) > r2.get(0); // 比较最小牌
            case "triple+single": case "triple+pair": case "airplane+wings":
                return getMainRank(r1, 3) > getMainRank(r2, 3);
            case "four+two":
                return getMainRank(r1, 4) > getMainRank(r2, 4);
            default:
                return false;
        }
    }

    // 辅助方法
    private boolean allEqual(List<Integer> list) {
        return new HashSet<>(list).size() == 1;
    }

    private Map<Integer, Integer> getFrequency(List<Integer> ranks) {
        Map<Integer, Integer> freq = new HashMap<>();
        for (int r : ranks) freq.put(r, freq.getOrDefault(r, 0) + 1);
        return freq;
    }

    private int getMainRank(List<Integer> ranks, int target) {
        Map<Integer, Integer> freq = getFrequency(ranks);
        for (Map.Entry<Integer, Integer> entry : freq.entrySet()) {
            if (entry.getValue() == target) return entry.getKey();
        }
        return -1;
    }
}