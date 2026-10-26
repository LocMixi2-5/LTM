from docx import Document
from docx.shared import Cm,Pt,RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH as Align
from docx.enum.table import WD_TABLE_ALIGNMENT,WD_CELL_VERTICAL_ALIGNMENT
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from pathlib import Path
import subprocess
base=Path(__file__).resolve().parent
p=base/'Bao_cao_LTM_4_phan.docx'
subprocess.run(['pandoc',str(base/'Bao_cao_LTM_4_phan.md'),'--resource-path',str(base),'-o',str(p)],check=True)
d=Document(p)
styles={s.name:s for s in d.styles}
for s in d.sections:
 s.page_width=Cm(21); s.page_height=Cm(29.7)
 s.top_margin=Cm(2.2);s.bottom_margin=Cm(2.2);s.left_margin=Cm(2.7);s.right_margin=Cm(2)
 s.header_distance=Cm(1);s.footer_distance=Cm(1)
 s.different_first_page_header_footer=True
 h=s.header.paragraphs[0];h.alignment=Align.RIGHT
 r=h.add_run('LẬP TRÌNH MẠNG • 8-BALL POOL • BÁO CÁO 4 PHẦN');r.font.size=Pt(8);r.font.color.rgb=RGBColor.from_string('666666')
 f=s.footer.paragraphs[0];f.alignment=Align.CENTER
 f.add_run('Trang ')
 field=OxmlElement('w:fldSimple');field.set(qn('w:instr'),'PAGE');f._p.append(field)
 for r in f.runs:r.font.size=Pt(10)
for name in ['Normal','Body Text','First Paragraph']:
 st=styles[name];st.font.name='Times New Roman';st.font.size=Pt(12)
 st.paragraph_format.line_spacing=1.25;st.paragraph_format.space_after=Pt(6)
 st.paragraph_format.widow_control=True
 st.paragraph_format.alignment=Align.JUSTIFY
 st._element.get_or_add_rPr().rFonts.set(qn('w:eastAsia'),'Times New Roman')
for name,size,color in [('Heading 1',16,'17365D'),('Heading 2',13,'234E70'),('Heading 3',12,'234E70')]:
 st=styles[name];st.font.name='Times New Roman';st.font.size=Pt(size);st.font.bold=True;st.font.color.rgb=RGBColor.from_string(color)
 st.paragraph_format.keep_with_next=True;st.paragraph_format.space_before=Pt(12);st.paragraph_format.space_after=Pt(7)
 st.paragraph_format.alignment=Align.LEFT
for par in d.paragraphs:
 text=par.text
 if text.startswith('PHẦN '):par.paragraph_format.page_break_before=True
 if text in ['TÓM TẮT','PHÂN CÔNG VÀ PHẠM VI BÁO CÁO','PHẠM VI CẬP NHẬT NGÀY 26/10/2026','TÀI LIỆU VÀ MÃ NGUỒN ĐỐI CHIẾU']:
  par.paragraph_format.page_break_before=True
 if text.startswith('Hình '):
  par.alignment=Align.CENTER
  par.paragraph_format.space_before=Pt(6);par.paragraph_format.space_after=Pt(8)
  for r in par.runs:r.font.italic=True;r.font.size=Pt(10.5);r.font.color.rgb=RGBColor.from_string('666666')
 if text.startswith('Thành viên phụ trách theo phân công:'):
  for r in par.runs:r.font.bold=True
for i,par in enumerate(d.paragraphs):
 if par.text=='TÓM TẮT':break
 par.alignment=Align.CENTER
 if i==0:
  par.paragraph_format.space_before=Pt(48);par.paragraph_format.space_after=Pt(24)
  for r in par.runs:r.font.size=Pt(19);r.font.bold=True
 elif i==1:
  par.paragraph_format.space_after=Pt(30)
  for r in par.runs:r.font.size=Pt(17);r.font.bold=True
 else:par.paragraph_format.space_after=Pt(12)
for table in d.tables:
 table.style=styles['Table'];table.alignment=WD_TABLE_ALIGNMENT.CENTER;table.autofit=False
 n=len(table.columns)
 widths={3:[3.9,5.8,5.6],4:[1.1,3.3,4.7,6.2]}.get(n,[15.3/n]*n)
 if table.cell(0,0).text=='Mã':widths=[1.2,9.2,4.9]
 for col,w in zip(table.columns,widths):col.width=Cm(w)
 borders=OxmlElement('w:tblBorders')
 for side in ['top','left','bottom','right','insideH','insideV']:
  e=OxmlElement('w:'+side);e.set(qn('w:val'),'single');e.set(qn('w:sz'),'4');e.set(qn('w:color'),'B8C4CE');borders.append(e)
 table._tbl.tblPr.append(borders)
 for ri,row in enumerate(table.rows):
  pr=row._tr.get_or_add_trPr();pr.append(OxmlElement('w:cantSplit'))
  if ri==0:pr.append(OxmlElement('w:tblHeader'))
  for ci,cell in enumerate(row.cells):
   cell.width=Cm(widths[ci]);cell.vertical_alignment=WD_CELL_VERTICAL_ALIGNMENT.CENTER
   if ri==0:
    sh=OxmlElement('w:shd');sh.set(qn('w:fill'),'E6EEF5');cell._tc.get_or_add_tcPr().append(sh)
   for par in cell.paragraphs:
    par.alignment=Align.LEFT;par.paragraph_format.line_spacing=1.05
    par.paragraph_format.space_before=Pt(3);par.paragraph_format.space_after=Pt(3)
    par.paragraph_format.keep_with_next=(ri==0)
    for r in par.runs:r.font.name='Times New Roman';r.font.size=Pt(10.5);r.font.bold=(ri==0)
