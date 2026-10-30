# Báo cáo tổng hợp

**Cập nhật 30/10/2026, đối chiếu mã ứng dụng `89f7926`: [Word đã sửa](Bao_Cao_LTM_cap_nhat_89f7926.docx) · [PDF](Bao_Cao_LTM_cap_nhat_89f7926.pdf).** Giữ bố cục 10 phần của file “Bao Cao LTM.docx”; cập nhật nội dung theo GitHub, kết quả kiểm tra mới, 14 UML và 20 ảnh giao diện hiện hành. [Biên bản đối chiếu](reviews/Doi_chieu_GitHub_2026-10-30.md) · [Minh chứng](evidence/89f7926/README.md) · [Danh mục ảnh](images_89f7926/README.md).

Các phiên bản phía dưới là tài liệu lịch sử, không phải bản hiện hành.

**Bản sửa trước ngày 30/10 — giữ bố cục file người dùng gửi:** [Word](Bao_cao_tong_hop_nhom_LTM_chinh_sua.docx) · [PDF 61 trang](Bao_cao_tong_hop_nhom_LTM_chinh_sua.pdf). Đã viết lại các đoạn nhắc TV1–TV4 theo chức năng, bỏ tiêu đề và nhãn thành viên trong hình. [Chi tiết thay đổi và cách tái tạo](CHINH_SUA_BAN_GUI.md).

## Rà soát phần kiểm thử của bản Word mới nhất

- [Prompt chỉnh sửa và kiểm tra lại báo cáo (.md)](PROMPT_Ra_soat_va_sua_bao_cao_LTM.md)
- [Kết quả rà soát ngày 29/10/2026](reviews/Ra_soat_bao_cao_2026-10-29.md)

Hai file trên dựa trên bản người dùng gửi tiếp theo, có bố cục 10 phần. Lần này chỉ rà soát và tạo prompt; chưa sửa bản Word mới hoặc chạy lại game.

## Bản biên soạn theo chương trước đó

Cập nhật **29/10/2026**, đối chiếu mã ứng dụng **00bf1cc**. Bản này được biên tập theo một mạch nội dung chung để một người có thể trình bày toàn bộ đồ án.

- **[Word để chỉnh sửa](Bao_cao_tong_hop_nhom_LTM.docx)**
- **[PDF — 53 trang](Bao_cao_tong_hop_nhom_LTM.pdf)**
- [Đọc nội dung trên GitHub](Bao_cao_tong_hop_nhom_LTM.md)

Phần chính gồm bảy chương: giới thiệu, cơ sở lý thuyết, kiến trúc, quy trình hoạt động, triển khai/minh họa, kiểm thử/đánh giá và kết luận. Nội dung giải thích sự phối hợp giữa kết nối, dữ liệu, gameplay và giao diện trong từng thao tác của người chơi. Không chia chương theo người phụ trách và không đưa mã UC vào phần trình bày chính.

Trong PDF, phần chính nằm ở **trang 5–27**. **Phụ lục A (trang 28–37)** giữ 31 đặc tả để tra cứu; **Phụ lục B (trang 38–52)** chứa 14 UML với tiêu đề theo chức năng. Bảy ảnh JavaFX nằm trong chương triển khai, đánh số 5.1–5.7 và ghi rõ điều kiện chụp. PDF có mục lục số trang/liên kết; Word có mục lục nội dung liên kết.

## Nguồn và minh chứng

- [UML của bản thống nhất: PNG, SVG và PlantUML](uml_unified/README.md)
- [Nguồn ảnh giao diện](images/README.md)
- [Kịch bản và nhật ký kiểm tra ngày 26/10](evidence/00bf1cc/README.md)
- [Tài liệu đặc tả ban đầu](Bao_cao_LTM_4_phan.md), giữ riêng để đối chiếu lịch sử

Việc sửa văn phong và bố cục không thay đổi mã ứng dụng. Các lỗi và giới hạn kiểm thử vẫn được ghi đúng theo minh chứng; lần cập nhật này không chạy lại game hoặc tạo EXE. Thông tin trường, lớp, giảng viên, danh sách thành viên và MSSV cần được điền trước khi nộp.

## Tạo lại tài liệu

Cần Pandoc, Python, python-docx, lxml; PDF cần WeasyPrint, thư viện hệ thống và font DejaVu. Chạy từ thư mục gốc repository:

```bash
python3 reports/build_report.py --full --pdf
```

Nguồn nội dung chính:

- `chapters/he_thong_thong_nhat.md`: chương 1–4.
- `chapters/trien_khai_thong_nhat.md`: chương 5–7.
- `chapters/tai_lieu_tham_khao.md`: nguồn đối chiếu.
- `uml_spec.json`: đặc tả chức năng cho phụ lục A.
- `uml_unified/manifest.json`: danh mục biểu đồ cho phụ lục B.
- `write_group_report.py`: ghép bìa, danh mục hình, các chương và phụ lục.

Hướng dẫn dựng lại hình nằm trong [thư mục UML](uml_unified/README.md). HTML cần giữ `report.css`, `uml_unified` và `images` nằm cạnh tài liệu. Bản đặc tả ban đầu vẫn có thể tạo bằng `python3 reports/build_report.py --pdf`.
