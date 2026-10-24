import os
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.patches as patches

OUT_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'report_assets', 'diagrams')
os.makedirs(OUT_DIR, exist_ok=True)

def setup_canvas(width=10, height=6, dpi=300):
    fig, ax = plt.subplots(figsize=(width, height), dpi=dpi)
    ax.set_xlim(0, 100)
    ax.set_ylim(0, 100)
    ax.axis('off')
    return fig, ax

def draw_box(ax, x, y, w, h, title, subtitle="", bg_color="#EBF5FB", border_color="#2980B9", title_color="#1B4F72", corner_radius=1.5):
    rect = patches.FancyBboxPatch((x, y), w, h, boxstyle=f"round,pad=0.5,rounding_size={corner_radius}",
                                  ec=border_color, fc=bg_color, lw=1.5)
    ax.add_patch(rect)
    if subtitle:
        ax.text(x + w/2, y + h*0.62, title, ha='center', va='center', fontsize=9.5, fontweight='bold', color=title_color)
        ax.text(x + w/2, y + h*0.32, subtitle, ha='center', va='center', fontsize=8, color="#2C3E50")
    else:
        ax.text(x + w/2, y + h/2, title, ha='center', va='center', fontsize=9.5, fontweight='bold', color=title_color)

def draw_arrow(ax, x1, y1, x2, y2, label="", color="#2C3E50", lw=1.2, rad=0.0):
    ax.annotate("", xy=(x2, y2), xytext=(x1, y1),
                arrowprops=dict(arrowstyle="->", color=color, lw=lw,
                                connectionstyle=f"arc3,rad={rad}"))
    if label:
        mx = (x1 + x2)/2
        my = (y1 + y2)/2
        ax.text(mx, my + 1.8, label, ha='center', va='center', fontsize=7.5, color=color,
                bbox=dict(boxstyle="round,pad=0.2", fc="white", ec="none", alpha=0.85))

def draw_dashed_boundary(ax, x, y, w, h, label="", color="#7F8C8D"):
    rect = patches.FancyBboxPatch((x, y), w, h, boxstyle="round,pad=0.8,rounding_size=2",
                                  ec=color, fc="none", lw=1.2, ls="--")
    ax.add_patch(rect)
    if label:
        ax.text(x + 2, y + h - 3, label, fontsize=8.5, fontweight='bold', color=color)

