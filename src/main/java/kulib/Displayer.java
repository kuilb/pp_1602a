package kulib;

import static kulib.Dis_1602a.display_costom;
import picoapi.CustomChar;
import picoapi.NormalChar;
import picoapi.ProtocolBuilder;
import picoapi.CharPacket;

public class Displayer {
    // 动画区宽度（左侧动画，右侧4字符数值）
    private static final int ANIM_WIDTH = 12;
    
    // 全黑自定义字符（8x5点阵全黑）
    private static final CustomChar FULL_BLOCK_CHAR = new CustomChar(new byte[]{0x1F, 0x1F, 0x1F, 0x1F, 0x1F, 0x1F, 0x1F, 0x1F});

    // 黑块数据结构
    private static class KeyBlock {
        int pos;      // 当前左边位置（0~ANIM_WIDTH-1）
        int length;   // 黑块长度
        boolean isActive; // true=正在按下中（静止），false=已松开（流动）
        
        KeyBlock(int pos, int length, boolean isActive) {
            this.pos = pos;
            this.length = length;
            this.isActive = isActive;
        }
        
        @Override
        public String toString() {
            return String.format("KeyBlock{pos=%d, len=%d, active=%s}", pos, length, isActive);
        }
    }

    // K1/K2黑块队列
    private final java.util.LinkedList<KeyBlock> k1Blocks = new java.util.LinkedList<>();
    private final java.util.LinkedList<KeyBlock> k2Blocks = new java.util.LinkedList<>();

    // 按键按下时间戳
    private long k1PressTime = 0;
    private long k2PressTime = 0;

    // 上一帧按键状态
    private boolean lastK1Pressed = false;
    private boolean lastK2Pressed = false;

    // 每帧调用，更新动画队列
    private void updateKeyBlocks(Info gameInfo) {
        boolean k1Pressed = gameInfo.isK1Pressed();
        boolean k2Pressed = gameInfo.isK2Pressed();
        long now = System.currentTimeMillis();
        
        // 调试信息：打印按键状态
        // if (k1Pressed != lastK1Pressed || k2Pressed != lastK2Pressed) {
        //     System.out.println("[Displayer Debug] 按键状态变化: K1=" + k1Pressed + ", K2=" + k2Pressed + ", K1计数=" + gameInfo.getK1Count() + ", K2计数=" + gameInfo.getK2Count());
        // }

        // K1逻辑
        if (k1Pressed) {
            if (!lastK1Pressed) {
                k1PressTime = now;
                // System.out.println("[Displayer Debug] K1 按下开始，时间戳: " + now);
            }
        } else {
            if (lastK1Pressed) {
                int duration = (int)Math.min(now - k1PressTime, 1000);
                // System.out.println("[Displayer Debug] K1 松开，持续时间: " + duration + "ms，停止补充新黑块");
            }
        }
        lastK1Pressed = k1Pressed;

        // K2逻辑
        if (k2Pressed) {
            if (!lastK2Pressed) {
                k2PressTime = now;
                // System.out.println("[Displayer Debug] K2 按下开始，时间戳: " + now);
            }
        } else {
            if (lastK2Pressed) {
                int duration = (int)Math.min(now - k2PressTime, 1000);
                // System.out.println("[Displayer Debug] K2 松开，持续时间: " + duration + "ms，停止补充新黑块");
            }
        }
        lastK2Pressed = k2Pressed;

        // 按下时持续在右侧补充黑块来模拟长度增长
        if (k1Pressed) {
            int duration = (int)Math.min(now - k1PressTime, 1000);
            int targetLength = Math.max(1, duration / 80);
            
            // 检查右侧是否需要补充黑块
            boolean needRightBlock = true;
            for (KeyBlock b : k1Blocks) {
                // 检查是否有黑块覆盖右侧边缘（考虑向右延伸一格）
                if (b.pos + b.length > ANIM_WIDTH) {
                    needRightBlock = false;
                    break;
                }
            }
            
            if (needRightBlock) {
                // 在右侧补充一个新黑块，长度足够覆盖目标区域
                int blockLength = Math.min(targetLength, ANIM_WIDTH);
                k1Blocks.add(new KeyBlock(ANIM_WIDTH - blockLength + 1, blockLength, false));
                // System.out.println("[Displayer Debug] K1 补充右侧黑块，位置: " + (ANIM_WIDTH - blockLength + 1) + ", 长度: " + blockLength);
            }
        }
        if (k2Pressed) {
            int duration = (int)Math.min(now - k2PressTime, 1000);
            int targetLength = Math.max(1, duration / 80);
            
            // 检查右侧是否需要补充黑块
            boolean needRightBlock = true;
            for (KeyBlock b : k2Blocks) {
                // 检查是否有黑块覆盖右侧边缘（考虑向右延伸一格）
                if (b.pos + b.length > ANIM_WIDTH) {
                    needRightBlock = false;
                    break;
                }
            }
            
            if (needRightBlock) {
                // 在右侧补充一个新黑块，长度足够覆盖目标区域
                int blockLength = Math.min(targetLength, ANIM_WIDTH);
                k2Blocks.add(new KeyBlock(ANIM_WIDTH - blockLength + 1, blockLength, false));
                // System.out.println("[Displayer Debug] K2 补充右侧黑块，位置: " + (ANIM_WIDTH - blockLength + 1) + ", 长度: " + blockLength);
            }
        }

        // 所有黑块都以相同速度左移
        for (KeyBlock b : k1Blocks) {
            b.pos--;
        }
        for (KeyBlock b : k2Blocks) {
            b.pos--;
        }
        
        // 移除超出显示区的黑块
        k1Blocks.removeIf(b -> b.pos + b.length <= 0);
        k2Blocks.removeIf(b -> b.pos + b.length <= 0);
    }
    
