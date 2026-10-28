"""Build report content from the agreed 7/7/8/9 allocation and current-code use cases."""
from pathlib import Path
import json
base=Path(__file__).resolve().parent
groups=json.loads((base/'uml_spec.json').read_text())
diagrams=json.loads((base/'uml_manifest.json').read_text())
intro='''# BÁO CÁO PHÂN TÍCH VÀ THIẾT KẾ HỆ THỐNG

## TRÒ CHƠI BI-A HAI NGƯỜI QUA MẠNG — PHÂN CÔNG 4 PHÂN HỆ

Học phần: Lập trình mạng

Trường / Khoa / Lớp: [Bổ sung]

Giảng viên hướng dẫn: [Bổ sung]

Nhóm thực hiện: TV1 — TV2 — TV3 — TV4

Họ tên và MSSV tương ứng: [Nhóm bổ sung theo phân công mới]

Ngày cập nhật: 28/10/2026 — múi giờ Việt Nam.

Mã ứng dụng đối chiếu: 00bf1ccaf8df2cdd54b79cb75ca188ae9c38627e.

## TÓM TẮT

Báo cáo phân tích hệ thống game bi-a Java/JavaFX theo bốn phân hệ: kết nối và giao thức; xác thực và dữ liệu; gameplay và vật lý; giao diện và cá nhân hóa. Nội dung được tổ chức theo bảng phân công mới do người dùng cung cấp ngày 28/10/2026, gồm 31 use case và 14 biểu đồ UML. Mỗi use case có tác nhân, tiền điều kiện, luồng chính, ngoại lệ, hậu điều kiện và liên hệ mã nguồn.

Các biểu đồ mô tả hành vi hiện có ở phiên bản 00bf1cc, không giả định rằng các cơ chế đồng bộ v2 của nhánh trước đang hoạt động. Hệ thống hiện định nghĩa 47 mã packet, dùng TCP cổng 12345 cho nghiệp vụ và UDP cổng 12346 để khám phá máy chủ LAN. Vật lý chạy tại client; chuyển lượt dựa trên PHYSICS_DONE, chưa có snapshot 16 bi và ACK của hai máy.

Kết quả kiểm tra ngày 26/10/2026 được dùng làm minh chứng có ghi rõ phạm vi. Lần cập nhật ngày 28/10 tập trung thay cấu trúc báo cáo và bổ sung đặc tả UML; không sửa mã ứng dụng và không gán cho các biểu đồ ý nghĩa là chức năng đã vượt qua mọi bài nghiệm thu.

## PHÂN CÔNG MỚI VÀ QUY ƯỚC

| Thành viên | Phân hệ phụ trách | Use Cases | Số biểu đồ | Loại biểu đồ |
| --- | --- | --- | --- | --- |
| TV1 | Quản lý kết nối & Giao thức truyền tin | UC-C01 → UC-C07 (7 UC) | 3 | Use Case, Sequence, Activity |
| TV2 | Xác thực & Quản lý dữ liệu | UC-D01 → UC-D07 (7 UC) | 4 | Use Case, Activity ×2, Sequence |
| TV3 | Gameplay & Engine Vật lý | UC-G01 → UC-G08 (8 UC) | 3 | Use Case, Activity, Sequence |
| TV4 | Giao diện & Cá nhân hóa | UC-UI01 → UC-UI09 (9 UC) | 4 | Use Case, Activity, Sequence, Component |
| Tổng | 4 phân hệ | 31 UC | 14 | 4 Use Case, 5 Activity, 4 Sequence, 1 Component |

Báo cáo giữ nhãn TV1–TV4 đúng theo ảnh phân công mới. Ảnh không cung cấp ánh xạ họ tên, vì vậy không tự gán các tên trong bảng phân công cũ cho những trách nhiệm mới. Mã use case do báo cáo đặt theo đúng dải đã yêu cầu; chúng không phải tên hằng số có sẵn trong mã Java.

Use case biểu diễn mục tiêu/chức năng ở ranh giới phân hệ. Với một số xử lý nền, GameRoom hoặc GameScene được dùng làm tác nhân hệ thống đối với phân hệ đang xét. Các bước kỹ thuật của engine được mô hình hóa như use case chức năng theo phạm vi bài tập; không đồng nhất mọi bước nội bộ với một thao tác riêng mà người chơi trực tiếp bấm.

Một chức năng xuyên nhiều phân hệ có các góc nhìn khác nhau: TV1 chuyển gói và điều phối phiên; TV2 xác thực/ghi dữ liệu; TV3 quyết định/mô phỏng gameplay; TV4 hiển thị và nhận thao tác. Ví dụ UC-UI08 chọn và xem skin, còn UC-D03 kiểm tra quyền và lưu skin. Hai use case liên quan nhưng không thay thế nhau.

Mỗi biểu đồ Use Case liệt kê đủ use case của thành viên. Các Activity/Sequence đi sâu vào luồng đại diện được nêu ở chú thích; không có yêu cầu mỗi use case phải có một biểu đồ Sequence riêng. Tổng số biểu đồ trong tài liệu được giữ đúng 3/4/3/4.

## DANH MỤC 14 BIỂU ĐỒ

| Mã hình | Thành viên | Loại | Nội dung / phạm vi |
| --- | --- | --- | --- |
'''
for d in diagrams:intro+=f'| {d["number"]} | {d["tv"]} | {d["type"]} | {d["title"].split(" • ",1)[1]} |\n'
texts={
'C':('''TV1 phụ trách đường đi dữ liệu từ client đến server, khám phá máy chủ, định dạng packet, đo độ trễ, ghép trận và vòng đời kết nối. Các lớp chính là NetworkClient, Packet, PacketType, ServerMain, ClientHandler và LobbyManager. Điều kiện xác thực và quyết định ghi dữ liệu thuộc TV2; việc vẽ màn hình thuộc TV4.

Giao thức có một byte mã loại và các trường tuần tự big-endian. Chuỗi dùng int độ dài cùng UTF-8, giới hạn đọc 65.536 byte. TCP chỉ bảo toàn luồng byte và thứ tự, nên bên nhận phải đọc chính xác payload. Client hiện ghi/flush trực tiếp; chưa có hàng đợi ghi giới hạn riêng hoặc handshake xác nhận phiên bản.

CUE_AIM mang một boolean và bốn double, tổng 34 byte cả mã gói. CUE_SHOT mang sáu double, tổng 49 byte. PING có long timestamp, gửi ngay lúc kết nối rồi mỗi hai giây. Các con số này mô tả định dạng/lịch gửi trong mã, không phải kết quả đo băng thông Internet.
''','''Sơ đồ Sequence 1.2 tập trung thiết lập TCP và ping, còn Activity 1.3 tập trung lời mời thách đấu. Các use case khác vẫn được thể hiện trong Use Case 1.1 và đặc tả riêng. Timer lời mời server là 15 giây; hộp thoại client đếm 10 giây. Nhánh phản hồi sai tên trong Activity được giữ lại vì đó là hành vi hiện có, không mô tả như đã sửa.

Các mối liên hệ xuyên phân hệ: sau UC-C01, TV2 xử lý UC-D01/UC-D02; UC-C05/UC-C06 tạo ngữ cảnh để TV3 thực hiện UC-G01; UC-C03 vận chuyển packet cho tất cả các phân hệ. Việc ngắt kết nối UC-C07 có thể kích hoạt ghi kết quả UC-D04 theo quy tắc phòng hiện tại.
'''),
'D':('''TV2 phụ trách xác thực, kiểm tra quyền ghi hồ sơ và các thao tác dữ liệu bền vững qua JDBC. AuthManager băm mật khẩu SHA-256 cùng salt ngẫu nhiên 16 byte. DatabaseManager thao tác các bảng players, match_history, friendships và messages trong PostgreSQL. Dữ liệu GUI được hiển thị bởi TV4; TV2 cung cấp kết quả và trạng thái để GUI sử dụng.

Email được trim và chuyển thường; tra cứu đăng nhập dùng LOWER(username) hoặc LOWER(email). Mã hiện tại chưa kiểm tra định dạng email ở server tương đương client, chưa xác minh quyền sở hữu email bằng thư/OTP và chưa có TLS. Đây là mô tả giới hạn thực tế, không phải tính năng bảo mật đã triển khai.

EXP thắng trận là 100; level = floor(EXP/200)+1. DatabaseManager giữ một Connection chung và recordMatchResult chưa bao các câu SQL trong transaction hoàn chỉnh. Activity 2.3 thể hiện ghi tuần tự và ghi chú việc không tự rollback toàn bộ khi lỗi giữa chừng.
''','''Bốn biểu đồ của TV2 gồm Use Case tổng thể, Activity đăng ký, Activity ghi kết quả và Sequence đăng nhập. Không thêm ERD thành biểu đồ thứ năm; cấu trúc lưu trữ được tóm tắt bằng bảng dưới đây để giữ đúng định mức.

| Bảng | Trường/ràng buộc chính | Use case sử dụng |
| --- | --- | --- |
| players | id PK; email/username UNIQUE; password_hash; exp, level, wins, losses; avatar_id, cue_color | UC-D01–UC-D05 |
| match_history | id PK; winner_id và loser_id FK; exp_gained; played_at | UC-D04 |
| friendships | Khóa kép user_id/friend_id; status; created_at | UC-D06 |
| messages | id PK; sender_id/receiver_id FK; content; is_read; sent_at | UC-D07 |

UNIQUE trên username gốc chưa thay cho UNIQUE trên LOWER(username) khi có yêu cầu đồng thời. Việc lấy username hoặc email trong một truy vấn còn cần kiểm tra trùng chéo định danh. Hai điểm này là nhận xét từ mã/schema; không được đánh dấu như đã có bài thử đồng thời đầy đủ.
'''),
'G':('''TV3 phụ trách bàn chơi, bi, ngắm/lực/xoáy, cú đánh, va chạm, xử lý lỗ và luật lượt. Ball biểu diễn x/y, vx/vy, spin và các trạng thái active/inPocket/isSinking. PhysicsEngine có sáu lỗ, xử lý 10 bước con trong mỗi update. Các hệ số hiện có gồm ma sát 0,985, phục hồi băng 0,78, phục hồi bi–bi 0,96.

GameScene gọi update theo AnimationTimer khi physicsRunning. Chưa có bộ tích lũy thời gian cố định 1/60 giây như nhánh v2 cũ. Máy đánh mô phỏng ngay sau gửi CUE_SHOT; server chỉ chuyển thông số cho đối thủ. Người đánh báo PHYSICS_DONE hoặc CLAIM_WIN theo logic client; server chưa kiểm chứng va chạm bằng một mô phỏng riêng.

MatchSeed dùng xáo 14 bi mục tiêu khác bi 8 và chia hai nhóm bảy bi. Đây là luật phân nhóm của dự án; không thể mô tả là luôn chia nhóm trơn/sọc chuẩn. Cùng seed không chứng minh mọi bi sau cú đánh sẽ khớp ở mọi tốc độ khung hình.
''','''Activity 3.2 mô tả đường đi từ quyền lượt tới mô phỏng và xử lý cuối cú đánh. Sequence 3.3 thể hiện rõ việc người đánh không chờ ACK từ server, server chỉ phát GAME_STATE cho đối thủ và không chốt snapshot cuối lượt.

Giới hạn đã tái hiện ở kiểm tra trước: hai READY của một client có thể đủ mở trận; gói CUE_SHOT lặp được chuyển tiếp; góc NaN có thể được relay. Đó là nội dung cần hoàn thiện, không được lược bỏ để biến sơ đồ thành một thiết kế lý tưởng khác với mã hiện có. Solo UC-G08 là chế độ cục bộ và không cần đối thủ mạng.
'''),
'UI':('''TV4 phụ trách tương tác, điều hướng, trình bày trạng thái và cá nhân hóa. Các lớp tiêu biểu gồm SceneManager, LoginScene, RegisterScene, LobbyScene, PreGameScene, GameScene, ResultScene, LeaderboardScene, SettingsDialog, AvatarManager, CueSkinManager và SoundManager. Giao diện tiêu thụ dữ liệu nghiệp vụ, không tự thay thế xác thực server hoặc cơ chế ghi bền vững.

Lời mời thách đấu dùng popup có owner, modality và alwaysOnTop; thao tác Timeline được đưa về FX Application Thread. Kết quả lời mời dùng toast thay cho Alert chặn màn hình. Solo ẩn vsBadge, oppBox và oppBallsBox, đồng thời loại chúng khỏi bố cục bằng managed=false.

Cá nhân hóa có hai đường xử lý: avatar/skin gửi SETTINGS_UPDATE tới server; âm thanh BGM/SFX/mute áp dụng cục bộ và lưu Preferences. VFX của gậy được điều khiển bằng cờ cục bộ. Không mô tả âm thanh hoặc VFX như packet gameplay mới nếu mã không gửi chúng.
''','''Component 4.4 là cách nhóm logic các lớp đang có; các hộp không đại diện microservice hoặc tiến trình triển khai riêng. NetworkClient là cầu nối với TV1; các thao tác lưu hồ sơ qua server thuộc TV2; GameScene đồng thời trình bày HUD và sử dụng logic TV3.

Activity 4.2 phản ánh saveSettings hiện cập nhật session/callback cục bộ rồi đóng dialog, không đợi xác nhận DB. Phản hồi SETTINGS_RESPONSE hiện có lỗi đọc thừa string ở client. Vì vậy hậu điều kiện “đã hiển thị lựa chọn” không đồng nghĩa “đã lưu thành công và được xác nhận”. Sequence 4.3 tập trung popup, phản hồi, toast và chuyển cảnh trước trận.
''')}
s=intro+'\n'
for k,g in enumerate(groups,1):
 prefix=g['prefix'];s+=f'# PHẦN {k}. {g["tv"]} — {g["title"].upper()}\n\n'
 s+=f'Thành viên phụ trách: **{g["tv"]}**. Họ tên/MSSV: [Bổ sung]. Phạm vi: **{len(g["items"])} use case**, **{len([d for d in diagrams if d["tv"]==g["tv"]])} biểu đồ**.\n\n'
 s+=f'## {k}.1. Phạm vi và ranh giới trách nhiệm\n\n'+texts[prefix][0]+'\n'
 s+=f'## {k}.2. Danh mục use case\n\n| Mã | Tên use case | Tác nhân chính |\n| --- | --- | --- |\n'
 for u in g['items']:s+=f'| {u["id"]} | {u["name"]} | {u["actor"]} |\n'
 s+=f'\n## {k}.3. Đặc tả use case\n\n'
 for u in g['items']:
  s+=f'### {u["id"]} — {u["name"]}\n\n'
  s+=f'**Tác nhân:** {u["actor"]}.\n\n**Tiền điều kiện:** {u["pre"]}\n\n**Luồng chính:**\n\n'
  steps=[x.strip().rstrip('.') for x in u['flow'].split(';') if x.strip()]
  for j,step in enumerate(steps,1):s+=f'{j}. {step[0].upper()+step[1:]}.\n'
  s+=f'\n**Ngoại lệ/giới hạn:** {u["alt"]}\n\n**Hậu điều kiện:** {u["post"]}\n\n**Mã nguồn liên quan:** {u["source"]}.\n\n'
 s+=f'## {k}.4. Biểu đồ UML của phân hệ\n\n'
 for d in diagrams:
  if d['tv']!=g['tv']:continue
  s+=f'### Biểu đồ {d["number"]} — {d["type"]}\n\n'
  s+='Phạm vi: '+', '.join(d['use_cases'])+'.\n\n'
  s+=f'![Hình {d["number"]} — {d["title"]}. Loại: {d["type"]}; dựng từ mã nguồn 00bf1cc.](%s)\n\n'%d['path']
 s+=f'## {k}.5. Liên hệ triển khai và giới hạn\n\n'+texts[prefix][1]+'\n'
