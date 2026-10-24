# PROJECT HANDOFF: 8-Ball Pool Multiplayer (v2.0)

> **Mục đích file này:** AI hoặc lập trình viên đọc file này là nắm rõ toàn bộ cấu trúc dự án, kiến trúc mạng TCP, động cơ vật lý 2D, các tính năng đã hoàn thiện và quy trình vận hành.
> **Thư mục dự án:** `d:\Nam 4\Ki 1\LTM\BTL LTM\billiards-game\`
> **Cập nhật lần cuối:** 2026-10-07

---

## 1. TỔNG QUAN DỰ ÁN

**Tên dự án:** 8-Ball Pool Multiplayer (BTL Lập Trình Mạng)

| Thành phần | Công nghệ |
|---|---|
| **Client** | Java 17+ & JavaFX 17+ (Canvas 2D, Particle System, SoundManager) |
| **Server** | Java 17+ (Multi-threaded TCP Socket Server, 1 thread / client) |
| **Database** | SQLite (File cục bộ `billiards.db`, tự động migrate schema) |
| **Giao tiếp Mạng** | TCP Binary Protocol tùy chỉnh (`DataInputStream` / `DataOutputStream`) |
| **Động cơ Vật lý** | 2D Physics Engine tất định (Deterministic Elastic Collision, Cushion Bounce, Draw Shot / Trô bi, Cule, Ép-phê) |

---

## 2. CẤU TRÚC FILE CHI TIẾT

```text
billiards-game/
├── HANDOFF.md                       ← Tài liệu bàn giao dự án
├── client/
│   ├── build_no_pause.bat           ✅ Script biên dịch & đóng gói BilliardsClient.jar (không pause)
│   ├── compile.bat                  ✅ Script biên dịch JavaFX Client
│   ├── run_client.bat               ✅ Script chạy client JAR
│   ├── lib/                         ✅ JavaFX SDK 17 & SQLite JDBC
│   ├── assets/
│   │   └── sounds/                  ✅ bgm.mp3, bgm2.mp3, collision.wav, cue_hit.mp3, pocket.wav, victory.wav, foul.wav
│   └── src/com/billiards/client/
│       ├── ClientMain.java          ✅ Khởi động JavaFX, thiết lập giao diện chính
│       ├── NetworkClient.java       ✅ Quản lý Socket TCP Client, luồng đọc gói tin & PlayerSession
│       ├── protocol/
│       │   ├── Packet.java          ✅ Helper đọc/ghi kiểu dữ liệu binary chuẩn qua DataStream
│       │   └── PacketType.java      ✅ Định nghĩa 45 mã byte gói tin TCP (Gameplay, Friends, Chat, Challenge, Auth)
│       ├── game/
│       │   ├── Ball.java            ✅ Model viên bi (tọa độ, vận tốc, spinX, spinY, hoạt ảnh rơi lỗ 3D)
│       │   └── PhysicsEngine.java   ✅ Động cơ vật lý 2D va chạm đàn hồi, nảy băng, Trô bi (Draw shot), Cule, Ép-phê
│       ├── theme/
│       │   ├── Theme.java           ✅ Hệ thống thiết kế Modern Billiards Club: hình vuông (radius: 0), bảng màu chuẩn & tokens
│       │   └── SilverCornerPane.java ✅ Component bọc viền hình vuông nẹp bạc 4 góc (Silver corner accents & đinh tán bạc)
│       ├── vfx/
│       │   ├── NatureAmbience.java   ✅ VFX cành lá đung đưa mượt mà & bọ côn trùng cử động chân/râu bò tự nhiên (Lobby & In-game)
│       │   ├── DragonCloudBackground.java ✅ Hiệu ứng mây bay & rồng lượn kết hợp cành lá và bọ bò phong cách sang trọng
│       │   └── AvatarManager.java   ✅ Quản lý 16 avatar tạo sẵn, tải ảnh máy tính, màu gậy cơ
│       ├── audio/
│       │   └── SoundManager.java    ✅ Quản lý phát nhạc nền đa bài (BGM), SFX âm thanh va chạm, còi lượt, âm lượng
│       └── scenes/
│           ├── SceneManager.java    ✅ Quản lý chuyển đổi mượt mà giữa các Scene
│           ├── LoginScene.java      ✅ Đăng nhập & Đăng ký tài khoản
│           ├── LobbyScene.java      ✅ Sảnh chính (Tìm trận, Danh sách bạn bè, Chat realtime, Thách đấu, Cài đặt)
│           ├── PreGameScene.java    ✅ Màn hình đếm ngược 13s trước trận (Avatar 2 đối thủ)
│           ├── GameScene.java       ✅ Gameplay bi-a chính (Render bàn 3D luxury, kéo cơ, chỉnh Trô bi, đồng bộ mạng)
│           ├── LeaderboardScene.java✅ Bảng xếp hạng Top người chơi theo EXP / Cấp độ / Trận thắng
│           ├── ResultScene.java     ✅ Màn hình kết quả trận đấu Thắng / Thua & EXP
│           ├── SettingsDialog.java  ✅ Hộp thoại đổi Avatar, màu gậy, chỉnh âm lượng BGM/SFX
│           ├── FriendSearchDialog.java ✅ Hộp thoại tìm kiếm & gửi lời mời kết bạn
│           └── PendingRequestsDialog.java ✅ Quản lý lời mời kết bạn đang chờ
│
└── server/
    ├── build_no_pause.bat           ✅ Script biên dịch & đóng gói BilliardsServer.jar (không pause)
    ├── compile.bat                  ✅ Script biên dịch Server
    ├── run_server.bat               ✅ Script khởi chạy TCP Server (Port 8888)
    ├── billiards.db                 ✅ File cơ sở dữ liệu SQLite
    ├── lib/                         ✅ sqlite-jdbc-3.45.1.0.jar, slf4j-api-1.7.36.jar
    └── src/com/billiards/server/
        ├── ServerMain.java          ✅ Lắng nghe kết nối TCP trên Port 8888, quản lý danh sách online
        ├── ClientHandler.java       ✅ Luồng I/O riêng cho từng kết nối Client (Xử lý toàn bộ 45 packet types)
        ├── GameRoom.java            ✅ Phòng đấu 2 người: điều phối lượt, chuyển tiếp cú đánh (CUE_SHOT), xử lý foul, end game
        ├── LobbyManager.java        ✅ Quản lý hàng đợi tìm trận tự động & tạo phòng đấu
        ├── AuthManager.java         ✅ Băm mật khẩu an toàn SHA-256 + Salt
        ├── DatabaseManager.java     ✅ SQLite CRUD: users, match_history, friends, friend_requests, chat_messages
        └── protocol/
            ├── Packet.java          ✅ Helper đọc/ghi binary
            └── PacketType.java      ✅ Định nghĩa gói tin phía server
