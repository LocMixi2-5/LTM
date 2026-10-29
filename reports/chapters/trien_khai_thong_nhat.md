# CHƯƠNG 5. TRIỂN KHAI VÀ MINH HỌA SẢN PHẨM

## 5.1. Môi trường và cấu trúc bàn giao

| Hạng mục | Cấu hình được mã/script sử dụng | Ghi chú triển khai |
| --- | --- | --- |
| Java | Các script Windows trỏ JDK 17 | Máy build cần javac, jar và jpackage khi đóng gói EXE |
| JavaFX | SDK Windows x64 17.0.13 theo đường dẫn script | Đặt tại client/lib/javafx-sdk-17.0.13; có cả JAR và DLL |
| PostgreSQL | Database billiards_db, JDBC ở localhost:5432 theo mã mặc định | Tạo schema trước khi dùng tài khoản và dữ liệu |
| JDBC driver | server/lib/postgresql-42.7.3.jar | Phải có trong classpath của server |
| Mạng ứng dụng | TCP 12345; UDP discovery 12346 | Hai client phải truy cập được địa chỉ server |
| Đóng gói một EXE | jpackage, tar.exe và csc.exe theo script | Tạo trên Windows; chưa được nghiệm thu trong môi trường báo cáo |

Thư mục client chứa giao diện, engine, protocol, assets và script build; server chứa phần kết nối, nghiệp vụ, dữ liệu và protocol; database chứa schema; reports chứa tài liệu và minh chứng. Các thư mục out/dist, SDK lớn và file EXE bị loại khỏi Git theo .gitignore hiện tại. Vì vậy, tải mã nguồn ZIP từ GitHub không đồng nghĩa đã tải được một bản EXE mới.

## 5.2. Chuẩn bị database và server

Với một môi trường phát triển mới, cài PostgreSQL và chạy schema bằng psql từ thư mục gốc dự án:

```text
psql -U postgres -f database/schema.sql
```

Script có CREATE DATABASE và lệnh riêng của psql là `\c`. Nếu database đã tồn tại, cần chọn đúng database và thực hiện phần tạo bảng phù hợp; không xóa dữ liệu đang dùng để lặp lại bước khởi tạo. Nếu dùng pgAdmin, tạo/chọn database trước và chạy phần SQL tạo bảng, không dán nguyên lệnh `\c` vào Query Tool.

Kiểm tra DB_URL, DB_USER và DB_PASSWORD trong DatabaseManager.java để khớp cấu hình cục bộ, sau đó biên dịch lại server nếu đã đổi giá trị. Phiên bản này đặt cấu hình trong mã; báo cáo không giả định có hỗ trợ biến môi trường hoặc tệp cấu hình ngoài. Cần có thông báo kết nối PostgreSQL thành công; việc tiến trình server vẫn chạy không có nghĩa đăng ký hay lưu dữ liệu sẽ hoạt động khi DB lỗi.

Mở Command Prompt tại thư mục server, chạy `compile.bat`, sau đó `run_server.bat`. Các script này dùng thư mục làm việc hiện tại và trỏ JDK tại `C:\Program Files\Java\jdk-17`. Người triển khai cần điều chỉnh đường dẫn nếu JDK nằm ở nơi khác. Giữ cửa sổ server để theo dõi lỗi trong quá trình thử nghiệm.

## 5.3. Biên dịch và đóng gói client EXE

Máy phát triển cần đặt JavaFX Windows SDK đúng thư mục mà script yêu cầu. Từ thư mục client, có thể chạy `compile.bat` rồi `run_client.bat` để chẩn đoán trước khi đóng gói. Script chạy ưu tiên BilliardsClient.jar nếu file này tồn tại, nên phải build lại JAR khi cần kiểm tra mã mới; chỉ thay file .java không cập nhật binary đã có.

Đối với người dùng cuối ưu tiên EXE, quy trình đóng gói có sẵn trong mã là:

