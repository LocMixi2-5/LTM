"""Revise the exact uploaded report against source 89f7926 without changing app code."""
from pathlib import Path
from zipfile import ZipFile
from io import BytesIO
from copy import deepcopy
from difflib import SequenceMatcher
import argparse,hashlib,re,json
from lxml import etree as E
from PIL import Image
import cairosvg

ap=argparse.ArgumentParser();ap.add_argument('input',type=Path);ap.add_argument('--output',type=Path,required=True);args=ap.parse_args()
b=Path(__file__).resolve().parent
assert hashlib.sha256(args.input.read_bytes()).hexdigest()=='f7e3541d0a56a307bcd30ee2c81604173c74db95ee602a6e99e72dcfa16db917'
W='http://schemas.openxmlformats.org/wordprocessingml/2006/main';ns={'w':W};space='{http://www.w3.org/XML/1998/namespace}space'
def q(s):return '{'+W+'}'+s
def txt(p):return ''.join(p.xpath('.//w:t/text()',namespaces=ns))
def edit_range(p,start,end,value):
 nodes=p.xpath('.//w:t',namespaces=ns);offset=0;done=False
 if start==end:
  for node in nodes:
   t=node.text or ''
   if offset+len(t)>=start:
    n=start-offset;node.text=t[:n]+value+t[n:];node.set(space,'preserve');return
   offset+=len(t)
  raise AssertionError('Insertion outside paragraph')
 for node in nodes:
  t=node.text or '';l,r=offset,offset+len(t);offset=r
  if r<=start or l>=end:continue
  node.text=t[:max(0,start-l)]+(value if not done else '')+(t[max(0,end-l):] if end<r else '')
  node.set(space,'preserve');done=True
 assert done

def settext(p,value):
 old=txt(p)
 for tag,i,j,a,c in reversed(SequenceMatcher(None,old,value,autojunk=False).get_opcodes()):
  if tag!='equal':edit_range(p,i,j,value[a:c])
 assert txt(p)==value

def newp(value,style='BodyText'):
 p=E.Element(q('p'));pr=E.SubElement(p,q('pPr'));E.SubElement(pr,q('pStyle'),{q('val'):style})
 r=E.SubElement(p,q('r'));t=E.SubElement(r,q('t'));t.text=value;t.set(space,'preserve');return p

def insert_after(node,items):
 for item in reversed(items):node.addnext(item)

