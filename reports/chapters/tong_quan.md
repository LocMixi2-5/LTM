# MỞ ĐẦU

## Lý do chọn đề tài

Trò chơi nhiều người qua mạng kết hợp các vấn đề cốt lõi của lập trình mạng: duy trì kết nối, trao đổi dữ liệu có cấu trúc, quản lý phiên, xử lý đồng thời và phản hồi trạng thái trên giao diện. Với bi-a, người chơi còn cần quan sát hướng gậy, lực đánh, chuyển động của bi và quyền đánh của từng bên. Vì vậy, đề tài vừa có nghiệp vụ tài khoản và dữ liệu, vừa có yêu cầu phối hợp trạng thái theo thời gian.

Nhóm lựa chọn xây dựng trò chơi bi-a hai người bằng Java và JavaFX, sử dụng TCP Socket để truyền dữ liệu nghiệp vụ, UDP để tìm máy chủ trong LAN và PostgreSQL để lưu dữ liệu. Sản phẩm tạo điều kiện áp dụng kiến thức môn Lập trình mạng vào một quy trình hoàn chỉnh: từ kết nối, đăng nhập và tìm đối thủ đến thi đấu, cập nhật kết quả và tương tác bạn bè.

## Mục tiêu và sản phẩm của nhóm

Mục tiêu chức năng là cung cấp một ứng dụng có tài khoản người chơi, sảnh, ghép trận hoặc thách đấu, bàn bi-a tương tác, luyện tập solo, bạn bè, chat, bảng xếp hạng và cá nhân hóa. Mục tiêu kỹ thuật là tổ chức rõ trách nhiệm của client, server và cơ sở dữ liệu; xây dựng giao thức nhị phân dùng chung; liên kết thao tác giao diện với xử lý nghiệp vụ và phản hồi từ mạng.

Sản phẩm được trình bày trong báo cáo gồm mã nguồn client/server, schema PostgreSQL, các script biên dịch và đóng gói, 31 đặc tả use case, 14 biểu đồ UML, ảnh giao diện và minh chứng kiểm tra. Báo cáo phân biệt ba mức: chức năng có trong mã, hành vi đã được thực thi kiểm tra và nội dung còn cần nghiệm thu. Việc một use case có đặc tả không tự chứng minh toàn bộ các nhánh đã chạy đúng.

## Đối tượng, phạm vi và phương pháp

Đối tượng sử dụng chính là người chơi; mỗi trận trực tuyến có hai người. Máy chủ quản lý kết nối, sảnh và nhiều đối tượng phòng; PostgreSQL lưu hồ sơ và lịch sử. Trong phạm vi triển khai hiện tại, mô phỏng bi diễn ra tại các client, còn server kiểm tra quyền lượt và chuyển tiếp thông tin cú đánh. Đây chưa phải mô hình server tự mô phỏng và quyết định toàn bộ trạng thái vật lý.

Phạm vi đồ án gồm bốn phân hệ theo bảng phân công của nhóm. Không đưa thanh toán, vật phẩm mua bằng tiền, giải đấu hoặc khán giả vào danh sách tính năng hiện có. Quy tắc phân nhóm bi được mô tả theo mã dự án; không khẳng định toàn bộ luật thi đấu bi-a chuẩn quốc tế đã được triển khai.

Phương pháp thực hiện báo cáo gồm đọc mã nguồn và schema; phân rã use case theo trách nhiệm; mô hình hóa Use Case, Activity, Sequence và Component; đối chiếu với nhật ký kiểm tra; mô tả cài đặt và xây dựng kế hoạch nghiệm thu còn thiếu. Các nhận xét về lỗi được đặt cạnh luồng nghiệp vụ liên quan để tránh nhầm lẫn giữa thiết kế mong muốn và hành vi đang có.

## Yêu cầu chức năng và chất lượng

