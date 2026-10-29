import dev.dov.onetint.*;
public final class VolumePreviewTest implements PreviewScene.Draw {
    int accent=0xffe9088b;float filledWidth=-1;String number="";int icon;
    public void box(float l,float t,float r,float b,float radius,int c){if(c==accent)filledWidth=r-l;}
    public void circle(float x,float y,float r,int c){} public void line(float x,float y,float x2,float y2,float width,int c){}
    public void arc(float l,float t,float r,float b,float start,float sweep,float width,int c){} public void polygon(float[] p,int c){}
    public void text(String s,float x,float y,float size,int c){number=s;} public void textLeft(String s,float x,float y,float size,int c){}
    public void wifi(float x,float y,int c){} public void volumeIcon(float x,float y,int c){icon=c;}
    static void check(int level,int max,String expected,float fraction,boolean white){VolumePreviewTest d=new VolumePreviewTest();ThemeOptions options=new ThemeOptions();options.volumeIconsWhite=white;PreviewScene.draw(d,312,2,d.accent,options,level,max);if(!d.number.equals(expected))throw new AssertionError(d.number);if(fraction==0?d.filledWidth!=-1:Math.abs(d.filledWidth-264*fraction)>.01)throw new AssertionError("Fill disagrees with system index");if(d.icon!=(white?0xffffffff:0xff000000))throw new AssertionError("Icon choice lost");}
    public static void main(String[] args){check(0,15,"0 / 15",0,true);check(8,15,"8 / 15",8/15f,false);check(15,15,"15 / 15",1,true);check(50,150,"50 / 150",1/3f,false);check(20,15,"15 / 15",1,true);check(0,0,"—",0,true);System.out.println("PASS: system-index label and fill agree at zero, middle and maximum; manual icon colors preserved");}
}
