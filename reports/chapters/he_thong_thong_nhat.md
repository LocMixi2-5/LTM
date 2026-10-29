# CHƯƠNG 1. GIỚI THIỆU ĐỀ TÀI

## 1.1. Bối cảnh và lý do lựa chọn

Trò chơi nhiều người qua mạng là một bài toán tiêu biểu của lập trình mạng: các máy cần trao đổi dữ liệu, duy trì phiên và phản ánh thao tác của nhau trong khi người dùng vẫn tương tác liên tục với giao diện. Với trò chơi bi-a, yêu cầu này thể hiện qua việc tìm đối thủ, quan sát hướng gậy, thực hiện cú đánh và xác định lượt tiếp theo. Trải nghiệm của người chơi phụ thuộc vào sự phối hợp của toàn bộ hệ thống, từ kết nối đến xử lý trận đấu và lưu kết quả.

Xuất phát từ bài toán đó, nhóm xây dựng ứng dụng bi-a hai người bằng Java và JavaFX, sử dụng Socket để truyền thông và PostgreSQL để lưu trữ. Đề tài cho phép vận dụng kiến thức môn Lập trình mạng vào một sản phẩm có quy trình sử dụng hoàn chỉnh, đồng thời làm rõ những khó khăn khi phối hợp trạng thái giữa các máy.

## 1.2. Mục tiêu và phạm vi

Ứng dụng hướng tới việc người chơi có thể tạo tài khoản, đăng nhập, tìm đối thủ, thi đấu, xem kết quả và tiếp tục tương tác trong sảnh. Bên cạnh trận đấu trực tuyến, hệ thống có luyện tập solo, bạn bè, chat, bảng xếp hạng và tùy chỉnh avatar, gậy, âm thanh. Các chức năng này cùng phục vụ một hành trình sử dụng, thay vì tồn tại như những phần mềm độc lập.

Về kỹ thuật, mục tiêu là kết nối được giao diện, truyền thông, xử lý trò chơi và cơ sở dữ liệu trong kiến trúc client–server. Hệ thống cần thống nhất cách diễn giải thông điệp, xác định trạng thái phiên và trả phản hồi phù hợp với thao tác của người dùng. Tài liệu trình bày cách triển khai hiện tại, kết quả kiểm tra và những điểm còn cần hoàn thiện.

Mỗi trận trực tuyến có hai người. Client đảm nhiệm hiển thị và mô phỏng bi; server quản lý kết nối, sảnh, phòng đấu và điều phối lượt; PostgreSQL lưu dữ liệu bền vững. Luật chơi được mô tả theo phiên bản dự án đang có, chưa khẳng định đáp ứng toàn bộ luật thi đấu bi-a chuẩn quốc tế. Thanh toán, giải đấu và chế độ khán giả nằm ngoài phạm vi sản phẩm hiện tại.

## 1.3. Yêu cầu đặt ra

| Nhu cầu sử dụng | Yêu cầu đối với hệ thống |
| --- | --- |
| Vào trò chơi và sử dụng tài khoản | Tìm hoặc nhập địa chỉ server, kết nối, đăng ký và đăng nhập |
| Tìm người chơi cùng | Hiển thị sảnh, ghép trận hoặc gửi lời mời trực tiếp |
| Chơi một trận bi-a | Khởi tạo bàn, nhận thao tác ngắm/đánh, mô phỏng và điều phối lượt |
| Theo dõi kết quả | Hiển thị người thắng/thua, lưu lịch sử và cập nhật chỉ số |
| Duy trì tương tác | Quản lý bạn bè, trao đổi tin nhắn và xem xếp hạng |
| Điều chỉnh trải nghiệm | Chọn avatar/gậy, âm thanh và hiệu ứng; có chế độ solo |

Ngoài chức năng, giao diện cần phản hồi rõ khi kết nối hoặc thao tác thất bại. Các thông điệp phải có định dạng nhất quán giữa hai đầu; dữ liệu dùng chung cần được quản lý khi nhiều phiên cùng hoạt động. Đây là mục tiêu chất lượng của đồ án, không phải cam kết rằng mọi tình huống đã vượt qua kiểm thử. Những giới hạn đã phát hiện được đánh giá ở Chương 6.

## 1.4. Cách tiếp cận và bố cục báo cáo

