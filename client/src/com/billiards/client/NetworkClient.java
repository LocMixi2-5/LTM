package com.billiards.client;

import com.billiards.client.protocol.Packet;
import com.billiards.client.protocol.PacketType;
import javafx.application.Platform;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Quản lý kết nối TCP tới Server.
 * 
 * Chạy một thread listener riêng để nhận gói tin bất đồng bộ.
 * Khi nhận packet → gọi callback đăng ký bởi các Scene tương ứng.
 * 
 * Hỗ trợ:
 * - Đổi IP & Port động lúc runtime mà không cần khởi động lại
 * - Tự động phát hiện Server trong mạng nội bộ (LAN Auto-Discovery)
 * - Tự động ghi nhớ IP máy chủ vào server_ip.txt
 */
public class NetworkClient {

    public static final int DEFAULT_PORT = 12345;
    private static final String CONFIG_FILE = "server_ip.txt";

    private String host;
    private int    port;

    private Socket socket;
    private DataInputStream  in;
    private DataOutputStream out;
    private boolean connected = false;

    // Thông tin người chơi sau khi đăng nhập
    private PlayerSession session = null;

    // Callback khi nhận được gói tin (chạy trên Network thread)
    private Consumer<ReceivedPacket> onPacketReceived;

    // Cache bảng xếp hạng ngầm để hiển thị tức thì khi người chơi mở BXH
    private volatile String cachedLeaderboardData = null;
    private Consumer<String> onLeaderboardUpdated;

    // Đo độ trễ mạng (Ping RTT in milliseconds)
    private volatile int currentPingMs = 0;
    private Consumer<Integer> onPingUpdated;

    public int getPingMs() { return currentPingMs; }
    public void setOnPingUpdated(Consumer<Integer> callback) { this.onPingUpdated = callback; }

    public NetworkClient() {
        String sysHost = System.getProperty("server.host");
        String sysPort = System.getProperty("server.port");

        if (sysHost != null && !sysHost.trim().isEmpty() && !sysHost.equals("[IP_ADDRESS]")) {
            this.host = sysHost.trim();
        } else {
            this.host = loadSavedHost();
        }

        if (sysPort != null && !sysPort.trim().isEmpty()) {
            try {
                this.port = Integer.parseInt(sysPort.trim());
            } catch (NumberFormatException e) {
                this.port = DEFAULT_PORT;
            }
        } else {
            this.port = loadSavedPort();
        }
    }

    public NetworkClient(String host, int port) {
        this.host = (host != null && !host.trim().isEmpty()) ? host.trim() : "127.0.0.1";
        this.port = (port > 0) ? port : DEFAULT_PORT;
    }

    // ============================================================
    // KẾT NỐI & NGẮT KẾT NỐI
    // ============================================================

    public boolean connect() {
        return connect(this.host, this.port);
    }

    public synchronized boolean connect(String targetHost, int targetPort) {
        disconnect(); // Đóng kết nối cũ nếu có

        this.host = (targetHost != null && !targetHost.trim().isEmpty()) ? targetHost.trim() : "127.0.0.1";
        this.port = (targetPort > 0) ? targetPort : DEFAULT_PORT;

        try {
            socket = new Socket();
            // Kết nối với timeout 2.5 giây tránh treo giao diện
            socket.connect(new InetSocketAddress(this.host, this.port), 2500);
            socket.setKeepAlive(true);
            socket.setTcpNoDelay(true); // Vô hiệu hóa Nagle's algorithm để giảm thiểu tối đa độ trễ TCP
            in  = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            connected = true;

            // Bắt đầu thread lắng nghe packet
            Thread listenerThread = new Thread(this::listenLoop);
            listenerThread.setDaemon(true);
            listenerThread.setName("Client-Packet-Listener");
            listenerThread.start();

            // Khởi động luồng ping đo RTT định kỳ mỗi 2s
            Thread pingThread = new Thread(() -> {
                while (connected) {
                    try {
                        Thread.sleep(2000);
                        if (connected) {
                            sendPing();
                        }
                    } catch (InterruptedException ignored) {
                        break;
                    }
                }
            });
            pingThread.setDaemon(true);
            pingThread.setName("Client-Ping-Thread");
            pingThread.start();
            sendPing();

            saveSavedHost(this.host, this.port);
            System.out.println("[Network] Kết nối Server thành công: " + this.host + ":" + this.port);
            return true;
        } catch (IOException e) {
            System.err.println("[Network] Không thể kết nối Server (" + this.host + ":" + this.port + "): " + e.getMessage());
            connected = false;
            return false;
        }
    }

