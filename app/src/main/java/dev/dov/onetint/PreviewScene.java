package dev.dov.onetint;

/** Shared geometry for the native previews and development renders. */
public final class PreviewScene {
    public interface Draw {
        void box(float l,float t,float r,float b,float radius,int color);
        void circle(float x,float y,float radius,int color);
        void line(float x,float y,float x2,float y2,float width,int color);
        void arc(float l,float t,float r,float b,float start,float sweep,float width,int color);
        void polygon(float[] points,int color);
        void text(String value,float cx,float baseline,float size,int color);
        void textLeft(String value,float x,float baseline,float size,int color);
        void wifi(float cx,float cy,int color);
        void volumeIcon(float cx,float cy,int color);
    }
    static final int KEY=0xff181818,WHITE=0xfff5f4fa,MUTED=0xffaaaab2,OFF=0xff757575;
    public static int height(int mode){return mode==0?194:mode==1?256:128;}
    public static void draw(Draw d,float w,int mode,int seed,ThemeOptions options,int mediaLevel,int mediaMax){
        d.box(0,0,w,height(mode),20,0xff000000);
        if(mode==0)keyboard(d,w,seed);else if(mode==1)quickPanel(d,w,seed,options);else volume(d,w,seed,options,mediaLevel,mediaMax);
    }
    static void key(Draw d,float x,float y,float width,float height,String letter,int bg,int ink){d.box(x,y,x+width,y+height,5,bg);d.text(letter,x+width/2,y+height/2+4,11,ink);}
    static void keyboard(Draw d,float w,int seed){
        float gap=3,k=(w-16-9*gap)/10,h=30,pressedX=w/2;int key=0xff0c0c0c;
        for(int i=0;i<10;i++)key(d,8+i*(k+gap),12,k,24,"1234567890".substring(i,i+1),key,WHITE);
        String[] rows={"qwertyuiop","asdfghjkl"};
        for(int row=0;row<2;row++){String letters=rows[row];float start=(w-(letters.length()*k+(letters.length()-1)*gap))/2;for(int i=0;i<letters.length();i++){
            float x=start+i*(k+gap);boolean pressed=row==1&&i==4;int fill=pressed?Palette.mix(seed,0xff000000,.5f):key;
            key(d,x,42+row*36,k,h,letters.substring(i,i+1),fill,pressed?Palette.on(fill):WHITE);if(pressed)pressedX=x+k/2;
        }}
        float shift=k*1.4f,lettersWidth=7*k+6*gap,total=shift*2+lettersWidth+2*gap,start=(w-total)/2;
        key(d,start,114,shift,h,"",key,WHITE);float cx=start+shift/2,cy=129;
        float[] arrow={cx,cy-8,cx-7,cy,cx-3,cy,cx-3,cy+6,cx+3,cy+6,cx+3,cy,cx+7,cy,cx,cy-8};for(int i=0;i<arrow.length-2;i+=2)d.line(arrow[i],arrow[i+1],arrow[i+2],arrow[i+3],1.5f,WHITE);
        for(int i=0;i<7;i++)key(d,start+shift+gap+i*(k+gap),114,k,h,"zxcvbnm".substring(i,i+1),key,WHITE);
        float right=start+shift+gap+lettersWidth+gap;key(d,right,114,shift,h,"",key,WHITE);cx=right+shift/2;cy=129;
        d.line(cx-7,cy,cx-2,cy-5,1.4f,WHITE);d.line(cx-7,cy,cx-2,cy+5,1.4f,WHITE);d.line(cx-2,cy-5,cx+7,cy-5,1.4f,WHITE);d.line(cx+7,cy-5,cx+7,cy+5,1.4f,WHITE);d.line(cx+7,cy+5,cx-2,cy+5,1.4f,WHITE);d.line(cx,cy-2,cx+4,cy+2,1.3f,WHITE);d.line(cx+4,cy-2,cx,cy+2,1.3f,WHITE);
        float x=8,a=k*1.2f,b=k,c=k*.85f,search=k*1.25f,space=w-16-a-b-c*2-search-5*gap;
        key(d,x,150,a,h,"!#1",key,WHITE);x+=a+gap;key(d,x,150,b,h,"",key,WHITE);cx=x+b/2;cy=165;d.circle(cx,cy,6,WHITE);d.circle(cx,cy,4.9f,key);d.arc(cx-3,cy-6,cx+3,cy+6,0,360,1,WHITE);d.line(cx-6,cy,cx+6,cy,1,WHITE);
        x+=b+gap;key(d,x,150,c,h,"/",key,WHITE);x+=c+gap;key(d,x,150,space,h,"EN(US)",key,WHITE);x+=space+gap;key(d,x,150,c,h,".",key,WHITE);x+=c+gap;key(d,x,150,search,h,"",key,WHITE);cx=x+search/2;cy=164;d.circle(cx,cy,5,seed);d.circle(cx,cy,3.6f,key);d.line(cx+3.8f,cy+3.8f,cx+8,cy+8,1.6f,seed);
        // Fill the translucent popup as one shape, including its neck.
        java.util.ArrayList<Float> outline=new java.util.ArrayList<>();
        popupCorner(outline,pressedX-11,29,180);popupCorner(outline,pressedX+11,29,270);popupCorner(outline,pressedX+11,62,0);
        outline.add(pressedX+9);outline.add(80f);outline.add(pressedX-9);outline.add(80f);popupCorner(outline,pressedX-11,62,90);
        float[] points=new float[outline.size()];for(int i=0;i<points.length;i++)points[i]=outline.get(i);d.polygon(points,0x80171717);d.text("g",pressedX,54,26,0xffffffff);
    }
    static void popupCorner(java.util.List<Float> points,float x,float y,int start){for(int i=0;i<=8;i++){double angle=Math.toRadians(start+i*90/8f);points.add(x+10*(float)Math.cos(angle));points.add(y+10*(float)Math.sin(angle));}}
    static void quickPanel(Draw d,float w,int seed,ThemeOptions options){
        String[] labels={"Wi-Fi","Bluetooth","Airplane","Flashlight","Location","Do not disturb"};boolean[] active={true,true,false,false,true,false};
        float cell=(w-24)/3;
        for(int i=0;i<6;i++){float x=12+cell*(i%3+.5f),y=38+(i/3)*78;int fill=active[i]?seed:options.inactive;d.circle(x,y,23,fill);glyph(d,i,x,y,options.quickIconsWhite?0xffffffff:0xff000000,fill);d.text(labels[i],x,y+39,10,WHITE);}
        notification(d,w,174,options);
    }
    static void notification(Draw d,float w,float y,ThemeOptions options){
        int ink=options.notificationText,fill=options.notificationBackground,icon=options.notificationIcons,iconInk=Palette.on(icon);
        d.box(8,y,w-8,y+70,35,fill);d.circle(38,y+35,20,icon);
        d.box(27,y+26,49,y+41,5,iconInk);d.polygon(new float[]{31,y+38,31,y+45,38,y+38},iconInk);for(int i=0;i<3;i++)d.circle(32+i*6,y+33,1,icon);
        d.textLeft("Messages",70,y+21,11,ink);d.textLeft("7:20 AM",130,y+21,8,ink);
        d.textLeft("Hey, what's your ETA?",70,y+39,10.5f,ink);d.textLeft("We're going to order a pizza.",70,y+54,10.5f,ink);
        d.line(w-32,y+32,w-27,y+37,1.7f,options.notificationIcons);d.line(w-27,y+37,w-22,y+32,1.7f,options.notificationIcons);
    }
    /** A white or black symbol is drawn inside the circle; its fill never changes here. */
    public static void glyph(Draw d,int kind,float x,float y,int ink,int fill){
        if(kind==0){d.wifi(x,y,ink);}
        else if(kind==1){d.line(x,y-11,x,y+11,2.6f,ink);d.line(x,y-11,x+7,y-5,2.6f,ink);d.line(x+7,y-5,x-7,y+6,2.6f,ink);d.line(x-7,y-6,x+7,y+5,2.6f,ink);d.line(x+7,y+5,x,y+11,2.6f,ink);}
        else if(kind==2){d.polygon(new float[]{x-2,y-11,x+2,y-11,x+3,y-2,x+11,y+3,x+11,y+6,x+3,y+3,x+2,y+9,x+5,y+11,x+5,y+13,x,y+11,x-5,y+13,x-5,y+11,x-2,y+9,x-3,y+3,x-11,y+6,x-11,y+3,x-3,y-2},ink);}
        else if(kind==3){d.box(x-6,y-8,x+6,y-3,1,ink);d.polygon(new float[]{x-6,y-2,x+6,y-2,x+3,y+3,x+3,y+11,x-3,y+11,x-3,y+3},ink);d.circle(x,y+5,1,fill);for(int j=-1;j<=1;j++)d.circle(x+j*5,y-12,1,ink);}
        else if(kind==4){d.circle(x,y-3,8,ink);d.polygon(new float[]{x-7,y+1,x+7,y+1,x,y+12},ink);d.circle(x,y-3,3,fill);}
        else{d.circle(x,y,10,ink);d.box(x-6,y-1.5f,x+6,y+1.5f,1,fill);}
    }
    static void volume(Draw d,float w,int seed,ThemeOptions options,int level,int max){
        level=Math.max(0,Math.min(level,max));float fraction=max>0?(float)level/max:0;
        d.box(10,8,w-10,120,18,0xff151515);d.textLeft("Media volume",24,34,13,WHITE);d.text(max>0?level+" / "+max:"—",w-48,34,11,WHITE);
        float l=24,r=w-24,progress=l+(r-l)*fraction;d.box(l,55,r,101,23,0xff333333);
        if(fraction>0)d.box(l,55,progress,101,Math.min(23,(progress-l)/2),seed);
        d.volumeIcon(48,78,options.volumeIconsWhite?0xffffffff:0xff000000);
    }
}