Nhóm tiếp cận bài toán theo dòng dữ liệu: thao tác bắt đầu ở giao diện, được chuyển thành yêu cầu mạng, xử lý theo trạng thái phiên và trả kết quả về màn hình; các thông tin cần lưu được ghi vào cơ sở dữ liệu. Cách tiếp cận này giúp giải thích đồng thời trải nghiệm của người chơi và vai trò của các thành phần kỹ thuật.

Báo cáo đi từ nền tảng công nghệ đến kiến trúc, sau đó theo dõi một phiên sử dụng từ kết nối tới kết thúc trận. Phần triển khai minh họa sản phẩm bằng ảnh giao diện; phần đánh giá đối chiếu với minh chứng thực thi. Đặc tả chức năng và các biểu đồ UML được đặt ở phụ lục để tra cứu khi cần làm rõ một luồng xử lý cụ thể.

# CHƯƠNG 2. CƠ SỞ LÝ THUYẾT VÀ CÔNG NGHỆ

## 2.1. Mô hình client–server và truyền thông Socket

Trong mô hình client–server, client chủ động kết nối và gửi yêu cầu, còn server lắng nghe, quản lý phiên và trả phản hồi. Dự án sử dụng TCP cho nghiệp vụ cần trao đổi có thứ tự như xác thực, lời mời và thông tin trận đấu. ServerMain tiếp nhận socket, còn ClientHandler xử lý dữ liệu của từng kết nối. Người đã kết nối có thể ở sảnh mà chưa tham gia phòng đấu; vì vậy vòng đời kết nối và vòng đời trận được phân biệt.

TCP cung cấp một luồng byte có thứ tự, không cung cấp ranh giới thông điệp ứng dụng [7]. Một thông điệp có thể tới qua nhiều lần đọc; nhiều thông điệp cũng có thể nằm liền nhau trong bộ đệm. Phía nhận phải đọc đủ các trường theo quy ước thay vì giả định một lần đọc tương ứng một yêu cầu hoàn chỉnh.

Độ tin cậy của TCP không tự bảo đảm trạng thái trò chơi nhất quán. Nếu ứng dụng gửi lại lệnh đánh hoặc áp dụng lệnh vào sai lượt, thông điệp vẫn có thể được truyền chính xác nhưng kết quả nghiệp vụ sai. Tương tự, hai máy nhận cùng thông số cú đánh chưa chắc kết thúc mô phỏng tại cùng tọa độ. Tính nhất quán vì thế cần được bảo vệ ở tầng ứng dụng.

UDP được sử dụng cho khám phá server trong LAN. Client có thể phát truy vấn tìm kiếm trước khi biết địa chỉ máy chủ; sau khi nhận phản hồi, các nghiệp vụ tiếp tục trên TCP. Nếu cấu hình mạng không cho phép broadcast, người chơi cần nhập địa chỉ server trực tiếp.

## 2.2. Giao thức nhị phân

Thông điệp của dự án bắt đầu bằng một byte mã loại, sau đó là các trường dữ liệu theo thứ tự đã định. Các số nhiều byte dùng big-endian; chuỗi gồm độ dài kiểu int và nội dung UTF-8. Cách biểu diễn này gọn và phù hợp với các lớp đọc/ghi dữ liệu của Java, nhưng đòi hỏi phía gửi và phía nhận thống nhất chính xác kiểu và số trường.

Một thay đổi payload phải được cập nhật ở cả hai đầu. Chẳng hạn, nếu server chỉ gửi trạng thái boolean mà client còn chờ một chuỗi, quá trình đọc sẽ không còn khớp với hợp đồng. Đây là dạng lỗi khác với mất gói ở tầng mạng; nó cần được kiểm tra bằng cách đối chiếu bộ ghi và bộ đọc.

## 2.3. JavaFX và xử lý đồng thời

JavaFX cung cấp các thành phần giao diện, Canvas và cơ chế cập nhật theo khung hình. Ứng dụng có luồng nhận mạng riêng để việc chờ dữ liệu không chặn thao tác người dùng. Những thay đổi node và thao tác giao diện phải được chuyển về JavaFX Application Thread, thường thông qua Platform.runLater [8].

Ở server, nhiều kết nối có thể cùng truy cập sảnh, danh sách người chơi và phòng đấu. Các thao tác liên quan trạng thái dùng chung cần có điều kiện kiểm tra và cơ chế bảo vệ phù hợp. Đồng bộ hóa một phương thức chỉ bảo vệ phạm vi thao tác đó; không tự biến nhiều cập nhật ở các đối tượng khác nhau thành một giao dịch thống nhất.

