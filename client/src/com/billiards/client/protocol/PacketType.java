package com.billiards.client.protocol;

/** 
 * Copy của server PacketType để Client dùng.
 * Phải giữ nguyên giá trị byte giống server.
 */
public enum PacketType {
    LOGIN_REQUEST(1), LOGIN_RESPONSE(2),
    REGISTER_REQUEST(3), REGISTER_RESPONSE(4),
    FIND_MATCH(5), MATCH_FOUND(6), CANCEL_FIND(7),
    PREGAME_READY(8), GAME_START(9),
    CUE_SHOT(10), GAME_STATE(11), TURN_CHANGE(12),
    BALL_POCKETED(13), GAME_OVER(14),
    LEADERBOARD_REQUEST(15), LEADERBOARD_RESPONSE(16),
    DISCONNECT(17), PING(18), ERROR(19),
    PHYSICS_DONE(20), CLAIM_WIN(21),
    SETTINGS_UPDATE(22), SETTINGS_RESPONSE(23),

    // Friends
    SEARCH_PLAYERS_REQUEST(24),   // Client gửi: String keyword
    SEARCH_PLAYERS_RESPONSE(25),  // Server trả: String "username|level|avatarId;..."
    ADD_FRIEND_REQUEST(26),       // Client gửi: String friendUsername (gửi lời mời kết bạn)
    ADD_FRIEND_RESPONSE(27),      // Server trả: bool success, String message
    FRIEND_LIST_REQUEST(28),      // Client gửi: (trống)
    FRIEND_LIST_RESPONSE(29),     // Server trả: String "username|level|avatarId|status;..."

    // Friend request accept/decline
    FRIEND_ACCEPT_REQUEST(30),    // Client gửi: String senderUsername (chấp nhận lời mời)
    FRIEND_ACCEPT_RESPONSE(31),   // Server trả: bool success, String message
    FRIEND_DECLINE_REQUEST(32),   // Client gửi: String senderUsername (từ chối lời mời)
    FRIEND_DECLINE_RESPONSE(33),  // Server trả: bool success, String message
    PENDING_REQUESTS_REQUEST(34), // Client gửi: (trống) - yêu cầu danh sách lời mời chờ
    PENDING_REQUESTS_RESPONSE(35),// Server trả: String "username|level|avatarId;..."
    REMOVE_FRIEND_REQUEST(36),    // Client gửi: String friendUsername
    REMOVE_FRIEND_RESPONSE(37),   // Server trả: bool success, String message

    // Chat
    CHAT_MESSAGE_REQUEST(38),     // Client gửi: String recipientUsername, String message
    CHAT_MESSAGE_RESPONSE(39),    // Server trả về cho người nhận: String senderUsername, String message
    CHAT_HISTORY_REQUEST(40),     // Client gửi: String friendUsername
    CHAT_HISTORY_RESPONSE(41),    // Server trả: String "sender|msg;sender|msg;..."

    // Challenge
    CHALLENGE_REQUEST(42),        // Client A gửi: String targetUsername
    CHALLENGE_RECEIVE(43),        // Server gửi cho Client B: String challengerUsername
    CHALLENGE_RESPONSE(44),       // Client B gửi: String challengerUsername, boolean accept
    CHALLENGE_RESULT(45),         // Server gửi cho A và B: boolean success, String message
    CUE_BALL_PLACED(46),          // Client gửi: double cueX, double cueY - Server chuyển tiếp đối thủ
    CUE_AIM(47);                  // Client gửi: boolean isAiming, double angle, double forceRatio, double spinX, double spinY

    private final byte code;
    PacketType(int code) { this.code = (byte) code; }
    public byte getCode() { return code; }

    public static PacketType fromByte(byte b) {
        for (PacketType t : values()) if (t.code == b) return t;
        return ERROR;
    }
}
