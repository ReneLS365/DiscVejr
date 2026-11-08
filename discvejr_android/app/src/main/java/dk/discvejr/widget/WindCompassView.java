package dk.discvejr.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

/** Dynamic in-app compass. Widget uses a bitmap of the same artwork. */
public final class WindCompassView extends View {
    private double windFrom=0;
    private int heading=0;
    public WindCompassView(Context context,AttributeSet attrs) {super(context,attrs);}
    public void setAngles(double windFrom,int heading) {
        this.windFrom=windFrom;
        this.heading=WindAnalysis.normalize(heading);
        invalidate();
    }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int side=Math.min(getWidth(),getHeight());
        if(side<=0)return;
        int saved=canvas.save();
        canvas.translate((getWidth()-side)/2f,(getHeight()-side)/2f);
        CompassArt.draw(canvas,side,windFrom,heading);
        canvas.restoreToCount(saved);
    }
}
