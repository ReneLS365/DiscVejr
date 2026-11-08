package dk.discvejr.widget;

import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.appwidget.AppWidgetManager;

public final class DiscWidget extends AppWidgetProvider {
    @Override public void onUpdate(Context ctx, AppWidgetManager manager, int[] ids) {
        for (int id : ids) WidgetRenderer.updateOne(ctx, manager, id);
        WeatherJobs.refresh(ctx);
    }

    @Override public void onEnabled(Context ctx) {
        WeatherJobs.refresh(ctx);
    }
}
