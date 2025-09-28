package kulib;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonReader {
    private Gameplay gameplay = new Gameplay();

    public void parse(String rawJson) throws Exception {
        // 读取JSON文件,将内容给status
        ObjectMapper mapper = new ObjectMapper();
        OsuStatus status = mapper.readValue(rawJson, OsuStatus.class);

        gameplay = new Gameplay();

        // 获取PP对象并发送给gameplay
        gameplay.setpp(status.gameplay.pp);
        // 获取hits Map并发送给gameplay
        gameplay.setHits(status.gameplay.hits);
        // 获取keyOverlay Map并发送给gameplay
        gameplay.setKeyOverlay(status.gameplay.getKeyOverlay());

        // 读取ACC PP
        if (status.menu != null) {
            gameplay.setStates(status.menu.getState());
            gameplay.setMenuPP(status.menu.pp);
        }
    }

    public Gameplay getGameplay() {
        return gameplay;
    }
}
