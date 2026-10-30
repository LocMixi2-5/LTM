# UML đối chiếu 89f7926

14 biểu đồ giữ cấu trúc của báo cáo người dùng gửi, bỏ tiêu đề tên thành viên trong hình. PlantUML là nguồn, SVG là bản vector, PNG là ảnh chèn Word theo kích thước khung gốc.

Đã cập nhật luồng lời mời sai tên/timer, kiểm tra email ở server, parser settings một boolean, kiểm tra cú đánh và shotInProgress. Những giới hạn chưa khắc phục vẫn được ghi đúng trong chú thích và nội dung báo cáo.

Tạo SVG bằng PlantUML: `java -Djava.awt.headless=true -jar plantuml.jar -charset UTF-8 -tsvg -failfast2 reports/uml_89f7926/*.puml`.

`reports/update_report_89f7926.py` dùng CairoSVG để tạo PNG và thay đúng 14 ảnh của bản gốc. Không dùng ảnh sinh tự động để mô tả cấu trúc mã.
