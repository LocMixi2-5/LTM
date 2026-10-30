# Đối chiếu và kiểm tra mã 89f7926 — 30/10/2026

Mã ứng dụng: `89f7926c13345bcd21fbaccaf7d8e13fe3fdb562`. Không sửa client/server trong lần đối chiếu báo cáo.

## Các lần thực thi

- `build.txt`: biên dịch sạch 9 file server và 26 file client; JDK 21, `--release 17`, JavaFX Linux. Không chạy EXE Windows.
- `ReportServer.java`: dùng nguyên DatabaseManager/AuthManager/LobbyManager/ClientHandler của ứng dụng, lắng nghe riêng trên loopback TCP 12355. Không chạy lại ServerMain/UDP discovery. DB PostgreSQL 17 phát triển cục bộ đã chuẩn bị từ trước.
- `protocol.txt`: `check_current.py` hoàn tất mã 0. Kiểm tra đăng nhập/email, lời mời, READY theo người chơi, 60 mẫu ngắm, các giá trị NaN/Infinity ở góc ngắm/cú đánh, lực âm, cú đánh lặp, định dạng settings/chat history và chuyển lượt. Tài khoản có tiền tố ngẫu nhiên; cleanup chỉ xóa dữ liệu thuộc lần chạy.
- `ui.txt`: `ReportUiCheck89.java` hoàn tất mã 0 trên JavaFX/Xvfb :99, transport giả lập. Xác nhận TLD dài, hai tin giống nhau, lịch sử sai/đúng người nhận, refresh A→B→A, popup/toast/solo, đọc mẫu ngắm và parser settings.
- `ui-first-run.txt`: lần đầu dừng do harness tìm phương thức private ở lớp con thay vì NetworkClient. Chỉ sửa vị trí phản chiếu trong harness, không sửa mã ứng dụng; sau đó chạy lại đầy đủ và giữ cả log thất bại.

## Hai giới hạn tái hiện

1. Người đang có lượt gửi PHYSICS_DONE trước khi có cú đánh vẫn làm chuyển lượt. onPhysicsDone kiểm tra người gửi nhưng chưa bắt buộc shotInProgress đang bật.
2. Lực 1000 (hữu hạn, lớn hơn MAX_FORCE=25 của giao diện) vẫn được relay. Chặn NaN/Infinity và lực âm chưa thay thế kiểm tra miền giá trị nghiệp vụ.

Các dòng FINDING là xác nhận hiện trạng cần hoàn thiện, không phải nghiệm thu đạt. Mã thoát 0 cho biết script hoàn tất các assertion, bao gồm assertion tái hiện giới hạn.

## Phạm vi chưa kiểm chứng

Không chạy hai UI thật qua Internet/jitter, không chơi trọn ván, không đo độ mượt/FPS, không nghiệm thu Windows/EXE, không kiểm chứng hội tụ 16 bi. Các thay đổi phản xạ băng/đường ngắm xoáy được đối chiếu mã và biên dịch. ResultScene đã được mở/chụp ở cả hai nhánh thắng và thua bằng dữ liệu giả lập, chưa thử một ván đầu cuối. Kiểm tra parser settings không chứng minh mọi lần lưu DB hoặc mọi skin đều thành công.

CHAT_HISTORY_RESPONSE hiện có hai chuỗi: friendUsername và historyData. Client/server cũ và mới không được mặc nhiên xem là tương thích vì chưa có handshake phiên bản. PacketType vẫn có 47 mã; comment về payload lịch sử còn cũ, báo cáo ưu tiên bộ ghi/đọc thực tế.

## Tái lập

Biên dịch vào thư mục sạch, dùng JDK/JavaFX phù hợp. Biên dịch và chạy ReportServer trên classpath server và PostgreSQL JDBC, rồi chạy script Python. Harness chỉ dùng DB phát triển; đường dẫn psql trong script phải điều chỉnh cho môi trường khác. Không trỏ vào database thật.

UI harness cần JavaFX controls/fxml/media, classpath client mới và Xvfb. Tham số đầu vào là thư mục ảnh đầu ra. Ảnh hiện hành được tập hợp tại `reports/images_89f7926/`; báo cáo đã thay toàn bộ 7 ảnh lịch sử bằng bộ 20 ảnh của bản mới. `CaptureAllUi89.java` bổ sung các màn hình và dialog còn thiếu; log `ui-capture.txt`. Các cảnh báo cache/preferences thuộc môi trường chụp được giữ trong log, không phải kết quả kiểm thử nghiệp vụ.

Lệnh UI tương ứng: biên dịch hai harness với `--module-path <JavaFX Linux> --add-modules javafx.controls,javafx.fxml,javafx.media` và classpath client mới, rồi chạy `DISPLAY=:99 java -Dprism.order=sw ... ReportUiCheck89 <thư-mục-ảnh>` và `CaptureAllUi89 <thư-mục-ảnh>`. Không cần kết nối server cho các fixture UI.