## 2.4. Mô phỏng vật lý và nội suy hiển thị

Bi được biểu diễn bằng vị trí, vận tốc, trạng thái hoạt động và tham số xoáy. Engine cập nhật chuyển động, giảm vận tốc do ma sát, kiểm tra va chạm giữa các bi, phản xạ với băng và phát hiện bi vào lỗ. Chia một lần cập nhật thành các bước con giúp giảm quãng dịch chuyển giữa hai lần kiểm tra va chạm.

Hướng gậy nhận qua mạng được nội suy để hình vẽ chuyển dần tới trạng thái mới. Nội suy giúp giảm cảm giác giật khi các mẫu tới cách nhau, nhưng không thay thế việc kiểm tra quyền đánh hoặc hiệu chỉnh vị trí bi cuối lượt. AnimationTimer cũng không bảo đảm mỗi callback cách nhau đúng một khoảng thời gian cố định; yếu tố này cần được xét khi đánh giá mô phỏng trên hai máy khác nhau.

## 2.5. Lưu trữ bằng PostgreSQL và JDBC

PostgreSQL lưu tài khoản, lịch sử trận, quan hệ bạn bè và tin nhắn. JDBC cung cấp kết nối và cơ chế thực thi truy vấn từ Java. PreparedStatement tách dữ liệu đầu vào khỏi cấu trúc SQL; các điều kiện nghiệp vụ như quyền sử dụng skin hoặc điều kiện ghi kết quả vẫn cần được kiểm tra trong ứng dụng [9–10].

Các cập nhật phụ thuộc nhau nên được xem xét trong cùng giao dịch. Ví dụ, ghi lịch sử và cộng điểm sau trận cần tránh tình trạng chỉ một phần được lưu khi xảy ra lỗi. Khóa chính, khóa ngoại và ràng buộc duy nhất bảo vệ một phần tính toàn vẹn; chúng phải phù hợp với quy tắc tra cứu và xử lý ở ứng dụng.

Mật khẩu hiện được băm bằng SHA-256 cùng salt ngẫu nhiên. Salt giúp các mật khẩu giống nhau không luôn có cùng giá trị lưu, nhưng thuật toán băm nhanh vẫn có giới hạn khi chống dò mật khẩu. TLS và thuật toán chuyên dụng cho mật khẩu là hướng hoàn thiện, chưa phải tính năng đã có trong phiên bản được báo cáo.

# CHƯƠNG 3. KIẾN TRÚC VÀ THIẾT KẾ HỆ THỐNG

## 3.1. Kiến trúc tổng thể

Hệ thống gồm ứng dụng JavaFX, máy chủ Java Socket và PostgreSQL. Trên client, màn hình nhận thao tác, NetworkClient trao đổi dữ liệu và engine mô phỏng bàn chơi. Trên server, yêu cầu được xử lý theo phiên người dùng, trạng thái sảnh và phòng đấu; dữ liệu cần lưu đi qua lớp truy cập cơ sở dữ liệu. Client không kết nối trực tiếp tới PostgreSQL.

| Thành phần | Vai trò trong hệ thống |
| --- | --- |
| ClientMain, SceneManager và các màn hình | Khởi chạy, điều hướng và phản hồi cho người dùng |
| NetworkClient và các lớp protocol | Kết nối, tuần tự hóa, đọc thông điệp và phân phối callback |
| GameScene, Ball và PhysicsEngine | Nhận thao tác trên bàn, biểu diễn bi và mô phỏng cú đánh |
| ServerMain và ClientHandler | Tiếp nhận kết nối, quản lý phiên và điều phối yêu cầu |
| LobbyManager và GameRoom | Ghép cặp, tạo phòng, xác định quyền lượt và kết thúc trận |
| AuthManager và DatabaseManager | Xác thực, truy vấn và cập nhật dữ liệu |
| PostgreSQL | Lưu hồ sơ, kết quả, bạn bè và tin nhắn |

Các thành phần cùng tham gia một nghiệp vụ. Khi đăng nhập, màn hình thu nhận thông tin, kết nối chuyển yêu cầu, server kiểm tra dữ liệu rồi trả hồ sơ để giao diện vào sảnh. Khi đánh bi, GameScene đồng thời làm việc với engine và kết nối mạng. Do đó, đường đi của thao tác là cơ sở để hiểu sự phối hợp của hệ thống.

## 3.2. Thiết kế dữ liệu

