# Báo cáo cập nhật theo main 00bf1cc

Báo cáo ngày 26/10/2026 giữ đúng bốn phần của nhóm, đối chiếu mã nguồn **00bf1cc** và thay thế mô tả v2 không còn đúng với main hiện tại.

- [Word để chỉnh sửa](Bao_cao_LTM_4_phan.docx)
- [PDF 30 trang để đọc và in](Bao_cao_LTM_4_phan.pdf)
- [Đọc nội dung và hình ngay trên GitHub](Bao_cao_LTM_4_phan.md)
- [Nguồn ảnh và sơ đồ](images/README.md)
- [Kịch bản và nhật ký kiểm tra](evidence/00bf1cc/README.md)

Có **11 hình** gồm 4 sơ đồ và 7 ảnh JavaFX mới. Nội dung cập nhật thách đấu, popup/toast và solo. Các phần giao thức/gameplay đã viết lại theo 47 packet, CUE_AIM 34 byte và PHYSICS_DONE; không mô tả snapshot/ACK v2 là tính năng hiện có.

Đã biên dịch client/server, kiểm tra giao thức với server thật và các màn hình JavaFX bằng transport giả lập. Báo cáo ghi rõ lỗi còn tái hiện và các hạng mục chưa xác minh. Không có sửa đổi mã nguồn ứng dụng hoặc tạo EXE trong lần này.

Thông tin trường, lớp, giảng viên và MSSV cần nhóm bổ sung. Tên “Trung” giữ đúng theo bảng phân công.

## Tạo lại tài liệu

Cần Pandoc, Python, python-docx, lxml; PDF cần thêm WeasyPrint cùng thư viện hệ thống của công cụ.

```bash
python3 reports/build_report.py       # Word
python3 reports/build_report.py --pdf # Word, HTML và PDF
```

Ảnh được nhúng trong Word/PDF. HTML dùng file `report.css` và `images` nằm cạnh tài liệu. Tái tạo tài liệu không chạy game hoặc ghi dữ liệu vào database.