s+='''## TỔNG HỢP ĐỐI CHIẾU VÀ KIỂM CHỨNG

### Phân biệt yêu cầu tài liệu và tính năng đã kiểm thử

Ngày 28/10/2026, main vẫn chứa mã ứng dụng tại 00bf1cc; các commit sau đó là cập nhật báo cáo. Lần này kiểm tra cấu trúc tài liệu: đủ 31 mã UC độc lập, không thiếu hoặc lặp trong danh mục, đủ 14 sơ đồ theo đúng loại của từng TV, các ảnh nhúng tồn tại và tài liệu Word/PDF đọc được. Không chạy lại gameplay chỉ để xác nhận việc thay bố cục báo cáo.

Các kết quả thực thi dưới đây được kế thừa có ghi ngày từ bộ kiểm tra 26/10/2026 trong reports/evidence/00bf1cc. Chúng không chứng minh toàn bộ 31 use case đã nghiệm thu. Mã thoát 0 của script mô tả hiện trạng cũng có thể đi cùng FINDING, vì script chủ động xác nhận việc tái hiện lỗi.

| Nhóm UC | Minh chứng đã có ngày 26/10 | Giới hạn áp dụng |
| --- | --- | --- |
| UC-C03, UC-C06, UC-C07 | TCP server thật: chấp nhận/từ chối lời mời, tìm tên khác kiểu chữ, timeout 15 giây, ngắt kết nối | Chưa stress test mời đồng thời hoặc mạng Internet |
| UC-D01, UC-D02 | Đăng ký và đăng nhập với tài khoản tạm; email sai định dạng vẫn được server nhận | Chưa thử mọi ràng buộc hoặc race đăng ký |
| UC-G01, UC-G03, UC-G04, UC-G07 | 60 mẫu ngắm relay; đổi lượt bằng PHYSICS_DONE; tái hiện READY/cú đánh lặp và NaN | Không phải bài hai UI qua jitter; không chứng minh khớp 16 bi |
| UC-UI02–UC-UI06 | JavaFX thật với transport giả lập: form, menu, chat, popup/toast, solo | Không thay cho kiểm tra đầu cuối hoặc Windows |
| UC-D03, UC-UI08 | Parser client đọc payload SETTINGS_RESPONSE chỉ boolean gây EOF | Chưa xác nhận toàn bộ quy trình lưu hồ sơ thành công |
| UC-D04–UC-D07, UC-G05–UC-G06, UC-UI07, UC-UI09 | Đã đối chiếu mã và mô tả chức năng | Chưa có nghiệm thu đầy đủ cho mọi luồng trong phạm vi báo cáo này |

### Các giới hạn cần ghi nhận

SETTINGS_RESPONSE có sự khác nhau giữa server gửi một boolean và client đọc boolean kèm string. READY dùng bộ đếm số packet thay vì hai cờ riêng. GameRoom chưa loại cú đánh lặp theo token/phase. Góc ngắm không hữu hạn có thể được chuyển tiếp. Các điểm này cần kiểm tra hợp đồng giao thức và tính hợp lệ dữ liệu tại server.

Lời mời thách đấu hủy timer trước khi kiểm tra tên phản hồi; tình huống sai tên sau khi timer đã thiết lập có thể để người nhận bận kéo dài. Chat thiếu message/conversation ID nên có thể gộp tin hợp lệ giống nhau hoặc áp dụng lịch sử cũ vào cuộc trò chuyện mới. Refresh bạn bè còn tình huống A→B→A nhưng áp dụng B khi menu đóng.

Cơ sở dữ liệu chưa có transaction bao trọn ghi kết quả và chưa dùng connection pool. Vật lý theo AnimationTimer chưa có bước thời gian cố định; chưa chốt bàn bằng snapshot/ACK. Các sơ đồ giữ nguyên các giới hạn này dưới dạng nhánh hoặc ghi chú, không ngầm coi chúng đã được sửa.

### Phạm vi nghiệm thu còn thiếu

Cần kiểm tra hai máy Windows với binary tương thích; chơi một ván hoàn chỉnh; thử các tình huống bi 8/phạm lỗi/solo; kiểm tra toàn bộ skin, âm thanh và bảng xếp hạng; gây lỗi database có kiểm soát; thử tải nhiều phòng và lời mời đồng thời. Khi có số liệu mới, ghi rõ phiên bản, môi trường, thao tác, kết quả mong đợi và kết quả thực tế. Không lấy FPS/ping trên ảnh dữ liệu mẫu làm benchmark.

### Kết luận

Báo cáo đáp ứng phân công mới: TV1 có 7 UC và 3 biểu đồ; TV2 có 7 UC và 4 biểu đồ; TV3 có 8 UC và 3 biểu đồ; TV4 có 9 UC và 4 biểu đồ. Phần kiểm thử được đưa thành mục đối chiếu chung, không còn chiếm phần việc TV4. Các diagram Use Case, Activity, Sequence và Component được cung cấp kèm mã nguồn PlantUML để nhóm có thể chỉnh sửa.

Nội dung phản ánh mã hiện tại của dự án và phân biệt triển khai với kiểm chứng. Các đề xuất hoàn thiện được ghi như việc còn làm, không dùng để mô tả sai khả năng đang có của ứng dụng.

## TÀI LIỆU VÀ NGUỒN ĐỐI CHIẾU

[1] Bảng phân công trong ảnh người dùng cung cấp ngày 28/10/2026: TV1 7 UC/3 biểu đồ; TV2 7 UC/4 biểu đồ; TV3 8 UC/3 biểu đồ; TV4 9 UC/4 biểu đồ. Bảng này thay thế cách chia phần công việc trong báo cáo trước.

[2] Mã nguồn ứng dụng: https://github.com/LocMixi2-5/LTM/tree/00bf1ccaf8df2cdd54b79cb75ca188ae9c38627e . Lần lấy main ngày 28/10 không có thay đổi Java sau phiên bản này.

[3] Client: client/src/com/billiards/client/NetworkClient.java; scenes/{SceneManager,ServerConfigDialog,LoginScene,RegisterScene,LobbyScene,PreGameScene,GameScene,SettingsDialog,ResultScene,LeaderboardScene}.java; game/{Ball,PhysicsEngine,CueSkinManager}.java; AvatarManager.java; audio/SoundManager.java.

[4] Server: server/src/com/billiards/server/{ServerMain,ClientHandler,GameRoom,LobbyManager,AuthManager,DatabaseManager}.java; protocol/{Packet,PacketType,CueSkinConfig}.java; database/schema.sql.

[5] Danh mục và đặc tả: reports/uml_spec.json. Ánh xạ 14 biểu đồ: reports/uml_manifest.json. Nguồn hình: reports/uml/*.puml, bản PNG và SVG tương ứng. PlantUML 1.2025.2 dùng để kết xuất UML.

[6] Minh chứng thực thi kế thừa: reports/evidence/00bf1cc/README.md và các nhật ký/script cùng thư mục. Các ảnh chụp UI ngày 26/10 vẫn được giữ để tham khảo, không tính vào 14 biểu đồ UML của bản phân công mới.
'''
(base/'Bao_cao_LTM_4_phan.md').write_text(s)
print('Report written:',len(s.split()),'words; 31 specs; 14 diagram references.')
