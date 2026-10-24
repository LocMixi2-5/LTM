package com.billiards.client.scenes;

import com.billiards.client.AvatarManager;
import com.billiards.client.NetworkClient;
import com.billiards.client.audio.SoundManager;
import com.billiards.client.game.CueSkin;
import com.billiards.client.game.CueSkinManager;
import com.billiards.client.theme.Theme;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.function.BiConsumer;

/**
 * Hộp thoại Cài Đặt & Bộ Sưu Tập Gậy Bida - Phong cách Modern Billiards Club.
 * Tích hợp hệ thống mở khóa skin gậy theo cấp độ và bộ sưu tập 4 loài bọ sừng quý hiếm.
 */
public class SettingsDialog {

    private final Stage dialog;
    private final NetworkClient network;
    private final BiConsumer<Integer, String> onSaveCallback;

    private int selectedAvatarId;
    private String equippedCueSkinId;    // Skin đang trang bị thật
    private String previewingCueSkinId;   // Skin đang chọn để xem trước
    private Image tempCustomImage;
    private final int playerLevel;

    // UI elements
    private StackPane currentAvatarPreview;
    private Label currentAvatarNameLabel;
    private FlowPane avatarGrid;

    // Bộ sưu tập gậy UI
    private Canvas cuePreviewCanvas;
    private VBox cueCardsContainer;
    private Label previewSkinNameLabel;
    private Label previewSkinTaglineLabel;
    private Label previewSkinStatusLabel;
    private Button equipCueButton;
    private Button testVfxButton;

    // Animation timer cho bản xem trước
    private AnimationTimer previewTimer;
    private double previewStartTime = 0;
    private boolean testStrikeActive = false;
    private double testStrikeTime = 0;

    public SettingsDialog(Stage parentStage, NetworkClient network, BiConsumer<Integer, String> onSaveCallback) {
        this.network = network;
        this.onSaveCallback = onSaveCallback;

        // Lấy thông tin từ session
        var session = (network != null) ? network.getSession() : null;
        this.selectedAvatarId = (session != null) ? session.avatarId : 0;
        this.equippedCueSkinId = (session != null && session.cueColor != null) ? session.cueColor : CueSkinManager.ID_DEFAULT;
        this.previewingCueSkinId = this.equippedCueSkinId;
        this.playerLevel = (session != null) ? session.level : 1;
        this.tempCustomImage = AvatarManager.getCustomAvatarImage();

        dialog = new Stage();
        dialog.initOwner(parentStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setTitle("Cài đặt & Bộ sưu tập");
        dialog.setResizable(false);

        buildUI();

        // Tự động hủy animation khi đóng dialog để tránh rò rỉ bộ nhớ
        dialog.setOnHidden(e -> {
            if (previewTimer != null) {
                previewTimer.stop();
            }
        });
    }

    public void show() {
        if (previewTimer != null) {
            previewStartTime = System.currentTimeMillis() / 1000.0;
            previewTimer.start();
        }
        dialog.showAndWait();
    }

    private void buildUI() {
        VBox rootContent = new VBox(14);
        rootContent.setPadding(new Insets(20));

        com.billiards.client.theme.SilverCornerPane root = new com.billiards.client.theme.SilverCornerPane(rootContent);
        root.setPrefWidth(660);

        // Header Tiêu đề
        Label titleLabel = new Label("Cài đặt & Bộ sưu tập gậy cơ");
        titleLabel.setFont(Font.font(Theme.FONT_FAMILY, FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.web(Theme.HEX_TEXT));

        TabPane tabPane = new TabPane();
        tabPane.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-tab-min-width: 140px;"
        );

        // Tab 1: Đổi Avatar
        Tab avatarTab = new Tab("Avatar", buildAvatarTab());
        avatarTab.setClosable(false);

        // Tab 2: Bộ Sưu Tập Gậy Cơ (Mở khóa theo cấp độ & 4 loài bọ sừng)
        Tab cueTab = new Tab("Bộ sưu tập gậy", buildCueCollectionTab());
        cueTab.setClosable(false);

        // Tab 3: Âm Thanh & Đồ Họa
        Tab audioTab = new Tab("Âm thanh & Đồ họa", buildAudioTab());
        audioTab.setClosable(false);

        tabPane.getTabs().addAll(cueTab, avatarTab, audioTab);

        // Footer Buttons (Lưu & Đóng)
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(8, 0, 0, 0));

        Button cancelBtn = Theme.btnSecondary("Đóng", 100, 38);
        cancelBtn.setOnAction(e -> dialog.close());

        Button saveBtn = Theme.btnPrimary("Lưu cài đặt", 130, 38);
        saveBtn.setOnAction(e -> saveSettings());

        footer.getChildren().addAll(cancelBtn, saveBtn);

        rootContent.getChildren().addAll(titleLabel, tabPane, footer);

        Scene scene = new Scene(root);
        dialog.setScene(scene);
    }

