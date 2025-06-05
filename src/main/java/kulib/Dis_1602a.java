package kulib;

import picoapi.*;

public class Dis_1602a {
    public static PicoLink pico = new PicoLink("192.168.8.125", 13000);

    public static void display_costom(String customString){
        try{
            pico.connect();
            pico.send(ProtocolBuilder.build(
                0,
                (Object[])NormalChar.fromString(customString)
                ));
        }
        catch (Exception e) {
            //e.printStackTrace();
            pico.close();
            System.err.println("1602发送失败: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        finally{
            //pico.close();
        }
    }
}
