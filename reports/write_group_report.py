"""Compose a unified project narrative; detailed use cases/UML are reference appendices."""
from pathlib import Path
import json
import re

base = Path(__file__).resolve().parent
groups = json.loads((base / 'uml_spec.json').read_text())
diagrams = json.loads((base / 'uml_unified/manifest.json').read_text())
cover = '''# BÁO CÁO TỔNG HỢP ĐỒ ÁN

## XÂY DỰNG TRÒ CHƠI BI-A HAI NGƯỜI QUA MẠNG

Học phần: Lập trình mạng

Trường / Khoa / Lớp: [Bổ sung]

Giảng viên hướng dẫn: [Bổ sung]

Nhóm thực hiện: [Tên nhóm]

Danh sách thành viên và MSSV: [Bổ sung]

Ngày cập nhật: 29/10/2026 — múi giờ Việt Nam.

Phiên bản mã ứng dụng đối chiếu: 00bf1cc.

## TÓM TẮT

Đồ án xây dựng trò chơi bi-a hai người qua mạng bằng Java, JavaFX, TCP/UDP Socket và PostgreSQL. Ứng dụng cung cấp một hành trình sử dụng từ kết nối, đăng ký và đăng nhập tới tìm đối thủ, thi đấu, xem kết quả và tiếp tục tương tác trong sảnh. Các chức năng bạn bè, chat, luyện tập và cá nhân hóa bổ sung cho trải nghiệm đó.

Báo cáo trình bày hệ thống theo mạch từ bài toán đến giải pháp: cơ sở công nghệ, kiến trúc, quy trình hoạt động, triển khai và đánh giá. Trong từng luồng, thao tác người chơi được liên hệ với truyền thông, xử lý nghiệp vụ, lưu trữ và phản hồi giao diện. Các đặc tả và biểu đồ chi tiết được tập hợp ở phụ lục để hỗ trợ tra cứu.

Mã nguồn đối chiếu là 00bf1cc, vẫn là phiên bản mã ứng dụng trên main khi kiểm tra ngày 29/10/2026. Minh chứng thực thi từ ngày 26/10/2026 cho thấy ứng dụng biên dịch được và nhiều luồng hoạt động trong phạm vi đã thử, đồng thời xác định một số lỗi cần hoàn thiện. Báo cáo phân biệt kết quả đã quan sát với kế hoạch nghiệm thu, đặc biệt đối với đồng bộ hai client và vận hành EXE trên Windows.

Từ khóa: lập trình mạng; JavaFX; TCP Socket; PostgreSQL; trò chơi nhiều người.

'''
catalog = '## DANH MỤC HÌNH\n\n| Hình | Nội dung | Vị trí |\n| --- | --- | --- |\n'
ui_titles = ['Form đăng ký','Sảnh và menu bạn bè','Hội thoại chat','Popup thách đấu','Toast phản hồi lời mời','Bàn luyện tập solo','Bàn chơi và hướng gậy']
for i, title in enumerate(ui_titles, 1):
    catalog += f'| 5.{i} | {title} | Chương 5 |\n'
for d in diagrams:
    catalog += f'| {d["number"]} | {d["title"]} ({d["type"]}) | Phụ lục B |\n'
main = (base/'chapters/he_thong_thong_nhat.md').read_text() + '\n' + (base/'chapters/trien_khai_thong_nhat.md').read_text()
assert not re.search(r'TV[1-4]|UC-|phân công', main)
appendix = '''# PHỤ LỤC A. ĐẶC TẢ CHỨC NĂNG

Phụ lục tập hợp 31 đặc tả để đối chiếu khi cần kiểm tra một chức năng cụ thể. Các mã được giữ ổn định để tra cứu với sơ đồ; nội dung chính của báo cáo diễn giải các chức năng trong quy trình sử dụng ở Chương 4. Những nhóm dưới đây là nhóm chức năng của cùng hệ thống.

'''
for g in groups:
    appendix += f'## {g["title"]}\n\n'
    for u in g['items']:
        appendix += f'### {u["id"]} — {u["name"]}\n\n'
        appendix += f'**Tác nhân:** {u["actor"]}.\n\n**Điều kiện bắt đầu:** {u["pre"]}\n\n'
        flow = u['flow'].replace('chuyển việc ghi bền vững cho TV2', 'server kiểm tra quyền và ghi dữ liệu')
        steps = [s.strip().rstrip('.') for s in flow.split(';') if s.strip()]
        appendix += '**Diễn tiến:** ' + '. '.join(s[0].upper()+s[1:] for s in steps) + '.\n\n'
        appendix += f'**Ngoại lệ và giới hạn:** {u["alt"]}\n\n**Kết quả:** {u["post"]}\n\n'
        appendix += f'**Liên hệ mã nguồn:** {u["source"]}.\n\n'
appendix += '''# PHỤ LỤC B. BIỂU ĐỒ PHÂN TÍCH VÀ THIẾT KẾ

Các sơ đồ hỗ trợ giải thích những luồng đã trình bày trong báo cáo. Use Case thể hiện phạm vi chức năng; Activity mô tả các bước và nhánh điều kiện; Sequence làm rõ tương tác theo thời gian; Component biểu diễn phụ thuộc giữa các thành phần. Chú thích và nhánh lỗi phản ánh mã hiện tại, không được hiểu là những cải tiến đã hoàn thành.

'''
for d in diagrams:
    appendix += f'## {d["number"]}. {d["title"]}\n\n'
    appendix += f'Loại sơ đồ: {d["type"]}. Đối chiếu Phụ lục A: '+', '.join(d['use_cases'])+'.\n\n'
    appendix += f'![Hình {d["number"]} — {d["title"]}; {d["type"]}, đối chiếu mã 00bf1cc.]({d["path"]})\n\n'
result = cover + catalog + '\n' + main + '\n' + appendix + '\n' + (base/'chapters/tai_lieu_tham_khao.md').read_text()
assert not re.search(r'TV[1-4]|Thành viên phụ trách|phân công', result)
assert len(re.findall(r'^# CHƯƠNG [1-7]\.', result, re.M)) == 7
assert len(re.findall(r'^### UC-[A-Z]+\d{2} —', result, re.M)) == 31
assert len(re.findall(r'^!\[', result, re.M)) == 21
(base/'Bao_cao_tong_hop_nhom_LTM.md').write_text(result)
print('Unified report:', len(result.split()), 'words; 7 narrative chapters; UC and UML in appendices.')