1. Chạy `client\build_single_exe.bat` trên Windows có các công cụ yêu cầu.
2. Script gọi make_exe.bat, biên dịch client, dùng jpackage tạo app-image có runtime, sao chép JavaFX DLL và assets rồi tạo ZIP.
3. Trình biên dịch C# nhúng ZIP vào launcher, tạo `client\dist\8BallPool_Game.exe` và sao chép ra thư mục gốc.
4. Chuyển EXE được tạo thành công sang máy kiểm tra sạch, chạy và xác minh kết nối, âm thanh, tài nguyên và phiên bản nội dung.

Launcher giải nén dữ liệu vào `%LOCALAPPDATA%\BilliardsClubGame` rồi khởi chạy BilliardsClient.exe trong đó. Đường app-image cũng có thể dùng trực tiếp, nhưng cần giữ nguyên thư mục runtime, app và assets cùng launcher. Các bước trên là hướng dẫn từ script hiện có; lần viết báo cáo này không tạo hay phát hành EXE và chưa xác minh gói trên Windows sạch.

## 5.4. Quy trình sử dụng chung của hai người chơi

Hai máy mở cùng phiên bản client và chọn server. Khi thử hai client trên cùng một máy, có thể dùng localhost; khi thử hai máy khác nhau, client phải nhập IP LAN của máy chạy server. localhost ở máy thứ hai sẽ trỏ về chính máy đó. Nếu khám phá tự động không thấy server, kiểm tra IP, kết nối mạng và quyền truy cập cổng trước khi kết luận lỗi giao diện.

Người chơi đăng ký tài khoản, đăng nhập bằng tên hoặc email rồi vào sảnh. Tại sảnh có thể tìm bạn, gửi lời mời kết bạn, chat, tìm trận hoặc thách đấu trực tiếp. Khi người nhận chấp nhận và server tạo phòng, hai bên chuyển sang phần trước trận và thao tác sẵn sàng. Chỉ một người có lượt đánh; người còn lại quan sát.

Trên bàn chơi, người có lượt ngắm gậy, kéo lực và thả để đánh khi điều kiện cho phép. Bộ chọn điểm chạm trên bi cái điều chỉnh xoáy; chuột phải hoặc nhấp đúp trên vùng chọn có đường xử lý đặt lại tâm. Khi có quyền đặt bi cái, chọn vị trí theo giới hạn bàn. Sau cú đánh, chờ bi dừng và phản hồi chuyển lượt. Luyện tập solo dùng mô phỏng cục bộ, không cần đối thủ.

Avatar và skin được chỉnh qua SettingsDialog; skin còn phụ thuộc level được server kiểm tra. BGM, SFX và mute là tùy chọn cục bộ. Do lỗi parser SETTINGS_RESPONSE còn tồn tại, thao tác hiển thị thay đổi ngay tại client chưa đủ để kết luận hồ sơ đã được lưu và phản hồi hoàn tất; cần xác minh lại ở DB hoặc phiên đăng nhập sau trong đợt nghiệm thu.

## 5.5. Minh họa giao diện sản phẩm

Bảy hình dưới đây là ảnh JavaFX thực từ mã 00bf1cc, chụp ngày 26/10/2026 trên Linux/Xvfb. Tài khoản và nội dung là dữ liệu mẫu, transport dùng fixture để điều khiển callback. Chúng minh họa bố cục và phản hồi giao diện; không phải ảnh hai máy Windows thi đấu đầu cuối. Chỉ số ping/FPS trong ảnh không được dùng làm kết quả benchmark. Các ảnh được đánh số theo chương; sơ đồ kỹ thuật được tập hợp tại Phụ lục B.

### Minh họa 5.1. Form đăng ký

Form nhận thông tin tài khoản và phản hồi kiểm tra đầu vào. Kiểm tra client hỗ trợ trải nghiệm nhập liệu nhưng không thay thế quy tắc xác thực đầu vào ở server.

![Hình 5.1 — Form đăng ký JavaFX; dữ liệu mẫu và transport giả lập, ngày 26/10/2026.](images/registration_actual.png)

### Minh họa 5.2. Sảnh và menu bạn bè