with ZipFile(args.input) as z:infos=z.infolist();files={n:z.read(n) for n in z.namelist()}
root=E.fromstring(files['word/document.xml']);body=root.find('w:body',ns);parts=list(body)
updates={
8:'Ngày cập nhật báo cáo: 30/10/2026 — múi giờ Việt Nam.',
9:'Phiên bản mã ứng dụng đối chiếu: 89f7926c13345bcd21fbaccaf7d8e13fe3fdb562.',
38:'Giao thức nhị phân yêu cầu hai đầu thống nhất kiểu, thứ tự và số trường. Bản 89f7926 đã tách SETTINGS_RESPONSE thành một boolean ở client, khớp bộ ghi server. CHAT_HISTORY_RESPONSE hiện gồm friendUsername và historyData; cả client và server phải được cập nhật cùng phiên bản để đọc đúng payload.',
48:'Mật khẩu được băm bằng SHA-256 cùng salt ngẫu nhiên. Salt giúp các mật khẩu giống nhau không luôn có cùng giá trị lưu, nhưng băm nhanh còn hạn chế khi chống dò mật khẩu. TLS và thuật toán chuyên dụng như Argon2id hoặc bcrypt vẫn là hướng hoàn thiện, chưa được triển khai.',
61:'Giao thức hiện có 47 mã loại. GameRoom ghi nhận sẵn sàng theo từng người chơi hợp lệ bằng readyPlayers; shotInProgress chặn cú đánh lặp và mẫu ngắm/đặt bi trong khi đang có cú đánh. Các cơ chế này bảo vệ một số chuyển tiếp, chưa tạo thành máy trạng thái bảo đảm mọi tình huống đồng bộ.',
102:'Ngoại lệ: Payload sai hoặc stream kết thúc có thể gây IOException. SETTINGS_RESPONSE đã thống nhất một boolean; CHAT_HISTORY_RESPONSE đổi sang hai chuỗi nên không bảo đảm tương thích với client/server của bản cũ.',
132:'Hậu điều kiện: Có MATCH_FOUND hoặc thông báo không tạo trận. Phản hồi sai tên bị bỏ qua trước khi hủy timer; lời mời vẫn có thể hết hạn hoặc được trả lời hợp lệ.',
156:'Email được trim và chuyển thường; đăng nhập tra theo LOWER(username) hoặc LOWER(email). RegisterScene, AuthManager và DatabaseManager đã dùng regex cho phép đuôi miền dài; server từ chối email sai định dạng. Kiểm tra cú pháp chưa xác minh quyền sở hữu email và ứng dụng chưa có TLS.',
157:'EXP thắng trận là 100; level = floor(EXP/200)+1. DatabaseManager giữ một Connection chung và recordMatchResult chưa có transaction bao trọn các câu SQL. Sơ đồ ghi kết quả ở Hình 5.3 thể hiện các cập nhật tuần tự và giới hạn khi lỗi giữa chừng.',
172:'Ngoại lệ: Email sai định dạng, email/username trùng hoặc mật khẩu quá ngắn bị từ chối. Kiểm tra cú pháp ở client và server không thay thế xác minh email/OTP.',
191:'Ngoại lệ: Không đủ cấp hoặc lỗi DB trả false. Client đã đọc SETTINGS_RESPONSE đúng một boolean; SettingsDialog vẫn cập nhật session/đóng dialog trước khi có xác nhận ghi DB hoàn chỉnh.',
231:'Ngoại lệ: Lịch sử trả kèm friendUsername và chỉ áp dụng khi trùng người đang mở hội thoại. Chưa có message ID hoặc request ID để phân biệt mọi lần tải lịch sử, đặc biệt nhiều yêu cầu cho cùng một người.',
267:'Ngoại lệ: READY lặp của một người không làm tăng số người sẵn sàng; chỉ hai người hợp lệ trong phòng mới đủ điều kiện bắt đầu. Cùng seed chưa bảo đảm hai mô phỏng luôn kết thúc đồng nhất.',
286:'Ngoại lệ: Sai lượt, đang có cú đánh hoặc tham số không hữu hạn thì server bỏ qua. Chưa có kiểm tra đầy đủ miền lực tương đối/xoáy và tọa độ trong mọi đường gửi.',
297:'Ngoại lệ: Cú kéo quá ngắn không tạo cú đánh; client và server lọc giá trị không hữu hạn. Server từ chối lực âm và cú đánh khi shotInProgress đang bật, nhưng chưa chặn mọi giá trị hữu hạn vượt giới hạn giao diện.',
325:'Ngoại lệ: Timer lượt bị hủy khi có cú đánh; chưa có timeout riêng chờ hoàn tất mô phỏng hoặc ACK hai client. onPhysicsDone chưa bắt buộc phải có cú đánh đang diễn ra; gửi sớm có thể chuyển lượt.',
372:'Ngoại lệ: Regex hiện cho phép đuôi miền dài hơn 7 chữ cái. Client và server đều kiểm tra cú pháp email; kiểm tra này chưa xác minh quyền sở hữu địa chỉ.',
382:'Ngoại lệ: Khi menu mở, dữ liệu mới được hoãn; nếu chuỗi cập nhật là A→B→A thì dữ liệu chờ B được xóa, nên đóng menu vẫn giữ A mới nhất. Kiểm tra này đã xác nhận bằng fixture JavaFX.',
383:'Hậu điều kiện: Sảnh/menu hiển thị dữ liệu mới nhất trong các trường hợp refresh đã kiểm tra; chưa nghiệm thu mọi tình huống đồng thời.',
392:'Ngoại lệ: Hai tin cố ý giống nhau trong cùng phút được giữ thành hai dòng. Lịch sử khác friendUsername bị bỏ qua; vẫn chưa có ID yêu cầu/tin nhắn để xử lý hoàn toàn phản hồi lặp hoặc muộn cho cùng hội thoại.',
429:'Ngoại lệ: Skin khóa bị hạn chế và server kiểm tra lại cấp. Parser SETTINGS_RESPONSE đã sửa; việc lưu và đọc lại mọi tùy chọn qua lần đăng nhập sau vẫn cần nghiệm thu riêng.',
464:'Menu bạn bè cung cấp ngữ cảnh thao tác trong sảnh. Bản 89f7926 đã sửa chuỗi refresh A→B→A để giữ dữ liệu mới nhất khi menu đóng; bài kiểm tra JavaFX với transport giả lập đã xác nhận trường hợp này.',
468:'Hội thoại hiển thị tin nhắn và lịch sử. Bản mới giữ hai tin có cùng nội dung/thời gian và đối chiếu friendUsername trước khi áp dụng lịch sử. Ảnh là minh họa bố cục; kiểm thử không bao quát mọi trường hợp tải lại cùng hội thoại.',
491:'Đợt đối chiếu ngày 30/10/2026 dùng mã 89f7926, biên dịch sạch 9 file Java server và 26 file client bằng JDK 21 với --release 17 trên Linux. Bài giao thức dùng nguyên các lớp server qua harness TCP loopback 12355 và PostgreSQL 17 phát triển cục bộ; cổng ứng dụng chính vẫn là 12345. Bài UI dùng JavaFX/Xvfb với transport giả lập. Không chạy EXE Windows hoặc chơi ván hoàn chỉnh trên hai UI thật.',
497:'Các kiểm tra ngày 30/10 xác nhận những sửa đổi về sẵn sàng, chặn giá trị không hữu hạn/cú đánh lặp, lời mời, email, lịch sử chat và parser settings trong phạm vi đã thử. Sự kết hợp giữa đọc mã, biên dịch, bài giao thức và fixture UI giúp phân biệt hành vi đã xác nhận với yêu cầu còn cần nghiệm thu.',
498:'Hai giới hạn còn tái hiện là PHYSICS_DONE có thể được gửi trước cú đánh và lực hữu hạn vượt giới hạn giao diện vẫn được relay. Ngoài ra chưa có token lượt, snapshot/ACK chốt 16 bi, transaction ghi kết quả hoặc nghiệm thu hai máy Windows. Vì vậy chưa có cơ sở tuyên bố ứng dụng hết mọi lỗi bất đồng bộ.',
505:'Các lỗi cũ về parser settings, READY theo gói, NaN, cú đánh lặp khi đang mô phỏng, phản hồi lời mời sai tên, email và một số tình huống chat/refresh đã có sửa đổi trong 89f7926 và được kiểm tra lại trong phạm vi nêu ở Phần 9. Ưu tiên tiếp theo là kiểm tra đầy đủ miền giá trị lực/xoáy/tọa độ, chỉ nhận PHYSICS_DONE khi có cú đánh hợp lệ và bổ sung định danh yêu cầu cho lịch sử chat.',
}
for index,value in updates.items():settext(parts[index],value)
# Keep source descriptions coherent with changed wire payload and physics behavior.
insert_after(parts[60],[newp('CHAT_HISTORY_RESPONSE hiện gửi hai chuỗi theo thứ tự friendUsername, historyData. Số mã packet không đổi nhưng payload đã đổi; cần dùng client/server cùng phiên bản. Chưa có handshake thương lượng phiên bản. Comment cũ trong PacketType không thay thế bộ ghi/đọc thực tế.')])
insert_after(parts[250],[newp('Bản 89f7926 chỉ phản xạ băng khi bi đang hướng vào cạnh va chạm, giảm cả hai thành phần vận tốc theo hệ số băng và tính ảnh hưởng xoáy ngang theo tốc độ. Đường ngắm mở rộng cũng xét spinX và bỏ các giao điểm quá gần/không hợp lệ. Các thay đổi này đã đối chiếu mã và biên dịch, chưa chứng minh quỹ đạo dự báo luôn khớp mô phỏng thực tế.')])
insert_after(parts[420],[newp('ResultScene hiện hiển thị kết quả, EXP và nút về sảnh; khung thông báo mở khóa skin sau trận đã được bỏ. Quyền dùng skin theo level vẫn được server kiểm tra khi cập nhật tùy chọn.')])
insert_after(parts[458],[newp('Các ảnh giao diện dưới đây được chụp từ bản 00bf1cc ngày 26/10/2026 trên JavaFX/Linux với dữ liệu và transport giả lập. Chúng minh họa bố cục, không phải ảnh nghiệm thu đầu cuối của bản mới. Đợt kiểm tra 89f7926 ngày 30/10 có log riêng; chưa nghiệm thu EXE trên Windows.')])
insert_after(parts[491],[newp('Minh chứng mới nằm tại reports/evidence/89f7926/: build.txt, protocol.txt và ui.txt. Log 26/10 của 00bf1cc chỉ dùng đối chiếu lịch sử. Lần chạy UI đầu của đợt mới dừng do harness tìm sai lớp khai báo phương thức private; đã sửa harness và chạy lại, giữ ui-first-run.txt. Mã thoát 0 xác nhận script hoàn tất, kể cả các assertion tái hiện giới hạn (FINDING), không có nghĩa mọi chức năng đều đạt nghiệm thu.')])
# Update packet table cells without reconstructing other tables.
for p in root.xpath('//w:p',namespaces=ns):
 t=txt(p)
 if t=='Lưu tùy chọn và cập nhật giao diện; còn sai lệch parser':settext(p,'Lưu tùy chọn; parser đã thống nhất một boolean')
 if t=='Thông tin hiển thị trên màn hình chơi: người chơi, lượt và trạng thái':settext(p,'Heads-Up Display: khung thông tin trên màn hình chơi, gồm người chơi, lượt và trạng thái')
 if 'hướng hoàn thiện,.' in t:settext(p,t.replace('hướng hoàn thiện,.','hướng hoàn thiện.'))
 if t.strip()=='.':settext(p,'')
 if 'Activity 2.3' in t:settext(p,txt(p).replace('Activity 2.3','Hình 5.3'))