    public static long getMinIntervalMs() {
        return PPWindow.getRefreshIntervalMillis();
    }
    
    private static final Displayer INSTANCE = new Displayer();

    public static Displayer getInstance() {
        return INSTANCE;
    }

    public void display(Info gameInfo) {
        if (!Dis_1602a.initialized || !Dis_1602a.sender.isConnected()) {
            System.out.println("[Displayer Debug] 1602a未初始化或未连接，跳过显示");
            return;
        }

        boolean keyAnimEnabled = PPWindow.isKeyAnimationEnabled();
        //System.out.println("[Displayer Debug] 按键动画模式: " + (keyAnimEnabled ? "启用" : "禁用"));
        
        if (keyAnimEnabled) {
            // System.out.println("[Displayer Debug] 当前按键状态 - K1: " + gameInfo.isK1Pressed() + ", K2: " + gameInfo.isK2Pressed());
            updateKeyBlocks(gameInfo);
            sendKeyAnimPacket(gameInfo);
        } else {
            String firstLine = buildDisplayLine(PPWindow.getFirstLineDisplay(), gameInfo);
            String secondLine = buildDisplayLine(PPWindow.getSecondLineDisplay(), gameInfo);
            String displayContent = firstLine + secondLine;
            // System.out.println("[Displayer] 显示内容: '" + displayContent + "'");
            display_costom(displayContent);
        }
    }
    
    private void sendKeyAnimPacket(Info gameInfo) {
        try {
            java.util.List<CharPacket> allChars = new java.util.ArrayList<>();
            
            allChars.addAll(buildAnimLineChars(k1Blocks, gameInfo.getK1Count()));
            allChars.addAll(buildAnimLineChars(k2Blocks, gameInfo.getK2Count()));
            
            // 调试信息：打印动画队列状态
            // System.out.println("[Displayer Debug] 动画队列状态 - K1块数: " + k1Blocks.size() + ", K2块数: " + k2Blocks.size());
            // if (!k1Blocks.isEmpty() || !k2Blocks.isEmpty()) {
            //     System.out.println("[Displayer Debug] K1队列: " + k1Blocks.toString());
            //     System.out.println("[Displayer Debug] K2队列: " + k2Blocks.toString());
            // }
            
            int frameRate = PPWindow.getRefreshIntervalMillis();
            CharPacket[] charArray = allChars.toArray(new CharPacket[0]);
            byte[] packet = ProtocolBuilder.build(frameRate, (Object[])charArray);
            
            if (Dis_1602a.sender.isConnected()) {
                Dis_1602a.sender.send(packet);
                // System.out.println("[Displayer] 发送动画包: " + packet.length + " bytes, 刷新频率: " + frameRate + "ms, 字符数: " + charArray.length);
            } else {
                // System.out.println("[Displayer Debug] 1602a设备未连接，跳过发送");
            }
        } catch (Exception e) {
            System.err.println("[Displayer] 发送动画包失败: " + e.getMessage());
        }
    }
    
