package kulib;

import picoapi.*;

public class Dis_1602a {
    public static Sender sender = new Sender("192.168.8.125", 13000);

    public static void display_costom(String customString){
        try{
            sender.connect();
            sender.send(ProtocolBuilder.build(
                (Object[])NormalChar.fromString(customString)
                ));
        }
        catch (Exception e) {
            //e.printStackTrace();
            sender.close();
            System.err.println("1602发送失败: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        finally{
            //sender.close();
        }
    }
}