# ==============================================================================
# 1. SƠ ĐỒ KIẾN TRÚC TỔNG THỂ (Architecture Diagram)
# ==============================================================================
def gen_architecture_diagram():
    fig, ax = setup_canvas(11, 7.5)
    ax.text(50, 97, "HỆ THỐNG TRÒ CHƠI 8-BALL POOL MULTIPLAYER - KIẾN TRÚC TỔNG THỂ",
            ha='center', va='center', fontsize=12, fontweight='bold', color="#1A5276")

    # Client A
    draw_dashed_boundary(ax, 3, 48, 28, 44, "CLIENT 1 (JavaFX 17 / JDK 17)")
    draw_box(ax, 5, 78, 24, 9, "Giao diện người dùng (UI)", "JavaFX Scenes / Custom Canvas 2D", "#D4EFDF", "#27AE60")
    draw_box(ax, 5, 65, 24, 9, "Động cơ Vật lý (PhysicsEngine)", "2D Deterministic Physics (Cục bộ)", "#FCF3CF", "#F39C12")
    draw_box(ax, 5, 51, 24, 10, "NetworkClient", "Socket TCP (12345) / Listener Thread", "#D6EAF8", "#2980B9")
    draw_arrow(ax, 17, 78, 17, 74, "Sự kiện")
    draw_arrow(ax, 17, 65, 17, 61, "Thông số cơ")

    # Client B
    draw_dashed_boundary(ax, 3, 2, 28, 44, "CLIENT 2 (JavaFX 17 / JDK 17)")
    draw_box(ax, 5, 32, 24, 9, "Giao diện người dùng (UI)", "JavaFX Scenes / Custom Canvas 2D", "#D4EFDF", "#27AE60")
    draw_box(ax, 5, 19, 24, 9, "Động cơ Vật lý (PhysicsEngine)", "2D Deterministic Physics (Cục bộ)", "#FCF3CF", "#F39C12")
    draw_box(ax, 5, 5, 24, 10, "NetworkClient", "Socket TCP (12345) / Listener Thread", "#D6EAF8", "#2980B9")
    draw_arrow(ax, 17, 32, 17, 28, "Sự kiện")
    draw_arrow(ax, 17, 19, 17, 15, "Thông số cơ")

    # Kênh truyền Mạng (Network Channels)
    draw_dashed_boundary(ax, 35, 18, 28, 62, "KÊNH TRUYỀN MẠNG (TCP / UDP)")
    draw_box(ax, 37, 56, 24, 18, "TCP Connection (Port 12345)", "Giao thức nhị phân Big-Endian\nPacket Type (1 byte) + DataPayload\nĐộ tin cậy cao, đảm bảo thứ tự", "#E8DAEF", "#8E44AD")
    draw_box(ax, 37, 24, 24, 18, "UDP Broadcast (Port 12346)", "LAN Auto-Discovery\nClient: BILLIARDS_DISCOVER\nServer: BILLIARDS_SERVER:12345", "#FDEDEC", "#E74C3C")

    # Mũi tên Client <-> Network
    draw_arrow(ax, 29, 56, 37, 64, "TCP Socket", "#2980B9")
    draw_arrow(ax, 37, 62, 29, 54, "Packet In", "#2980B9")
    draw_arrow(ax, 29, 10, 37, 60, "TCP Socket", "#2980B9")
    draw_arrow(ax, 37, 58, 29, 8, "Packet In", "#2980B9")
    draw_arrow(ax, 29, 52, 37, 34, "UDP Probe", "#E74C3C")
    draw_arrow(ax, 29, 6, 37, 32, "UDP Probe", "#E74C3C")

    # Server Side
    draw_dashed_boundary(ax, 67, 18, 30, 74, "SERVER HỆ THỐNG (Java 17 Server)")
    draw_box(ax, 69, 78, 26, 10, "ServerMain & Listener", "ServerSocket(12345) / DatagramSocket(12346)", "#E5E8E8", "#7F8C8D")
    draw_box(ax, 69, 64, 26, 10, "ClientHandler (Thread/Client)", "Xử lý 45 PacketTypes & I/O Stream", "#D6EAF8", "#2980B9")
    draw_box(ax, 69, 50, 26, 10, "LobbyManager & GameRoom", "Ghép trận, Trọng tài lượt đánh, Sync Seed", "#FCF3CF", "#F39C12")
    draw_box(ax, 69, 36, 26, 10, "AuthManager & Security", "SHA-256 + Salt / Cấp quyền Skin", "#D4EFDF", "#27AE60")
    draw_box(ax, 69, 22, 26, 10, "DatabaseManager (DAO)", "Quản lý kết nối JDBC Connection", "#FADBD8", "#C0392B")

    # Mũi tên Network -> Server
    draw_arrow(ax, 61, 65, 69, 69, "Gói tin TCP", "#8E44AD")
    draw_arrow(ax, 61, 33, 69, 79, "Gói tin UDP", "#E74C3C")
    draw_arrow(ax, 82, 78, 82, 74, "accept()")
    draw_arrow(ax, 82, 64, 82, 60, "Điều phối")
    draw_arrow(ax, 82, 50, 82, 46, "Xác thực")
    draw_arrow(ax, 82, 36, 82, 32, "CRUD")

    # Database
    draw_dashed_boundary(ax, 67, 1, 30, 14, "CƠ SỞ DỮ LIỆU (Lưu trữ bền vững)")
    draw_box(ax, 69, 3, 26, 9, "PostgreSQL Database", "billiards_db (Port 5432) / JDBC Driver 42.7.3", "#E8F8F5", "#16A085")
    draw_arrow(ax, 82, 22, 82, 12, "SQL Queries (JDBC)", "#16A085")

    plt.tight_layout()
    fig.savefig(os.path.join(OUT_DIR, "architecture_diagram.png"), dpi=300)
    plt.close(fig)

