package kulib;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Api {
    // 线程池管理异步任务
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final AtomicLong lastSendTime = new AtomicLong(0); // 线程安全的时间戳

    // 单例模式确保API实例唯一
    private static final Api INSTANCE = new Api();
    public static Api getInstance() {
        return INSTANCE;
    }

    public String getJson() {
        try {
            URL url = new URL(PPWindow.getJsonIp() + "/json");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(2000); // 连接超时2秒
            conn.setReadTimeout(2000);    // 读取超时2秒

            int responseCode = conn.getResponseCode();
            if (responseCode == 500) {
                Platform.runLater(() -> PPWindow.showErrorHint("请打开OSU"));
                conn.disconnect();
                return null;
            }

            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder content = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                return content.toString();
            } finally {
                conn.disconnect();
            }
        } catch (IOException e) {
            if (e.getMessage() != null && e.getMessage().contains("Connection refused")) {
                Platform.runLater(() -> PPWindow.showErrorHint("请打开TOSU"));
            }
            System.err.printf("JSON请求失败 %s%n",e.getMessage());
            try {
                Thread.sleep(2000); // 重试间隔2秒
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
    return null;
    }

    public Info fetchGameData() {
        String json = getJson();
        if (json == null) {
            return null;
        }

        JsonReader reader = new JsonReader();
        try {
            reader.parse(json);
        } catch (Exception e) {
            System.err.println("JSON解析错误: " + e.getMessage());
            return null;
        }

        if (reader.getGameplay() == null || reader.getGameplay().getpp() == null) {
            return null;
        }

        try {
            Gameplay gameplay = reader.getGameplay();
            Info gameInfo = new Info(
                    gameplay.getpp(),
                    gameplay.getHits(),
                    gameplay.getMenuPP(),
                    gameplay.getKeyOverlay(),
                    gameplay.getStates()
            );

            // 速率限制，确保发送和更新时间原子性
            long now = System.currentTimeMillis();
            if (now - lastSendTime.get() >= Displayer.getMinIntervalMs()) {
                lastSendTime.set(now);
                //System.out.println("[API] 状态为: " + gameInfo.get_states() + " @ " + gameInfo.hashCode());
                //System.out.println(now+" "+lastSendTime.get()+"=="+(now - lastSendTime.get()));
                new Thread(() -> Displayer.getInstance().display(gameInfo)).start();
            }
            return gameInfo;
        } catch (Exception e) {
            System.err.println("数据构建错误: " + e.getMessage());
        }
        return null;
    }

    // 关闭线程池资源
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
