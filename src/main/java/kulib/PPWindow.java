package kulib;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.function.Supplier;

public class PPWindow extends Application {
    // 动态错误提示
    public static void showErrorHint(String msg) {
        Platform.runLater(() -> {
            // 只更新errorHint文本
            // 可根据需要扩展为弹窗或其他UI
            // 需保证errorHint已初始化
            // 建议只在UI线程调用
            // 这里假定errorHint为static或可访问
            // 如果不是static可用单例或其他方式
            // 这里直接用全局静态
            // 已删除无效类型转换代码
            // 简单做法：直接设置静态errorHint文本
            // 推荐将errorHint设为static
            errorHint.setText(msg);
            // 显示错误区
            // 这里假定errorContent可访问
            // 推荐将errorContent设为static
        });
    }
    // 刷新间隔 Getter
    public static int getRefreshIntervalMillis() {
        return refreshIntervalMillis;
    }
    // 配置文件路径
    private static final String CONFIG_FILE = "config.properties";

    // 初始化配置（读取或创建）
    private static void initConfig() {
        java.io.File file = new java.io.File(CONFIG_FILE);
        java.util.Properties props = new java.util.Properties();
        boolean needSave = false;
        if (file.exists()) {
            try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
                props.load(fis);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
            // 读取或设置默认值
            String intervalStr = props.getProperty("refreshIntervalMillis");
            String urlStr = props.getProperty("deviceUrl");
            String jsonIpStr = props.getProperty("jsonIp");
            String firstLineStr = props.getProperty("firstLineDisplay");
            String secondLineStr = props.getProperty("secondLineDisplay");
            String keyAnimStr = props.getProperty("keyAnimationEnabled");
            // 刷新间隔
            if (intervalStr != null) {
                try {
                    refreshIntervalMillis = Integer.parseInt(intervalStr);
                } catch (NumberFormatException e) {
                    refreshIntervalMillis = 250;
                    needSave = true;
                }
            } else {
                refreshIntervalMillis = 250;
                props.setProperty("refreshIntervalMillis", String.valueOf(refreshIntervalMillis));
                needSave = true;
            }
            // 设备IP，防止为NULL或空
            if (urlStr != null && !urlStr.trim().isEmpty() && !"NULL".equalsIgnoreCase(urlStr.trim())) {
                deviceUrl = urlStr.trim();
                System.out.println("1602a URL loaded: " + deviceUrl);
                Dis_1602a.init_display(); // 初始化1602显示器
            } else {
                deviceUrl = "http://192.168.8.125";
                props.setProperty("deviceUrl", deviceUrl);
                needSave = true;
            }
            // JSON IP
            if (jsonIpStr != null && !jsonIpStr.trim().isEmpty() && !"NULL".equalsIgnoreCase(jsonIpStr.trim())) {
                jsonIp = jsonIpStr.trim();
            } else {
                jsonIp = "http://127.0.0.1:24050";
                props.setProperty("jsonIp", jsonIp);
                needSave = true;
            }
            // 第一行显示设置
            if (firstLineStr != null && !firstLineStr.trim().isEmpty()) {
                firstLineDisplay = firstLineStr.trim();
            } else {
                firstLineDisplay = "PP";
                props.setProperty("firstLineDisplay", firstLineDisplay);
                needSave = true;
            }
            // 第二行显示设置
            if (secondLineStr != null && !secondLineStr.trim().isEmpty()) {
                secondLineDisplay = secondLineStr.trim();
            } else {
                secondLineDisplay = "Hits";
                props.setProperty("secondLineDisplay", secondLineDisplay);
                needSave = true;
            }
            // 按键动画启用状态
            if (keyAnimStr != null) {
                keyAnimationEnabled = Boolean.parseBoolean(keyAnimStr);
            } else {
                keyAnimationEnabled = false;
                props.setProperty("keyAnimationEnabled", String.valueOf(keyAnimationEnabled));
                needSave = true;
            }
        // 如果需要保存默认值，写回文件
        if (needSave) {
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
                props.store(fos, "PPWindow 配置");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 保存配置到文件
    private static void saveConfig() {
        java.util.Properties props = new java.util.Properties();
        props.setProperty("refreshIntervalMillis", String.valueOf(refreshIntervalMillis));
        props.setProperty("deviceUrl", deviceUrl);
        props.setProperty("jsonIp", jsonIp);
        props.setProperty("firstLineDisplay", firstLineDisplay);
        props.setProperty("secondLineDisplay", secondLineDisplay);
        props.setProperty("keyAnimationEnabled", String.valueOf(keyAnimationEnabled));
        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(CONFIG_FILE)) {
            props.store(fos, "PPWindow 配置");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // 设置默认值
    private static int refreshIntervalMillis; // 默认250毫秒刷新间隔
    private static String deviceUrl; // 1602a的IP默认值
    private static String jsonIp; // 获取Json的IP默认值
    
    // 1602a显示设置
    private static String firstLineDisplay = "PP"; // 第一行显示内容：PP、KeyOverlay等
    private static String secondLineDisplay = "Hits"; // 第二行显示内容：Hits、ACC等
    private static boolean keyAnimationEnabled = false; // 按键动画启用状态

    private static Supplier<Info> dataSupplier;  // 外部提供 map 的方法

    // 1602a的IP Getter
    public static String getDeviceUrl() {
        return deviceUrl;
    }

    // Json的IP Getter
    public static String getJsonIp() {
        return jsonIp;
    }
    
    // 显示设置 Getter
    public static String getFirstLineDisplay() {
        return firstLineDisplay;
    }
    
    public static String getSecondLineDisplay() {
        return secondLineDisplay;
    }
    
    // 按键动画启用状态 Getter
    public static boolean isKeyAnimationEnabled() {
        return keyAnimationEnabled;
    }

    public static void setDataSupplier(Supplier<Info> supplier) {
        dataSupplier = supplier;
    }

    // 默认元素
    private final Label currentPPLabel = new Label("");
    private final Label fullPPLabel = new Label("");
    private final Label hitsLabel = new Label("");
    private final Label missLabel = new Label("");

    private final Label statusLabel = new Label("状态：等待中...");

    //选图区元素
    private final Label pp95Label = new Label();
    private final Label pp98Label = new Label();
    private final Label pp100Label = new Label();

    // 错误提示（仅声明，实际初始化在start方法）
    public static Label errorHint;
    public static VBox errorContent;

    private VBox selectContent;  // 选图内容区
    private VBox playContent;    // 打图内容区

    @Override
    public void start(Stage stage) {
    // 初始化配置文件，读取变量
    initConfig();
        // 选图内容
        selectContent = new VBox(
            10,
            new Label("ACC PP"),
            pp95Label,
            pp98Label,
            pp100Label
        );
        selectContent.setStyle("-fx-alignment: center;");

        // 打图内容
        playContent = new VBox(
            10, 
            currentPPLabel, 
            fullPPLabel, 
            hitsLabel, 
            missLabel
        );
        playContent.setStyle("-fx-alignment: center;");

        // 错误提示
        errorHint = new Label("请检查TOSU和OSU是否正常启动");
        errorContent = new VBox(
            10, 
            statusLabel, 
            errorHint
        );
        errorContent.setStyle("-fx-alignment: center; -fx-border-color: red; -fx-border-width: 2;");
        errorHint.setStyle("-fx-text-fill: red;");

        // StackPane 覆盖内容区域
        StackPane contentPane = new StackPane(selectContent, playContent, errorContent);

        // 初始化只显示 mainContent
        playContent.setVisible(true);
        errorContent.setVisible(false);

        Button settingsButton = new Button("设置");
        settingsButton.setOnAction(e -> openSettingsWindow());
        
        Button displaySettingsButton = new Button("显示设置");
        displaySettingsButton.setOnAction(e -> openDisplaySettingsWindow());
        
        // 按钮用水平布局HBox
        javafx.scene.layout.HBox buttonBox = new javafx.scene.layout.HBox(10, settingsButton, displaySettingsButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10));

        // root视图
        BorderPane root = new BorderPane();
        root.setStyle("-fx-padding: 20; -fx-font-size: 16; -fx-alignment: center;");
        // 内容居中显示
        root.setCenter(contentPane);
        settingsButton.setStyle("-fx-alignment: center;");
        root.setBottom(buttonBox);

        Scene scene = new Scene(root, 330, 220);
        stage.setTitle("PP 显示窗口");
        stage.setScene(scene);
        stage.show();

        // 设置关闭事件
        stage.setOnCloseRequest(event -> {
            Dis_1602a.display_costom(" Disconnected");
            Platform.exit();
            System.exit(0);
        });

        startUpdater();
    }

    private void openSettingsWindow() {
        Stage settingsStage = new Stage();
        settingsStage.setTitle("设置");

        TextField urlField = new TextField(deviceUrl);
        TextField jsonIpField = new TextField(jsonIp); // 新增的 JSON IP 输入框
        TextField intervalField = new TextField(String.valueOf(refreshIntervalMillis));

        Button saveButton = new Button("保存");
        saveButton.setOnAction(e -> {
            // 从输入框读取文本，赋值给deviceUrl
            deviceUrl = urlField.getText().trim();
            System.out.println("1602a地址已保存: " + deviceUrl);

            // 保存刷新间隔，转换数字，防止非法输入
            try {
                int interval = Integer.parseInt(intervalField.getText().trim());
                if (interval <= 0) throw new NumberFormatException();
                refreshIntervalMillis = interval;
                System.out.println("刷新时间已保存: " + refreshIntervalMillis);
            } catch (NumberFormatException ex) {
                // 弹窗提示错误
                Alert alert = new Alert(Alert.AlertType.ERROR, "刷新间隔必须是正整数（毫秒）！");
                alert.showAndWait();
                return; // 不关闭窗口，不保存
            }

            // 保存 JSON IP
            jsonIp = jsonIpField.getText().trim();
            System.out.println("JSON IP 已保存：" + jsonIp);

            // 保存到配置文件
            saveConfig();

            // 关闭设置窗口
            settingsStage.close();
        });

        VBox settingsLayout = new VBox(
                10,
                new Label("1602a地址:"), urlField,
                new Label("JSON数据地址:"), jsonIpField,
                new Label("刷新间隔（毫秒）:"), intervalField,
                saveButton
        );
        settingsLayout.setStyle("-fx-padding: 60; -fx-alignment: center;");

        settingsStage.setScene(new Scene(settingsLayout, 300, 200));
        settingsStage.show();
    }

    private void openDisplaySettingsWindow() {
        Stage displaySettingsStage = new Stage();
        displaySettingsStage.setTitle("显示设置");

        // 按键动画勾选框
        javafx.scene.control.CheckBox keyAnimCheckBox = new javafx.scene.control.CheckBox("启用按键动画");
        keyAnimCheckBox.setSelected(keyAnimationEnabled); // 设置为当前配置值

        // 信息显示选单
        ComboBox<String> firstLineCombo = new ComboBox<>();
        firstLineCombo.getItems().addAll("PP", "KeyOverlay", "Hits", "ACC");
        firstLineCombo.setValue(firstLineDisplay);
        ComboBox<String> secondLineCombo = new ComboBox<>();
        secondLineCombo.getItems().addAll("Hits", "ACC", "PP", "KeyOverlay");
        secondLineCombo.setValue(secondLineDisplay);

        // 根据当前按键动画状态设置初始禁用状态
        firstLineCombo.setDisable(keyAnimationEnabled);
        secondLineCombo.setDisable(keyAnimationEnabled);

        // 勾选时禁用信息选单
        keyAnimCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            firstLineCombo.setDisable(newVal);
            secondLineCombo.setDisable(newVal);
        });

        Button saveButton = new Button("保存");
        saveButton.setOnAction(e -> {
            // 保存按键动画启用状态
            keyAnimationEnabled = keyAnimCheckBox.isSelected();
            
            // 保存信息选单内容（仅未启用动画时）
            if (!keyAnimationEnabled) {
                firstLineDisplay = firstLineCombo.getValue();
                secondLineDisplay = secondLineCombo.getValue();
            }
            
            saveConfig();
            displaySettingsStage.close();
        });

        VBox layout = new VBox(
            18,
            keyAnimCheckBox,
            new Label("1602a 第一行显示:"), firstLineCombo,
            new Label("1602a 第二行显示:"), secondLineCombo,
            saveButton
        );
        layout.setStyle("-fx-padding: 40; -fx-alignment: center;");

        displaySettingsStage.setScene(new Scene(layout, 320, 260));
        displaySettingsStage.show();
    }

    //设置定时刷新窗体
    private void startUpdater() {
        Timeline timeline = new Timeline(
                // 默认每0.25秒刷新一次
                new KeyFrame(Duration.millis(refreshIntervalMillis), e -> updateDisplay())
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    // 从外部获取最新 map,异步获取防止卡死UI
    private Info currentMap;  // 保存异步获取的Info
    private long lastFailTime = 0; // 上次失败调用时间戳（毫秒）
    private boolean isUpdating = false; // 正在更新标志位
    private void updateBeatmap(){
        long now = System.currentTimeMillis();
        if (isUpdating || now - lastFailTime < 2000) { // 若获取失败进入2000ms冷却时间
            return;
        }
        isUpdating = true; // 标记为正在更新

        Task<Info> task = new Task<>() {
            @Override
            protected Info call() {
                return dataSupplier.get(); // 使用新的 dataSupplier
            }
        };

        task.setOnSucceeded(e -> {
            currentMap = task.getValue(); // 保存获取的 beatmap
            isUpdating = false; // 标记为已完成
            updateDisplay(); // 立即刷新 UI
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            statusLabel.setText("状态：获取失败：" + (ex != null ? ex.getMessage() : "未知错误"));
            lastFailTime = System.currentTimeMillis();  // 设置冷却时间起点
            isUpdating = false; // 标记为已完成
            if (ex != null) {
                ex.printStackTrace();
            }
        });

        new Thread(task).start();
    }

    // 更新窗体显示数据
    private void updateDisplay() {
            updateBeatmap();
            if (currentMap == null) {
                statusLabel.setText("状态：无数据\n");
                errorContent.setVisible(true);
                errorContent.setManaged(true);
                playContent.setVisible(false);
                playContent.setManaged(false);
                return;
            }

            // 如果 currentMap 有效，隐藏错误提示
            errorContent.setVisible(false);
            errorContent.setManaged(false);
            playContent.setVisible(true);
            errorContent.setVisible(false);

            int state = currentMap.get_states();
            //System.out.println(state);
            if (state == 2 || state == 7) {
                // 显示打图内容
                playContent.setVisible(true);
                playContent.setManaged(true);
                selectContent.setVisible(false);
                selectContent.setManaged(false);

                currentPPLabel.setText("当前 PP:" + String.format("%.2f", currentMap.get_c_pp()));
                fullPPLabel.setText("FC PP:" + String.format("%.2f", currentMap.get_f_pp()));
                hitsLabel.setText(
                "300:" + currentMap.get_s300() + 
                "  100:" + currentMap.get_s100() + 
                "  50:" + currentMap.get_s50());
                missLabel.setText("Miss:" + currentMap.get_smiss() + "  SB:" + currentMap.get_sb());
                statusLabel.setText("");
            }
            else{
                // 显示选图内容
                playContent.setVisible(false);
                playContent.setManaged(false);
                selectContent.setVisible(true);
                selectContent.setManaged(true);

                // 更新 ACC PP 显示
                pp95Label.setText("95% PP: " + String.format("%.2f", currentMap.getPp95()));
                pp98Label.setText("98% PP: " + String.format("%.2f", currentMap.getPp98()));
                pp100Label.setText("100% PP: " + String.format("%.2f", currentMap.getPp100()));
                // 提前返回，不执行打图内容
            }
    }
}
