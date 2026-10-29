package dev.dov.onetint;
import android.content.Context;
import android.graphics.*;
import android.view.View;
public final class Preview extends View {
    ThemeOptions options=new ThemeOptions();int seed=0xffb49aff,mode=0,mediaLevel=0,mediaMax=0;final Paint paint=new Paint(3);final float density;final android.graphics.drawable.Drawable wifi,volumeIcon;
    public Preview(Context context){super(context);density=getResources().getDisplayMetrics().density;wifi=getResources().getDrawable(getResources().getIdentifier("ic_wifi","drawable",context.getPackageName()),context.getTheme()).mutate();volumeIcon=loadVolume(context);setMode(0);}
    android.graphics.drawable.Drawable loadVolume(Context context){
        try{Context system=context.createPackageContext("com.android.systemui",0);android.content.res.Resources resources=system.getResources();for(String name:new String[]{"tw_ic_audio_media","ic_audio_media","ic_volume_media","ic_volume_media_normal","tw_ic_audio_sound","ic_volume_media_on"}){int id=resources.getIdentifier(name,"drawable","com.android.systemui");if(id!=0)try{return resources.getDrawable(id,system.getTheme()).mutate();}catch(RuntimeException ignored){}}}catch(Exception ignored){}
        return getResources().getDrawable(getResources().getIdentifier("ic_volume_up","drawable",context.getPackageName()),context.getTheme()).mutate();
    }
    public void setMode(int mode){this.mode=mode;setContentDescription(new String[]{"Keyboard preview","Quick Panel preview with six distinct circular buttons","Volume preview"}[mode]);requestLayout();invalidate();}
    protected void onMeasure(int w,int h){setMeasuredDimension(MeasureSpec.getSize(w),Math.round(PreviewScene.height(mode)*density));}
    protected void onDraw(Canvas canvas){canvas.save();canvas.scale(density,density);PreviewScene.draw(new PreviewScene.Draw(){
        void fill(int color){paint.setShader(null);paint.setColor(color);paint.setStyle(Paint.Style.FILL);}
        void stroke(int color,float width){paint.setColor(color);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(width);paint.setStrokeCap(Paint.Cap.ROUND);paint.setStrokeJoin(Paint.Join.ROUND);}
        public void box(float l,float t,float r,float b,float radius,int color){fill(color);canvas.drawRoundRect(l,t,r,b,radius,radius,paint);}
        public void circle(float x,float y,float radius,int color){fill(color);canvas.drawCircle(x,y,radius,paint);}
        public void line(float x,float y,float x2,float y2,float width,int color){stroke(color,width);canvas.drawLine(x,y,x2,y2,paint);}
        public void arc(float l,float t,float r,float b,float start,float sweep,float width,int color){stroke(color,width);canvas.drawArc(l,t,r,b,start,sweep,false,paint);}
        public void polygon(float[] coords,int color){fill(color);Path path=new Path();path.moveTo(coords[0],coords[1]);for(int i=2;i<coords.length;i+=2)path.lineTo(coords[i],coords[i+1]);path.close();canvas.drawPath(path,paint);}
        public void wifi(float cx,float cy,int color){wifi.setTint(color);wifi.setBounds(Math.round(cx-13),Math.round(cy-13),Math.round(cx+13),Math.round(cy+13));wifi.draw(canvas);}
        public void volumeIcon(float cx,float cy,int color){volumeIcon.setTint(color);volumeIcon.setBounds(Math.round(cx-13),Math.round(cy-13),Math.round(cx+13),Math.round(cy+13));volumeIcon.draw(canvas);}
        public void textLeft(String value,float x,float baseline,float size,int color){fill(color);paint.setTextSize(size);paint.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));paint.setTextAlign(Paint.Align.LEFT);canvas.drawText(value,x,baseline,paint);}
        public void text(String value,float cx,float baseline,float size,int color){fill(color);paint.setTextSize(size);paint.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));paint.setTextAlign(Paint.Align.CENTER);canvas.drawText(value,cx,baseline,paint);}
    },getWidth()/density,mode,seed,options,mediaLevel,mediaMax);canvas.restore();}
}