# Number all captions by actual document section, keeping the figures in place.
manifest=json.loads((b/'uml_manifest.json').read_text())
numbers=[f'4.{i}' for i in range(1,4)]+[f'5.{i}' for i in range(1,5)]+[f'6.{i}' for i in range(1,4)]+[f'7.{i}' for i in range(1,5)]+[f'8.{i}' for i in range(1,8)]
titles=[d['title'].split(' • ',1)[1] for d in manifest]+['Form đăng ký','Sảnh và menu bạn bè','Hội thoại chat','Popup thách đấu','Toast kết quả lời mời','Bàn luyện tập solo','Bàn chơi nhận mẫu ngắm']
caps=[p for p in root.xpath('//w:p',namespaces=ns) if txt(p).startswith('Hình ')]
assert len(caps)==21
for p,n,title in zip(caps,numbers,titles):settext(p,f'Hình {n} — {title}.')

# Revised test table: each result is tied to a fresh execution log.
table=parts[493];assert table.tag==q('tbl')
rows=[
['Mã','Kịch bản kiểm tra','Kết quả trong phạm vi đã thử','Minh chứng'],
['KT01','Biên dịch sạch source','Đạt: 9 file server, 26 file client; JDK 21, --release 17. Chưa chạy EXE Windows.','build.txt'],
['KT02','Đăng ký/đăng nhập và email','Đạt: đăng nhập khác hoa/thường; server từ chối email sai cú pháp và chấp nhận TLD dài.','protocol.txt'],
['KT03','Lời mời không hợp lệ','Đạt: chặn tên rỗng, tự mời, offline; tra tên trim/khác hoa thường.','protocol.txt'],
['KT04','Chấp nhận/từ chối và ghép phòng','Đạt: tạo phòng đúng luồng; người mời được loại khỏi hàng chờ.','protocol.txt'],
['KT05','READY lặp','Đạt: hai READY từ một người chưa mở trận; người còn lại sẵn sàng mới bắt đầu.','protocol.txt'],
['KT06','Ngắm liên tục qua TCP','Đạt relay 60 mẫu 34 byte, không echo. Chưa đo độ mượt hai UI hoặc jitter.','protocol.txt'],
['KT07','Góc NaN/Infinity, lực âm, cú đánh lặp','Đạt: các đầu vào đã thử bị chặn; không relay cú đánh lặp hoặc ngắm khi shotInProgress.','protocol.txt'],
['KT08','Hoàn tất mô phỏng và đổi lượt','Đạt luồng PHYSICS_DONE sau cú đánh. Chưa có chốt snapshot/ACK; còn G01 bên dưới.','protocol.txt'],
['KT09','Payload settings và lịch sử chat','Đạt: settings một boolean, không đọc lấn gói sau; lịch sử kèm tên bạn và dữ liệu.','protocol.txt; ui.txt'],
['KT10','Timer lời mời và ngắt kết nối','Đạt: hết hạn khoảng 15 giây; phản hồi sai tên không hủy timer; xử lý đối tác rời.','protocol.txt'],
['KT11','Form email và refresh A→B→A','Đạt trong fixture JavaFX: chấp nhận TLD dài; đóng menu giữ A mới nhất.','ui.txt'],
['KT12','Tin trùng và lịch sử khác bạn','Đạt trong fixture: giữ hai tin giống nhau; bỏ lịch sử khác friendUsername, nhận đúng người.','ui.txt'],
['KT13','Popup/toast/solo và hiển thị mẫu ngắm','Đạt các callback JavaFX đã thử; không tương đương một ván đầy đủ trên hai UI thật.','ui.txt'],
]
oldrows=table.findall('w:tr',ns);templates=[deepcopy(oldrows[0]),deepcopy(oldrows[1])]
for r in oldrows:table.remove(r)
widths=[680,2350,4500,1600]
grid=table.find('w:tblGrid',ns)
if grid is not None:
 for c in list(grid):grid.remove(c)
 for w in widths:E.SubElement(grid,q('gridCol'),{q('w'):str(w)})
