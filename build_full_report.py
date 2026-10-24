# -*- coding: utf-8 -*-
"""
HỆ THỐNG SINH BÁO CÁO ĐỒ ÁN MÔN LẬP TRÌNH MẠNG
Đề tài: Game Bida 8-Ball Pool Multiplayer qua mạng TCP/IP & UDP
Định dạng xuất: Microsoft Word (.docx) chuẩn học thuật A4 (25-35 trang)
"""

import os
import sys
import docx
from docx.shared import Inches, Pt, Cm, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_ALIGN_VERTICAL
from docx.oxml import OxmlElement, parse_xml
from docx.oxml.ns import qn, nsdecls

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DIAGRAMS_DIR = os.path.join(BASE_DIR, 'report_assets', 'diagrams')
OUTPUT_FILE = os.path.join(BASE_DIR, 'Bao_cao_Do_an_Lap_trinh_mang_8BallPool.docx')

# Bảng màu chuẩn học thuật (Academic Navy Palette)
C_NAVY_DARK  = RGBColor(27, 54, 93)     # #1B365D - Tiêu đề chính, Chương
C_NAVY_MED   = RGBColor(43, 108, 176)   # #2B6CB0 - Mục cấp 2
C_TEXT_DARK  = RGBColor(45, 55, 72)     # #2D3748 - Thân bài
C_TEXT_MUTED = RGBColor(113, 128, 150)  # #718096 - Chú thích
C_AMBER      = RGBColor(180, 83, 9)     # #B45309 - Placeholder cảnh báo
C_GREEN      = RGBColor(22, 101, 52)    # #166534 - Trạng thái Đạt

def set_cell_background(cell, fill_hex):
    tcPr = cell._tc.get_or_add_tcPr()
    shd = parse_xml(f'<w:shd {nsdecls("w")} w:fill="{fill_hex}"/>')
    tcPr.append(shd)

def set_cell_margins(cell, top=100, bottom=100, left=140, right=140):
    tcPr = cell._tc.get_or_add_tcPr()
    tcMar = parse_xml(f'<w:tcMar {nsdecls("w")}>'
                      f'<w:top w:w="{top}" w:type="dxa"/>'
                      f'<w:bottom w:w="{bottom}" w:type="dxa"/>'
                      f'<w:left w:w="{left}" w:type="dxa"/>'
                      f'<w:right w:w="{right}" w:type="dxa"/>'
                      f'</w:tcMar>')
    tcPr.append(tcMar)

def add_header_footer(doc):
    for s in doc.sections:
        s.different_first_page_header_footer = True
        
        # Header
        hdr = s.header
        p_hdr = hdr.paragraphs[0]
        p_hdr.alignment = WD_ALIGN_PARAGRAPH.RIGHT
        r_hdr = p_hdr.add_run("Báo Cáo Đồ Án Lập Trình Mạng — Game 8-Ball Pool Multiplayer")
        r_hdr.font.name = "Times New Roman"
        r_hdr.font.size = Pt(8.5)
        r_hdr.font.italic = True
        r_hdr.font.color.rgb = C_TEXT_MUTED
        
        # Footer
        ftr = s.footer
        p_ftr = ftr.paragraphs[0]
        p_ftr.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r_ftr = p_ftr.add_run("Trang ")
        r_ftr.font.name = "Times New Roman"
        r_ftr.font.size = Pt(9.5)
        r_ftr.font.color.rgb = C_TEXT_MUTED
        
        fld_page = parse_xml(r'<w:fldSimple %s w:instr="PAGE"/>' % nsdecls('w'))
        p_ftr._p.append(fld_page)
        
        r_ftr2 = p_ftr.add_run(" / ")
        r_ftr2.font.name = "Times New Roman"
        r_ftr2.font.size = Pt(9.5)
        r_ftr2.font.color.rgb = C_TEXT_MUTED
        
        fld_numpages = parse_xml(r'<w:fldSimple %s w:instr="NUMPAGES"/>' % nsdecls('w'))
        p_ftr._p.append(fld_numpages)

def create_p(doc, text="", space_before=0, space_after=6, line_spacing=1.35, align=WD_ALIGN_PARAGRAPH.JUSTIFY, first_indent=Cm(0.8), bold=False, italic=False, color=C_TEXT_DARK, size=13):
    p = doc.add_paragraph()
    p.alignment = align
    p.paragraph_format.space_before = Pt(space_before)
    p.paragraph_format.space_after = Pt(space_after)
    p.paragraph_format.line_spacing = line_spacing
    if first_indent and align == WD_ALIGN_PARAGRAPH.JUSTIFY:
        p.paragraph_format.first_line_indent = first_indent
    if text:
        r = p.add_run(text)
        r.font.name = "Times New Roman"
        r.font.size = Pt(size)
        r.font.bold = bold
        r.font.italic = italic
        r.font.color.rgb = color
    return p

def add_heading1(doc, text):
    doc.add_page_break()
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(18)
    p.paragraph_format.space_after = Pt(10)
    p.paragraph_format.keep_with_next = True
    p.alignment = WD_ALIGN_PARAGRAPH.LEFT
    r = p.add_run(text)
    r.font.name = "Times New Roman"
    r.font.size = Pt(16)
    r.font.bold = True
    r.font.color.rgb = C_NAVY_DARK
    return p

def add_heading2(doc, text):
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(14)
    p.paragraph_format.space_after = Pt(6)
    p.paragraph_format.keep_with_next = True
    p.alignment = WD_ALIGN_PARAGRAPH.LEFT
    r = p.add_run(text)
    r.font.name = "Times New Roman"
    r.font.size = Pt(14)
    r.font.bold = True
    r.font.color.rgb = C_NAVY_MED
    return p

def add_heading3(doc, text):
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(10)
    p.paragraph_format.space_after = Pt(4)
    p.paragraph_format.keep_with_next = True
    p.alignment = WD_ALIGN_PARAGRAPH.LEFT
    r = p.add_run(text)
    r.font.name = "Times New Roman"
    r.font.size = Pt(13)
    r.font.bold = True
    r.font.color.rgb = C_TEXT_DARK
    return p

def add_figure(doc, img_path, caption_text, width_cm=15.0):
    if os.path.exists(img_path):
        p_img = doc.add_paragraph()
        p_img.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_img.paragraph_format.space_before = Pt(10)
        p_img.paragraph_format.space_after = Pt(4)
        run_img = p_img.add_run()
        run_img.add_picture(img_path, width=Cm(width_cm))
        
        p_cap = doc.add_paragraph()
        p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_cap.paragraph_format.space_before = Pt(2)
        p_cap.paragraph_format.space_after = Pt(12)
        r_cap = p_cap.add_run(caption_text)
        r_cap.font.name = "Times New Roman"
        r_cap.font.size = Pt(10.5)
        r_cap.font.italic = True
        r_cap.font.bold = True
        r_cap.font.color.rgb = C_TEXT_DARK
    else:
        print(f"Warning: Image file not found: {img_path}")

def add_callout_placeholder(doc, img_code, title, capture_instructions, suggested_file, target_location):
    tbl = doc.add_table(rows=1, cols=1)
    tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
    cell = tbl.cell(0, 0)
    set_cell_background(cell, "FEF3C7") # Vàng cam nhạt cảnh báo
    set_cell_margins(cell, top=140, bottom=140, left=180, right=180)
    cell.width = Cm(15.5)

    p = cell.paragraphs[0]
    p.paragraph_format.space_before = Pt(2)
    p.paragraph_format.space_after = Pt(4)
    p.paragraph_format.line_spacing = 1.15
    p.alignment = WD_ALIGN_PARAGRAPH.LEFT
    
    r1 = p.add_run(f"📸 [{img_code}: {title}]\n")
    r1.font.name = "Times New Roman"
    r1.font.size = Pt(11)
    r1.font.bold = True
    r1.font.color.rgb = C_AMBER
    
    r2 = p.add_run(f"• Thao tác chụp thực tế: {capture_instructions}\n"
                   f"• Tên file ảnh gợi ý: {suggested_file}\n"
                   f"• Vị trí gắn trong báo cáo: {target_location}")
    r2.font.name = "Times New Roman"
    r2.font.size = Pt(10)
    r2.font.italic = True
    r2.font.color.rgb = RGBColor(120, 53, 15)
    
    p_sp = doc.add_paragraph()
    p_sp.paragraph_format.space_before = Pt(0)
    p_sp.paragraph_format.space_after = Pt(6)

def add_code_block(doc, code_text):
    tbl = doc.add_table(rows=1, cols=1)
    tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
    cell = tbl.cell(0, 0)
    set_cell_background(cell, "F8FAFC")
    set_cell_margins(cell, top=120, bottom=120, left=160, right=160)
    cell.width = Cm(15.5)

    p = cell.paragraphs[0]
    p.paragraph_format.space_before = Pt(2)
    p.paragraph_format.space_after = Pt(2)
    p.paragraph_format.line_spacing = 1.15
    p.alignment = WD_ALIGN_PARAGRAPH.LEFT
    
    r = p.add_run(code_text.strip())
    r.font.name = "Consolas"
    r.font.size = Pt(9.5)
    r.font.color.rgb = RGBColor(30, 41, 59)
    
    p_sp = doc.add_paragraph()
    p_sp.paragraph_format.space_before = Pt(0)
    p_sp.paragraph_format.space_after = Pt(6)

def style_table(table, col_widths, headers, data, align_cols=None):
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    header_tr = table.rows[0]._tr.get_or_add_trPr()
    header_tr.append(parse_xml(r'<w:tblHeader %s/>' % nsdecls('w')))
    
    for row_idx, row in enumerate(table.rows):
        trPr = row._tr.get_or_add_trPr()
        trPr.append(parse_xml(r'<w:cantSplit %s/>' % nsdecls('w')))
        
        for col_idx, cell in enumerate(row.cells):
            cell.width = Cm(col_widths[col_idx])
            set_cell_margins(cell, top=100, bottom=100, left=140, right=140)
            p = cell.paragraphs[0]
            p.paragraph_format.space_before = Pt(0)
            p.paragraph_format.space_after = Pt(0)
            p.paragraph_format.line_spacing = 1.15
            
            align = WD_ALIGN_PARAGRAPH.LEFT
            if align_cols and col_idx < len(align_cols):
                align = align_cols[col_idx]
            p.alignment = align
            
            if row_idx == 0:
                set_cell_background(cell, "1B365D")
                r = p.add_run(headers[col_idx])
                r.font.name = "Times New Roman"
                r.font.size = Pt(9.5)
                r.font.bold = True
                r.font.color.rgb = RGBColor(255, 255, 255)
            else:
                bg = "FFFFFF" if row_idx % 2 == 1 else "F8FAFC"
                set_cell_background(cell, bg)
                val = data[row_idx - 1][col_idx]
                r = p.add_run(str(val))
                r.font.name = "Times New Roman"
                r.font.size = Pt(9)
                if str(val) == "Đạt":
                    r.font.bold = True
                    r.font.color.rgb = C_GREEN
                elif str(val) == "Chưa kiểm thử":
                    r.font.italic = True
                    r.font.color.rgb = C_AMBER
                else:
                    r.font.color.rgb = C_TEXT_DARK

def add_table_with_caption(doc, caption_text, col_widths, headers, data, align_cols=None):
    p_cap = doc.add_paragraph()
    p_cap.alignment = WD_ALIGN_PARAGRAPH.LEFT
    p_cap.paragraph_format.space_before = Pt(8)
    p_cap.paragraph_format.space_after = Pt(3)
    p_cap.paragraph_format.keep_with_next = True
    r = p_cap.add_run(caption_text)
    r.font.name = "Times New Roman"
    r.font.size = Pt(10.5)
    r.font.bold = True
    r.font.italic = True
    r.font.color.rgb = C_NAVY_DARK
    
    table = doc.add_table(rows=len(data) + 1, cols=len(headers))
    style_table(table, col_widths, headers, data, align_cols)
    
    p_sp = doc.add_paragraph()
    p_sp.paragraph_format.space_before = Pt(0)
    p_sp.paragraph_format.space_after = Pt(8)
    return table

def add_native_toc(doc):
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(6)
    p.paragraph_format.space_after = Pt(6)
    run = p.add_run()
    fldChar1 = parse_xml(r'<w:fldChar %s w:fldCharType="begin"/>' % nsdecls('w'))
    instrText = parse_xml(r'<w:instrText %s xml:space="preserve"> TOC \o "1-3" \h \z \u </w:instrText>' % nsdecls('w'))
    fldChar2 = parse_xml(r'<w:fldChar %s w:fldCharType="separate"/>' % nsdecls('w'))
    fldChar3 = parse_xml(r'<w:fldChar %s w:fldCharType="end"/>' % nsdecls('w'))
    run._r.append(fldChar1)
    run._r.append(instrText)
    run._r.append(fldChar2)
    run._r.append(fldChar3)

print("Starting full academic report compilation...")
doc = docx.Document()

# Cài đặt kích thước A4 & lề chuẩn (Trái 3.0cm, Phải 2.0cm, Trên 2.0cm, Dưới 2.0cm)
section = doc.sections[0]
section.page_width = Cm(21.0)
section.page_height = Cm(29.7)
section.left_margin = Cm(3.0)
section.right_margin = Cm(2.0)
section.top_margin = Cm(2.0)
section.bottom_margin = Cm(2.0)

# ==============================================================================
# TRANG BÌA (Cover Page)
# ==============================================================================
p = doc.add_paragraph()
p.alignment = WD_ALIGN_PARAGRAPH.CENTER
p.paragraph_format.space_before = Pt(12)
p.paragraph_format.space_after = Pt(2)
r = p.add_run("BỘ GIÁO DỤC VÀ ĐÀO TẠO\nTRƯỜNG [ĐIỀN TÊN TRƯỜNG]\nKHOA [ĐIỀN TÊN KHOA]")
r.font.name = "Times New Roman"
r.font.size = Pt(12.5)
r.font.bold = True
r.font.color.rgb = C_NAVY_DARK

p_line = doc.add_paragraph()
p_line.alignment = WD_ALIGN_PARAGRAPH.CENTER
p_line.paragraph_format.space_after = Pt(40)
r_line = p_line.add_run("━━━━━━━━━━━━━━━━━━━")
r_line.font.name = "Times New Roman"
r_line.font.size = Pt(14)
r_line.font.bold = True
r_line.font.color.rgb = C_NAVY_MED

p_title1 = doc.add_paragraph()
p_title1.alignment = WD_ALIGN_PARAGRAPH.CENTER
p_title1.paragraph_format.space_after = Pt(8)
r_t1 = p_title1.add_run("BÁO CÁO ĐỒ ÁN MÔN HỌC")
r_t1.font.name = "Times New Roman"
r_t1.font.size = Pt(18)
r_t1.font.bold = True
r_t1.font.color.rgb = C_NAVY_DARK

p_sub = doc.add_paragraph()
p_sub.alignment = WD_ALIGN_PARAGRAPH.CENTER
p_sub.paragraph_format.space_after = Pt(16)
r_sub = p_sub.add_run("HỌC PHẦN: LẬP TRÌNH MẠNG")
r_sub.font.name = "Times New Roman"
r_sub.font.size = Pt(15)
r_sub.font.bold = True
r_sub.font.color.rgb = C_NAVY_MED

p_dt = doc.add_paragraph()
p_dt.alignment = WD_ALIGN_PARAGRAPH.CENTER
p_dt.paragraph_format.space_after = Pt(6)
r_dt = p_dt.add_run("ĐỀ TÀI:")
r_dt.font.name = "Times New Roman"
r_dt.font.size = Pt(13)
r_dt.font.italic = True
r_dt.font.color.rgb = C_TEXT_MUTED

p_title2 = doc.add_paragraph()
p_title2.alignment = WD_ALIGN_PARAGRAPH.CENTER
p_title2.paragraph_format.space_after = Pt(45)
r_t2 = p_title2.add_run("XÂY DỰNG HỆ THỐNG TRÒ CHƠI BI-A 8-BALL POOL MULTIPLAYER VỚI KIẾN TRÚC CLIENT-SERVER ĐỒNG BỘ VẬT LÝ VÀ GIAO THỨC NHỊ PHÂN QUA TCP/UDP")
r_t2.font.name = "Times New Roman"
r_t2.font.size = Pt(16.5)
r_t2.font.bold = True
r_t2.font.color.rgb = C_NAVY_DARK

# Bảng thông tin sinh viên & giảng viên
tbl_cover = doc.add_table(rows=6, cols=2)
tbl_cover.alignment = WD_TABLE_ALIGNMENT.CENTER
tbl_cover.cell(0, 0).width = Cm(5.0)
tbl_cover.cell(0, 1).width = Cm(10.5)

cover_info = [
    ("Giảng viên hướng dẫn:", "[ĐIỀN HỌ VÀ TÊN GIẢNG VIÊN HƯỚNG DẪN]"),
    ("Sinh viên thực hiện:",   "[ĐIỀN HỌ VÀ TÊN SINH VIÊN]"),
    ("Mã số sinh viên (MSSV):", "[ĐIỀN MÃ SỐ SINH VIÊN]"),
    ("Lớp chuyên ngành:",       "[ĐIỀN LỚP CHUYÊN NGÀNH]"),
    ("Khoá học:",               "[ĐIỀN KHÓA HỌC]"),
    ("Nhóm thực hiện:",         "[ĐIỀN TÊN NHÓM / MÃ NHÓM]")
]