Menu bạn bè cung cấp ngữ cảnh thao tác trong sảnh. Kịch bản kiểm tra có xác nhận menu được giữ khi refresh và cũng ghi nhận tình huống áp dụng dữ liệu chờ đã cũ.

![Hình 5.2 — Menu bạn bè trên giao diện thật; fixture dữ liệu, ngày 26/10/2026.](images/friend_menu_actual.png)

### Minh họa 5.3. Hội thoại chat

Giao diện hội thoại biểu diễn lịch sử và tin nhắn. Ảnh thể hiện cách trình bày, không chứng minh việc nhận lịch sử bất đồng bộ đã đúng cho mọi lần chuyển bạn chat.

![Hình 5.3 — Hội thoại với nội dung mẫu trên JavaFX, ngày 26/10/2026.](images/chat_actual.png)

### Minh họa 5.4. Lời mời thách đấu

Popup lời mời nằm trên cửa sổ chủ và có bộ đếm. Server và client có mốc timeout khác nhau, lần lượt 15 và 10 giây trong mã đối chiếu.

![Hình 5.4 — Popup thách đấu trên cửa sổ chủ; callback giả lập, ngày 26/10/2026.](images/challenge_actual.png)

### Minh họa 5.5. Phản hồi bằng toast

Kết quả lời mời dùng thông báo toast, giúp hiển thị phản hồi mà không mở thêm Alert chặn giao diện. Thông báo giúp người chơi biết kết quả và tiếp tục thao tác trong sảnh.

![Hình 5.5 — Toast kết quả lời mời trên JavaFX, ngày 26/10/2026.](images/challenge_toast.png)

### Minh họa 5.6. Chế độ luyện tập

Solo ẩn và loại khung đối thủ khỏi bố cục. Ảnh minh họa việc loại khoảng trống; chưa thay cho nghiệm thu toàn bộ quy tắc vật lý và kết thúc bài luyện tập.

![Hình 5.6 — Bàn luyện tập solo, không có khung đối thủ trống, ngày 26/10/2026.](images/practice_actual.png)

### Minh họa 5.7. Bàn chơi và hiển thị hướng gậy

GameScene tiếp nhận mẫu CUE_AIM từ callback có kiểm soát và hiển thị trạng thái tương ứng. Để nghiệm thu ngắm liên tục, vẫn cần hai client thật với mạng có độ trễ và thay đổi lượt, không chỉ callback đơn lẻ.

![Hình 5.7 — GameScene nhận mẫu ngắm qua fixture; không phải phép đo đồng bộ hai máy, ngày 26/10/2026.](images/game_actual.png)

## 5.6. Các sự cố triển khai thường gặp

| Hiện tượng | Nội dung cần kiểm tra | Kết quả cần có trước khi tiếp tục |
| --- | --- | --- |
| Windows hỏi ứng dụng để mở JAR | Người dùng đang mở file .jar, chưa chạy bản EXE đã đóng gói | Dùng EXE từ bước build thành công hoặc script Java để chẩn đoán |
| Tải GitHub nhưng không thấy EXE mới | .gitignore loại *.exe và dist; source ZIP không phải gói phát hành | Có gói phát hành riêng hoặc build trên Windows |
| Không tìm thấy JavaFX/JVM | SDK, kiến trúc x64, runtime và DLL đi kèm | App mở trên máy kiểm tra với đủ tài nguyên |
| Không kết nối được server | Server đang chạy, IP đúng, TCP 12345 truy cập được | Có kết nối trước khi thử đăng nhập |
| Đăng ký/đăng nhập lỗi | Database, JDBC classpath, cấu hình và bảng dữ liệu | Server kết nối DB và có phản hồi xác thực |
| Mã mới nhưng hành vi cũ | JAR/EXE chưa được build lại hoặc đang chạy gói khác | Ghi nhận phiên bản binary đang thử |

# CHƯƠNG 6. KIỂM THỬ VÀ ĐÁNH GIÁ

## 6.1. Phương pháp và môi trường kiểm chứng