# ==============================================================================
# 2. SƠ ĐỒ GÓI / PACKAGE DIAGRAM
# ==============================================================================
def gen_package_diagram():
    fig, ax = setup_canvas(10, 6.5)
    ax.text(50, 96, "TỔ CHỨC CẤU TRÚC GÓI MÃ NGUỒN (PACKAGE STRUCTURE)",
            ha='center', va='center', fontsize=11.5, fontweight='bold', color="#1A5276")

    # Client Packages
    draw_dashed_boundary(ax, 3, 6, 45, 86, "com.billiards.client (Client Module)")
    draw_box(ax, 5, 78, 41, 9, "com.billiards.client", "ClientMain, NetworkClient, AvatarManager", "#EBF5FB", "#2980B9")
    draw_box(ax, 5, 64, 41, 10, "com.billiards.client.scenes", "LoginScene, LobbyScene, PreGameScene, GameScene,\nResultScene, LeaderboardScene, SettingsDialog...", "#D4EFDF", "#27AE60")
    draw_box(ax, 5, 50, 41, 10, "com.billiards.client.game", "Ball, PhysicsEngine, CueSkin, CueSkinManager", "#FCF3CF", "#F39C12")
    draw_box(ax, 5, 36, 41, 10, "com.billiards.client.protocol", "Packet (Data I/O), PacketType (45 Types)", "#E8DAEF", "#8E44AD")
    draw_box(ax, 5, 22, 41, 10, "com.billiards.client.theme", "Theme (Bảng màu Modern), SilverCornerPane", "#E5E8E8", "#7F8C8D")
    draw_box(ax, 5, 8, 41, 10, "com.billiards.client.vfx & audio", "NatureAmbience, DragonCloudBackground, SoundManager", "#FDEDEC", "#E74C3C")

    # Server Packages
    draw_dashed_boundary(ax, 52, 26, 45, 66, "com.billiards.server (Server Module)")
    draw_box(ax, 54, 76, 41, 11, "com.billiards.server", "ServerMain (Socket Server & UDP Discovery),\nClientHandler (Thread Worker), GameRoom (Trọng tài)", "#EBF5FB", "#2980B9")
    draw_box(ax, 54, 59, 41, 12, "com.billiards.server.managers", "AuthManager (Mã hóa mật khẩu SHA-256 + Salt),\nLobbyManager (Hàng đợi ghép trận),\nDatabaseManager (JDBC DAO)", "#D4EFDF", "#27AE60")
    draw_box(ax, 54, 42, 41, 12, "com.billiards.server.protocol", "Packet (Đóng gói Binary),\nPacketType (Enum mã hóa byte),\nCueSkinConfig (Quy chuẩn Level mở khóa)", "#E8DAEF", "#8E44AD")

    # Thư viện ngoài
    draw_dashed_boundary(ax, 52, 6, 45, 17, "Thư viện phụ thuộc (Libraries / Drivers)")
    draw_box(ax, 54, 8, 41, 11, "External Dependencies", "OpenJFX 17 (controls, fxml, media)\nPostgreSQL JDBC Driver 42.7.3", "#FCF3CF", "#D68910")

    plt.tight_layout()
    fig.savefig(os.path.join(OUT_DIR, "package_diagram.png"), dpi=300)
    plt.close(fig)