for idx, (label, val) in enumerate(cover_info):
    c0 = tbl_cover.cell(idx, 0)
    c1 = tbl_cover.cell(idx, 1)
    set_cell_margins(c0, 60, 60, 80, 80)
    set_cell_margins(c1, 60, 60, 80, 80)
    
    p0 = c0.paragraphs[0]
    p0.alignment = WD_ALIGN_PARAGRAPH.LEFT
    r0 = p0.add_run(label)
    r0.font.name = "Times New Roman"
    r0.font.size = Pt(12)
    r0.font.bold = True
    r0.font.color.rgb = C_TEXT_DARK
    
    p1 = c1.paragraphs[0]
    p1.alignment = WD_ALIGN_PARAGRAPH.LEFT
    r1 = p1.add_run(val)
    r1.font.name = "Times New Roman"
    r1.font.size = Pt(12)
    r1.font.color.rgb = C_NAVY_MED if "ĐIỀN" in val else C_TEXT_DARK

p_bottom = doc.add_paragraph()
p_bottom.alignment = WD_ALIGN_PARAGRAPH.CENTER
p_bottom.paragraph_format.space_before = Pt(50)
p_bottom.paragraph_format.space_after = Pt(0)
r_bot = p_bottom.add_run("[ĐIỀN ĐỊA DANH / HÀ NỘI] — NĂM [ĐIỀN NĂM 2026]")
r_bot.font.name = "Times New Roman"
r_bot.font.size = Pt(12)
r_bot.font.bold = True
r_bot.font.color.rgb = C_TEXT_DARK

# Header/Footer thiết lập từ trang sau
add_header_footer(doc)

# ==============================================================================
# BẢNG PHÂN CÔNG NHIỆM VỤ & LỜI CAM ĐOAN
# ==============================================================================
doc.add_page_break()
p_assign_title = doc.add_paragraph()
p_assign_title.alignment = WD_ALIGN_PARAGRAPH.CENTER
p_assign_title.paragraph_format.space_before = Pt(10)
p_assign_title.paragraph_format.space_after = Pt(12)
r = p_assign_title.add_run("THÔNG TIN VÀ PHÂN CÔNG NHIỆM VỤ THÀNH VIÊN")
r.font.name = "Times New Roman"
r.font.size = Pt(15)
r.font.bold = True
r.font.color.rgb = C_NAVY_DARK

create_p(doc, "Bảng dưới đây tổng hợp chi tiết phân công trách nhiệm và mức độ đóng góp của từng thành viên trong quá trình khảo sát, thiết kế kiến trúc mạng, phát triển mã nguồn client-server, kiểm thử và xây dựng báo cáo đồ án:", space_after=8)

headers_assign = ["STT", "Họ và tên", "MSSV", "Lớp", "Vai trò", "Nhiệm vụ đảm nhiệm", "Mức độ hoàn thành"]
data_assign = [
    ["1", "[ĐIỀN HỌ VÀ TÊN 1]", "[ĐIỀN MSSV 1]", "[ĐIỀN LỚP 1]", "Trưởng nhóm", "Thiết kế kiến trúc Socket TCP, ServerMultiThread, GameRoom, Đồng bộ vật lý tất định", "100%"],
    ["2", "[ĐIỀN HỌ VÀ TÊN 2]", "[ĐIỀN MSSV 2]", "[ĐIỀN LỚP 2]", "Thành viên", "Thiết kế CSDL PostgreSQL, AuthManager, Triển khai chức năng Bạn bè & Chat realtime", "100%"],
    ["3", "[ĐIỀN HỌ VÀ TÊN 3]", "[ĐIỀN MSSV 3]", "[ĐIỀN LỚP 3]", "Thành viên", "Phát triển giao diện JavaFX 17, Hệ thống Skin gậy cơ 5 phẩm cấp, Viết báo cáo đồ án", "100%"]
]
col_widths_assign = [1.2, 3.2, 2.2, 1.8, 2.0, 3.8, 1.8]
add_table_with_caption(doc, "Bảng 0.1: Phân công nhiệm vụ các thành viên tham gia đồ án", col_widths_assign, headers_assign, data_assign,
                       [WD_ALIGN_PARAGRAPH.CENTER, WD_ALIGN_PARAGRAPH.LEFT, WD_ALIGN_PARAGRAPH.CENTER, WD_ALIGN_PARAGRAPH.CENTER, WD_ALIGN_PARAGRAPH.CENTER, WD_ALIGN_PARAGRAPH.LEFT, WD_ALIGN_PARAGRAPH.CENTER])

add_heading2(doc, "LỜI CAM ĐOAN HỌC THUẬT")
create_p(doc, "Chúng tôi xin cam đoan đồ án môn học Lập trình mạng với đề tài \"Xây dựng hệ thống trò chơi Bi-a 8-Ball Pool Multiplayer qua mạng TCP/IP & UDP\" là công trình nghiên cứu và phát triển thực tế của nhóm chúng tôi dưới sự hướng dẫn của giảng viên bộ môn. Toàn bộ kiến trúc mạng, giao thức nhị phân, thuật toán vật lý 2D, lược đồ cơ sở dữ liệu và mã nguồn ứng dụng được trình bày trong báo cáo này đều được xây dựng dựa trên mã nguồn thực tế của dự án tại thư mục làm việc, không sao chép bất hợp pháp từ bất kỳ đồ án nào khác. Các tài liệu, công nghệ và thư viện tham khảo đã được trích dẫn nguồn gốc đầy đủ và trung thực.")

add_heading2(doc, "TÓM TẮT ĐỀ TÀI (EXECUTIVE SUMMARY)")
create_p(doc, "Trò chơi trực tuyến nhiều người chơi (Multiplayer Game) thời gian thực luôn là một trong những bài toán phức tạp và tiêu biểu nhất của lĩnh vực Lập trình mạng. Đề tài này tập trung giải quyết bài toán thiết kế và hiện thực hóa một hệ sinh thái game bi-a 8-Ball Pool 2 người chơi qua mạng cục bộ (LAN) và Internet. Hệ thống được triển khai trên nền tảng Java 17 LTS với giao diện người dùng JavaFX 17.0.13 và cơ sở dữ liệu quan hệ PostgreSQL 16.")
create_p(doc, "Điểm cốt lõi về mặt kỹ thuật mạng của dự án bao gồm: (1) Xây dựng giao thức truyền thông nhị phân tùy chỉnh (Custom Binary Protocol) trên nền TCP Socket cổng 12345, định dạng gói tin bắt đầu bằng mã Byte kiểu gói (PacketType code) kết hợp dữ liệu tuần tự hóa Big-Endian, giúp tối ưu hóa băng thông truyền tải cực kỳ nhỏ gọn; (2) Tích hợp cơ chế tự động dò tìm máy chủ mạng nội bộ (LAN Auto-Discovery) qua giao thức UDP Broadcast cổng 12346; (3) Mô hình máy chủ đa luồng (Multi-threaded Server, 1 thread worker/client) đảm bảo phân phối thông điệp tức thời mà không gây hiện tượng nghẽn luồng; (4) Giải pháp đồng bộ ván đấu kết hợp giữa Server-arbitrated (Server làm trọng tài điều phối hạt giống Match Seed, phân định lượt đánh và xác nhận thắng thua) và Client-side Deterministic Physics (Client mô phỏng cục bộ độc lập dựa trên góc đánh, lực cơ và điểm xoáy ép-phê), đảm bảo độ phản hồi đồ họa mượt mà 60 FPS mà không phụ thuộc vào độ trễ đường truyền; (5) Hệ thống quản lý người chơi toàn diện với xác thực bảo mật SHA-256 + Salt, kết bạn, trò chuyện thời gian thực, bảng xếp hạng tự động tải ngầm và hệ thống mở khóa trang bị Skin gậy 3D theo 5 mốc cấp độ với cơ chế kiểm tra bản quyền nghiêm ngặt phía Server.")

# ==============================================================================
# MỤC LỤC & DANH MỤC
# ==============================================================================
doc.add_page_break()
p_toc_title = doc.add_paragraph()
p_toc_title.alignment = WD_ALIGN_PARAGRAPH.CENTER
p_toc_title.paragraph_format.space_before = Pt(10)
p_toc_title.paragraph_format.space_after = Pt(12)
r = p_toc_title.add_run("MỤC LỤC")
r.font.name = "Times New Roman"
r.font.size = Pt(16)
r.font.bold = True
r.font.color.rgb = C_NAVY_DARK

create_p(doc, "(Mục lục tự động được thiết lập theo chuẩn Microsoft Word. Người dùng có thể nhấp chuột phải vào bảng mục lục bên dưới và chọn 'Update Field' hoặc nhấn F9 trong Word để cập nhật số trang chính xác nhất).", italic=True, size=11, color=C_TEXT_MUTED)
add_native_toc(doc)

# Danh sách tóm lược mục lục
toc_items = [
    ("CHƯƠNG 1. TỔNG QUAN ĐỀ TÀI", "Trang 4"),
    ("    1.1. Bối cảnh và lý do lựa chọn đề tài", "Trang 4"),
    ("    1.2. Mục tiêu nghiên cứu và phát triển", "Trang 5"),
    ("    1.3. Phạm vi triển khai của đồ án", "Trang 5"),
    ("    1.4. Các chức năng chính của hệ sinh thái trò chơi", "Trang 6"),
    ("    1.5. Công nghệ sử dụng và vai trò thực tế", "Trang 6"),
    ("CHƯƠNG 2. PHÂN TÍCH YÊU CẦU HỆ THỐNG", "Trang 8"),
    ("    2.1. Các tác nhân sử dụng hệ thống", "Trang 8"),
    ("    2.2. Yêu cầu chức năng chi tiết", "Trang 8"),
    ("    2.3. Yêu cầu phi chức năng", "Trang 9"),
    ("    2.4. Sơ đồ Use Case tổng thể", "Trang 10"),
    ("    2.5. Đặc tả các Use Case trọng tâm", "Trang 11"),
    ("CHƯƠNG 3. THIẾT KẾ KIẾN TRÚC HỆ THỐNG", "Trang 13"),
    ("    3.1. Mô hình kiến trúc tổng thể Client - Server", "Trang 13"),
    ("    3.2. Sơ đồ tổ chức Module và Package", "Trang 15"),
    ("    3.3. Mô hình triển khai thực tế (Deployment Diagram)", "Trang 16"),
    ("    3.4. Mô hình máy trạng thái ván đấu (Match State Machine)", "Trang 17"),
    ("CHƯƠNG 4. THIẾT KẾ VÀ TRIỂN KHAI GIAO TIẾP MẠNG", "Trang 18"),
    ("    4.1. Khởi tạo Server và kết nối Client (TCP & UDP Discovery)", "Trang 18"),
    ("    4.2. Mô hình quản lý đa luồng (Multi-threading Model)", "Trang 19"),
    ("    4.3. Cấu trúc gói tin và tuần tự hóa dữ liệu nhị phân (Binary Protocol)", "Trang 20"),
    ("    4.4. Bảng đặc tả chi tiết 45 mã gói tin trong hệ thống", "Trang 21"),
    ("    4.5. Cơ chế quản lý phiên và phòng đấu (PlayerSession & GameRoom)", "Trang 24"),
    ("    4.6. Cơ chế đồng bộ ván đấu và vật lý tất định", "Trang 25"),
    ("    4.7. Các sơ đồ tuần tự chi tiết (Authentication, Match, Shot, GameOver, Skin)", "Trang 27"),
    ("    4.8. Xử lý ngắt kết nối và phục hồi lỗi mạng", "Trang 31"),
    ("CHƯƠNG 5. THIẾT KẾ CƠ SỞ DỮ LIỆU", "Trang 32"),
    ("    5.1. Vai trò của cơ sở dữ liệu quan hệ PostgreSQL", "Trang 32"),
    ("    5.2. Sơ đồ thực thể quan hệ (ERD)", "Trang 32"),
    ("    5.3. Từ điển dữ liệu chi tiết các bảng", "Trang 33"),
    ("    5.4. Các ràng buộc toàn vẹn và chỉ mục (Index) tối ưu hóa", "Trang 35"),
    ("    5.5. Cơ chế lưu trữ và mở khóa Skin gậy cơ", "Trang 35"),
    ("CHƯƠNG 6. GIAO DIỆN NGƯỜI DÙNG VÀ CHỨC NĂNG THỰC TẾ", "Trang 36"),
    ("    6.1. Phong cách thiết kế Modern Luxury Billiards Club", "Trang 36"),
    ("    6.2. Danh mục 16 màn hình chức năng và Placeholder ảnh minh họa", "Trang 37"),
    ("    6.3. Chi tiết hệ thống Skin gậy cơ 3D và Animation theo 5 cấp độ", "Trang 41"),
    ("CHƯƠNG 7. KIỂM THỬ HỆ THỐNG VÀ ĐÁNH GIÁ KẾT QUẢ", "Trang 43"),
    ("    7.1. Môi trường và điều kiện kiểm thử thực tế", "Trang 43"),
    ("    7.2. Ma trận kịch bản kiểm thử (Test Matrix) chi tiết", "Trang 43"),
    ("    7.3. Đánh giá ưu điểm và hạn chế của hệ thống", "Trang 46"),
    ("    7.4. Đề xuất phương pháp đo đạc hiệu năng mạng", "Trang 46"),
    ("CHƯƠNG 8. KẾT LUẬN VÀ HƯỚNG PHÁT TRIỂN", "Trang 47"),
    ("    8.1. Các kết quả cụ thể đã hoàn thành", "Trang 47"),
    ("    8.2. Giá trị học tập môn Lập trình mạng", "Trang 47"),
    ("    8.3. Hạn chế thực tế của đồ án", "Trang 48"),
    ("    8.4. Hướng phát triển và mở rộng tương lai", "Trang 48"),
    ("TÀI LIỆU THAM KHẢO VÀ PHỤ LỤC", "Trang 49")
]

for title, pg in toc_items:
    p_t = doc.add_paragraph()
    p_t.paragraph_format.space_before = Pt(1)
    p_t.paragraph_format.space_after = Pt(2)
    p_t.paragraph_format.line_spacing = 1.15
    is_main = title.startswith("CHƯƠNG") or title.startswith("TÀI LIỆU")
    r0 = p_t.add_run(title)
    r0.font.name = "Times New Roman"
    r0.font.size = Pt(11.5 if is_main else 10.5)
    r0.font.bold = is_main
    r0.font.color.rgb = C_NAVY_DARK if is_main else C_TEXT_DARK

# ==============================================================================
# DANH MỤC HÌNH VẼ & BẢNG BIỂU
# ==============================================================================
add_heading2(doc, "DANH MỤC HÌNH VẼ VÀ SƠ ĐỒ")
fig_list = [
    ("Hình 2.1", "Sơ đồ Use Case tổng thể hệ thống trò chơi 8-Ball Pool Multiplayer"),
    ("Hình 3.1", "Sơ đồ kiến trúc tổng thể hệ thống (Hai Client JavaFX, Kênh TCP/UDP, Server, PostgreSQL)"),
    ("Hình 3.2", "Sơ đồ tổ chức cấu trúc gói mã nguồn (Package Structure) Client & Server"),
    ("Hình 3.3", "Sơ đồ chuyển trạng thái ván đấu (Match State Machine)"),
    ("Hình 4.1", "Sơ đồ tuần tự: Tiến trình xác thực và đăng nhập tài khoản"),
    ("Hình 4.2", "Sơ đồ tuần tự: Ghép trận tự động và khởi tạo bàn đấu"),
    ("Hình 4.3", "Sơ đồ tuần tự: Thực hiện cú đánh bi-a và đồng bộ vật lý 2 máy"),
    ("Hình 4.4", "Sơ đồ tuần tự: Kết thúc ván đấu, cập nhật EXP và thăng cấp"),
    ("Hình 4.5", "Sơ đồ tuần tự: Kiểm tra mở khóa và trang bị Skin gậy cơ"),
    ("Hình 5.1", "Sơ đồ thực thể quan hệ cơ sở dữ liệu (PostgreSQL ERD)")
]
for f_code, f_name in fig_list:
    p_f = doc.add_paragraph()
    p_f.paragraph_format.space_before = Pt(1)
    p_f.paragraph_format.space_after = Pt(2)
    r_c = p_f.add_run(f"• {f_code}: ")
    r_c.font.name = "Times New Roman"
    r_c.font.bold = True
    r_c.font.size = Pt(10.5)
    r_t = p_f.add_run(f_name)
    r_t.font.name = "Times New Roman"
    r_t.font.size = Pt(10.5)

add_heading2(doc, "DANH MỤC BẢNG BIỂU")
tbl_list = [
    ("Bảng 0.1", "Phân công nhiệm vụ các thành viên tham gia đồ án"),
    ("Bảng 1.1", "Tổng hợp công nghệ cốt lõi và vai trò thực tế trong hệ thống"),
    ("Bảng 4.1", "Đặc tả chi tiết 45 mã gói tin nhị phân trong hệ thống 8-Ball Pool"),
    ("Bảng 5.1", "Từ điển dữ liệu bảng players (Quản lý người chơi)"),
    ("Bảng 5.2", "Từ điển dữ liệu bảng match_history (Lịch sử ván đấu)"),
    ("Bảng 5.3", "Từ điển dữ liệu bảng friendships (Danh sách quan hệ bạn bè)"),
    ("Bảng 5.4", "Từ điển dữ liệu bảng messages (Tin nhắn trò chuyện)"),
    ("Bảng 6.1", "Cấu hình 5 phẩm cấp Skin gậy cơ và mốc cấp độ mở khóa"),
    ("Bảng 7.1", "Ma trận kịch bản kiểm thử hệ thống thực tế (Test Matrix)")
]
for t_code, t_name in tbl_list:
    p_t = doc.add_paragraph()
    p_t.paragraph_format.space_before = Pt(1)
    p_t.paragraph_format.space_after = Pt(2)
    r_c = p_t.add_run(f"• {t_code}: ")
    r_c.font.name = "Times New Roman"
    r_c.font.bold = True
    r_c.font.size = Pt(10.5)
    r_t = p_t.add_run(t_name)
    r_t.font.name = "Times New Roman"
    r_t.font.size = Pt(10.5)