Bộ minh chứng được lưu tại reports/evidence/00bf1cc, thực thi ngày 26/10/2026 trên môi trường Linux. Biên dịch dùng JDK 21 với --release 17, gồm 9 file Java server và 26 file Java client; cả hai thành công. Kiểm tra giao thức dùng server thực và PostgreSQL phát triển cục bộ. Kiểm tra giao diện dùng các lớp JavaFX thật, Xvfb và transport giả lập.

Bản báo cáo ngày 29/10/2026 kế thừa các log này theo đúng phiên bản và phạm vi. Các lần biên tập tài liệu không được tính là lần kiểm thử ứng dụng mới. Mã thoát 0 của script mô tả hiện trạng chỉ có nghĩa các assertion đã hoàn tất; một assertion có thể cố ý xác nhận việc tái hiện lỗi.

Lần chạy giao thức đầu tiên dừng ở tình huống phản hồi sai tên vì chưa bảo đảm timer đã được tạo. Lần sau thêm khoảng chờ 250 ms và tái hiện trạng thái bận còn lưu. Cả log lần đầu và lần hoàn tất đều được giữ; không gộp chúng thành một lần chạy hoàn hảo.

## 6.2. Kết quả thực thi đã có

| Mã | Trường hợp kiểm tra | Quan sát thực tế | Đánh giá |
| --- | --- | --- | --- |
| KT01 | Biên dịch client/server | 9 file server, 26 file client biên dịch thành công | Đạt bước build trong môi trường đã nêu |
| KT02 | Đăng ký và đăng nhập | Tài khoản tạm đăng ký và đăng nhập tên khác hoa/thường | Đạt luồng đã thử |
| KT03 | Lời mời không hợp lệ | Từ chối tên rỗng, tự mời và offline | Đạt các trường hợp đã thử |
| KT04 | Chấp nhận/từ chối lời mời | Tra tên trim/khác hoa thường, tạo phòng, loại người mời khỏi hàng chờ | Đạt luồng đã thử |
| KT05 | Ngắm liên tục | 60 mẫu 34 byte được chuyển cho đối thủ, không echo về người gửi | Đạt relay; chưa kết luận độ mượt hai UI |
| KT06 | Kết thúc mô phỏng | PHYSICS_DONE chuyển lượt | Đạt chuyển lượt; không có chốt snapshot |
| KT07 | Hết hạn/mất kết nối | Hết hạn khoảng 15 giây; phản hồi thất bại khi đối tác rời | Đạt các nhánh được kích hoạt |
| KT08 | Form và refresh | Callback chạy đúng luồng ở các trường hợp đã thử | Có hành vi đúng, còn lỗi riêng bên dưới |
| KT09 | Popup/toast | Popup trên owner, đóng theo vòng đời; kết quả dùng toast | Đạt trong fixture JavaFX |
| KT10 | Solo và nhận mẫu ngắm | Không còn khung đối thủ trống; đọc mẫu ngắm hiện hành | Đạt kiểm tra UI có kiểm soát |

## 6.3. Lỗi và giới hạn cần hoàn thiện

| Mã | Hiện tượng | Tác động | Hướng hoàn thiện |
| --- | --- | --- | --- |
| L01 | Hai READY từ cùng người có thể mở trận | Trận bắt đầu trước khi đối thủ thực sự sẵn sàng | Ghi nhận trạng thái sẵn sàng riêng cho từng người |
| L02 | Góc NaN được relay; cú đánh lặp tiếp tục được chuyển | Trạng thái bất hợp lệ hoặc áp dụng cú đánh nhiều lần | Kiểm tra tham số và nhận diện lệnh đã xử lý |
| L03 | Phản hồi sai tên hủy timer nhưng còn currentChallenger | Lời mời có thể giữ trạng thái bận quá hạn | Chỉ kết thúc timer khi phản hồi phù hợp với lời mời |
| L04 | SETTINGS_RESPONSE server chỉ có boolean, client đọc thêm string | Luồng đọc không khớp; có thể chờ hoặc lỗi | Thống nhất payload giữa bộ ghi và bộ đọc |
| L05 | Email sai định dạng được server nhận; UI từ chối TLD dài hợp lệ | Quy tắc hai đầu không thống nhất | Dùng quy tắc kiểm tra nhất quán ở hai đầu |
| L06 | Hai tin giống nhau trong cùng phút bị gộp; lịch sử cũ vào cuộc chat mới | Mất biểu diễn tin hoặc sai hội thoại | Gắn định danh tin và hội thoại vào yêu cầu/phản hồi |
| L07 | Refresh A→B→A khi menu mở áp dụng B lúc đóng | Danh sách hiển thị dữ liệu cũ | Áp dụng bản cập nhật mới nhất khi đóng menu |

