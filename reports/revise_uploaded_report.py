"""Edit the user's DOCX in place structurally, preserving its layout and custom content.

Input is supplied explicitly; the unedited uploaded document is not committed.
UML sources under uml_uploaded/ are regenerated without chart titles. The script
renders these vector drawings at each original image's canvas size before embedding.
"""
from argparse import ArgumentParser
from pathlib import Path
from zipfile import ZipFile
from collections import Counter
from io import BytesIO
import hashlib
import re
from lxml import etree
from PIL import Image
import cairosvg

parser = ArgumentParser()
parser.add_argument('input', type=Path)
parser.add_argument('--output', type=Path, required=True)
args = parser.parse_args()
base = Path(__file__).resolve().parent
W = 'http://schemas.openxmlformats.org/wordprocessingml/2006/main'
NS = {'w': W}
XML_SPACE = '{http://www.w3.org/XML/1998/namespace}space'

def paragraph_text(p):
    return ''.join(p.xpath('.//w:t/text()', namespaces=NS))

def replace_range(p, start, end, replacement):
    """Keep runs, paragraph properties, hyperlinks and bookmarks in their positions."""
    nodes = p.xpath('.//w:t', namespaces=NS)
    cursor = 0
    inserted = False
    for node in nodes:
        text = node.text or ''
        left, right = cursor, cursor + len(text)
        cursor = right
        if right <= start or left >= end:
            continue
        prefix = text[:max(0, start-left)]
        suffix = text[max(0, end-left):] if end < right else ''
        node.text = prefix + (replacement if not inserted else '') + suffix
        node.set(XML_SPACE, 'preserve')
        inserted = True
    assert inserted

def replace_whole(p, replacement):
    current = paragraph_text(p)
    if current != replacement:
        replace_range(p, 0, len(current), replacement)

