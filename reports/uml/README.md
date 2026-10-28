# 14 biểu đồ UML theo phân công mới

Ngày 28/10/2026; đối chiếu mã ứng dụng `00bf1cc`. Định mức TV1/TV2/TV3/TV4 là **3/4/3/4**, gồm 4 Use Case, 5 Activity, 4 Sequence và 1 Component.

PNG 180 dpi được nhúng trong Word/PDF. SVG là ảnh vector để phóng to; `.puml` là nguồn PlantUML có thể chỉnh sửa. Các sơ đồ mô tả hành vi hiện tại, có ghi chú giới hạn; không phải ảnh chụp hoặc bằng chứng kiểm thử.

| Hình | Thành viên | Loại | Nội dung | File |
| --- | --- | --- | --- | --- |
| 1.1 | TV1 | Use Case | Các use case thuộc phân hệ | [PNG](c_usecase.png) · [SVG](c_usecase.svg) · [Nguồn](c_usecase.puml) |
| 1.2 | TV1 | Sequence | Kết nối TCP và trao đổi PING | [PNG](c_sequence.png) · [SVG](c_sequence.svg) · [Nguồn](c_sequence.puml) |
| 1.3 | TV1 | Activity | Điều phối thách đấu trực tiếp | [PNG](c_activity.png) · [SVG](c_activity.svg) · [Nguồn](c_activity.puml) |
| 2.1 | TV2 | Use Case | Các use case thuộc phân hệ | [PNG](d_usecase.png) · [SVG](d_usecase.svg) · [Nguồn](d_usecase.puml) |
| 2.2 | TV2 | Activity | Đăng ký và lưu tài khoản | [PNG](d_register_activity.png) · [SVG](d_register_activity.svg) · [Nguồn](d_register_activity.puml) |
| 2.3 | TV2 | Activity | Ghi kết quả, EXP và lịch sử | [PNG](d_result_activity.png) · [SVG](d_result_activity.svg) · [Nguồn](d_result_activity.puml) |
| 2.4 | TV2 | Sequence | Đăng nhập bằng username hoặc email | [PNG](d_login_sequence.png) · [SVG](d_login_sequence.svg) · [Nguồn](d_login_sequence.puml) |
| 3.1 | TV3 | Use Case | Các use case thuộc phân hệ | [PNG](g_usecase.png) · [SVG](g_usecase.svg) · [Nguồn](g_usecase.puml) |
| 3.2 | TV3 | Activity | Một cú đánh và xử lý cuối lượt | [PNG](g_activity.png) · [SVG](g_activity.svg) · [Nguồn](g_activity.puml) |
| 3.3 | TV3 | Sequence | Truyền cú đánh và đổi lượt hiện tại | [PNG](g_sequence.png) · [SVG](g_sequence.svg) · [Nguồn](g_sequence.puml) |
| 4.1 | TV4 | Use Case | Các use case thuộc phân hệ | [PNG](ui_usecase.png) · [SVG](ui_usecase.svg) · [Nguồn](ui_usecase.puml) |
| 4.2 | TV4 | Activity | Cá nhân hóa avatar, gậy và âm thanh | [PNG](ui_activity.png) · [SVG](ui_activity.svg) · [Nguồn](ui_activity.puml) |
| 4.3 | TV4 | Sequence | Popup thách đấu, toast và chuyển cảnh | [PNG](ui_sequence.png) · [SVG](ui_sequence.svg) · [Nguồn](ui_sequence.puml) |
| 4.4 | TV4 | Component | Thành phần giao diện và cá nhân hóa | [PNG](ui_component.png) · [SVG](ui_component.svg) · [Nguồn](ui_component.puml) |

## Tạo lại hình

Yêu cầu Java, Graphviz (`dot`), font DejaVu Sans và PlantUML. Bản đã dùng: **PlantUML 1.2025.2**; JAR không được đưa vào repository. Đặt JAR tại đường dẫn phù hợp và chạy từ thư mục gốc:

```bash
python3 reports/make_uml.py
java -Djava.awt.headless=true -jar /path/to/plantuml.jar -charset UTF-8 -tpng -failfast2 reports/uml/*.puml
java -Djava.awt.headless=true -jar /path/to/plantuml.jar -charset UTF-8 -tsvg -failfast2 reports/uml/*.puml
python3 reports/build_report.py --pdf
```

Chỉnh bố cục và nội dung biểu đồ trong `reports/make_uml.py`; chỉnh danh mục/đặc tả trong `reports/uml_spec.json`. Script tạo hình ghi đè `.puml` và `uml_manifest.json`, nên sửa trực tiếp `.puml` sẽ mất khi tạo lại. Manifest liên kết loại hình, thành viên, use case và số hình trong báo cáo.