# ==============================================================================
# 3. SƠ ĐỒ CHUYỂN TRẠNG THÁI VÁN ĐẤU (Match State Diagram)
# ==============================================================================
def gen_match_state_diagram():
    fig, ax = setup_canvas(10, 6.5)
    ax.text(50, 96, "SƠ ĐỒ CHUYỂN TRẠNG THÁI VÁN ĐẤU (MATCH STATE MACHINE)",
            ha='center', va='center', fontsize=11.5, fontweight='bold', color="#1A5276")

    draw_box(ax, 5, 75, 20, 10, "IDLE / LOBBY", "Người chơi tại sảnh", "#EBF5FB", "#2980B9")
    draw_box(ax, 40, 75, 20, 10, "MATCHMAKING", "Hàng đợi tìm trận", "#FCF3CF", "#F39C12")
    draw_box(ax, 75, 75, 20, 10, "PRE_GAME", "Đếm ngược 13s sẵn sàng", "#D4EFDF", "#27AE60")
    
    draw_box(ax, 75, 45, 20, 12, "PLAYER_AIMING", "Đến lượt: kéo cơ, chọn ép-phê\nTimer đếm ngược 60s", "#E8DAEF", "#8E44AD")
    draw_box(ax, 75, 15, 20, 12, "OPPONENT_WAITING", "Chờ đối thủ chọc cơ\nKhóa tương tác chuột", "#E5E8E8", "#7F8C8D")
    draw_box(ax, 40, 30, 20, 12, "PHYSICS_RUNNING", "Mô phỏng 2D tất định tại 2 Client\nBi lăn, va chạm, rơi lỗ", "#FDEDEC", "#E74C3C")
    draw_box(ax, 5, 30, 20, 12, "GAME_OVER", "Hiển thị ResultScene\nCộng EXP, cập nhật BXH", "#D5F5E3", "#229954")

    # Mũi tên chuyển trạng thái
    draw_arrow(ax, 25, 80, 40, 80, "FIND_MATCH / Thách đấu")
    draw_arrow(ax, 60, 80, 75, 80, "MATCH_FOUND")
    draw_arrow(ax, 85, 75, 85, 57, "GAME_START (seed)")
    draw_arrow(ax, 85, 45, 50, 42, "CUE_SHOT (Gửi tham số)", rad=-0.2)
    draw_arrow(ax, 50, 30, 85, 45, "PHYSICS_DONE (Ăn bi hợp lệ -> Đánh tiếp)", rad=0.2)
    draw_arrow(ax, 50, 30, 85, 27, "PHYSICS_DONE (Phạm lỗi/Hết lượt -> Đổi lượt)", rad=-0.2)
    draw_arrow(ax, 85, 27, 50, 30, "Đối thủ bắn -> GAME_STATE -> Lăn bi", rad=0.2)
    draw_arrow(ax, 40, 36, 25, 36, "CLAIM_WIN / Bi 8 vào lỗ")
    draw_arrow(ax, 15, 42, 15, 75, "Trở về Sảnh / Chơi tiếp")

    plt.tight_layout()
    fig.savefig(os.path.join(OUT_DIR, "match_state_diagram.png"), dpi=300)
    plt.close(fig)

