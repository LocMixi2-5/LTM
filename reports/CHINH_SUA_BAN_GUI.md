# Bản sửa trực tiếp từ Word người dùng gửi

Ngày 29/10/2026. Bản này giữ bố cục riêng của file người dùng cung cấp, không tạo lại bằng mẫu báo cáo bảy chương của lần trước.

- [Word đã sửa](Bao_cao_tong_hop_nhom_LTM_chinh_sua.docx)
- [PDF để xem — 61 trang](Bao_cao_tong_hop_nhom_LTM_chinh_sua.pdf)

Đã sửa 51 đoạn/cụm văn bản: mô tả luồng xử lý theo thành phần và chức năng, bỏ cách gọi TV1–TV4 trong phần kỹ thuật, tiêu đề và chú thích. Bảng phân công với các tên Lộc, Minh, Trung, Mạnh được giữ theo file đã gửi; thông tin bìa dùng các tên tương ứng và để trống MSSV.

14 UML được kết xuất lại từ đúng nguồn đã đối chiếu, bỏ dòng tiêu đề trên hình và nhãn thành viên. Nhãn tác nhân, lớp, chức năng và các đường nối vẫn giữ để sơ đồ có thể đọc được. Bảy ảnh giao diện giữ nguyên từng byte.

Kiểm tra cấu trúc xác nhận giữ nguyên số lượng và thứ tự đoạn, bảng, section, hình, bookmark và hyperlink. Các thuộc tính đoạn/bảng, kích thước và vị trí khung ảnh, styles, numbering, header/footer và các phần khác trong gói Word không được tạo lại. Khi xuất cùng môi trường LibreOffice, cả bản gốc và bản sửa đều có 61 trang. Word trên máy khác có thể phân trang tùy font và phiên bản ứng dụng.

## Tái tạo từ file gốc đã gửi

File đầu vào gốc không được lưu trong repository. Chạy với đúng bản người dùng đã gửi, không dùng báo cáo do `build_report.py` tạo thay thế:

```bash
python3 reports/revise_uploaded_report.py /path/to/original.docx --output reports/Bao_cao_tong_hop_nhom_LTM_chinh_sua.docx
soffice --headless --convert-to pdf --outdir reports reports/Bao_cao_tong_hop_nhom_LTM_chinh_sua.docx
```

Python cần lxml, Pillow, CairoSVG; SVG đã có trong `uml_uploaded`. Cách dựng lại SVG xem [nguồn hình](uml_uploaded/README.md). Script chỉ thay nội dung đã xác định và ảnh khớp nguồn; không sửa mã client/server.