```

---

## 3. ĐẶC TẢ GIAO THỨC TCP (45 PACKET TYPES)

Định dạng gói tin: `[1 byte: PacketType Code] [Payload tùy loại]`

| Code | PacketType | Hướng | Payload chi tiết | Mục đích |
|---|---|---|---|---|
| **1** | `LOGIN_REQUEST` | C ➔ S | `String username`, `String password` | Đăng nhập tài khoản |
| **2** | `LOGIN_RESPONSE` | S ➔ C | `bool success`, `String username`, `int exp`, `int level`, `int wins`, `int losses`, `int avatarId`, `String cueColor` | Kết quả đăng nhập + thông tin hồ sơ |
| **3** | `REGISTER_REQUEST` | C ➔ S | `String email`, `String username`, `String password` | Đăng ký tài khoản mới |
| **4** | `REGISTER_RESPONSE` | S ➔ C | `bool success`, `String message` | Kết quả đăng ký |
| **5** | `FIND_MATCH` | C ➔ S | *(trống)* | Vào hàng đợi tìm trận |
| **6** | `MATCH_FOUND` | S ➔ C | `String oppName`, `int oppLevel`, `int oppAvatarId`, `String oppCueColor` | Ghép trận thành công |
| **7** | `CANCEL_FIND` | C ➔ S | *(trống)* | Rời hàng đợi tìm trận |
| **8** | `PREGAME_READY` | C ➔ S | *(trống)* | Báo sẵn sàng trong phòng chờ |
| **9** | `GAME_START` | S ➔ C | `int playerIndex (0/1)`, `bool isFirstTurn`, `long matchSeed` | Bắt đầu trận đấu (kèm matchSeed để cả 2 client xáo ngẫu nhiên bi đồng bộ) |
| **10** | `CUE_SHOT` | C ➔ S | `double angle`, `double force`, `double cueX`, `double cueY`, `double spinX`, `double spinY` | Người chơi thực hiện cú đánh |
| **11** | `GAME_STATE` | S ➔ C | `double angle`, `double force`, `double cueX`, `double cueY`, `double spinX`, `double spinY` | Server chuyển tiếp cú đánh tới đối thủ |
| **12** | `TURN_CHANGE` | S ➔ C | `bool isYourTurn`, `bool isBallInHand`, `String reason` | Thay đổi lượt đánh & cấp quyền Ball-in-Hand nếu có lỗi |
| **13** | `BALL_POCKETED` | S ➔ C | `int ballIndex` | Thông báo bi rơi vào lỗ |
| **14** | `GAME_OVER` | S ➔ C | `bool youWin`, `int expGained` | Kết thúc trận & cộng điểm EXP |
| **15** | `LEADERBOARD_REQUEST` | C ➔ S | *(trống)* | Yêu cầu bảng xếp hạng |
| **16** | `LEADERBOARD_RESPONSE` | S ➔ C | `String "rank\|user\|level\|exp\|wins\|losses;..."` | Dữ liệu BXH |
| **20** | `PHYSICS_DONE` | C ➔ S | `bool keepTurn`, `bool isFoul` | Người vừa đánh báo vật lý đã dừng hoàn toàn |
| **21** | `CLAIM_WIN` | C ➔ S | `bool isWin` | Báo ăn bi 8 thắng hoặc lỗi bi 8 thua |
| **22** | `SETTINGS_UPDATE` | C ➔ S | `int avatarId`, `String cueColor` | Lưu Avatar & màu gậy |
| **23** | `SETTINGS_RESPONSE` | S ➔ C | `bool success` | Kết quả cập nhật cài đặt |
| **24-29** | `SEARCH / ADD / FRIEND_LIST` | C ⇄ S | Các gói quản lý bạn bè | Tìm bạn bè, thêm bạn bè, lấy danh sách bạn |
| **30-37** | `ACCEPT / DECLINE / PENDING / REMOVE` | C ⇄ S | Các gói lời mời kết bạn | Đồng ý, từ chối, xóa bạn bè |
| **38-41** | `CHAT_MESSAGE / CHAT_HISTORY` | C ⇄ S | `String recipient/sender`, `String message` | Nhắn tin riêng thời gian thực & lịch sử chat |
| **42-45** | `CHALLENGE_REQUEST / RECEIVE / RESPONSE / RESULT` | C ⇄ S | `String challenger`, `bool accept`, `bool success` | Thách đấu 1v1 trực tiếp từ danh sách bạn bè |
| **46** | `CUE_BALL_PLACED` | C ➔ S ➔ C | `double cueX`, `double cueY` | Gửi tọa độ bi cái vừa đặt (khi thả bi cái lúc phá bi hoặc sau lỗi) để đối thủ thấy trước khi bắn |

---

## 4. LUỒNG GAMEPLAY & CƠ CHẾ VẬT LÝ V2.0

### A. Cơ Chế Chọc Cơ & Trô Bi (Draw Shot / English)
- **Điểm đặt cơ (Spin Selector Panel)**:
  - Bảng điều khiển xoáy bi chuyên dụng ở cột HUD bên phải (không còn đè lên lỗ góc dưới-phải của bàn chơi).
  - Cửa sổ GameScene mở rộng lên `1000 x 650` (đồng bộ với LobbyScene), mặt bàn mở rộng lên `760 x 424`, 6 lỗ hoàn toàn thông thoáng.
  - Hỗ trợ nút click nhanh "Đặt lại tâm" hoặc nhấp chuột phải trên bảng để reset xoáy về tâm bi `(0, 0)`.
  - `spinY > 0` (**Trô bi / Draw shot / Backspin**): Tác động lực xoáy ngược khi va chạm bi mục tiêu, kéo bi cái lùi về phía sau.
  - `spinY < 0` (**Cule / Follow shot / Topspin**): Giữ động lượng lăn tới trước sau va chạm.
  - `spinX` (**Ép-phê / Side spin**): Tác động thay đổi góc nảy khi đập vào băng cao su.
- **Tiêu hao ma sát**: Độ xoáy suy giảm tự nhiên theo thời gian khi bi lăn trên mặt nỉ.

### B. Luồng Đồng Bộ Không Bị Desync / Không Bắn Lặp
```text
1. Người A (có lượt) kéo cơ & thả chuột:
   - Máy A: Ngay lập tức gọi applyCueShot(angle, force, spinX, spinY, true) → Bi cái lăn ngay.
   - Máy A: Gửi CUE_SHOT(angle, force, cueX, cueY, spinX, spinY) lên Server.
   - Máy A: Đặt cờ iWasShooterThisTurn = true.

