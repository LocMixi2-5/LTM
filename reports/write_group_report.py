"""Compose the complete group report while preserving the agreed four-subsystem report."""
from pathlib import Path
import re
import subprocess
import sys

base = Path(__file__).resolve().parent
subprocess.run([sys.executable, str(base / 'write_uml_report.py')], check=True)
original = (base / 'Bao_cao_LTM_4_phan.md').read_text()
allocation = original.split('## PHÂN CÔNG MỚI VÀ QUY ƯỚC', 1)[1].split('# PHẦN 1.', 1)[0]
allocation = '## PHÂN CÔNG VÀ QUY ƯỚC\n' + allocation
allocation = allocation.replace(
    'Báo cáo giữ nhãn TV1–TV4 đúng theo ảnh phân công mới. Ảnh không cung cấp ánh xạ họ tên, vì vậy không tự gán các tên trong bảng phân công cũ cho những trách nhiệm mới.',
    'Báo cáo dùng nhãn TV1–TV4 theo phân công của nhóm; họ tên và MSSV được điền trên trang bìa và từng phần trước khi nộp.')
core = '# PHẦN 1.' + original.split('# PHẦN 1.', 1)[1].split('## TỔNG HỢP ĐỐI CHIẾU VÀ KIỂM CHỨNG', 1)[0]
core = core.replace('như nhánh v2 cũ', 'trong phiên bản đang đối chiếu')
references = original.split('## TÀI LIỆU VÀ NGUỒN ĐỐI CHIẾU', 1)[1]
references = references.replace(',', ', ')
references = references.replace('Bảng phân công trong ảnh người dùng cung cấp ngày 28/10/2026', 'Phân công nhóm được thống nhất ngày 28/10/2026')
references = references.replace('Bảng này thay thế cách chia phần công việc trong báo cáo trước.', 'Tổng cộng 31 use case và 14 biểu đồ UML.')
references = references.replace('Các ảnh chụp UI ngày 26/10 vẫn được giữ để tham khảo, không tính vào 14 biểu đồ UML của bản phân công mới.', 'Bảy ảnh JavaFX ngày 26/10 được dùng làm minh họa M1–M7 trong báo cáo tổng, không tính vào 14 biểu đồ UML.')
cover = '''# BÁO CÁO TỔNG HỢP ĐỒ ÁN

## XÂY DỰNG TRÒ CHƠI BI-A HAI NGƯỜI QUA MẠNG

Học phần: Lập trình mạng

Trường / Khoa / Lớp: [Bổ sung]

Giảng viên hướng dẫn: [Bổ sung]

Nhóm thực hiện: [Tên nhóm]

TV1: [Họ tên — MSSV] · TV2: [Họ tên — MSSV]

TV3: [Họ tên — MSSV] · TV4: [Họ tên — MSSV]

Ngày hoàn thiện báo cáo: 28/10/2026 — múi giờ Việt Nam.

Phiên bản mã ứng dụng đối chiếu: 00bf1cc.

## TÓM TẮT

Báo cáo trình bày toàn bộ đồ án trò chơi bi-a hai người qua mạng do nhóm xây dựng bằng Java, JavaFX, TCP/UDP Socket và PostgreSQL. Tài liệu bao gồm mục tiêu và phạm vi, cơ sở lý thuyết, kiến trúc chung, phân tích bốn phân hệ, triển khai và sử dụng, minh họa giao diện, kiểm thử, đánh giá và hướng phát triển.

Bốn phần chuyên môn được phân công gồm quản lý kết nối và giao thức; xác thực và dữ liệu; gameplay và engine vật lý; giao diện và cá nhân hóa. Tổng số đặc tả là 31 use case, phân bố 7/7/8/9; tổng số UML là 14 biểu đồ, phân bố 3/4/3/4. Bảy ảnh JavaFX được bổ sung ở phần triển khai và đánh số riêng để minh họa sản phẩm tích hợp.

Mã nguồn đối chiếu là 00bf1cc; lần kiểm tra GitHub ngày 28/10/2026 chưa có thay đổi mã ứng dụng sau phiên bản này. Minh chứng thực thi được kế thừa từ ngày 26/10/2026, gồm biên dịch, thử giao thức với server thật và kiểm tra JavaFX bằng transport giả lập. Kết quả cho thấy nhiều luồng đã hoạt động trong phạm vi thử, đồng thời xác định các lỗi trạng thái, hợp đồng packet và giao diện cần hoàn thiện. Các yêu cầu chưa nghiệm thu được nêu rõ, không được gộp thành kết quả đã đạt.

Từ khóa: lập trình mạng; JavaFX; TCP Socket; PostgreSQL; trò chơi nhiều người; UML.

'''
illustrations = '''## DANH MỤC ẢNH MINH HỌA GIAO DIỆN

| Mã hình | Nội dung | Liên hệ phân hệ |
| --- | --- | --- |
| M1 | Form đăng ký | TV2–TV4 |
| M2 | Sảnh và menu bạn bè | TV2–TV4 |
| M3 | Hội thoại chat | TV2–TV4 |
| M4 | Popup thách đấu | TV1–TV4 |
| M5 | Toast phản hồi lời mời | TV1–TV4 |
| M6 | Luyện tập solo | TV3–TV4 |
| M7 | Bàn chơi và hiển thị hướng gậy | TV1–TV3–TV4 |

Các ảnh là minh họa JavaFX từ môi trường kiểm tra ngày 26/10/2026. Điều kiện chụp và giới hạn kiểm chứng được ghi ở phần triển khai.

'''
extra_refs = '''
[7] Oracle. Java SE 17 API: Socket, ServerSocket, DataInputStream, DataOutputStream. Tài liệu nền về kết nối và đọc/ghi dữ liệu: https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/net/Socket.html và https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/io/DataInputStream.html .

[8] OpenJFX. JavaFX 17 API: Platform và AnimationTimer. https://openjfx.io/javadoc/17/javafx.graphics/javafx/application/Platform.html ; https://openjfx.io/javadoc/17/javafx.graphics/javafx/animation/AnimationTimer.html .

[9] PostgreSQL Global Development Group. PostgreSQL 17 Documentation: Transactions, Constraints và Indexes. https://www.postgresql.org/docs/17/tutorial-transactions.html ; https://www.postgresql.org/docs/17/ddl-constraints.html ; https://www.postgresql.org/docs/17/indexes.html .

[10] PostgreSQL JDBC Driver. Tài liệu JDBC và thao tác dữ liệu: https://jdbc.postgresql.org/documentation/ .

[11] PlantUML. Tài liệu Use Case, Activity, Sequence và Component: https://plantuml.com/use-case-diagram ; https://plantuml.com/activity-diagram-beta ; https://plantuml.com/sequence-diagram ; https://plantuml.com/component-diagram . Các tài liệu công nghệ dùng làm nguồn kiến thức nền; hiện trạng dự án được xác định từ mã nguồn [2–4] và minh chứng [6].
'''
result = cover + allocation + illustrations + (base / 'chapters/tong_quan.md').read_text() + '\n\n' + core + (base / 'chapters/trien_khai_va_danh_gia.md').read_text() + '\n\n# TÀI LIỆU THAM KHẢO\n' + references + extra_refs
assert len(re.findall(r'^### UC-[A-Z]+\d{2} —', result, re.M)) == 31
assert len(re.findall(r'^!\[', result, re.M)) == 21
(base / 'Bao_cao_tong_hop_nhom_LTM.md').write_text(result)
print('Group report:', len(result.split()), 'words; 31 use cases; 14 UML + 7 UI figures.')
