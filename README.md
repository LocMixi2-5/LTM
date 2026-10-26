# LTM - 8-Ball Pool Multiplayer Game

> 📄 **Báo cáo cập nhật theo `00bf1cc` (26/10/2026):** [Word](reports/Bao_cao_LTM_4_phan.docx) · [PDF có hình](reports/Bao_cao_LTM_4_phan.pdf) · [Đọc trên GitHub](reports/Bao_cao_LTM_4_phan.md). Chia đúng 4 phần phân công; mô tả giao thức và kết quả kiểm tra của main hiện tại.

> **Bài tập lớn môn Lập Trình Mạng (LTM)**  
> Trò chơi Bi-a 8-Ball Pool chơi nhiều người qua mạng thời gian thực (Real-time Multiplayer) xây dựng trên nền tảng **Java & JavaFX** kết nối qua **TCP Socket** và cơ sở dữ liệu **PostgreSQL**.

---

## 📌 1. Giới Thiệu Tổng Quan

Dự án mô phỏng trò chơi bi-a 8 lỗ (8-Ball Pool) tiêu chuẩn quốc tế với đầy đủ các tính năng đối kháng 2 người chơi qua mạng:
- **Client (JavaFX 17+):** Giao diện đồ họa hiện đại với chủ đề *Modern Billiards Club*, nẹp góc bạc, động cơ kết xuất Canvas 2D mượt mà, hiệu ứng môi trường sống động, âm thanh chân thực (BGM, tiếng va chạm bi, tiếng cơ, tiếng bóng rơi lỗ).
- **Server (Java Socket TCP):** Kiến trúc máy chủ đa luồng (Multi-threaded TCP Server), điều phối phòng đấu (GameRoom), quản lý hàng đợi ghép trận (Matchmaking), kết bạn, chat thời gian thực và quản lý phiên người chơi.
- **Động cơ Vật lý 2D (Deterministic Physics):** Tính toán va chạm đàn hồi 2 chiều, phản xạ băng, kỹ thuật đánh nâng cao (Trô bi - Backspin, Cù-lê - Topspin, Ép-phê - Sidespin).
- **Cơ sở dữ liệu (PostgreSQL):** Lưu trữ thông tin tài khoản, mật khẩu băm bảo mật SHA-256 + Salt, EXP, Level, lịch sử đấu, quan hệ bạn bè và tin nhắn trực tiếp.

---

## 📂 2. Cấu Trúc Thư Mục Dự Án

```text
billiards-game/
├── README.md                                  # Tài liệu tổng quan & hướng dẫn sử dụng
├── HANDOFF.md                                 # Tài liệu kỹ thuật chi tiết kiến trúc & mã packet
├── Bao_cao_Do_an_Lap_trinh_mang_8BallPool.docx # Báo cáo hoàn chỉnh đề tài BTL LTM
├── run_server.bat                             # Script chạy nhanh Server
├── run_client.bat                             # Script chạy nhanh Client
├── server_ip.txt                              # Cấu hình IP máy chủ cho Client kết nối
│
├── database/                                  # CƠ SỞ DỮ LIỆU
│   ├── README.md                              # Mô tả chi tiết thiết kế CSDL (ERD, bảng, khóa, index)
│   └── schema.sql                             # Script SQL khởi tạo bảng, ràng buộc & index PostgreSQL
│
├── server/                                    # MÃ NGUỒN SERVER
│   ├── compile.bat                            # Script biên dịch Java Server
│   ├── run_server.bat                         # Khởi chạy TCP Server trên Port 8888
│   ├── BilliardsServer.jar                    # File JAR Server đã đóng gói
│   ├── lib/                                   # Thư viện phụ thuộc (postgresql-42.7.3.jar)
│   └── src/com/billiards/server/
│       ├── ServerMain.java                    # Điểm khởi chạy Server Socket (Port 8888)
│       ├── ClientHandler.java                 # Quản lý I/O luồng TCP riêng cho từng Client
│       ├── GameRoom.java                      # Điều phối phòng đấu 2 người, luật 8-ball & lượt chơi
│       ├── LobbyManager.java                  # Hàng đợi ghép trận (Matchmaking Queue)
│       ├── AuthManager.java                   # Xác thực đăng nhập, băm mật khẩu SHA-256 + Salt
│       ├── DatabaseManager.java               # DAO kết nối và thao tác với PostgreSQL
│       └── protocol/                          # Giao thức Packet TCP máy chủ
│
└── client/                                    # MÃ NGUỒN CLIENT
    ├── compile.bat                            # Script biên dịch Client với JavaFX
    ├── run_client.bat                         # Khởi chạy giao diện game Client
    ├── BilliardsClient.jar                    # File JAR Client đã đóng gói
    ├── lib/                                   # Thư mục chứa JavaFX SDK (xem client/lib/README.md)
    ├── assets/                                # Tài nguyên âm thanh (BGM, sound effects)
    └── src/com/billiards/client/
        ├── ClientMain.java                    # Điểm vào JavaFX Client
        ├── NetworkClient.java                 # Kết nối TCP Client, lắng nghe và phân phối Packet
        ├── AvatarManager.java                 # Quản lý danh mục Avatar & tùy biến gậy cơ
        ├── game/                              # Động cơ vật lý & mô hình bóng (PhysicsEngine, Ball, CueSkin)
        ├── scenes/                            # Các màn hình (Login, Lobby, Matchmaking, PreGame, Game, Leaderboard...)
        ├── theme/                             # Hệ thống giao diện Modern Billiards Club
        ├── audio/                             # Quản lý phát nhạc & hiệu ứng âm thanh
        ├── vfx/                               # Hiệu ứng nền mây rồng & thiên nhiên
        └── protocol/                          # Giao thức Packet TCP máy trạm
```

