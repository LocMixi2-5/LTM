package com.billiards.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Điểm khởi động của Server.
 * 
 * Lắng nghe trên PORT, mỗi kết nối mới tạo ra một ClientHandler
 * chạy trên thread riêng (mô hình multi-thread: 1 thread/client).
 * 
 * Cách chạy:
 *   cd billiards-game/server
 *   compile.bat   (hoặc build thủ công)
 *   run_server.bat
 */
public class ServerMain {

    public static final int PORT = 12345;
    
    // Lưu trữ danh sách người chơi đang online (username -> ClientHandler)
    public static final ConcurrentHashMap<String, ClientHandler> onlinePlayers = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════╗");
        System.out.println("║   8-Ball Pool Server - Khởi động...   ║");
        System.out.println("╚═══════════════════════════════════════╝");

        // Khởi tạo Database
        DatabaseManager db = new DatabaseManager();
        if (!db.isConnected()) {
            System.err.println("[Server] Không kết nối được database! Server vẫn khởi động (offline mode).");
        }

        AuthManager  authManager  = new AuthManager(db);
        LobbyManager lobbyManager = new LobbyManager(db);

        String localIp = getLocalLanIp();
        System.out.println("[Server] Đang lắng nghe trên cổng " + PORT + "...");
        System.out.println("[Server] 🌐 IP Mạng nội bộ (LAN) của Server: " + localIp + ":" + PORT);
        System.out.println("[Server] (Client trong cùng mạng Wi-Fi/LAN hãy kết nối tới IP: " + localIp + ")");

        // Bật phát hiện máy chủ tự động qua UDP (Discovery)
        startUdpDiscoveryListener();

        // Vòng lặp chấp nhận kết nối
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    clientSocket.setKeepAlive(true);

                    ClientHandler handler = new ClientHandler(clientSocket, authManager, lobbyManager, db);
                    Thread thread = new Thread(handler);
                    thread.setDaemon(true);
                    thread.start();

                } catch (IOException e) {
                    System.err.println("[Server] Lỗi kết nối client: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("[Server] Không thể mở cổng " + PORT + ": " + e.getMessage());
            System.err.println("[Server] Kiểm tra xem cổng này có đang bị dùng không.");
        }
    }

    public static String getLocalLanIp() {
        try {
            try (Socket s = new Socket()) {
                s.connect(new java.net.InetSocketAddress("8.8.8.8", 53), 1000);
                return s.getLocalAddress().getHostAddress();
            }
        } catch (Exception ignored) {}

        try {
            java.util.Enumeration<java.net.NetworkInterface> interfaces = java.net.NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                java.net.NetworkInterface ni = interfaces.nextElement();
                if (ni.isLoopback() || !ni.isUp()) continue;
                java.util.Enumeration<java.net.InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    java.net.InetAddress addr = addresses.nextElement();
                    if (addr instanceof java.net.Inet4Address && !addr.isLoopbackAddress()) {
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

    private static void startUdpDiscoveryListener() {
        Thread t = new Thread(() -> {
            try (java.net.DatagramSocket socket = new java.net.DatagramSocket(12346)) {
                socket.setBroadcast(true);
                byte[] buffer = new byte[256];
                while (true) {
                    java.net.DatagramPacket packet = new java.net.DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String msg = new String(packet.getData(), 0, packet.getLength()).trim();
                    if ("BILLIARDS_DISCOVER".equals(msg)) {
                        byte[] replyData = ("BILLIARDS_SERVER:" + PORT).getBytes();
                        java.net.DatagramPacket reply = new java.net.DatagramPacket(
                            replyData, replyData.length, packet.getAddress(), packet.getPort()
                        );
                        socket.send(reply);
                        System.out.println("[Server Discovery] Phản hồi tới client: " + packet.getAddress().getHostAddress());
                    }
                }
            } catch (Exception e) {
                // Ignore when socket closes
            }
        });
        t.setDaemon(true);
        t.setName("UDP-Discovery-Server");
        t.start();
    }
}