players là bảng trung tâm, chứa định danh, email, username, mật khẩu băm, EXP, level, thống kê thắng/thua và tùy chọn cá nhân. Các bảng còn lại tham chiếu tới người chơi để lưu kết quả trận, quan hệ bạn bè và hội thoại.

| Bảng | Dữ liệu chính | Liên kết |
| --- | --- | --- |
| players | Tài khoản, chỉ số và avatar/gậy | Khóa chính id; email và username có UNIQUE |
| match_history | Người thắng/thua, EXP thưởng và thời gian | Hai khóa ngoại tới players |
| friendships | Cặp người dùng và trạng thái quan hệ | Khóa kép user_id/friend_id; tham chiếu players |
| messages | Người gửi/nhận, nội dung, trạng thái đọc và thời gian | Khóa ngoại tới players |

Schema bảo vệ các liên kết cơ bản nhưng chưa bao quát mọi quy tắc nghiệp vụ. Trạng thái bạn bè đang là varchar không có CHECK giới hạn tập giá trị; ràng buộc UNIQUE của username gốc chưa tương đương với duy nhất không phân biệt hoa/thường. Những điểm này cần được đối chiếu với cách tra cứu của ứng dụng khi xử lý yêu cầu đồng thời.

## 3.3. Thiết kế truyền thông và vòng đời phiên

Máy chủ dùng TCP 12345 cho nghiệp vụ và UDP 12346 cho khám phá trong LAN. Giao thức hiện có 47 mã loại. Một số thông điệp quan trọng trong phiên chơi được tóm tắt dưới đây; đặc tả chi tiết được giữ ở tài liệu kỹ thuật đi kèm.

| Thông điệp | Ý nghĩa |
| --- | --- |
| PING | Trao đổi timestamp để đo thời gian khứ hồi |
| CUE_AIM | Cập nhật trạng thái ngắm, góc, lực tương đối và xoáy |
| CUE_SHOT / GAME_STATE | Gửi thông số cú đánh và chuyển sang đối thủ |
| PHYSICS_DONE / TURN_CHANGE | Báo mô phỏng kết thúc và thông báo quyền lượt tiếp theo |
| SETTINGS_UPDATE / SETTINGS_RESPONSE | Yêu cầu lưu tùy chọn và phản hồi trạng thái |

Một phiên có thể diễn tiến từ kết nối, xác thực và vào sảnh tới chờ đối thủ, vào phòng, sẵn sàng, thi đấu và kết thúc. Server cần quyết định thông điệp nào được chấp nhận ở từng thời điểm. Phiên bản hiện tại chưa có một máy trạng thái thống nhất bảo vệ mọi chuyển tiếp; các trường hợp lệnh lặp và phản hồi muộn vẫn là điểm cần hoàn thiện.

## 3.4. Tổ chức giao diện và tài nguyên

SceneManager điều hướng giữa các màn hình tài khoản, sảnh, trước trận, bàn chơi và kết quả. GameScene kết hợp Canvas với phần thông tin người chơi, lượt và trạng thái. SettingsDialog, AvatarManager, CueSkinManager và SoundManager cung cấp lựa chọn cá nhân và phản hồi nghe/nhìn.

Hồ sơ avatar/gậy cần đi qua server để kiểm tra quyền và lưu dữ liệu. Ngược lại, các mức âm lượng và mute áp dụng tại máy người chơi. Sự khác biệt này quyết định khi nào giao diện có thể cập nhật ngay và khi nào cần xác nhận từ hệ thống lưu trữ. Sơ đồ thành phần ở Phụ lục B thể hiện các phụ thuộc giữa những lớp đang triển khai.

# CHƯƠNG 4. QUY TRÌNH HOẠT ĐỘNG CỦA ỨNG DỤNG

## 4.1. Từ khởi động đến tài khoản người chơi

Khi mở ứng dụng, người chơi chọn server hoặc sử dụng địa chỉ đã lưu. Nếu cùng LAN, client có thể gửi truy vấn khám phá để tìm máy chủ. Khi đã xác định địa chỉ, client mở TCP socket, tạo luồng đọc dữ liệu và bắt đầu trao đổi PING. Kết nối thành công tạo đường truyền cho nghiệp vụ; người chơi vẫn cần xác thực để sử dụng tài khoản.

