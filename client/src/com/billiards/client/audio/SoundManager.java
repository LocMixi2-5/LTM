package com.billiards.client.audio;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.prefs.Preferences;

/**
 * Quản lý toàn bộ âm thanh chân thực trong game:
 * - 5 Bản nhạc nền (BGM) đa phong cách có sẵn trong Client:
 *   1. 🎱 Billiards Lounge (Jazz Quán Bi-a Cổ Điển)
 *   2. 🔥 Cyberpunk Night (Sôi Động / Neon Club)
 *   3. ☕ Chillhop Cafe (Thư Giãn / Lo-Fi Beats)
 *   4. 🐉 Thần Long Xuất Hải (Huyền Ảo / Tiên Hiệp)
 *   5. ⚡ Arcade Speed (Điện Tử / Nhịp Nhanh)
 * - Hiệu ứng âm thanh (SFX) đa dạng & chân thực
 * - Tự động lưu cấu hình và bài hát đã chọn vào Preferences
 */
public class SoundManager {

    // Nhạc nền (BGM) cố định: ⚡ Arcade Speed (Điện Tử / Nhịp Nhanh)
    public static final String[][] BGM_TRACKS = {
        {"⚡ Arcade Speed (Điện Tử)", "assets/sounds/bgm_arcade.mp3"}
    };

    private static MediaPlayer bgmPlayer;
    private static int         currentTrackIndex = 0;

    // SFX AudioClips
    private static final List<AudioClip> collisionSounds = new ArrayList<>();
    private static AudioClip cushionSound;
    private static AudioClip cueHitSound;
    private static AudioClip pocketSound;
    private static AudioClip turnAlertSound;
    private static AudioClip timerTickSound;
    private static AudioClip applauseSound;
    private static AudioClip foulSound;
    private static AudioClip btnClickSound;

    private static final Random random = new Random();

    // Cài đặt âm lượng (0.0 đến 1.0)
    private static double bgmVolume = 0.35;
    private static double sfxVolume = 1.0;
    private static boolean muted    = false;

    private static final Preferences prefs = Preferences.userNodeForPackage(SoundManager.class);

    public static void init() {
        bgmVolume         = prefs.getDouble("bgm_volume", 0.35);
        sfxVolume         = prefs.getDouble("sfx_volume", 1.0);
        muted             = prefs.getBoolean("muted", false);
        currentTrackIndex = 0;
        prefs.putInt("bgm_track_index", 0);

        try {
            // Load SFX
            loadCollisionSounds();

            cushionSound   = loadClip("assets/sounds/cushion.mp3");
            cueHitSound    = loadClipFirst("assets/sounds/cue_shot.mp3", "assets/sounds/cue_hit.mp3");
            pocketSound    = loadClipFirst("assets/sounds/pocket_drop.mp3", "assets/sounds/pocket.wav");
            turnAlertSound = loadClip("assets/sounds/turn_alert.wav");
            timerTickSound = loadClip("assets/sounds/timer_tick.wav");
            applauseSound  = loadClip("assets/sounds/applause.mp3");
            foulSound      = loadClip("assets/sounds/foul.wav");
            btnClickSound  = loadClip("assets/sounds/btn_click.wav");

            // Khởi động Track BGM đã lưu
            loadTrack(currentTrackIndex);

            System.out.println("[Audio] Hệ thống âm thanh đã nạp 5 BGM tracks và SFX hoàn tất!");
        } catch (Exception e) {
            System.err.println("[Audio] Lỗi khi tải âm thanh: " + e.getMessage());
        }
    }