# ==============================================================================
# 4. SƠ ĐỒ ERD CƠ SỞ DỮ LIỆU (Database ERD Diagram)
# ==============================================================================
def gen_database_erd():
    fig, ax = setup_canvas(10.5, 7.5)
    ax.text(50, 97, "SƠ ĐỒ THỰC THỂ QUAN HỆ CƠ SỞ DỮ LIỆU (POSTGRESQL ERD)",
            ha='center', va='center', fontsize=11.5, fontweight='bold', color="#1A5276")

    # Table players
    t1 = """PLAYERS (Người chơi)
--------------------------------------
PK  id : SERIAL
     email : VARCHAR(100) [UNIQUE, NOT NULL]
     username : VARCHAR(50) [UNIQUE, NOT NULL]
     password_hash : VARCHAR(255) [NOT NULL]
     exp : INT [DEFAULT 0]
     level : INT [DEFAULT 1]
     wins : INT [DEFAULT 0]
     losses : INT [DEFAULT 0]
     avatar_id : INT [DEFAULT 0]
     cue_color : VARCHAR(20) [DEFAULT '#DEB887']
     created_at : TIMESTAMP [DEFAULT NOW()]"""
    draw_box(ax, 3, 35, 42, 56, "PLAYERS (Bảng tài khoản chính)", t1, "#EBF5FB", "#2980B9")

    # Table match_history
    t2 = """MATCH_HISTORY (Lịch sử đấu)
--------------------------------------
PK  id : SERIAL
FK  winner_id : INT -> players(id)
FK  loser_id : INT -> players(id)
     exp_gained : INT [DEFAULT 100]
     played_at : TIMESTAMP [DEFAULT NOW()]"""
    draw_box(ax, 55, 62, 42, 29, "MATCH_HISTORY", t2, "#FCF3CF", "#F39C12")

    # Table friendships
    t3 = """FRIENDSHIPS (Quan hệ bạn bè)
--------------------------------------
PK,FK user_id : INT -> players(id)
PK,FK friend_id : INT -> players(id)
      status : VARCHAR(20) [PENDING/ACCEPTED]
      created_at : TIMESTAMP [DEFAULT NOW()]"""
    draw_box(ax, 55, 33, 42, 25, "FRIENDSHIPS", t3, "#D4EFDF", "#27AE60")

    # Table messages
    t4 = """MESSAGES (Tin nhắn trò chuyện)
--------------------------------------
PK  id : SERIAL
FK  sender_id : INT -> players(id)
FK  receiver_id : INT -> players(id)
     content : TEXT [NOT NULL]
     is_read : BOOLEAN [DEFAULT FALSE]
     sent_at : TIMESTAMP [DEFAULT NOW()]"""
    draw_box(ax, 55, 4, 42, 26, "MESSAGES", t4, "#E8DAEF", "#8E44AD")

    # Mối quan hệ
    draw_arrow(ax, 45, 75, 55, 75, "1 : N (winner/loser)")
    draw_arrow(ax, 45, 50, 55, 50, "N : M (tự quan hệ)")
    draw_arrow(ax, 45, 40, 55, 20, "1 : N (sender/receiver)")

    plt.tight_layout()
    fig.savefig(os.path.join(OUT_DIR, "database_erd.png"), dpi=300)
    plt.close(fig)

# ==============================================================================
# 5. SƠ ĐỒ TUẦN TỰ: ĐĂNG NHẬP (Sequence Login)
# ==============================================================================
def gen_seq_login():
    fig, ax = setup_canvas(9, 6.5)
    ax.text(50, 96, "SƠ ĐỒ TUẦN TỰ: TIẾN TRÌNH XÁC THỰC & ĐĂNG NHẬP",
            ha='center', va='center', fontsize=11, fontweight='bold', color="#1A5276")

    # Lifelines
    actors = [("Client (UI)", 15), ("NetworkClient", 38), ("ClientHandler (Server)", 63), ("DatabaseManager / Auth", 88)]
    for name, x in actors:
        draw_box(ax, x-9, 86, 18, 7, name, "", "#EBF5FB", "#2980B9")
        ax.plot([x, x], [10, 86], ls="--", color="#BDC3C7", lw=1.2)

    # Messages
    y = 78
    draw_arrow(ax, 15, y, 38, y, "1. Nhập username, password -> onLogin()")
    y -= 11
    draw_arrow(ax, 38, y, 63, y, "2. Gửi LOGIN_REQUEST [1, user, pass]")
    y -= 11
    draw_arrow(ax, 63, y, 88, y, "3. findUserByUsername() & verifyPassword()")
    y -= 11
    draw_arrow(ax, 88, y, 63, y, "4. Trả về PlayerInfo (id, exp, level, skin)")
    y -= 11
    draw_arrow(ax, 63, y, 38, y, "5. LOGIN_RESPONSE [2, true, user, exp, lv, skin]")
    y -= 11
    draw_arrow(ax, 38, y, 15, y, "6. Platform.runLater() -> showLobbyScene()")

    plt.tight_layout()
    fig.savefig(os.path.join(OUT_DIR, "seq_login.png"), dpi=300)
    plt.close(fig)