    // ============================================================
    // TAB 1: AVATAR
    // ============================================================
    private VBox buildAvatarTab() {
        VBox box = new VBox(14);
        box.setPadding(new Insets(14));
        box.setStyle(
            "-fx-background-color: " + Theme.HEX_SURFACE_HOVER + ";" +
            "-fx-background-radius: 0;" +
            "-fx-border-color: " + Theme.HEX_BORDER + ";" +
            "-fx-border-radius: 0;" +
            "-fx-border-width: 1.0;"
        );

        HBox currentBox = new HBox(16);
        currentBox.setAlignment(Pos.CENTER_LEFT);
        currentBox.setPadding(new Insets(12, 16, 12, 16));
        currentBox.setStyle(
            "-fx-background-color: #141D1A;" +
            "-fx-background-radius: 0;" +
            "-fx-border-color: " + Theme.HEX_BORDER + ";" +
            "-fx-border-radius: 0;" +
            "-fx-border-width: 1.0;"
        );

        currentAvatarPreview = AvatarManager.createAvatarNode(selectedAvatarId, 56, tempCustomImage);

        VBox infoBox = new VBox(4);
        var session = (network != null) ? network.getSession() : null;
        String uName = (session != null) ? session.username : "Cơ thủ";

        Label nameLbl = new Label(uName + " • Cấp " + playerLevel);
        nameLbl.setFont(Font.font(Theme.FONT_FAMILY, FontWeight.BOLD, 15));
        nameLbl.setTextFill(Color.web(Theme.HEX_TEXT));

        currentAvatarNameLabel = new Label("Đang chọn: " + AvatarManager.getAvatarName(selectedAvatarId));
        currentAvatarNameLabel.setFont(Font.font(Theme.FONT_FAMILY, 12));
        currentAvatarNameLabel.setTextFill(Color.web(Theme.HEX_BRONZE));

        infoBox.getChildren().addAll(nameLbl, currentAvatarNameLabel);
        currentBox.getChildren().addAll(currentAvatarPreview, infoBox);

        Label listTitle = new Label("Chọn từ kho avatar có sẵn:");
        listTitle.setFont(Font.font(Theme.FONT_FAMILY, FontWeight.BOLD, 13));
        listTitle.setTextFill(Color.web(Theme.HEX_TEXT_MUTED));

        avatarGrid = new FlowPane(10, 10);
        avatarGrid.setPrefWrapLength(560);
        avatarGrid.setAlignment(Pos.CENTER);
        refreshAvatarGrid();

        ScrollPane scroll = new ScrollPane(avatarGrid);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(180);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");

        box.getChildren().addAll(currentBox, listTitle, scroll);
        return box;
    }

    private void refreshAvatarGrid() {
        avatarGrid.getChildren().clear();
        for (int i = 0; i < AvatarManager.getAvatarCount(); i++) {
            final int avatarIndex = i;
            boolean isSelected = (selectedAvatarId == avatarIndex && tempCustomImage == null);

            VBox item = new VBox(4);
            item.setAlignment(Pos.CENTER);
            item.setStyle("-fx-cursor: hand;");

            StackPane node = AvatarManager.createSelectableAvatar(avatarIndex, 44, isSelected);

            Label name = new Label(AvatarManager.getAvatarName(avatarIndex));
            name.setFont(Font.font(Theme.FONT_FAMILY, 10));
            name.setTextFill(isSelected ? Color.web(Theme.HEX_BRONZE) : Color.web(Theme.HEX_TEXT_MUTED));

            item.getChildren().addAll(node, name);
            item.setOnMouseClicked(e -> {
                selectedAvatarId = avatarIndex;
                tempCustomImage = null;
                updateAvatarPreview();
                refreshAvatarGrid();
            });

            avatarGrid.getChildren().add(item);
        }
    }

