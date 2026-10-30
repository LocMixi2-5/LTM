# Đối chiếu báo cáo với GitHub — 30/10/2026

## Nguồn và phạm vi

- Repository: https://github.com/LocMixi2-5/LTM
- Mã ứng dụng đối chiếu: `89f7926c13345bcd21fbaccaf7d8e13fe3fdb562` (main khi kiểm tra).
- Đầu vào: “Bao Cao LTM.docx”, SHA-256 `f7e3541d0a56a307bcd30ee2c81604173c74db95ee602a6e99e72dcfa16db917`.
- Đầu ra: [Word](../Bao_Cao_LTM_cap_nhat_89f7926.docx), [PDF](../Bao_Cao_LTM_cap_nhat_89f7926.pdf). Bản Word ở gốc repository `Bao_cao_tong_hop_nhom_LTM_chinh_sua.docx` được đồng bộ với bản này để tránh tải nhầm bản cũ.
- Giữ 10 phần chính, 31 đặc tả UC, 14 UML và danh sách thành viên như file gửi. Nội dung kỹ thuật trình bày theo chức năng, không chia lời thuyết trình thành TV1–TV4. Không sửa mã client/server hoặc JAR/EXE.

## Nội dung đã sửa

| Nội dung bản gửi | Điều chỉnh theo mã hiện tại |
|---|---|
| SETTINGS_RESPONSE còn lệch parser | Đọc đúng một boolean; kiểm tra không đọc lấn packet kế tiếp. UI vẫn có cập nhật lạc quan trước xác nhận DB. |
| READY đếm số gói | Set theo người chơi hợp lệ; cần hai người khác nhau cùng sẵn sàng. |
| NaN/lực âm/cú đánh lặp | Cập nhật kiểm tra giá trị hữu hạn và shotInProgress; không suy ra đã chặn toàn bộ miền giá trị. |
| CHAT_HISTORY_RESPONSE chỉ có dữ liệu | Hiện gồm friendUsername rồi historyData; cập nhật cả mô tả wire format và giới hạn tương thích phiên bản. |
| Lịch sử sai bạn, loại trùng tin, refresh A→B→A | Ghi nhận các nhánh đã sửa và kiểm tra lại. Vẫn thiếu request/message ID cho mọi phản hồi muộn của cùng hội thoại. |
| Phản hồi sai tên hủy timer | Thứ tự kiểm tra đã sửa; lời mời vẫn hết hạn sau phản hồi sai tên. |
| Regex email giới hạn TLD và server chưa kiểm tra | Client/server chấp nhận TLD dài, server từ chối sai cú pháp; chưa có xác minh email. |
| Mô tả vật lý cũ | Bổ sung phản xạ băng theo hướng vận tốc, giảm vận tốc/xoáy, cập nhật đường ngắm spinX; chỉ ghi phạm vi đọc mã/biên dịch. |
| Giao diện kết quả cũ | Bỏ mô tả khung mở khóa skin ở kết quả; ảnh thắng/thua phản ánh ResultScene mới. |
| Nhật ký cũ dùng như hiện trạng | Thay bằng bảng KT01–KT13 của lần chạy mới, tách giới hạn G01/G02 và kế hoạch nghiệm thu NT01–NT07. |
| Mục lục trống, số hình lệch chương | Thêm mục lục liên kết và số trang đã đối chiếu PDF; đánh số 14 UML theo Phần 4–7 và 20 giao diện theo Phần 8. |

## Bổ sung đầy đủ giao diện

Thay 7 ảnh lịch sử bằng **20 hình từ mã hiện tại**, gồm đăng nhập; cấu hình server; đăng ký và lỗi đầu vào; sảnh; menu bạn; tìm/kết bạn và lời mời kết bạn; chat; thách đấu; toast; tìm trận; chuẩn bị; thi đấu; luyện tập; 3 tab cài đặt; bảng xếp hạng; thắng; thua. Bao quát 8 lớp Scene và 3 lớp Dialog. Mỗi hình có mô tả chức năng và chú thích bên ngoài ảnh, không thêm tên thành viên lên ảnh.

Ảnh chụp lớp JavaFX thật, sử dụng tài khoản/callback mạng giả lập trong Linux/Xvfb. Không giả danh ảnh nghiệm thu hai máy hoặc bản EXE Windows. Xem [nguồn ảnh](../images_89f7926/README.md) và `manifest.json`.

## Kiểm tra đã thực hiện và giới hạn

- Biên dịch sạch 9 Java server, 26 Java client, `--release 17` trên JDK 21.
- Bài giao thức qua TCP loopback với lớp server nguyên bản và PostgreSQL phát triển: kết thúc mã 0; giữ kết quả đạt và FINDING riêng.
- Fixture JavaFX: xác nhận parser settings, email, refresh, tin giống nhau, lịch sử đúng/sai bạn, popup/toast, solo và mẫu ngắm.
- Chụp bổ sung các scene/dialog và tab cài đặt. Sửa harness để chọn đúng thứ tự tab và khởi động preview qua SettingsDialog.show(); không sửa ứng dụng.
- Lần UI đầu gặp lỗi phản chiếu trong harness; log thất bại và lần chạy lại đều được giữ.

**Hai giới hạn tái hiện:** người có lượt vẫn gửi được PHYSICS_DONE trước cú đánh để đổi lượt; lực hữu hạn 1000 vẫn được relay dù giao diện có MAX_FORCE=25. Báo cáo ghi là tồn tại cần sửa, không ghi đạt nghiệm thu.

Chưa chơi trọn ván trên hai UI thật, chưa thử Internet/jitter, chưa đo FPS/độ mượt, chưa chạy Windows EXE, chưa kiểm chứng hội tụ 16 bi hoặc transaction DB. Tệp Word/PDF không tuyên bố hệ thống hết lỗi đồng bộ.

## Tái tạo tài liệu

Các script thao tác tài liệu, không biên dịch vào ứng dụng:

1. `update_report_89f7926.py`: nhận đúng file đầu vào theo SHA-256; sửa nội dung, bảng kiểm thử, UML. Đầu vào gốc cũng có thể lấy từ commit 89f7926 nếu hash khớp.
2. `complete_ui_report_89f7926.py`: chèn 20 hình vào mục giao diện. Đường dẫn ảnh tạm trong script cần đổi nếu chạy ở môi trường khác.
3. Xuất PDF bằng LibreOffice; `finalize_report_89f7926.py` đọc PDF để điền mục lục. Xuất lại và kiểm tra số trang khớp; chạy lại bước mục lục nếu có thay đổi phân trang.

Cần python-docx, lxml, Pillow, CairoSVG, PyMuPDF; PlantUML để tái tạo SVG. Các đường dẫn môi trường kiểm tra trong script/harness cần điều chỉnh khi chạy trên máy khác. Không trỏ harness dữ liệu vào DB thật.