for ri,row in enumerate(rows):
 tr=deepcopy(templates[0 if ri==0 else 1])
 for cell,value,w in zip(tr.findall('w:tc',ns),row,widths):
  pr=cell.find('w:tcPr',ns)
  if pr is not None:
   wd=pr.find('w:tcW',ns)
   if wd is not None:wd.set(q('w'),str(w));wd.set(q('type'),'dxa')
  paragraphs=cell.findall('w:p',ns)
  p=paragraphs[0]
  for extra in paragraphs[1:]:cell.remove(extra)
  if txt(p):settext(p,value)
  else:p.append(newp(value)[-1])
 table.append(tr)
insert_after(table,[
 newp('Giới hạn còn lại của bản 89f7926','Heading3'),
 newp('G01 — Đã tái hiện: người đang có lượt gửi PHYSICS_DONE trước khi có cú đánh vẫn làm đổi lượt. G02 — Đã tái hiện: lực hữu hạn 1000 được relay dù MAX_FORCE của giao diện là 25. Minh chứng: các dòng FINDING trong protocol.txt. Đây là giới hạn cần sửa, không phải kết quả nghiệm thu đạt.'),
 newp('Qua đọc mã, hệ thống còn thiếu token/sequence cho lượt và cú đánh, snapshot/ACK chốt 16 bi, request ID cho nhiều lần tải lịch sử cùng người, và transaction bao trọn ghi kết quả. Chưa có bài thực nghiệm khẳng định mức ảnh hưởng của mọi tình huống này. Các thay đổi phản xạ băng/đường ngắm và ResultScene mới được đối chiếu mã, chưa nghiệm thu trải nghiệm đầy đủ.')])
