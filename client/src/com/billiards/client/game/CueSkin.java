package com.billiards.client.game;

/**
 * Định nghĩa một Skin gậy cơ bi-a trong bộ sưu tập.
 */
public class CueSkin {
    public final String id;
    public final String name;
    public final String beetleName;
    public final int requiredLevel;
    public final String tagline;
    public final String description;
    public final String mainHex;
    public final String accentHex;
    public final boolean isDefault;

    public final String tierName;
    public final String tierColorHex;
    public final String auraHex;

    public CueSkin(String id, String name, String beetleName, int requiredLevel,
                   String tagline, String description, String mainHex, String accentHex, boolean isDefault) {
        this(id, name, beetleName, requiredLevel, tagline, description, mainHex, accentHex, isDefault,
             deriveTierName(requiredLevel), deriveTierColor(requiredLevel), deriveAuraColor(requiredLevel));
    }

    public CueSkin(String id, String name, String beetleName, int requiredLevel,
                   String tagline, String description, String mainHex, String accentHex, boolean isDefault,
                   String tierName, String tierColorHex, String auraHex) {
        this.id = id;
        this.name = name;
        this.beetleName = beetleName;
        this.requiredLevel = requiredLevel;
        this.tagline = tagline;
        this.description = description;
        this.mainHex = mainHex;
        this.accentHex = accentHex;
        this.isDefault = isDefault;
        this.tierName = tierName;
        this.tierColorHex = tierColorHex;
        this.auraHex = auraHex;
    }

    private static String deriveTierName(int level) {
        if (level >= 20) return "THẦN THOẠI";
        if (level >= 15) return "HUYỀN THOẠI";
        if (level >= 10) return "SỬ THI";
        if (level >= 5)  return "QUÝ HIẾM";
        return "CỔ ĐIỂN";
    }

    private static String deriveTierColor(int level) {
        if (level >= 20) return "#F1C40F"; // Vàng hoàng kim 24K
        if (level >= 15) return "#E74C3C"; // Đỏ ruby
        if (level >= 10) return "#9B59B6"; // Tím obsidian
        if (level >= 5)  return "#2ECC71"; // Xanh ngọc lục bảo
        return "#A0A8A0"; // Bạc mộc cổ điển
    }

    private static String deriveAuraColor(int level) {
        if (level >= 20) return "#FFD700";
        if (level >= 15) return "#FF3838";
        if (level >= 10) return "#B066FF";
        if (level >= 5)  return "#4EFA8B";
        return "#D2B48C";
    }

    public boolean isUnlocked(int playerLevel) {
        return playerLevel >= requiredLevel;
    }
}