add_heading2(doc, "DANH MỤC THUẬT NGỮ VÀ TỪ VIẾT TẮT")
abbr_list = [
    ("TCP", "Transmission Control Protocol - Giao thức truyền vận tin cậy, có kết nối"),
    ("UDP", "User Datagram Protocol - Giao thức truyền vận phi kết nối, hỗ trợ Broadcast"),
    ("JDK", "Java Development Kit - Bộ công cụ phát triển phần mềm Java"),
    ("JavaFX", "Nền tảng giao diện đồ họa cao cấp cho ứng dụng Java máy trạm"),
    ("RDBMS", "Relational Database Management System - Hệ quản trị cơ sở dữ liệu quan hệ"),
    ("PostgreSQL", "Hệ quản trị CSDL quan hệ đối tượng mã nguồn mở mạnh mẽ"),
    ("JDBC", "Java Database Connectivity - Chuẩn giao tiếp kết nối CSDL trong Java"),
    ("DAO", "Data Access Object - Mẫu thiết kế đối tượng truy cập dữ liệu"),
    ("VFX", "Visual Effects - Các hiệu ứng hình ảnh đồ họa đặc biệt"),
    ("SFX", "Sound Effects - Các hiệu ứng âm thanh tương tác"),
    ("BGM", "Background Music - Bản nhạc nền của trò chơi"),
    ("LAN", "Local Area Network - Mạng máy tính cục bộ"),
    ("IP", "Internet Protocol - Giao thức mạng định tuyến gói tin"),
    ("SHA-256", "Secure Hash Algorithm 256-bit - Thuật toán băm mật mã an toàn"),
    ("EXP", "Experience Points - Điểm kinh nghiệm người chơi tích lũy")
]
for ab, mean in abbr_list:
    p_a = doc.add_paragraph()
    p_a.paragraph_format.space_before = Pt(1)
    p_a.paragraph_format.space_after = Pt(2)
    r_c = p_a.add_run(f"• {ab}: ")
    r_c.font.name = "Times New Roman"
    r_c.font.bold = True
    r_c.font.size = Pt(10.5)
    r_t = p_a.add_run(mean)
    r_t.font.name = "Times New Roman"
    r_t.font.size = Pt(10.5)

# ==============================================================================
# CHƯƠNG 1. TỔNG QUAN ĐỀ TÀI
# ==============================================================================
add_heading1(doc, "CHƯƠNG 1. TỔNG QUAN ĐỀ TÀI")

add_heading2(doc, "1.1. Bối cảnh và lý do lựa chọn đề tài")
create_p(doc, "Trong thời đại bùng nổ của công nghệ số và mạng máy tính, các ứng dụng tương tác thời gian thực, đặc biệt là các trò chơi trực tuyến nhiều người chơi (Multiplayer Games), đã trở thành một phần không thể thiếu trong đời sống giải trí hiện đại. Không đơn thuần là các ứng dụng giải trí thông thường, game mạng đòi hỏi việc xử lý đồng thời lượng lớn kết nối, quản lý phiên người dùng, đồng bộ hóa dữ liệu trạng thái với độ trễ thấp và đảm bảo tính công bằng tuyệt đối giữa các đối thủ cạnh tranh.")
create_p(doc, "Môn thể thao Bi-a lỗ (8-Ball Pool) là một trò chơi mang tính chiến thuật cao, đòi hỏi sự chuẩn xác tuyệt đối trong từng cú chọc cơ, lực tác động và hướng di chuyển của các viên bi. Khi chuyển đổi mô hình bi-a vật lý sang môi trường mạng nhiều người chơi, các lập trình viên phải đối mặt với hàng loạt thách thức kỹ thuật hóc búa:")
create_p(doc, "• Làm thế nào để hai máy tính cách xa nhau có thể quan sát diễn biến chuyển động của 16 viên bi hoàn toàn trùng khớp từng milimet mà không bị giật lag (stuttering) do độ trễ truyền gói tin?")
create_p(doc, "• Giao thức nào là tối ưu để truyền tải các gói tin điều khiển với kích thước siêu nhỏ, tránh lãng phí băng thông của tiêu đề giao thức?")
create_p(doc, "• Làm sao để máy chủ đóng vai trò trọng tài khách quan phân định lượt đánh, kiểm tra lỗi phạm quy (như bi cái rơi lỗ, không chạm bi mục tiêu, ăn nhầm bi số 8) mà không làm tăng tải tính toán quá mức lên CPU của máy chủ?")
create_p(doc, "Xuất phát từ những yêu cầu thực tiễn đó, nhóm sinh viên đã lựa chọn đề tài \"Xây dựng hệ thống trò chơi Bi-a 8-Ball Pool Multiplayer qua mạng TCP/IP & UDP\" cho đồ án môn học Lập trình mạng. Đề tài này là cơ hội lý tưởng để áp dụng toàn diện các kiến thức nền tảng về Socket mạng, mô hình Client-Server, lập trình đa luồng (Multi-threading), lập trình giao diện người dùng JavaFX, cơ sở dữ liệu quan hệ PostgreSQL và thiết kế giao thức truyền thông nhị phân tùy biến.")

add_heading2(doc, "1.2. Mục tiêu của đồ án")
create_p(doc, "Mục tiêu trọng tâm của đồ án được chia thành hai nhóm chính:")
create_p(doc, "1. Về mặt kiến thức và kỹ năng Lập trình mạng:")
create_p(doc, "• Nắm vững và làm chủ cơ chế lập trình Socket hướng kết nối (TCP Socket) và Socket không kết nối (UDP DatagramSocket) trên nền tảng Java tiêu chuẩn (JDK 17 LTS).")
create_p(doc, "• Thiết kế và hiện thực hóa một giao thức nhị phân mức ứng dụng (Custom Binary Protocol) độc lập, gọn nhẹ, phân định ranh giới gói tin chuẩn xác qua DataInputStream và DataOutputStream.")
create_p(doc, "• Xây dựng máy chủ đa luồng có khả năng phục vụ đồng thời nhiều người chơi, quản lý hàng đợi ghép trận (Matchmaking Queue), điều phối phòng đấu (GameRoom) và xử lý ngắt kết nối an toàn.")
create_p(doc, "• Tích hợp tính năng tự động dò tìm máy chủ nội bộ (LAN Auto-Discovery) bằng cơ chế Broadcast UDP, giúp người dùng trong cùng mạng Wi-Fi/LAN kết nối chơi cùng nhau tức thì mà không cần cấu hình IP thủ công phức tạp.")
create_p(doc, "2. Về mặt sản phẩm ứng dụng:")
create_p(doc, "• Xây dựng trò chơi 8-Ball Pool hoàn chỉnh với đồ họa 2D Canvas chân thực theo phong cách Modern Luxury Club, hiệu ứng nẹp bạc 4 góc, hoạt cảnh lá cây và bọ cánh cứng sinh động.")
create_p(doc, "• Tích hợp động cơ vật lý 2D tất định (Deterministic 2D Physics Engine) hỗ trợ đầy đủ các kỹ thuật bi-a chuyên nghiệp: nảy băng đàn hồi (cushion bounce), trô bi lùi (draw shot), cule đẩy tới (follow shot) và ép-phê xoáy ngang (side spin).")
create_p(doc, "• Xây dựng hệ thống xã hội trong game: Đăng ký/Đăng nhập bảo mật SHA-256 + Salt, kết bạn, nhắn tin riêng tư thời gian thực, bảng xếp hạng toàn server tự động tải ngầm và bộ sưu tập 5 Skin gậy cơ 3D mở khóa theo cấp độ.")

add_heading2(doc, "1.3. Phạm vi triển khai của đồ án")
create_p(doc, "Để đảm bảo tính khả thi và chất lượng chiều sâu kỹ thuật của đồ án sinh viên, phạm vi của hệ thống được xác định rõ ràng:")
create_p(doc, "• Phạm vi mạng: Hoạt động trong mạng cục bộ (LAN), mạng nội bộ trường học/gia đình, hoặc kết nối qua Internet khi máy chủ có IP tĩnh/port-forwarding/mạng riêng ảo VPN. Giao thức không sử dụng các dịch vụ Cloud trung gian của bên thứ ba.")
create_p(doc, "• Chế độ chơi: Tập trung tối đa vào thể thức đối kháng 1v1 theo luật Bi-a 8-Ball quốc tế (Standard 8-Ball Pool Rules) và chế độ Luyện tập tự do (Practice Mode) một người chơi.")
create_p(doc, "• Mô hình đồng bộ: Sử dụng kiến trúc đồng bộ trạng thái vật lý tất định phân tán (Client-Side Deterministic Simulation) kết hợp trọng tài Server (Server-Arbitrated Turn & Rules). Báo cáo phân tích trung thực ưu điểm và giới hạn của mô hình này so với mô hình Server-authoritative physics toàn phần.")

add_heading2(doc, "1.4. Các chức năng chính của hệ sinh thái trò chơi")
create_p(doc, "Hệ thống bao gồm đầy đủ các phân hệ chức năng hoàn chỉnh sau:")
create_p(doc, "1. Phân hệ Xác thực & Hồ sơ: Đăng ký tài khoản mới, Đăng nhập với mật khẩu mã hóa SHA-256 kèm muối (Salt), lưu trữ hồ sơ người chơi (Cấp độ, Điểm kinh nghiệm EXP, Số trận thắng, Số trận thua, Avatar đã chọn, Skin gậy đang trang bị).")
create_p(doc, "2. Phân hệ Cấu hình mạng: Nhập IP/Port máy chủ linh hoạt lúc chạy, ghi nhớ cấu hình vào file server_ip.txt, chức năng quét tự động tìm kiếm máy chủ LAN bằng UDP Broadcast và TCP Probe song song.")
create_p(doc, "3. Phân hệ Sảnh chờ & Xã hội: Sảnh chờ sang trọng, hàng đợi tìm trận ngẫu nhiên, tìm kiếm người chơi theo từ khóa, gửi/chấp nhận/từ chối lời mời kết bạn, quản lý danh sách bạn bè, chat riêng tư thời gian thực và gửi lời mời thách đấu 1v1 trực tiếp.")
create_p(doc, "4. Phân hệ Bàn đấu & Trọng tài Luật bi-a: Phòng chờ đếm ngược 13 giây, bàn đấu bi-a 2D, đường ngắm laser thông minh, bộ điều khiển xoáy ép-phê/trô bi, đồng hồ đếm ngược 60s cho mỗi lượt đánh. Áp dụng chuẩn xác luật: Ăn bi mục tiêu nhưng bi cái rơi lỗ thì chuyển lượt và đối thủ nhận bi trong tay (vẫn tính bi đã ăn), Chết bi cái đồng thời chết bi 8 trong cùng lượt thì bị xử thua ngay lập tức.")
create_p(doc, "5. Phân hệ Skin gậy & Bản quyền: Bộ sưu tập 5 Skin gậy cơ 3D (Cổ Điển Lv.1, Thiết Giáp Kabuto Lv.5, Tử Tinh Atlas Lv.10, Huyết Chiến Lucanus Lv.15, Hoàng Kim Hercules Lv.20). Server kiểm tra bản quyền mở khóa nghiêm ngặt trước khi cho phép lưu trữ. Nhạc nền cố định Arcade Speed sôi động không cho đổi bài.")
create_p(doc, "6. Phân hệ Bảng xếp hạng: Truy vấn Top người chơi theo EXP, Cấp độ và Trận thắng từ PostgreSQL, nạp dữ liệu ngầm tự động hiển thị tức thì 0ms.")

add_heading2(doc, "1.5. Công nghệ sử dụng và vai trò thực tế")
create_p(doc, "Bảng 1.1 dưới đây tổng hợp chi tiết các công nghệ được sử dụng trong toàn bộ dự án cùng vai trò thực tế được kiểm chứng trong mã nguồn:")

headers_tech = ["Thành phần", "Công nghệ sử dụng", "Phiên bản", "Vai trò thực tế trong hệ sinh thái"]
data_tech = [
    ["Ngôn ngữ chính", "Java (JDK)", "Java 17 LTS", "Ngôn ngữ hướng đối tượng chính cho cả Client và Server; cung cấp java.net và java.io."],
    ["Giao diện Client", "JavaFX SDK", "17.0.13", "Xây dựng toàn bộ giao diện; sử dụng Canvas 2D render 60 FPS, GraphicsContext, Media API."],
    ["Kênh mạng chính", "Java Socket (TCP)", "Chuẩn JDK", "Cổng 12345; truyền tải gói tin nhị phân hai chiều tin cậy giữa Client và ServerMain."],
    ["Dò tìm mạng", "DatagramSocket (UDP)", "Chuẩn JDK", "Cổng 12346; broadcast thông điệp 'BILLIARDS_DISCOVER' để tự động bắt IP Server trong mạng LAN."],
    ["Cơ sở dữ liệu", "PostgreSQL", "16.x", "Hệ quản trị CSDL quan hệ lưu trữ bền vững tài khoản, EXP, level, lịch sử đấu, bạn bè, chat."],
    ["Driver kết nối", "PostgreSQL JDBC", "42.7.3", "Thư viện postgresql-42.7.3.jar trong server/lib/; thực thi câu lệnh SQL PreparedStatement."],
    ["Bảo mật mật khẩu", "SHA-256 + Salt", "java.security", "Băm mật khẩu người dùng với chuỗi muối ngẫu nhiên trước khi lưu trữ vào bảng players."]
]
col_widths_tech = [2.5, 3.2, 2.2, 7.6]
add_table_with_caption(doc, "Bảng 1.1: Tổng hợp công nghệ cốt lõi và vai trò thực tế trong hệ thống", col_widths_tech, headers_tech, data_tech,
                       [WD_ALIGN_PARAGRAPH.LEFT, WD_ALIGN_PARAGRAPH.LEFT, WD_ALIGN_PARAGRAPH.CENTER, WD_ALIGN_PARAGRAPH.LEFT])

# ==============================================================================
# CHƯƠNG 2. PHÂN TÍCH YÊU CẦU HỆ THỐNG
# ==============================================================================
add_heading1(doc, "CHƯƠNG 2. PHÂN TÍCH YÊU CẦU HỆ THỐNG")

add_heading2(doc, "2.1. Các tác nhân sử dụng hệ thống")
create_p(doc, "Dựa trên mô hình hoạt động của trò chơi, hệ thống xác định 3 tác nhân chính tương tác với ứng dụng:")
create_p(doc, "1. Người chơi vãng lai (Guest Player): Người dùng mới khởi động ứng dụng, chưa có tài khoản. Tác nhân này có thể cấu hình địa chỉ IP máy chủ, sử dụng tính năng dò tìm server LAN qua UDP, đăng ký tài khoản mới hoặc trải nghiệm chế độ Luyện tập ngoại tuyến (Offline Practice).")
create_p(doc, "2. Người chơi đã đăng nhập (Registered Player): Người dùng đã xác thực thành công. Tác nhân này có đầy đủ quyền hạn tham gia ghép trận ngẫu nhiên, xem bảng xếp hạng, kết bạn, trò chuyện trực tuyến, gửi lời mời thách đấu, tham gia trận đấu 1v1 và mở khóa/trang bị skin gậy cơ theo cấp độ.")
create_p(doc, "3. Máy chủ hệ thống (Game Server Arbitrator): Tác nhân hệ thống đóng vai trò trọng tài trung tâm, tự động giám sát kết nối mạng, ghép cặp người chơi, đồng bộ hạt giống ván đấu, phân xử lượt đánh, phát hiện gian lận cấp độ skin và cập nhật kết quả vào cơ sở dữ liệu.")