    private java.util.List<CharPacket> buildAnimLineChars(java.util.LinkedList<KeyBlock> blocks, int keyCount) {
        java.util.List<CharPacket> chars = new java.util.ArrayList<>();
        
        for (int i = 0; i < ANIM_WIDTH; i++) {
            boolean hasBlock = false;
            
            for (KeyBlock b : blocks) {
                int start = b.pos;
                int end = b.pos + b.length;
                if (i >= start && i < end) {
                    hasBlock = true;
                    break;
                }
            }
            
            if (hasBlock) {
                chars.add(FULL_BLOCK_CHAR);
            } else {
                NormalChar[] spaceChars = NormalChar.fromString(" ");
                for (NormalChar nc : spaceChars) {
                    chars.add(nc);
                }
            }
        }
        
        String valueStr = String.format("%4d", keyCount % 10000);
        NormalChar[] valueChars = NormalChar.fromString(valueStr);
        for (NormalChar nc : valueChars) {
            chars.add(nc);
        }
        
        return chars;
    }
    
    private String buildDisplayLine(String displayType, Info gameInfo) {
        switch (displayType) {
            case "PP":
                return buildPPLine(gameInfo);
            case "KeyOverlay":
                return buildKeyOverlayLine(gameInfo);
            case "Hits":
                return buildHitsLine(gameInfo);
            case "ACC":
                return buildACCLine(gameInfo);
            default:
                return "                ";
        }
    }
    
    private String buildPPLine(Info gameInfo) {
        int states = gameInfo.get_states();
        if (states == 2 || states == 7) {
            return String.format("C:%5.1f  F:%5.1f", gameInfo.get_c_pp(), gameInfo.get_f_pp());
        } else {
            int pp100 = Math.round(gameInfo.getPp100());
            return String.format("SSPP:       %4d", pp100);
        }
    }
    
    private String buildKeyOverlayLine(Info gameInfo) {
        int k1 = gameInfo.getK1Count();
        int k2 = gameInfo.getK2Count();
        int m1 = gameInfo.getM1Count();
        int m2 = gameInfo.getM2Count();
        
        if (k1 > 0 || k2 > 0) {
            return String.format("K1:%4d  K2:%4d", k1 % 1000, k2 % 1000);
        } else {
            return String.format("L:%5d  R:%5d", m1 % 10000, m2 % 10000);
        }
    }
    
    private String buildHitsLine(Info gameInfo) {
        return String.format("%4d/%3d/%3d/%3d", 
            gameInfo.get_s300(),
            gameInfo.get_s100(), 
            gameInfo.get_s50(),
            gameInfo.get_smiss());
    }
    
    private String buildACCLine(Info gameInfo) {
        int states = gameInfo.get_states();
        if (states == 2 || states == 7) {
            int total = gameInfo.get_s300() + gameInfo.get_s100() + gameInfo.get_s50() + gameInfo.get_smiss();
            if (total > 0) {
                double acc = (gameInfo.get_s300() * 300.0 + gameInfo.get_s100() * 100.0 + gameInfo.get_s50() * 50.0) / (total * 300.0) * 100.0;
                return String.format("ACC:     %6.2f%%", acc);
            } else {
                return "ACC:       0.00%";
            }
        } else {
            int pp95 = Math.round(gameInfo.getPp95());
            int pp98 = Math.round(gameInfo.getPp98());
            return String.format("95:%3d    98:%3d", pp95, pp98);
        }
    }
}