# Mô Tả Cơ Sở Dữ Liệu (Database Specification) - 8-Ball Pool Multiplayer

Dự án sử dụng hệ quản trị cơ sở dữ liệu **PostgreSQL** để lưu trữ thông tin người chơi, kết quả trận đấu, quan hệ bạn bè và tin nhắn trực tiếp giữa các người chơi.

---

## 1. Thông Tin Kết Nối (Mặc Định)
- **Hệ quản trị CSDL:** PostgreSQL 14+
- **Database Name:** `billiards_db`
- **Host / Port:** `localhost:5432`
- **User:** `postgres`
- **Password:** `123456` (có thể cấu hình lại trong file `server/src/com/billiards/server/DatabaseManager.java`)
- **JDBC Driver:** `org.postgresql.Driver` (đã có sẵn trong `server/lib/postgresql-42.7.3.jar`)

---

## 2. Sơ Đồ Thực Thể & Quan Hệ (ERD)

```text
       +-----------------------+
       |        PLAYERS        |
       +-----------------------+
       | PK id                 |<-------------+
       |    email              |              |
       |    username           |              |
       |    password_hash      |              |
       |    exp                |              |
       |    level              |              |
       |    wins               |              |
       |    losses             |              |
       |    avatar_id          |              |
       |    cue_color          |              |
       |    created_at         |              |
       +-----------------------+              |
          ^       ^         ^                 |
          |       |         |                 |
(winner)  |       | (loser) | (user / friend) | (sender / receiver)
          |       |         |                 |
  +-------+-------+---+   +-+-----------------+-+   +-------------------+
  |   MATCH_HISTORY   |   |     FRIENDSHIPS     |   |     MESSAGES      |
  +-------------------+   +---------------------+   +-------------------+
  | PK id             |   | PK,FK user_id       |   | PK id             |
  | FK winner_id      |   | PK,FK friend_id     |   | FK sender_id      |
  | FK loser_id       |   |       status        |   | FK receiver_id    |
  |    exp_gained     |   |       created_at    |   |    content        |
  |    played_at      |   +---------------------+   |    is_read        |
  +-------------------+                             |    sent_at        |
                                                    +-------------------+
```

---

## 3. Chi Tiết Các Bảng (Tables)

### 3.1. Bảng `players` (Thông tin người chơi)
Lưu trữ thông tin tài khoản, chỉ số thi đấu và tùy biến người dùng.

| Tên trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---|---|---|---|
| `id` | `SERIAL` | `PRIMARY KEY` | Khóa chính, mã định danh người chơi |
| `email` | `VARCHAR(100)` | `UNIQUE`, `NOT NULL` | Email đăng ký tài khoản |
| `username` | `VARCHAR(50)` | `UNIQUE`, `NOT NULL` | Tên đăng nhập và hiển thị |
| `password_hash` | `VARCHAR(255)` | `NOT NULL` | Mật khẩu đã băm (SHA-256 + Salt) |
| `exp` | `INT` | `DEFAULT 0` | Điểm kinh nghiệm tích lũy |
| `level` | `INT` | `DEFAULT 1` | Cấp bậc hiện tại (Level = exp / 200 + 1) |
| `wins` | `INT` | `DEFAULT 0` | Tổng số trận thắng |
| `losses` | `INT` | `DEFAULT 0` | Tổng số trận thua |
| `avatar_id` | `INT` | `DEFAULT 0` | Mã avatar (0-15 cho avatar mặc định, >0 cho custom) |
| `cue_color` | `VARCHAR(20)` | `DEFAULT '#DEB887'` | Mã màu HEX của gậy cơ hiển thị trong trận |
| `created_at` | `TIMESTAMP` | `DEFAULT NOW()` | Thời điểm tạo tài khoản |

---

### 3.2. Bảng `match_history` (Lịch sử trận đấu)
Lưu kết quả chi tiết các trận đấu đối kháng PvP.

| Tên trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---|---|---|---|
| `id` | `SERIAL` | `PRIMARY KEY` | Mã bản ghi trận đấu |
| `winner_id` | `INT` | `REFERENCES players(id)` | Khóa ngoại người chiến thắng |
| `loser_id` | `INT` | `REFERENCES players(id)` | Khóa ngoại người thua cuộc |
| `exp_gained` | `INT` | `DEFAULT 100` | Số EXP trao thưởng cho người thắng |
| `played_at` | `TIMESTAMP` | `DEFAULT NOW()` | Thời gian diễn ra trận đấu |

---

### 3.3. Bảng `friendships` (Danh sách bạn bè & kết bạn)
Quản lý tương tác xã hội giữa các người chơi.

| Tên trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---|---|---|---|
| `user_id` | `INT` | `REFERENCES players(id)` | ID người chơi gửi yêu cầu |
| `friend_id` | `INT` | `REFERENCES players(id)` | ID người chơi nhận yêu cầu |
| `status` | `VARCHAR(20)` | `DEFAULT 'PENDING'` | Trạng thái: `PENDING`, `ACCEPTED`, `BLOCKED` |
| `created_at` | `TIMESTAMP` | `DEFAULT NOW()` | Thời gian tạo mối quan hệ |
| *(Khóa chính)* | `(user_id, friend_id)` | `PRIMARY KEY` | Bộ đôi khóa chính chống trùng lặp |

---

### 3.4. Bảng `messages` (Tin nhắn trực tiếp)
Lưu trữ tin nhắn văn bản giữa 2 người chơi.

| Tên trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---|---|---|---|
| `id` | `SERIAL` | `PRIMARY KEY` | Khóa chính tin nhắn |
| `sender_id` | `INT` | `REFERENCES players(id)` | ID người gửi tin nhắn |
| `receiver_id` | `INT` | `REFERENCES players(id)` | ID người nhận tin nhắn |
| `content` | `TEXT` | `NOT NULL` | Nội dung tin nhắn chat |
| `is_read` | `BOOLEAN` | `DEFAULT FALSE` | Trạng thái đã xem hay chưa |
| `sent_at` | `TIMESTAMP` | `DEFAULT NOW()` | Thời điểm gửi tin nhắn |

---

## 4. Các Chỉ Mục Tối Ưu Hóa (Indexes)
- **`idx_players_exp`**: `CREATE INDEX idx_players_exp ON players(exp DESC);`  
  *Mục đích:* Tối ưu hóa truy vấn Top Leaderboard (bảng xếp hạng) với độ phức tạp `O(log N)`.
- **`idx_players_username`**: `CREATE INDEX idx_players_username ON players(username);`  
  *Mục đích:* Tăng tốc tìm kiếm bạn bè theo tên và xác thực đăng nhập.

---

## 5. Hướng Dẫn Khởi Tạo Database
1. Cài đặt PostgreSQL (nếu chưa có).
2. Chạy file SQL bằng lệnh:
   ```bash
   psql -U postgres -f database/schema.sql
   ```
   Hoặc mở file `database/schema.sql` trong pgAdmin và chạy toàn bộ mã lệnh.
