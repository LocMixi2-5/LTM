# Kết quả rà soát bản Word mới nhất — 29/10/2026

## Phạm vi

Đã đọc nội dung và cấu trúc bản `Bao_cao_tong_hop_nhom_LTM_chinh_sua.docx` người dùng đính kèm mới nhất, đối chiếu Phần 9 với các log và script tại `reports/evidence/00bf1cc`, đồng thời xuất PDF để kiểm tra mục lục và bảng kiểm thử. Chưa chỉnh sửa file Word và chưa chạy lại game trong lần rà soát này.

- SHA-256 bản đầu vào: `b8a8bfaed1f4fe0f21672c004507ef8405e4b9d43f3b10a640098d0be484b40f`.
- Cấu trúc: 10 phần, 31 mã đặc tả riêng biệt (7/7/8/9), 21 hình.
- 14 ảnh UML khớp bộ `uml_uploaded`, 7 ảnh UI khớp bộ `images` theo SHA-256.
- PDF xuất trong môi trường rà soát có 55 trang; số trang này chỉ định vị bản đã kiểm tra, có thể khác khi đổi font hoặc phần mềm.
- Mã ứng dụng trên main vẫn là `00bf1cc`; các commit sau là tài liệu. Không suy diễn đây là một lần nghiệm thu lại mã đó.

## Kết luận chính

Phần kiểm thử không cần viết lại thành kết quả “tất cả đều đạt”. Các bảng hiện tại đã có những giới hạn đúng: KT05 chỉ xác nhận chuyển tiếp mẫu ngắm; KT06 chưa chốt trạng thái bàn; NT01–NT07 còn là kế hoạch. Điểm cần sửa chính là bổ sung bối cảnh/môi trường, khả năng đối chiếu minh chứng và cách trình bày các kết quả gộp.

| Mức ưu tiên | Vị trí | Nhận xét xác nhận trong bản gửi | Cách xử lý |
| --- | --- | --- | --- |
| Cao | Phần 9.1, trang 51 | Chỉ còn đoạn nói về lần chạy đầu và khoảng chờ 250 ms; chưa mô tả môi trường/phương pháp | Khôi phục ngày chạy, commit, Linux, JDK 21 với --release 17, server/DB thật và UI giả lập theo README minh chứng |
| Cao | KT01 | Ghi “môi trường đã nêu” nhưng phần trên chưa nêu; không đính kèm đường dẫn nguồn kết quả build | Nêu nguồn README và mức bằng chứng; không gọi đó là log biên dịch mới |
| Cao | KT01–KT10, L01–L07 | Không gắn trực tiếp kết quả với log/script | Thêm nguồn theo từng dòng hoặc bảng đối chiếu gọn; chuẩn hóa PASS/FINDING/chưa kiểm tra |
| Cao | KT08 | Gộp form, refresh, thread và nhận xét “có hành vi đúng” | Tách hành vi đã kiểm tra khỏi lỗi email/refresh; liên hệ L05/L07 |
| Vừa | Mục lục, trang 2 | Chỉ có tiêu đề; không tìm thấy trường TOC trong XML, PDF cũng chỉ hiện tiêu đề | Tạo mục lục từ heading, cập nhật liên kết và số trang |
| Vừa | Hình ở các phần 4–8 | “Hình 1/2” lặp theo phần nhưng không ghi tiền tố; còn “Hình 4.3” của cách đánh số trước | Chuẩn hóa theo phần, cập nhật tham chiếu và giữ vị trí ảnh |
| Vừa | Phần 8.2 | Chú thích ảnh đã rút gọn, không còn đoạn chung về ngày/môi trường chụp | Bổ sung nguồn ảnh và điều kiện fixture trước loạt ảnh; không biến thành minh chứng hai UI đầu cuối |
| Vừa | Cuối tài liệu | Chưa thấy danh mục nguồn tham khảo/minh chứng trong bản gửi | Thêm mục ngắn cuối tài liệu, không đổi thứ tự 10 phần |
| Vừa | Bảng kiểm thử, trang 51 | Mã KT01–KT10 bị ngắt thành KT0 và chữ số cuối do cột quá hẹp | Chỉnh độ rộng cột và ngắt dòng; giữ kích thước chữ đọc được |
| Nhẹ | Phần 2.5, chú thích ảnh | Có “hướng hoàn thiện,.”, đoạn riêng chỉ có “.” và “lời mờ.” | Sửa dấu câu, lỗi chữ và khoảng trắng |

## Đối chiếu minh chứng đáng chú ý

- `current-protocol.txt` xác nhận 60 mẫu ngắm 34 byte được relay, không echo. `check_current.py` gửi các mẫu cách nhau khoảng 34 ms; đây là bài giao thức ngắn, không phải đánh giá độ mượt trong một ván trên hai UI.
- Cùng log có cả PASS và FINDING. Chuyển lượt bằng PHYSICS_DONE không bao gồm snapshot/ACK hoặc xác minh vị trí 16 bi.
- READY lặp, NaN, cú đánh lặp, lời mời sai tên và email sai định dạng có dòng FINDING. Không phải lỗi mới phát hiện qua lần rà soát tài liệu này.
- `ui-check.txt` xác nhận các trường hợp UI chạy trên JavaFX thật nhưng sử dụng transport giả lập. Có cả lỗi TLD dài, refresh cũ, tin trùng/lịch sử vào sai hội thoại và parser SETTINGS_RESPONSE.
- PASS của server về SETTINGS_RESPONSE không phủ định lỗi parser client: hai kiểm tra quan sát hai phía của hợp đồng khác nhau. Báo cáo cần giải thích điểm này nếu tổng hợp kết quả lưu tùy chọn.
- `current-protocol-first-run.txt` kết thúc tại AssertionError; lần sau mới kiểm tra với khoảng chờ 250 ms. Không được xóa lịch sử thất bại hoặc gọi thay đổi kịch bản là bản sửa ứng dụng.
- Kết quả build 9 file server/26 file client được ghi trong README minh chứng. Lần rà soát này không tái biên dịch và không bổ sung log build mới.

## Những nội dung đang đúng nên giữ

- Bố cục 10 phần và bảng phân công với các tên người dùng đã điền.
- 31 đặc tả và 14 UML, cùng 7 ảnh giao diện; không phát hiện thiếu mã đặc tả trong kiểm tra cấu trúc.
- Bảng thuật ngữ đã giải thích HUD; chỉ cần dùng cách gọi tiếng Việt nhất quán hơn trong nội dung.
- Phần kỹ thuật đã bỏ cách gọi TV1–TV4.
- KT05/KT06 đã ghi giới hạn, KT09/KT10 đã nhắc fixture; NT01–NT07 chưa bị ghi thành kết quả đã đạt.
- Kết luận còn nêu hạn chế về bất đồng bộ và Windows, phù hợp với phạm vi minh chứng đang có.

## Đầu ra của lần rà soát

Đã tạo [prompt chỉnh sửa và kiểm tra lại báo cáo](../PROMPT_Ra_soat_va_sua_bao_cao_LTM.md), dùng trực tiếp với bản Word mới nhất. Prompt yêu cầu giữ bố cục, sửa Phần 9 có dẫn chứng, rà toàn tài liệu và xuất Word/PDF cùng nhật ký thay đổi. File Word đầu vào chưa bị thay đổi.
