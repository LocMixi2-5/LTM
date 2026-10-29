# TÀI LIỆU THAM KHẢO



[1] Hồ sơ yêu cầu của đồ án trò chơi bi-a hai người qua mạng: kết nối và giao thức, tài khoản và dữ liệu, gameplay và vật lý, giao diện và cá nhân hóa.

[2] Mã nguồn ứng dụng: https://github.com/LocMixi2-5/LTM/tree/00bf1ccaf8df2cdd54b79cb75ca188ae9c38627e . Lần kiểm tra main ngày 29/10/2026 không có thay đổi mã ứng dụng sau phiên bản này.

[3] Client: client/src/com/billiards/client/NetworkClient.java; scenes/{SceneManager, ServerConfigDialog, LoginScene, RegisterScene, LobbyScene, PreGameScene, GameScene, SettingsDialog, ResultScene, LeaderboardScene}.java; game/{Ball, PhysicsEngine, CueSkinManager}.java; AvatarManager.java; audio/SoundManager.java.

[4] Server: server/src/com/billiards/server/{ServerMain, ClientHandler, GameRoom, LobbyManager, AuthManager, DatabaseManager}.java; protocol/{Packet, PacketType, CueSkinConfig}.java; database/schema.sql.

[5] Đặc tả chức năng: reports/uml_spec.json. Biểu đồ dùng trong báo cáo: reports/uml_unified/manifest.json và các nguồn PlantUML, PNG, SVG cùng thư mục. Kết xuất bằng PlantUML 1.2025.2.

[6] Minh chứng thực thi: reports/evidence/00bf1cc/README.md cùng nhật ký và script ngày 26/10/2026. Bảy ảnh JavaFX của đợt kiểm tra này được dùng làm minh họa ở Chương 5, có ghi rõ điều kiện chụp và phạm vi áp dụng.

[7] Oracle. Java SE 17 API: Socket, ServerSocket, DataInputStream, DataOutputStream. Tài liệu nền về kết nối và đọc/ghi dữ liệu: https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/net/Socket.html và https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/io/DataInputStream.html .

[8] OpenJFX. JavaFX 17 API: Platform và AnimationTimer. https://openjfx.io/javadoc/17/javafx.graphics/javafx/application/Platform.html ; https://openjfx.io/javadoc/17/javafx.graphics/javafx/animation/AnimationTimer.html .

[9] PostgreSQL Global Development Group. PostgreSQL 17 Documentation: Transactions, Constraints và Indexes. https://www.postgresql.org/docs/17/tutorial-transactions.html ; https://www.postgresql.org/docs/17/ddl-constraints.html ; https://www.postgresql.org/docs/17/indexes.html .

[10] PostgreSQL JDBC Driver. Tài liệu JDBC và thao tác dữ liệu: https://jdbc.postgresql.org/documentation/ .

[11] PlantUML. Tài liệu Use Case, Activity, Sequence và Component: https://plantuml.com/use-case-diagram ; https://plantuml.com/activity-diagram-beta ; https://plantuml.com/sequence-diagram ; https://plantuml.com/component-diagram . Các tài liệu công nghệ dùng làm nguồn kiến thức nền; hiện trạng dự án được xác định từ mã nguồn [2–4] và minh chứng [6].
