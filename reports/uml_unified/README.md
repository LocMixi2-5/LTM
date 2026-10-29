# Biểu đồ dùng trong báo cáo thống nhất

Bộ hình ngày 29/10/2026 dùng tiêu đề theo chức năng hệ thống, không gắn nhãn thành viên. Mô hình nghiệp vụ giữ nguyên theo mã ứng dụng 00bf1cc. Tổng cộng 14 UML gồm 4 Use Case, 5 Activity, 4 Sequence và 1 Component, đặt ở Phụ lục B của báo cáo.

PNG 180 dpi được nhúng trong Word/PDF. SVG cho phép phóng to; PlantUML là nguồn có thể chỉnh sửa.

| Hình | Nội dung | Loại | File |
| --- | --- | --- | --- |
| B.1 | Chức năng quản lý kết nối & giao thức truyền tin | Use Case | [PNG](c_usecase.png) · [SVG](c_usecase.svg) · [Nguồn](c_usecase.puml) |
| B.2 | Kết nối TCP và trao đổi PING | Sequence | [PNG](c_sequence.png) · [SVG](c_sequence.svg) · [Nguồn](c_sequence.puml) |
| B.3 | Điều phối thách đấu trực tiếp | Activity | [PNG](c_activity.png) · [SVG](c_activity.svg) · [Nguồn](c_activity.puml) |
| B.4 | Chức năng xác thực & quản lý dữ liệu | Use Case | [PNG](d_usecase.png) · [SVG](d_usecase.svg) · [Nguồn](d_usecase.puml) |
| B.5 | Đăng ký và lưu tài khoản | Activity | [PNG](d_register_activity.png) · [SVG](d_register_activity.svg) · [Nguồn](d_register_activity.puml) |
| B.6 | Ghi kết quả, EXP và lịch sử | Activity | [PNG](d_result_activity.png) · [SVG](d_result_activity.svg) · [Nguồn](d_result_activity.puml) |
| B.7 | Đăng nhập bằng username hoặc email | Sequence | [PNG](d_login_sequence.png) · [SVG](d_login_sequence.svg) · [Nguồn](d_login_sequence.puml) |
| B.8 | Chức năng gameplay & engine vật lý | Use Case | [PNG](g_usecase.png) · [SVG](g_usecase.svg) · [Nguồn](g_usecase.puml) |
| B.9 | Một cú đánh và xử lý cuối lượt | Activity | [PNG](g_activity.png) · [SVG](g_activity.svg) · [Nguồn](g_activity.puml) |
| B.10 | Truyền cú đánh và đổi lượt hiện tại | Sequence | [PNG](g_sequence.png) · [SVG](g_sequence.svg) · [Nguồn](g_sequence.puml) |
| B.11 | Chức năng giao diện & cá nhân hóa | Use Case | [PNG](ui_usecase.png) · [SVG](ui_usecase.svg) · [Nguồn](ui_usecase.puml) |
| B.12 | Cá nhân hóa avatar, gậy và âm thanh | Activity | [PNG](ui_activity.png) · [SVG](ui_activity.svg) · [Nguồn](ui_activity.puml) |
| B.13 | Popup thách đấu, toast và chuyển cảnh | Sequence | [PNG](ui_sequence.png) · [SVG](ui_sequence.svg) · [Nguồn](ui_sequence.puml) |
| B.14 | Thành phần giao diện và cá nhân hóa | Component | [PNG](ui_component.png) · [SVG](ui_component.svg) · [Nguồn](ui_component.puml) |

## Tái tạo

Cần Java, Graphviz, font DejaVu Sans và PlantUML 1.2025.2. JAR không được lưu trong repository. Chạy từ thư mục gốc, thay đường dẫn JAR bằng vị trí trên máy:

```bash
python3 reports/make_uml.py
python3 reports/make_unified_uml.py
java -Djava.awt.headless=true -jar /path/to/plantuml.jar -charset UTF-8 -tpng -failfast2 reports/uml_unified/*.puml
java -Djava.awt.headless=true -jar /path/to/plantuml.jar -charset UTF-8 -tsvg -failfast2 reports/uml_unified/*.puml
python3 reports/build_report.py --full --pdf
```

`make_uml.py` quản lý mô hình kỹ thuật gốc; `make_unified_uml.py` tạo bản tiêu đề trung tính và manifest dùng trong báo cáo tổng. Sửa nguồn trong script để thay đổi được giữ lại khi dựng lại, thay vì chỉ sửa các file sinh tự động. Bộ hình cũ trong `reports/uml` được giữ để đối chiếu với tài liệu trước.
