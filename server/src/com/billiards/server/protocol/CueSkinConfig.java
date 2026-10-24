package com.billiards.server.protocol;

/**
 * Cấu hình hệ thống Skin gậy cơ và mốc cấp độ mở khóa thống nhất phía Server.
 * Server chịu trách nhiệm kiểm tra quyền sở hữu skin để bảo mật, chống gian lận client.
 */
public class CueSkinConfig {

    public static final String CUE_DEFAULT  = "cue_default";
    public static final String CUE_KABUTO   = "cue_kabuto";
    public static final String CUE_ATLAS    = "cue_atlas";
    public static final String CUE_LUCANUS  = "cue_lucanus";
    public static final String CUE_HERCULES = "cue_hercules";

    /**
     * Trả về cấp độ yêu cầu tối thiểu để mở khóa skin.
     * Fallback an toàn: Nếu chuỗi là mã màu Hex cũ (#DEB887, ...) hoặc không xác định -> Level 1 (Mộc Nguyên Bản).
     */
    public static int getRequiredLevel(String skinId) {
        if (skinId == null || skinId.trim().isEmpty()) {
            return 1;
        }
        String id = skinId.trim().toLowerCase();
        return switch (id) {
            case CUE_KABUTO   -> 5;
            case CUE_ATLAS    -> 10;
            case CUE_LUCANUS  -> 15;
            case CUE_HERCULES -> 20;
            case CUE_DEFAULT  -> 1;
            default           -> 1; // Hỗ trợ tương thích ngược cho mã hex cũ
        };
    }

    /**
     * Kiểm tra người chơi có đủ cấp độ để sử dụng skin hay không.
     */
    public static boolean isSkinUnlocked(String skinId, int playerLevel) {
        return playerLevel >= getRequiredLevel(skinId);
    }
}