rewrites = {
 'TV1: [Họ tên — MSSV] · TV2: [Họ tên — MSSV]': 'Lộc — [MSSV] · Minh — [MSSV]',
 'TV3: [Họ tên — MSSV] · TV4: [Họ tên — MSSV]': 'Trung — [MSSV] · Mạnh — [MSSV]',
 'TV2 → TV4, qua TV1': 'Dữ liệu hồ sơ → giao diện, qua kết nối mạng',
 'TV1 → TV3 và TV4': 'Thông tin phòng → bàn chơi và màn hình trước trận',
 'TV4/TV3 → TV1 → TV3/TV4 đối thủ': 'Thao tác đánh → truyền thông → mô phỏng và hiển thị ở đối thủ',
 'TV3/TV1 → TV2 → TV4': 'Kết quả trận → lưu trữ → màn hình kết quả',
 'TV4 → TV2, qua TV1': 'Thao tác giao diện → yêu cầu mạng → xử lý dữ liệu',
 'TV1, HUD của TV4': 'Đo độ trễ và hiển thị trên HUD',
 'TV2, TV4; có sai lệch parser đã ghi nhận': 'Lưu tùy chọn và cập nhật giao diện; còn sai lệch parser',
 'Chuyển việc ghi bền vững cho TV2.': 'Gửi yêu cầu để server kiểm tra quyền và lưu dữ liệu.',
 'TV1–TV3': 'Giao thức và trạng thái trận đấu',
 'TV1, phản hồi UI của TV4': 'Vòng đời lời mời và phản hồi giao diện',
 'TV1–TV2–TV4': 'Thông điệp, dữ liệu và phản hồi giao diện',
 'TV2–TV4': 'Kiểm tra dữ liệu và giao diện',
 'TV4': 'Cập nhật giao diện',
 'Bản chuyên sâu theo phân công đã chốt trước đó': 'Tài liệu đặc tả kỹ thuật chi tiết',
 'Ngôn ngữ mô hình hóa; báo cáo dùng bốn loại biểu đồ đã phân công': 'Ngôn ngữ mô hình hóa; báo cáo dùng Use Case, Activity, Sequence và Component',
 'Phối hợp xử lý': 'Thành phần cần hoàn thiện',
}
paragraphs_by_prefix = {
 'Ma trận này là ranh giới phối hợp trong báo cáo.': 'Ma trận mô tả đường đi của dữ liệu giữa các thành phần trong hệ thống. Một lớp có thể tham gia nhiều luồng xử lý; chẳng hạn GameScene vừa trình bày HUD vừa gọi engine vật lý. Sự liên kết được xác định theo chức năng và trạng thái nghiệp vụ để các thao tác từ giao diện đến server và cơ sở dữ liệu được xử lý nhất quán.',
 'TV1 phụ trách đường đi dữ liệu': 'Phân hệ kết nối và giao thức đảm nhiệm truyền dữ liệu giữa client và server, khám phá máy chủ, định dạng packet, đo độ trễ, ghép trận và quản lý vòng đời kết nối. Các lớp chính là NetworkClient, Packet, PacketType, ServerMain, ClientHandler và LobbyManager. Yêu cầu nhận được được chuyển tới phần xác thực hoặc xử lý nghiệp vụ; kết quả được gửi lại để giao diện cập nhật.',
 'Các mối liên hệ xuyên phân hệ: sau UC-C01': 'Sau khi thiết lập kết nối (UC-C01), ứng dụng có thể xử lý đăng ký hoặc đăng nhập (UC-D01/UC-D02). Ghép trận và thách đấu (UC-C05/UC-C06) tạo ngữ cảnh khởi tạo bàn (UC-G01), còn UC-C03 vận chuyển dữ liệu giữa các thành phần. Ngắt kết nối (UC-C07) có thể kích hoạt ghi kết quả (UC-D04) theo trạng thái phòng hiện tại.',
 'TV2 phụ trách xác thực': 'Phân hệ xác thực và dữ liệu kiểm tra tài khoản, quyền cập nhật hồ sơ và thực hiện lưu trữ qua JDBC. AuthManager băm mật khẩu SHA-256 cùng salt ngẫu nhiên 16 byte. DatabaseManager thao tác các bảng players, match_history, friendships và messages trong PostgreSQL. Kết quả xử lý được trả về client để giao diện hiển thị trạng thái phù hợp.',
 'Bốn biểu đồ của TV2': 'Phân hệ xác thực và dữ liệu được mô tả bằng Use Case tổng thể, Activity đăng ký, Activity ghi kết quả và Sequence đăng nhập. Các sơ đồ thể hiện cách yêu cầu được kiểm tra, truy vấn và cập nhật; cấu trúc lưu trữ được tóm tắt trong bảng dưới đây.',
 'TV3 phụ trách bàn chơi': 'Phân hệ gameplay và vật lý quản lý bàn chơi, trạng thái bi, hướng ngắm, lực, xoáy, cú đánh, va chạm và xử lý lượt. Ball biểu diễn x/y, vx/vy, spin và các trạng thái active/inPocket/isSinking. PhysicsEngine có sáu lỗ và xử lý 10 bước con trong mỗi update. Các hệ số hiện có gồm ma sát 0,985, phục hồi băng 0,78 và phục hồi bi–bi 0,96.',
 'TV4 phụ trách tương tác': 'Phân hệ giao diện và cá nhân hóa tiếp nhận thao tác, điều hướng màn hình, trình bày trạng thái và áp dụng tùy chọn người chơi. Các lớp tiêu biểu gồm SceneManager, LoginScene, RegisterScene, LobbyScene, PreGameScene, GameScene, ResultScene, LeaderboardScene, SettingsDialog, AvatarManager, CueSkinManager và SoundManager. Giao diện sử dụng phản hồi nghiệp vụ từ server; việc xác thực và lưu dữ liệu vẫn được xử lý tại các thành phần tương ứng.',
 'Component 4.4 là cách nhóm logic': 'Component 4.4 mô tả các nhóm lớp và phụ thuộc trong ứng dụng; các hộp không đại diện cho microservice hoặc tiến trình triển khai riêng. NetworkClient kết nối giao diện với server. Các yêu cầu lưu hồ sơ được xử lý ở phía server, trong khi GameScene kết hợp HUD với engine vật lý để thể hiện trạng thái bàn chơi.',
 'Theo phân công, TV1 phụ trách': 'Hệ thống kết hợp kết nối và giao thức, xác thực và dữ liệu, gameplay và vật lý, giao diện và cá nhân hóa thành một quy trình sử dụng liên tục. Tổng cộng 31 đặc tả và 14 biểu đồ UML được đối chiếu với mã nguồn. Phần triển khai và kiểm thử đánh giá sự phối hợp giữa các thành phần, từ thao tác của người chơi đến xử lý tại server, lưu trữ và phản hồi trên màn hình.',
 'Thông tin hành chính cần nhóm điền': 'Thông tin hành chính cần hoàn thiện trước khi nộp gồm trường/khoa/lớp, giảng viên, tên nhóm, họ tên đầy đủ và MSSV của các thành viên. Những trường còn thiếu được để nhãn bổ sung. Nếu trường có mẫu bìa hoặc quy định riêng, có thể điều chỉnh trong Word và giữ nguyên cấu trúc chuyên môn của báo cáo.',
}

with ZipFile(args.input) as source:
    original = {name: source.read(name) for name in source.namelist()}
    infos = source.infolist()
root = etree.fromstring(original['word/document.xml'])
# Names in the allocation table were supplied by the user and remain untouched.
# In the requirements table, describe technical components instead of authors.
for table in root.xpath('//w:tbl', namespaces=NS):
    if 'Phân hệ chịu trách nhiệm' in ''.join(table.xpath('.//w:t/text()', namespaces=NS)):
        replacements = {
            'Phân hệ chịu trách nhiệm': 'Thành phần liên quan',
            'Lộc': 'Kết nối và giao thức', 'Minh': 'Xác thực và dữ liệu',
            'Trung': 'Gameplay và vật lý', 'Mạnh': 'Giao diện và cá nhân hóa',
            'Phối hợp Lộc và Trung': 'Giao thức và trạng thái trận',
            'Phối hợp cả nhóm': 'Toàn hệ thống',
        }
        for p in table.xpath('.//w:p', namespaces=NS):
            if paragraph_text(p) in replacements:
                replace_whole(p, replacements[paragraph_text(p)])