| Nhóm yêu cầu | Nội dung | Phân hệ chịu trách nhiệm |
| --- | --- | --- |
| Kết nối và phiên | Kết nối TCP, khám phá LAN, trao đổi packet, RTT, ghép trận, lời mời và ngắt kết nối | TV1 |
| Tài khoản và dữ liệu | Đăng ký, đăng nhập, hồ sơ, kết quả, xếp hạng, bạn bè và tin nhắn | TV2 |
| Thi đấu | Khởi tạo bàn, đặt bi, ngắm/lực/xoáy, mô phỏng, luật lượt và solo | TV3 |
| Tương tác | Màn hình, form, sảnh, chat, lời mời, HUD, kết quả và tùy chọn cá nhân | TV4 |
| Tính nhất quán | Hai phía diễn giải cùng một payload; quyền lượt và chuyển trạng thái không mâu thuẫn | Phối hợp TV1–TV3 |
| Khả năng sử dụng | Hiển thị phản hồi rõ, thao tác trên luồng JavaFX đúng quy tắc, có đường xử lý lỗi | Phối hợp cả nhóm |
| Khả năng bảo trì | Mã giao thức, nghiệp vụ, dữ liệu và giao diện có trách nhiệm xác định | Phối hợp cả nhóm |

Các yêu cầu chất lượng là mục tiêu đánh giá, chưa phải cam kết hiệu năng đã đo. Báo cáo không đặt số người dùng đồng thời, FPS tối thiểu hoặc độ trễ tối đa nếu chưa có môi trường và phép đo tương ứng.

## Tổ chức phối hợp và bàn giao

TV1 cung cấp hợp đồng truyền thông; TV2 sử dụng hợp đồng đó để nhận yêu cầu xác thực và dữ liệu; TV3 gửi/nhận thông số gameplay; TV4 hiển thị trạng thái và thu nhận thao tác. Mỗi thay đổi payload cần đồng thời cập nhật phía gửi, phía nhận và đặc tả. Một sửa đổi giao diện có liên quan dữ liệu phải được kiểm tra cả nhánh thành công lẫn phản hồi thất bại từ server.

Báo cáo giữ nhãn TV1–TV4 theo phân công đã thống nhất. Các nhãn thể hiện trách nhiệm trình bày và bàn giao, không được dùng để suy ra số dòng code hoặc tỷ lệ đóng góp cá nhân khi chưa có xác nhận của nhóm. Kiểm thử tích hợp và tổng hợp báo cáo là công việc phối hợp chung.

# CƠ SỞ LÝ THUYẾT VÀ CÔNG NGHỆ

## Mô hình client–server và Socket

Trong mô hình client–server, client chủ động mở kết nối và gửi yêu cầu; server lắng nghe, tạo ngữ cảnh xử lý cho kết nối và trả kết quả. Ở dự án này, ServerMain tiếp nhận socket, ClientHandler xử lý luồng dữ liệu của từng client, LobbyManager quản lý ghép trận và GameRoom quản lý một trận. Cách tổ chức này tách vòng đời kết nối khỏi vòng đời trận đấu: một người đã đăng nhập có thể ở sảnh mà chưa thuộc phòng nào.

TCP cung cấp luồng byte có thứ tự và cơ chế truyền lại ở tầng vận chuyển [7]. Tuy nhiên, một lần ghi không tương ứng một lần đọc. Một packet nghiệp vụ có thể đến qua nhiều lần đọc hoặc nhiều packet có thể nằm liền trong bộ đệm. Do đó, phía nhận cần đọc đủ từng trường theo định dạng chung; không được dùng số byte đang sẵn có như độ dài cố định của toàn bộ thông điệp.

TCP cũng không bảo đảm hai giao diện đang ở cùng trạng thái ứng dụng. Hai lệnh đánh hợp lệ về mặt byte vẫn có thể bị áp dụng lặp nếu ứng dụng không nhận diện lệnh đã xử lý. Tương tự, truyền chính xác thông số cú đánh không tự bảo đảm hai engine kết thúc tại cùng tọa độ. Đây là lý do cần phân biệt độ tin cậy của vận chuyển với tính nhất quán của trò chơi.