# ==============================================================================
# 6. SƠ ĐỒ TUẦN TỰ: GHÉP TRẬN & BẮT ĐẦU (Sequence Matchmaking)
# ==============================================================================
def gen_seq_matchmaking():
    fig, ax = setup_canvas(10, 6.5)
    ax.text(50, 96, "SƠ ĐỒ TUẦN TỰ: GHÉP TRẬN & BẮT ĐẦU VÁN ĐẤU",
            ha='center', va='center', fontsize=11, fontweight='bold', color="#1A5276")

    actors = [("Client A", 12), ("LobbyManager", 38), ("GameRoom (Server)", 65), ("Client B", 88)]
    for name, x in actors:
        draw_box(ax, x-9, 86, 18, 7, name, "", "#EBF5FB", "#2980B9")
        ax.plot([x, x], [10, 86], ls="--", color="#BDC3C7", lw=1.2)

    y = 78
    draw_arrow(ax, 12, y, 38, y, "1. Gửi FIND_MATCH [5]")
    y -= 10
    draw_arrow(ax, 88, y, 38, y, "2. Gửi FIND_MATCH [5]")
    y -= 10
    draw_arrow(ax, 38, y, 65, y, "3. Khởi tạo GameRoom(ClientA, ClientB)")
    y -= 10
    draw_arrow(ax, 65, y, 12, y, "4. MATCH_FOUND [6, đối thủ, avatar, skin]")
    draw_arrow(ax, 65, y-2, 88, y-2, "MATCH_FOUND [6, đối thủ, avatar, skin]")
    y -= 14
    draw_arrow(ax, 12, y, 65, y, "5. PREGAME_READY [8]")
    draw_arrow(ax, 88, y-2, 65, y-2, "PREGAME_READY [8]")
    y -= 14
    draw_arrow(ax, 65, y, 12, y, "6. GAME_START [9, playerIdx=0, matchSeed]")
    draw_arrow(ax, 65, y-2, 88, y-2, "GAME_START [9, playerIdx=1, matchSeed]")

    plt.tight_layout()
    fig.savefig(os.path.join(OUT_DIR, "seq_matchmaking.png"), dpi=300)
    plt.close(fig)

# ==============================================================================
# 7. SƠ ĐỒ TUẦN TỰ: ĐỒNG BỘ CÚ ĐÁNH & VẬT LÝ (Sequence Cue Shot)
# ==============================================================================
def gen_seq_cue_shot():
    fig, ax = setup_canvas(10, 7)
    ax.text(50, 96, "SƠ ĐỒ TUẦN TỰ: THỰC HIỆN CÚ ĐÁNH & ĐỒNG BỘ VẬT LÝ",
            ha='center', va='center', fontsize=11, fontweight='bold', color="#1A5276")

    actors = [("Client A (Người bắn)", 15), ("GameRoom (Server)", 50), ("Client B (Người xem)", 85)]
    for name, x in actors:
        draw_box(ax, x-12, 86, 24, 7, name, "", "#EBF5FB", "#2980B9")
        ax.plot([x, x], [8, 86], ls="--", color="#BDC3C7", lw=1.2)

    y = 78
    draw_arrow(ax, 15, y, 50, y, "1. CUE_SHOT [10, angle, force, cueX, cueY, spinX, spinY]")
    y -= 10
    draw_arrow(ax, 50, y, 85, y, "2. GAME_STATE [11, thông số cú đánh]")
    y -= 12
    # Local physics
    ax.text(15, y, "[PhysicsEngine A chạy mô phỏng]", fontsize=8, color="#C0392B", fontweight='bold', ha='center')
    ax.text(85, y, "[PhysicsEngine B chạy mô phỏng]", fontsize=8, color="#C0392B", fontweight='bold', ha='center')
    y -= 12
    draw_arrow(ax, 15, y, 50, y, "3. PHYSICS_DONE [20, keepTurn, isFoul] (Sau khi dừng bi)")
    y -= 12
    draw_arrow(ax, 50, y, 15, y, "4. TURN_CHANGE [12, isMyTurn, ballInHand, reason]")
    draw_arrow(ax, 50, y-3, 85, y-3, "TURN_CHANGE [12, isMyTurn, ballInHand, reason]")
    y -= 14
    ax.text(50, y, "5. Nếu có lỗi bi cái: Người kế tiếp nhận quyền Ball-in-hand tự do di chuyển",
            fontsize=8, color="#27AE60", ha='center', bbox=dict(boxstyle="round,pad=0.2", fc="#EAFAF1", ec="#27AE60"))

    plt.tight_layout()
    fig.savefig(os.path.join(OUT_DIR, "seq_cue_shot.png"), dpi=300)
    plt.close(fig)

