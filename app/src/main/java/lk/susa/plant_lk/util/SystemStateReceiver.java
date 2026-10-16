package lk.susa.plant_lk.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.provider.Settings;

public class SystemStateReceiver extends BroadcastReceiver {

    public static final String ACTION_ORDER_PLACED = "lk.susa.plant_lk.ACTION_ORDER_PLACED";
    public static final String EXTRA_ORDER_ID = "orderId";

    public static IntentFilter buildFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        filter.addAction(Intent.ACTION_BATTERY_LOW);
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(ACTION_ORDER_PLACED);
        return filter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;

        switch (intent.getAction()) {
            case Intent.ACTION_AIRPLANE_MODE_CHANGED:
                boolean on = Settings.Global.getInt(context.getContentResolver(),
                        Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
                NotificationHelper.showInfoNotification(context, 10,
                        on ? "Airplane mode on" : "Airplane mode off",
                        on ? "Orders and live updates need an internet connection."
                           : "Connection restored. You can continue shopping.");
                break;

            case Intent.ACTION_BATTERY_LOW:
                NotificationHelper.showInfoNotification(context, 11,
                        "Battery low", "Complete your order soon or plug in your charger.");
                break;

            case Intent.ACTION_POWER_CONNECTED:
                NotificationHelper.showInfoNotification(context, 12,
                        "Charger connected", "Your device is charging.");
                break;

            case ACTION_ORDER_PLACED:
                String orderId = intent.getStringExtra(EXTRA_ORDER_ID);
                NotificationHelper.showInfoNotification(context, 13,
                        "Order placed",
                        "Thank you! Your order " + (orderId != null ? orderId : "") + " is confirmed.");
                break;
        }
    }

    public static int getBatteryPercent(Context context) {
        Intent i = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (i == null) return -1;
        int level = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = i.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        return (level < 0 || scale <= 0) ? -1 : (int) (level * 100f / scale);
    }
}
