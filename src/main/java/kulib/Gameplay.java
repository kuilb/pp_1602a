package kulib;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Gameplay {
    public Current_pp pp;
    public Map<String, Object> hits;
    private Map<String, Double> menuPP;
    private Map<String, Object> keyOverlay;
    private int states;

    // PP类
    public Current_pp getpp(){
        return pp;
    }
    public void setpp(Current_pp pp){
        this.pp=pp;
    }

    // Hits类
    public Map<String, Object> getHits() {
        return hits;
    }
    public void setHits(Map<String, Object> hits) {
        this.hits = hits;
    }

    // ACC PP类
    public void setMenuPP(Map<String, Double> menuPP) {
        this.menuPP = menuPP;
    }
    public Map<String, Double> getMenuPP() {
        return menuPP;
    }

    // KeyOverlay类
    public void setKeyOverlay(Map<String, Object> keyOverlay) {
        this.keyOverlay = keyOverlay;
    }
    public Map<String, Object> getKeyOverlay() {
        return keyOverlay;
    }

    // 状态标志
    public void setStates(int states) {
        this.states = states;
    }
    public int getStates() {
        return states;
    }

}
