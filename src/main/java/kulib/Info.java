package kulib;
import java.util.Map;

public class Info {
    private Current_pp pp;
    private Map<String, Object> hits;
    private Map<String, Double> menuPP;
    private Map<String, Object> keyOverlay;

    // 普通 PP（打图时）
    private float current_pp;
    private float fc_pp;

    // 选图用的 ACC PP
    private float pp_95;
    private float pp_98;
    private float pp_100;

    //打图信息
    private int s300;
    private int s100;
    private int s50;
    private int smiss;
    private int sliderBreaks;
    private int states;

    // KeyOverlay 数据
    private boolean k1Pressed;
    private int k1Count;
    private boolean k2Pressed;
    private int k2Count;
    private boolean m1Pressed;
    private int m1Count;
    private boolean m2Pressed;
    private int m2Count;

    @SuppressWarnings("unchecked")
    public Info(Current_pp pp, Map<String, Object> hits, Map<String, Double> menuPP, Map<String, Object> keyOverlay, int states) {
        this.pp = pp;
        this.hits = hits;
        this.menuPP = menuPP;
        this.keyOverlay = keyOverlay;
        this.states=states;

        if (pp == null) {
            System.err.println("当前 pp 为空，跳过设置");
            return;
        }

        // 设置 pp
        this.current_pp = pp.getCurrent();
        this.fc_pp = pp.getFc();

        // 处理ACC PP
        if (menuPP != null) {
            this.pp_95 = menuPP.get("95") != null ? menuPP.get("95").floatValue() : 0f;
            this.pp_98 = menuPP.get("98") != null ? menuPP.get("98").floatValue() : 0f;
            this.pp_100 = menuPP.get("100") != null ? menuPP.get("100").floatValue() : 0f;
        } else {
            System.err.println("ACC PP数据为空");
        }

        // 设置命中统计
        if (hits != null) {
            this.s300 = (int) hits.getOrDefault("300", 0);
            this.s100 = (int) hits.getOrDefault("100", 0);
            this.s50 = (int) hits.getOrDefault("50", 0);
            this.smiss = (int) hits.getOrDefault("0", 0);  // miss 是 key "0"
            this.sliderBreaks = (int) hits.getOrDefault("sliderBreaks", 0);
        }
        else {
            System.err.println("hits 数据为空");
        }

        // 设置 KeyOverlay 数据
        if (keyOverlay != null) {
            // System.out.println("[Info] KeyOverlay 数据接收成功: " + keyOverlay);
            Map<String, Object> k1 = (Map<String, Object>) keyOverlay.get("k1");
            if (k1 != null) {
                this.k1Pressed = (boolean) k1.getOrDefault("isPressed", false);
                this.k1Count = (int) k1.getOrDefault("count", 0);
            }

            Map<String, Object> k2 = (Map<String, Object>) keyOverlay.get("k2");
            if (k2 != null) {
                this.k2Pressed = (boolean) k2.getOrDefault("isPressed", false);
                this.k2Count = (int) k2.getOrDefault("count", 0);
            }

            Map<String, Object> m1 = (Map<String, Object>) keyOverlay.get("m1");
            if (m1 != null) {
                this.m1Pressed = (boolean) m1.getOrDefault("isPressed", false);
                this.m1Count = (int) m1.getOrDefault("count", 0);
                // System.out.println("[Info] M1 按键数据: pressed=" + this.m1Pressed + ", count=" + this.m1Count);
            }

            Map<String, Object> m2 = (Map<String, Object>) keyOverlay.get("m2");
            if (m2 != null) {
                this.m2Pressed = (boolean) m2.getOrDefault("isPressed", false);
                this.m2Count = (int) m2.getOrDefault("count", 0);
                // System.out.println("[Info] M2 按键数据: pressed=" + this.m2Pressed + ", count=" + this.m2Count);
            }
        } else {
            // System.err.println("KeyOverlay 数据为空");
        }
    }

    public float get_c_pp() {return current_pp;}
    public float get_f_pp() {return fc_pp;}
    public int get_s300() {return s300;}
    public int get_s100() {return s100;}
    public int get_s50() {return s50;}
    public int get_smiss() {return smiss;}
    public int get_sb() {return sliderBreaks;}

    public void setStates(int states) { this.states = states; }
    public int get_states() {return states;}

    public float getPp95() { return pp_95; }
    public float getPp98() { return pp_98; }
    public float getPp100() { return pp_100; }

    // KeyOverlay getter 方法
    public boolean isK1Pressed() { return k1Pressed; }
    public int getK1Count() { return k1Count; }
    public boolean isK2Pressed() { return k2Pressed; }
    public int getK2Count() { return k2Count; }
    public boolean isM1Pressed() { return m1Pressed; }
    public int getM1Count() { return m1Count; }
    public boolean isM2Pressed() { return m2Pressed; }
    public int getM2Count() { return m2Count; }
}
