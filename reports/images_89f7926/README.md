# Giao diện mã 89f7926 — 30/10/2026

20 hình được dùng trong Phần 8 của `Bao_Cao_LTM_cap_nhat_89f7926.docx`; danh mục và lớp nguồn trong `manifest.json`. Bao quát cả 8 lớp Scene (GameScene có thi đấu/luyện tập, ResultScene có thắng/thua) và 3 lớp Dialog, cùng các trạng thái menu, chat, popup, toast, lỗi đăng ký và 3 tab cài đặt.

Ảnh chụp các lớp JavaFX thật biên dịch từ `89f7926c13345bcd21fbaccaf7d8e13fe3fdb562`, Linux/Xvfb :99, kết xuất phần mềm. Tài khoản Demo, số liệu và callback mạng là fixture có kiểm soát; không phải dữ liệu thật hoặc ảnh chạy EXE Windows. Không vẽ lại giao diện, không chèn tên thành viên/tiêu đề đồ án lên ảnh.

- `ReportUiCheck89.java`: đăng ký/validation, sảnh/menu/chat, lời mời/toast, luyện tập và mẫu ngắm trong bàn thi đấu.
- `CaptureAllUi89.java`: đăng nhập, cấu hình server, sảnh, tìm/kết bạn, 3 tab cài đặt, bảng xếp hạng, tìm trận, chuẩn bị trận và thắng/thua. SettingsDialog được mở qua `show()` để khởi động cả animation preview gậy.
- Hai harness ở `../evidence/89f7926/`; nhật ký `ui.txt`, `ui-capture.txt`.

Ảnh chỉ minh họa giao diện hiện hành, không thay kiểm tra cập nhật DB, ván hoàn chỉnh hay đồng bộ qua mạng. Các cảnh báo cache/preferences của môi trường Linux nằm trong log. Có thêm `lobby_actual.png` từ bài UI ban đầu; báo cáo dùng `lobby.png` có session minh họa đầy đủ.