changed = 0
for p in root.xpath('//w:p', namespaces=NS):
    old = paragraph_text(p)
    new = rewrites.get(old, old)
    for prefix, replacement in paragraphs_by_prefix.items():
        if old.startswith(prefix):
            new = replacement
            break
    if old.startswith('Thành viên phụ trách:'):
        new = 'Phạm vi chức năng: ' + old.split('Phạm vi:', 1)[1].strip()
    if new != old:
        replace_whole(p, new)
        changed += 1
    # Small deletions preserve the surrounding run formatting.
    for pattern, replacement in [
        (r'TV[1-4] — ', ''), (r'TV[1-4] • ', ''),
        (r'use case của thành viên', 'use case của phân hệ'),
    ]:
        current = paragraph_text(p)
        for match in reversed(list(re.finditer(pattern, current))):
            replace_range(p, match.start(), match.end(), replacement)
            changed += 1

assert not re.search(r'\bTV\s*[1-4]\b', '\n'.join(root.xpath('//w:t/text()', namespaces=NS)))
changes = {'word/document.xml': etree.tostring(root, xml_declaration=True, encoding='UTF-8', standalone=True)}
if 'docProps/core.xml' in original:
    core = etree.fromstring(original['docProps/core.xml'])
    for node in core:
        if node.text and re.search(r'TV[1-4]', node.text):
            node.text = 'Báo cáo hệ thống bi-a qua mạng; đối chiếu mã ứng dụng 00bf1cc.'
    changes['docProps/core.xml'] = etree.tostring(core, xml_declaration=True, encoding='UTF-8', standalone=True)

# Exact hashes ensure that only diagrams with known, matching technical sources change.
hashes = {hashlib.sha256(p.read_bytes()).hexdigest(): p.stem for p in (base/'uml').glob('*.png')}
image_changes = []
SVG = 'http://www.w3.org/2000/svg'
for name, data in original.items():
    if not name.startswith('word/media/'):
        continue
    identity = hashes.get(hashlib.sha256(data).hexdigest())
    if identity is None:
        continue
    svg_path = base/'uml_uploaded'/f'{identity}.svg'
    vector = etree.fromstring(svg_path.read_bytes())
    viewbox = [float(x) for x in vector.get('viewBox').split()]
    width, height = Image.open(BytesIO(data)).size
    scale = min(width/viewbox[2], height/viewbox[3])
    canvas = etree.Element('{'+SVG+'}svg', nsmap={None:SVG}, width=str(width), height=str(height), viewBox=f'0 0 {width} {height}')
    etree.SubElement(canvas, '{'+SVG+'}rect', width='100%', height='100%', fill='white')
    vector.set('x',str((width-viewbox[2]*scale)/2))
    vector.set('y',str((height-viewbox[3]*scale)/2))
    vector.set('width',str(viewbox[2]*scale));vector.set('height',str(viewbox[3]*scale))
    canvas.append(vector)
    png = cairosvg.svg2png(bytestring=etree.tostring(canvas), output_width=width, output_height=height)
    assert Image.open(BytesIO(png)).size == (width,height)
    changes[name] = png
    (base/'uml_uploaded'/f'{identity}.png').write_bytes(png)
    image_changes.append(name)
assert len(image_changes)==14, image_changes

args.output.parent.mkdir(parents=True, exist_ok=True)
with ZipFile(args.output, 'w') as destination:
    for info in infos:
        destination.writestr(info, changes.get(info.filename, original[info.filename]))
with ZipFile(args.output) as destination:
    assert destination.namelist() == list(original)
    untouched = [n for n in original if n not in changes]
    assert all(destination.read(n)==original[n] for n in untouched)
newroot=etree.fromstring(changes['word/document.xml'])
oldroot=etree.fromstring(original['word/document.xml'])
for path in ['//w:p','//w:tbl','//w:sectPr','//w:drawing','//w:bookmarkStart','//w:hyperlink']:
    assert len(newroot.xpath(path,namespaces=NS))==len(oldroot.xpath(path,namespaces=NS)),path
# No paragraph/table/drawing/section properties are reconstructed.
for path in ['//w:pPr','//w:tblPr','//w:sectPr','//w:drawing']:
    before=[etree.tostring(x) for x in oldroot.xpath(path,namespaces=NS)]
    after=[etree.tostring(x) for x in newroot.xpath(path,namespaces=NS)]
    assert before==after,path
print(f'Edited {changed} text passages; replaced 14 UML drawings; preserved 7 UI screenshots.')
print('PASS: same paragraphs/tables/sections/drawing geometry/bookmarks/links; unchanged styles, numbering, headers/footers and other package parts.')
print(args.output)