    public synchronized void disconnect() {
        connected = false;
        try {
            if (out != null) {
                try {
                    Packet.writeType(out, PacketType.DISCONNECT);
                    Packet.flush(out);
                } catch (Exception ignored) {}
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {}
        finally {
            socket = null;
            in = null;
            out = null;
            session = null;
        }
    }

    // ============================================================
    // CẤU HÌNH & LƯU TRỮ IP
    // ============================================================

    public String getHost() { return host; }
    public int getPort() { return port; }
    public void setHost(String host) { this.host = host; }
    public void setPort(int port) { this.port = port; }

    public static String loadSavedHost() {
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
                String line = reader.readLine();
                if (line != null && !line.trim().isEmpty()) {
                    String clean = line.trim();
                    if (clean.contains(":")) {
                        clean = clean.split(":")[0].trim();
                    }
                    if (!clean.isEmpty()) return clean;
                }
            } catch (Exception ignored) {}
        }
        return "127.0.0.1";
    }

    public static int loadSavedPort() {
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
                String line = reader.readLine();
                if (line != null && line.contains(":")) {
                    String[] parts = line.split(":");
                    if (parts.length > 1) {
                        try {
                            return Integer.parseInt(parts[1].trim());
                        } catch (NumberFormatException ignored) {}
                    }
                }
            } catch (Exception ignored) {}
        }
        return DEFAULT_PORT;
    }

    public static void saveSavedHost(String host, int port) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(CONFIG_FILE, StandardCharsets.UTF_8))) {
            writer.println(host + ":" + port);
        } catch (Exception e) {
            System.err.println("[Network] Không thể lưu cấu hình IP: " + e.getMessage());
        }
    }

    /**
     * Lấy địa chỉ IPv4 nội bộ (LAN) của máy tính này.
     */
    public static String getLocalLanIp() {
        try {
            try (Socket s = new Socket()) {
                s.connect(new InetSocketAddress("8.8.8.8", 53), 800);
                return s.getLocalAddress().getHostAddress();
            }
        } catch (Exception ignored) {}

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (ni.isLoopback() || !ni.isUp()) continue;
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        String ip = addr.getHostAddress();
                        if (!ip.startsWith("169.254.")) {
                            return ip;
                        }
                    }
                }
            }
        } catch (Exception ignored) {}

        return "127.0.0.1";
    }

    /**
     * Tự động quét Server trên mạng nội bộ (LAN):
     * 1. Gửi gói UDP Broadcast "BILLIARDS_DISCOVER" tới cổng 12346.
     * 2. Nếu UDP bị tường lửa/router chặn, quét nhanh dải subnet cổng 12345 bằng TCP.
     */
    public static void autoDiscoverServerAsync(Consumer<String> onFound, Runnable onNotFound) {
        Thread t = new Thread(() -> {
            String foundIp = null;

            // Bước 1: Thử UDP Broadcast trên cổng 12346
            try (DatagramSocket udp = new DatagramSocket()) {
                udp.setBroadcast(true);
                udp.setSoTimeout(1200);
                byte[] data = "BILLIARDS_DISCOVER".getBytes(StandardCharsets.UTF_8);
                DatagramPacket packet = new DatagramPacket(
                    data, data.length, InetAddress.getByName("255.255.255.255"), 12346
                );
                udp.send(packet);

                byte[] buf = new byte[256];
                DatagramPacket resp = new DatagramPacket(buf, buf.length);
                udp.receive(resp);
                String msg = new String(resp.getData(), 0, resp.getLength()).trim();
                if (msg.startsWith("BILLIARDS_SERVER")) {
                    foundIp = resp.getAddress().getHostAddress();
                }
            } catch (Exception ignored) {}

            // Bước 2: Nếu chưa tìm thấy qua UDP, quét subnet TCP cổng 12345
            if (foundIp == null) {
                String myIp = getLocalLanIp();
                if (!myIp.equals("127.0.0.1") && myIp.contains(".")) {
                    String prefix = myIp.substring(0, myIp.lastIndexOf('.') + 1);
                    ExecutorService pool = Executors.newFixedThreadPool(32);
                    AtomicReference<String> discovered = new AtomicReference<>(null);

                    List<Future<?>> futures = new ArrayList<>();
                    for (int i = 1; i <= 254; i++) {
                        final String target = prefix + i;
                        futures.add(pool.submit(() -> {
                            if (discovered.get() != null) return;
                            try (Socket s = new Socket()) {
                                s.connect(new InetSocketAddress(target, 12345), 250);
                                discovered.compareAndSet(null, target);
                            } catch (Exception ignored) {}
                        }));
                    }
                    pool.shutdown();
                    try {
                        pool.awaitTermination(3500, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException ignored) {}
                    foundIp = discovered.get();
                }
            }

            final String res = foundIp;
            Platform.runLater(() -> {
                if (res != null) {
                    onFound.accept(res);
                } else {
                    onNotFound.run();
                }
            });
        });
        t.setDaemon(true);
        t.setName("LAN-Auto-Discover-Thread");
        t.start();
    }

    // ============================================================
    // VÒNG LẶP NHẬN GÓI TIN (chạy trên thread riêng)
    // ============================================================

    private void listenLoop() {
        try {
            while (connected) {
                PacketType type = Packet.readType(in);

                // Đọc toàn bộ payload của packet vào buffer byte an toàn
                // Đảm bảo stream TCP không bao giờ bị lệch byte (desync) dù callback có đọc hay bỏ qua packet
                byte[] payload = readPacketPayload(type, in);

                // Tự động giải mã và lưu cache bảng xếp hạng ngầm ngay khi nhận được
                if (type == PacketType.LEADERBOARD_RESPONSE) {
                    try {
                        String data = Packet.readString(new DataInputStream(new ByteArrayInputStream(payload)));
                        cachedLeaderboardData = data;
                        if (onLeaderboardUpdated != null) {
                            try {
                                onLeaderboardUpdated.accept(data);
                            } catch (Exception e) {
                                System.err.println("[Network] Lỗi onLeaderboardUpdated: " + e.getMessage());
                            }
                        }
                    } catch (Exception ignored) {}
                }

                // Tự động tính toán độ trễ RTT khi nhận phản hồi PING
                if (type == PacketType.PING) {
                    try {
                        long sentTime = Packet.readLong(new DataInputStream(new ByteArrayInputStream(payload)));
                        long rtt = System.currentTimeMillis() - sentTime;
                        this.currentPingMs = (int) Math.max(0, rtt);
                        if (onPingUpdated != null) {
                            try {
                                onPingUpdated.accept(this.currentPingMs);
                            } catch (Exception ignored) {}
                        }
                    } catch (Exception ignored) {}
                }

                if (onPacketReceived != null) {
                    ReceivedPacket pkt = new ReceivedPacket(type, payload);
                    try {
                        onPacketReceived.accept(pkt);
                    } catch (Throwable t) {
                        System.err.println("[Network] Exception trong onPacketReceived (" + type + "): " + t.getMessage());
                        t.printStackTrace();
                    }
                }
            }
        } catch (EOFException | java.net.SocketException e) {
            System.out.println("[Network] Kết nối bị đóng: " + e.getMessage());
        } catch (Throwable e) {
            System.err.println("[Network] Lỗi nhận packet: " + e.getMessage());
        } finally {
            connected = false;
        }
    }

    /**
     * Đọc toàn bộ dữ liệu (payload) của packet từ Socket stream vào byte array.
     * Phương thức này bảo đảm stream TCP luôn được đọc hết chính xác cho từng loại gói tin,
     * ngăn chặn 100% lỗi lệch byte hoặc tràn bộ đệm khi scene chuyển đổi.
     */
    private byte[] readPacketPayload(PacketType type, DataInputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        switch (type) {
            case LOGIN_RESPONSE -> {
                boolean success = Packet.readBoolean(in);
                Packet.writeBoolean(dos, success);
                String msgOrUser = Packet.readString(in);
                Packet.writeString(dos, msgOrUser);
                if (success) {
                    Packet.writeInt(dos, Packet.readInt(in));       // exp
                    Packet.writeInt(dos, Packet.readInt(in));       // level
                    Packet.writeInt(dos, Packet.readInt(in));       // wins
                    Packet.writeInt(dos, Packet.readInt(in));       // losses
                    Packet.writeInt(dos, Packet.readInt(in));       // avatarId
                    Packet.writeString(dos, Packet.readString(in));  // cueColor
                }
            }
            case REGISTER_RESPONSE, ADD_FRIEND_RESPONSE, FRIEND_ACCEPT_RESPONSE,
                 FRIEND_DECLINE_RESPONSE, REMOVE_FRIEND_RESPONSE, SETTINGS_RESPONSE,
                 CHALLENGE_RESULT -> {
                boolean success = Packet.readBoolean(in);
                String msg = Packet.readString(in);
                Packet.writeBoolean(dos, success);
                Packet.writeString(dos, msg);
            }
            case MATCH_FOUND -> {
                String oppName = Packet.readString(in);
                int oppLevel = Packet.readInt(in);
                int oppAvatarId = Packet.readInt(in);
                String oppCueColor = Packet.readString(in);
                Packet.writeString(dos, oppName);
                Packet.writeInt(dos, oppLevel);
                Packet.writeInt(dos, oppAvatarId);
                Packet.writeString(dos, oppCueColor);
            }
            case GAME_START -> {
                int pIdx = Packet.readInt(in);
                boolean isFirstTurn = Packet.readBoolean(in);
                long seed = Packet.readLong(in);
                Packet.writeInt(dos, pIdx);
                Packet.writeBoolean(dos, isFirstTurn);
                Packet.writeLong(dos, seed);
            }
            case GAME_STATE -> {
                Packet.writeDouble(dos, Packet.readDouble(in)); // angle
                Packet.writeDouble(dos, Packet.readDouble(in)); // force
                Packet.writeDouble(dos, Packet.readDouble(in)); // cueX
                Packet.writeDouble(dos, Packet.readDouble(in)); // cueY
                Packet.writeDouble(dos, Packet.readDouble(in)); // spinX
                Packet.writeDouble(dos, Packet.readDouble(in)); // spinY
            }
            case CUE_BALL_PLACED -> {
                Packet.writeDouble(dos, Packet.readDouble(in)); // cueX
                Packet.writeDouble(dos, Packet.readDouble(in)); // cueY
            }
            case CUE_AIM -> {
                Packet.writeBoolean(dos, Packet.readBoolean(in)); // isAiming
                Packet.writeDouble(dos, Packet.readDouble(in));   // angle
                Packet.writeDouble(dos, Packet.readDouble(in));   // forceRatio
                Packet.writeDouble(dos, Packet.readDouble(in));   // spinX
                Packet.writeDouble(dos, Packet.readDouble(in));   // spinY
            }
            case TURN_CHANGE -> {
                boolean isYourTurn = Packet.readBoolean(in);
                boolean isBallInHand = Packet.readBoolean(in);
                String reason = Packet.readString(in);
                Packet.writeBoolean(dos, isYourTurn);
                Packet.writeBoolean(dos, isBallInHand);
                Packet.writeString(dos, reason);
            }
            case BALL_POCKETED -> {
                Packet.writeInt(dos, Packet.readInt(in)); // ballIndex
            }
            case GAME_OVER -> {
                boolean youWin = Packet.readBoolean(in);
                int exp = Packet.readInt(in);
                Packet.writeBoolean(dos, youWin);
                Packet.writeInt(dos, exp);
            }
            case LEADERBOARD_RESPONSE, SEARCH_PLAYERS_RESPONSE, PENDING_REQUESTS_RESPONSE,
                 FRIEND_LIST_RESPONSE, CHAT_HISTORY_RESPONSE, CHALLENGE_RECEIVE -> {
                String data = Packet.readString(in);
                Packet.writeString(dos, data);
            }
            case CHAT_MESSAGE_RESPONSE -> {
                String sender = Packet.readString(in);
                String msg = Packet.readString(in);
                Packet.writeString(dos, sender);
                Packet.writeString(dos, msg);
            }
            case PING -> {
                Packet.writeLong(dos, Packet.readLong(in));
            }
            default -> {
                // DISCONNECT, ERROR hoặc gói không có payload
            }
        }
        dos.flush();
        return baos.toByteArray();
    }

    // ============================================================
    // GỬI GÓI TIN (thread-safe)
    // ============================================================

    public synchronized void sendLogin(String username, String password) {
        try {
            Packet.writeType(out, PacketType.LOGIN_REQUEST);
            Packet.writeString(out, username);
            Packet.writeString(out, password);
            Packet.flush(out);
        } catch (IOException e) { System.err.println("[Network] sendLogin: " + e.getMessage()); }
    }

    public synchronized void sendRegister(String email, String username, String password) {
        try {
            Packet.writeType(out, PacketType.REGISTER_REQUEST);
            Packet.writeString(out, email);
            Packet.writeString(out, username);
            Packet.writeString(out, password);
            Packet.flush(out);
        } catch (IOException e) { System.err.println("[Network] sendRegister: " + e.getMessage()); }
    }

    public synchronized void sendRegister(String username, String password) {
        sendRegister(username + "@game.local", username, password);
    }

    public synchronized void sendFindMatch() {
        try {
            Packet.writeType(out, PacketType.FIND_MATCH);
            Packet.flush(out);
        } catch (IOException e) { System.err.println("[Network] sendFindMatch: " + e.getMessage()); }
    }

    public synchronized void sendCancelFind() {
        try {
            Packet.writeType(out, PacketType.CANCEL_FIND);
            Packet.flush(out);
        } catch (IOException e) { System.err.println("[Network] sendCancelFind: " + e.getMessage()); }
    }

    public synchronized void sendPregameReady() {
        try {
            Packet.writeType(out, PacketType.PREGAME_READY);
            Packet.flush(out);
        } catch (IOException e) { System.err.println("[Network] sendPregameReady: " + e.getMessage()); }
    }

    public synchronized void sendCueShot(double angle, double force, double cueX, double cueY, double spinX, double spinY) {
        try {
            Packet.writeType(out, PacketType.CUE_SHOT);
            Packet.writeDouble(out, angle);
            Packet.writeDouble(out, force);
            Packet.writeDouble(out, cueX);
            Packet.writeDouble(out, cueY);
            Packet.writeDouble(out, spinX);
            Packet.writeDouble(out, spinY);
            Packet.flush(out);
        } catch (IOException e) { System.err.println("[Network] sendCueShot: " + e.getMessage()); }
    }

    /** Gửi tọa độ bi cái vừa đặt (sau khi thả bi cái trên bàn) để đối thủ thấy trước khi bắn */
    public synchronized void sendCueBallPlaced(double cueX, double cueY) {
        try {
            Packet.writeType(out, PacketType.CUE_BALL_PLACED);
            Packet.writeDouble(out, cueX);
            Packet.writeDouble(out, cueY);
            Packet.flush(out);
        } catch (IOException e) { System.err.println("[Network] sendCueBallPlaced: " + e.getMessage()); }
    }

    /** Gửi trạng thái ngắm cơ và kéo lực thời gian thực để đối thủ nhìn thấy */
    public synchronized void sendCueAim(boolean isAiming, double angle, double forceRatio, double spinX, double spinY) {
        try {
            Packet.writeType(out, PacketType.CUE_AIM);
            Packet.writeBoolean(out, isAiming);
            Packet.writeDouble(out, angle);
            Packet.writeDouble(out, forceRatio);
            Packet.writeDouble(out, spinX);
            Packet.writeDouble(out, spinY);
            Packet.flush(out);
        } catch (IOException e) { System.err.println("[Network] sendCueAim: " + e.getMessage()); }
    }

    /** Gửi gói PING kèm timestamp để tính toán độ trễ Round-Trip Time (RTT) */
    public synchronized void sendPing() {
        if (!connected || out == null) return;
        try {
            Packet.writeType(out, PacketType.PING);
            Packet.writeLong(out, System.currentTimeMillis());
            Packet.flush(out);
        } catch (IOException e) { System.err.println("[Network] sendPing: " + e.getMessage()); }
    }

    /** Gửi tín hiệu physics đã xong – server sẽ chuyển lượt hoặc giữ lượt tùy vào keepTurn và xử lý lỗi */
    public synchronized void sendPhysicsDone(boolean keepTurn, boolean isFoul) {
        try {
            Packet.writeType(out, PacketType.PHYSICS_DONE);
            Packet.writeBoolean(out, keepTurn);
            Packet.writeBoolean(out, isFoul);
            Packet.flush(out);
        } catch (IOException e) { System.err.println("[Network] sendPhysicsDone: " + e.getMessage()); }
    }

    /** Gửi tín hiệu báo thắng hoặc thua (bi 8 vào lỗ) */
    public synchronized void sendClaimWin(boolean isWin) {
        try {
            Packet.writeType(out, PacketType.CLAIM_WIN);
            Packet.writeBoolean(out, isWin);
            Packet.flush(out);
        } catch (IOException e) { System.err.println("[Network] sendClaimWin: " + e.getMessage()); }
    }

    public synchronized void sendLeaderboardRequest() {
        try {
            Packet.writeType(out, PacketType.LEADERBOARD_REQUEST);
            Packet.flush(out);
        } catch (IOException e) { System.err.println("[Network] sendLeaderboard: " + e.getMessage()); }
    }

    public synchronized void sendSettingsUpdate(int avatarId, String cueColor) {
        try {
            Packet.writeType(out, PacketType.SETTINGS_UPDATE);
            Packet.writeInt(out, avatarId);
            Packet.writeString(out, cueColor != null ? cueColor : "#DEB887");
            Packet.flush(out);
        } catch (IOException e) { System.err.println("[Network] sendSettingsUpdate: " + e.getMessage()); }
    }

    /** Tìm kiếm người chơi theo keyword */
    public synchronized void sendSearchPlayers(String keyword) {
        try {
            Packet.writeType(out, PacketType.SEARCH_PLAYERS_REQUEST);
            Packet.writeString(out, keyword);
            Packet.flush(out);
        } catch (IOException e) { System.err.println("[Network] sendSearchPlayers: " + e.getMessage()); }
    }

    /** Gửi yêu cầu thêm bạn */
    public synchronized void sendAddFriend(String friendUsername) {
        try {
            Packet.writeType(out, PacketType.ADD_FRIEND_REQUEST);
            Packet.writeString(out, friendUsername);
            Packet.flush(out);
        } catch (IOException e) { System.err.println("[Network] sendAddFriend: " + e.getMessage()); }
    }

    /** Yêu cầu danh sách bạn bè */
    public synchronized void sendFriendListRequest() {
        try {
            Packet.writeType(out, PacketType.FRIEND_LIST_REQUEST);
            Packet.flush(out);
        } catch (IOException e) { System.err.println("[Network] sendFriendListRequest: " + e.getMessage()); }
    }

    /** Chấp nhận lời mời kết bạn */
    public synchronized void sendAcceptFriend(String senderUsername) {
        try {
            Packet.writeType(out, PacketType.FRIEND_ACCEPT_REQUEST);
            Packet.writeString(out, senderUsername);
            Packet.flush(out);
        } catch (IOException e) { System.err.println("[Network] sendAcceptFriend: " + e.getMessage()); }
    }

    /** Từ chối lời mời kết bạn */
    public synchronized void sendDeclineFriend(String senderUsername) {
        try {
            Packet.writeType(out, PacketType.FRIEND_DECLINE_REQUEST);
            Packet.writeString(out, senderUsername);
            Packet.flush(out);
        } catch (IOException e) { System.err.println("[Network] sendDeclineFriend: " + e.getMessage()); }
    }

    /** Yêu cầu danh sách lời mời kết bạn đang chờ */
    public synchronized void sendPendingRequestsList() {
        try {
            Packet.writeType(out, PacketType.PENDING_REQUESTS_REQUEST);
            Packet.flush(out);
        } catch (IOException e) { System.err.println("[Network] sendPendingRequestsList: " + e.getMessage()); }
    }

    /** Xóa bạn bè */
    public synchronized void sendRemoveFriend(String friendUsername) {
        try {
            Packet.writeType(out, PacketType.REMOVE_FRIEND_REQUEST);
            Packet.writeString(out, friendUsername);
            Packet.flush(out);
        } catch (IOException e) { System.err.println("[Network] sendRemoveFriend: " + e.getMessage()); }
    }

    /** Yêu cầu lịch sử chat */
    public synchronized void sendChatHistoryRequest(String friendUsername) {
        try {
            Packet.writeType(out, PacketType.CHAT_HISTORY_REQUEST);
            Packet.writeString(out, friendUsername);
            Packet.flush(out);
        } catch (IOException e) { System.err.println("[Network] sendChatHistoryRequest: " + e.getMessage()); }
    }

    /** Gửi tin nhắn chat */
    public synchronized void sendChatMessage(String recipientUsername, String message) {
        try {
            Packet.writeType(out, PacketType.CHAT_MESSAGE_REQUEST);
            Packet.writeString(out, recipientUsername);
            Packet.writeString(out, message);
            Packet.flush(out);
        } catch (IOException e) { System.err.println("[Network] sendChatMessage: " + e.getMessage()); }
    }

    /** Gửi lời thách đấu */
    public synchronized void sendChallengeRequest(String targetUsername) {
        try {
            Packet.writeType(out, PacketType.CHALLENGE_REQUEST);
            Packet.writeString(out, targetUsername);
            Packet.flush(out);
        } catch (IOException e) { System.err.println("[Network] sendChallengeRequest: " + e.getMessage()); }
    }

    /** Phản hồi lời thách đấu */
    public synchronized void sendChallengeResponse(String challengerUsername, boolean accept) {
        try {
            Packet.writeType(out, PacketType.CHALLENGE_RESPONSE);
            Packet.writeString(out, challengerUsername);
            Packet.writeBoolean(out, accept);
            Packet.flush(out);
        } catch (IOException e) { System.err.println("[Network] sendChallengeResponse: " + e.getMessage()); }
    }

    // ============================================================
    // SESSION – Lưu thông tin người chơi sau khi đăng nhập
    // ============================================================

    public void setSession(String username, int exp, int level, int wins, int losses) {
        setSession(username, exp, level, wins, losses, 0, "#DEB887");
    }

    public void setSession(String username, int exp, int level, int wins, int losses, int avatarId, String cueColor) {
        this.session = new PlayerSession(username, exp, level, wins, losses, avatarId, cueColor);
    }

    public PlayerSession getSession() { return session; }

    // ============================================================
    // CALLBACK & GETTER
    // ============================================================

    public void setOnPacketReceived(Consumer<ReceivedPacket> callback) {
        this.onPacketReceived = callback;
    }

    public String getCachedLeaderboardData() {
        return cachedLeaderboardData;
    }

    public void setOnLeaderboardUpdated(Consumer<String> callback) {
        this.onLeaderboardUpdated = callback;
    }

    public boolean isConnected() { return connected && socket != null && !socket.isClosed(); }

    // ============================================================
    // Inner class: Gói tin đã nhận, kèm stream để đọc dữ liệu
    // ============================================================
    public static class ReceivedPacket {
        public final PacketType type;
        public final DataInputStream stream;
        private final String preReadString;

        public ReceivedPacket(PacketType type, DataInputStream stream) {
            this.type   = type;
            this.stream = stream;
            this.preReadString = null;
        }

        public ReceivedPacket(PacketType type, byte[] payload) {
            this.type   = type;
            this.stream = new DataInputStream(new ByteArrayInputStream(payload != null ? payload : new byte[0]));
            this.preReadString = null;
        }

        public ReceivedPacket(PacketType type, String preReadString) {
            this.type   = type;
            this.stream = null;
            this.preReadString = preReadString;
        }

        // Các phương thức đọc tiện ích
        public String readString()  throws IOException {
            if (preReadString != null) return preReadString;
            return Packet.readString(stream);
        }
        public boolean readBoolean()throws IOException { return Packet.readBoolean(stream); }
        public int readInt()        throws IOException { return Packet.readInt(stream); }
        public double readDouble()  throws IOException { return Packet.readDouble(stream); }
        public long readLong()      throws IOException { return Packet.readLong(stream); }
    }

    // ============================================================
    // Inner class: Thông tin phiên người chơi
    // ============================================================
    public static class PlayerSession {
        public final String username;
        public int exp, level, wins, losses;
        public int avatarId;
        public String cueColor;

        public PlayerSession(String username, int exp, int level, int wins, int losses) {
            this(username, exp, level, wins, losses, 0, "#DEB887");
        }

        public PlayerSession(String username, int exp, int level, int wins, int losses, int avatarId, String cueColor) {
            this.username = username;
            this.exp      = exp;
            this.level    = level;
            this.wins     = wins;
            this.losses   = losses;
            this.avatarId = avatarId;
            this.cueColor = (cueColor != null && !cueColor.isEmpty()) ? cueColor : "#DEB887";
        }
    }
}