    private static AudioClip loadClip(String path) {
        try {
            File f = new File(path);
            if (f.exists()) {
                return new AudioClip(f.toURI().toString());
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static AudioClip loadClipFirst(String... paths) {
        for (String p : paths) {
            AudioClip clip = loadClip(p);
            if (clip != null) return clip;
        }
        return null;
    }

    private static void loadCollisionSounds() {
        collisionSounds.clear();
        String[] colPaths = {
            "assets/sounds/ball_hit1.mp3",
            "assets/sounds/ball_hit2.mp3",
            "assets/sounds/ball_hit3.mp3",
            "assets/sounds/bob_hit.wav",
            "assets/sounds/collision.wav"
        };
        for (String p : colPaths) {
            AudioClip clip = loadClip(p);
            if (clip != null) collisionSounds.add(clip);
        }
    }

    // ============================================================
    // BGM CONTROLS & BUILT-IN TRACK SELECTION
    // ============================================================

    public static int getTrackCount() {
        return BGM_TRACKS.length;
    }

    public static String getTrackName(int index) {
        if (index < 0 || index >= BGM_TRACKS.length) return BGM_TRACKS[0][0];
        return BGM_TRACKS[index][0];
    }

    public static int getCurrentTrackIndex() {
        return currentTrackIndex;
    }

    public static String getCurrentBgmName() {
        return BGM_TRACKS[currentTrackIndex][0];
    }

    public static void selectTrack(int index) {
        currentTrackIndex = 0;
        prefs.putInt("bgm_track_index", 0);
        loadTrack(0);
    }

    public static void nextTrack() {
        // Cố định nhạc ⚡ Arcade Speed, không cho đổi nhạc
        selectTrack(0);
    }

    public static void prevTrack() {
        // Cố định nhạc ⚡ Arcade Speed, không cho đổi nhạc
        selectTrack(0);
    }

    private static boolean loadTrack(int trackIdx) {
        try {
            boolean wasPlaying = (bgmPlayer != null && bgmPlayer.getStatus() == MediaPlayer.Status.PLAYING);
            if (bgmPlayer != null) {
                bgmPlayer.stop();
                bgmPlayer.dispose();
            }

            String path = BGM_TRACKS[trackIdx][1];
            File file = new File(path);
            if (!file.exists()) {
                file = new File("assets/sounds/bgm.mp3"); // fallback
            }

            Media media = new Media(file.toURI().toString());
            bgmPlayer = new MediaPlayer(media);
            bgmPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            bgmPlayer.setVolume(muted ? 0.0 : bgmVolume);

            if (wasPlaying) {
                bgmPlayer.play();
            }
            return true;
        } catch (Exception e) {
            System.err.println("[Audio] Lỗi tải bài hát BGM #" + trackIdx + ": " + e.getMessage());
            return false;
        }
    }

    public static void playBGM() {
        if (bgmPlayer != null) {
            bgmPlayer.setVolume(muted ? 0.0 : bgmVolume);
            bgmPlayer.play();
        }
    }

    public static void stopBGM() {
        if (bgmPlayer != null) {
            bgmPlayer.stop();
        }
    }

    public static void pauseBGM() {
        if (bgmPlayer != null) {
            bgmPlayer.pause();
        }
    }

    // ============================================================
    // SFX CONTROLS
    // ============================================================

    public static void playCollision() {
        playCollision(0.8);
    }

    public static void playCollision(double forceRatio) {
        if (muted || sfxVolume <= 0.01 || collisionSounds.isEmpty()) return;
        try {
            AudioClip clip = collisionSounds.get(random.nextInt(collisionSounds.size()));
            // Âm va chạm bi đanh giòn, to rõ và sống động
            double effectiveVol = Math.max(0.35, Math.min(1.0, sfxVolume * (0.5 + forceRatio * 0.5)));
            clip.play(effectiveVol);
        } catch (Exception ignored) {}
    }

    public static void playCushion(double speedRatio) {
        if (muted || sfxVolume <= 0.01 || cushionSound == null) return;
        try {
            double effectiveVol = Math.max(0.25, Math.min(1.0, sfxVolume * (0.4 + speedRatio * 0.6)));
            cushionSound.play(effectiveVol);
        } catch (Exception ignored) {}
    }

    public static void playCueHit() {
        playCueHit(1.0);
    }

    public static void playCueHit(double forceRatio) {
        if (muted || sfxVolume <= 0.01 || cueHitSound == null) return;
        try {
            // Âm thụt cơ to vang, có lực và nẩy dứt khoát
            double effectiveVol = Math.max(0.40, Math.min(1.0, sfxVolume * (0.6 + forceRatio * 0.4)));
            cueHitSound.play(effectiveVol);
        } catch (Exception ignored) {}
    }

    public static void playPocket() {
        if (muted || sfxVolume <= 0.01 || pocketSound == null) return;
        try {
            pocketSound.play(Math.min(1.0, sfxVolume * 1.1));
        } catch (Exception ignored) {}
    }

    public static void playTurnAlert() {
        if (muted || sfxVolume <= 0.01 || turnAlertSound == null) return;
        try {
            turnAlertSound.play(Math.min(1.0, sfxVolume * 0.9));
        } catch (Exception ignored) {}
    }

    public static void playTimerTick() {
        if (muted || sfxVolume <= 0.01 || timerTickSound == null) return;
        try {
            timerTickSound.play(Math.min(1.0, sfxVolume * 0.75));
        } catch (Exception ignored) {}
    }

    public static void playVictory() {
        if (muted || sfxVolume <= 0.01 || applauseSound == null) return;
        try {
            applauseSound.play(sfxVolume);
        } catch (Exception ignored) {}
    }

    public static void playFoul() {
        if (muted || sfxVolume <= 0.01 || foulSound == null) return;
        try {
            foulSound.play(Math.min(1.0, sfxVolume * 0.8));
        } catch (Exception ignored) {}
    }

    public static void playError() {
        playFoul();
    }

    public static void playButtonClick() {
        if (muted || sfxVolume <= 0.01 || btnClickSound == null) return;
        try {
            btnClickSound.play(Math.min(1.0, sfxVolume * 0.6));
        } catch (Exception ignored) {}
    }

    public static void testSFX() {
        playCollision(0.9);
    }

    // ============================================================
    // GETTER & SETTER
    // ============================================================

    public static double getBgmVolume() {
        return bgmVolume;
    }

    public static void setBgmVolume(double volume) {
        bgmVolume = Math.max(0.0, Math.min(1.0, volume));
        if (bgmPlayer != null) {
            bgmPlayer.setVolume(muted ? 0.0 : bgmVolume);
        }
        prefs.putDouble("bgm_volume", bgmVolume);
    }

    public static double getSfxVolume() {
        return sfxVolume;
    }

    public static void setSfxVolume(double volume) {
        sfxVolume = Math.max(0.0, Math.min(1.0, volume));
        prefs.putDouble("sfx_volume", sfxVolume);
    }

    public static boolean isMuted() {
        return muted;
    }

    public static void setMuted(boolean isMuted) {
        muted = isMuted;
        if (bgmPlayer != null) {
            bgmPlayer.setVolume(muted ? 0.0 : bgmVolume);
        }
        prefs.putBoolean("muted", muted);
    }
}