UDP phù hợp cho truy vấn khám phá trong LAN vì client có thể phát thông điệp tìm kiếm mà chưa biết địa chỉ server. Dự án dùng UDP 12346 cho BILLIARDS_DISCOVER và phản hồi BILLIARDS_SERVER:12345. Đường khám phá không thay thế kết nối TCP cho tài khoản hoặc thi đấu; khi broadcast không đi qua cấu hình mạng, người chơi vẫn cần nhập IP server.

## Giao thức nhị phân và tuần tự hóa

Giao thức nhị phân biểu diễn dữ liệu bằng các kiểu byte, boolean, int, long và double theo quy ước. Dự án đặt mã loại một byte trước payload; thứ tự trường phụ thuộc PacketType. Số nhiều byte dùng big-endian. Chuỗi gồm độ dài kiểu int rồi dữ liệu UTF-8; bộ đọc giới hạn độ dài để không cấp phát theo giá trị tùy ý [2–4].

Ưu điểm của cách này là payload nhỏ và đọc/ghi trực tiếp bằng Java. Đổi lại, hai đầu phải thống nhất chính xác kiểu, thứ tự và số trường. Nếu server chỉ ghi boolean nhưng client đọc thêm string, các byte kế tiếp có thể bị hiểu sai hoặc quá trình đọc bị chặn. Lỗi SETTINGS_RESPONSE trong phần kiểm thử là ví dụ cụ thể của việc hợp đồng hai đầu chưa khớp.

## Luồng xử lý và JavaFX

Server xử lý nhiều kết nối nên cần quản lý dữ liệu dùng chung như danh sách online, hàng chờ và trạng thái phòng. Đồng bộ hóa một phương thức giúp bảo vệ vùng thao tác cụ thể, nhưng không tự biến chuỗi tác vụ liên quan nhiều đối tượng hoặc nhiều câu SQL thành một giao dịch nguyên tử.

Client có luồng nhận mạng và JavaFX Application Thread. Luồng nhận phân tích packet, còn thay đổi node và các thao tác giao diện phải chuyển về luồng JavaFX, thường qua Platform.runLater [8]. Tác vụ đọc socket không nên chạy trực tiếp trên luồng hiển thị vì có thể chặn thao tác người dùng. AnimationTimer hỗ trợ cập nhật hình theo khung hình; nó không đồng nghĩa mỗi callback cách nhau đúng một khoảng thời gian cố định.

## Nguyên lý mô phỏng bi và làm mượt hiển thị

Mỗi bi được mô hình hóa bằng vị trí, vận tốc, trạng thái hoạt động và tham số xoáy. Engine lần lượt cập nhật chuyển động, tác động ma sát, kiểm tra va chạm bi–bi, phản xạ băng và phát hiện lỗ. Việc chia một lần update thành các bước con giúp giảm khoảng dịch chuyển giữa hai lần kiểm tra va chạm. Tuy nhiên, nó không thay thế bộ bước thời gian cố định hoặc cơ chế hiệu chỉnh trạng thái giữa hai máy.

Các hệ số ma sát và phục hồi trong dự án là tham số của mô hình trò chơi; không được xem là số liệu vật lý thực nghiệm. Hướng gậy nhận qua mạng được nội suy để giảm cảm giác giật khi các mẫu tới cách nhau. Nội suy chỉ làm mượt cách vẽ giữa các trạng thái đã nhận, không xác nhận cú đánh hoặc sửa khác biệt vị trí bi cuối lượt.

## PostgreSQL, JDBC và bảo vệ dữ liệu

Cơ sở dữ liệu quan hệ liên kết hồ sơ, kết quả trận, bạn bè và tin nhắn bằng khóa chính/khóa ngoại. JDBC cung cấp kết nối, PreparedStatement và ResultSet để mã Java thực hiện truy vấn. PreparedStatement tách giá trị đầu vào khỏi cấu trúc câu SQL; các quy tắc nghiệp vụ như quyền thay skin hoặc quyền ghi kết quả vẫn phải được kiểm tra ở ứng dụng.