Các dòng trên là hiện tượng đã ghi trong log. Ngoài ra, đọc mã cho thấy ghi kết quả chưa có transaction toàn vẹn, DB dùng Connection chung, mô phỏng chưa có bước thời gian cố định và chưa có snapshot/ACK hai phía. Những nhận xét từ mã cần được tách khỏi khẳng định đã gây lỗi ở mọi điều kiện thực tế.

## 6.4. Kế hoạch nghiệm thu tích hợp còn lại

| Mã | Kịch bản cần chạy | Tiêu chí quan sát mong đợi | Trạng thái |
| --- | --- | --- | --- |
| NT01 | Hai máy Windows dùng EXE cùng bản | Mở được, đủ tài nguyên, kết nối và đăng nhập thành công | Chưa thực thi trong bộ minh chứng |
| NT02 | Ngắm liên tục, đổi góc qua biên ±π, kéo lực và chỉnh xoáy qua nhiều lượt | Hướng gậy đúng người có lượt; không kẹt hình ngắm sau đánh hoặc đổi lượt | Chưa nghiệm thu hai UI qua mạng |
| NT03 | Chơi trọn ván với phạm lỗi, hết giờ, bi cái và bi 8 vào lỗ | Quyền lượt, kết quả và dữ liệu sau trận thống nhất với luật công bố | Chưa có ván đầy đủ trong bộ minh chứng |
| NT04 | Thêm độ trễ/jitter, ngắt mạng giữa ngắm/đánh/lời mời | Có phản hồi và dọn phiên; không áp dụng lệnh của trạng thái cũ | Cần chạy sau khi sửa lỗi trạng thái |
| NT05 | Gây lỗi DB giữa ghi kết quả, đăng ký đồng thời | Không ghi kết quả một phần; định danh tài khoản nhất quán | Chưa kiểm thử giao dịch/đồng thời đầy đủ |
| NT06 | Lưu và đăng nhập lại với avatar/skin; thử âm thanh/mute | Quyền theo level đúng; tùy chọn tồn tại theo thiết kế | Chưa nghiệm thu toàn bộ tùy chọn |
| NT07 | Nhiều phòng, lời mời chéo và chuyển chat nhanh | Phòng độc lập; phản hồi thuộc đúng đối tượng; đo tải có cấu hình rõ | Chưa có stress test/benchmark |

Mỗi lần chạy cần lưu phiên bản mã và binary, hệ điều hành, cấu hình mạng, dữ liệu đầu vào, bước thao tác, kết quả mong đợi và thực tế. Với NT02–NT04, nên ghi cả thời điểm gửi/nhận và ảnh hai phía để phân biệt chậm hiển thị với sai trạng thái. Các tiêu chí này là kế hoạch kiểm tra, không được ghi là đã đạt chỉ vì tài liệu hoặc sơ đồ đã hoàn thành.

## 6.5. Đánh giá kết quả của nhóm

Dự án đã liên kết được các nhóm chức năng từ kết nối, tài khoản và sảnh đến bàn chơi, kết quả và cá nhân hóa trong một kiến trúc client–server. Các kiểm tra đã có cung cấp bằng chứng cho việc biên dịch, nhiều nhánh thách đấu, relay mẫu ngắm và một số phản hồi JavaFX. Các lỗi còn lại cho thấy chất lượng cần được đánh giá trên toàn bộ đường đi của một thao tác, từ yêu cầu mạng đến dữ liệu và phản hồi giao diện.

