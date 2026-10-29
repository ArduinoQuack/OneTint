package dev.dov.onetint;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.*;
import android.view.*;
import android.view.accessibility.AccessibilityNodeInfo;

/** Two-position color control. Tap either label or slide the selection. */
public final class IconColorSlider extends View {
    public interface Listener {void changed(boolean white);}
    Listener listener;boolean white;float position,downX,downY;boolean dragging;ValueAnimator animator;
    final Paint paint=new Paint(3);final float density;
    public IconColorSlider(Context c){super(c);density=getResources().getDisplayMetrics().density;setFocusable(true);setClickable(true);setMinimumHeight(Math.round(48*density));}
    void setWhite(boolean value){white=value;position=value?1:0;setStateDescription(value?"White":"Black");invalidate();}
    void choose(boolean value){if(animator!=null)animator.cancel();boolean changed=white!=value;white=value;setStateDescription(value?"White":"Black");animator=ValueAnimator.ofFloat(position,value?1:0);animator.setDuration(160);animator.addUpdateListener(a->{position=(Float)a.getAnimatedValue();invalidate();});animator.start();if(changed&&listener!=null)listener.changed(value);}
    protected void onDraw(Canvas c){float w=getWidth(),h=getHeight(),in=4*density,half=(w-2*in)/2;c.save();c.translate(0,(h-40*density)/2);h=40*density;paint.setColor(0xff24262e);c.drawRoundRect(0,0,w,h,13*density,13*density,paint);paint.setColor(0xff444752);float l=in+half*position;c.drawRoundRect(l,in,l+half,h-in,10*density,10*density,paint);paint.setTypeface(Typeface.create("sans-serif-medium",0));paint.setTextSize(12*density);paint.setTextAlign(Paint.Align.CENTER);for(int i=0;i<2;i++){paint.setColor((i==1)==white?0xffffffff:0xffb7b8c2);c.drawText(i==0?"Black":"White",in+half*(i+.5f),h/2-(paint.ascent()+paint.descent())/2,paint);}c.restore();}
    public boolean onTouchEvent(android.view.MotionEvent e){if(!isEnabled())return false;switch(e.getActionMasked()){case MotionEvent.ACTION_DOWN:downX=e.getX();downY=e.getY();dragging=false;return true;case MotionEvent.ACTION_MOVE:if(Math.abs(e.getX()-downX)>8*density&&Math.abs(e.getX()-downX)>Math.abs(e.getY()-downY)){dragging=true;getParent().requestDisallowInterceptTouchEvent(true);}if(dragging){if(animator!=null)animator.cancel();position=Math.max(0,Math.min(1,(e.getX()-getWidth()/4f)/(getWidth()/2f)));invalidate();}return true;case MotionEvent.ACTION_UP:choose(dragging?position>=.5f:e.getX()>=getWidth()/2f);getParent().requestDisallowInterceptTouchEvent(false);return true;case MotionEvent.ACTION_CANCEL:setWhite(white);return true;}return super.onTouchEvent(e);}
    protected void onDetachedFromWindow(){if(animator!=null)animator.cancel();super.onDetachedFromWindow();}
    public boolean performClick(){super.performClick();if(isEnabled())choose(!white);return true;}
    public boolean onKeyDown(int key,android.view.KeyEvent e){if(isEnabled()&&(key==KeyEvent.KEYCODE_DPAD_LEFT||key==KeyEvent.KEYCODE_DPAD_RIGHT)){choose(key==KeyEvent.KEYCODE_DPAD_RIGHT);return true;}return super.onKeyDown(key,e);}
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info){super.onInitializeAccessibilityNodeInfo(info);info.setClassName("android.widget.Switch");info.setCheckable(true);info.setChecked(white);}
    public void setEnabled(boolean enabled){super.setEnabled(enabled);setAlpha(enabled?1:.45f);}
}