for p in parts[495].xpath('.//w:p',namespaces=ns):
 if txt(p)=='Cần chạy sau khi sửa lỗi trạng thái':settext(p,'Chưa thử jitter/ngắt mạng trong ván; cần kiểm tra G01/G02')
# Add compact, explicit sources at the end without rearranging the ten sections.
ref=newp('Tài liệu tham khảo và minh chứng','Heading1');body.insert(len(body)-1,ref)
for value in [
 'Mã ứng dụng: https://github.com/LocMixi2-5/LTM/tree/89f7926c13345bcd21fbaccaf7d8e13fe3fdb562 .',
 'Minh chứng ngày 30/10/2026: reports/evidence/89f7926/README.md, build.txt, protocol.txt, ui.txt và ui-first-run.txt; kèm script/harness tái lập. Nguồn hình cập nhật: reports/uml_89f7926/.',
 'Minh chứng lịch sử và nguồn ảnh ngày 26/10/2026: reports/evidence/00bf1cc/README.md; reports/images/README.md. Không dùng log cũ làm kết quả chạy mới.',
 'Tài liệu nền: Java SE 17 API (docs.oracle.com/en/java/javase/17/docs/api/), JavaFX 17 API (openjfx.io/javadoc/17/), PostgreSQL Documentation (postgresql.org/docs/), PostgreSQL JDBC (jdbc.postgresql.org/documentation/) và PlantUML (plantuml.com).']:
 insert_after(ref,[newp(value)]);ref=ref.getnext()