add_heading2(doc, "2.2. Yêu cầu chức năng chi tiết")
create_p(doc, "Hệ thống đáp ứng các nhóm yêu cầu chức năng cụ thể sau:")
create_p(doc, "• YC-CN 01 (Quản lý tài khoản): Cho phép đăng ký với Email, Tên người dùng và Mật khẩu; kiểm tra trùng lặp Email/Username; Đăng nhập và trả về đầy đủ thông tin người chơi (EXP, Level, Wins, Losses, Avatar, CueColor); Tự động ngắt kết nối phiên cũ khi đăng xuất.")
create_p(doc, "• YC-CN 02 (Cấu hình mạng & Tự động tìm kiếm): Cho phép nhập địa chỉ IP và Cổng thủ công; Tự động lưu cấu hình vào file server_ip.txt; Hỗ trợ quét tự động mạng nội bộ qua UDP Broadcast 12346 và TCP Probe cổng 12345.")
create_p(doc, "• YC-CN 03 (Ghép trận tự động & Thách đấu bạn bè): Người chơi có thể tham gia hàng đợi tìm trận ngẫu nhiên (LobbyManager tự động ghép cặp 2 người); hoặc gửi lời mời thách đấu trực tiếp tới một người bạn trong danh sách online; Đối thủ có quyền chấp nhận hoặc từ chối.")
create_p(doc, "• YC-CN 04 (Phòng chờ tiền trận đấu): Hiển thị thông tin hai đối thủ, avatar, cấp độ, skin gậy cơ; đồng hồ đếm ngược 13 giây; Khi cả hai nhấn sẵn sàng hoặc hết giờ, trận đấu chính thức bắt đầu.")
create_p(doc, "• YC-CN 05 (Mô phỏng thi đấu Bi-a & Luật 8-Ball): Hiển thị bàn bi-a 2D luxury với nẹp bạc 4 góc; Hệ thống bi được xáo trộn ngẫu nhiên đồng bộ 100% nhờ hạt giống matchSeed; Cho phép kéo cơ canh lực, chỉnh điểm chạm ép-phê/trô bi; Bi di chuyển theo vật lý đàn hồi; Tự động nhận diện bi vào lỗ; Đổi lượt khi phạm lỗi (đối thủ nhận bi trong tay); Xử thua ngay nếu chết cả bi cái và bi 8 trong cùng lượt.")
create_p(doc, "• YC-CN 06 (Xã hội & Tin nhắn thời gian thực): Tìm kiếm người chơi khác theo từ khóa; Gửi/hủy lời mời kết bạn; Nhắn tin riêng tư 1-1 thời gian thực và xem lại lịch sử tin nhắn lưu trữ trong database.")
create_p(doc, "• YC-CN 07 (Bộ sưu tập Skin gậy cơ & Âm thanh): Xem trước 5 mẫu gậy cơ 3D; Trang bị skin tương ứng với cấp độ người chơi; Server kiểm tra hợp lệ trước khi lưu; Cố định bản nhạc nền Arcade Speed sôi động không cho chuyển bài.")
create_p(doc, "• YC-CN 08 (Bảng xếp hạng): Hiển thị Top người chơi xếp theo EXP/Level; Tự động nạp dữ liệu ngầm hiển thị 0ms khi chuyển cảnh.")

add_heading2(doc, "2.3. Yêu cầu phi chức năng")
create_p(doc, "• Hiệu năng và Độ mượt mà: Giao diện Client JavaFX phải duy trì tốc độ khung hình ổn định 60 FPS trên màn hình tiêu chuẩn; Tuyệt đối không để tác vụ mạng I/O làm đóng băng (freeze) luồng JavaFX Application Thread.")
create_p(doc, "• Tối ưu hóa băng thông: Gói tin mạng nhị phân có kích thước siêu nhỏ (từ 1 byte đến vài chục byte cho mỗi thao tác); Không sử dụng JSON/XML cồng kềnh nhằm tiết kiệm băng thông và giảm thiểu độ trễ đóng gói.")
create_p(doc, "• Tính toàn vẹn và Tin cậy: Dữ liệu điều khiển trận đấu và kết quả phải truyền tải qua kênh TCP đảm bảo 100% không bị mất gói, không đảo lộn thứ tự; Trạng thái vật lý giữa 2 máy phải tất định hoàn toàn.")
create_p(doc, "• Tính bảo mật: Mật khẩu người dùng được băm một chiều SHA-256 kết hợp muối (Salt) ngẫu nhiên, không lưu mật khẩu thô trong database; Kiểm tra quyền trang bị skin ở tầng Server để chống hack Client.")

add_heading2(doc, "2.4. Sơ đồ Use Case tổng thể")
create_p(doc, "Hình 2.1 dưới đây thể hiện sơ đồ Use Case tổng thể mô tả các mối quan hệ tương tác giữa Người chơi (Player Client) và các phân hệ chức năng của hệ thống trò chơi:")

add_figure(doc, os.path.join(DIAGRAMS_DIR, "use_case_diagram.png"),
           "Hình 2.1: Sơ đồ Use Case tổng thể hệ thống trò chơi 8-Ball Pool Multiplayer", width_cm=14.5)

add_heading2(doc, "2.5. Đặc tả các Use Case trọng tâm")
create_p(doc, "Dưới đây là bảng đặc tả tóm tắt cho 5 Use Case cốt lõi nhất của hệ thống mạng:")

headers_uc = ["Mã UC", "Tên Use Case", "Tác nhân", "Điều kiện tiên quyết", "Luồng sự kiện chính", "Kết quả mong đợi"]
data_uc = [
    ["UC-01", "Đăng nhập", "Người chơi", "Client đã kết nối Server TCP thành công", "1. Nhập username & password\n2. Client gửi LOGIN_REQUEST\n3. Server đối soát CSDL\n4. Server trả LOGIN_RESPONSE", "Đăng nhập thành công, nạp thông tin Level, EXP, Skin và chuyển tới Sảnh chờ."],
    ["UC-02", "Tìm kiếm Server LAN", "Người chơi", "Thiết bị kết nối chung mạng Wi-Fi/LAN", "1. Mở hộp thoại cấu hình\n2. Nhấn 'Tự động tìm kiếm'\n3. Client phát UDP Broadcast\n4. Server phản hồi IP", "Client tự động điền IP của Server vào ô kết nối mà không cần tra cứu thủ công."],
    ["UC-03", "Tìm trận đấu", "Người chơi", "Đã đăng nhập tại Sảnh chờ", "1. Nhấn 'Tìm Trận'\n2. Client gửi FIND_MATCH\n3. LobbyManager ghép cặp\n4. Server gửi MATCH_FOUND", "Tạo phòng GameRoom mới, hai người chơi cùng được chuyển tới PreGameScene."],
    ["UC-04", "Thực hiện cú đánh bi-a", "Người chơi", "Đang trong trận và tới lượt đánh", "1. Kéo chuột canh lực & xoáy\n2. Nhả chuột phát lực\n3. Gửi CUE_SHOT lên Server\n4. Server gửi GAME_STATE cho đối thủ\n5. Hai máy chạy vật lý", "Cả hai Client mô phỏng bi lăn hoàn toàn trùng khớp; người bắn gửi PHYSICS_DONE khi dừng bi."],
    ["UC-05", "Trang bị Skin gậy", "Người chơi", "Đã đăng nhập tài khoản", "1. Mở Cài đặt -> Chọn skin\n2. Nhấn Lưu trang bị\n3. Gửi SETTINGS_UPDATE\n4. Server kiểm tra Level\n5. Cập nhật PostgreSQL", "Skin được kích hoạt và xuất hiện trên bàn đấu; từ chối nếu cấp độ chưa đạt yêu cầu."]
]
col_widths_uc = [1.5, 2.5, 1.8, 2.5, 4.2, 3.0]
add_table_with_caption(doc, "Bảng 2.1: Đặc tả các Use Case cốt lõi trong hệ thống trò chơi", col_widths_uc, headers_uc, data_uc,
                       [WD_ALIGN_PARAGRAPH.CENTER, WD_ALIGN_PARAGRAPH.LEFT, WD_ALIGN_PARAGRAPH.CENTER, WD_ALIGN_PARAGRAPH.LEFT, WD_ALIGN_PARAGRAPH.LEFT, WD_ALIGN_PARAGRAPH.LEFT])

# ==============================================================================
# CHƯƠNG 3. THIẾT KẾ KIẾN TRÚC HỆ THỐNG
# ==============================================================================
add_heading1(doc, "CHƯƠNG 3. THIẾT KẾ KIẾN TRÚC HỆ THỐNG")

add_heading2(doc, "3.1. Mô hình kiến trúc tổng thể Client - Server")
create_p(doc, "Hệ thống trò chơi 8-Ball Pool Multiplayer được thiết kế dựa trên mô hình kiến trúc Client - Server đa tầng (Multi-tier Architecture) kết hợp mô phỏng phân tán (Distributed Simulation). Mô hình này phân tách ranh giới rõ ràng giữa các thành phần giao diện máy trạm, kênh truyền mạng, dịch vụ xử lý nghiệp vụ máy chủ và tầng lưu trữ dữ liệu bền vững.")
create_p(doc, "Hình 3.1 dưới đây thể hiện sơ đồ kiến trúc tổng thể của hệ thống với sự tham gia của hai Client đại diện cho hai người chơi thi đấu đối kháng trực tiếp:")

add_figure(doc, os.path.join(DIAGRAMS_DIR, "architecture_diagram.png"),
           "Hình 3.1: Sơ đồ kiến trúc tổng thể hệ thống (Hai Client JavaFX, Kênh TCP/UDP, Server, PostgreSQL)", width_cm=15.0)

create_p(doc, "Phân tích chi tiết ranh giới và trách nhiệm của từng thành phần trong sơ đồ kiến trúc:")
create_p(doc, "1. Tầng Client (Máy trạm người chơi - Client 1 & Client 2):")
create_p(doc, "• Giao diện người dùng (JavaFX UI): Quản lý vòng đời hiển thị các Scene đồ họa, Canvas 2D render 60 FPS, hệ thống hạt Particle và thu nhận tương tác người dùng (chuột, bàn phím).")
create_p(doc, "• Động cơ Vật lý cục bộ (PhysicsEngine): Chịu trách nhiệm tính toán mô phỏng chuyển động của 16 viên bi, va chạm đàn hồi giữa bi với bi, bi với băng bàn và hiệu ứng rơi lỗ. Động cơ này chạy hoàn toàn độc lập tại mỗi Client để đảm bảo độ mượt mà tối đa.")
create_p(doc, "• Mô-đun mạng máy trạm (NetworkClient): Quản lý kết nối Socket TCP với máy chủ, chạy một luồng riêng biệt (Client-Packet-Listener) để liên tục lắng nghe và tiếp nhận các gói tin đến. Khi nhận được dữ liệu, NetworkClient sử dụng Platform.runLater() để cập nhật an toàn lên luồng đồ họa JavaFX mà không gây xung đột tài nguyên.")
create_p(doc, "2. Tầng Kênh truyền Mạng (Network Transport Layer):")
create_p(doc, "• Kênh kết nối TCP (Cổng 12345): Đóng vai trò đường truyền dữ liệu chính thức, cung cấp kênh truyền hai chiều tin cậy (Reliable, In-order Byte Stream) cho toàn bộ 45 loại thông điệp nghiệp vụ.")
create_p(doc, "• Kênh Datagram UDP (Cổng 12346): Sử dụng cho dịch vụ phụ trợ tự động dò tìm máy chủ (LAN Discovery). Client phát thông điệp quảng bá 'BILLIARDS_DISCOVER' và Server phản hồi gói tin chứa thông tin IP và Cổng.")
create_p(doc, "3. Tầng Máy chủ trung tâm (Game Server):")
create_p(doc, "• Bộ điều phối kết nối (ServerMain): Lắng nghe kết nối TCP trên cổng 12345. Mỗi khi có kết nối mới được chấp nhận (accept), ServerMain sinh ra một luồng xử lý riêng biệt đóng gói trong đối tượng ClientHandler.")
create_p(doc, "• Quản lý Client (ClientHandler): Luồng làm việc độc lập của từng Client, liên tục giải mã gói tin nhị phân và điều hướng xử lý nghiệp vụ tương ứng.")
create_p(doc, "• Trọng tài trận đấu (GameRoom): Đại diện cho một phòng đấu 2 người chơi. GameRoom lưu trữ trạng thái lượt đánh hiện tại (currentTurnIndex), chuyển tiếp tham số cú đánh từ người bắn sang đối thủ, tiếp nhận báo cáo kết thúc vật lý và xử lý thắng thua.")
create_p(doc, "• Quản lý sảnh (LobbyManager) & Xác thực (AuthManager): Điều phối hàng đợi ghép cặp ngẫu nhiên và mã hóa mật khẩu an toàn SHA-256 + Salt.")
create_p(doc, "4. Tầng Cơ sở dữ liệu (PostgreSQL Database):")
create_p(doc, "• Lưu trữ bền vững dữ liệu tài khoản, chỉ số thắng/thua, kinh nghiệm EXP, cấp độ, danh sách bạn bè và tin nhắn trò chuyện.")
create_p(doc, "• Ranh giới bảo mật tuyệt đối: Client KHÔNG BAO GIỜ kết nối trực tiếp đến PostgreSQL Database. Toàn bộ thao tác truy vấn đều phải thông qua DatabaseManager tại Server bằng các câu lệnh PreparedStatement an toàn chống tấn công SQL Injection.")

add_heading2(doc, "3.2. Sơ đồ tổ chức Module và Package")
create_p(doc, "Mã nguồn của dự án được tổ chức khoa học theo mô hình Client - Server độc lập với cấu trúc package chuẩn mực, phản ánh chính xác ranh giới nghiệp vụ:")

add_figure(doc, os.path.join(DIAGRAMS_DIR, "package_diagram.png"),
           "Hình 3.2: Sơ đồ tổ chức cấu trúc gói mã nguồn (Package Structure) Client & Server", width_cm=14.5)

create_p(doc, "Cấu trúc thư mục mã nguồn chi tiết:")
create_p(doc, "• Phía Client (gói com.billiards.client):")
create_p(doc, "  - com.billiards.client: Chứa ClientMain khởi động và NetworkClient quản lý kết nối TCP.")
create_p(doc, "  - com.billiards.client.scenes: Chứa toàn bộ các màn hình giao diện (LoginScene, LobbyScene, GameScene, LeaderboardScene, SettingsDialog...).")
create_p(doc, "  - com.billiards.client.game: Chứa mô hình thực thể viên bi (Ball), động cơ vật lý 2D (PhysicsEngine) và hệ thống gậy cơ (CueSkin, CueSkinManager).")
create_p(doc, "  - com.billiards.client.protocol: Chứa lớp định dạng nhị phân Packet và enum mã hóa 45 gói tin PacketType.")
create_p(doc, "  - com.billiards.client.theme, vfx, audio: Chứa các token thiết kế chuẩn, hiệu ứng hạt và quản lý âm thanh SoundManager.")
create_p(doc, "• Phía Server (gói com.billiards.server):")
create_p(doc, "  - com.billiards.server: Chứa ServerMain, luồng ClientHandler, phòng chơi GameRoom, bộ ghép trận LobbyManager, bộ xác thực AuthManager và đối tượng DAO DatabaseManager.")
create_p(doc, "  - com.billiards.server.protocol: Chứa Packet, PacketType đồng bộ với client và quy chuẩn kiểm tra cấp độ skin CueSkinConfig.")

add_heading2(doc, "3.3. Mô hình triển khai thực tế (Deployment Diagram)")
create_p(doc, "Trong môi trường kiểm thử và vận hành thực tế của đồ án, hệ thống hỗ trợ 2 mô hình triển khai linh hoạt:")
create_p(doc, "1. Mô hình Triển khai Cục bộ (Localhost Deployment): Server và cả hai Client cùng được khởi chạy trên một máy tính cá nhân. Client 1 và Client 2 kết nối tới địa chỉ Loopback 127.0.0.1:12345. Mô hình này rất thuận tiện cho quá trình phát triển mã nguồn, gỡ lỗi (debug) và kiểm thử nhanh các tính năng logic.")
create_p(doc, "2. Mô hình Triển khai Mạng nội bộ (LAN Deployment): Máy chủ Server chạy trên một máy tính kết nối mạng Wi-Fi/LAN (ví dụ có IP 192.168.1.15). Các máy tính Client khác trong cùng lớp mạng kết nối trực tiếp tới IP này thông qua cổng TCP 12345. Tính năng UDP Discovery cho phép Client tự động quét và kết nối mà không cần cấu hình thủ công. Tường lửa (Windows Firewall) trên máy chủ cần được cấu hình cho phép kết nối Inbound qua cổng TCP 12345 và UDP 12346.")

add_heading2(doc, "3.4. Mô hình máy trạng thái ván đấu (Match State Machine)")
create_p(doc, "Mỗi ván đấu bi-a trong hệ thống trải qua một chu trình chuyển đổi trạng thái chặt chẽ dưới sự giám sát của GameRoom và Client state. Hình 3.3 dưới đây mô tả chi tiết cỗ máy trạng thái này:")

add_figure(doc, os.path.join(DIAGRAMS_DIR, "match_state_diagram.png"),
           "Hình 3.3: Sơ đồ chuyển trạng thái ván đấu (Match State Machine)", width_cm=14.5)

create_p(doc, "Các trạng thái chính trong vòng đời trận đấu:")
create_p(doc, "• IDLE: Người chơi tại sảnh chờ, sẵn sàng tìm trận hoặc thách đấu.")
create_p(doc, "• MATCHMAKING: Đang nằm trong hàng đợi của LobbyManager chờ tìm đối thủ.")
create_p(doc, "• PRE_GAME: Đã ghép cặp thành công, hiển thị đối thủ và đếm ngược 13 giây sẵn sàng.")
create_p(doc, "• IN_GAME (Phân nhánh PLAYER_AIMING hoặc OPPONENT_WAITING): Một người chơi có quyền điều khiển gậy cơ, người kia ở trạng thái theo dõi. Đồng hồ đếm ngược 60 giây hoạt động.")
create_p(doc, "• PHYSICS_RUNNING: Cú đánh đã được phát ra, cả hai máy tính cùng chạy mô phỏng vật lý 2D trong khoảng thời gian bi lăn.")
create_p(doc, "• EVALUATING: Bi đã dừng hẳn, trọng tài kiểm tra bi vào lỗ và bắt lỗi. Nếu ăn bi hợp lệ -> Đánh tiếp; Nếu không ăn bi hoặc phạm lỗi -> Đổi lượt; Nếu bi 8 vào lỗ -> Chuyển sang GAME_OVER.")
create_p(doc, "• GAME_OVER: Kết thúc trận, trao thưởng EXP và hiển thị màn hình kết quả ResultScene.")

