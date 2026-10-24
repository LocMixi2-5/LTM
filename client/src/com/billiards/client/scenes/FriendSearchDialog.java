package com.billiards.client.scenes;

import com.billiards.client.AvatarManager;
import com.billiards.client.NetworkClient;
import com.billiards.client.theme.Theme;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Hộp thoại tìm kiếm và thêm bạn bè - Phong cách Modern Billiards Club.
 */
public class FriendSearchDialog {

    private final Stage dialog;
    private final NetworkClient network;
    private final Stage owner;

    // UI elements
    private VBox resultsContainer;
    private VBox pendingContainer;
    private TextField searchField;
    private Label statusLabel;
    private Label pendingCountLabel;

    // Danh sách bạn bè hiện tại
    private final Set<String> currentFriends = new HashSet<>();

    // Callback khi danh sách bạn bè thay đổi
    private Runnable onFriendListChanged;

    public FriendSearchDialog(Stage owner, NetworkClient network) {
        this.owner = owner;
        this.network = network;

        dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setTitle("Tìm bạn bè");

        build();
    }

    public void setOnFriendListChanged(Runnable callback) {
        this.onFriendListChanged = callback;
    }

    private void build() {
        VBox rootContent = new VBox(0);
        rootContent.setPrefSize(520, 620);

        com.billiards.client.theme.SilverCornerPane root = new com.billiards.client.theme.SilverCornerPane(rootContent);
        root.setPrefSize(520, 620);

        // HEADER
        HBox header = buildHeader();

        // SEARCH BAR
        HBox searchBar = buildSearchBar();

        // STATUS LABEL
        statusLabel = new Label("");
        statusLabel.setFont(Font.font(Theme.FONT_FAMILY, 12));
        statusLabel.setTextFill(Color.web(Theme.HEX_TEXT_MUTED));
        statusLabel.setPadding(new Insets(0, 20, 4, 20));

        // TAB AREA
        VBox tabContent = buildTabContent();

        rootContent.getChildren().addAll(header, searchBar, statusLabel, tabContent);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);
    }

    private HBox buildHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(16, 20, 12, 20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle(
            "-fx-background-color: #141D1A;" +
            "-fx-background-radius: 0;" +
            "-fx-border-color: " + Theme.HEX_BORDER + ";" +
            "-fx-border-width: 0 0 1 0;"
        );

        Label title = new Label("Tìm bạn bè");
        title.setFont(Font.font(Theme.FONT_FAMILY, FontWeight.BOLD, 18));
        title.setTextFill(Color.web(Theme.HEX_TEXT));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        closeBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + Theme.HEX_TEXT_MUTED + ";" +
            "-fx-font-size: 16;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 2 8;"
        );
        closeBtn.setOnMouseEntered(e -> closeBtn.setStyle(
            "-fx-background-color: #24322B;" +
            "-fx-text-fill: " + Theme.HEX_TEXT + ";" +
            "-fx-font-size: 16;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 2 8;" +
            "-fx-background-radius: 4;"
        ));
        closeBtn.setOnMouseExited(e -> closeBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + Theme.HEX_TEXT_MUTED + ";" +
            "-fx-font-size: 16;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 2 8;"
        ));
        closeBtn.setOnAction(e -> close());

        header.getChildren().addAll(title, spacer, closeBtn);
        return header;
    }

    private HBox buildSearchBar() {
        HBox bar = new HBox(8);
        bar.setPadding(new Insets(12, 20, 8, 20));
        bar.setAlignment(Pos.CENTER);

        searchField = new TextField();
        searchField.setPromptText("Nhập tên người chơi...");
        searchField.setPrefHeight(38);
        searchField.setStyle(Theme.inputStyle());
        HBox.setHgrow(searchField, Priority.ALWAYS);

        Button searchBtn = Theme.btnPrimary("Tìm kiếm", 90, 38);
        searchBtn.setOnAction(e -> doSearch());
        searchField.setOnAction(e -> doSearch());

        bar.getChildren().addAll(searchField, searchBtn);
        return bar;
    }

    private VBox buildTabContent() {
        VBox content = new VBox(8);
        content.setPadding(new Insets(4, 20, 16, 20));
        VBox.setVgrow(content, Priority.ALWAYS);

        // Phần trên: Kết quả tìm kiếm
        Label searchTitle = new Label("Người chơi");
        searchTitle.setFont(Font.font(Theme.FONT_FAMILY, FontWeight.BOLD, 13));
        searchTitle.setTextFill(Color.web(Theme.HEX_TEXT_MUTED));

        resultsContainer = new VBox(6);
        resultsContainer.setStyle(
            "-fx-background-color: #141D1A;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 8;" +
            "-fx-border-color: " + Theme.HEX_BORDER + ";" +
            "-fx-border-radius: 8;" +
            "-fx-border-width: 1.0;"
        );
        resultsContainer.setMinHeight(100);

        ScrollPane searchScroll = new ScrollPane(resultsContainer);
        searchScroll.setFitToWidth(true);
        searchScroll.setPrefHeight(200);
        searchScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(searchScroll, Priority.ALWAYS);

        // Phần dưới: Lời mời kết bạn
        HBox pendingHeader = new HBox(8);
        pendingHeader.setAlignment(Pos.CENTER_LEFT);
        pendingHeader.setPadding(new Insets(8, 0, 0, 0));

        Label pendingTitle = new Label("Lời mời kết bạn");
        pendingTitle.setFont(Font.font(Theme.FONT_FAMILY, FontWeight.BOLD, 13));
        pendingTitle.setTextFill(Color.web(Theme.HEX_BRONZE));

        pendingCountLabel = new Label("");
        pendingCountLabel.setFont(Font.font(Theme.FONT_FAMILY, FontWeight.BOLD, 11));
        pendingCountLabel.setTextFill(Color.web(Theme.HEX_TEXT));
        pendingCountLabel.setStyle(
            "-fx-background-color: " + Theme.HEX_PRIMARY + ";" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 1 6;"
        );
        pendingCountLabel.setVisible(false);

        pendingHeader.getChildren().addAll(pendingTitle, pendingCountLabel);

        pendingContainer = new VBox(6);
        pendingContainer.setStyle(
            "-fx-background-color: #141D1A;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 8;" +
            "-fx-border-color: " + Theme.HEX_BORDER + ";" +
            "-fx-border-radius: 8;" +
            "-fx-border-width: 1.0;"
        );
        pendingContainer.setMinHeight(80);

        ScrollPane pendingScroll = new ScrollPane(pendingContainer);
        pendingScroll.setFitToWidth(true);
        pendingScroll.setPrefHeight(180);
        pendingScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(pendingScroll, Priority.ALWAYS);

        content.getChildren().addAll(searchTitle, searchScroll, pendingHeader, pendingScroll);
        return content;
    }

    private void doSearch() {
        String keyword = searchField.getText().trim();
        resultsContainer.getChildren().clear();

        Label searching = new Label("Đang tìm kiếm...");
        searching.setTextFill(Color.web(Theme.HEX_TEXT_MUTED));
        searching.setFont(Font.font(Theme.FONT_FAMILY, 13));
        resultsContainer.getChildren().add(searching);

        network.sendSearchPlayers(keyword.isEmpty() ? "" : keyword);
    }

    private void handleSearchResults(String data) {
        Platform.runLater(() -> {
            resultsContainer.getChildren().clear();

            if (data == null || data.isEmpty()) {
                Label noResult = new Label("Không tìm thấy người chơi nào.");
                noResult.setTextFill(Color.web(Theme.HEX_TEXT_MUTED));
                noResult.setFont(Font.font(Theme.FONT_FAMILY, 13));
                noResult.setPadding(new Insets(10));
                resultsContainer.getChildren().add(noResult);
                statusLabel.setText("0 kết quả");
                return;
            }

            String[] players = data.split(";");
            statusLabel.setText(players.length + " kết quả");

            for (String playerData : players) {
                String[] parts = playerData.split("\\|");
                if (parts.length >= 3) {
                    String name = parts[0];
                    int level = Integer.parseInt(parts[1]);
                    int avatarId = Integer.parseInt(parts[2]);

                    HBox card = createSearchResultCard(name, level, avatarId);
                    resultsContainer.getChildren().add(card);
                }
            }
        });
    }

    private void handleFriendList(String data) {
        Platform.runLater(() -> {
            currentFriends.clear();
            if (data != null && !data.isEmpty()) {
                String[] friends = data.split(";");
                for (String friendData : friends) {
                    String[] parts = friendData.split("\\|");
                    if (parts.length >= 3) {
                        String name = parts[0];
                        currentFriends.add(name);
                    }
                }
            }
        });
    }

    private void handlePendingRequests(String data) {
        Platform.runLater(() -> {
            pendingContainer.getChildren().clear();

            if (data == null || data.isEmpty()) {
                Label noPending = new Label("Không có lời mời kết bạn nào.");
                noPending.setTextFill(Color.web(Theme.HEX_TEXT_MUTED));
                noPending.setFont(Font.font(Theme.FONT_FAMILY, FontPosture.ITALIC, 13));
                noPending.setPadding(new Insets(10));
                pendingContainer.getChildren().add(noPending);
                pendingCountLabel.setVisible(false);
                return;
            }

            String[] requests = data.split(";");
            pendingCountLabel.setText(String.valueOf(requests.length));
            pendingCountLabel.setVisible(true);

            for (String requestData : requests) {
                String[] parts = requestData.split("\\|");
                if (parts.length >= 3) {
                    String name = parts[0];
                    int level = Integer.parseInt(parts[1]);
                    int avatarId = Integer.parseInt(parts[2]);

                    HBox card = createPendingRequestCard(name, level, avatarId);
                    pendingContainer.getChildren().add(card);
                }
            }
        });
    }

    private void handleAddFriendResponse(boolean success, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(success ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING);
            alert.initOwner(dialog);
            alert.setTitle(success ? "Thành công" : "Thông báo");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();

            if (success) {
                doSearch();
            }
        });
    }

    private void handleAcceptResponse(boolean success, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(success ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING);
            alert.initOwner(dialog);
            alert.setTitle(success ? "Thành công" : "Thông báo");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();

            if (success) {
                network.sendPendingRequestsList();
                network.sendFriendListRequest();
                if (onFriendListChanged != null) {
                    onFriendListChanged.run();
                }
            }
        });
    }

    private void handleDeclineResponse(boolean success, String message) {
        Platform.runLater(() -> {
            if (success) {
                network.sendPendingRequestsList();
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.initOwner(dialog);
                alert.setTitle("Thông báo");
                alert.setHeaderText(null);
                alert.setContentText(message);
                alert.showAndWait();
            }
        });
    }

    private HBox createSearchResultCard(String name, int level, int avatarId) {
        HBox card = new HBox(12);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(8, 12, 8, 12));
        card.setStyle(
            "-fx-background-color: " + Theme.HEX_SURFACE + ";" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: " + Theme.HEX_BORDER + ";" +
            "-fx-border-radius: 8;" +
            "-fx-border-width: 1.0;"
        );

        StackPane avatar = AvatarManager.createAvatarNode(avatarId, 34);

        VBox info = new VBox(2);
        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font(Theme.FONT_FAMILY, FontWeight.BOLD, 13));
        nameLabel.setTextFill(Color.web(Theme.HEX_TEXT));

        Label levelLabel = new Label("Cấp độ " + level);
        levelLabel.setFont(Font.font(Theme.FONT_FAMILY, 11));
        levelLabel.setTextFill(Color.web(Theme.HEX_TEXT_MUTED));

        info.getChildren().addAll(nameLabel, levelLabel);
        HBox.setHgrow(info, Priority.ALWAYS);

        if (currentFriends.contains(name)) {
            Label addedLabel = new Label("Đã là bạn bè");
            addedLabel.setFont(Font.font(Theme.FONT_FAMILY, 12));
            addedLabel.setTextFill(Color.web(Theme.HEX_PRIMARY));
            card.getChildren().addAll(avatar, info, addedLabel);
        } else {
            Button addBtn = Theme.btnPrimary("Kết bạn", 85, 30);
            addBtn.setStyle(addBtn.getStyle() + "; -fx-font-size: 11px;");
            addBtn.setOnAction(e -> {
                addBtn.setDisable(true);
                addBtn.setText("Đang gửi...");
                network.sendAddFriend(name);
            });
            card.getChildren().addAll(avatar, info, addBtn);
        }

        return card;
    }

    private HBox createPendingRequestCard(String name, int level, int avatarId) {
        HBox card = new HBox(12);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(8, 12, 8, 12));
        card.setStyle(
            "-fx-background-color: " + Theme.HEX_SURFACE + ";" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: " + Theme.HEX_BRONZE + ";" +
            "-fx-border-radius: 8;" +
            "-fx-border-width: 1.0;"
        );

        StackPane avatar = AvatarManager.createAvatarNode(avatarId, 34);

        VBox info = new VBox(2);
        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font(Theme.FONT_FAMILY, FontWeight.BOLD, 13));
        nameLabel.setTextFill(Color.web(Theme.HEX_TEXT));

        Label requestLabel = new Label("Cấp " + level + " • Yêu cầu kết bạn");
        requestLabel.setFont(Font.font(Theme.FONT_FAMILY, 11));
        requestLabel.setTextFill(Color.web(Theme.HEX_BRONZE));

        info.getChildren().addAll(nameLabel, requestLabel);
        HBox.setHgrow(info, Priority.ALWAYS);

        Button acceptBtn = Theme.btnPrimary("Đồng ý", 75, 28);
        acceptBtn.setStyle(acceptBtn.getStyle() + "; -fx-font-size: 11px; -fx-padding: 2 8;");
        acceptBtn.setOnAction(e -> {
            acceptBtn.setDisable(true);
            acceptBtn.setText("...");
            network.sendAcceptFriend(name);
        });

        Button declineBtn = Theme.btnSecondary("Từ chối", 75, 28);
        declineBtn.setStyle(declineBtn.getStyle() + "; -fx-font-size: 11px; -fx-padding: 2 8;");
        declineBtn.setOnAction(e -> {
            declineBtn.setDisable(true);
            declineBtn.setText("...");
            network.sendDeclineFriend(name);
        });

        HBox buttons = new HBox(6, acceptBtn, declineBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(avatar, info, buttons);
        return card;
    }

    public void show() {
        setupNetworkCallback();
        network.sendFriendListRequest();
        network.sendPendingRequestsList();
        network.sendSearchPlayers("");

        statusLabel.setText("Đang tải...");
        dialog.show();

        if (owner != null) {
            dialog.setX(owner.getX() + (owner.getWidth() - dialog.getWidth()) / 2);
            dialog.setY(owner.getY() + (owner.getHeight() - dialog.getHeight()) / 2);
        }

        searchField.requestFocus();
    }

    private void close() {
        dialog.close();
    }

    public Stage getDialog() {
        return dialog;
    }

    private void setupNetworkCallback() {
        network.setOnPacketReceived(pkt -> {
            try {
                switch (pkt.type) {
                    case SEARCH_PLAYERS_RESPONSE -> {
                        String data = pkt.readString();
                        handleSearchResults(data);
                    }
                    case ADD_FRIEND_RESPONSE -> {
                        boolean success = pkt.readBoolean();
                        String message = pkt.readString();
                        handleAddFriendResponse(success, message);
                    }
                    case FRIEND_LIST_RESPONSE -> {
                        String data = pkt.readString();
                        handleFriendList(data);
                    }
                    case PENDING_REQUESTS_RESPONSE -> {
                        String data = pkt.readString();
                        handlePendingRequests(data);
                    }
                    case FRIEND_ACCEPT_RESPONSE -> {
                        boolean success = pkt.readBoolean();
                        String message = pkt.readString();
                        handleAcceptResponse(success, message);
                    }
                    case FRIEND_DECLINE_RESPONSE -> {
                        boolean success = pkt.readBoolean();
                        String message = pkt.readString();
                        handleDeclineResponse(success, message);
                    }
                    default -> {}
                }
            } catch (IOException e) {
                System.err.println("[FriendSearchDialog] Lỗi đọc packet: " + e.getMessage());
            }
        });
    }
}