# ==============================================================================
# 8. SƠ ĐỒ TUẦN TỰ: KẾT THÚC TRẬN & CẬP NHẬT EXP (Sequence Game Over)
# ==============================================================================
def gen_seq_game_over():
    fig, ax = setup_canvas(10, 6.5)
    ax.text(50, 96, "SƠ ĐỒ TUẦN TỰ: KẾT THÚC VÁN ĐẤU & GHI NHẬN EXP",
            ha='center', va='center', fontsize=11, fontweight='bold', color="#1A5276")

    actors = [("Client A (Thắng)", 12), ("GameRoom (Server)", 40), ("DatabaseManager", 68), ("Client B (Thua)", 90)]
    for name, x in actors:
        draw_box(ax, x-9, 86, 18, 7, name, "", "#EBF5FB", "#2980B9")
        ax.plot([x, x], [10, 86], ls="--", color="#BDC3C7", lw=1.2)

    y = 78
    draw_arrow(ax, 12, y, 40, y, "1. CLAIM_WIN [21, isWin=true] (Ăn bi 8 hợp lệ)")
    y -= 11
    draw_arrow(ax, 40, y, 68, y, "2. recordMatchResult(winnerId, loserId)")
    y -= 11
    draw_arrow(ax, 68, y, 40, y, "3. Update EXP (+100), Level (exp/200), Wins/Losses")
    y -= 11
    draw_arrow(ax, 40, y, 12, y, "4. GAME_OVER [14, youWin=true, expGain=100]")
    draw_arrow(ax, 40, y-3, 90, y-3, "GAME_OVER [14, youWin=false, expGain=0]")
    y -= 15
    draw_arrow(ax, 12, y, 12, y-6, "Hiển thị ResultScene & Pháo hoa", rad=1.5)
    draw_arrow(ax, 90, y, 90, y-6, "Hiển thị ResultScene Thua", rad=1.5)

    plt.tight_layout()
    fig.savefig(os.path.join(OUT_DIR, "seq_game_over.png"), dpi=300)
    plt.close(fig)

# ==============================================================================
# 9. SƠ ĐỒ TUẦN TỰ: TRANG BỊ VÀ ĐỒNG BỘ SKIN (Sequence Skin Equip)
# ==============================================================================
def gen_seq_skin_equip():
    fig, ax = setup_canvas(10, 6.5)
    ax.text(50, 96, "SƠ ĐỒ TUẦN TỰ: KIỂM TRA MỞ KHÓA & TRANG BỊ SKIN",
            ha='center', va='center', fontsize=11, fontweight='bold', color="#1A5276")

    actors = [("Client A (Settings UI)", 15), ("ClientHandler (Server)", 45), ("CueSkinConfig", 72), ("PostgreSQL DB", 90)]
    for name, x in actors:
        draw_box(ax, x-10, 86, 20, 7, name, "", "#EBF5FB", "#2980B9")
        ax.plot([x, x], [10, 86], ls="--", color="#BDC3C7", lw=1.2)

    y = 78
    draw_arrow(ax, 15, y, 45, y, "1. SETTINGS_UPDATE [22, avatarId, cueSkinId]")
    y -= 11
    draw_arrow(ax, 45, y, 72, y, "2. isSkinUnlocked(cueSkinId, playerLevel)")
    y -= 11
    draw_arrow(ax, 72, y, 45, y, "3. Trả về hợp lệ (Level >= Required)")
    y -= 11
    draw_arrow(ax, 45, y, 90, y, "4. UPDATE players SET cue_color=?, avatar_id=?")
    y -= 11
    draw_arrow(ax, 45, y, 15, y, "5. SETTINGS_RESPONSE [23, success=true]")
    y -= 11
    ax.text(50, y, "6. Cập nhật PlayerSession & Áp dụng Skin gậy cơ 3D trong trận",
            fontsize=8, color="#2980B9", ha='center', bbox=dict(boxstyle="round,pad=0.2", fc="#EBF5FB", ec="#2980B9"))

    plt.tight_layout()
    fig.savefig(os.path.join(OUT_DIR, "seq_skin_equip.png"), dpi=300)
    plt.close(fig)

