package com.billiards.client.scenes;

import com.billiards.client.AvatarManager;
import com.billiards.client.NetworkClient;
import com.billiards.client.game.Ball;
import com.billiards.client.game.PhysicsEngine;
import com.billiards.client.game.CueSkin;
import com.billiards.client.game.CueSkinManager;
import com.billiards.client.protocol.PacketType;
import com.billiards.client.audio.SoundManager;
import com.billiards.client.theme.SilverCornerPane;
import com.billiards.client.vfx.NatureAmbience;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.text.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Màn hình Gameplay Đẳng Cấp Cao – Bi-a 8 bi (8-Ball Pool)
 * 
 * Tính năng đồ họa & âm thanh đột phá:
 * - Bàn bi-a 3D Luxury: Gỗ mun/óc chó sang trọng, viền vàng kim loại, 18 điểm ngắm kim cương xà cừ, nỉ nhung ánh sáng đèn rọi tâm bàn, 6 lỗ mạ đồng sâu thẳm.
 * - Bi bi-a 3D Realistic: Đổ bóng thực tế, khối cầu 3D specular highlight, dải sọc uốn cong, hoạt ảnh bi xoay & hút vào lỗ (sinking animation).
 * - Hệ thống hạt & hiệu ứng động (VFX):
 *   + Sóng xung kích va chạm (Collision Shockwaves)
 *   + Bụi phấn lơ đầu cơ (Chalk Puff Particles)
 *   + Banner thông báo lượt trượt động (Turn Toast Announcement)
 *   + Pháo hoa giấy chúc mừng chiến thắng (Victory Confetti)
 * - Đường ngắm Laser Neon & Bóng Ảo (Ghost Ball) dự đoán quỹ đạo bi trắng và bi đích.
 * - Đồng hồ đếm ngược sinh động (Pulsing Timer) kèm âm thanh tick-tock khi < 10s.
 * - Nút chọn & phát nhạc nền bản quyền/cá nhân nhanh ngay trên thanh HUD.
 */
public class GameScene {

    // ────── Bàn bi-a ─────────────────────────────────────────────
    private static final double CANVAS_W  = 980;
    private static final double CANVAS_H  = 520;
    private static final double TABLE_X   = 45;
    private static final double TABLE_Y   = 48;
    private static final double TABLE_W   = 760;
    private static final double TABLE_H   = 424;
    private static final double POCKET_R  = 23.0;

    // ────── Tham chiếu ──────────────────────────────────────────
    private final NetworkClient network;
    private final SceneManager  sceneManager;
    private Scene scene;

    private final String myName, opponentName;
    private final int    playerIndex; // 0 hoặc 1
    private boolean isMyTurn;
    private int myAvatarId = 0, oppAvatarId = 0;
    private String myCueColor = "#DEB887", oppCueColor = "#DEB887";

    // ────── Game objects ────────────────────────────────────────
    private List<Ball>    balls;
    private PhysicsEngine physics;
    private boolean       physicsRunning = false;
    private final boolean isPractice;

    // ────── Cú đánh đang chờ áp dụng (nhận từ server) ──────────
    private boolean pendingShot  = false;
    private double  pendingAngle = 0, pendingForce = 0;
    private double  pendingCueX  = 0, pendingCueY  = 0;
    private double  pendingSpinX = 0, pendingSpinY = 0;
    
    // Cờ đánh dấu để chỉ người bắn mới được gửi PHYSICS_DONE
    private boolean iWasShooterThisTurn = false;

    // ────── Đặt bi (Ball in hand) ───────────────────────────────
    private boolean isBreakPlacement = true;
    private boolean ballInHand = false;
    private boolean isDraggingCue = false;
    private boolean cueBallHitSomething = false;
    private boolean shotFiredThisTurn = false;

    // ────── Điểm đặt cơ / Trô bi (Cue Spin / Draw shot) ────────
    private double  cueSpinX = 0.0; // [-1.0, 1.0] Ép-phê trái/phải
    private double  cueSpinY = 0.0; // [-1.0, 1.0] Đáy = Trô bi (>0), Đỉnh = Cule (<0)
    private boolean isDraggingSpin = false;
    private static final double SPIN_PANEL_X  = 840;
    private static final double SPIN_PANEL_Y  = 182;
    private static final double SPIN_PANEL_W  = 124;
    private static final double SPIN_PANEL_H  = 156;
    private static final double SPIN_WIDGET_X = 902;
    private static final double SPIN_WIDGET_Y = 270;
    private static final double SPIN_WIDGET_R = 36.0;

