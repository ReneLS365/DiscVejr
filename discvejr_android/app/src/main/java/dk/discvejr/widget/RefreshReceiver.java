package dk.discvejr.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public final class RefreshReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context ctx, Intent intent) {
        WeatherJobs.refresh(ctx);
    }
}
