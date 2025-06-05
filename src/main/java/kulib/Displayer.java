package kulib;

import static kulib.Dis_1602a.display_costom;

public class Displayer {
    public static final long MIN_INTERVAL_MS = 50;  // 20fps
    private static final Displayer INSTANCE = new Displayer();

    public static Displayer getInstance() {
        return INSTANCE;
    }

    public void display(Info gameInfo) {
        // 原Dis_1602a的显示逻辑
        int states = gameInfo.get_states();
        //System.out.println("[Displayer] 接收状态为: " + gameInfo.get_states() + " @ " + gameInfo.hashCode());
        if(states==2 || states ==7)
            display_costom(String.format("CUR:%12.1fFC:%13.1f", gameInfo.get_c_pp(), gameInfo.get_f_pp()));
        else{
            int pp95 = Math.round(gameInfo.getPp95());
            int pp98 = Math.round(gameInfo.getPp98());

            // 构造 95 和 98 的合并字符串，共16字符
            String part1 = String.format("95:%-4d 98:%-4d", pp95, pp98);
            // → "95:1234 98:5678"（总共16字符，数字部分不足4位自动空格补齐）

            int pp100 = Math.round(gameInfo.getPp100());

            display_costom(part1 + " 100:" + pp100);
        }
    }
}
