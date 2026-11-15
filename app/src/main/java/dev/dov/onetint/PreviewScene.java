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
    public static int height(int mode){return mode==0?194:mode==1?256:230;}
    public static void draw(Draw d,float w,int mode,int seed,ThemeOptions options,int mediaLevel,int mediaMax){
        d.box(0,0,w,height(mode),20,0xff000000);
        if(mode==0)keyboard(d,w,seed,options);else if(mode==1)quickPanel(d,w,seed,options);else volume(d,w,seed,options,mediaLevel,mediaMax);
    }
    static void key(Draw d,float x,float y,float width,float height,String letter,int bg,int ink){d.box(x,y,x+width,y+height,5,bg);d.text(letter,x+width/2,y+height/2+4,11,ink);}
    static int mapped(ThemeOptions o,String group,String uid,int fallback){Integer c=o.override(group,uid);return c==null?fallback:c;}
    static void keyboard(Draw d,float w,int seed,ThemeOptions options){
        d.box(0,0,w,194,20,mapped(options,"Keyboard","75-1-1",0xff000000));
        int WHITE=mapped(options,"Keyboard","75-3-2",0xfff5f4fa);
        float gap=3,k=(w-16-9*gap)/10,h=30,pressedX=w/2;int key=mapped(options,"Keyboard","75-2-2",0x80181818);
        for(int i=0;i<10;i++)key(d,8+i*(k+gap),12,k,24,"1234567890".substring(i,i+1),mapped(options,"Keyboard","75-2-1",0x80181818),WHITE);
        String[] rows={"qwertyuiop","asdfghjkl"};
        for(int row=0;row<2;row++){String letters=rows[row];float start=(w-(letters.length()*k+(letters.length()-1)*gap))/2;for(int i=0;i<letters.length();i++){
            float x=start+i*(k+gap);boolean pressed=row==1&&i==4;int fill=pressed?mapped(options,"Keyboard","75-2-4",Palette.alpha(seed,50)):key;
            key(d,x,42+row*36,k,h,letters.substring(i,i+1),fill,WHITE);if(pressed)pressedX=x+k/2;
        }}
        float shift=k*1.4f,lettersWidth=7*k+6*gap,total=shift*2+lettersWidth+2*gap,start=(w-total)/2;
        key(d,start,114,shift,h,"",key,WHITE);float cx=start+shift/2,cy=129;
        float[] arrow={cx,cy-8,cx-7,cy,cx-3,cy,cx-3,cy+6,cx+3,cy+6,cx+3,cy,cx+7,cy,cx,cy-8};for(int i=0;i<arrow.length-2;i+=2)d.line(arrow[i],arrow[i+1],arrow[i+2],arrow[i+3],1.5f,WHITE);
        for(int i=0;i<7;i++)key(d,start+shift+gap+i*(k+gap),114,k,h,"zxcvbnm".substring(i,i+1),key,WHITE);
        float right=start+shift+gap+lettersWidth+gap;key(d,right,114,shift,h,"",key,WHITE);cx=right+shift/2;cy=129;
        d.line(cx-7,cy,cx-2,cy-5,1.4f,WHITE);d.line(cx-7,cy,cx-2,cy+5,1.4f,WHITE);d.line(cx-2,cy-5,cx+7,cy-5,1.4f,WHITE);d.line(cx+7,cy-5,cx+7,cy+5,1.4f,WHITE);d.line(cx+7,cy+5,cx-2,cy+5,1.4f,WHITE);d.line(cx,cy-2,cx+4,cy+2,1.3f,WHITE);d.line(cx+4,cy-2,cx,cy+2,1.3f,WHITE);
        float x=8,a=k*1.2f,b=k,c=k*.85f,search=k*1.25f,space=w-16-a-b-c*2-search-5*gap;
        key(d,x,150,a,h,"!#1",key,WHITE);x+=a+gap;key(d,x,150,b,h,"",key,WHITE);cx=x+b/2;cy=165;d.arc(cx-6,cy-6,cx+6,cy+6,0,360,1.2f,WHITE);d.arc(cx-3,cy-6,cx+3,cy+6,0,360,1,WHITE);d.line(cx-6,cy,cx+6,cy,1,WHITE);
        x+=b+gap;key(d,x,150,c,h,"/",key,WHITE);x+=c+gap;key(d,x,150,space,h,"EN(US)",key,WHITE);x+=space+gap;key(d,x,150,c,h,".",key,WHITE);x+=c+gap;key(d,x,150,search,h,"",key,WHITE);cx=x+search/2;cy=164;d.arc(cx-5,cy-5,cx+5,cy+5,0,360,1.4f,seed);d.line(cx+3.8f,cy+3.8f,cx+8,cy+8,1.6f,seed);
        // Fill the translucent popup as one shape, including its neck.
        java.util.ArrayList<Float> outline=new java.util.ArrayList<>();
        popupCorner(outline,pressedX-11,29,180);popupCorner(outline,pressedX+11,29,270);popupCorner(outline,pressedX+11,62,0);
        outline.add(pressedX+9);outline.add(80f);outline.add(pressedX-9);outline.add(80f);popupCorner(outline,pressedX-11,62,90);
        float[] points=new float[outline.size()];for(int i=0;i<points.length;i++)points[i]=outline.get(i);d.polygon(points,mapped(options,"Keyboard","75-1-1",0xff000000));d.polygon(points,mapped(options,"Keyboard","75-5-1",0x80171717));d.text("g",pressedX,54,26,mapped(options,"Keyboard","75-5-3",0xffffffff));
    }
    static void popupCorner(java.util.List<Float> points,float x,float y,int start){for(int i=0;i<=8;i++){double angle=Math.toRadians(start+i*90/8f);points.add(x+10*(float)Math.cos(angle));points.add(y+10*(float)Math.sin(angle));}}
    static void quickPanel(Draw d,float w,int seed,ThemeOptions options){
        int background=Palette.over(mapped(options,"QuickPanel","4-1-2",0xff000000),0xff000000);d.box(0,0,w,256,20,background);
        int active=Palette.over(mapped(options,"QuickPanel","4-2-3",seed),background),off=Palette.over(mapped(options,"QuickPanel","4-2-4",options.inactive),background),ink=mapped(options,"QuickPanel","4-2-1",options.quickIconsWhite?0xffffffff:0xff000000),offInk=mapped(options,"QuickPanel","4-2-2",options.quickIconsWhite?0xffffffff:0xff000000);
        int container=Palette.over(mapped(options,"QuickPanel","4-2-6",0xff000000),background);float gap=8,half=(w-24-gap)/2;
        for(int i=0;i<2;i++){float left=8+i*(half+gap);outline(d,left,8,left+half,70,31,container);d.circle(left+26,39,20,active);glyph(d,i,left+26,39,ink,active);d.textLeft(i==0?"Wi-Fi":"Bluetooth",left+52,35,10.5f,WHITE);d.textLeft("Connected",left+52,51,8,MUTED);}
        outline(d,8,79,w-8,157,26,container);String[] names={"Rotate","Airplane","Flashlight","Sound"};int[] kinds={6,2,3,7};
        for(int i=0;i<4;i++){float x=8+(w-16)/4*(i+.5f);boolean on=i==0;int fill=on?active:off;d.circle(x,109,19,fill);glyph(d,kinds[i],x,109,on?ink:offInk,fill);d.text(names[i],x,143,8.5f,WHITE);}
        notification(d,w,174,options);
    }
    static void outline(Draw d,float l,float t,float r,float b,float radius,int fill){d.box(l,t,r,b,radius,0xff191919);d.box(l+1,t+1,r-1,b-1,radius-1,fill);}

    static void notification(Draw d,float w,float y,ThemeOptions options){
        int ink=mapped(options,"QuickPanel","4-22-2",options.notificationText),fill=Palette.over(mapped(options,"QuickPanel","4-4-1",options.notificationBackground),mapped(options,"QuickPanel","4-1-2",0xff000000)),icon=Palette.over(mapped(options,"QuickPanel","4-99-2",options.notificationIcons),fill),iconInk=Palette.on(icon);
        d.box(8,y,w-8,y+70,35,fill);d.circle(38,y+35,20,icon);
        d.box(27,y+26,49,y+41,5,iconInk);d.polygon(new float[]{31,y+38,31,y+45,38,y+38},iconInk);for(int i=0;i<3;i++)d.circle(32+i*6,y+33,1,icon);
        d.textLeft("Messages",70,y+21,11,ink);d.textLeft("7:20 AM",130,y+21,8,mapped(options,"QuickPanel","4-22-3",options.notificationText));
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
        else if(kind==6){d.arc(x-10,y-10,x+10,y+10,35,265,2.2f,ink);d.polygon(new float[]{x+5,y-12,x+12,y-9,x+6,y-5},ink);d.box(x-5,y-7,x+5,y+7,2,ink);d.box(x-3,y-5,x+3,y+5,1,fill);}
        else if(kind==7){d.volumeIcon(x,y,ink);}
        else{d.circle(x,y,10,ink);d.box(x-6,y-1.5f,x+6,y+1.5f,1,fill);}
    }
    static void volume(Draw d,float w,int seed,ThemeOptions options,int level,int max){
        level=Math.max(0,Math.min(level,max));float fraction=max>0?(float)level/max:0;
        float x=w/2,top=10,bottom=220,trackHeight=bottom-top;
        d.box(x-21,top,x+21,bottom,21,mapped(options,"Volume","2001-3",0x80181818));
        if(fraction>0){float fillHeight=trackHeight*fraction;d.box(x-21,bottom-fillHeight,x+21,bottom,Math.min(21,fillHeight/2),mapped(options,"Volume","2001-1",seed));}
        int labels=mapped(options,"Volume","2001-5",WHITE);for(int i=-1;i<=1;i++)d.circle(x+i*6,30,1.7f,labels);
        d.text(max>0?Integer.toString(level):"—",x,48,11,labels);
        d.volumeIcon(x,bottom-22,mapped(options,"Volume","2001-2",options.volumeIconsWhite?0xffffffff:0xff000000));
    }
}