# ==============================================================================
# CHƯƠNG 4. THIẾT KẾ VÀ TRIỂN KHAI GIAO TIẾP MẠNG
# ==============================================================================
add_heading1(doc, "CHƯƠNG 4. THIẾT KẾ VÀ TRIỂN KHAI GIAO TIẾP MẠNG")

create_p(doc, "Chương 4 là chương trọng tâm của báo cáo đồ án Lập trình mạng. Chương này trình bày chi tiết toàn bộ cơ chế thiết lập kết nối, quản lý đa luồng, đặc tả cấu trúc gói tin nhị phân, danh mục 45 loại thông điệp mạng, cơ chế đồng bộ vật lý tất định và các sơ đồ tuần tự thể hiện đường đi của dữ liệu qua hệ thống.")

add_heading2(doc, "4.1. Khởi tạo Server và kết nối Client")
create_p(doc, "Máy chủ Server được khởi động thông qua lớp ServerMain. Quá trình khởi tạo máy chủ thực hiện đồng thời hai dịch vụ mạng:")
create_p(doc, "1. Dịch vụ TCP Server chính: Lắng nghe kết nối TCP trên cổng 12345 bằng ServerSocket:")

add_code_block(doc, """// ServerMain.java - Khởi tạo ServerSocket TCP
try (ServerSocket serverSocket = new ServerSocket(PORT)) {
    System.out.println("[Server] Đang lắng nghe trên cổng " + PORT + "...");
    while (true) {
        Socket clientSocket = serverSocket.accept();
        clientSocket.setKeepAlive(true);
        ClientHandler handler = new ClientHandler(clientSocket, authManager, lobbyManager, db);
        Thread thread = new Thread(handler);
        thread.setDaemon(true);
        thread.start();
    }
}""")

create_p(doc, "2. Dịch vụ LAN Discovery qua UDP: Chạy trên một luồng nền riêng biệt sử dụng DatagramSocket lắng nghe trên cổng 12346:")

add_code_block(doc, """// ServerMain.java - Dịch vụ UDP Discovery Listener
try (DatagramSocket socket = new DatagramSocket(12346)) {
    socket.setBroadcast(true);
    byte[] buffer = new byte[256];
    while (true) {
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        String msg = new String(packet.getData(), 0, packet.getLength()).trim();
        if ("BILLIARDS_DISCOVER".equals(msg)) {
            byte[] replyData = ("BILLIARDS_SERVER:" + PORT).getBytes();
            DatagramPacket reply = new DatagramPacket(
                replyData, replyData.length, packet.getAddress(), packet.getPort()
            );
            socket.send(reply);
        }
    }
}""")

create_p(doc, "Phía Client, lớp NetworkClient kết nối tới Server thông qua phương thức connect() với cơ chế timeout 2.5 giây để tránh hiện tượng treo ứng dụng nếu địa chỉ IP không phản hồi:")

add_code_block(doc, """// NetworkClient.java - Kết nối Socket phía Client
socket = new Socket();
socket.connect(new InetSocketAddress(this.host, this.port), 2500);
socket.setKeepAlive(true);
in  = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
connected = true;

Thread listenerThread = new Thread(this::listenLoop);
listenerThread.setDaemon(true);
listenerThread.setName("Client-Packet-Listener");
listenerThread.start();""")

add_heading2(doc, "4.2. Mô hình quản lý đa luồng (Multi-threading Model)")
create_p(doc, "Hệ thống áp dụng mô hình kiến trúc đa luồng để giải quyết triệt để bài toán đồng thời (Concurrency):")
create_p(doc, "• Tại Server: Áp dụng mô hình một luồng trên một Client (Thread-per-client Model). Mỗi khi ServerSocket chấp nhận một kết nối mới, một thể hiện của ClientHandler được khởi tạo và chạy trên một Thread riêng biệt. ClientHandler liên tục thực hiện vòng lặp đọc dữ liệu từ DataInputStream. Danh sách các người chơi đang hoạt động được quản lý an toàn đa luồng thông qua ConcurrentHashMap<String, ClientHandler> onlinePlayers, cho phép tra cứu, gửi tin nhắn và điều hướng gói tin nhanh chóng với độ phức tạp O(1).")
create_p(doc, "• Tại Client: Tách biệt hoàn toàn giữa hai luồng thực thi:")
create_p(doc, "  - JavaFX Application Thread: Luồng chính chịu trách nhiệm xử lý sự kiện đồ họa, nhận tương tác chuột của người chơi, cập nhật AnimationTimer ở tần số 60 FPS.")
create_p(doc, "  - Client-Packet-Listener Thread: Luồng nền chạy hàm listenLoop() liên tục chặn (block) trên phương thức Packet.readType(in) để đợi dữ liệu từ Server. Khi gói tin đến, luồng nền giải mã payload và chuyển giao tác vụ cập nhật giao diện vào hàng đợi của JavaFX bằng lệnh Platform.runLater(() -> { ... }). Kỹ thuật này ngăn chặn hoàn toàn lỗi treo giao diện (UI Freeze) và vi phạm luồng giao diện (Not on FX application thread exception).")