anchor=next(p for p in d.paragraphs if p.text.startswith('PHẦN 1.'))
head=anchor.insert_paragraph_before('NỘI DUNG CHÍNH',styles['Heading 2'])
head.paragraph_format.page_break_before=True
for title in [
 'Phần 1. Tổng hợp hệ thống và giao thức mạng — Nguyễn Xuân Lộc',
 'Phần 2. Server và cơ sở dữ liệu — Trung',
 'Phần 3. Client, giao diện và game engine — Phạm Hoàng Minh',
 'Phần 4. Kiểm thử, kết quả và đánh giá — Lê Tiến Mạnh']:
 anchor.insert_paragraph_before(title)
d.core_properties.title='Báo cáo Lập trình mạng — Game 8-Ball Pool — 4 phần công việc'
d.core_properties.subject='Báo cáo theo bảng phân công nhóm và mã nguồn 00bf1cc'
d.core_properties.author='Nhóm đồ án LTM'
d.core_properties.keywords='LTM, JavaFX, TCP, PostgreSQL, đồng bộ, phân công'
d.core_properties.comments='Đối chiếu mã nguồn 00bf1cc ngày 26/10/2026. 4 sơ đồ kỹ thuật và 7 ảnh giao diện; nguồn ảnh được chú thích.'
# Keep figures inside the printable area and captions with their picture.
for shape in d.inline_shapes:
 scale=min(1.0,int(Cm(15.3))/shape.width,int(Cm(18.5))/shape.height)
 shape.width=int(shape.width*scale);shape.height=int(shape.height*scale)
for par in d.paragraphs:
 if par._p.xpath('.//w:drawing'):
  par.alignment=Align.CENTER
  par.paragraph_format.keep_with_next=True
  par.paragraph_format.space_before=Pt(8)
  par.paragraph_format.space_after=Pt(3)
 if par.style.name in ['Source Code','Block Text']:
  par.paragraph_format.alignment=Align.LEFT
  for r in par.runs:r.font.name='DejaVu Sans Mono';r.font.size=Pt(8)
d.save(p)
r=Document(p)
parts=[x.text for x in r.paragraphs if x.style.name=='Heading 1' and x.text.startswith('PHẦN ')]
assert len(parts)==4,parts
assert len(r.inline_shapes)==11
assert len(r.tables)>=11,len(r.tables)
body='\n'.join(x.text for x in r.paragraphs)
for name in ['Nguyễn Xuân Lộc','Trung','Phạm Hoàng Minh','Lê Tiến Mạnh']:assert name in body
assert 'không cần tạo ảnh' not in body
print('PASS: 4 main parts; all 4 members; 11 embedded figures.')
print('Paragraphs:',len(r.paragraphs),'Figures:',len(r.inline_shapes))
print('DOCX size:',p.stat().st_size)
print('Markdown words:',len((base/'Bao_cao_LTM_4_phan.md').read_text().split()))

# Optional PDF and standalone HTML, using the same Markdown and image files.
import sys
if '--pdf' in sys.argv:
 from lxml import html,etree
 html_path=base/'Bao_cao_LTM_4_phan.html'
 subprocess.run(['pandoc',str(base/'Bao_cao_LTM_4_phan.md'),'--standalone','--metadata','title=Báo cáo LTM — bản mới 00bf1cc','--css=report.css','--output',str(html_path)],check=True)
 doc=html.document_fromstring(html_path.read_text())
 body=doc.find('body');children=list(body)
 start=next(e for e in children if e.tag=='h1')
 cover=html.Element('div',attrib={'class':'cover'})
 body.insert(body.index(start),cover)
 for node in children[children.index(start):]:
  if node.tag=='h2' and node.text_content().strip()=='TÓM TẮT':break
  cover.append(node)
 for h in doc.xpath('//h2'):
  if ' '.join(h.text_content().split()) in ['TÓM TẮT','PHÂN CÔNG VÀ PHẠM VI BÁO CÁO','PHẠM VI CẬP NHẬT NGÀY 26/10/2026','TÀI LIỆU VÀ MÃ NGUỒN ĐỐI CHIẾU']:h.set('class','front-break')
 html_path.write_text(etree.tostring(doc,encoding='unicode',method='html',doctype='<!DOCTYPE html>'))
 subprocess.run([sys.executable,'-m','weasyprint',str(html_path),str(base/'Bao_cao_LTM_4_phan.pdf')],check=True)