---

## 🗄️ 3. Mô Tả Cơ Sở Dữ Liệu (PostgreSQL)

Chi tiết đầy đủ xem tại file [database/README.md](database/README.md).

- **`players`**: Quản lý tài khoản (id, email, username, password_hash, exp, level, wins, losses, avatar_id, cue_color, created_at).
- **`match_history`**: Lưu kết quả các trận đấu PvP (id, winner_id, loser_id, exp_gained, played_at).
- **`friendships`**: Quản lý mối quan hệ và yêu cầu kết bạn (user_id, friend_id, status: PENDING/ACCEPTED/BLOCKED, created_at).
- **`messages`**: Tin nhắn trò chuyện trực tiếp (id, sender_id, receiver_id, content, is_read, sent_at).
- **Indexes**: `idx_players_exp` hỗ trợ lấy Top Bảng xếp hạng siêu nhanh và `idx_players_username` tối ưu tìm kiếm bạn bè / đăng nhập.

Khởi tạo cơ sở dữ liệu:
```bash
psql -U postgres -f database/schema.sql
```

---

## 🌐 4. Đặc Tả Giao Thức Mạng (TCP Binary Protocol)

Hệ thống giao tiếp qua Socket TCP với cấu trúc nhị phân tối ưu hóa độ trễ:
```text
[1 byte: PacketType Code] [Dữ liệu nhị phân (Binary Payload)]
```
Hỗ trợ 45 mã gói tin bao gồm:
- **Xác thực:** `LOGIN_REQUEST`, `LOGIN_RESPONSE`, `REGISTER_REQUEST`, `REGISTER_RESPONSE`.
- **Ghép trận & Phòng đấu:** `FIND_MATCH`, `MATCH_FOUND`, `CANCEL_FIND`, `PREGAME_READY`, `GAME_START`.
- **Đồng bộ trận đấu:** `CUE_SHOT`, `TURN_CHANGE`, `BALL_POCKETED`, `FOUL`, `GAME_OVER`, `LEAVE_GAME`.
- **Xã hội & Bạn bè:** `FRIEND_LIST`, `FRIEND_REQUEST`, `FRIEND_ACCEPT`, `CHAT_DIRECT`, `LEADERBOARD`.

Chi tiết toàn bộ 45 mã byte packet được đặc tả tại [HANDOFF.md](HANDOFF.md).

---

## 🚀 5. Hướng Dẫn Cài Đặt & Chạy Game

### Yêu Cầu Môi Trường
- **JDK 17+** (Khuyên dùng Eclipse Temurin 17 / Adoptium).
- **PostgreSQL 14+** chạy cổng `5432`.
- **JavaFX SDK 17+** (đặt vào `client/lib/javafx-sdk-17.0.13/`).

### Bước 1: Thiết Lập Cơ Sở Dữ Liệu
Tạo cơ sở dữ liệu `billiards_db` và khởi chạy script schema:
```bash
psql -U postgres -c "CREATE DATABASE billiards_db;"
psql -U postgres -d billiards_db -f database/schema.sql
```
*(Nếu mật khẩu postgres khác `123456`, cập nhật tại `server/src/com/billiards/server/DatabaseManager.java`)*.

### Bước 2: Chạy Server
Mở terminal tại thư mục gốc và chạy:
```cmd
run_server.bat
```
*(Hoặc vào `server/` chạy `compile.bat` để build và `run_server.bat` để chạy).*

### Bước 3: Chạy Client
Chạy client trên cùng máy hoặc máy cùng mạng LAN:
- Nếu chạy khác máy trong LAN: Nhập IP của máy chạy Server vào file `server_ip.txt` (ví dụ `192.168.1.15`).
- Khởi động Client:
```cmd
run_client.bat
```
*(Hoặc vào `client/` chạy `compile.bat` và `run_client.bat`).*
