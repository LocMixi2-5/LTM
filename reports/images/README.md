# Hình báo cáo — nguồn 00bf1cc

Báo cáo nhúng 11 hình: 4 sơ đồ kỹ thuật và 7 ảnh chụp JavaFX của mã nguồn `00bf1cc`, ngày 26/10/2026. Không dùng ảnh v2 cũ để mô tả gameplay hiện tại.

## Sơ đồ

- `architecture`: client–server, TCP/UDP, JDBC/PostgreSQL.
- `shot_flow`: cú đánh và PHYSICS_DONE của nhánh hiện tại; không có snapshot/ACK v2.
- `challenge_flow`: lời mời, phản hồi, tạo phòng hoặc thất bại; client 10 giây, server 15 giây.
- `database_erd`: bốn bảng, các khóa tham chiếu players.

Nguồn `.dot` dựng bằng Graphviz; PNG ở 180 dpi để chèn tài liệu; SVG là bản vector để phóng to. Ví dụ tái tạo: `dot -Tpng -Gdpi=180 challenge_flow.dot -o challenge_flow.png`.

## Ảnh chụp

- `registration_actual.png`: form đăng ký.
- `friend_menu_actual.png`: menu bạn bè.
- `chat_actual.png`: nội dung chat mẫu.
- `challenge_actual.png`: lời mời nổi trên cửa sổ chủ.
- `challenge_toast.png`: kết quả lời mời bằng toast.
- `practice_actual.png`: solo không có khung đối thủ trống.
- `game_actual.png`: GameScene nhận CUE_AIM qua callback có kiểm soát.

Tất cả là màn hình JavaFX thật chạy trên Linux/Xvfb. Tài khoản/nội dung là dữ liệu mẫu; transport giả lập nên ảnh không chứng minh luồng mạng đầu cuối hoặc khả năng chạy Windows. Không chỉnh sửa nội dung giao diện sau chụp. Ping/FPS trong ảnh GameScene không được dùng làm benchmark.

Hai ảnh phụ `lobby_actual.png` và `registration_validation.png` được tạo trong bài kiểm tra, không nhúng thêm vào báo cáo để tránh lặp bố cục.