    private void updateAvatarPreview() {
        currentAvatarPreview.getChildren().clear();
        StackPane updated = AvatarManager.createAvatarNode(selectedAvatarId, 56, tempCustomImage);
        currentAvatarPreview.getChildren().addAll(updated.getChildren());
        if (tempCustomImage == null) {
            currentAvatarNameLabel.setText("Đang chọn: " + AvatarManager.getAvatarName(selectedAvatarId));
        }
    }

    // ============================================================
    // TAB 2: BỘ SƯU TẬP GẬY CƠ (CUE COLLECTION TAB)
    // ============================================================
    private VBox buildCueCollectionTab() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(12));
        box.setStyle(
            "-fx-background-color: " + Theme.HEX_SURFACE_HOVER + ";" +
            "-fx-background-radius: 0;" +
            "-fx-border-color: " + Theme.HEX_BORDER + ";" +
            "-fx-border-radius: 0;" +
            "-fx-border-width: 1.0;"
        );

        // 1. KHU VỰC XEM TRƯỚC & TRƯNG BÀY (PREVIEW STAGE)
        VBox previewBox = new VBox(8);
        previewBox.setPadding(new Insets(10));
        previewBox.setStyle(
            "-fx-background-color: #121A17;" +
            "-fx-border-color: " + Theme.HEX_BORDER + ";" +
            "-fx-border-width: 1.0;"
        );

        cuePreviewCanvas = new Canvas(595, 140);

        // Thông tin skin đang xem trước & các nút thao tác
        HBox infoBar = new HBox(12);
        infoBar.setAlignment(Pos.CENTER_LEFT);

        VBox textInfo = new VBox(2);
        previewSkinNameLabel = new Label();
        previewSkinNameLabel.setFont(Font.font(Theme.FONT_FAMILY, FontWeight.BOLD, 15));
        previewSkinNameLabel.setTextFill(Color.web(Theme.HEX_TEXT));

        previewSkinTaglineLabel = new Label();
        previewSkinTaglineLabel.setFont(Font.font(Theme.FONT_FAMILY, 12));
        previewSkinTaglineLabel.setTextFill(Color.web(Theme.HEX_TEXT_MUTED));

        previewSkinStatusLabel = new Label();
        previewSkinStatusLabel.setFont(Font.font(Theme.FONT_FAMILY, FontWeight.BOLD, 11));

        textInfo.getChildren().addAll(previewSkinNameLabel, previewSkinTaglineLabel, previewSkinStatusLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Nút thử hiệu ứng cú đánh
        testVfxButton = Theme.btnSecondary("Thử hiệu ứng", 120, 32);
        testVfxButton.setOnAction(e -> {
            SoundManager.playButtonClick();
            testStrikeActive = true;
            testStrikeTime = System.currentTimeMillis() / 1000.0;
        });

        // Nút trang bị
        equipCueButton = Theme.btnPrimary("Trang bị", 110, 32);
        equipCueButton.setOnAction(e -> {
            SoundManager.playButtonClick();
            CueSkin current = CueSkinManager.getSkin(previewingCueSkinId);
            if (current.isUnlocked(playerLevel)) {
                equippedCueSkinId = current.id;
                updatePreviewDetails();
                refreshCueCards();
            }
        });

        infoBar.getChildren().addAll(textInfo, spacer, testVfxButton, equipCueButton);
        previewBox.getChildren().addAll(cuePreviewCanvas, infoBar);

        // 2. DANH SÁCH CÁC THẺ SKIN TRONG BỘ SƯU TẬP
        Label listTitle = new Label("Bộ sưu tập bọ sừng & Mốc cấp độ mở khóa:");
        listTitle.setFont(Font.font(Theme.FONT_FAMILY, FontWeight.BOLD, 12));
        listTitle.setTextFill(Color.web(Theme.HEX_TEXT_MUTED));

        cueCardsContainer = new VBox(8);
        refreshCueCards();

        ScrollPane scrollPane = new ScrollPane(cueCardsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(180);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");

        box.getChildren().addAll(previewBox, listTitle, scrollPane);

        // Khởi động Animation Timer cho Canvas xem trước
        setupPreviewAnimationTimer();

        // Cập nhật thông tin skin ban đầu
        updatePreviewDetails();

        return box;
    }

    private void setupPreviewAnimationTimer() {
        previewTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double nowSec = System.currentTimeMillis() / 1000.0;
                double animTime = nowSec - previewStartTime;

                double strikeAge = nowSec - testStrikeTime;
                if (testStrikeActive && strikeAge > 0.65) {
                    testStrikeActive = false;
                }

                CueSkin skin = CueSkinManager.getSkin(previewingCueSkinId);
                var gc = cuePreviewCanvas.getGraphicsContext2D();
                CueSkinManager.renderCollectionPreview(
                    gc,
                    cuePreviewCanvas.getWidth(),
                    cuePreviewCanvas.getHeight(),
                    skin,
                    animTime,
                    testStrikeActive,
                    testStrikeActive ? strikeAge : 999.0
                );
            }
        };
    }

    private void updatePreviewDetails() {
        CueSkin skin = CueSkinManager.getSkin(previewingCueSkinId);
        boolean isEquipped = skin.id.equalsIgnoreCase(equippedCueSkinId);
        boolean isUnlocked = skin.isUnlocked(playerLevel);

        previewSkinNameLabel.setText("[" + skin.tierName + "] " + skin.name);
        previewSkinNameLabel.setTextFill(Color.web(skin.tierColorHex));
        previewSkinTaglineLabel.setText(skin.beetleName + " • " + skin.tagline);

        if (isEquipped) {
            previewSkinStatusLabel.setText("✓ ĐANG TRANG BỊ");
            previewSkinStatusLabel.setTextFill(Color.web(Theme.HEX_BRONZE));
            equipCueButton.setDisable(true);
            equipCueButton.setText("Đang dùng");
        } else if (isUnlocked) {
            previewSkinStatusLabel.setText("● CÓ THỂ TRANG BỊ (Đã mở khóa ở Cấp " + skin.requiredLevel + ")");
            previewSkinStatusLabel.setTextFill(Color.web(Theme.HEX_PRIMARY));
            equipCueButton.setDisable(false);
            equipCueButton.setText("Trang bị");
        } else {
            int levelsNeeded = skin.requiredLevel - playerLevel;
            previewSkinStatusLabel.setText("🔒 CHƯA MỞ KHÓA — Yêu cầu Cấp " + skin.requiredLevel + " (Cần thêm " + levelsNeeded + " cấp nữa)");
            previewSkinStatusLabel.setTextFill(Color.web(Theme.HEX_DANGER));
            equipCueButton.setDisable(true);
            equipCueButton.setText("Khóa Cấp " + skin.requiredLevel);
        }
    }

    private void refreshCueCards() {
        cueCardsContainer.getChildren().clear();

        for (CueSkin skin : CueSkinManager.getAllSkins()) {
            boolean isEquipped = skin.id.equalsIgnoreCase(equippedCueSkinId);
            boolean isPreviewing = skin.id.equalsIgnoreCase(previewingCueSkinId);
            boolean isUnlocked = skin.isUnlocked(playerLevel);

            HBox card = new HBox(10);
            card.setAlignment(Pos.CENTER_LEFT);
            card.setPadding(new Insets(8, 12, 8, 12));
            card.setStyle(
                "-fx-background-color: " + (isPreviewing ? "#1A2520;" : "#131B18;") +
                "-fx-border-color: " + (isPreviewing ? skin.tierColorHex : (isUnlocked ? "#2D3D35" : "#1E2723")) + ";" +
                "-fx-border-width: " + (isPreviewing ? "1.5;" : "1.0;") +
                "-fx-cursor: hand;"
            );

            // 1. Huy hiệu loài bọ / Chấm màu
            Circle badge = new Circle(7, Color.web(skin.mainHex));
            badge.setStroke(Color.web(skin.accentHex));
            badge.setStrokeWidth(1.5);

            // 2. Nhãn Phẩm cấp (Tier Pill)
            Label tierBadge = new Label(skin.tierName);
            tierBadge.setFont(Font.font(Theme.FONT_FAMILY, FontWeight.BOLD, 10));
            tierBadge.setPadding(new Insets(2, 6, 2, 6));
            tierBadge.setStyle(
                "-fx-background-color: " + skin.tierColorHex + "25;" +
                "-fx-border-color: " + skin.tierColorHex + ";" +
                "-fx-border-width: 1.0;" +
                "-fx-text-fill: " + skin.tierColorHex + ";"
            );

            // 3. Nhãn cấp yêu cầu
            Label reqLabel = new Label("Cấp " + skin.requiredLevel);
            reqLabel.setFont(Font.font(Theme.FONT_FAMILY, FontWeight.BOLD, 11));
            reqLabel.setTextFill(isUnlocked ? Color.web(Theme.HEX_BRONZE) : Color.web(Theme.HEX_TEXT_MUTED));
            reqLabel.setPrefWidth(48);

            // 4. Tên skin và mô tả ngắn
            VBox nameBox = new VBox(2);
            Label nameLbl = new Label(skin.name);
            nameLbl.setFont(Font.font(Theme.FONT_FAMILY, FontWeight.BOLD, 13));
            nameLbl.setTextFill(isUnlocked ? Color.web(Theme.HEX_TEXT) : Color.web(Theme.HEX_TEXT_MUTED));

            Label descLbl = new Label(skin.tagline);
            descLbl.setFont(Font.font(Theme.FONT_FAMILY, 11));
            descLbl.setTextFill(Color.web(Theme.HEX_TEXT_MUTED));
            nameBox.getChildren().addAll(nameLbl, descLbl);

            Region cardSpacer = new Region();
            HBox.setHgrow(cardSpacer, Priority.ALWAYS);

            // 5. Trạng thái thẻ
            Label statusBadge = new Label();
            statusBadge.setFont(Font.font(Theme.FONT_FAMILY, FontWeight.BOLD, 11));
            if (isEquipped) {
                statusBadge.setText("✓ ĐANG DÙNG");
                statusBadge.setTextFill(Color.web(Theme.HEX_BRONZE));
            } else if (isUnlocked) {
                statusBadge.setText("ĐÃ MỞ KHÓA");
                statusBadge.setTextFill(Color.web(Theme.HEX_PRIMARY));
            } else {
                statusBadge.setText("🔒 CẤP " + skin.requiredLevel);
                statusBadge.setTextFill(Color.web("#7F8C8D"));
            }

            card.getChildren().addAll(badge, tierBadge, reqLabel, nameBox, cardSpacer, statusBadge);

            // Nhấp để xem trước
            card.setOnMouseClicked(e -> {
                previewingCueSkinId = skin.id;
                updatePreviewDetails();
                refreshCueCards();
            });

            cueCardsContainer.getChildren().add(card);
        }
    }

    // ============================================================
    // TAB 3: ÂM THANH & HIỆU ỨNG ĐỒ HỌA
    // ============================================================
    private VBox buildAudioTab() {
        VBox box = new VBox(16);
        box.setPadding(new Insets(16));
        box.setStyle(
            "-fx-background-color: " + Theme.HEX_SURFACE_HOVER + ";" +
            "-fx-background-radius: 0;" +
            "-fx-border-color: " + Theme.HEX_BORDER + ";" +
            "-fx-border-radius: 0;" +
            "-fx-border-width: 1.0;"
        );

        // 1. Nhạc nền (BGM)
        VBox bgmBox = new VBox(6);
        HBox bgmHeader = new HBox(10);
        Label bgmLbl = new Label("Âm lượng nhạc nền (BGM):");
        bgmLbl.setFont(Font.font(Theme.FONT_FAMILY, FontWeight.BOLD, 13));
        bgmLbl.setTextFill(Color.web(Theme.HEX_TEXT));

        Label bgmValLbl = new Label((int)(SoundManager.getBgmVolume() * 100) + "%");
        bgmValLbl.setFont(Font.font(Theme.FONT_FAMILY, FontWeight.BOLD, 13));
        bgmValLbl.setTextFill(Color.web(Theme.HEX_PRIMARY));
        bgmHeader.getChildren().addAll(bgmLbl, bgmValLbl);

        Slider bgmSlider = new Slider(0, 100, SoundManager.getBgmVolume() * 100);
        bgmSlider.setShowTickMarks(true);
        bgmSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double v = newVal.doubleValue() / 100.0;
            SoundManager.setBgmVolume(v);
            bgmValLbl.setText(newVal.intValue() + "%");
        });
        bgmBox.getChildren().addAll(bgmHeader, bgmSlider);

        // 2. Hiệu ứng va chạm bi (SFX)
        VBox sfxBox = new VBox(6);
        HBox sfxHeader = new HBox(10);
        Label sfxLbl = new Label("Hiệu ứng va chạm bi (SFX):");
        sfxLbl.setFont(Font.font(Theme.FONT_FAMILY, FontWeight.BOLD, 13));
        sfxLbl.setTextFill(Color.web(Theme.HEX_TEXT));

        Label sfxValLbl = new Label((int)(SoundManager.getSfxVolume() * 100) + "%");
        sfxValLbl.setFont(Font.font(Theme.FONT_FAMILY, FontWeight.BOLD, 13));
        sfxValLbl.setTextFill(Color.web(Theme.HEX_PRIMARY));
        sfxHeader.getChildren().addAll(sfxLbl, sfxValLbl);

        Slider sfxSlider = new Slider(0, 100, SoundManager.getSfxVolume() * 100);
        sfxSlider.setShowTickMarks(true);
        sfxSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double v = newVal.doubleValue() / 100.0;
            SoundManager.setSfxVolume(v);
            sfxValLbl.setText(newVal.intValue() + "%");
        });
        sfxBox.getChildren().addAll(sfxHeader, sfxSlider);

        // 3. Tắt tiếng & Thử âm thanh
        HBox controlRow = new HBox(16);
        controlRow.setAlignment(Pos.CENTER_LEFT);

        CheckBox muteCheck = new CheckBox("Tắt toàn bộ âm thanh (Mute)");
        muteCheck.setSelected(SoundManager.isMuted());
        muteCheck.setTextFill(Color.web(Theme.HEX_TEXT));
        muteCheck.setFont(Font.font(Theme.FONT_FAMILY, 13));
        muteCheck.setOnAction(e -> SoundManager.setMuted(muteCheck.isSelected()));

        Button testSoundBtn = Theme.btnSecondary("Thử va chạm", 120, 32);
        testSoundBtn.setOnAction(e -> SoundManager.testSFX());
        controlRow.getChildren().addAll(muteCheck, testSoundBtn);

        // 4. Bật/Tắt hiệu ứng skin gậy (VFX Toggle) theo yêu cầu
        Separator sep = new Separator();
        VBox vfxBox = new VBox(6);
        Label vfxTitle = new Label("Tùy chọn Đồ Họa Cây Cơ:");
        vfxTitle.setFont(Font.font(Theme.FONT_FAMILY, FontWeight.BOLD, 13));
        vfxTitle.setTextFill(Color.web(Theme.HEX_TEXT));

        CheckBox vfxToggle = new CheckBox("Bật hiệu ứng phản quang & Chớp đánh của Skin Gậy (VFX Animation)");
        vfxToggle.setSelected(CueSkinManager.isVfxEnabled());
        vfxToggle.setTextFill(Color.web(Theme.HEX_TEXT));
        vfxToggle.setFont(Font.font(Theme.FONT_FAMILY, 13));
        vfxToggle.setOnAction(e -> CueSkinManager.setVfxEnabled(vfxToggle.isSelected()));

        Label vfxNote = new Label("Khi tắt, gậy vẫn giữ nguyên 100% hình khối chất liệu bọ sừng nhưng tắt ánh sáng động.");
        vfxNote.setFont(Font.font(Theme.FONT_FAMILY, 11));
        vfxNote.setTextFill(Color.web(Theme.HEX_TEXT_MUTED));
        vfxBox.getChildren().addAll(vfxTitle, vfxToggle, vfxNote);

        box.getChildren().addAll(bgmBox, sfxBox, controlRow, sep, vfxBox);
        return box;
    }

    // ============================================================
    // LƯU CÀI ĐẶT
    // ============================================================
    private void saveSettings() {
        AvatarManager.setCustomAvatarImage(tempCustomImage);

        // Đảm bảo dùng skin đã trang bị
        String finalCueSkin = equippedCueSkinId;

        if (network != null) {
            network.sendSettingsUpdate(selectedAvatarId, finalCueSkin);
            var session = network.getSession();
            if (session != null) {
                session.avatarId = selectedAvatarId;
                session.cueColor = finalCueSkin;
            }
        }

        if (onSaveCallback != null) {
            onSaveCallback.accept(selectedAvatarId, finalCueSkin);
        }

        if (previewTimer != null) {
            previewTimer.stop();
        }

        dialog.close();
    }
}