2. Server (GameRoom):
   - Nhận CUE_SHOT từ A.
   - CHỈ gửi GAME_STATE tới B (đối thủ của A). KHÔNG gửi ngược lại cho A (tránh lỗi bắn kép).

3. Máy B (đối thủ):
   - Nhận GAME_STATE.
   - Do iWasShooterThisTurn == false, máy B đặt vị trí bi cái và gọi applyCueShot(...) đồng bộ.

4. Vật lý kết thúc (tất cả bi dừng hẳn):
   - Chỉ người bắn (A) có cờ iWasShooterThisTurn == true mới gửi PHYSICS_DONE(keepTurn, isFoul).
   - Máy B kết thúc vật lý mà không gửi PHYSICS_DONE thừa.

5. Server xử lý chuyển lượt:
   - Nếu keepTurn == true: Báo A đánh tiếp.
   - Nếu keepTurn == false: Server switchTurn() và gửi TURN_CHANGE(isYourTurn, isBallInHand, reason) cho cả 2.
   - Chỉ khi isFoul == true (bi trắng vào lỗ, không chạm bi, hết giờ 60s) người chơi mới được Ball-in-Hand.
```
### C. Nút Đăng Xuất & Đổi IP Máy Chủ Mạng Nội Bộ (LAN)
1. **Nút "🚪 Đăng xuất" tại màn hình chính (`LobbyScene.java`)**:
   - Nằm ở góc trên bên phải của Top Bar bên cạnh nút *⚙ Cài đặt*.
   - Phong cách viền đỏ nổi bật `#ef4444`.
   - Có hộp thoại xác nhận tránh bấm nhầm; khi đồng ý sẽ dọn dẹp phiên làm việc (`network.disconnect(); network.connect();`) và quay trở lại màn hình đăng nhập (`sceneManager.showLogin()`).
