package dk.discvejr.widget;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

/** Reusable north-up compass: amber arrow points FROM source of wind into center;
 * green arrow points TO direction the disc is thrown. */
public final class CompassArt {
    private CompassArt() {}
    private static final int GREEN=Color.rgb(178,242,106);
    private static final int GOLD=Color.rgb(255,185,105);
    private static final int SOFT=Color.rgb(178,202,190);
    private static final int GRID=Color.rgb(66,94,81);
    public static Bitmap bitmap(double windFrom,int throwTowards,int px) {
        Bitmap b=Bitmap.createBitmap(px,px,Bitmap.Config.ARGB_8888);
        draw(new Canvas(b),px,windFrom,throwTowards);
        return b;
    }
    public static void draw(Canvas canvas,int px,double windFrom,int throwTowards) {
        float cx=px/2f,cy=px/2f,r=px*.335f,s=px/180f;
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(GRID);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2*s);
        canvas.drawCircle(cx,cy,r,p); canvas.drawCircle(cx,cy,r*.70f,p);
        for(int i=0;i<360;i+=45) {
            double a=Math.toRadians(i);
            float x1=cx+(float)Math.sin(a)*r, y1=cy-(float)Math.cos(a)*r;
            float x2=cx+(float)Math.sin(a)*(r+(i%90==0?9:5)*s);
            float y2=cy-(float)Math.cos(a)*(r+(i%90==0?9:5)*s);
            p.setStrokeWidth(i%90==0?2.5f*s:1.3f*s);canvas.drawLine(x1,y1,x2,y2,p);
        }
        p.setStyle(Paint.Style.FILL);p.setTextSize(15*s);p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(android.graphics.Typeface.create("sans-serif-medium",android.graphics.Typeface.BOLD));
        String[] card={"N","Ø","S","V"};
        for(int i=0;i<4;i++) {
            double a=Math.toRadians(i*90);
            p.setColor(i==0?GREEN:SOFT);
            canvas.drawText(card[i],cx+(float)Math.sin(a)*(r+23*s),
                    cy-(float)Math.cos(a)*(r+23*s)+5*s,p);
        }
        drawArrow(canvas,cx,cy,r*.90f,r*.15f,windFrom,GOLD,4.6f*s,true);
        drawArrow(canvas,cx,cy,r*.10f,r*.83f,throwTowards,GREEN,5.0f*s,false);
        p.setColor(Color.rgb(22,33,28));p.setStyle(Paint.Style.FILL);
        canvas.drawCircle(cx,cy,5*s,p);
        p.setColor(GREEN);canvas.drawCircle(cx,cy,2.4f*s,p);
    }
    private static void drawArrow(Canvas c,float cx,float cy,float from,float to,
                                  double bearing,int color,float thickness,boolean incoming) {
        double a=Math.toRadians(bearing);
        float dx=(float)Math.sin(a),dy=-(float)Math.cos(a);
        float x1=cx+dx*from,y1=cy+dy*from,x2=cx+dx*to,y2=cy+dy*to;
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);p.setColor(color);p.setStyle(Paint.Style.STROKE);
        p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeWidth(thickness);
        c.drawLine(x1,y1,x2,y2,p);
        float vx=x2-x1,vy=y2-y1,len=(float)Math.hypot(vx,vy);
        if(len<1) return;
        vx/=len;vy/=len;
        float head=thickness*2.5f,half=thickness*1.45f;
        Path tri=new Path();tri.moveTo(x2+vx*head*.35f,y2+vy*head*.35f);
        tri.lineTo(x2-vx*head+(-vy)*half,y2-vy*head+vx*half);
        tri.lineTo(x2-vx*head-(-vy)*half,y2-vy*head-vx*half);
        tri.close();
        p.setStyle(Paint.Style.FILL);c.drawPath(tri,p);
    }
}
