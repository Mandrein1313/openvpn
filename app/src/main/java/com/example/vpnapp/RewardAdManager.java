package com.example.vpnapp;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

public class RewardAdManager {
    // ไอดีโฆษณาทดสอบของ Google (เมื่อปล่อยแอปจริงให้เปลี่ยนเป็น Ad Unit ID ของคุณ)
    private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"; 
    
    private RewardedAd rewardedAd;
    private boolean isLoading = false;

    public interface AdRewardCallback {
        void onUserEarnedReward();
        void onAdFailedToLoad(String errorReason);
    }

    // โหลดโฆษณาเตรียมไว้
    public void loadAd(Context context) {
        if (isLoading || rewardedAd != null) return;

        isLoading = true;
        AdRequest adRequest = new AdRequest.Builder().build();

        RewardedAd.load(context, AD_UNIT_ID, adRequest, new RewardedAdLoadCallback() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                rewardedAd = null;
                isLoading = false;
            }

            @Override
            public void onAdLoaded(@NonNull RewardedAd ad) {
                rewardedAd = ad;
                isLoading = false;
            }
        });
    }

    // แสดงโฆษณา
    public void showAd(Activity activity, final AdRewardCallback callback) {
        if (rewardedAd != null) {
            rewardedAd.show(activity, rewardItem -> {
                // ผู้ใช้ดูโฆษณาจบ
                if (callback != null) {
                    callback.onUserEarnedReward();
                }
                rewardedAd = null;
                loadAd(activity); // โหลดโฆษณาอันถัดไปเตรียมไว้
            });
        } else {
            if (callback != null) {
                callback.onAdFailedToLoad("โฆษณายังไม่พร้อมใช้งาน กรุณาลองใหม่อีกครั้ง");
            }
            loadAd(activity); // สั่งโหลดใหม่ถ้ายังไม่มี
        }
    }
}
