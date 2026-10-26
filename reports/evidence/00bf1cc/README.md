# Kiểm tra mã nguồn 00bf1cc — ngày 26/10/2026 (Asia/Saigon)

Phiên bản: `00bf1ccaf8df2cdd54b79cb75ca188ae9c38627e`. Mã ứng dụng không được thay đổi trong lần viết báo cáo. Các script trong thư mục này mô tả hiện trạng và được bổ sung cùng báo cáo.

## Các lần chạy

- Biên dịch vào thư mục mới: 9 file Java server, 26 file Java client; JDK 21, `--release 17`; cả hai mã thoát 0.
- Khởi chạy server vừa biên dịch trên TCP 12345; PostgreSQL phát triển trên loopback.
- `current-protocol.txt`: lần chạy hoàn tất `check_current.py`, mã thoát 0. Gồm các tình huống PASS và FINDING; 60 mẫu ngắm, timeout lời mời 15,00 giây, các tình huống mất kết nối và trạng thái không hợp lệ. Tài khoản tạm và dữ liệu liên quan được xóa sau bài thử.
- `current-protocol-first-run.txt`: lần chạy đầu kết thúc mã 1 tại tình huống phản hồi sai tên. Phản hồi được gửi ngay sau khi nhận lời mời, khi chưa có hàng rào bảo đảm server đã tạo timer. Lần sau bổ sung khoảng chờ 250 ms để kiểm tra trạng thái timer đã thiết lập. Không ghi lần đầu là kết quả đạt.
- `ui-check.txt`: `ReportUiCheck.java`, mã thoát 0. Các lớp JavaFX thật; mạng giả lập; tạo ảnh và kiểm tra callback. Kiểm tra UI có cả hành vi đúng lẫn lỗi được tái hiện.

Mã thoát 0 chỉ xác nhận script hoàn tất các assertion, **không có nghĩa ứng dụng không còn lỗi**. Các dòng FINDING xác nhận hiện tượng cần sửa; không được ghi chúng là chức năng đã đạt nghiệm thu.

## Phạm vi và giới hạn

Không chạy bộ kiểm thử v2 của nhánh cũ. Không chạy Windows, không benchmark Internet, không kiểm tra toàn bộ 16 bi hội tụ và không chơi một ván đầy đủ trên hai UI thật. Hình GameScene hiện tại cũng dùng callback giả lập; không tái sử dụng ảnh từ v2.

`check_current.py` chỉ được chạy với server/DB phát triển cục bộ. Script dùng PostgreSQL 17 ở `/workspace/ltm-env/sysroot/usr/lib/postgresql/17/bin/psql`, tài khoản SQL `postgres` với trust loopback của môi trường này. Không trỏ vào DB thật. Tên tài khoản có tiền tố ngẫu nhiên; cleanup chỉ xóa bản ghi của lần chạy.

## Tái lập trong môi trường đã chuẩn bị

Biên dịch mã nguồn hiện tại vào hai thư mục rỗng để không lẫn các lớp từ nhánh cũ. Khởi chạy server mới với PostgreSQL JDBC. Sau đó:

```bash
python3 reports/evidence/00bf1cc/check_current.py
source /workspace/ltm-env/scripts/env.sh
javac --release 17 -encoding UTF-8 --module-path "$JAVAFX_PATH" \
  --add-modules javafx.controls,javafx.fxml,javafx.media \
  -cp /workspace/ltm-env/report-latest/classes/client \
  -d /workspace/ltm-env/report-latest/classes \
  reports/evidence/00bf1cc/ReportUiCheck.java
DISPLAY=:99 java -Dprism.order=sw --module-path "$JAVAFX_PATH" \
  --add-modules javafx.controls,javafx.fxml,javafx.media \
  -cp /workspace/ltm-env/report-latest/classes/client:/workspace/ltm-env/report-latest/classes \
  ReportUiCheck /workspace/LTM-report-latest/reports/images
```

Các đường dẫn trên là của môi trường kiểm tra, cần điều chỉnh ở máy khác. Xvfb :99 và JavaFX Linux phải sẵn có. Nhật ký không chứa mật khẩu thật của người dùng; mật khẩu trong script chỉ dành cho tài khoản tạm.