# Replace only matching technical figures; retain screenshot bytes and drawing extents.
changes={};SVG='http://www.w3.org/2000/svg'
hashes={hashlib.sha256(p.read_bytes()).hexdigest():p.stem for p in (b/'uml_uploaded').glob('*.png')}
count=0
for name,data in files.items():
 if not name.startswith('word/media/'):continue
 identity=hashes.get(hashlib.sha256(data).hexdigest())
 if not identity:continue
 vector=E.fromstring((b/'uml_89f7926'/f'{identity}.svg').read_bytes());v=[float(x) for x in vector.get('viewBox').split()]
 width,height=Image.open(BytesIO(data)).size;scale=min(width/v[2],height/v[3])
 canvas=E.Element('{'+SVG+'}svg',nsmap={None:SVG},width=str(width),height=str(height),viewBox=f'0 0 {width} {height}')
 E.SubElement(canvas,'{'+SVG+'}rect',width='100%',height='100%',fill='white')
 for key,val in {'x':(width-v[2]*scale)/2,'y':(height-v[3]*scale)/2,'width':v[2]*scale,'height':v[3]*scale}.items():vector.set(key,str(val))
 canvas.append(vector);png=cairosvg.svg2png(bytestring=E.tostring(canvas),output_width=width,output_height=height)
 changes[name]=png;(b/'uml_89f7926'/f'{identity}.png').write_bytes(png);count+=1
assert count==14
changes['word/document.xml']=E.tostring(root,encoding='UTF-8',xml_declaration=True,standalone=True)
core=E.fromstring(files['docProps/core.xml'])
for node in core:
 if E.QName(node).localname in ['subject','description']:node.text='Đối chiếu mã ứng dụng 89f7926; kiểm tra ngày 30/10/2026.'
changes['docProps/core.xml']=E.tostring(core,encoding='UTF-8',xml_declaration=True,standalone=True)
args.output.parent.mkdir(exist_ok=True,parents=True)
with ZipFile(args.output,'w') as z:
 for info in infos:z.writestr(info,changes.get(info.filename,files[info.filename]))
text='\n'.join(root.xpath('//w:t/text()',namespaces=ns))
assert len(re.findall(r'UC-(?:C|D|G|UI)\d{2} —',text))==31
assert not re.search(r'TV[1-4]',text)
print('Updated',len(updates),'paragraphs; fresh regression table; 14 diagrams; 31 UC and ten sections retained.')
print(args.output)