# ==============================================================================
# 10. SƠ ĐỒ USE CASE (Use Case Diagram)
# ==============================================================================
def gen_use_case_diagram():
    fig, ax = setup_canvas(10, 7)
    ax.text(50, 96, "SƠ ĐỒ USE CASE TỔNG THỂ HỆ THỐNG GAME 8-BALL POOL",
            ha='center', va='center', fontsize=11.5, fontweight='bold', color="#1A5276")

    # Actor Người chơi
    ax.plot([10, 10], [55, 65], color="#2C3E50", lw=2)
    circle = plt.Circle((10, 70), 3.5, ec="#2C3E50", fc="#EBF5FB", lw=2)
    ax.add_patch(circle)
    ax.plot([5, 15], [60, 60], color="#2C3E50", lw=2) # arms
    ax.plot([10, 5], [55, 45], color="#2C3E50", lw=2)  # leg 1
    ax.plot([10, 15], [55, 45], color="#2C3E50", lw=2) # leg 2
    ax.text(10, 39, "NGƯỜI CHƠI\n(Player Client)", ha='center', fontsize=9, fontweight='bold', color="#2C3E50")

    # Boundary System
    draw_dashed_boundary(ax, 28, 5, 68, 87, "Hệ Thống 8-Ball Pool Multiplayer")

    cases = [
        ("UC-01: Đăng ký & Đăng nhập tài khoản", 32, 78),
        ("UC-02: Cấu hình Server & Tự động dò IP (UDP)", 32, 65),
        ("UC-03: Tìm trận ngẫu nhiên (Matchmaking)", 32, 52),
        ("UC-04: Kết bạn, Trò chuyện & Thách đấu bạn bè", 32, 39),
        ("UC-05: Chơi bi-a (Ngắm, Kéo lực, Trô bi, Ép-phê)", 32, 26),
        ("UC-06: Chế độ Luyện tập tự do (Offline)", 32, 13),
        ("UC-07: Xem Bảng xếp hạng (Top EXP / Level)", 66, 78),
        ("UC-08: Mở khóa & Trang bị Skin gậy theo cấp", 66, 65),
        ("UC-09: Tùy chỉnh Avatar & Cài đặt hệ thống", 66, 52),
        ("UC-10: Đồng bộ lượt, Bắt lỗi bi cái & Bi 8", 66, 39),
        ("UC-11: Ghi nhận kết quả trận đấu & Cộng EXP", 66, 26),
    ]

    for title, cx, cy in cases:
        draw_box(ax, cx, cy, 30, 8, title, "", "#F4F6F7", "#5D6D7E", title_color="#1A252C", corner_radius=1.8)
        draw_arrow(ax, 16, 58, cx, cy + 4, color="#7F8C8D")

    plt.tight_layout()
    fig.savefig(os.path.join(OUT_DIR, "use_case_diagram.png"), dpi=300)
    plt.close(fig)

if __name__ == '__main__':
    print("Generating diagrams...")
    gen_architecture_diagram()
    print("- architecture_diagram.png")
    gen_package_diagram()
    print("- package_diagram.png")
    gen_match_state_diagram()
    print("- match_state_diagram.png")
    gen_database_erd()
    print("- database_erd.png")
    gen_seq_login()
    print("- seq_login.png")
    gen_seq_matchmaking()
    print("- seq_matchmaking.png")
    gen_seq_cue_shot()
    print("- seq_cue_shot.png")
    gen_seq_game_over()
    print("- seq_game_over.png")
    gen_seq_skin_equip()
    print("- seq_skin_equip.png")
    gen_use_case_diagram()
    print("- use_case_diagram.png")
    print("Done! All 10 diagrams generated successfully in:", OUT_DIR)
