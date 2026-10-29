package dev.dov.onetint;
import android.content.Context;
import android.graphics.*;
import android.view.*;

/** Hue ring with a saturation/value square: one picker covers the full RGB gamut. */
public final class ColorPicker extends View {
    public interface Listener {void change(int color,boolean finished);}
    Listener listener; Paint paint=new Paint(3);float[] hsv={262,.65f,1};
    float cx,cy,radius,thickness,left,top,side;int mode;
    public ColorPicker(Context c){super(c);setContentDescription("Color picker. Use the hue slider and saturation and brightness sliders below with TalkBack.");setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);setLayerType(LAYER_TYPE_SOFTWARE,null);}
    public void setColor(int color){Color.colorToHSV(color,hsv);invalidate();}
    public int color(){return Color.HSVToColor(hsv);}
    protected void onMeasure(int w,int h){int width=MeasureSpec.getSize(w);setMeasuredDimension(width,width);}
    protected void onSizeChanged(int w,int h,int ow,int oh){cx=w/2f;cy=h/2f;radius=w*.45f;thickness=w*.075f;side=w*.45f;left=cx-side/2;top=cy-side/2;}
    protected void onDraw(Canvas c){
        int[] colors=new int[13];for(int i=0;i<13;i++)colors[i]=Color.HSVToColor(new float[]{i*30,1,1});
        paint.setShader(new SweepGradient(cx,cy,colors,null));paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(thickness);c.drawCircle(cx,cy,radius,paint);paint.setShader(null);paint.setStyle(Paint.Style.FILL);
        paint.setShader(new LinearGradient(left,top,left+side,top,Color.WHITE,Color.HSVToColor(new float[]{hsv[0],1,1}),Shader.TileMode.CLAMP));c.drawRoundRect(left,top,left+side,top+side,12,12,paint);
        paint.setShader(new LinearGradient(left,top,left,top+side,Color.TRANSPARENT,Color.BLACK,Shader.TileMode.CLAMP));c.drawRoundRect(left,top,left+side,top+side,12,12,paint);paint.setShader(null);
        double a=Math.toRadians(hsv[0]);selector(c,cx+radius*(float)Math.cos(a),cy+radius*(float)Math.sin(a),thickness*.58f);selector(c,left+hsv[1]*side,top+(1-hsv[2])*side,thickness*.39f);
    }
    void selector(Canvas c,float x,float y,float r){paint.setStyle(Paint.Style.STROKE);paint.setColor(0xaa000000);paint.setStrokeWidth(7);c.drawCircle(x,y,r,paint);paint.setColor(Color.WHITE);paint.setStrokeWidth(4);c.drawCircle(x,y,r,paint);paint.setStyle(Paint.Style.FILL);}
    public boolean onTouchEvent(android.view.MotionEvent e){
        float x=e.getX(),y=e.getY();int action=e.getActionMasked();
        if(action==MotionEvent.ACTION_DOWN){float d=(float)Math.hypot(x-cx,y-cy);mode=d>radius-thickness*1.3&&d<radius+thickness?1:(x>=left-10&&x<=left+side+10&&y>=top-10&&y<=top+side+10?2:0);if(mode==0)return false;getParent().requestDisallowInterceptTouchEvent(true);}
        if(mode==0)return false;
        if(action!=MotionEvent.ACTION_CANCEL){if(mode==1)hsv[0]=((float)Math.toDegrees(Math.atan2(y-cy,x-cx))+360)%360;else{hsv[1]=Math.max(0,Math.min(1,(x-left)/side));hsv[2]=Math.max(0,Math.min(1,1-(y-top)/side));}invalidate();if(listener!=null)listener.change(color(),action==MotionEvent.ACTION_UP);}
        if(action==MotionEvent.ACTION_UP||action==MotionEvent.ACTION_CANCEL){mode=0;getParent().requestDisallowInterceptTouchEvent(false);if(action==MotionEvent.ACTION_UP)performClick();}return true;
    }
    public boolean performClick(){super.performClick();return true;}
}