Mức hoàn thiện còn giới hạn ở tính nhất quán trong các tình huống lặp, muộn hoặc sai trạng thái và ở phạm vi kiểm chứng trên Windows. Vì vậy, kết quả hiện tại phù hợp để trình bày sản phẩm đồ án cùng phân tích kỹ thuật và hướng hoàn thiện; chưa đủ cơ sở tuyên bố không còn lỗi bất đồng bộ hoặc bảo đảm hiệu năng trong mọi điều kiện mạng.

# CHƯƠNG 7. KẾT LUẬN VÀ HƯỚNG PHÁT TRIỂN

## 7.1. Kết luận

Đồ án xây dựng ứng dụng bi-a hai người qua mạng bằng Java/JavaFX, Socket và PostgreSQL, kết hợp nghiệp vụ tài khoản, tương tác xã hội, thi đấu và cá nhân hóa. Báo cáo tổng hợp toàn bộ phạm vi của nhóm, từ mục tiêu, kiến thức nền và kiến trúc đến đặc tả, triển khai, minh họa sản phẩm và đánh giá.

Các chức năng tài khoản, sảnh, truyền thông, mô phỏng và hiển thị đã được kết nối thành một quy trình sử dụng chung. Giá trị của sản phẩm nằm ở sự phối hợp đó: người chơi có thể đi từ việc xác thực tới tìm đối thủ, thao tác trên bàn và xem kết quả trong cùng ứng dụng. Báo cáo mô tả quy trình này cùng các giới hạn hiện tại để làm cơ sở đánh giá và tiếp tục phát triển.

Bài học kỹ thuật chính là giao thức cần được kiểm chứng đồng thời ở hai đầu, thao tác UI cần tuân thủ luồng JavaFX, và vận chuyển tin cậy chưa đủ để bảo đảm trạng thái trò chơi nhất quán. Các lỗi được ghi lại cung cấp đầu việc cụ thể cho giai đoạn hoàn thiện, thay vì suy luận chất lượng chỉ từ giao diện hoặc việc biên dịch thành công.

## 7.2. Hướng phát triển theo mức ưu tiên

Ưu tiên trước hết là sửa những sai lệch đã tái hiện: thống nhất SETTINGS_RESPONSE; chống READY và cú đánh lặp; kiểm tra giá trị hữu hạn/phạm vi tham số; bảo vệ vòng đời lời mời; gắn phản hồi chat với đúng hội thoại và thống nhất kiểm tra email. Sau mỗi thay đổi cần tái lập trường hợp lỗi rồi chạy lại luồng bình thường liên quan.

Bước tiếp theo là củng cố trạng thái trận bằng mã trận/lượt, thứ tự lệnh và quy tắc chấp nhận thông điệp. Có thể lựa chọn server mô phỏng có thẩm quyền hoặc kết hợp bước thời gian cố định ở client với cơ chế chốt/hiệu chỉnh bàn. Thiết kế được chọn cần được đánh giá bằng hai client thật, đo sai lệch vị trí và kiểm tra mất kết nối, không chỉ bổ sung tên packet vào tài liệu.

Về dữ liệu, cần transaction cho kết quả trận, quản lý kết nối phù hợp tải đồng thời và ràng buộc định danh nhất quán với cách tra cứu. Về triển khai, cần cấu hình ngoài mã, gói Windows có mã phiên bản rõ, kiểm tra trên máy sạch và quy trình phát hành riêng với source ZIP. TLS và thuật toán băm mật khẩu chuyên dụng cũng cần được xem xét trước khi mở rộng sử dụng.

Sau khi nền tảng ổn định, nhóm có thể bổ sung tái kết nối, lịch sử trận chi tiết hoặc tính năng quan sát. Những chức năng này là định hướng tương lai và nằm ngoài phạm vi chức năng hiện tại.

