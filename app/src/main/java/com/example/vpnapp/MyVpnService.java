package com.example.vpnapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.net.VpnService;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;

public class MyVpnService extends VpnService {

    private static final String TAG = "MyVpnService";
    private static final String CHANNEL_ID = "vpn_channel";
    private ParcelFileDescriptor vpnInterface;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            if ("START".equals(action)) {
                startForegroundWithNotification();
                connectVpn();
            } else if ("STOP".equals(action)) {
                disconnectVpn();
            }
        }
        return START_STICKY;
    }

    private void connectVpn() {
        try {
            Builder builder = new Builder();
            builder.setSession("My OpenVPN")
                    .addAddress("10.0.0.2", 32)
                    .addDnsServer("8.8.8.8")
                    .addRoute("0.0.0.0", 0)
                    .setMtu(1500);

            vpnInterface = builder.establish();
            Log.d(TAG, "Dummy VPN Started Successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to start VPN", e);
        }
    }

    private void disconnectVpn() {
        if (vpnInterface != null) {
            try { vpnInterface.close(); } catch (Exception ignored) {}
            vpnInterface = null;
        }
        stopForeground(true);
        stopSelf();
    }

    private void startForegroundWithNotification() {
        createNotificationChannel();
        Notification notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("OpenVPN Running")
                .setContentText("Connected")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build();
        startForeground(1, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "VPN Service", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }
}
