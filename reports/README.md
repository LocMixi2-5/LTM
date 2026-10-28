# Báo cáo tổng hợp đồ án của nhóm

Cập nhật **28/10/2026**, đối chiếu mã ứng dụng **00bf1cc**. Báo cáo tổng bao quát toàn bộ đồ án: mở đầu, lý thuyết, kiến trúc, bốn phần phân công, triển khai/sử dụng, minh họa giao diện, kiểm thử, kết luận và hướng phát triển.

- **[Tải báo cáo tổng Word](Bao_cao_tong_hop_nhom_LTM.docx)**
- **[Tải báo cáo tổng PDF — 63 trang](Bao_cao_tong_hop_nhom_LTM.pdf)**
- [Đọc báo cáo tổng trên GitHub](Bao_cao_tong_hop_nhom_LTM.md)

Bản tổng giữ **31 đặc tả use case và 14 biểu đồ UML** theo phân công, bổ sung **7 ảnh JavaFX** có ghi rõ nguồn và giới hạn kiểm chứng. PDF có mục lục kèm số trang và liên kết; Word có mục lục nội dung liên kết tới các phần chính. Các ảnh minh họa đánh số M1–M7, không tính vào định mức 14 UML.

| Thành viên | Phân hệ | Use case | Biểu đồ |
| --- | --- | --- | --- |
| TV1 | Quản lý kết nối & Giao thức truyền tin | UC-C01–UC-C07 (7) | Use Case, Sequence, Activity (3) |
| TV2 | Xác thực & Quản lý dữ liệu | UC-D01–UC-D07 (7) | Use Case, Activity ×2, Sequence (4) |
| TV3 | Gameplay & Engine Vật lý | UC-G01–UC-G08 (8) | Use Case, Activity, Sequence (3) |
| TV4 | Giao diện & Cá nhân hóa | UC-UI01–UC-UI09 (9) | Use Case, Activity, Sequence, Component (4) |

Bản chuyên sâu bốn phân hệ đã chốt trước đó được giữ riêng để tham khảo:

- [Word bốn phân hệ](Bao_cao_LTM_4_phan.docx)
- [PDF để đọc và in](Bao_cao_LTM_4_phan.pdf)
- [Nội dung trên GitHub](Bao_cao_LTM_4_phan.md)
- [14 biểu đồ: PNG, SVG và nguồn PlantUML](uml/README.md)
- [Kịch bản và nhật ký kiểm tra ngày 26/10](evidence/00bf1cc/README.md)
- [Ảnh JavaFX và sơ đồ bổ sung của bản trước](images/README.md)

Mỗi use case có tác nhân, tiền điều kiện, luồng chính, ngoại lệ, hậu điều kiện và liên hệ mã nguồn. Nội dung mô tả hành vi đang triển khai cùng giới hạn đã phát hiện; biểu đồ không được dùng thay cho kết quả nghiệm thu. Kiểm thử được đưa vào phụ lục chung, phần TV4 tập trung giao diện và cá nhân hóa.

Các hình UML đã nhúng trong Word/PDF; SVG cho phép phóng to. Bảy ảnh JavaFX ngày 26/10 được nhúng trong **báo cáo tổng**, không tính vào 14 UML. Bản chuyên sâu bốn phân hệ vẫn chỉ nhúng 14 UML. Không chạy lại kiểm thử ứng dụng hoặc tạo EXE trong lần sửa tài liệu này.

Nhóm cần bổ sung trường, lớp, giảng viên, họ tên và MSSV tương ứng TV1–TV4.

## Tạo lại tài liệu

Cần Pandoc, Python, python-docx, lxml; PDF cần WeasyPrint cùng thư viện hệ thống và font DejaVu. Chạy từ thư mục gốc repository:

```bash
python3 reports/build_report.py --full --pdf # Báo cáo tổng: Markdown, Word, HTML, PDF
python3 reports/build_report.py --pdf        # Bản chuyên sâu bốn phân hệ
```

`chapters/tong_quan.md` và `chapters/trien_khai_va_danh_gia.md` chứa phần chung; `write_group_report.py` ghép chúng với bốn phân hệ.

`uml_spec.json` chứa đặc tả use case. `write_uml_report.py` ghép nội dung; `make_uml.py` tạo nguồn biểu đồ và manifest. Sau khi sửa nguồn hình, dựng lại PNG/SVG theo [hướng dẫn UML](uml/README.md) rồi tạo tài liệu.

HTML cần giữ `report.css`, thư mục `uml` và `images` nằm cạnh tài liệu. Các lệnh tạo báo cáo không khởi động game hoặc ghi dữ liệu vào database.