Người chưa có tài khoản nhập thông tin ở màn hình đăng ký. Giao diện kiểm tra đầu vào và gửi yêu cầu; server kiểm tra điều kiện, băm mật khẩu và lưu tài khoản nếu hợp lệ. Với tài khoản đã có, server tìm theo username hoặc email, đối chiếu mật khẩu rồi trả trạng thái và hồ sơ. Giao diện dựa trên phản hồi để vào sảnh hoặc hiển thị lỗi.

Như vậy, trải nghiệm đăng nhập là một luồng liên tục giữa màn hình, kết nối và dữ liệu. Kiểm tra tại client giúp người dùng sửa lỗi nhập nhanh, còn server vẫn phải chịu trách nhiệm kiểm tra yêu cầu nhận được. Hiện quy tắc email ở hai đầu chưa hoàn toàn thống nhất; giới hạn này được ghi trong phần đánh giá.

## 4.2. Tương tác trong sảnh và tìm đối thủ

Sau đăng nhập, sảnh là nơi người chơi xem thông tin, tìm bạn, mở hội thoại hoặc chọn cách vào trận. Danh sách bạn bè và tin nhắn cần kết hợp dữ liệu lưu trong DB với trạng thái phiên đang online. Giao diện nhận dữ liệu qua callback và cập nhật trên luồng JavaFX.

Khi tìm trận ngẫu nhiên, server đưa người chơi vào hàng chờ và ghép với đối tượng phù hợp. Với thách đấu trực tiếp, server kiểm tra người nhận, trạng thái bận và tính hợp lệ của lời mời trước khi chuyển tới client đối phương. Người nhận thấy popup, chọn chấp nhận hoặc từ chối; nếu không phản hồi, lời mời có thể hết hạn.

Khi lời mời được chấp nhận và điều kiện vẫn còn hợp lệ, server loại người chơi liên quan khỏi hàng chờ, tạo phòng và thông báo cho hai client. Giao diện chuyển tới bước trước trận. Nếu đối tác rời hoặc yêu cầu không còn hợp lệ, kết quả thất bại cần được phản ánh để người chơi quay lại hoạt động trong sảnh.

Quá trình này cho thấy một thay đổi ở kết nối ảnh hưởng trực tiếp tới trải nghiệm màn hình. Để tránh trạng thái bận kéo dài, timer, người mời và kết quả phản hồi phải được xử lý nhất quán. Phiên bản đang báo cáo còn lỗi ở nhánh phản hồi sai tên, nên phần kiểm thử không coi mọi trường hợp thách đấu đã hoàn tất nghiệm thu.

## 4.3. Khởi tạo bàn và bắt đầu trận

Sau khi tạo phòng, hai client nhận thông tin trận và đối thủ. Dữ liệu seed hỗ trợ khởi tạo cách sắp xếp/phân nhóm bi. Mã hiện tại xáo 14 bi mục tiêu khác bi 8 rồi chia thành hai nhóm bảy bi; đây là quy tắc của dự án, không nên hiểu là luôn chia trơn/sọc theo luật chuẩn.

Màn hình trước trận thu nhận trạng thái sẵn sàng. Khi trận bắt đầu, bàn hiển thị người chơi, bi mục tiêu và quyền lượt. Các thao tác đặt bi cái, ngắm hoặc đánh chỉ nên được áp dụng khi trạng thái cho phép. Tuy nhiên, bộ đếm READY hiện chưa phân biệt đầy đủ việc một người gửi lặp; trường hợp này cần sửa để bảo đảm điều kiện bắt đầu thực sự đến từ hai phía.

## 4.4. Ngắm gậy, chọn lực và thực hiện cú đánh

Ở lượt của mình, người chơi điều chỉnh hướng gậy, kéo lực và chọn điểm chạm trên bi cái. Giao diện hiển thị các lựa chọn đó, đồng thời client gửi mẫu ngắm để đối thủ quan sát. Client đối phương nội suy hướng và lực đã nhận nhằm làm chuyển động hiển thị liên tục hơn giữa các mẫu.

Các mẫu ngắm được gửi theo ngưỡng thời gian và mức thay đổi, giúp giảm việc gửi thông tin gần như không đổi. Một số đường gửi cưỡng bức có thể bỏ qua ngưỡng thông thường; vì vậy không mô tả đây là một giới hạn tần số cứng trong mọi trường hợp. Độ mượt thực tế còn phụ thuộc thời điểm nhận mẫu và tốc độ vẽ ở máy đối thủ.