Một giao dịch dữ liệu cần bảo đảm các bước phụ thuộc nhau cùng thành công hoặc cùng bị hủy. Ví dụ ghi lịch sử và tăng EXP nên được xem xét như một đơn vị nghiệp vụ. Phần hiện trạng nêu rõ recordMatchResult chưa có transaction bao trọn các cập nhật. Index có thể hỗ trợ truy vấn phù hợp nhưng hiệu quả còn phụ thuộc điều kiện truy vấn, dữ liệu và kế hoạch thực thi; báo cáo không gán một độ phức tạp cố định cho mọi truy vấn [9–10].

Mật khẩu trong dự án được băm với SHA-256 và salt ngẫu nhiên. Salt giúp tránh việc các mật khẩu giống nhau luôn cho cùng giá trị lưu, nhưng băm nhanh chưa phải lựa chọn tối ưu để chống dò mật khẩu. TLS cho đường truyền và thuật toán chuyên dụng như Argon2id hoặc bcrypt là hướng hoàn thiện, không được mô tả là tính năng sẵn có.

## Vai trò các biểu đồ UML

Use Case xác định chức năng và tác nhân ở ranh giới phân hệ. Activity thể hiện bước xử lý và nhánh điều kiện. Sequence thể hiện thứ tự tương tác giữa các đối tượng. Component trình bày nhóm thành phần và phụ thuộc. Báo cáo dùng đúng 14 biểu đồ được giao; ảnh chụp giao diện ở chương triển khai là minh họa sản phẩm, không phải biểu đồ UML bổ sung. Nguồn PlantUML được giữ cùng ảnh SVG và PNG để nhóm có thể điều chỉnh [11].

# KIẾN TRÚC VÀ THIẾT KẾ CHUNG

## Các thành phần và đường đi dữ liệu

| Thành phần | Trách nhiệm | Liên kết chính |
| --- | --- | --- |
| Client JavaFX | Điều hướng, form, bàn chơi, mô phỏng và âm thanh | Gửi/nhận qua NetworkClient |
| NetworkClient và protocol | Kết nối, đọc/ghi payload, callback và đo RTT | TCP đến ClientHandler; UDP discovery |
| ServerMain và ClientHandler | Nhận kết nối, quản lý phiên và điều phối yêu cầu | AuthManager, LobbyManager, GameRoom, DatabaseManager |
| LobbyManager và GameRoom | Ghép cặp, quản lý phòng, quyền lượt và kết quả | Hai ClientHandler của trận |
| AuthManager và DatabaseManager | Xác thực, hồ sơ và lưu trữ | PostgreSQL qua JDBC |
| PostgreSQL | players, match_history, friendships, messages | Máy chủ ứng dụng truy cập; client không kết nối DB trực tiếp |

Một thao tác đăng nhập bắt đầu từ LoginScene, được NetworkClient tuần tự hóa, ClientHandler phân phối đến phần xác thực, DatabaseManager truy vấn tài khoản và kết quả được trả lại client. Giao diện chỉ chuyển cảnh khi xử lý phản hồi phù hợp. Với cú đánh, đường đi khác: GameScene vừa gửi thông số vừa mô phỏng tại máy đánh; GameRoom chuyển thông số sang đối thủ và xử lý báo cáo cuối lượt.

## Hợp đồng liên kết bốn phân hệ

| Điểm bàn giao | Bên cung cấp → bên sử dụng | Điều cần thống nhất |
| --- | --- | --- |
| Dữ liệu tài khoản | TV2 → TV4, qua TV1 | Trạng thái thành công/thất bại và các trường hồ sơ |
| Tạo trận | TV1 → TV3 và TV4 | Đối thủ, seed, thông tin trước trận và trạng thái sẵn sàng |
| Ngắm và đánh | TV4/TV3 → TV1 → TV3/TV4 đối thủ | Góc, lực, xoáy, tọa độ bi cái và quyền lượt |
| Kết quả | TV3/TV1 → TV2 → TV4 | Người thắng/thua, EXP và thông báo kết thúc |
| Cá nhân hóa | TV4 → TV2, qua TV1 | Avatar/skin, quyền theo level và phản hồi lưu |
| Bạn bè và chat | TV4 → TV2, qua TV1 | Định danh đối tượng, dữ liệu hiển thị và vòng đời yêu cầu |