2. **Cấu hình & Tự đổi IP mạng nội bộ lúc chạy (`NetworkClient.java`, `ServerConfigDialog.java`, `LoginScene.java`)**:
   - **Tự động lưu/đọc IP**: Đọc và ghi IP máy chủ vào file `server_ip.txt`. Không cần sửa `.bat` mỗi khi IP đổi.
   - **Khởi động an toàn**: Không còn tình trạng client bị crash/exit nếu Server chưa bật lúc mở. Thay vào đó, client hiển thị màn hình Login và tự động mở hộp thoại cấu hình IP nếu chưa kết nối.
   - **Thanh trạng thái Server tại Login**: Hiển thị rõ `🟢 Server: IP:PORT (Đã kết nối)` hoặc `🔴 Chưa kết nối` cùng nút `🌐 Đổi IP Server`.
   - **Hộp thoại Cấu hình IP (`ServerConfigDialog.java`)**:
     - Hiển thị rõ ràng `💻 IP máy này` (Local LAN IP) kèm nút sao chép nhanh để tiện chia sẻ cho bạn bè kết nối.
     - Nút `🏠 Localhost` (127.0.0.1) và `💻 Dùng IP máy này`.
     - Nút `🔍 Dò Server LAN`: Tự động gửi gói UDP Broadcast (cổng 12346) và quét subnet TCP (cổng 12345) để tự động phát hiện IP Server trong mạng Wi-Fi/LAN mà không cần gõ tay.
     - Nút `🔗 KẾT NỐI & LƯU`: Thử kết nối trực tiếp với timeout 2.5s, báo kết quả tức thì và lưu file cấu hình.

---

## 5. HƯỚNG DẪN BUILD VÀ KHỞI CHẠY

### 1. Build & Chạy Server
```cmd
cd server
build_no_pause.bat
run_server.bat
```
*(Server tự động lắng nghe TCP 12345, UDP 12346 và in ra địa chỉ IP LAN của máy để các máy khác kết nối).*

### 2. Build & Chạy Client
```cmd
cd client
build_no_pause.bat
run_client.bat
```
*(Có thể chạy `run_client.bat` trực tiếp, hoặc truyền IP dòng lệnh `run_client.bat 192.168.1.15`. Nếu không truyền IP, client tự động dùng IP đã lưu trong `server_ip.txt` hoặc dò tìm trong GUI).*

---

## 6. LƯU Ý KỸ THUẬT CHO PHIÊN LÀM VIỆC TIẾP THEO
1. **Không sửa đổi tính tất định của vật lý**: Các hàm toán học trong `PhysicsEngine.java` không được dùng biến ngẫu nhiên trong vòng lặp va chạm.
2. **Gói tin mở rộng**: Nếu bổ sung dữ liệu mới vào `CUE_SHOT` hoặc `GAME_STATE`, luôn đảm bảo cập nhật đồng bộ cả 4 file: `PacketType.java` (server + client), `ClientHandler.java`, `NetworkClient.java`, và `GameRoom.java`.
3. **Môi trường Database**: Server sử dụng SQLite cục bộ `billiards.db`, không cần cài đặt thêm dịch vụ database bên ngoài.
4. **Cấu hình mạng LAN**: Khi phát hành máy khách mới, danh sách lớp trong `build_no_pause.bat`, `compile.bat`, và `package_client.bat` đã bao gồm `ServerConfigDialog.java`.