Khi thả gậy với điều kiện hợp lệ, máy đánh gửi thông số cú đánh và áp dụng mô phỏng ngay tại chỗ. Server kiểm tra quyền lượt rồi chuyển thông số tới đối thủ. Máy đối thủ đưa cú đánh vào quá trình mô phỏng của mình. Cách làm này phản hồi nhanh cho người thao tác nhưng chưa có bước xác nhận rằng hai máy đã bắt đầu từ một trạng thái bàn hoàn toàn giống nhau.

## 4.5. Mô phỏng, chuyển lượt và kết thúc trận

Trong khi bi chuyển động, PhysicsEngine cập nhật vị trí, ma sát, va chạm và kiểm tra lỗ. Phiên bản hiện tại chia mỗi update thành 10 bước con, với các hệ số ma sát và phục hồi phục vụ mô hình trò chơi. Đây không phải bộ tham số thực nghiệm bảo đảm mô phỏng vật lý chính xác tuyệt đối.

Sau khi các bi dừng, client đánh giá bi vào lỗ, lỗi và điều kiện kết thúc. Nếu trận tiếp tục, thông báo PHYSICS_DONE cho biết khả năng giữ lượt và trạng thái phạm lỗi; server xử lý rồi phát TURN_CHANGE. Khi có kết quả theo logic hiện tại, server xử lý kết thúc và cập nhật dữ liệu người chơi. Màn hình kết quả là phần thể hiện cuối cùng của chuỗi xử lý này.

Điểm quan trọng là server hiện chưa tự mô phỏng va chạm để kiểm chứng toàn bộ bàn. Hai client cũng chưa chốt snapshot vị trí 16 bi kèm xác nhận. Vì vậy, chuyển lượt thành công chỉ chứng minh bước điều phối đã diễn ra, chưa đủ chứng minh hai bàn luôn hội tụ. Đánh giá tính nhất quán cần quan sát cả trạng thái cuối cú đánh, không chỉ gói đổi lượt.

Kết quả trận liên quan đồng thời lịch sử, điểm kinh nghiệm và thống kê thắng/thua. Mã hiện tại thưởng 100 EXP cho người thắng, level được tính bằng floor(EXP/200)+1. Các bước ghi này chưa được bao trọn trong một transaction, nên cần hoàn thiện để tránh kết quả lưu một phần nếu lỗi phát sinh giữa chừng.

## 4.6. Luyện tập và cá nhân hóa trải nghiệm

Ở chế độ solo, người chơi sử dụng bàn và engine cục bộ mà không cần đối thủ. Giao diện ẩn khung đối thủ và loại phần đó khỏi bố cục. Chế độ này cho phép làm quen với ngắm, lực và xoáy, nhưng không thay thế thử nghiệm tương tác giữa hai client qua mạng.

Avatar và skin gậy được chọn trong SettingsDialog. Server kiểm tra điều kiện level trước khi lưu lựa chọn vào hồ sơ; các tùy chọn âm thanh được SoundManager áp dụng cục bộ và lưu bằng Preferences. VFX được điều khiển bằng cờ tại client. Như vậy, cá nhân hóa kết hợp phần dữ liệu tài khoản và phần trải nghiệm riêng trên mỗi máy.

Giao diện hiện cập nhật lựa chọn và đóng hộp thoại trước khi có xác nhận lưu hoàn chỉnh. Cùng với sai lệch định dạng SETTINGS_RESPONSE, điều này cho thấy cần phân biệt việc đã nhìn thấy thay đổi với việc đã lưu thành công. Sau khi hoàn thiện luồng phản hồi, hệ thống cần kiểm tra lại khả năng giữ tùy chọn qua lần đăng nhập tiếp theo.

## 4.7. Sự liên kết của toàn bộ quy trình

Từ kết nối đến kết thúc trận, cùng một thao tác thường đi qua nhiều thành phần. Hướng gậy là ví dụ rõ: thao tác chuột trở thành dữ liệu ngắm, được truyền tới đối thủ và chuyển thành hình vẽ; cú đánh tiếp theo sử dụng các thông số đó để mô phỏng và tạo kết quả lượt. Tính đúng đắn của từng bước chỉ có ý nghĩa khi chúng phối hợp đúng trong toàn bộ phiên chơi.

Phần triển khai dưới đây minh họa quy trình bằng môi trường chạy và các màn hình thực tế. Phần đánh giá sau đó kiểm tra lại các mắt xích đã hoạt động, nêu lỗi còn tồn tại và xác định phạm vi nghiệm thu tiếp theo.
