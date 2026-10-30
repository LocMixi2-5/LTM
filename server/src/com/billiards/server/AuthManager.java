package com.billiards.server;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Quản lý xác thực người dùng: đăng ký và đăng nhập.
 * 
 * Sử dụng SHA-256 + Salt để bảo vệ mật khẩu.
 * (Không dùng BCrypt vì không cần thư viện ngoài)
 * 
 * Định dạng password_hash lưu DB: base64(salt):base64(hash)
 */
public class AuthManager {

    private final DatabaseManager db;

    private static final java.util.regex.Pattern EMAIL_PATTERN =
        java.util.regex.Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$");

    public AuthManager(DatabaseManager db) {
        this.db = db;
    }

    // ============================================================
    // ĐĂNG KÝ
    // ============================================================

    /**
     * Xử lý yêu cầu đăng ký tài khoản mới.
     * 
     * @return "OK" nếu thành công
     *         "INVALID_EMAIL" nếu định dạng email không hợp lệ
     *         "EMAIL_EXISTS" nếu email đã dùng
     *         "USERNAME_EXISTS" nếu username đã dùng
     *         "ERROR" nếu có lỗi hệ thống
     */
    public String register(String email, String username, String plainPassword) {
        // Validate đầu vào cơ bản
        if (email == null || email.isBlank() || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            return "INVALID_EMAIL";
        }
        if (username == null || username.isBlank())   return "ERROR";
        if (plainPassword == null || plainPassword.length() < 6) return "ERROR";

        // Hash mật khẩu trước khi lưu
        String passwordHash = hashPassword(plainPassword);
        return db.register(email, username, passwordHash);
    }

    // ============================================================
    // ĐĂNG NHẬP
    // ============================================================

    /**
     * Xác thực đăng nhập.
     * 
     * @return PlayerInfo nếu đúng, null nếu sai username/password
     */
    public DatabaseManager.PlayerInfo login(String username, String plainPassword) {
        DatabaseManager.PlayerInfo player = db.getPlayerByUsername(username);
        if (player == null) return null; // Username không tồn tại

        // Kiểm tra password
        if (!verifyPassword(plainPassword, player.passwordHash)) return null;

        return player;
    }

    // ============================================================
    // HASH MẬT KHẨU (SHA-256 + Salt)
    // ============================================================

    /** Tạo hash từ plain password, tạo salt ngẫu nhiên */
    public static String hashPassword(String plainPassword) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hash = md.digest(plainPassword.getBytes("UTF-8"));

            String saltBase64 = Base64.getEncoder().encodeToString(salt);
            String hashBase64 = Base64.getEncoder().encodeToString(hash);
            return saltBase64 + ":" + hashBase64;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi hash mật khẩu", e);
        }
    }

    /** Xác minh plain password khớp với stored hash */
    public static boolean verifyPassword(String plainPassword, String storedHash) {
        try {
            String[] parts = storedHash.split(":");
            if (parts.length != 2) return false;

            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[1]);

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] actualHash = md.digest(plainPassword.getBytes("UTF-8"));

            // So sánh constant-time để tránh timing attack
            if (actualHash.length != expectedHash.length) return false;
            int diff = 0;
            for (int i = 0; i < actualHash.length; i++) {
                diff |= actualHash[i] ^ expectedHash[i];
            }
            return diff == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
