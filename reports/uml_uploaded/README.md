# Sơ đồ cho bản Word người dùng gửi

Bộ hình này dùng trong `Bao_cao_tong_hop_nhom_LTM_chinh_sua.docx`, giữ mô hình kỹ thuật của các sơ đồ gốc và bỏ dòng tiêu đề bên trên cùng nhãn thành viên trong hình. Tên lớp, tác nhân, nội dung chức năng, đường nối và ký hiệu UML được giữ.

Các ảnh gốc trong file gửi lên khớp hoàn toàn theo SHA-256 với bộ PNG tại `reports/uml`. Nguồn PlantUML được đối chiếu và tạo lại thành SVG; PNG được kết xuất trên khung có kích thước đúng bằng ảnh gốc để giữ hình học bố trí trong Word. Đây là kết xuất từ mô hình kỹ thuật, không vẽ lại bằng suy đoán từ ảnh.

- `.puml`: nguồn không có lệnh title và không có nhãn TV.
- `.svg`: hình vector để đọc/phóng to.
- `.png`: bản nhúng có kích thước khung tương ứng ảnh trong Word.

Dựng SVG bằng Java, PlantUML 1.2025.2, Graphviz và font DejaVu Sans:

```bash
java -Djava.awt.headless=true -jar /path/to/plantuml.jar -charset UTF-8 -tsvg -failfast2 reports/uml_uploaded/*.puml
```

Sau đó `reports/revise_uploaded_report.py` kết xuất PNG và thay đúng các hình trong bản Word đầu vào. Script cần Python, lxml, Pillow và CairoSVG; xem [ghi chú bản sửa](../CHINH_SUA_BAN_GUI.md).
