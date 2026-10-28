"""Generate the 14 UML sources required by the assignment. Render with PlantUML separately."""
from pathlib import Path
import json
base=Path(__file__).resolve().parent
out=base/'uml';out.mkdir(exist_ok=True)
groups=json.loads((base/'uml_spec.json').read_text())
manifest=[]
style='''@startuml
skinparam backgroundColor white
skinparam defaultFontName DejaVu Sans
skinparam defaultFontSize 15
skinparam shadowing false
skinparam dpi 180
skinparam ArrowColor #3D5E78
skinparam ArrowFontSize 13
skinparam roundcorner 12
skinparam sequenceMessageAlign center
skinparam maxMessageSize 46
skinparam ParticipantPadding 12
skinparam BoxPadding 10
skinparam usecaseBackgroundColor #EDF4FA
skinparam usecaseBorderColor #3D5E78
skinparam activityBackgroundColor #EDF4FA
skinparam activityBorderColor #3D5E78
skinparam activityDiamondBackgroundColor #FFF1D6
skinparam componentBackgroundColor #E8F3ED
skinparam componentBorderColor #3D5E78
skinparam NoteBackgroundColor #FFF5DC
skinparam NoteBorderColor #A58842
hide footbox
'''
def add(tv,id,kind,title,ucs,body):
 if kind=='Activity':
  lines=[]
  for line in body.splitlines():
   if line.lstrip().startswith(':') and line.count(';')>1:
    head,tail=line.rsplit(';',1);line=head.replace(';',',')+';'+tail
   lines.append(line)
  body='\n'.join(lines)
 (out/(id+'.puml')).write_text(style+'title '+title+'\n'+body+'\n@enduml\n')
 manifest.append(dict(tv=tv,id=id,type=kind,title=title,use_cases=ucs,path='uml/'+id+'.png'))
for g in groups:
 tv=g['tv'];prefix=g['prefix']
 body='left to right direction\nactor "Người chơi" as Player\n'
 if prefix in ['C','G','UI']:body+='actor "Người chơi khác" as Other\n'
 if prefix=='D':body+='actor "GameRoom" as Room <<hệ thống>>\n'
 if prefix=='G':body+='actor "GameScene" as Loop <<hệ thống>>\n'
 body+='rectangle "'+tv+' — '+g['title']+'" {\n'
 for i,u in enumerate(g['items'],1):
  # Controlled line wrapping for legibility at A4 width.
  words=u['name'].split();mid=len(words)//2
  label=' '.join(words[:mid])+'\\n'+' '.join(words[mid:]) if len(u['name'])>28 else u['name']
  body+=f'usecase "{u["id"]}\\n{label}" as U{i}\n'
 body+='}\n'
 for i,u in enumerate(g['items'],1):
  actor='Room' if prefix=='D' and i==4 else 'Loop' if prefix=='G' and i in [5,6] else 'Player'
  body+=f'{actor} -- U{i}\n'
 if prefix=='C':body+='Other -- U5\nOther -- U6\n'
 if prefix=='G':body+='Other -- U3\nOther -- U7\n'
 if prefix=='UI':body+='Other -- U4\nOther -- U5\n'
 add(tv,prefix.lower()+'_usecase','Use Case',tv+' • Các use case thuộc phân hệ',[u['id'] for u in g['items']],body)