    // ────── Caching Gradients tránh cấp phát GC (Hiệu suất 60 FPS) ──
    private static final LinearGradient WOOD_GRADIENT = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
        new Stop(0.0, Color.web("#1E140E")),
        new Stop(0.5, Color.web("#2A1C14")),
        new Stop(1.0, Color.web("#140D09"))
    );
    private static final RadialGradient FELT_GRADIENT = new RadialGradient(
        0, 0, TABLE_X + TABLE_W / 2, TABLE_Y + TABLE_H / 2, TABLE_W * 0.65, false, CycleMethod.NO_CYCLE,
        new Stop(0.0, Color.web("#1B5E3C")),
        new Stop(0.55, Color.web("#164E32")),
        new Stop(1.0, Color.web("#0F3A24"))
    );
    private static final LinearGradient POWER_GRADIENT = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
        new Stop(0.0, Color.web("#287A57")),
        new Stop(0.7, Color.web("#C2A36B")),
        new Stop(1.0, Color.web("#A83A3A"))
    );
    private static final RadialGradient SPIN_CUE_GRADIENT = new RadialGradient(0, 0,
        SPIN_WIDGET_X - 10, SPIN_WIDGET_Y - 10, SPIN_WIDGET_R, false, CycleMethod.NO_CYCLE,
        new Stop(0.0, Color.WHITE),
        new Stop(0.75, Color.web("#EDF1E8")),
        new Stop(1.0, Color.web("#A5B1A8"))
    );

    // ────── Aiming (nhắm cú đánh) ───────────────────────────────
    private boolean  isAiming      = false;
    private double   aimStartX,    aimStartY;
    private double   aimAngle      = 0;
    private double   aimForce      = 0;
    private static final double MAX_AIM_DIST = 130.0;
    private static final double MAX_FORCE    = 25.0;
    private double   cueIdleTimer  = 0.0;

    // ────── Đối thủ ngắm cú đánh (Opponent Aim Sync) ──────────
    private volatile boolean oppIsAiming = false;
    private volatile double  oppTargetAngle = 0.0;
    private volatile double  oppTargetForceRatio = 0.0;
    private volatile double  oppSpinX = 0.0;
    private volatile double  oppSpinY = 0.0;
    private double           oppRenderAngle = 0.0;
    private double           oppRenderForceRatio = 0.0;

    // ────── Throttling gửi trạng thái ngắm ────────────────────
    private long    lastAimSendTime = 0;
    private double  lastSentAngle   = 0;
    private double  lastSentForce   = 0;
    private boolean lastSentAiming  = false;

    // ────── UI Nodes ────────────────────────────────────────────
    private Canvas canvas;
    private Label  statusLabel;
    private Label  myGroupLabel;
    private Label  oppGroupLabel;
    private HBox   myBallsBox;
    private HBox   oppBallsBox;
    private StackPane myAvatarPane;
    private StackPane oppAvatarPane;
    private Label  timerLabel;
    private Label  pingFpsLabel;
    private Label  bgmLabel;
    private AnimationTimer gameLoop;

    // ────── Đo FPS & Độ trễ (Performance Tracking) ─────────────
    private long lastFpsCalcTime  = 0;
    private int  renderFrameCount = 0;
    private int  currentFps       = 60;

    // ────── Luật 8-Ball ─────────────────────────────────────────
    private List<Integer> myTargetBalls;
    private List<Integer> oppTargetBalls;
    private boolean cueBallPocketed    = false;
    private boolean pocketedThisTurn   = false;
    private boolean anyObjectBallPocketedThisTurn = false;
    private boolean eightBallPocketed  = false;
    private boolean gameOver           = false;

    // ────── Timer ───────────────────────────────────────────────
    private long turnStartTime = 0;
    private static final long TURN_LIMIT_MS = 60000; // 60s
    private int lastTickSecond = -1;

    // ────── VFX Particle Systems ────────────────────────────────
    private final List<Shockwave> shockwaves = new ArrayList<>();
    private final List<ChalkParticle> chalkParticles = new ArrayList<>();
    private final List<CueTrailParticle> cueTrailParticles = new ArrayList<>();
    private final List<ConfettiParticle> confettiList = new ArrayList<>();

    // Hiệu ứng thiên nhiên cây lá và bọ côn trùng
    private final NatureAmbience natureAmbience = new NatureAmbience();
    private final long gameStartTime = System.currentTimeMillis();

    // Theo dõi cú đánh của gậy cơ và animation phản quang
    private double  lastShotStrikeTime  = -100.0;
    private boolean isShotStrikeActive  = false;
    private boolean lastShooterIsMine   = true;
    private double  lastStrikeAngle     = 0.0;
    private double  lastStrikeX         = 0.0;
    private double  lastStrikeY         = 0.0;

    // Toast thông báo lượt
    private String toastMessage = "";
    private double toastAlpha   = 0.0;
    private Color  toastColor   = Color.WHITE;
    private double toastScale   = 1.0;

    // Lớp nội bộ cho vệt đuôi bi cái theo Skin gậy khi di chuyển
    private static class CueTrailParticle {
        double x, y;
        double alpha = 0.85;
        double size;
        Color color;
        boolean isStar;

        CueTrailParticle(double x, double y, String skinId) {
            this.x = x + (Math.random() - 0.5) * 6.0;
            this.y = y + (Math.random() - 0.5) * 6.0;
            switch (skinId != null ? skinId : "") {
                case CueSkinManager.ID_HERCULES -> {
                    this.color = (Math.random() > 0.3) ? Color.web("#FFD700") : Color.web("#FFF8DC");
                    this.size = 2.4 + Math.random() * 2.6;
                    this.isStar = Math.random() > 0.5;
                }
                case CueSkinManager.ID_LUCANUS -> {
                    this.color = (Math.random() > 0.3) ? Color.web("#FF3333") : Color.web("#FF7675");
                    this.size = 2.2 + Math.random() * 2.2;
                    this.isStar = false;
                }
                case CueSkinManager.ID_ATLAS -> {
                    this.color = (Math.random() > 0.3) ? Color.web("#AF52DE") : Color.web("#D1D8E0");
                    this.size = 2.0 + Math.random() * 2.2;
                    this.isStar = false;
                }
                case CueSkinManager.ID_KABUTO -> {
                    this.color = (Math.random() > 0.4) ? Color.web("#2ECC71") : Color.web("#F39C12");
                    this.size = 2.0 + Math.random() * 2.0;
                    this.isStar = false;
                }
                default -> {
                    this.color = Color.rgb(220, 220, 220);
                    this.size = 1.6 + Math.random() * 1.6;
                    this.isStar = false;
                }
            }
        }

        boolean update() {
            alpha -= 0.045;
            size *= 0.96;
            return alpha > 0.0;
        }
    }

    // Lớp nội bộ cho Shockwave
    private static class Shockwave {
        double x, y;
        double radius = 5;
        double maxRadius;
        double alpha = 0.9;
        Color color;

        Shockwave(double x, double y, double forceRatio) {
            this.x = x;
            this.y = y;
            this.maxRadius = 15 + forceRatio * 25;
            this.color = Color.rgb(255, 235, 150);
        }

        boolean update() {
            radius += 1.8;
            alpha = Math.max(0.0, 1.0 - (radius / maxRadius));
            return radius < maxRadius;
        }
    }

    // Lớp nội bộ cho hạt bụi phấn lơ
    private static class ChalkParticle {
        double x, y, vx, vy;
        double alpha = 1.0;
        double size;
        Color color;

        ChalkParticle(double x, double y, double dirX, double dirY) {
            this.x = x;
            this.y = y;
            double speed = 1.0 + Math.random() * 2.5;
            double spread = (Math.random() - 0.5) * 1.5;
            this.vx = dirX * speed + (-dirY) * spread;
            this.vy = dirY * speed + dirX * spread;
            this.size = 2.0 + Math.random() * 3.0;
            this.color = (Math.random() > 0.4) ? Color.web("#4fc3f7") : Color.WHITE;
        }

        boolean update() {
            x += vx;
            y += vy;
            vx *= 0.92;
            vy *= 0.92;
            alpha -= 0.04;
            return alpha > 0.0;
        }
    }

    // Lớp nội bộ cho pháo hoa chiến thắng
    private static class ConfettiParticle {
        double x, y, vx, vy, rot, rotSpeed;
        double size;
        Color color;

        ConfettiParticle(double x, double y) {
            this.x = x;
            this.y = y;
            this.vx = (Math.random() - 0.5) * 8.0;
            this.vy = -3.0 - Math.random() * 6.0;
            this.rot = Math.random() * 360;
            this.rotSpeed = (Math.random() - 0.5) * 12.0;
            this.size = 5.0 + Math.random() * 6.0;
            Color[] colors = {
                Color.web("#ff4757"), Color.web("#2ed573"), Color.web("#1e90ff"),
                Color.web("#ffa502"), Color.web("#9b59b6"), Color.web("#f1c40f")
            };
            this.color = colors[(int)(Math.random() * colors.length)];
        }

        void update() {
            x += vx;
            y += vy;
            vy += 0.15; // trọng lực
            vx *= 0.98;
            rot += rotSpeed;
        }
    }

    // ============================================================
    // CONSTRUCTORS
    // ============================================================
    public GameScene(NetworkClient network, SceneManager sceneManager,
                     String myName, String opponentName,
                     int playerIndex, boolean isFirstTurn) {
        this(network, sceneManager, myName, 0, "#DEB887", opponentName, 0, "#DEB887", playerIndex, isFirstTurn);
    }

    public GameScene(NetworkClient network, SceneManager sceneManager,
                     String myName, int myAvatarId, String myCueColor,
                     String opponentName, int oppAvatarId, String oppCueColor,
                     int playerIndex, boolean isFirstTurn) {
        this(network, sceneManager, myName, myAvatarId, myCueColor, opponentName, oppAvatarId, oppCueColor,
             playerIndex, isFirstTurn, System.nanoTime() ^ ((long) (Math.random() * 1_000_000_000L) << 32));
    }

    public GameScene(NetworkClient network, SceneManager sceneManager,
                     String myName, int myAvatarId, String myCueColor,
                     String opponentName, int oppAvatarId, String oppCueColor,
                     int playerIndex, boolean isFirstTurn, long matchSeed) {
        this.network       = network;
        this.sceneManager  = sceneManager;
        this.myName        = myName;
        this.myAvatarId    = myAvatarId;
        this.myCueColor    = (myCueColor != null && !myCueColor.isEmpty()) ? myCueColor : "#DEB887";
        this.opponentName  = opponentName;
        this.oppAvatarId   = oppAvatarId;
        this.oppCueColor   = (oppCueColor != null && !oppCueColor.isEmpty()) ? oppCueColor : "#DEB887";
        this.playerIndex   = playerIndex;
        this.isMyTurn      = isFirstTurn;
        this.isPractice    = false;
        
        // 14 bi mục tiêu (1-7 và 9-15) được xáo ngẫu nhiên hoàn toàn theo matchSeed
        List<Integer> allTargetBalls = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            if (i == 8) continue;
            allTargetBalls.add(i);
        }
        Collections.shuffle(allTargetBalls, new Random(matchSeed));
        
        List<Integer> group1 = new ArrayList<>(allTargetBalls.subList(0, 7));
        List<Integer> group2 = new ArrayList<>(allTargetBalls.subList(7, 14));
        
        if (playerIndex == 0) {
            myTargetBalls = group1;
            oppTargetBalls = group2;
        } else {
            myTargetBalls = group2;
            oppTargetBalls = group1;
        }
        
        this.turnStartTime = System.currentTimeMillis();
        this.isBreakPlacement = true;
        this.ballInHand = isFirstTurn;
        if (isMyTurn) {
            showTurnToast("🎱 PHÁ BI: Đặt bi cái tự do sau vạch trắng!", Color.web("#2ecc71"));
            SoundManager.playTurnAlert();
        } else {
            showTurnToast("⏳ ĐỐI THỦ PHÁ BI (Đặt bi sau vạch)...", Color.web("#f39c12"));
        }

        initBalls(matchSeed);
        initPhysics();
        build();
        startGameLoop();
        setupNetworkCallback();
        updateGroupLabels();
    }

    // Constructor Practice (Luyện tập)
    public GameScene(NetworkClient network, SceneManager sceneManager, String myName) {
        this.network       = network;
        this.sceneManager  = sceneManager;
        this.myName        = myName;
        this.opponentName  = "";
        this.playerIndex   = 0;
        this.isMyTurn      = true;
        this.isPractice    = true;

        var session = (network != null) ? network.getSession() : null;
        this.myAvatarId    = (session != null) ? session.avatarId : 0;
        this.myCueColor    = (session != null && session.cueColor != null) ? session.cueColor : "#DEB887";
        this.oppAvatarId   = 0;
        this.oppCueColor   = "#DEB887";
        
        // Mỗi lần luyện tập tạo seed ngẫu nhiên độc lập hoàn toàn
        long practiceSeed = System.nanoTime() ^ ((long) (Math.random() * 1_000_000_000L) << 32);
        
        // Chế độ luyện tập: Dọn sạch toàn bộ 15 bi mục tiêu trên bàn
        myTargetBalls = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            myTargetBalls.add(i);
        }
        oppTargetBalls = null;

        this.isBreakPlacement = true;
        this.ballInHand = true;

        initBalls(practiceSeed);
        initPhysics();
        build();
        startGameLoop();
        updateGroupLabels();
        showTurnToast("🎱 LUYỆN TẬP: Đặt bi cái tự do và dọn sạch 15 bi trên bàn!", Color.web("#00d2ff"));
    }

    // ============================================================
    // KHỞI TẠO BI & VẬT LÝ
    // ============================================================
    private void initBalls(long seed) {
        balls = new ArrayList<>();
        double cx = TABLE_X + TABLE_W / 2;
        double cy = TABLE_Y + TABLE_H / 2;

        // Cue ball (bi trắng)
        balls.add(new Ball(0, cx - TABLE_W / 4, cy));

        // 14 bi mục tiêu được xáo ngẫu nhiên theo seed, bi 8 luôn ở trung tâm hình tam giác (vị trí index 4)
        List<Integer> rackBalls = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            if (i == 8) continue;
            rackBalls.add(i);
        }
        Collections.shuffle(rackBalls, new Random(seed));

        // Chèn bi 8 vào vị trí trung tâm tam giác (hàng 3, chính giữa - index 4 của 15 bi)
        rackBalls.add(4, 8);

        double tipX = cx + TABLE_W / 4;
        double tipY = cy;
        double r    = Ball.RADIUS * 2 + 0.6;

        int idx = 0;
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col <= row; col++) {
                double bx = tipX + row * r * Math.cos(Math.toRadians(30));
                double by = tipY - row * r / 2.0 + col * r;
                balls.add(new Ball(rackBalls.get(idx++), bx, by));
            }
        }
    }

    private void initPhysics() {
        physics = new PhysicsEngine(TABLE_X, TABLE_Y, TABLE_W, TABLE_H);
        physics.setPocketCallback(this::handleBallPocketed);
        physics.setCollisionListener((colX, colY, forceRatio) -> {
            shockwaves.add(new Shockwave(colX, colY, forceRatio));
        });
    }

    // ============================================================
    // TOAST NOTIFICATION
    // ============================================================
    private void showTurnToast(String message, Color color) {
        this.toastMessage = message;
        this.toastColor   = color;
        this.toastAlpha   = 1.0;
        this.toastScale   = 1.25;
    }

    // ============================================================
    // BUILD GIAO DIỆN CHÍNH
    // ============================================================
    private void build() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #111816;");

        // ── TOP: Modern Billiards Club Status Bar ───────────────────
        HBox topBar = new HBox(16);
        topBar.setAlignment(Pos.CENTER);
        topBar.setPadding(new Insets(8, 18, 8, 18));
        topBar.setStyle("-fx-background-color: #1A2420; -fx-border-color: #34443B; -fx-border-width: 0 0 1px 0;");

        // Player 1 Card (Tôi)
        myAvatarPane = AvatarManager.createSmallAvatar(myAvatarId);
        Label myLabel = new Label(myName);
        myLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        myLabel.setTextFill(Color.web("#EDF1E8"));

        myGroupLabel = new Label("Nhóm: ...");
        myGroupLabel.setFont(Font.font("Segoe UI", 11));
        myGroupLabel.setTextFill(Color.web("#A5B1A8"));

        myBallsBox = new HBox(3);
        myBallsBox.setAlignment(Pos.CENTER);

        VBox myBox = new VBox(2, new HBox(8, myAvatarPane, new VBox(2, myLabel, myGroupLabel)), myBallsBox);
        myBox.setAlignment(Pos.CENTER_LEFT);

        // VS Badge tinh tế
        StackPane vsBadge = new StackPane();
        Circle vsCircle = new Circle(14, Color.web("#24322B"));
        vsCircle.setStroke(Color.web("#34443B"));
        vsCircle.setStrokeWidth(1.0);
        Label vsLbl = new Label("VS");
        vsLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        vsLbl.setTextFill(Color.web("#C2A36B"));
        vsBadge.getChildren().addAll(vsCircle, vsLbl);

        // Player 2 Card (Ẩn hoàn toàn khi luyện tập solo)
        VBox oppBox = new VBox();
        if (isPractice) {
            vsBadge.setVisible(false);
            vsBadge.setManaged(false);
            oppBox.setVisible(false);
            oppBox.setManaged(false);

            oppBallsBox = new HBox(3);
            oppBallsBox.setVisible(false);
            oppBallsBox.setManaged(false);
        } else {
            oppAvatarPane = AvatarManager.createSmallAvatar(oppAvatarId);
            Label oppLabel = new Label(opponentName);
            oppLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            oppLabel.setTextFill(Color.web("#EDF1E8"));

            oppGroupLabel = new Label("Nhóm: ...");
            oppGroupLabel.setFont(Font.font("Segoe UI", 11));
            oppGroupLabel.setTextFill(Color.web("#A5B1A8"));

            oppBallsBox = new HBox(3);
            oppBallsBox.setAlignment(Pos.CENTER);

            oppBox = new VBox(2, new HBox(8, oppAvatarPane, new VBox(2, oppLabel, oppGroupLabel)), oppBallsBox);
            oppBox.setAlignment(Pos.CENTER_RIGHT);
        }

        // Status Center & Timer
        statusLabel = new Label();
        statusLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        updateStatusLabel();

        timerLabel = new Label("60s");
        timerLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        timerLabel.setTextFill(Color.web("#EDF1E8"));
        timerLabel.setStyle("-fx-background-color: #141D1A; -fx-border-color: #34443B; -fx-border-radius: 0; -fx-padding: 3 10; -fx-background-radius: 0;");
        if (isPractice) {
            timerLabel.setVisible(false);
            timerLabel.setManaged(false);
        }

        // Đo độ trễ (Ping RTT) và hiệu năng vẽ (FPS) thời gian thực
        pingFpsLabel = new Label("⚡ 0 ms  |  🎮 60 FPS");
        pingFpsLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        pingFpsLabel.setTextFill(Color.web("#22c55e"));
        pingFpsLabel.setStyle("-fx-background-color: #141D1A; -fx-border-color: #34443B; -fx-border-radius: 0; -fx-padding: 3 8; -fx-background-radius: 0;");
        updatePingFpsDisplay();

        // Audio & Music Label (Cố định Arcade Speed)
        bgmLabel = new Label("🎵 " + SoundManager.getCurrentBgmName());
        bgmLabel.setMaxWidth(160);
        bgmLabel.setFont(Font.font("Segoe UI", 11));
        bgmLabel.setTextFill(Color.web("#A5B1A8"));

        VBox bgmBox = new VBox(2, bgmLabel);
        bgmBox.setAlignment(Pos.CENTER);

        Button exitBtn = new Button("Thoát");
        exitBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        exitBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #A83A3A;" +
            "-fx-border-color: #A83A3A;" +
            "-fx-border-radius: 0;" +
            "-fx-background-radius: 0;" +
            "-fx-padding: 4 10;" +
            "-fx-cursor: hand;"
        );
        exitBtn.setOnAction(e -> {
            SoundManager.playButtonClick();
            if (network != null && !isPractice) network.sendClaimWin(false);
            if (network != null && network.getSession() != null) {
                var s = network.getSession();
                sceneManager.showLobby(s.username, s.exp, s.level, s.wins, s.losses);
            } else {
                sceneManager.showLobby(myName, 0, 1, 0, 0);
            }
        });

        Region sp1 = new Region(); HBox.setHgrow(sp1, Priority.ALWAYS);
        Region sp2 = new Region(); HBox.setHgrow(sp2, Priority.ALWAYS);

        topBar.getChildren().addAll(myBox, sp1, vsBadge, sp2, oppBox, 
                                    new Separator(javafx.geometry.Orientation.VERTICAL),
                                    new VBox(2, statusLabel, new HBox(6, timerLabel, pingFpsLabel)),
                                    new Separator(javafx.geometry.Orientation.VERTICAL),
                                    bgmBox, exitBtn);
        root.setTop(topBar);

        // ── CENTER: Canvas Bàn Bi-a Modern Club ─────────────────
        canvas = new Canvas(CANVAS_W, CANVAS_H);
        StackPane canvasPane = new StackPane(canvas);
        canvasPane.setStyle("-fx-background-color: #111816;");
        root.setCenter(canvasPane);

        canvas.setOnMousePressed(this::onMousePressed);
        canvas.setOnMouseDragged(this::onMouseDragged);
        canvas.setOnMouseReleased(this::onMouseReleased);
        canvas.setOnMouseMoved(this::onMouseMoved);

        // ── BOTTOM: Status & Control Tips đã được lược bỏ để bàn chơi thoáng đãng và tập trung
        scene = new Scene(root, 1000, 650);
    }

    // ============================================================
    // VÒNG LẶP GAME (60fps)
    // ============================================================
    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                cueIdleTimer += 0.04;

                // Tính toán FPS & Ping RTT mỗi 500ms
                renderFrameCount++;
                if (now - lastFpsCalcTime >= 500_000_000L) {
                    if (lastFpsCalcTime > 0) {
                        currentFps = (int) Math.round((renderFrameCount * 1_000_000_000.0) / (now - lastFpsCalcTime));
                    }
                    renderFrameCount = 0;
                    lastFpsCalcTime = now;
                    updatePingFpsDisplay();
                }

                // Countdown Timer Logic
                if (!isPractice && !physicsRunning && !gameOver) {
                    long elapsed = System.currentTimeMillis() - turnStartTime;
                    long remaining = Math.max(0, (TURN_LIMIT_MS - elapsed) / 1000);
                    timerLabel.setText(remaining + "s");

                    if (remaining <= 10) {
                        timerLabel.setTextFill(Color.web("#ef4444"));
                        timerLabel.setStyle("-fx-background-color: rgba(239, 68, 68, 0.25); -fx-padding: 4 10; -fx-background-radius: 12;");
                        if (isMyTurn && lastTickSecond != (int)remaining && remaining > 0) {
                            lastTickSecond = (int)remaining;
                            SoundManager.playTimerTick();
                        }
                    } else if (remaining <= 25) {
                        timerLabel.setTextFill(Color.web("#f59e0b"));
                        timerLabel.setStyle("-fx-background-color: rgba(245, 158, 11, 0.2); -fx-padding: 4 10; -fx-background-radius: 12;");
                    } else {
                        timerLabel.setTextFill(Color.web("#22c55e"));
                        timerLabel.setStyle("-fx-background-color: rgba(34, 197, 94, 0.2); -fx-padding: 4 10; -fx-background-radius: 12;");
                    }

                    if (isMyTurn && elapsed > TURN_LIMIT_MS) {
                        isMyTurn = false;
                        iWasShooterThisTurn = true;
                        if (network != null) network.sendPhysicsDone(false, true);
                    }
                } else if (isPractice) {
                    timerLabel.setText("Tự do");
                    timerLabel.setTextFill(Color.web("#38bdf8"));
                } else {
                    timerLabel.setText("Đang lăn");
                    timerLabel.setTextFill(Color.web("#94a3b8"));
                    timerLabel.setStyle("-fx-background-color: rgba(148, 163, 184, 0.15); -fx-padding: 4 10; -fx-background-radius: 12;");
                }

                // Nội suy góc và lực của gậy đối thủ (60 FPS LERP)
                if (oppIsAiming && !isMyTurn && !physicsRunning) {
                    double angleDiff = oppTargetAngle - oppRenderAngle;
                    while (angleDiff > Math.PI)  angleDiff -= 2 * Math.PI;
                    while (angleDiff < -Math.PI) angleDiff += 2 * Math.PI;
                    oppRenderAngle += angleDiff * 0.35;
                    oppRenderForceRatio += (oppTargetForceRatio - oppRenderForceRatio) * 0.4;
                } else {
                    oppRenderForceRatio = 0.0;
                }

                // Nhận cú đánh từ mạng (Đối thủ bắn)
                if (pendingShot && !physicsRunning) {
                    pendingShot = false;
                    oppIsAiming = false;
                    balls.get(0).setPosition(pendingCueX, pendingCueY);
                    applyCueShot(pendingAngle, pendingForce, pendingSpinX, pendingSpinY, false);
                }

                if (physicsRunning) {
                    physics.update(balls);

                    // Thêm hạt vệt đuôi đặc trưng của Skin gậy khi bi cái di chuyển nhanh
                    if (CueSkinManager.isVfxEnabled()) {
                        Ball cueBall = balls.get(0);
                        if (cueBall.active) {
                            double speedSq = cueBall.vx * cueBall.vx + cueBall.vy * cueBall.vy;
                            if (speedSq > 2200) { // Khi bi cái di chuyển có tốc độ
                                String activeSkin = lastShooterIsMine ? myCueColor : oppCueColor;
                                cueTrailParticles.add(new CueTrailParticle(cueBall.x, cueBall.y, activeSkin));
                            }
                        }
                    }

                    if (!cueBallHitSomething) {
                        for (int i = 1; i < balls.size(); i++) {
                            if (balls.get(i).active && (balls.get(i).vx != 0 || balls.get(i).vy != 0)) {
                                cueBallHitSomething = true;
                                break;
                            }
                        }
                    }

                    if (physics.allBallsStopped(balls)) {
                        physicsRunning = false;
                        onPhysicsComplete();
                    }
                }

                // Cập nhật các hạt & hiệu ứng
                updateVFX();

                // Vẽ frame hoàn chỉnh
                render();
            }
        };
        gameLoop.start();
    }

    private void updateVFX() {
        shockwaves.removeIf(s -> !s.update());
        chalkParticles.removeIf(p -> !p.update());
        cueTrailParticles.removeIf(p -> !p.update());
        for (ConfettiParticle cp : confettiList) cp.update();

        if (toastAlpha > 0.0) {
            toastAlpha -= 0.015;
            if (toastScale > 1.0) toastScale -= 0.015;
            if (toastAlpha < 0) toastAlpha = 0;
        }
    }

    // ============================================================
    // RENDER ĐỒ HỌA BÀN BI-A LUXURY 3D
    // ============================================================
    private void render() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Xóa Canvas nền tối đồng bộ (#111816)
        gc.setFill(Color.web("#111816"));
        gc.fillRect(0, 0, CANVAS_W, CANVAS_H);

        // Hiệu ứng thiên nhiên cây lá đung đưa & con bọ bò xung quanh mép bàn bi-a
        double timeSec = (System.currentTimeMillis() - gameStartTime) / 1000.0;
        natureAmbience.render(gc, CANVAS_W, CANVAS_H, timeSec, true);

        // 1. Khung Gỗ Tối Cổ Điển Hình Vuông Tráng Bạc 4 Góc
        drawLuxuryTableFrame(gc);

        // 2. Mặt Vải Nỉ Xanh Trầm Chiếu Sáng Tâm Bàn (Tournament Green Felt)
        drawFeltCloth(gc);

        // 3. 6 Miệng Lỗ Bi-a Sang Trọng (Pockets)
        drawPockets(gc);

        // 4. Vẽ Toàn Bộ Các Viên Bi 3D
        for (int i = balls.size() - 1; i >= 0; i--) {
            Ball ball = balls.get(i);
            if (!ball.active && !ball.isSinking) continue;
            drawBall3D(gc, ball);
        }

        // Vòng định vị đặt bi tự do (Ball-in-hand)
        drawBallInHandGuide(gc);

        // 5. Vẽ Các Hiệu Ứng Sóng Xung Kích & Hạt Phấn (VFX Particles)
        drawVFX(gc);

        // 6. Vẽ Gậy & Đường Nhắm của Mình khi đang kéo ngắm, hoặc của Đối thủ
        if (isAiming && !physicsRunning) {
            drawAimLine(gc);
        } else if (!isMyTurn && oppIsAiming && !physicsRunning) {
            drawOpponentAimLine(gc);
        }

        // 7. Vẽ Điểm Chọc Cơ & Trô Bi (Spin Selector Widget)
        drawSpinSelector(gc);

        // 8. Vẽ Banner Thông Báo Lượt Bay Động (Toast Announcement)
        drawTurnToast(gc);

        // 9. Vẽ Pháo Hoa Chiến Thắng
        drawConfetti(gc);
    }

    private void drawLuxuryTableFrame(GraphicsContext gc) {
        // Đổ bóng ngoài của bàn
        gc.setFill(Color.rgb(0, 0, 0, 0.5));
        gc.fillRect(TABLE_X - 26, TABLE_Y - 24, TABLE_W + 52, TABLE_H + 52);

        // Thành gỗ óc chó tối sang trọng hình chữ nhật phẳng không bo tròn (Cached Gradient)
        gc.setFill(WOOD_GRADIENT);
        gc.fillRect(TABLE_X - 24, TABLE_Y - 24, TABLE_W + 48, TABLE_H + 48);

        // Viền chỉ đồng kim loại
        gc.setStroke(Color.web("#C2A36B", 0.75));
        gc.setLineWidth(1.4);
        gc.strokeRect(TABLE_X - 10, TABLE_Y - 10, TABLE_W + 20, TABLE_H + 20);

        // Nẹp góc tráng bạc ở 4 góc thành bàn bi-a
        SilverCornerPane.drawSilverCorners(gc, TABLE_X - 24, TABLE_Y - 24, TABLE_W + 48, TABLE_H + 48, 24, 3.0);

        // 18 Điểm Ngắm Kim Cương Xà Cừ (Ivory Diamond Inlays)
        drawDiamondSights(gc);
    }

    private void drawDiamondSights(GraphicsContext gc) {
        gc.setFill(Color.web("#E8DEC8", 0.9));
        double sightYTop = TABLE_Y - 17;
        double sightYBot = TABLE_Y + TABLE_H + 17;
        double sightXLeft = TABLE_X - 17;
        double sightXRight = TABLE_X + TABLE_W + 17;

        // Thành trên & dưới (6 điểm mỗi cạnh: 3 nửa trái, 3 nửa phải)
        double[] xFractions = { 0.125, 0.25, 0.375, 0.625, 0.75, 0.875 };
        for (double f : xFractions) {
            double sx = TABLE_X + TABLE_W * f;
            drawDiamond(gc, sx, sightYTop, 3.5);
            drawDiamond(gc, sx, sightYBot, 3.5);
        }

        // Thành trái & phải (3 điểm mỗi cạnh)
        double[] yFractions = { 0.25, 0.5, 0.75 };
        for (double f : yFractions) {
            double sy = TABLE_Y + TABLE_H * f;
            drawDiamond(gc, sightXLeft, sy, 3.5);
            drawDiamond(gc, sightXRight, sy, 3.5);
        }
    }

    private void drawDiamond(GraphicsContext gc, double cx, double cy, double r) {
        gc.fillPolygon(
            new double[] { cx, cx + r, cx, cx - r },
            new double[] { cy - r, cy, cy + r, cy },
            4
        );
    }

    private void drawFeltCloth(GraphicsContext gc) {
        // Băng cao su đệm màu xanh sẫm tách biệt
        gc.setFill(Color.web("#123824"));
        gc.fillRect(TABLE_X - 6, TABLE_Y - 6, TABLE_W + 12, TABLE_H + 12);

        // Mặt vải nỉ xanh trầm sang trọng với ánh sáng rọi nhẹ tâm bàn (Cached Gradient)
        gc.setFill(FELT_GRADIENT);
        gc.fillRect(TABLE_X, TABLE_Y, TABLE_W, TABLE_H);

        // Vạch xuất phát (Head string line)
        double midX = TABLE_X + TABLE_W / 4;
        if (isBreakPlacement) {
            // Vùng đặt bi phá (Kitchen area) phát sáng êm dịu
            gc.save();
            gc.setFill(Color.rgb(56, 189, 248, 0.08));
            gc.fillRect(TABLE_X, TABLE_Y, midX - TABLE_X, TABLE_H);
            gc.setStroke(Color.rgb(56, 189, 248, 0.35 + 0.25 * Math.sin(cueIdleTimer * 4.0)));
            gc.setLineWidth(1.8);
            gc.strokeLine(midX, TABLE_Y + 10, midX, TABLE_Y + TABLE_H - 10);
            gc.restore();
        } else {
            gc.setStroke(Color.rgb(237, 241, 232, 0.1));
            gc.setLineWidth(1.0);
            gc.strokeLine(midX, TABLE_Y + 10, midX, TABLE_Y + TABLE_H - 10);
        }

        // Điểm đặt bi tâm bàn & chân bàn (Spot markers)
        gc.setFill(Color.rgb(237, 241, 232, 0.2));
        gc.fillOval(midX - 2.5, TABLE_Y + TABLE_H / 2 - 2.5, 5, 5);
        gc.fillOval(TABLE_X + TABLE_W * 0.75 - 2.5, TABLE_Y + TABLE_H / 2 - 2.5, 5, 5);
    }

    private void drawPockets(GraphicsContext gc) {
        for (double[] pocket : physics.getPockets()) {
            double px = pocket[0];
            double py = pocket[1];

            // Vành kim loại đồng ngoài của miệng lỗ (Antique Brass)
            gc.setFill(Color.web("#7D6537"));
            gc.fillOval(px - POCKET_R - 2, py - POCKET_R - 2, (POCKET_R + 2) * 2, (POCKET_R + 2) * 2);

            // Lòng lỗ sâu thẳm
            RadialGradient pocketDepth = new RadialGradient(
                0, 0, px, py, POCKET_R, false, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#080A08")),
                new Stop(0.85, Color.web("#121613")),
                new Stop(1.0, Color.web("#262010"))
            );
            gc.setFill(pocketDepth);
            gc.fillOval(px - POCKET_R, py - POCKET_R, POCKET_R * 2, POCKET_R * 2);
        }
    }

    // ============================================================
    // VẼ BI BI-A 3D PHOTOREALISTIC
    // ============================================================
    private void drawBall3D(GraphicsContext gc, Ball ball) {
        double r = Ball.RADIUS * ball.scale;
        double x = ball.x - r;
        double y = ball.y - r;
        double d = r * 2;

        gc.save();
        if (ball.isSinking) {
            gc.setGlobalAlpha(ball.opacity);
        }

        // 1. Bóng đổ dưới chân bi (Soft Drop Shadow)
        gc.setFill(Color.rgb(0, 0, 0, 0.38));
        gc.fillOval(x + 2.5 * ball.scale, y + 3.5 * ball.scale, d, d * 0.92);

        // 2. Thân bi dạng khối cầu 3D (3D Spherical Volume)
        Color baseColor = Color.web(ball.color);
        Color highlightColor = baseColor.interpolate(Color.WHITE, 0.45);
        Color darkShadowColor = baseColor.darker().darker();

        RadialGradient ball3DGrad = new RadialGradient(
            -35, 0.45, ball.x, ball.y, r, false, CycleMethod.NO_CYCLE,
            new Stop(0.0, highlightColor),
            new Stop(0.65, baseColor),
            new Stop(1.0, darkShadowColor)
        );
        gc.setFill(ball3DGrad);
        gc.fillOval(x, y, d, d);

        // 3. Bi Sọc (Stripe Balls 9-15): Dải màu uốn cong 3D
        if (ball.isStripe()) {
            RadialGradient whiteGrad = new RadialGradient(
                -35, 0.45, ball.x, ball.y, r, false, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.WHITE),
                new Stop(0.7, Color.web("#f1f5f9")),
                new Stop(1.0, Color.web("#94a3b8"))
            );
            gc.setFill(whiteGrad);
            // Sọc trên & dưới
            gc.fillArc(x, y, d, d, 40, 100, ArcType.ROUND);
            gc.fillArc(x, y, d, d, 220, 100, ArcType.ROUND);
        }

        // 4. Số trên bi (Center Number Badge)
        if (!ball.isCue()) {
            double badgeR = r * 0.52;
            gc.setFill(Color.WHITE);
            gc.fillOval(ball.x - badgeR, ball.y - badgeR, badgeR * 2, badgeR * 2);

            gc.setFill(Color.web("#0f172a"));
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, Math.max(7, r * 0.75)));
            gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
            gc.fillText(String.valueOf(ball.index), ball.x, ball.y + r * 0.28);
        }

        // 5. Specular Glint (Đốm lóa bóng đèn trên mặt bi)
        gc.setFill(Color.rgb(255, 255, 255, 0.7));
        gc.fillOval(ball.x - r * 0.45, ball.y - r * 0.5, r * 0.45, r * 0.32);

        // 6. Vòng chỉ thị lượt nhẹ quanh Cue Ball khi đến lượt mình
        if (ball.isCue() && isMyTurn && !physicsRunning) {
            gc.setStroke(Color.web("#C2A36B", 0.85));
            gc.setLineWidth(1.6);
            gc.strokeOval(x - 2, y - 2, d + 4, d + 4);
        }

        gc.restore();
    }

    // ============================================================
    // VẼ HIỆU ỨNG VFX (Shockwaves & Chalk Puffs)
    // ============================================================
    private void drawVFX(GraphicsContext gc) {
        // 1. Vẽ vệt đuôi bi cái theo skin gậy
        for (CueTrailParticle p : cueTrailParticles) {
            gc.setFill(p.color.deriveColor(0, 1, 1, Math.min(1.0, Math.max(0.0, p.alpha))));
            if (p.isStar) {
                gc.fillRect(p.x - p.size / 2, p.y - p.size / 2, p.size, p.size);
            } else {
                gc.fillOval(p.x - p.size / 2, p.y - p.size / 2, p.size, p.size);
            }
        }

        // 2. Vẽ sóng va chạm
        for (Shockwave s : shockwaves) {
            gc.setStroke(Color.rgb(237, 241, 232, s.alpha * 0.6));
            gc.setLineWidth(1.5 * s.alpha);
            gc.strokeOval(s.x - s.radius, s.y - s.radius, s.radius * 2, s.radius * 2);
        }

        // 3. Vẽ bụi phấn lơ
        for (ChalkParticle p : chalkParticles) {
            gc.setFill(p.color.deriveColor(0, 1, 1, p.alpha * 0.8));
            gc.fillOval(p.x - p.size / 2, p.y - p.size / 2, p.size, p.size);
        }

        // 4. Hiệu ứng cú đánh đặc trưng của skin gậy tại điểm đánh (0.50s)
        double nowSec = System.currentTimeMillis() / 1000.0;
        double strikeAge = nowSec - lastShotStrikeTime;
        if (isShotStrikeActive && strikeAge < 0.50 && CueSkinManager.isVfxEnabled()) {
            String skinId = lastShooterIsMine ? myCueColor : oppCueColor;
            CueSkin skin = CueSkinManager.getSkin(skinId);
            // Chỉ phát vụ nổ Strike VFX hoành tráng tại điểm va chạm, KHÔNG vẽ gậy cơ sau khi bắn
            CueSkinManager.drawStrikeVFX(gc, skin, lastStrikeX, lastStrikeY, Math.cos(lastStrikeAngle), Math.sin(lastStrikeAngle), strikeAge);
        } else if (strikeAge >= 0.50) {
            isShotStrikeActive = false;
        }
    }

    private void drawTurnToast(GraphicsContext gc) {
        if (toastAlpha <= 0.001) return;

        gc.save();
        gc.setGlobalAlpha(toastAlpha);
        double toastW = 240 * toastScale;
        double toastH = 34 * toastScale;
        double toastX = TABLE_X + (TABLE_W - toastW) / 2;
        double toastY = TABLE_Y + 12; // Đặt cao sát băng trên, không chắn tầm ngắm

        // Khung hình vuông Toast
        gc.setFill(Color.rgb(26, 36, 32, 0.94));
        gc.fillRect(toastX, toastY, toastW, toastH);

        // Viền và tráng bạc 4 góc
        gc.setStroke(Color.web("#C2A36B", 0.7));
        gc.setLineWidth(1.0);
        gc.strokeRect(toastX, toastY, toastW, toastH);

        SilverCornerPane.drawSilverCorners(gc, toastX, toastY, toastW, toastH, 8, 1.8);

        // Text thông báo
        gc.setFill(Color.web("#EDF1E8"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12 * toastScale));
        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
        gc.fillText(toastMessage, TABLE_X + TABLE_W / 2, toastY + toastH / 2 + 4 * toastScale);

        gc.restore();
    }

    private void drawConfetti(GraphicsContext gc) {
        for (ConfettiParticle cp : confettiList) {
            gc.save();
            gc.translate(cp.x, cp.y);
            gc.rotate(cp.rot);
            gc.setFill(cp.color);
            gc.fillRect(-cp.size / 2, -cp.size / 4, cp.size, cp.size / 2);
            gc.restore();
        }
    }

    // ============================================================
    // VẼ ĐƯỜNG NHẮM LASER & GẬY CƠ CHÂN THỰC
    // ============================================================
    // VẼ ĐƯỜNG NHẮM LASER & GẬY CƠ CHÂN THỰC
    // ============================================================
    private void drawAimLine(GraphicsContext gc) {
        Ball cueBall = balls.get(0);
        if (!cueBall.active) return;

        double dx = cueBall.x - aimStartX;
        double dy = cueBall.y - aimStartY;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist < 1) return;

        double nx = dx / dist;
        double ny = dy / dist;

        double forceRatio = Math.min(dist / MAX_AIM_DIST, 1.0);
        double cueBack = 12 + forceRatio * 55 + Math.sin(cueIdleTimer * 4.0) * 1.8;

        // Tâm căn hướng xa áp dụng cho mọi tài khoản không điều kiện level
        drawExtendedAim(gc, cueBall.x, cueBall.y, nx, ny, cueSpinX);

        // Vẽ cây gậy theo Skin và Animation đã trang bị
        CueSkin mySkin = CueSkinManager.getSkin(myCueColor);
        double nowSec = System.currentTimeMillis() / 1000.0;
        double animTime = nowSec - (gameStartTime / 1000.0);

        // Hào quang ma pháp trận / cổ ngữ hoàng gia quanh bi cái theo skin đang trang bị
        CueSkinManager.drawAimAura(gc, mySkin, cueBall.x, cueBall.y, nx, ny, animTime);

        CueSkinManager.drawCue(gc, mySkin, cueBall.x, cueBall.y, nx, ny, cueBack, animTime, false, 0.0);

        // Thanh lực neon tinh xảo (chỉ hiện khi đang ngắm kéo lực)
        drawNeonPowerMeter(gc, forceRatio, false);
    }

    private void drawOpponentAimLine(GraphicsContext gc) {
        Ball cueBall = balls.get(0);
        if (!cueBall.active) return;

        double nx = Math.cos(oppRenderAngle);
        double ny = Math.sin(oppRenderAngle);

        double cueBack = 12 + oppRenderForceRatio * 55 + Math.sin(cueIdleTimer * 4.0) * 1.8;

        // Tâm căn hướng xa áp dụng cho mọi tài khoản không điều kiện level
        drawExtendedAim(gc, cueBall.x, cueBall.y, nx, ny, oppSpinX);

        // Vẽ cây gậy theo Skin và Animation của đối thủ đã trang bị
        CueSkin oppSkin = CueSkinManager.getSkin(oppCueColor != null ? oppCueColor : CueSkinManager.ID_DEFAULT);
        double nowSec = System.currentTimeMillis() / 1000.0;
        double animTime = nowSec - (gameStartTime / 1000.0);

        // Hào quang ma pháp trận / cổ ngữ hoàng gia quanh bi cái theo skin đang trang bị của đối thủ
        CueSkinManager.drawAimAura(gc, oppSkin, cueBall.x, cueBall.y, nx, ny, animTime);

        CueSkinManager.drawCue(gc, oppSkin, cueBall.x, cueBall.y, nx, ny, cueBack, animTime, false, 0.0);

        // Thanh lực neon tinh xảo thể hiện lực đối thủ đang kéo
        if (oppRenderForceRatio > 0.01) {
            drawNeonPowerMeter(gc, oppRenderForceRatio, true);
        }
    }

    private void drawExtendedAim(GraphicsContext gc, double startX, double startY, double nx, double ny, double spinX) {
        double currentX = startX;
        double currentY = startY;
        double dirX = nx;
        double dirY = ny;
        double currentSpinX = spinX;

        gc.setStroke(Color.web("#38bdf8", 0.85));
        gc.setLineWidth(1.8);
        gc.setLineDashes(6, 4);

        double r = Ball.RADIUS;
        double left = TABLE_X + r;
        double right = TABLE_X + TABLE_W - r;
        double top = TABLE_Y + r;
        double bottom = TABLE_Y + TABLE_H - r;

        for (int i = 0; i < 3; i++) {
            double tX = Double.MAX_VALUE;
            double tY = Double.MAX_VALUE;

            if (dirX > 0.0001) tX = (right - currentX) / dirX;
            else if (dirX < -0.0001) tX = (left - currentX) / dirX;

            if (dirY > 0.0001) tY = (bottom - currentY) / dirY;
            else if (dirY < -0.0001) tY = (top - currentY) / dirY;

            if (tX < 0.0001) tX = Double.MAX_VALUE;
            if (tY < 0.0001) tY = Double.MAX_VALUE;

            double t = Math.min(tX, tY);
            if (t <= 0 || t == Double.MAX_VALUE) break;

            double tBall = Double.MAX_VALUE;
            Ball firstHitBall = null;

            for (int j = 1; j < balls.size(); j++) {
                Ball b = balls.get(j);
                if (!b.active || b.isSinking) continue;

                double cx = b.x - currentX;
                double cy = b.y - currentY;
                if (Math.abs(cx) < 1 && Math.abs(cy) < 1) continue;

                double proj = cx * dirX + cy * dirY;
                if (proj > 0) {
                    double dist2 = (cx * cx + cy * cy) - (proj * proj);
                    double rSum2 = (r * 2) * (r * 2);
                    if (dist2 <= rSum2) {
                        double dt = Math.sqrt(Math.max(0.0, rSum2 - dist2));
                        double tIntersect = proj - dt;
                        if (tIntersect > 0.1 && tIntersect < tBall) {
                            tBall = tIntersect;
                            firstHitBall = b;
                        }
                    }
                }
            }

            if (tBall < t && firstHitBall != null) {
                double hitX = currentX + tBall * dirX;
                double hitY = currentY + tBall * dirY;
                gc.strokeLine(currentX, currentY, hitX, hitY);

                // Bóng ảo (Ghost ball)
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(1.5);
                gc.strokeOval(hitX - r, hitY - r, r * 2, r * 2);

                // Hướng bi mục tiêu kéo dài hướng xa xuyên suốt bàn
                double targetDirX = firstHitBall.x - hitX;
                double targetDirY = firstHitBall.y - hitY;
                double len = Math.sqrt(targetDirX * targetDirX + targetDirY * targetDirY);
                if (len > 0) {
                    targetDirX /= len;
                    targetDirY /= len;

                    // Tính khoảng cách tới băng để kéo dài toàn bộ mặt bàn
                    double twX = Double.MAX_VALUE;
                    double twY = Double.MAX_VALUE;
                    if (targetDirX > 0.0001) twX = (right - firstHitBall.x) / targetDirX;
                    else if (targetDirX < -0.0001) twX = (left - firstHitBall.x) / targetDirX;

                    if (targetDirY > 0.0001) twY = (bottom - firstHitBall.y) / targetDirY;
                    else if (targetDirY < -0.0001) twY = (top - firstHitBall.y) / targetDirY;

                    double tTarget = Math.min(twX, twY);
                    if (tTarget > 0 && tTarget != Double.MAX_VALUE) {
                        gc.setStroke(Color.web("#22c55e", 0.95));
                        gc.setLineWidth(2.2);
                        gc.strokeLine(firstHitBall.x, firstHitBall.y, firstHitBall.x + targetDirX * tTarget, firstHitBall.y + targetDirY * tTarget);
                    }
                }

                // Tiếp tuyến nảy của bi cái sau va chạm
                double dot = dirX * targetDirX + dirY * targetDirY;
                double tanX = dirX - dot * targetDirX;
                double tanY = dirY - dot * targetDirY;
                double tanLen = Math.sqrt(tanX * tanX + tanY * tanY);
                if (tanLen > 0.05) {
                    tanX /= tanLen;
                    tanY /= tanLen;
                    gc.setStroke(Color.rgb(255, 255, 255, 0.65));
                    gc.setLineWidth(1.4);
                    gc.strokeLine(hitX, hitY, hitX + tanX * 80, hitY + tanY * 80);
                }
                break;
            }

            double hitX = currentX + t * dirX;
            double hitY = currentY + t * dirY;
            gc.strokeLine(currentX, currentY, hitX, hitY);

            currentX = hitX;
            currentY = hitY;
            if (t == tX) {
                dirX = -dirX;
                if (currentSpinX != 0) {
                    if (dirX > 0) dirY += currentSpinX * 0.18;
                    else dirY -= currentSpinX * 0.18;
                    currentSpinX *= 0.5;
                }
            }
            if (t == tY) {
                dirY = -dirY;
                if (currentSpinX != 0) {
                    if (dirY > 0) dirX += currentSpinX * 0.18;
                    else dirX -= currentSpinX * 0.18;
                    currentSpinX *= 0.5;
                }
            }
            double dLen = Math.hypot(dirX, dirY);
            if (dLen > 0.001) {
                dirX /= dLen;
                dirY /= dLen;
            }
        }
        gc.setLineDashes();
    }

    private void drawNeonPowerMeter(GraphicsContext gc, double forceRatio, boolean isOpponent) {
        double barW = 260;
        double barH = 12;
        double barX = TABLE_X + (TABLE_W - barW) / 2;
        double barY = TABLE_Y + TABLE_H + 10;

        // Background Box
        gc.setFill(Color.web("#141D1A"));
        gc.fillRoundRect(barX - 4, barY - 4, barW + 8, barH + 8, 8, 8);
        gc.setStroke(Color.web("#34443B"));
        gc.setLineWidth(1.0);
        gc.strokeRoundRect(barX - 4, barY - 4, barW + 8, barH + 8, 8, 8);

        // Power Fill: Xanh nỉ -> Đồng -> Đỏ trầm (Cached Gradient)
        gc.setFill(POWER_GRADIENT);
        gc.fillRoundRect(barX, barY, barW * forceRatio, barH, 4, 4);

        // Percentage Text
        gc.setFill(Color.web("#EDF1E8"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
        String label = isOpponent ? String.format("ĐỐI THỦ KÉO LỰC: %d%%", (int)(forceRatio * 100))
                                  : String.format("LỰC ĐÁNH: %d%%", (int)(forceRatio * 100));
        gc.fillText(label, TABLE_X + TABLE_W / 2, barY + 10);
    }

    private void drawSpinSelector(GraphicsContext gc) {
        // Khung hình vuông không bo tròn bên phải bàn bi-a
        gc.setFill(Color.web("#1A2420"));
        gc.fillRect(SPIN_PANEL_X, SPIN_PANEL_Y, SPIN_PANEL_W, SPIN_PANEL_H);
        gc.setStroke(Color.web("#34443B"));
        gc.setLineWidth(1.0);
        gc.strokeRect(SPIN_PANEL_X, SPIN_PANEL_Y, SPIN_PANEL_W, SPIN_PANEL_H);

        // Nẹp góc tráng bạc ở 4 góc của bảng trô bi
        SilverCornerPane.drawSilverCorners(gc, SPIN_PANEL_X, SPIN_PANEL_Y, SPIN_PANEL_W, SPIN_PANEL_H, 12, 2.0);

        // Tiêu đề nhỏ tinh gọn
        gc.setFill(Color.web("#C2A36B"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
        gc.fillText("TRÔ BI", SPIN_WIDGET_X, SPIN_PANEL_Y + 22);

        // Vùng viền đệm tròn quanh bi
        gc.setFill(Color.web("#141D1A"));
        gc.fillOval(SPIN_WIDGET_X - SPIN_WIDGET_R - 5, SPIN_WIDGET_Y - SPIN_WIDGET_R - 5, (SPIN_WIDGET_R + 5) * 2, (SPIN_WIDGET_R + 5) * 2);
        gc.setStroke(Color.web("#24322B"));
        gc.setLineWidth(1.0);
        gc.strokeOval(SPIN_WIDGET_X - SPIN_WIDGET_R - 5, SPIN_WIDGET_Y - SPIN_WIDGET_R - 5, (SPIN_WIDGET_R + 5) * 2, (SPIN_WIDGET_R + 5) * 2);

        // Bi cái 3D Realistic (Cached Gradient)
        gc.setFill(SPIN_CUE_GRADIENT);
        gc.fillOval(SPIN_WIDGET_X - SPIN_WIDGET_R, SPIN_WIDGET_Y - SPIN_WIDGET_R, SPIN_WIDGET_R * 2, SPIN_WIDGET_R * 2);
        gc.setStroke(Color.web("#34443B"));
        gc.setLineWidth(1.0);
        gc.strokeOval(SPIN_WIDGET_X - SPIN_WIDGET_R, SPIN_WIDGET_Y - SPIN_WIDGET_R, SPIN_WIDGET_R * 2, SPIN_WIDGET_R * 2);

        // Trục tâm (Crosshairs mờ)
        gc.setStroke(Color.rgb(52, 68, 59, 0.6));
        gc.setLineWidth(1.0);
        gc.strokeLine(SPIN_WIDGET_X - SPIN_WIDGET_R + 4, SPIN_WIDGET_Y, SPIN_WIDGET_X + SPIN_WIDGET_R - 4, SPIN_WIDGET_Y);
        gc.strokeLine(SPIN_WIDGET_X, SPIN_WIDGET_Y - SPIN_WIDGET_R + 4, SPIN_WIDGET_X, SPIN_WIDGET_Y + SPIN_WIDGET_R - 4);

        // Điểm đầu cơ màu đỏ (Chalk Tip)
        double activeSpinX = (isMyTurn || isPractice) ? cueSpinX : (oppIsAiming ? oppSpinX : cueSpinX);
        double activeSpinY = (isMyTurn || isPractice) ? cueSpinY : (oppIsAiming ? oppSpinY : cueSpinY);
        double tipX = SPIN_WIDGET_X + activeSpinX * (SPIN_WIDGET_R - 6);
        double tipY = SPIN_WIDGET_Y + activeSpinY * (SPIN_WIDGET_R - 6);

        // Chấm đỏ đầu cơ tinh tế
        gc.setFill(Color.web("#C94A4A"));
        gc.fillOval(tipX - 5, tipY - 5, 10, 10);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1.2);
        gc.strokeOval(tipX - 5, tipY - 5, 10, 10);
    }

    // ============================================================
    // SỰ KIỆN CHUỘT
    // ============================================================
    private void onMousePressed(MouseEvent e) {
        // Nhấp chuột phải hoặc nhấp đúp trên Spin Panel để reset về tâm
        if ((e.getButton() == MouseButton.SECONDARY || e.getClickCount() >= 2) &&
            e.getX() >= SPIN_PANEL_X && e.getX() <= SPIN_PANEL_X + SPIN_PANEL_W &&
            e.getY() >= SPIN_PANEL_Y && e.getY() <= SPIN_PANEL_Y + SPIN_PANEL_H) {
            cueSpinX = 0.0;
            cueSpinY = 0.0;
            SoundManager.playButtonClick();
            showTurnToast("🎯 ĐÃ ĐẶT LẠI TÂM BI", Color.web("#38bdf8"));
            return;
        }

        // Kiểm tra tương tác với Spin Selector Widget (Trô bi / Cule / Ép-phê)
        double swDx = e.getX() - SPIN_WIDGET_X;
        double swDy = e.getY() - SPIN_WIDGET_Y;
        if (Math.hypot(swDx, swDy) <= SPIN_WIDGET_R + 8) {
            isDraggingSpin = true;
            double sx = swDx / SPIN_WIDGET_R;
            double sy = swDy / SPIN_WIDGET_R;
            double d = Math.hypot(sx, sy);
            if (d > 1.0) { sx /= d; sy /= d; }
            if (d < 0.15) { sx = 0.0; sy = 0.0; } // Chạm gần tâm thì snap về tâm
            cueSpinX = sx;
            cueSpinY = sy;
            return;
        }

        if (!isMyTurn || physicsRunning || gameOver) return;
        Ball cueBall = balls.get(0);
        if (!cueBall.active) return;

        double dx = e.getX() - cueBall.x;
        double dy = e.getY() - cueBall.y;
        double dist = Math.sqrt(dx * dx + dy * dy);

        if (ballInHand) {
            // 1. Nhấp trực tiếp vào bi cái để kéo đi
            if (dist <= Ball.RADIUS * 2.2) {
                isDraggingCue = true;
                return;
            }
            // 2. Nhấp vào bất kỳ điểm nào trên bàn (ngoài vùng nhắm) để đặt bi cái tới đó
            if (dist > Ball.RADIUS + 120 && isInsideValidPlacement(e.getX(), e.getY())) {
                repositionCueBall(e.getX(), e.getY());
                isDraggingCue = true;
                return;
            }
        }

        if (dist <= Ball.RADIUS + 120) {
            isAiming  = true;
            aimStartX = e.getX();
            aimStartY = e.getY();
            sendAimUpdateThrottled(true, aimAngle, 0.0, true);
        }
    }

    private void onMouseDragged(MouseEvent e) {
        if (isDraggingSpin) {
            double swDx = e.getX() - SPIN_WIDGET_X;
            double swDy = e.getY() - SPIN_WIDGET_Y;
            double sx = swDx / SPIN_WIDGET_R;
            double sy = swDy / SPIN_WIDGET_R;
            double d = Math.hypot(sx, sy);
            if (d > 1.0) { sx /= d; sy /= d; }
            if (d < 0.15) { sx = 0.0; sy = 0.0; }
            cueSpinX = sx;
            cueSpinY = sy;
            return;
        }

        if (isDraggingCue) {
            repositionCueBall(e.getX(), e.getY());
            return;
        }

        if (!isAiming) return;
        aimStartX = e.getX();
        aimStartY = e.getY();

        Ball cueBall = balls.get(0);
        double dx = cueBall.x - aimStartX;
        double dy = cueBall.y - aimStartY;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist > 5) {
            aimAngle = Math.atan2(dy, dx);
        }
        double ratio = Math.min(dist / MAX_AIM_DIST, 1.0);
        aimForce = ratio * MAX_FORCE;
        sendAimUpdateThrottled(true, aimAngle, ratio, false);
    }

    private void onMouseMoved(MouseEvent e) {
        // Con trỏ chuột khi hover qua các nút bảng điều khiển
        if (e.getX() >= SPIN_PANEL_X && e.getX() <= SPIN_PANEL_X + SPIN_PANEL_W &&
            e.getY() >= SPIN_PANEL_Y && e.getY() <= SPIN_PANEL_Y + SPIN_PANEL_H) {
            canvas.setCursor(javafx.scene.Cursor.HAND);
        } else {
            canvas.setCursor(javafx.scene.Cursor.DEFAULT);
        }

        if (!isMyTurn || physicsRunning || gameOver || isAiming || isDraggingSpin) return;
        Ball cueBall = balls.get(0);
        if (!cueBall.active) return;

        // Tránh giật hướng cơ khi chuột đang di chuyển trên panel bên phải
        if (e.getX() > TABLE_X + TABLE_W + 15) return;

        if (ballInHand && !isDraggingCue) {
            double dist = Math.hypot(e.getX() - cueBall.x, e.getY() - cueBall.y);
            if (dist <= Ball.RADIUS * 2.2) {
                canvas.setCursor(javafx.scene.Cursor.OPEN_HAND);
                return;
            }
        }

        if (isDraggingCue) {
            canvas.setCursor(javafx.scene.Cursor.CLOSED_HAND);
            return;
        }

        double dx = cueBall.x - e.getX();
        double dy = cueBall.y - e.getY();
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist > 8) {
            aimAngle = Math.atan2(dy, dx);
        }
    }

    private void onMouseReleased(MouseEvent e) {
        if (isDraggingSpin) {
            isDraggingSpin = false;
            return;
        }

        if (isDraggingCue) {
            isDraggingCue = false;
            Ball cueBall = balls.get(0);
            shockwaves.add(new Shockwave(cueBall.x, cueBall.y, 0.25));
            // Gửi tọa độ mới của bi cái cho đối thủ xem ngay trên bàn sau khi thả bi
            if (!isPractice && network != null) {
                network.sendCueBallPlaced(cueBall.x, cueBall.y);
            }
            return;
        }

        if (!isAiming || !isMyTurn || physicsRunning) return;
        isAiming = false;
        sendAimUpdateThrottled(false, 0.0, 0.0, true);

        Ball cueBall = balls.get(0);
        if (!cueBall.active) return;

        double dx   = cueBall.x - aimStartX;
        double dy   = cueBall.y - aimStartY;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist < 5) return;

        aimAngle = Math.atan2(dy, dx);
        double ratio = Math.min(dist / MAX_AIM_DIST, 1.0);
        aimForce = ratio * MAX_FORCE;

        if (!Double.isFinite(aimAngle) || !Double.isFinite(aimForce)
                || !Double.isFinite(cueBall.x) || !Double.isFinite(cueBall.y)
                || !Double.isFinite(cueSpinX) || !Double.isFinite(cueSpinY)) {
            return;
        }
        
        ballInHand = false;
        isBreakPlacement = false;

        if (isPractice) {
            applyCueShot(aimAngle, aimForce, cueSpinX, cueSpinY, true);
        } else {
            if (network != null) network.sendCueShot(aimAngle, aimForce, cueBall.x, cueBall.y, cueSpinX, cueSpinY);
            applyCueShot(aimAngle, aimForce, cueSpinX, cueSpinY, true);
            iWasShooterThisTurn = true;
            isMyTurn = false;
            updateStatusLabel();
            updateTurnLabel();
        }
    }

    private void sendAimUpdateThrottled(boolean isAiming, double angle, double forceRatio, boolean forceSend) {
        if (isPractice || network == null) return;
        long now = System.currentTimeMillis();
        double dAngle = Math.abs(angle - lastSentAngle);
        double dForce = Math.abs(forceRatio - lastSentForce);

        if (forceSend || (isAiming != lastSentAiming) || (now - lastAimSendTime >= 33 && (dAngle > 0.008 || dForce > 0.015))) {
            lastAimSendTime = now;
            lastSentAngle = angle;
            lastSentForce = forceRatio;
            lastSentAiming = isAiming;
            network.sendCueAim(isAiming, angle, forceRatio, cueSpinX, cueSpinY);
        }
    }

    private void repositionCueBall(double targetX, double targetY) {
        double minX = TABLE_X + Ball.RADIUS;
        double maxX = isBreakPlacement ? (TABLE_X + TABLE_W / 4) : (TABLE_X + TABLE_W - Ball.RADIUS);
        double minY = TABLE_Y + Ball.RADIUS;
        double maxY = TABLE_Y + TABLE_H - Ball.RADIUS;

        double nx = Math.max(minX, Math.min(maxX, targetX));
        double ny = Math.max(minY, Math.min(maxY, targetY));

        for (int k = 0; k < 3; k++) {
            for (int i = 1; i < balls.size(); i++) {
                Ball b = balls.get(i);
                if (!b.active || b.isSinking) continue;

                double dx = nx - b.x;
                double dy = ny - b.y;
                double dist = Math.sqrt(dx * dx + dy * dy);
                double minDist = Ball.RADIUS * 2 + 1;

                if (dist < minDist) {
                    if (dist == 0) { dx = 1; dy = 0; dist = 1; }
                    nx = b.x + (dx / dist) * minDist;
                    ny = b.y + (dy / dist) * minDist;
                }
            }
        }

        nx = Math.max(minX, Math.min(maxX, nx));
        ny = Math.max(minY, Math.min(maxY, ny));

        Ball cueBall = balls.get(0);
        cueBall.setPosition(nx, ny);
        cueBall.setVelocity(0, 0);
    }

    private boolean isInsideValidPlacement(double x, double y) {
        double minX = TABLE_X;
        double maxX = isBreakPlacement ? (TABLE_X + TABLE_W / 4 + Ball.RADIUS) : (TABLE_X + TABLE_W);
        double minY = TABLE_Y;
        double maxY = TABLE_Y + TABLE_H;
        return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }

    private void drawBallInHandGuide(GraphicsContext gc) {
        if (!ballInHand || !isMyTurn || physicsRunning || gameOver) return;
        Ball cueBall = balls.get(0);
        if (!cueBall.active) return;

        double now = cueIdleTimer;
        double pulse = Math.sin(now * 5.0) * 2.5;
        double r = Ball.RADIUS + 7 + pulse;

        gc.save();
        Color ringColor = isDraggingCue ? Color.web("#22c55e", 0.95) : Color.web("#38bdf8", 0.85);
        gc.setStroke(ringColor);
        gc.setLineWidth(isDraggingCue ? 2.5 : 2.0);
        gc.strokeOval(cueBall.x - r, cueBall.y - r, r * 2, r * 2);

        // 4 mũi tên điều hướng
        double arrowOffset = r + 4;
        gc.setFill(ringColor);
        drawSmallArrow(gc, cueBall.x, cueBall.y - arrowOffset, 0);   // Lên
        drawSmallArrow(gc, cueBall.x, cueBall.y + arrowOffset, 180); // Xuống
        drawSmallArrow(gc, cueBall.x - arrowOffset, cueBall.y, 270); // Trái
        drawSmallArrow(gc, cueBall.x + arrowOffset, cueBall.y, 90);  // Phải

        // Hướng dẫn nhỏ
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        gc.setFill(Color.rgb(255, 255, 255, 0.9));
        gc.setTextAlign(TextAlignment.CENTER);
        String guideText = isBreakPlacement ? "Đặt bi sau vạch để phá" : "Kéo thả bi tự do";
        gc.fillText(guideText, cueBall.x, cueBall.y - r - 10);

        gc.restore();
    }

    private void drawSmallArrow(GraphicsContext gc, double x, double y, double angleDeg) {
        gc.save();
        gc.translate(x, y);
        gc.rotate(angleDeg);
        gc.fillPolygon(
            new double[] { 0, -3.5, 3.5 },
            new double[] { -4.5, 2.5, 2.5 },
            3
        );
        gc.restore();
    }

    private void applyCueShot(double angle, double force, double spinX, double spinY, boolean isMine) {
        isBreakPlacement = false;
        ballInHand = false;
        SoundManager.playCueHit(force / MAX_FORCE);
        Ball cueBall = balls.get(0);

        // Kích hoạt animation chớp sáng đặc trưng của skin gậy
        lastShotStrikeTime = System.currentTimeMillis() / 1000.0;
        lastShooterIsMine = isMine;
        isShotStrikeActive = true;
        lastStrikeAngle = angle;
        lastStrikeX = cueBall.x;
        lastStrikeY = cueBall.y;

        // Bắn ra các hạt bụi phấn lơ
        double dirX = Math.cos(angle);
        double dirY = Math.sin(angle);
        for (int i = 0; i < 12; i++) {
            chalkParticles.add(new ChalkParticle(cueBall.x, cueBall.y, dirX, dirY));
        }

        cueBall.setVelocity(Math.cos(angle) * force, Math.sin(angle) * force);
        cueBall.setSpin(spinX, spinY);

        physicsRunning      = true;
        shotFiredThisTurn    = true;
        pocketedThisTurn    = false;
        anyObjectBallPocketedThisTurn = false;
        cueBallPocketed     = false;
        eightBallPocketed   = false;
        cueBallHitSomething = false;
    }

    private void handleBallPocketed(Ball ball) {
        System.out.println("[Game] Bi " + ball.index + " vao lo!");

        if (ball.isCue()) {
            cueBallPocketed = true;
        } else if (ball.isEightBall()) {
            eightBallPocketed = true;
            anyObjectBallPocketedThisTurn = true;
            if (isPractice) {
                pocketedThisTurn = true;
            }
        } else {
            anyObjectBallPocketedThisTurn = true;
            if (myTargetBalls != null && myTargetBalls.contains(ball.index)) {
                pocketedThisTurn = true;
            }
        }
    }

    private boolean hasClearedMyGroup() {
        if (myTargetBalls == null) return false;
        for (int num : myTargetBalls) {
            boolean pocketed = false;
            for (Ball b : balls) {
                if (b.index == num && (!b.active || b.inPocket)) {
                    pocketed = true;
                    break;
                }
            }
            if (!pocketed) return false;
        }
        return true;
    }

    private boolean hasClearedOppGroup() {
        if (oppTargetBalls == null) return false;
        for (int num : oppTargetBalls) {
            boolean pocketed = false;
            for (Ball b : balls) {
                if (b.index == num && (!b.active || b.inPocket)) {
                    pocketed = true;
                    break;
                }
            }
            if (!pocketed) return false;
        }
        return true;
    }

    private void onPhysicsComplete() {
        if (isPractice) {
            updateGroupLabels();

            int remaining = 0;
            for (int i = 1; i < balls.size(); i++) {
                Ball b = balls.get(i);
                if (b.active && !b.inPocket) {
                    remaining++;
                }
            }

            if (remaining == 0) {
                gameOver = true;
                SoundManager.playVictory();
                for (int i = 0; i < 90; i++) {
                    confettiList.add(new ConfettiParticle(TABLE_X + TABLE_W / 2, TABLE_Y + 50));
                }
                showTurnToast("🎉 XUẤT SẮC! BẠN ĐÃ DỌN SẠCH TOÀN BỘ 15 BI!", Color.web("#22c55e"));
                new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            Platform.runLater(() -> {
                                if (network != null && network.getSession() != null) {
                                    var s = network.getSession();
                                    sceneManager.showLobby(s.username, s.exp, s.level, s.wins, s.losses);
                                } else {
                                    sceneManager.showLobby(myName, 0, 1, 0, 0);
                                }
                            });
                        }
                    }, 3500
                );
                return;
            }

            // Xử lý bi cái nếu rơi vào lỗ
            if (cueBallPocketed) {
                Ball cue = balls.get(0);
                cue.active = true;
                cue.inPocket = false;
                cue.setVelocity(0, 0);
                double rx = TABLE_X + TABLE_W / 4;
                double ry = TABLE_Y + TABLE_H / 2;
                for (int i = 1; i < balls.size(); i++) {
                    Ball b = balls.get(i);
                    if (b.active && !b.isSinking && Math.hypot(rx - b.x, ry - b.y) < Ball.RADIUS * 2) {
                        rx -= Ball.RADIUS * 2.5;
                        if (rx < TABLE_X + Ball.RADIUS * 2) rx = TABLE_X + Ball.RADIUS * 2;
                    }
                }
                cue.setPosition(rx, ry);
                ballInHand = true;
                isBreakPlacement = false;
                SoundManager.playFoul();
                showTurnToast("💀 Bi cái vào lỗ! Đặt bi tự do để bắn tiếp", Color.web("#ef4444"));
            } else if (!cueBallHitSomething) {
                ballInHand = true;
                isBreakPlacement = false;
                SoundManager.playFoul();
                showTurnToast("⚠️ Chưa chạm bi nào! Đặt bi tự do để bắn tiếp", Color.web("#f59e0b"));
            } else if (pocketedThisTurn || anyObjectBallPocketedThisTurn) {
                showTurnToast("🎯 ĂN BI THÀNH CÔNG! Còn " + remaining + " bi", Color.web("#22c55e"));
            } else {
                showTurnToast("Cố lên! Còn " + remaining + " bi trên bàn", Color.web("#38bdf8"));
            }

            isMyTurn = true;
            turnStartTime = System.currentTimeMillis();
            updateGroupLabels();
            updateStatusLabel();
            return;
        }

        updateGroupLabels();

        // 1. XỬ LÝ KHI BI SỐ 8 VÀO LỖ
        if (eightBallPocketed && !gameOver) {
            gameOver = true;
            boolean cueScratchWithEight = cueBallPocketed;

            if (cueBallPocketed) {
                Ball cue = balls.get(0);
                cue.active    = true;
                cue.inPocket  = false;
                cue.setPosition(TABLE_X + TABLE_W / 4, TABLE_Y + TABLE_H / 2);
                cue.setVelocity(0, 0);
            }

            // Trận đấu Online 2 người
            if (iWasShooterThisTurn) {
                // Người bắn thực hiện cú đánh:
                if (cueScratchWithEight) {
                    // Luật: Chết cái và chết 8 cùng turn đó -> THUA LUÔN!
                    SoundManager.playFoul();
                    showTurnToast("💀 BẠN ĐÃ THUA (Chết cả bi cái và bi số 8)!", Color.web("#ef4444"));
                    if (network != null) network.sendClaimWin(false);
                } else if (hasClearedMyGroup()) {
                    // Ăn bi 8 hợp lệ sau khi dọn sạch nhóm bi của mình -> THẮNG!
                    SoundManager.playVictory();
                    for (int i = 0; i < 90; i++) {
                        confettiList.add(new ConfettiParticle(TABLE_X + TABLE_W / 2, TABLE_Y + 50));
                    }
                    showTurnToast("🎉 BẠN ĐÃ CHIẾN THẮNG (Ăn bi 8 hợp lệ)!", Color.web("#22c55e"));
                    if (network != null) network.sendClaimWin(true);
                } else {
                    // Ăn bi 8 khi chưa dọn sạch nhóm bi -> THUA!
                    SoundManager.playFoul();
                    showTurnToast("💀 BẠN ĐÃ THUA (Ăn bi số 8 khi chưa hết nhóm)!", Color.web("#ef4444"));
                    if (network != null) network.sendClaimWin(false);
                }
            } else {
                // Người đang theo dõi đối thủ bắn:
                if (cueScratchWithEight) {
                    // Đối thủ chết cả cái và 8 -> Đối thủ thua, TÔI THẮNG!
                    SoundManager.playVictory();
                    for (int i = 0; i < 90; i++) {
                        confettiList.add(new ConfettiParticle(TABLE_X + TABLE_W / 2, TABLE_Y + 50));
                    }
                    showTurnToast("🎉 ĐỐI THỦ CHẾT CẢ BI CÁI VÀ BI 8 - BẠN THẮNG!", Color.web("#22c55e"));
                    if (network != null) network.sendClaimWin(true);
                } else if (hasClearedOppGroup()) {
                    // Đối thủ ăn bi 8 hợp lệ -> TÔI THUA!
                    SoundManager.playFoul();
                    showTurnToast("💀 ĐỐI THỦ ĐÃ ĂN BI 8 HỢP LỆ - BẠN THUA!", Color.web("#ef4444"));
                } else {
                    // Đối thủ ăn bi 8 khi chưa hết nhóm -> Đối thủ thua, TÔI THẮNG!
                    SoundManager.playVictory();
                    for (int i = 0; i < 90; i++) {
                        confettiList.add(new ConfettiParticle(TABLE_X + TABLE_W / 2, TABLE_Y + 50));
                    }
                    showTurnToast("🎉 ĐỐI THỦ ĂN BI 8 PHẠM QUY - BẠN THẮNG!", Color.web("#22c55e"));
                    if (network != null) network.sendClaimWin(true);
                }
            }
            return;
        }

        // 2. XỬ LÝ LƯỢT ĐÁNH THÔNG THƯỜNG (BI 8 CHƯA VÀO LỖ)
        boolean isFoul = cueBallPocketed || !cueBallHitSomething;

        if (cueBallPocketed) {
            Ball cue = balls.get(0);
            cue.active    = true;
            cue.inPocket  = false;
            cue.setVelocity(0, 0);
            double rx = TABLE_X + TABLE_W / 4;
            double ry = TABLE_Y + TABLE_H / 2;
            for (int i = 1; i < balls.size(); i++) {
                Ball b = balls.get(i);
                if (b.active && !b.isSinking && Math.hypot(rx - b.x, ry - b.y) < Ball.RADIUS * 2) {
                    rx -= Ball.RADIUS * 2.5;
                    if (rx < TABLE_X + Ball.RADIUS * 2) rx = TABLE_X + Ball.RADIUS * 2;
                }
            }
            cue.setPosition(rx, ry);
        }

        // Online PVP Match
        if (iWasShooterThisTurn) {
            iWasShooterThisTurn = false;
            boolean keepTurn = !isFoul && pocketedThisTurn;

            if (cueBallPocketed) {
                SoundManager.playFoul();
                if (pocketedThisTurn || anyObjectBallPocketedThisTurn) {
                    showTurnToast("💀 PHẠM LỖI: Bi cái vào lỗ! Đổi lượt (vẫn tính bi đã ăn)", Color.web("#ef4444"));
                } else {
                    showTurnToast("💀 PHẠM LỖI: Bi cái vào lỗ! Đổi lượt", Color.web("#ef4444"));
                }
            } else if (!cueBallHitSomething) {
                SoundManager.playFoul();
                showTurnToast("💀 PHẠM LỖI: Không chạm bi! Đổi lượt", Color.web("#ef4444"));
            } else if (keepTurn) {
                showTurnToast("🎯 ĂN BI THÀNH CÔNG! Bạn được đánh tiếp", Color.web("#22c55e"));
            } else {
                showTurnToast("🎯 Không ăn bi! Đổi lượt sang đối thủ", Color.web("#f59e0b"));
            }

            ballInHand = false;
            if (network != null) network.sendPhysicsDone(keepTurn, isFoul);
        } else {
            // Người xem đối thủ đánh
            ballInHand = isFoul;
            if (isFoul) isBreakPlacement = false;
            if (cueBallPocketed) {
                if (anyObjectBallPocketedThisTurn) {
                    showTurnToast("⚠️ ĐỐI THỦ PHẠM LỖI BI CÁI! Bạn được đặt bi tự do (vẫn tính bi đã ăn)", Color.web("#22c55e"));
                } else {
                    showTurnToast("⚠️ ĐỐI THỦ PHẠM LỖI BI CÁI! Bạn được đặt bi tự do (Ball-in-hand)", Color.web("#22c55e"));
                }
            } else if (!cueBallHitSomething) {
                showTurnToast("⚠️ ĐỐI THỦ KHÔNG CHẠM BI! Bạn được đặt bi tự do", Color.web("#22c55e"));
            }
        }
    }

    // ============================================================
    // CALLBACK MẠNG
    // ============================================================
    private void setupNetworkCallback() {
        if (network == null) return;
        network.setOnPacketReceived(pkt -> {
            try {
                switch (pkt.type) {
                    case GAME_STATE -> {
                        double angle = pkt.readDouble();
                        double force = pkt.readDouble();
                        double cueX  = pkt.readDouble();
                        double cueY  = pkt.readDouble();
                        double sX    = pkt.readDouble();
                        double sY    = pkt.readDouble();
                        if (!Double.isFinite(angle) || !Double.isFinite(force)
                                || !Double.isFinite(cueX) || !Double.isFinite(cueY)
                                || !Double.isFinite(sX) || !Double.isFinite(sY)) {
                            System.err.println("[GameScene] Bỏ qua GAME_STATE vì chứa tham số không hợp lệ (NaN/Infinity).");
                            break;
                        }
                        Platform.runLater(() -> {
                            // Người vừa bắn không áp dụng lại GAME_STATE để tránh bắn 2 lần.
                            // Đồng thời bỏ qua nếu physics đang chạy hoặc đã có cú đánh đang chờ để chống cú đánh lặp.
                            if (!iWasShooterThisTurn && !physicsRunning && !pendingShot) {
                                pendingAngle = angle;
                                pendingForce = force;
                                pendingCueX  = cueX;
                                pendingCueY  = cueY;
                                pendingSpinX = sX;
                                pendingSpinY = sY;
                                pendingShot  = true;
                            }
                        });
                    }
                    case CUE_BALL_PLACED -> {
                        double cueX = pkt.readDouble();
                        double cueY = pkt.readDouble();
                        if (!Double.isFinite(cueX) || !Double.isFinite(cueY)) break;
                        Platform.runLater(() -> {
                            Ball cue = balls.get(0);
                            cue.active = true;
                            cue.inPocket = false;
                            cue.setPosition(cueX, cueY);
                            cue.setVelocity(0, 0);
                            shockwaves.add(new Shockwave(cueX, cueY, 0.35));
                        });
                    }
                    case CUE_AIM -> {
                        boolean isAim = pkt.readBoolean();
                        double angle = pkt.readDouble();
                        double forceRatio = pkt.readDouble();
                        double sX = pkt.readDouble();
                        double sY = pkt.readDouble();
                        if (!Double.isFinite(angle) || !Double.isFinite(forceRatio)
                                || !Double.isFinite(sX) || !Double.isFinite(sY)) break;
                        Platform.runLater(() -> {
                            if (!isMyTurn) {
                                if (isAim && !oppIsAiming) {
                                    oppRenderAngle = angle;
                                    oppRenderForceRatio = forceRatio;
                                }
                                oppIsAiming = isAim;
                                oppTargetAngle = angle;
                                oppTargetForceRatio = forceRatio;
                                oppSpinX = sX;
                                oppSpinY = sY;
                            }
                        });
                    }
                    case TURN_CHANGE -> {
                        boolean myTurn   = pkt.readBoolean();
                        boolean biInHand = pkt.readBoolean();
                        String  reason   = pkt.readString();
                        Platform.runLater(() -> {
                            oppIsAiming = false;
                            oppRenderForceRatio = 0.0;
                            shotFiredThisTurn = false;
                            isMyTurn = myTurn;
                            iWasShooterThisTurn = false;
                            ballInHand = myTurn && biInHand;
                            if (ballInHand) {
                                isBreakPlacement = false;
                            }
                            turnStartTime = System.currentTimeMillis();
                            lastTickSecond = -1;
                            if (isMyTurn) {
                                showTurnToast(biInHand ? "⚠️ ĐỐI THỦ PHẠM LỖI! Bạn được đặt bi tự do (Ball-in-hand)!" : "🎯 ĐẾN LƯỢT BẠN!", Color.web("#22c55e"));
                                SoundManager.playTurnAlert();
                            } else {
                                if (biInHand) {
                                    showTurnToast("⏳ ĐỐI THỦ ĐƯỢC ĐẶT BI TỰ DO...", Color.web("#f59e0b"));
                                } else if (toastAlpha <= 0.25 || !toastMessage.contains("PHẠM LỖI")) {
                                    showTurnToast("⏳ ĐỐI THỦ ĐANG ĐÁNH...", Color.web("#f59e0b"));
                                }
                            }
                            updateStatusLabel();
                            updateTurnLabel();
                        });
                    }
                    case GAME_OVER -> {
                        boolean youWin   = pkt.readBoolean();
                        int     expGain  = pkt.readInt();
                        Platform.runLater(() -> {
                            oppIsAiming = false;
                            gameLoop.stop();
                            sceneManager.showResult(youWin, expGain, myName, opponentName);
                        });
                    }
                    default -> {}
                }
            } catch (Exception e) {
                System.err.println("[GameScene] Lỗi xử lý packet: " + e.getMessage());
            }
        });
    }

    // ============================================================
    // UI HELPERS
    // ============================================================
    private void updateStatusLabel() {
        if (isPractice) {
            statusLabel.setTextFill(Color.web("#C2A36B"));
            statusLabel.setText("LUYỆN TẬP SOLO");
            if (myAvatarPane != null) {
                myAvatarPane.setStyle("-fx-border-color: #C2A36B; -fx-border-radius: 20px; -fx-border-width: 2px;");
            }
            return;
        }

        if (isMyTurn) {
            statusLabel.setTextFill(Color.web("#C2A36B"));
            statusLabel.setText("LƯỢT CỦA BẠN");
            if (myAvatarPane != null) {
                myAvatarPane.setStyle("-fx-border-color: #C2A36B; -fx-border-radius: 20px; -fx-border-width: 2px;");
                myAvatarPane.setEffect(null);
            }
            if (oppAvatarPane != null) {
                oppAvatarPane.setStyle("-fx-border-color: transparent;");
                oppAvatarPane.setEffect(null);
            }
        } else {
            statusLabel.setTextFill(Color.web("#A5B1A8"));
            statusLabel.setText("Lượt của đối thủ");
            if (oppAvatarPane != null) {
                oppAvatarPane.setStyle("-fx-border-color: #C2A36B; -fx-border-radius: 20px; -fx-border-width: 2px;");
                oppAvatarPane.setEffect(null);
            }
            if (myAvatarPane != null) {
                myAvatarPane.setStyle("-fx-border-color: transparent;");
                myAvatarPane.setEffect(null);
            }
        }
    }

    private StackPane createBallIcon(int num) {
        Circle c = new Circle(8);
        c.setFill(Color.web(Ball.getColorForIndex(num)));
        c.setStroke(Color.WHITE);
        c.setStrokeWidth(0.8);
        
        Text t = new Text(String.valueOf(num));
        t.setFont(Font.font("Segoe UI", FontWeight.BOLD, 9));
        t.setFill((num == 1 || num == 9) ? Color.BLACK : Color.WHITE);
        
        return new StackPane(c, t);
    }

    private void updateGroupLabels() {
        Platform.runLater(() -> {
            if (isPractice) {
                myBallsBox.getChildren().clear();
                int remaining = 0;
                for (int i = 1; i < balls.size(); i++) {
                    Ball b = balls.get(i);
                    if (b.active && !b.inPocket) {
                        remaining++;
                        myBallsBox.getChildren().add(createBallIcon(b.index));
                    }
                }
                if (remaining > 0) {
                    myGroupLabel.setText("Mục tiêu: Dọn sạch bàn (Còn " + remaining + " bi)");
                } else {
                    myGroupLabel.setText("🎉 ĐÃ DỌN SẠCH TOÀN BỘ BÀN BI!");
                }
                if (oppBallsBox != null) oppBallsBox.getChildren().clear();
                return;
            }

            myBallsBox.getChildren().clear();
            List<Integer> myRemainingList = new ArrayList<>();
            if (myTargetBalls != null) {
                for (int num : myTargetBalls) {
                    boolean pocketed = false;
                    for (Ball b : balls) {
                        if (b.index == num && (!b.active || b.inPocket)) {
                            pocketed = true;
                            break;
                        }
                    }
                    if (!pocketed) {
                        myRemainingList.add(num);
                    }
                }
            }
            // Giữ nguyên thứ tự ngẫu nhiên của các bi, không sắp xếp từ bé đến lớn
            for (int num : myRemainingList) {
                myBallsBox.getChildren().add(createBallIcon(num));
            }
            int myRemaining = myRemainingList.size();

            if (oppBallsBox != null) {
                oppBallsBox.getChildren().clear();
                List<Integer> oppRemainingList = new ArrayList<>();
                if (oppTargetBalls != null) {
                    for (int num : oppTargetBalls) {
                        boolean pocketed = false;
                        for (Ball b : balls) {
                            if (b.index == num && (!b.active || b.inPocket)) {
                                pocketed = true;
                                break;
                            }
                        }
                        if (!pocketed) {
                            oppRemainingList.add(num);
                        }
                    }
                }
                // Giữ nguyên thứ tự ngẫu nhiên của các bi đối thủ, không sắp xếp từ bé đến lớn
                for (int num : oppRemainingList) {
                    oppBallsBox.getChildren().add(createBallIcon(num));
                }
                int oppRemaining = oppRemainingList.size();

                if (oppGroupLabel != null) {
                    if (oppRemaining > 0) {
                        oppGroupLabel.setText("Đối thủ: Còn " + oppRemaining + " bi");
                    } else {
                        oppGroupLabel.setText("Đang ngắm bi số 8!");
                    }
                }
            }

            if (myRemaining > 0) {
                myGroupLabel.setText("Mục tiêu: Còn " + myRemaining + " bi");
            } else {
                myGroupLabel.setText("Ăn bi số 8 để thắng!");
            }
        });
    }

    private void updatePingFpsDisplay() {
        if (pingFpsLabel == null) return;
        int ping = (network != null) ? network.getPingMs() : 0;
        String pingColor = ping < 35 ? "#22c55e" : (ping < 80 ? "#f59e0b" : "#ef4444");
        String text = isPractice ? String.format("🎮 %d FPS", currentFps)
                                 : String.format("⚡ %d ms  |  🎮 %d FPS", ping, currentFps);
        pingFpsLabel.setText(text);
        pingFpsLabel.setStyle(
            "-fx-background-color: #141D1A; " +
            "-fx-border-color: #34443B; " +
            "-fx-border-radius: 0; " +
            "-fx-padding: 3 8; " +
            "-fx-text-fill: " + pingColor + ";"
        );
    }

    private void updateTurnLabel() {
        // Đã lược bỏ thanh hiển thị theo dõi cú đánh dưới đáy màn hình
    }

    public Scene getScene() { return scene; }
}