Ma trận này là ranh giới phối hợp trong báo cáo. Một lớp có thể tham gia nhiều đường đi; ví dụ GameScene vừa thuộc phần trình bày của TV4 vừa gọi engine của TV3. Vì vậy, trách nhiệm được phân theo chức năng, không áp đặt mỗi lớp chỉ thuộc duy nhất một người.

## Thiết kế dữ liệu và tính toàn vẹn

players là bảng trung tâm, có định danh tài khoản, email, username, mật khẩu băm, EXP, level, thắng/thua và tùy chọn avatar/gậy. match_history tham chiếu hai người thắng/thua. friendships dùng cặp user_id/friend_id làm khóa; messages có sender_id và receiver_id tham chiếu players. Những liên kết này hỗ trợ truy vấn dữ liệu theo người chơi và tránh tham chiếu tới tài khoản không tồn tại khi ràng buộc được thực thi.

Các ràng buộc hiện có chưa thay thế toàn bộ quy tắc nghiệp vụ. Cột status của friendships là varchar, không có CHECK giới hạn tập trạng thái trong schema. UNIQUE của username gốc cũng không tự tạo tính duy nhất theo LOWER(username). Nếu ứng dụng tra cứu không phân biệt hoa/thường thì cần xem xét ràng buộc tương ứng và tình huống hai yêu cầu đồng thời. Đây là điểm cần cải thiện của thiết kế lưu trữ, không phải kết quả một bài thử tải đã hoàn tất.

## Các gói tiêu biểu và trạng thái phiên

| Packet/luồng | Payload hoặc vai trò | Liên hệ |
| --- | --- | --- |
| PING | long timestamp; phản hồi giữ timestamp để tính RTT | TV1, HUD của TV4 |
| CUE_AIM | boolean và 4 double; 34 byte kể cả mã loại | Ngắm, lực tương đối và xoáy |
| CUE_SHOT | 6 double; 49 byte kể cả mã loại | Cú đánh của người có lượt |
| GAME_STATE | Chuyển thông số cú đánh sang đối thủ | Mô phỏng cục bộ của đối thủ |
| PHYSICS_DONE | keepTurn và isFoul | Báo kết thúc mô phỏng để đổi/giữ lượt |
| SETTINGS_UPDATE / RESPONSE | Yêu cầu lưu tùy chọn và trạng thái trả về | TV2, TV4; có sai lệch parser đã ghi nhận |

Giao thức hiện có 47 mã loại. Các trạng thái nghiệp vụ có thể đọc theo chuỗi: đã kết nối, đã xác thực, đang ở sảnh, đang chờ ghép/lời mời, đã vào phòng, chờ sẵn sàng, thi đấu và kết thúc. Đây là cách diễn giải vòng đời để phân tích; mã hiện tại chưa có một máy trạng thái thống nhất bảo vệ mọi chuyển tiếp. Các tình huống READY lặp hoặc phản hồi lời mời sai tên cho thấy cần kiểm tra điều kiện chuyển trạng thái ở server.

## Ranh giới triển khai và kiểm chứng

Server chưa có handshake thương lượng phiên bản, token lượt, sequence number cho cú đánh hoặc snapshot/ACK để chốt toàn bộ bàn. Client gửi mẫu ngắm theo ngưỡng thời gian và mức thay đổi; các đường gửi cưỡng bức có thể vượt quy tắc hạn tần thông thường. Do đó, không suy luận rằng mọi luồng luôn bị giới hạn cứng ở 30 mẫu/giây.

Bốn phần tiếp theo trình bày chi tiết đúng phạm vi của từng TV. Phần triển khai sau đó ghép các chức năng thành quy trình sử dụng chung; phần kiểm thử đối chiếu những gì đã chạy và những gì còn cần kiểm tra trước khi nghiệm thu.
