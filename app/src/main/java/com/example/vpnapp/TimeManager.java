package com.example.vpnapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.widget.TextView;
import java.util.Locale;

public class TimeManager {
    private static final String PREF_NAME = "vpn_time_prefs";
    private static final String KEY_REMAINING_TIME = "remaining_time_ms";
    
    // เวลาเริ่มต้นเมื่อโหลดแอปครั้งแรก (เช่น 1 ชั่วโมง = 3600000 ms)
    private static final long DEFAULT_INITIAL_TIME = 1 * 60 * 60 * 1000; 

    private SharedPreferences prefs;
    private CountDownTimer countDownTimer;
    private long remainingTimeMs;
    private boolean isRunning = false;
    private TimeListener listener;

    public interface TimeListener {
        void onTick(long millisUntilFinished, String formattedTime);
        void onTimeExpired();
    }

    public TimeManager(Context context, TimeListener listener) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.listener = listener;
        // ดึงเวลาที่บันทึกไว้ หากไม่มีให้ใช้เวลาเริ่มต้น
        this.remainingTimeMs = prefs.getLong(KEY_REMAINING_TIME, DEFAULT_INITIAL_TIME);
    }

    // มีเวลาเหลือให้นับต่อหรือไม่
    public boolean hasValidTime() {
        return remainingTimeMs > 0;
    }

    // ดึงเวลาปัจจุบันที่เหลืออยู่ออกมาดู
    public long getRemainingTimeMs() {
        return remainingTimeMs;
    }

    // เพิ่มเวลาจากการดูโฆษณา (ระบุเป็นมิลลิวินาที เช่น 2 ชั่วโมง = 2 * 60 * 60 * 1000)
    public void addRewardTime(long addedTimeMs) {
        remainingTimeMs += addedTimeMs;
        saveTime(remainingTimeMs);
    }

    // บันทึกเวลาลงเครื่อง
    private void saveTime(long timeMs) {
        prefs.edit().putLong(KEY_REMAINING_TIME, timeMs).apply();
    }

    // เริ่มนับเวลาถอยหลัง (เรียกใช้ตอนเชื่อมต่อ VPN สำเร็จ)
    public void startCounting(final TextView displayTextView) {
        if (isRunning) return;

        countDownTimer = new CountDownTimer(remainingTimeMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingTimeMs = millisUntilFinished;
                saveTime(remainingTimeMs);

                String formatted = formatTime(millisUntilFinished);
                if (displayTextView != null) {
                    displayTextView.setText(formatted);
                }

                if (listener != null) {
                    listener.onTick(millisUntilFinished, formatted);
                }
            }

            @Override
            public void onFinish() {
                remainingTimeMs = 0;
                saveTime(0);
                isRunning = false;

                if (displayTextView != null) {
                    displayTextView.setText("00:00:00");
                }

                if (listener != null) {
                    listener.onTimeExpired();
                }
            }
        }.start();

        isRunning = true;
    }

    // หยุดนับเวลา (เรียกใช้ตอนตัดการเชื่อมต่อ VPN)
    public void stopCounting() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isRunning = false;
        saveTime(remainingTimeMs); // บันทึกเวลาล่าสุดไว้
    }

    public boolean isRunning() {
        return isRunning;
    }

    // แปลงเวลามิลลิวินาทีเป็นข้อความ (HH:mm:ss)
    public static String formatTime(long millis) {
        int hours = (int) (millis / (1000 * 60 * 60));
        int minutes = (int) ((millis % (1000 * 60 * 60)) / (1000 * 60));
        int seconds = (int) ((millis % (1000 * 60)) / 1000);
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }
}
