package kulib;

import java.io.IOException;

import picoapi.*;

public class Dis_1602a {
    public static Sender sender = new Sender(PPWindow.getDeviceUrl(), 13000);
    volatile static boolean initialized = false;

    public static void init_display(){
        try {
            System.out.println("try to connect " + PPWindow.getDeviceUrl());
            sender.connect();
            initialized = true;
        } catch (IOException e) {
            initialized = false;
            System.err.println("1602连接失败: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    public static void display_costom(String customString){
        if(initialized){
            try{
                int frameRateMs = PPWindow.getRefreshIntervalMillis();
                sender.send(ProtocolBuilder.build(
                    frameRateMs,
                    (Object[])NormalChar.fromString(customString)
                ));
            }
            catch (Exception e) {
                sender.close();
                System.err.println("1602发送失败: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
    }
}