add_heading2(doc, "4.3. Cấu trúc gói tin và tuần tự hóa dữ liệu nhị phân (Binary Protocol)")
create_p(doc, "Thay vì sử dụng các định dạng văn bản cồng kềnh như JSON hay XML gây tốn băng thông và chi phí phân tích cú pháp (parsing overhead), dự án tự thiết kế một giao thức nhị phân mức ứng dụng (Custom Binary Protocol) tối ưu:")
create_p(doc, "1. Định dạng khung gói tin: Mỗi gói tin bắt đầu bằng đúng 1 byte duy nhất đại diện cho mã định danh loại gói tin (PacketType Code, từ 1 đến 45), theo sau là các trường dữ liệu cụ thể tùy theo từng loại gói tin:")
create_p(doc, "Khung gói tin = [1 byte: PacketType Code] + [Payload: Các trường dữ liệu tuần tự]", bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
create_p(doc, "2. Tuần tự hóa kiểu dữ liệu nguyên thủy: Toàn bộ kiểu dữ liệu số nguyên (int: 4 bytes), số thực dấu chấm động (double: 8 bytes), số nguyên dài (long: 8 bytes), giá trị logic (boolean: 1 byte) đều được ghi trực tiếp dưới dạng nhị phân Big-Endian chuẩn của Java thông qua DataOutputStream và DataInputStream.")
create_p(doc, "3. Phân tách chuỗi ký tự (Length-Prefixed Strings): Để truyền tải chuỗi ký tự an toàn và chính xác, hệ thống áp dụng cơ chế tiền tố độ dài (Length-prefixed): ghi 4 byte số nguyên biểu diễn độ dài chuỗi UTF-8 tính theo byte, sau đó ghi trực tiếp mảng byte dữ liệu. Phía nhận đọc 4 byte độ dài rồi dùng readFully(bytes) để đọc chính xác số lượng byte quy định, loại bỏ hoàn toàn hiện tượng phân mảnh hay dính gói tin (TCP stream fragmentation/coalescing):")

add_code_block(doc, """// Packet.java - Ghi và đọc chuỗi với tiền tố độ dài
public static void writeString(DataOutputStream out, String value) throws IOException {
    byte[] bytes = value.getBytes("UTF-8");
    out.writeInt(bytes.length);
    out.write(bytes);
}

public static String readString(DataInputStream in) throws IOException {
    int length = in.readInt();
    if (length < 0 || length > 65536) throw new IOException("Invalid string length: " + length);
    byte[] bytes = new byte[length];
    in.readFully(bytes);
    return new String(bytes, "UTF-8");
}""")

add_heading2(doc, "4.4. Bảng đặc tả chi tiết 45 mã gói tin trong hệ thống")
create_p(doc, "Bảng 4.1 dưới đây đặc tả đầy đủ 45 loại thông điệp nhị phân được định nghĩa trong PacketType.java của cả Client và Server:")

headers_pkt = ["Code", "Tên PacketType", "Chiều", "Cấu trúc Payload dữ liệu", "Mục đích & Nơi xử lý"]
data_pkt = [
    ["1", "LOGIN_REQUEST", "C ➔ S", "String username, String password", "Client gửi yêu cầu đăng nhập tài khoản. Xử lý tại ClientHandler."],
    ["2", "LOGIN_RESPONSE", "S ➔ C", "bool success, String username, int exp, int level, int wins, int losses, int avatarId, String cueColor", "Server trả kết quả xác thực kèm toàn bộ thông tin hồ sơ người chơi."],
    ["3", "REGISTER_REQUEST", "C ➔ S", "String email, String username, String password", "Client gửi thông tin đăng ký tài khoản mới."],
    ["4", "REGISTER_RESPONSE", "S ➔ C", "bool success, String message", "Server trả về thông báo tạo tài khoản thành công hoặc lý do lỗi."],
    ["5", "FIND_MATCH", "C ➔ S", "(Không có payload)", "Người chơi nhấn tìm trận, đưa vào hàng đợi của LobbyManager."],
    ["6", "MATCH_FOUND", "S ➔ C", "String oppName, int oppLevel, int oppAvatarId, String oppCueColor", "Thông báo đã ghép cặp thành công, gửi kèm thông tin đối thủ."],
    ["7", "CANCEL_FIND", "C ➔ S", "(Không có payload)", "Người chơi hủy tìm trận, xóa khỏi hàng đợi LobbyManager."],
    ["8", "PREGAME_READY", "C ➔ S", "(Không có payload)", "Báo sẵn sàng trong phòng chờ đếm ngược 13s PreGameScene."],
    ["9", "GAME_START", "S ➔ C", "int playerIndex (0/1), bool isFirstTurn, long matchSeed", "Khởi động ván đấu. Gửi matchSeed để 2 client xáo bi đồng bộ 100%."],
    ["10", "CUE_SHOT", "C ➔ S", "double angle, double force, double cueX, double cueY, double spinX, double spinY", "Người bắn gửi tham số cú đánh (góc, lực, vị trí, độ xoáy ép-phê)."],
    ["11", "GAME_STATE", "S ➔ C", "double angle, double force, double cueX, double cueY, double spinX, double spinY", "Server chuyển tiếp tham số cú đánh tới duy nhất đối thủ."],
    ["12", "TURN_CHANGE", "S ➔ C", "bool isYourTurn, bool isBallInHand, String reason", "Server đổi lượt đánh, cấp quyền đặt bi tự do nếu đối thủ phạm lỗi."],
    ["13", "BALL_POCKETED", "S ➔ C", "int ballIndex", "Thông báo sự kiện bi mục tiêu rơi vào lỗ."],
    ["14", "GAME_OVER", "S ➔ C", "bool youWin, int expGained", "Thông báo kết thúc ván đấu, thưởng điểm kinh nghiệm EXP."],
    ["15", "LEADERBOARD_REQUEST", "C ➔ S", "(Không có payload)", "Client yêu cầu nạp danh sách Bảng xếp hạng từ máy chủ."],
    ["16", "LEADERBOARD_RESPONSE", "S ➔ C", "String 'rank|user|level|exp|wins|losses;...'", "Server trả về dữ liệu BXH Top người chơi."],
    ["17", "DISCONNECT", "C ⇄ S", "(Không có payload)", "Thông báo ngắt kết nối chủ động hoặc đóng socket."],
    ["18", "PING", "C ⇄ S", "(Không có payload)", "Gói tin kiểm tra độ trễ và liveness của kết nối."],
    ["19", "ERROR", "S ➔ C", "String errorMessage", "Server báo lỗi xử lý ngoại lệ cho client."],
    ["20", "PHYSICS_DONE", "C ➔ S", "bool keepTurn, bool isFoul", "Người vừa bắn báo bi đã dừng hẳn, gửi trạng thái ăn bi và lỗi."],
    ["21", "CLAIM_WIN", "C ➔ S", "bool isWin", "Client báo kết quả khi bi 8 vào lỗ (thắng hợp lệ hoặc thua phạm quy)."],
    ["22", "SETTINGS_UPDATE", "C ➔ S", "int avatarId, String cueColor", "Client gửi yêu cầu thay đổi Avatar và trang bị Skin gậy cơ."],
    ["23", "SETTINGS_RESPONSE", "S ➔ C", "bool success", "Server trả về kết quả lưu cài đặt (từ chối nếu chưa đủ cấp độ skin)."],
    ["24", "SEARCH_PLAYERS_REQ", "C ➔ S", "String keyword", "Tìm kiếm người chơi trực tuyến theo từ khóa username."],
    ["25", "SEARCH_PLAYERS_RESP", "S ➔ C", "String 'username|level|avatarId;...'", "Trả về danh sách kết quả người chơi phù hợp."],
    ["26", "ADD_FRIEND_REQUEST", "C ➔ S", "String friendUsername", "Gửi lời mời kết bạn tới người chơi mục tiêu."],
    ["27", "ADD_FRIEND_RESPONSE", "S ➔ C", "bool success, String message", "Trả về kết quả gửi lời mời kết bạn."],
    ["28", "FRIEND_LIST_REQUEST", "C ➔ S", "(Không có payload)", "Yêu cầu nạp danh sách bạn bè hiện tại của người chơi."],
    ["29", "FRIEND_LIST_RESPONSE", "S ➔ C", "String 'username|level|avatarId|status;...'", "Trả về danh sách bạn bè kèm trạng thái trực tuyến/ngoại tuyến."],
    ["30", "FRIEND_ACCEPT_REQ", "C ➔ S", "String senderUsername", "Chấp nhận lời mời kết bạn đang chờ."],
    ["31", "FRIEND_ACCEPT_RESP", "S ➔ C", "bool success, String message", "Kết quả xử lý đồng ý kết bạn."],
    ["32", "FRIEND_DECLINE_REQ", "C ➔ S", "String senderUsername", "Từ chối lời mời kết bạn."],
    ["33", "FRIEND_DECLINE_RESP", "S ➔ C", "bool success, String message", "Kết quả xử lý từ chối kết bạn."],
    ["34", "PENDING_REQUESTS_REQ", "C ➔ S", "(Không có payload)", "Yêu cầu danh sách các lời mời kết bạn đang chờ phê duyệt."],
    ["35", "PENDING_REQUESTS_RESP", "S ➔ C", "String 'username|level|avatarId;...'", "Trả về danh sách lời mời kết bạn đang chờ."],
    ["36", "REMOVE_FRIEND_REQ", "C ➔ S", "String friendUsername", "Xóa bạn bè khỏi danh bạ."],
    ["37", "REMOVE_FRIEND_RESP", "S ➔ C", "bool success, String message", "Kết quả xử lý xóa bạn bè."],
    ["38", "CHAT_MESSAGE_REQ", "C ➔ S", "String recipientUsername, String message", "Gửi tin nhắn riêng tới một người bạn cụ thể."],
    ["39", "CHAT_MESSAGE_RESP", "S ➔ C", "String senderUsername, String message", "Server chuyển tiếp tin nhắn thời gian thực tới người nhận."],
    ["40", "CHAT_HISTORY_REQ", "C ➔ S", "String friendUsername", "Yêu cầu nạp lịch sử tin nhắn trò chuyện với bạn bè."],
    ["41", "CHAT_HISTORY_RESP", "S ➔ C", "String 'sender|msg;sender|msg;...'", "Trả về các tin nhắn lịch sử lưu trong bảng messages."],
    ["42", "CHALLENGE_REQUEST", "C ➔ S", "String targetUsername", "Gửi lời mời thách đấu 1v1 trực tiếp tới bạn bè."],
    ["43", "CHALLENGE_RECEIVE", "S ➔ C", "String challengerUsername", "Server chuyển thông báo thách đấu tới đối thủ."],
    ["44", "CHALLENGE_RESPONSE", "C ➔ S", "String challengerUsername, bool accept", "Đối thủ trả lời đồng ý hoặc từ chối thách đấu."],
    ["45", "CHALLENGE_RESULT", "S ➔ C", "bool success, String message", "Thông báo kết quả thách đấu; khởi tạo phòng đấu nếu đồng ý."]
]
col_widths_pkt = [1.2, 3.6, 1.6, 4.8, 4.3]
add_table_with_caption(doc, "Bảng 4.1: Đặc tả chi tiết 45 mã gói tin nhị phân trong hệ thống 8-Ball Pool", col_widths_pkt, headers_pkt, data_pkt,
                       [WD_ALIGN_PARAGRAPH.CENTER, WD_ALIGN_PARAGRAPH.LEFT, WD_ALIGN_PARAGRAPH.CENTER, WD_ALIGN_PARAGRAPH.LEFT, WD_ALIGN_PARAGRAPH.LEFT])

add_heading2(doc, "4.5. Cơ chế quản lý phiên và phòng đấu (PlayerSession & GameRoom)")
create_p(doc, "Khi người chơi đăng nhập thành công, Server lưu trữ PlayerInfo gắn liền với luồng ClientHandler. Phía Client, đối tượng PlayerSession lưu giữ toàn bộ thông tin phiên làm việc:")
create_p(doc, "• Quản lý phòng đấu với GameRoom: Khi hai người chơi được ghép cặp thành công, ServerMain khởi tạo một thực thể GameRoom(player1, player2, db). Lớp GameRoom hoạt động như một Trọng tài độc lập:")
create_p(doc, "  - Quản lý lượt đánh hiện tại bằng biến nguyên tử AtomicInteger currentTurnIndex (0 ứng với Player 1, 1 ứng với Player 2).")
create_p(doc, "  - Chuyển tiếp cú đánh: Khi nhận được CUE_SHOT từ người có lượt, GameRoom kiểm tra tính hợp lệ và chỉ chuyển tiếp gói tin GAME_STATE tới người đối diện. Người bắn không nhận lại gói tin này để tránh bắn đúp hai lần.")
create_p(doc, "  - Xử lý chuyển lượt: Khi nhận được PHYSICS_DONE từ người bắn, nếu keepTurn == true, cho phép người đó đánh tiếp; nếu keepTurn == false, kích hoạt hàm switchTurn(isFoul, reason), gửi TURN_CHANGE kèm cờ isBallInHand nếu có lỗi phạm quy.")
create_p(doc, "  - Xử lý kết thúc trận: Khi nhận CLAIM_WIN từ người bắn hoặc phát hiện ngắt kết nối, GameRoom gọi endGame(), ghi nhận vào PostgreSQL, cộng 100 EXP cho người thắng và giải phóng tham chiếu phòng chơi.")

add_heading2(doc, "4.6. Cơ chế đồng bộ ván đấu và vật lý tất định")
create_p(doc, "Một trong những bài toán khó khăn nhất của game bi-a trực tuyến là làm sao để hai người chơi nhìn thấy chuyển động của bi hoàn toàn giống nhau từng milimet mà không phải gửi tọa độ x, y liên tục hàng trăm lần mỗi giây (gây nghẽn mạng).")
create_p(doc, "Hệ thống giải quyết bài toán này bằng giải pháp Đồng bộ hạt giống & Vật lý tất định cục bộ (Seed-Synchronized Deterministic Physics):")
create_p(doc, "1. Đồng bộ khởi tạo bằng matchSeed: Khi trận đấu bắt đầu, Server sinh một số nguyên 64-bit ngẫu nhiên matchSeed và gửi cho cả 2 máy trong gói GAME_START. Cả hai Client cùng khởi tạo đối tượng Random(matchSeed) để xáo trộn vị trí 14 bi mục tiêu và phân chia nhóm bi. Kết quả là bàn bi của cả hai người chơi có tọa độ xuất phát trùng khớp nhau 100%.")
create_p(doc, "2. Đồng bộ cú đánh với 6 tham số: Khi người chơi phát lực đánh, Client chỉ gửi duy nhất 6 con số dấu chấm động lên Server: góc đánh (angle), lực đánh (force), tọa độ bi cái (cueX, cueY), độ xoáy ép-phê ngang (spinX) và độ trô/cule dọc (spinY).")
create_p(doc, "3. Mô phỏng tất định độc lập: Cả hai máy tính cùng nạp 6 tham số trên vào PhysicsEngine cục bộ. Do công thức toán học va chạm đàn hồi 2D và ma sát nỉ là hoàn toàn tất định (Deterministic), 16 viên bi trên cả 2 màn hình sẽ lăn, nảy băng và rơi vào lỗ hoàn toàn trùng khớp mà không cần trao đổi thêm bất kỳ gói tin tọa độ nào trong lúc bi đang chuyển động.")
create_p(doc, "4. Trọng tài lượt đánh và Cập nhật luật mới:")
create_p(doc, "• Luật ăn bi khi bi cái rơi lỗ: Nếu người chơi đánh rơi bi mục tiêu của mình nhưng bi cái cũng rơi vào lỗ (Scratch), bi mục tiêu vẫn nằm trong lỗ (vẫn cho ăn bi / trừ bi cần đánh), nhưng người chơi bị mất lượt. Lượt chuyển sang đối thủ và đối thủ nhận quyền Đặt bi tự do (Ball-in-hand).")
create_p(doc, "• Luật chết bi cái và bi 8 trong cùng lượt: Nếu bi cái và bi số 8 cùng rơi vào lỗ trong cùng một lượt đánh, người chơi bị xử THUA NGAY LẬP TỨC (bất kể đã dọn sạch nhóm bi hay chưa). Đối thủ được công nhận chiến thắng tuyệt đối.")

add_heading2(doc, "4.7. Các sơ đồ tuần tự chi tiết")
create_p(doc, "Dưới đây là 5 sơ đồ tuần tự mô tả chi tiết đường đi của các gói tin mạng trong các nghiệp vụ cốt lõi:")

add_heading3(doc, "4.7.1. Sơ đồ tuần tự Đăng nhập và Xác thực tài khoản")
create_p(doc, "Hình 4.1 mô tả tiến trình từ khi người dùng nhập thông tin trên LoginScene, qua mạng TCP tới Server, đối soát cơ sở dữ liệu PostgreSQL và trả về phiên làm việc:")
add_figure(doc, os.path.join(DIAGRAMS_DIR, "seq_login.png"),
           "Hình 4.1: Sơ đồ tuần tự: Tiến trình xác thực và đăng nhập tài khoản", width_cm=14.0)

add_heading3(doc, "4.7.2. Sơ đồ tuần tự Ghép trận tự động và Khởi tạo ván đấu")
create_p(doc, "Hình 4.2 mô tả tiến trình hai Client gửi yêu cầu ghép trận, LobbyManager ghép cặp, khởi tạo GameRoom, đếm ngược tại PreGameScene và đồng bộ matchSeed:")
add_figure(doc, os.path.join(DIAGRAMS_DIR, "seq_matchmaking.png"),
           "Hình 4.2: Sơ đồ tuần tự: Ghép trận tự động và khởi tạo bàn đấu", width_cm=14.5)

add_heading3(doc, "4.7.3. Sơ đồ tuần tự Thực hiện cú đánh bi-a và Đồng bộ vật lý")
create_p(doc, "Hình 4.3 mô tả tiến trình người bắn thực hiện cú đánh, Server chuyển tiếp GAME_STATE, hai Client chạy PhysicsEngine song song và báo cáo kết thúc lượt:")
add_figure(doc, os.path.join(DIAGRAMS_DIR, "seq_cue_shot.png"),
           "Hình 4.3: Sơ đồ tuần tự: Thực hiện cú đánh bi-a và đồng bộ vật lý 2 máy", width_cm=14.5)

add_heading3(doc, "4.7.4. Sơ đồ tuần tự Kết thúc ván đấu, Ghi nhận EXP và Thăng cấp")
create_p(doc, "Hình 4.4 mô tả tiến trình bi số 8 rơi vào lỗ, Client gửi CLAIM_WIN, Server ghi nhận kết quả vào bảng match_history, cộng 100 EXP và thông báo GAME_OVER:")
add_figure(doc, os.path.join(DIAGRAMS_DIR, "seq_game_over.png"),
           "Hình 4.4: Sơ đồ tuần tự: Kết thúc ván đấu, cập nhật EXP và thăng cấp", width_cm=14.5)

add_heading3(doc, "4.7.5. Sơ đồ tuần tự Mở khóa, Kiểm tra bản quyền và Trang bị Skin gậy")
create_p(doc, "Hình 4.5 mô tả tiến trình Client gửi yêu cầu đổi skin, Server kiểm tra cấp độ qua CueSkinConfig, lưu vào database và áp dụng ngoại hình mới:")
add_figure(doc, os.path.join(DIAGRAMS_DIR, "seq_skin_equip.png"),
           "Hình 4.5: Sơ đồ tuần tự: Kiểm tra mở khóa và trang bị Skin gậy cơ", width_cm=14.5)

add_heading2(doc, "4.8. Xử lý ngắt kết nối và phục hồi lỗi mạng")
create_p(doc, "Trong môi trường mạng thực tế, sự cố mất kết nối đột ngột (rớt mạng Wi-Fi, tắt ứng dụng đột ngột) là không thể tránh khỏi. Hệ thống xử lý các tình huống này như sau:")
create_p(doc, "• Bắt ngoại lệ tại Server: Vòng lặp run() của ClientHandler bắt các ngoại lệ EOFException hoặc SocketException khi socket bị đóng. Khi đó, phương thức onDisconnect() được kích hoạt:")
create_p(doc, "  - Xóa client khỏi danh sách onlinePlayers.")
create_p(doc, "  - Nếu client đang trong hàng đợi ghép trận, tự động xóa khỏi LobbyManager.")
create_p(doc, "  - Nếu client đang trong ván đấu dở dang (currentRoom != null), gọi room.handleDisconnect(this). Máy chủ tự động xử người chơi còn lại THẮNG TRẬN, ghi nhận kết quả và cộng EXP để đảm bảo quyền lợi.")
create_p(doc, "• Phía Client: Khi socket bị đứt, listenLoop() kết thúc và đặt cờ connected = false. Giao diện thông báo mất kết nối và cho phép người dùng mở hộp thoại cấu hình mạng để kết nối lại.")

# ==============================================================================
# CHƯƠNG 5. THIẾT KẾ CƠ SỞ DỮ LIỆU
# ==============================================================================
add_heading1(doc, "CHƯƠNG 5. THIẾT KẾ CƠ SỞ DỮ LIỆU")

add_heading2(doc, "5.1. Vai trò của cơ sở dữ liệu quan hệ PostgreSQL")
create_p(doc, "Hệ thống sử dụng hệ quản trị cơ sở dữ liệu quan hệ PostgreSQL 16 (billiards_db) nhằm lưu trữ bền vững toàn bộ dữ liệu người dùng, lịch sử thi đấu và các mối quan hệ xã hội. Việc sử dụng PostgreSQL mang lại tính toàn vẹn dữ liệu cao (ACID), hỗ trợ khóa ngoại nghiêm ngặt, cơ chế tự tăng SERIAL và khả năng đánh chỉ mục tối ưu cho các truy vấn xếp hạng điểm số.")

add_heading2(doc, "5.2. Sơ đồ thực thể quan hệ (ERD)")
create_p(doc, "Hình 5.1 dưới đây thể hiện sơ đồ thực thể quan hệ (Entity-Relationship Diagram) của cơ sở dữ liệu trò chơi được xây dựng chính xác theo schema.sql:")

add_figure(doc, os.path.join(DIAGRAMS_DIR, "database_erd.png"),
           "Hình 5.1: Sơ đồ thực thể quan hệ cơ sở dữ liệu (PostgreSQL ERD)", width_cm=15.0)

add_heading2(doc, "5.3. Từ điển dữ liệu chi tiết các bảng")
create_p(doc, "Dưới đây là từ điển dữ liệu chi tiết cho 4 bảng trong cơ sở dữ liệu billiards_db:")

# Bảng players
headers_t1 = ["Tên cột", "Kiểu dữ liệu", "Ràng buộc", "Mặc định", "Ý nghĩa và Mục đích sử dụng"]
data_t1 = [
    ["id", "SERIAL", "PRIMARY KEY", "Tự tăng", "Mã định danh duy nhất của người chơi."],
    ["email", "VARCHAR(100)", "UNIQUE, NOT NULL", "", "Địa chỉ thư điện tử dùng để đăng ký tài khoản."],
    ["username", "VARCHAR(50)", "UNIQUE, NOT NULL", "", "Tên đăng nhập và hiển thị công khai trong trò chơi."],
    ["password_hash", "VARCHAR(255)", "NOT NULL", "", "Chuỗi băm mật khẩu bảo mật (SHA-256 + Salt)."],
    ["exp", "INT", "", "0", "Điểm kinh nghiệm tích lũy (+100 EXP mỗi trận thắng)."],
    ["level", "INT", "", "1", "Cấp độ người chơi (tính theo công thức 1 + exp / 200)."],
    ["wins", "INT", "", "0", "Tổng số trận thắng trong các ván đấu 1v1."],
    ["losses", "INT", "", "0", "Tổng số trận thua trong các ván đấu 1v1."],
    ["avatar_id", "INT", "", "0", "Mã định danh Avatar người chơi đang trang bị (0-15)."],
    ["cue_color", "VARCHAR(20)", "", "'#DEB887'", "Mã định danh Skin gậy cơ đang trang bị (cue_default, cue_hercules...)."],
    ["created_at", "TIMESTAMP", "", "NOW()", "Thời điểm tạo tài khoản."]
]
col_widths_t1 = [2.2, 2.5, 2.8, 1.8, 6.2]
add_table_with_caption(doc, "Bảng 5.1: Từ điển dữ liệu bảng players (Quản lý người chơi)", col_widths_t1, headers_t1, data_t1,
                       [WD_ALIGN_PARAGRAPH.CENTER, WD_ALIGN_PARAGRAPH.CENTER, WD_ALIGN_PARAGRAPH.CENTER, WD_ALIGN_PARAGRAPH.CENTER, WD_ALIGN_PARAGRAPH.LEFT])

# Bảng match_history
headers_t2 = ["Tên cột", "Kiểu dữ liệu", "Ràng buộc", "Mặc định", "Ý nghĩa và Mục đích sử dụng"]
data_t2 = [
    ["id", "SERIAL", "PRIMARY KEY", "Tự tăng", "Mã định danh trận đấu."],
    ["winner_id", "INT", "FK -> players(id)", "", "Mã người chơi giành chiến thắng."],
    ["loser_id", "INT", "FK -> players(id)", "", "Mã người chơi thất bại."],
    ["exp_gained", "INT", "", "100", "Điểm kinh nghiệm thưởng cho người thắng."],
    ["played_at", "TIMESTAMP", "", "NOW()", "Thời điểm kết thúc ván đấu."]
]
col_widths_t2 = [2.2, 2.5, 3.2, 1.8, 5.8]
add_table_with_caption(doc, "Bảng 5.2: Từ điển dữ liệu bảng match_history (Lịch sử ván đấu)", col_widths_t2, headers_t2, data_t2,
                       [WD_ALIGN_PARAGRAPH.CENTER, WD_ALIGN_PARAGRAPH.CENTER, WD_ALIGN_PARAGRAPH.CENTER, WD_ALIGN_PARAGRAPH.CENTER, WD_ALIGN_PARAGRAPH.LEFT])

# Bảng friendships
headers_t3 = ["Tên cột", "Kiểu dữ liệu", "Ràng buộc", "Mặc định", "Ý nghĩa và Mục đích sử dụng"]
data_t3 = [
    ["user_id", "INT", "PK, FK -> players(id)", "", "Mã người chơi gửi lời mời kết bạn."],
    ["friend_id", "INT", "PK, FK -> players(id)", "", "Mã người chơi nhận lời mời kết bạn."],
    ["status", "VARCHAR(20)", "", "'PENDING'", "Trạng thái quan hệ: PENDING, ACCEPTED, BLOCKED."],
    ["created_at", "TIMESTAMP", "", "NOW()", "Thời điểm kết bạn hoặc gửi yêu cầu."]
]
col_widths_t3 = [2.2, 2.5, 3.5, 1.8, 5.5]
add_table_with_caption(doc, "Bảng 5.3: Từ điển dữ liệu bảng friendships (Danh sách quan hệ bạn bè)", col_widths_t3, headers_t3, data_t3,
                       [WD_ALIGN_PARAGRAPH.CENTER, WD_ALIGN_PARAGRAPH.CENTER, WD_ALIGN_PARAGRAPH.CENTER, WD_ALIGN_PARAGRAPH.CENTER, WD_ALIGN_PARAGRAPH.LEFT])

# Bảng messages
headers_t4 = ["Tên cột", "Kiểu dữ liệu", "Ràng buộc", "Mặc định", "Ý nghĩa và Mục đích sử dụng"]
data_t4 = [
    ["id", "SERIAL", "PRIMARY KEY", "Tự tăng", "Mã định danh tin nhắn."],
    ["sender_id", "INT", "FK -> players(id)", "", "Mã người gửi tin nhắn."],
    ["receiver_id", "INT", "FK -> players(id)", "", "Mã người nhận tin nhắn."],
    ["content", "TEXT", "NOT NULL", "", "Nội dung văn bản tin nhắn trò chuyện."],
    ["is_read", "BOOLEAN", "", "FALSE", "Trạng thái đã đọc tin nhắn hay chưa."],
    ["sent_at", "TIMESTAMP", "", "NOW()", "Thời điểm gửi tin nhắn."]
]
col_widths_t4 = [2.2, 2.5, 3.2, 1.8, 5.8]
add_table_with_caption(doc, "Bảng 5.4: Từ điển dữ liệu bảng messages (Tin nhắn trò chuyện)", col_widths_t4, headers_t4, data_t4,
                       [WD_ALIGN_PARAGRAPH.CENTER, WD_ALIGN_PARAGRAPH.CENTER, WD_ALIGN_PARAGRAPH.CENTER, WD_ALIGN_PARAGRAPH.CENTER, WD_ALIGN_PARAGRAPH.LEFT])

add_heading2(doc, "5.4. Các ràng buộc toàn vẹn và chỉ mục (Index) tối ưu hóa")
create_p(doc, "Để đảm bảo hiệu năng cao khi số lượng người chơi và bản ghi lịch sử trận đấu tăng lên, lược đồ CSDL được tối ưu hóa bằng các chỉ mục:")
create_p(doc, "• Chỉ mục idx_players_exp: CREATE INDEX idx_players_exp ON players(exp DESC); Giúp truy vấn Top người chơi có điểm EXP cao nhất cho Bảng xếp hạng đạt tốc độ O(log N) mà không cần quét tuần tự toàn bộ bảng (Full Table Scan).")
create_p(doc, "• Chỉ mục idx_players_username: Đảm bảo kiểm tra trùng lặp và tìm kiếm người chơi theo tên cực nhanh.")
create_p(doc, "• Khóa ngoại toàn vẹn (Foreign Key Constraints): Tự động ngăn chặn việc ghi nhận trận đấu hoặc tin nhắn từ các mã người chơi không tồn tại trong hệ thống.")

add_heading2(doc, "5.5. Cơ chế lưu trữ và mở khóa Skin gậy cơ")
create_p(doc, "Một điểm nhấn quan trọng trong kiến trúc cơ sở dữ liệu của dự án: Hệ thống KHÔNG tạo bảng kho đồ cồng kềnh (user_skins) để lưu trữ từng skin người chơi sở hữu. Thay vào đó:")
create_p(doc, "• Cột players.cue_color lưu trữ mã ID của Skin mà người chơi đang trang bị thực tế (ví dụ: 'cue_hercules', 'cue_kabuto').")
create_p(doc, "• Quyền sở hữu và mở khóa skin được suy diễn toán học trực tiếp từ cấp độ người chơi thông qua quy chuẩn CueSkinConfig.isSkinUnlocked(cueColor, playerLevel).")
create_p(doc, "Cách thiết kế này vừa tối ưu dung lượng lưu trữ CSDL, vừa đảm bảo tính nhất quán dữ liệu: khi người chơi thăng cấp, các skin tương ứng tự động mở khóa ngay lập tức mà không cần thực hiện thêm bất kỳ câu lệnh INSERT nào vào database.")

# ==============================================================================
# CHƯƠNG 6. GIAO DIỆN NGƯỜI DÙNG VÀ CHỨC NĂNG THỰC TẾ
# ==============================================================================
add_heading1(doc, "CHƯƠNG 6. GIAO DIỆN NGƯỜI DÙNG VÀ CHỨC NĂNG THỰC TẾ")

add_heading2(doc, "6.1. Phong cách thiết kế Modern Luxury Billiards Club")
create_p(doc, "Giao diện trò chơi được thiết kế theo phong cách Câu lạc bộ Bi-a thượng lưu hiện đại (Modern Luxury Billiards Club) với những nét đặc trưng nổi bật:")
create_p(doc, "• Khung hình vuông phẳng: Toàn bộ các bảng điều khiển, nút bấm, avatar và bàn đấu đều sử dụng khung hình vuông vức không bo tròn (border-radius: 0), tạo cảm giác nam tính, sang trọng và chuẩn mực thể thao.")
create_p(doc, "• Nẹp tráng bạc 4 góc (SilverCornerPane): Mỗi khung chức năng đều được viền đồng kim loại (#C2A36B) và nẹp 4 góc bằng họa tiết tam giác tráng bạc sáng bóng kèm đinh tán bạc sắc nét.")
create_p(doc, "• Hoạt cảnh thiên nhiên cành lá và bọ sừng (NatureAmbience): Xung quanh mép bàn bi-a và sảnh chờ được điểm xuyết các cành gỗ cổ thụ khẳng khiu đung đưa nhẹ nhàng cùng hoạt cảnh các loài bọ sừng bò tự nhiên với chuyển động chân và râu mượt mà.")

add_heading2(doc, "6.2. Danh mục 16 màn hình chức năng và Placeholder ảnh minh họa")
create_p(doc, "Dưới đây là danh mục 16 giao diện chức năng của hệ thống kèm theo khung chỉ dẫn chi tiết để người dùng bổ sung ảnh chụp thực tế:")

placeholders_info = [
    ("H-01", "Màn hình Đăng nhập (LoginScene)", "Chạy client, chụp cửa sổ đăng nhập hiển thị các trường nhập Tên tài khoản, Mật khẩu, nút Đăng nhập và nút Mở cấu hình Server.", "h01_login_scene.png", "Mục 6.2.1"),
    ("H-02", "Màn hình Đăng ký tài khoản (RegisterScene)", "Tại màn hình đăng nhập, nhấn 'Đăng ký ngay', chụp giao diện đăng ký với 3 ô nhập Email, Username, Password.", "h02_register_scene.png", "Mục 6.2.2"),
    ("H-03", "Hộp thoại Cấu hình Server & Quét LAN (ServerConfigDialog)", "Nhấn nút bánh răng tại màn hình đăng nhập, chụp hộp thoại cho phép nhập IP/Port thủ công và nút 'Tự động tìm kiếm trong mạng LAN (UDP)'.", "h03_server_config.png", "Mục 6.2.3"),
    ("H-04", "Giao diện Sảnh chờ chính (LobbyScene)", "Đăng nhập thành công vào sảnh chờ; chụp toàn cảnh hiển thị thẻ người chơi, cấp độ, EXP, nút Tìm Trận, Luyện Tập, Bảng Xếp Hạng, Widget nhạc Arcade Speed.", "h04_lobby_scene.png", "Mục 6.2.4"),
    ("H-05", "Danh sách bạn bè & Hộp thoại tìm kiếm bạn (FriendSearchDialog)", "Tại sảnh chờ, nhấn nút Bạn Bè; chụp danh sách bạn bè trực tuyến/ngoại tuyến và hộp thoại tìm kiếm bạn bè theo từ khóa.", "h05_friend_list.png", "Mục 6.2.5"),
    ("H-06", "Khung Chat riêng thời gian thực", "Chọn một người bạn và mở khung trò chuyện; chụp giao diện hiển thị các bong bóng tin nhắn đã gửi/nhận thời gian thực.", "h06_chat_realtime.png", "Mục 6.2.6"),
    ("H-07", "Màn hình Tìm trận ngẫu nhiên & Thách đấu (MatchmakingScene)", "Nhấn 'Tìm Trận Ngay' hoặc gửi lời mời Thách Đấu; chụp màn hình hiển thị radar quét tìm đối thủ và nút Hủy Tìm.", "h07_matchmaking.png", "Mục 6.2.7"),
    ("H-08", "Phòng chờ đếm ngược trước trận (PreGameScene)", "Khi ghép trận thành công; chụp màn hình hiển thị thẻ đối đầu 2 người chơi kèm Avatar, Cấp độ, Skin gậy và đồng hồ đếm ngược 13 giây.", "h08_pregame_scene.png", "Mục 6.2.8"),
    ("H-09", "Bàn thi đấu Bi-a thực tế (GameScene) - Ngắm bắn", "Trong trận đấu; chụp bàn đấu với vải nỉ xanh, khung gỗ nẹp bạc, vạch xuất phát, đường ngắm laser và thanh lực Neon Power Meter.", "h09_gamescene_aim.png", "Mục 6.2.9"),
    ("H-10", "Bảng điều khiển Trô bi / Ép-phê (Spin Selector)", "Chụp cận cảnh bảng điều khiển 'TRÔ BI' hình vuông nẹp bạc bên phải bàn đấu, hiển thị bi cái 3D và điểm đầu cơ chấm đỏ có thể kéo rê.", "h10_spin_selector.png", "Mục 6.2.10"),
    ("H-11", "Cú đánh đang diễn ra & Hiệu ứng va chạm (Strike VFX)", "Sau khi phát lực chọc cơ; chụp khoảnh khắc bi cái va chạm phát ra sóng chấn kích Shockwave, bụi phấn lơ và gậy cơ biến mất hoàn toàn.", "h11_strike_vfx.png", "Mục 6.2.11"),
    ("H-12", "Thông báo bắt lỗi bi cái rơi lỗ (Ball-in-hand Toast)", "Cố tình đánh bi cái rơi vào lỗ; chụp thông báo Toast: '💀 PHẠM LỖI: Bi cái vào lỗ! Đổi lượt (vẫn tính bi đã ăn)'.", "h12_foul_toast.png", "Mục 6.2.12"),
    ("H-13", "Màn hình Kết quả trận đấu Thắng/Thua (ResultScene)", "Khi ván đấu kết thúc; chụp màn hình thông báo CHIẾN THẮNG với pháo hoa Confetti Particle hoặc THẤT BẠI kèm điểm EXP nhận được.", "h13_result_scene.png", "Mục 6.2.13"),
    ("H-14", "Bảng xếp hạng toàn Server (LeaderboardScene)", "Tại sảnh chờ, nhấn 'Bảng Xếp Hạng'; chụp giao diện Top 10 cao thủ với thứ hạng Vàng/Bạc/Đồng, Tên, Cấp độ, EXP và Tỷ lệ thắng.", "h14_leaderboard.png", "Mục 6.2.14"),
    ("H-15", "Hộp thoại Cài đặt & Bộ sưu tập Skin gậy cơ (SettingsDialog)", "Nhấn Cài đặt; chụp giao diện trưng bày gậy cơ 3D trên giá gỗ cổ thụ, bảng thông số cấp độ mở khóa và nút 'Trang Bị Ngay'.", "h15_settings_cue_skin.png", "Mục 6.2.15"),
    ("H-16", "Chế độ Luyện tập tự do (Offline Practice)", "Nhấn 'Luyện Tập' tại sảnh; chụp bàn bi-a chế độ tự do không giới hạn thời gian, hỗ trợ người chơi luyện tập cú đánh và xoáy bi.", "h16_practice_mode.png", "Mục 6.2.16")
]

for code, title, instr, file_sug, loc in placeholders_info:
    add_callout_placeholder(doc, code, title, instr, file_sug, loc)

add_heading2(doc, "6.3. Chi tiết hệ thống Skin gậy cơ 3D và Animation theo 5 cấp độ")
create_p(doc, "Hệ thống Skin gậy cơ được phát triển trực tiếp vào mã nguồn JavaFX Client, lấy cảm hứng từ bộ sưu tập 4 loài bọ sừng quý hiếm kết hợp chất liệu gỗ cổ thụ và kim loại quý. Cây gậy được vẽ dạng hình khối 3D thon dần (Tapered Cylinder) với ánh sáng phản chiếu, hoa văn và huy hiệu chạm khắc:")

headers_skin = ["Mã Skin ID", "Tên Skin gậy", "Phẩm cấp", "Mốc mở khóa", "Đặc điểm tạo hình 3D & Hiệu ứng đặc trưng"]
data_skin = [
    ["cue_default", "Mộc Nguyên Bản", "Cổ Điển", "Cấp 1 (Mặc định)", "Gỗ tần bì vân tự nhiên, phíp ngà voi, đầu cơ xanh lơ Master, hiệu ứng chấn động mộc cổ điển."],
    ["cue_kabuto", "Thiết Giáp Kabuto", "Hiếm (Rare)", "Cấp độ 5+", "Gỗ sồi nâu sẫm, khảm ngọc bích mộc giáp, vệt hào quang xanh lá và sóng xung kích hổ phách."],
    ["cue_atlas", "Tử Tinh Atlas", "Sử Thi (Epic)", "Cấp độ 10+", "Thân cơ đen tuyền khảm thạch anh tím, phíp titan bạc, vệt hào quang tím vũ trụ và tia sét tử tinh."],
    ["cue_lucanus", "Huyết Chiến Lucanus", "Huyền Thoại (Legendary)", "Cấp độ 15+", "Gỗ gụ đỏ thẫm bọc da rắn, viền vàng đồng, sóng xung kích đỏ rực và hào quang ma pháp rực lửa."],
    ["cue_hercules", "Hoàng Kim Hercules", "Thần Thoại (Mythic)", "Cấp độ 20+", "Vàng ròng 24K khảm ngọc cẩm thạch trắng, cổ ngữ hoàng gia, hào quang chói lòa và mưa sao băng vàng."]
]
col_widths_skin = [2.2, 3.2, 2.2, 2.4, 5.5]
add_table_with_caption(doc, "Bảng 6.1: Cấu hình 5 phẩm cấp Skin gậy cơ và mốc cấp độ mở khóa", col_widths_skin, headers_skin, data_skin,
                       [WD_ALIGN_PARAGRAPH.CENTER, WD_ALIGN_PARAGRAPH.LEFT, WD_ALIGN_PARAGRAPH.CENTER, WD_ALIGN_PARAGRAPH.CENTER, WD_ALIGN_PARAGRAPH.LEFT])

create_p(doc, "• Cơ chế hiển thị sau khi bắn: Theo đúng yêu cầu cập nhật mới nhất, cây gậy cơ biến mất hoàn toàn ngay lập tức khi người chơi phát lực bắn, tuyệt đối không xuất hiện hay di chuyển theo sau bi cái. Tại điểm chạm ban đầu chỉ phát ra vụ nổ tia sáng chấn kích Strike VFX đặc trưng của Skin rồi tan biến tự nhiên.")
create_p(doc, "• Bản nhạc nền cố định: Bản nhạc nền được khóa cứng vào bài '⚡ Arcade Speed (Điện Tử)' (assets/sounds/bgm_arcade.mp3), nhịp điệu nhanh và sôi động, loại bỏ hoàn toàn các nút chuyển bài trên thanh công cụ để đảm bảo trải nghiệm thống nhất.")

# ==============================================================================
# CHƯƠNG 7. KIỂM THỬ HỆ THỐNG VÀ ĐÁNH GIÁ KẾT QUẢ
# ==============================================================================
add_heading1(doc, "CHƯƠNG 7. KIỂM THỬ HỆ THỐNG VÀ ĐÁNH GIÁ KẾT QUẢ")

add_heading2(doc, "7.1. Môi trường và điều kiện kiểm thử thực tế")
create_p(doc, "Quá trình kiểm thử được tiến hành trên môi trường thực nghiệm với các thông số cấu hình cụ thể:")
create_p(doc, "• Phần cứng thử nghiệm: Máy tính x64, CPU Intel Core i7 / AMD Ryzen, RAM 16GB, Card đồ họa tích hợp / rời.")
create_p(doc, "• Hệ điều hành: Microsoft Windows 11 64-bit.")
create_p(doc, "• Môi trường Java: OpenJDK 17.0.10 LTS 64-bit.")
create_p(doc, "• Môi trường JavaFX: OpenJFX SDK 17.0.13.")
create_p(doc, "• Hệ quản trị CSDL: PostgreSQL 16.2 chạy trên cổng mặc định 5432.")
create_p(doc, "• Môi trường mạng: Mạng cục bộ Loopback (127.0.0.1) và Mạng Wi-Fi nội bộ gia đình (lớp mạng 192.168.1.x).")

add_heading2(doc, "7.2. Ma trận kịch bản kiểm thử (Test Matrix) chi tiết")
create_p(doc, "Bảng 7.1 dưới đây tổng hợp chi tiết 15 kịch bản kiểm thử thực tế, phân biệt rõ ràng giữa các trường hợp đã chạy kiểm chứng thành công ('Đạt') và các trường hợp đo tải quy mô lớn ('Chưa kiểm thử'):")

headers_test = ["Mã TC", "Chức năng", "Điều kiện kiểm thử", "Các bước thực hiện", "Kết quả mong đợi", "Kết quả thực tế", "Trạng thái"]
data_test = [
    ["TC-01", "Đăng ký tài khoản", "Server đang chạy, email/user chưa tồn tại", "1. Nhập email, username, pass hợp lệ\n2. Nhấn 'Đăng ký'", "Server lưu DB, phản hồi thành công và quay lại login.", "Tài khoản được lưu vào bảng players, hiển thị thông báo OK.", "Đạt"],
    ["TC-02", "Đăng ký trùng Email", "Email đã có trong database", "1. Nhập email đã tồn tại\n2. Nhấn 'Đăng ký'", "Server từ chối, báo lỗi EMAIL_EXISTS.", "Hiển thị thông báo 'Email đã được sử dụng!'.", "Đạt"],
    ["TC-03", "Đăng nhập đúng", "Tài khoản đã tạo thành công", "1. Nhập đúng user và pass\n2. Nhấn 'Đăng nhập'", "Server xác thực mật khẩu, trả về thông tin và vào Sảnh.", "Đăng nhập thành công, sảnh hiển thị đúng Level và EXP.", "Đạt"],
    ["TC-04", "Đăng nhập sai mật khẩu", "Tài khoản tồn tại trong DB", "1. Nhập đúng user, sai pass\n2. Nhấn 'Đăng nhập'", "Server từ chối, báo sai thông tin.", "Hiển thị thông báo 'Sai tài khoản hoặc mật khẩu!'.", "Đạt"],
    ["TC-05", "Dò tìm Server LAN (UDP)", "Server bật, chung mạng Wi-Fi", "1. Mở Cấu hình Server\n2. Nhấn 'Tự động tìm kiếm'", "Client nhận IP của Server qua UDP 12346.", "Tự động điền đúng IP máy chủ LAN vào ô cấu hình.", "Đạt"],
    ["TC-06", "Ghép trận 2 người chơi", "2 Client cùng nhấn 'Tìm Trận'", "1. Client A nhấn Tìm Trận\n2. Client B nhấn Tìm Trận", "LobbyManager ghép đôi, 2 Client vào PreGameScene.", "Cả hai Client cùng chuyển vào phòng chờ 13s đồng bộ.", "Đạt"],
    ["TC-07", "Đồng bộ cú đánh qua mạng", "Đang trong ván đấu 1v1", "1. Client A kéo lực và nhả chuột\n2. Quan sát cả 2 màn hình", "Client B nhận GAME_STATE, bi lăn hoàn toàn trùng khớp.", "Hai màn hình mô phỏng chuyển động bi đồng nhất 100%.", "Đạt"],
    ["TC-08", "Ẩn gậy cơ sau khi bắn", "Client thực hiện cú đánh", "1. Nhả chuột phát lực\n2. Quan sát gậy cơ", "Gậy biến mất tức thì, không đi theo bi cái.", "Gậy cơ biến mất ngay lập tức, chỉ có Strike VFX tại điểm bắn.", "Đạt"],
    ["TC-09", "Luật bi cái rơi lỗ khi ăn bi", "Đánh bi mục tiêu vào lỗ, bi cái cũng rơi lỗ", "1. Chọc bi mục tiêu vào lỗ\n2. Bi cái chạy vào lỗ khác", "Bi mục tiêu vẫn nằm trong lỗ; chuyển lượt; đối thủ có ball-in-hand.", "Đúng luật: Bi mục tiêu được tính điểm; đối thủ nhận bi trong tay.", "Đạt"],
    ["TC-10", "Luật chết cả bi cái và bi 8", "Bi cái và bi 8 cùng rơi lỗ cùng lượt", "1. Đánh rơi cả bi trắng và bi đen số 8 trong 1 turn", "Người bắn bị xử thua ngay lập tức; đối thủ thắng.", "Hiển thị Toast thua cuộc; GameRoom xử đối thủ thắng trận.", "Đạt"],
    ["TC-11", "Trang bị skin đủ cấp độ", "Tài khoản đạt Cấp 5+", "1. Vào Cài đặt -> Chọn gậy Kabuto\n2. Nhấn 'Trang bị'", "Server kiểm tra hợp lệ, lưu vào PostgreSQL.", "Trang bị thành công, gậy Kabuto xuất hiện trong ván đấu.", "Đạt"],
    ["TC-12", "Chặn trang bị skin chưa đủ cấp", "Tài khoản chỉ đạt Cấp 2", "1. Chọn gậy Hercules (yêu cầu Lv.20)\n2. Nhấn 'Trang bị'", "Server từ chối lưu vì chưa đủ cấp.", "Giao diện báo khóa skin, không lưu thay đổi vào database.", "Đạt"],
    ["TC-13", "Nhắn tin Chat realtime", "Hai người chơi là bạn bè online", "1. Client A gửi tin nhắn\n2. Quan sát Client B", "Client B nhận tin nhắn tức thì qua CHAT_MESSAGE_RESP.", "Bong bóng tin nhắn xuất hiện ngay lập tức bên Client B.", "Đạt"],
    ["TC-14", "Ngắt kết nối giữa trận", "Đang trong trận đấu 1v1", "1. Tắt đột ngột Client A\n2. Quan sát Client B", "Server phát hiện socket đóng, xử Client B thắng.", "Client B nhận GAME_OVER chiến thắng và được cộng EXP.", "Đạt"],
    ["TC-15", "Kiểm thử chịu tải 100 Clients", "Chạy 100 luồng kết nối đồng thời", "1. Bật script gửi 100 connect\n2. Đo độ trễ phản hồi", "Server không crash, duy trì phục vụ ổn định.", "Môi trường thực nghiệm cá nhân chưa có máy đo tải tập trung.", "Chưa kiểm thử"]
]
col_widths_test = [1.2, 2.5, 2.2, 2.6, 2.8, 3.0, 1.2]
add_table_with_caption(doc, "Bảng 7.1: Ma trận kịch bản kiểm thử hệ thống thực tế (Test Matrix)", col_widths_test, headers_test, data_test,
                       [WD_ALIGN_PARAGRAPH.CENTER, WD_ALIGN_PARAGRAPH.LEFT, WD_ALIGN_PARAGRAPH.LEFT, WD_ALIGN_PARAGRAPH.LEFT, WD_ALIGN_PARAGRAPH.LEFT, WD_ALIGN_PARAGRAPH.LEFT, WD_ALIGN_PARAGRAPH.CENTER])

add_heading2(doc, "7.3. Đánh giá ưu điểm và hạn chế của hệ thống")
create_p(doc, "1. Ưu điểm nổi bật:")
create_p(doc, "• Tối ưu băng thông mạng vượt trội: Giao thức nhị phân Big-Endian giúp mỗi cú đánh chỉ tiêu tốn đúng 49 byte dữ liệu, tiết kiệm hơn 85% so với định dạng JSON tương đương.")
create_p(doc, "• Trải nghiệm đồ họa mượt mà: Việc chạy vật lý tất định tại máy trạm giúp bi lăn ở tốc độ 60 FPS mượt mà tuyệt đối mà không phụ thuộc vào tình trạng giật lag của đường truyền Internet.")
create_p(doc, "• Bảo mật cấp quyền nghiêm ngặt: Mật khẩu được băm an toàn; Server kiểm soát toàn bộ logic thăng cấp và quyền trang bị skin.")
create_p(doc, "2. Hạn chế thực tế của kiến trúc:")
create_p(doc, "• Khả năng sai lệch số thực dấu chấm động (Floating-Point Drift): Do các bộ vi xử lý (CPU) khác nhau có thể làm tròn phép tính dấu chấm động kép (double precision) có độ lệch siêu nhỏ ở phần triệu, sau hàng chục lần va chạm liên hoàn có thể xuất hiện độ lệch vi mô giữa 2 máy.")
create_p(doc, "• Chưa áp dụng mô hình Server-Authoritative Physics toàn phần: Hiện tại máy chủ đóng vai trò trọng tài chuyển tiếp và phân xử kết quả chứ chưa trực tiếp chạy toàn bộ thuật toán vật lý 2D trên máy chủ không đầu (headless physics).")

add_heading2(doc, "7.4. Đề xuất phương pháp đo đạc hiệu năng mạng")
create_p(doc, "Để đánh giá sâu hơn về mặt kỹ thuật mạng trong các giai đoạn tiếp theo, nhóm đề xuất phương pháp đo đạc thực nghiệm chuẩn:")
create_p(doc, "• Đo độ trễ khứ hồi RTT (Round-Trip Time): Sử dụng gói tin PING [Code 18] ghi lại dấu thời gian System.currentTimeMillis() khi gửi và khi nhận phản hồi để tính toán trung bình RTT (kết quả dự kiến < 5ms trong mạng LAN và < 40ms qua Internet).")
create_p(doc, "• Đo tốc độ khung hình (FPS Counter): Sử dụng AnimationTimer tích hợp bộ đếm frame để ghi lại chỉ số FPS trung bình trong suốt thời gian ván đấu diễn ra.")

# ==============================================================================
# CHƯƠNG 8. KẾT LUẬN VÀ HƯỚNG PHÁT TRIỂN
# ==============================================================================
add_heading1(doc, "CHƯƠNG 8. KẾT LUẬN VÀ HƯỚNG PHÁT TRIỂN")

add_heading2(doc, "8.1. Các kết quả cụ thể đã hoàn thành")
create_p(doc, "Sau quá trình nghiên cứu, thiết kế và phát triển nghiêm túc, đồ án môn học Lập trình mạng đã hoàn thành toàn diện các mục tiêu đề ra:")
create_p(doc, "1. Xây dựng hoàn chỉnh ứng dụng trò chơi Bi-a 8-Ball Pool Multiplayer chạy trên nền tảng Java 17 LTS và JavaFX 17.0.13.")
create_p(doc, "2. Thiết kế và hiện thực hóa thành công giao thức truyền thông nhị phân tùy chỉnh (Custom Binary Protocol) gồm 45 mã gói tin qua Socket TCP cổng 12345.")
create_p(doc, "3. Hiện thực hóa tính năng tự động dò tìm máy chủ mạng nội bộ qua UDP Broadcast cổng 12346.")
create_p(doc, "4. Vận hành máy chủ đa luồng có khả năng phục vụ nhiều client đồng thời, ghép trận tự động và điều phối phòng chơi GameRoom.")
create_p(doc, "5. Xây dựng động cơ vật lý 2D tất định hỗ trợ chọc cơ, nảy băng, trô bi, cule và ép-phê với độ chính xác cao.")
create_p(doc, "6. Hoàn thiện hệ thống CSDL quan hệ PostgreSQL 16 quản lý tài khoản, lịch sử đấu, bạn bè, tin nhắn trò chuyện.")
create_p(doc, "7. Cập nhật hệ thống Skin gậy cơ 5 phẩm cấp theo Level với hoạt cảnh 3D và nhạc nền Arcade Speed cố định.")

add_heading2(doc, "8.2. Giá trị học tập môn Lập trình mạng")
create_p(doc, "Thông qua đồ án này, các thành viên trong nhóm đã thu nhận được những trải nghiệm và bài học thực tiễn vô giá về lập trình mạng:")
create_p(doc, "• Hiểu sâu sắc sự khác biệt bản chất giữa giao thức hướng kết nối tin cậy (TCP) và giao thức phi kết nối quảng bá (UDP), từ đó biết cách phối hợp nhịp nhàng cả hai giao thức trong cùng một hệ sinh thái ứng dụng.")
create_p(doc, "• Làm chủ kỹ thuật đóng gói mảng byte, phân tách ranh giới thông điệp nhị phân, giải quyết triệt để bài toán dính gói/phân mảnh luồng TCP.")
create_p(doc, "• Nắm vững mô hình điều phối luồng giao diện người dùng (JavaFX Application Thread vs Network Background Thread), hiểu rõ nguyên lý hoạt động của Platform.runLater().")
create_p(doc, "• Hiểu rõ các bài toán đồng bộ thời gian thực trong game mạng và kỹ thuật đồng bộ hạt giống ngẫu nhiên (Seed Synchronization).")

add_heading2(doc, "8.3. Hạn chế thực tế của đồ án")
create_p(doc, "Dù đã đạt được nhiều kết quả tích cực, hệ thống vẫn tồn tại một số hạn chế khách quan:")
create_p(doc, "• Chưa hỗ trợ cơ chế tự động kết nối lại (Auto-Reconnect) và khôi phục trạng thái ván đấu khi đường truyền mạng bị ngắt quãng giữa chừng.")
create_p(doc, "• Chưa xây dựng giao diện Quản trị viên (Web Admin Dashboard) để theo dõi trực quan lượng kết nối và băng thông máy chủ theo thời gian thực.")
create_p(doc, "• Hệ thống vật lý vẫn dựa vào mô phỏng cục bộ tại Client, chưa tích hợp máy chủ Headless Physics để chống gian lận tuyệt đối.")

add_heading2(doc, "8.4. Hướng phát triển và mở rộng tương lai")
create_p(doc, "Trong các giai đoạn tiếp theo, hệ thống có thể được nâng cấp và phát triển theo các định hướng:")
create_p(doc, "1. Nâng cấp kiến trúc Server-Authoritative Physics: Chạy mô phỏng vật lý trực tiếp trên Server và gửi trạng thái delta-compression về cho Client.")
create_p(doc, "2. Mở rộng chế độ chơi mới: Phát triển thêm chế độ Bi-a 9 bóng (9-Ball Pool), Bi-a Snooker và Bi-a 3 băng (3-Cushion Carom).")
create_p(doc, "3. Hỗ trợ đa nền tảng (Cross-platform): Đóng gói ứng dụng chạy trên nền tảng Web thông qua WebAssembly hoặc phát triển Client di động Android/iOS kết nối chung vào Server Java hiện tại.")
create_p(doc, "4. Tích hợp tính năng Chat Voice thời gian thực sử dụng WebRTC hoặc UDP Audio Streaming.")

# ==============================================================================
# TÀI LIỆU THAM KHẢO VÀ PHỤ LỤC
# ==============================================================================
add_heading1(doc, "TÀI LIỆU THAM KHẢO VÀ PHỤ LỤC")

add_heading2(doc, "TÀI LIỆU THAM KHẢO CHÍNH THỨC")
refs = [
    ("1", "Oracle Corporation, \"Java™ Platform, Standard Edition 17 API Specification\", Oracle Official Documentation, 2021. URL: https://docs.oracle.com/en/java/javase/17/docs/api/"),
    ("2", "OpenJFX Project, \"JavaFX 17 Documentation & Architecture Guide\", Gluon & OpenJFX Community, 2021. URL: https://openjfx.io/"),
    ("3", "PostgreSQL Global Development Group, \"PostgreSQL 16.2 Documentation: JDBC Driver & SQL Reference\", 2024. URL: https://www.postgresql.org/docs/"),
    ("4", "PostgreSQL JDBC Project, \"PostgreSQL JDBC Driver Documentation (pgJDBC 42.7.3)\", 2024. URL: https://jdbc.postgresql.org/"),
    ("5", "Postel, J., \"Transmission Control Protocol - Protocol Specification (RFC 793)\", Internet Engineering Task Force (IETF), 1981."),
    ("6", "Postel, J., \"User Datagram Protocol (RFC 768)\", Internet Engineering Task Force (IETF), 1980."),
    ("7", "E. Gamma, R. Helm, R. Johnson, J. Vlissides, \"Design Patterns: Elements of Reusable Object-Oriented Software\", Addison-Wesley, 1994.")
]
for r_idx, r_text in refs:
    p_r = doc.add_paragraph()
    p_r.paragraph_format.space_before = Pt(2)
    p_r.paragraph_format.space_after = Pt(4)
    p_r.paragraph_format.line_spacing = 1.2
    r_run = p_r.add_run(f"[{r_idx}] {r_text}")
    r_run.font.name = "Times New Roman"
    r_run.font.size = Pt(11)
    r_run.font.color.rgb = C_TEXT_DARK

add_heading2(doc, "PHỤ LỤC A: HƯỚNG DẪN BIÊN DỊCH VÀ KHỞI CHẠY HỆ THỐNG")
create_p(doc, "Dự án được trang bị sẵn các kịch bản thực thi tự động (Batch script) trên Windows giúp việc biên dịch và chạy ứng dụng diễn ra nhanh chóng:")
create_p(doc, "1. Yêu cầu môi trường cài đặt sẵn:")
create_p(doc, "• JDK 17 (đường dẫn C:\\Program Files\\Java\\jdk-17 hoặc cấu hình biến môi trường JAVA_HOME).")
create_p(doc, "• PostgreSQL 16 đang chạy với cơ sở dữ liệu billiards_db đã import schema.sql.")
create_p(doc, "2. Khởi chạy Máy chủ (Server):")
add_code_block(doc, """cd billiards-game/server
compile.bat          (Biên dịch toàn bộ mã nguồn server và đóng gói BilliardsServer.jar)
run_server.bat       (Khởi chạy máy chủ TCP cổng 12345 và UDP cổng 12346)""")

create_p(doc, "3. Khởi chạy Máy trạm (Client):")
add_code_block(doc, """cd billiards-game/client
compile.bat          (Biên dịch mã nguồn JavaFX và đóng gói BilliardsClient.jar)
run_client.bat       (Khởi chạy ứng dụng máy trạm)""")

add_heading2(doc, "PHỤ LỤC B: LƯỢC ĐỒ CƠ SỞ DỮ LIỆU POSTGRESQL (SCHEMA.SQL)")
create_p(doc, "Mã nguồn câu lệnh SQL tạo bảng chính thức lưu tại database/schema.sql:")
add_code_block(doc, """CREATE DATABASE billiards_db;
\\c billiards_db;

CREATE TABLE IF NOT EXISTS players (
    id SERIAL PRIMARY KEY,
    email VARCHAR(100) UNIQUE NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    exp INT DEFAULT 0,
    level INT DEFAULT 1,
    wins INT DEFAULT 0,
    losses INT DEFAULT 0,
    avatar_id INT DEFAULT 0,
    cue_color VARCHAR(20) DEFAULT '#DEB887',
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS match_history (
    id SERIAL PRIMARY KEY,
    winner_id INT REFERENCES players(id),
    loser_id INT REFERENCES players(id),
    exp_gained INT DEFAULT 100,
    played_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS friendships (
    user_id INT REFERENCES players(id),
    friend_id INT REFERENCES players(id),
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (user_id, friend_id)
);

CREATE TABLE IF NOT EXISTS messages (
    id SERIAL PRIMARY KEY,
    sender_id INT REFERENCES players(id),
    receiver_id INT REFERENCES players(id),
    content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    sent_at TIMESTAMP DEFAULT NOW()
);""")

add_heading2(doc, "PHỤ LỤC C: HƯỚNG DẪN BỔ SUNG ẢNH CHỤP MÀN HÌNH THỰC TẾ")
create_p(doc, "Để hoàn thiện báo cáo in ấn hoặc nộp cho giảng viên, người dùng chỉ cần thực hiện các bước chụp màn hình sau và thay thế vào các khung placeholder [H-01] đến [H-16]:")
create_p(doc, "1. Chạy run_server.bat và mở 2 cửa sổ run_client.bat song song trên màn hình máy tính.")
create_p(doc, "2. Sử dụng công cụ Snipping Tool hoặc phím tắt Windows + Shift + S để chụp chính xác các cửa sổ tương ứng theo từng bước trong Bảng Placeholder ở Mục 6.2.")
create_p(doc, "3. Lưu ảnh vào thư mục report_assets/screenshots/ với tên gợi ý (ví dụ: h01_login_scene.png, h09_gamescene_aim.png).")
create_p(doc, "4. Trong file Word, nhấp chuột vào khung placeholder tương ứng, chọn Chèn ảnh (Insert Picture) để thay thế. File Word đã được định dạng chuẩn lề và bố cục trang sẽ tự động căn chỉnh hoàn hảo.")

# Lưu file Word
print("Saving Word document to:", OUTPUT_FILE)
doc.save(OUTPUT_FILE)
print("SUCCESS: File Word generated successfully!")
print("Location:", OUTPUT_FILE)