# TV1: exactly Use Case, Sequence, Activity.
add('TV1','c_sequence','Sequence','TV1 • Kết nối TCP và trao đổi PING',['UC-C01','UC-C03','UC-C04'],r'''
actor "Người chơi" as P
participant "ServerConfigDialog" as UI
participant "NetworkClient" as NC
participant "ServerMain" as SM
participant "ClientHandler" as CH
P -> UI : Nhập host/port và kết nối
UI -> NC : connect(host, port)
NC -> SM : Mở TCP socket (timeout 2,5 s)
alt Kết nối thành công
 SM -> CH : Tạo handler và chạy luồng đọc
 NC -> NC : Tạo stream, listener và ping thread
 loop Ngay lúc kết nối và mỗi 2 giây
  NC -> CH : PING(18), timestamp: long
  CH --> NC : PING(18), cùng timestamp
  NC -> NC : RTT = hiện tại - timestamp
 end
 NC --> UI : connected = true
else Kết nối thất bại
 NC --> UI : false / thông báo lỗi
end
note over NC, CH
 Không có HELLO/version handshake trong 00bf1cc.
 Sơ đồ mô tả luồng mạng, không phải đăng nhập.
end note
''')
add('TV1','c_activity','Activity','TV1 • Điều phối thách đấu trực tiếp',['UC-C06','UC-C07'],r'''
start
:Nhận CHALLENGE_REQUEST,
trim và tra tên online;
if (Tên rỗng / tự mời / offline
hoặc người nhận bận?) then (Có)
 :Gửi kết quả thất bại;
 stop
endif
:Lưu lời mời, gửi CHALLENGE_RECEIVE,\nđặt timer 15 giây;
if (Không phản hồi trong 15 giây?) then (Có)
 :Xóa lời mời,
báo hết thời gian;
 stop
endif
:Hủy timer khi nhận phản hồi
(hành vi hiện tại);
if (Tên phản hồi không khớp?) then (Có)
 :Bỏ phản hồi,
currentChallenger vẫn còn;
 note right
  Giới hạn: lời mời có thể
  tiếp tục ở trạng thái bận.
 end note
 stop
endif
:Xóa currentChallenger;
if (Người mời offline / đã có phòng
hoặc người nhận từ chối?) then (Có)
 :Trả thất bại/từ chối;
 stop
endif
:Xóa cả hai khỏi queue,\ntạo GameRoom, gửi MATCH_FOUND;
stop
''')
add('TV2','d_register_activity','Activity','TV2 • Đăng ký và lưu tài khoản',['UC-D01'],r'''
start
:Nhận email, username, password;
if (Email/tên không trống\nvà password ít nhất 6 ký tự?) then (Có)
 :Sinh salt 16 byte; băm SHA-256;
 :DB manager trim email/username;\nchuyển email về chữ thường;
 if (Email đã tồn tại theo LOWER?) then (Có)
  :Trả EMAIL_EXISTS;
 else (Không)
  if (Username đã tồn tại theo LOWER?) then (Có)
   :Trả USERNAME_EXISTS;
  else (Không)
   :INSERT players bằng PreparedStatement;
   if (INSERT thành công?) then (Có)
    :Trả OK;
   else (Không)
    :Phân loại lỗi SQL và trả lỗi;
   endif
  endif
 endif
else (Không)
 :Trả ERROR;
endif
:Gửi REGISTER_RESPONSE;
note right: Đây là kiểm tra server hiện có.\nChưa xác minh email/OTP hoặc kiểm tra\nđịnh dạng tương đương client.
stop
''')
add('TV2','d_result_activity','Activity','TV2 • Ghi kết quả, EXP và lịch sử',['UC-D04'],r'''
start
:GameRoom chọn người thắng/thua;
if (gameFinished đã true?) then (Có)
 :Bỏ sự kiện kết thúc lặp;
 stop
endif
:Đặt gameFinished = true;
if (Database còn kết nối?) then (Có)
 :UPDATE người thắng:\nwins + 1, EXP + 100, level = floor(EXP/200) + 1;
 :UPDATE người thua: losses + 1;
 :INSERT match_history;
 note right
  Các câu SQL tuần tự, chưa có transaction chung.
  SQLException có thể dừng phần còn lại;
  dữ liệu trước lỗi không được rollback tự động.
 end note
else (Không)
 :recordMatchResult trả về, không ghi DB;
endif
:Nạp lại hồ sơ và gửi GAME_OVER\ntheo logic GameRoom hiện có;
:Dọn phòng;
stop
''')
add('TV2','d_login_sequence','Sequence','TV2 • Đăng nhập bằng username hoặc email',['UC-D02'],r'''
actor "Người chơi" as P
participant "Client" as C
participant "ClientHandler" as H
participant "AuthManager" as A
participant "DatabaseManager" as D
database "PostgreSQL" as DB
P -> C : Nhập định danh và mật khẩu
C -> H : LOGIN_REQUEST(1)
H -> A : login(identity, password)
A -> D : getPlayerByUsername(identity)
D -> DB : SELECT ... WHERE LOWER(username)=?\nOR LOWER(email)=?
DB --> D : Bản ghi hoặc rỗng
D --> A : PlayerInfo hoặc null
A -> A : verifyPassword với salt đã lưu
alt Xác thực thành công
 A --> H : PlayerInfo
 H -> H : Thiết lập phiên / onlinePlayers
 H --> C : LOGIN_RESPONSE(true, hồ sơ)
 C --> P : Vào sảnh
else Không tìm thấy / sai mật khẩu
 A --> H : null
 H --> C : LOGIN_RESPONSE(false, lỗi)
 C --> P : Hiển thị lỗi
end
''')
add('TV3','g_activity','Activity','TV3 • Một cú đánh và xử lý cuối lượt',['UC-G02','UC-G03','UC-G04','UC-G05','UC-G06','UC-G07'],r'''
start
if (Chưa đến lượt / bi đang chạy\nhoặc trận đã kết thúc?) then (Có)
 :Chỉ quan sát / chờ lượt;
 stop
endif
:Đặt bi nếu được phép,\nngắm, kéo lực và chỉnh xoáy;
if (Kéo quá ngắn hoặc bi cái không hoạt động?) then (Có)
 :Không áp dụng cú đánh;
 stop
endif
:Gửi CUE_SHOT nếu chơi mạng,\náp dụng cú đánh ngay tại máy đánh;
repeat
 :PhysicsEngine.update: 10 bước con,\nma sát, va chạm và kiểm tra lỗ;
repeat while (Còn bi chuyển động/chìm?) is (Có) not (Không)
:Đánh giá bi vào lỗ, lỗi\nvà điều kiện kết thúc;
if (Có kết quả trận theo client?) then (Có)
 :Gửi CLAIM_WIN nếu chơi mạng;
else (Không)
 :Gửi PHYSICS_DONE nếu chơi mạng,\nchờ TURN_CHANGE từ server;
endif
stop
''')
add('TV3','g_sequence','Sequence','TV3 • Truyền cú đánh và đổi lượt hiện tại',['UC-G03','UC-G04','UC-G07'],r'''
participant "Client người đánh" as A
participant "ClientHandler" as H
participant "GameRoom" as R
participant "Client đối thủ" as B
A -> H : CUE_AIM(47): bool + 4 double
H -> R : processCueAim(...)
R -> B : CUE_AIM (chỉ đúng người có lượt)
B -> B : Nội suy góc/lực mỗi khung hình
A -> H : CUE_SHOT(10): 6 double
A -> A : applyCueShot ngay, không chờ ACK
H -> R : processCueShot(...)
R -> R : Kiểm tra gameStarted/gameFinished và người có lượt
R -> B : GAME_STATE(11): 6 double
B -> B : pendingShot → mô phỏng cục bộ
A -> H : PHYSICS_DONE(20): keepTurn, isFoul
H -> R : onPhysicsDone(...)
R --> A : TURN_CHANGE(12)
R --> B : TURN_CHANGE(12)
note over A,B
 Không có token lượt, STATE_ACK hoặc chốt cùng vị trí 16 bi.
 Cú đánh lặp còn có thể được chuyển tiếp.
end note
''')
add('TV4','ui_activity','Activity','TV4 • Cá nhân hóa avatar, gậy và âm thanh',['UC-UI08','UC-UI09'],r'''
start
:Mở SettingsDialog;
if (Chỉnh âm thanh / VFX?) then (Có)
 :Áp dụng BGM, SFX, mute hoặc VFX;
 :SoundManager lưu tùy chọn âm thanh\nvào Preferences;
 note right: VFX là cờ cục bộ trong CueSkinManager;\nkhông có packet gameplay tương ứng.
else (Avatar / gậy)
 :Chọn avatar; xem preview skin và cấp yêu cầu;
 :Chọn skin được phép trang bị tại UI;
endif
if (Bấm lưu?) then (Có)
 :sendSettingsUpdate(avatarId, skinId);
 :Cập nhật session cục bộ và gọi onSaveCallback;
 :Dừng previewTimer; đóng dialog;
 note right
  UI hiện cập nhật trước xác nhận ghi DB.
  Server kiểm tra cấp và lưu thuộc UC-D03.
  SETTINGS_RESPONSE còn lỗi đọc thừa string.
 end note
else (Đóng / không lưu)
 :Kết thúc chỉnh sửa theo hành vi dialog;
endif
stop
''')
add('TV4','ui_sequence','Sequence','TV4 • Popup thách đấu, toast và chuyển cảnh',['UC-UI01','UC-UI03','UC-UI05'],r'''
actor "Người chơi" as P
participant "NetworkClient" as N
participant "LobbyScene" as L
participant "FX Application Thread" as FX
participant "Popup / Timeline" as POP
participant "SceneManager" as SM
N -> L : CHALLENGE_RECEIVE(challenger)
L -> FX : Platform.runLater(...)
FX -> POP : show, alwaysOnTop, toFront, focus\nđịnh vị và chạy đếm 10 giây
POP --> P : Hiển thị lời mời
alt Người chơi chọn trước hết hạn
 P -> POP : Đồng ý / Từ chối
 POP -> L : closeActiveChallengeDialog
 L -> N : sendChallengeResponse(name, accept)
else Hết 10 giây
 POP -> L : Đóng lời mời và hiện toast
 L -> N : sendChallengeResponse(name, false)
end
alt Nhận MATCH_FOUND
 N -> L : Thông tin đối thủ
 L -> FX : Platform.runLater(...)
 FX -> L : Dừng refresh, đóng popup/toast
 FX -> SM : showPreGame(...)
else Nhận CHALLENGE_RESULT
 N -> L : success, message
 L -> FX : Platform.runLater(...)
 FX -> L : Đóng popup; showToast(message)
end
''')
add('TV4','ui_component','Component','TV4 • Thành phần giao diện và cá nhân hóa',[f'UC-UI{i:02d}' for i in range(1,10)],r'''
top to bottom direction
skinparam componentStyle uml2
package "Client JavaFX" {
 component "Điều hướng\nClientMain / SceneManager" as Nav
 component "Các màn hình nghiệp vụ\nLogin / Register / Lobby\nPreGame / Result / Leaderboard" as Views
 component "GameScene\nCanvas / HUD" as Game
 component "SettingsDialog" as Settings
 component "Hiển thị và cá nhân hóa\nAvatarManager / CueSkinManager\nTheme / SilverCornerPane / VFX" as Visual
 component "SoundManager" as Sound
 component "NetworkClient\nPacket callbacks" as Net
}
component "Server\n(TV1 / TV2)" as Server
Nav --> Views : điều hướng
Nav --> Game : điều hướng
Views --> Settings : mở dialog
Settings --> Visual : preview
Game --> Visual : render
Settings --> Sound : cấu hình
Game --> Sound : sự kiện
Views ..> Visual : style
Views --> Net : nghiệp vụ
Game --> Net : gameplay
Settings --> Net : lưu hồ sơ
Net --> Server : TCP
note bottom of Nav
 Nhóm logic các lớp hiện có,
 không phải các dịch vụ độc lập.
end note
''')
# Enforce exact per-member allocation and order, even though sources were generated in groups.
order=['c_usecase','c_sequence','c_activity','d_usecase','d_register_activity','d_result_activity','d_login_sequence','g_usecase','g_activity','g_sequence','ui_usecase','ui_activity','ui_sequence','ui_component']
manifest.sort(key=lambda d:order.index(d['id']))
for i,g in enumerate(groups,1):
 diagrams=[d for d in manifest if d['tv']==g['tv']]
 for j,d in enumerate(diagrams,1):d['number']=f'{i}.{j}'
assert len(manifest)==14
(base/'uml_manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2))
print('Generated 14 PlantUML sources and manifest.')
