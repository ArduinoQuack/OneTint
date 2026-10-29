import dev.dov.onetint.Palette;
public class PaletteTest {
    static double luminance(int c){double sum=0;double[] w={.2126,.7152,.0722};for(int i=0;i<3;i++){double v=((c>>(16-i*8))&255)/255.;sum+=w[i]*(v<=.04045?v/12.92:Math.pow((v+.055)/1.055,2.4));}return sum;}
    public static void main(String[] args){
        int checked=0;
        for(int rgb=0;rgb<=0xffffff;rgb+=4093){int seed=0xff000000|rgb;int on=Palette.on(seed);double a=luminance(seed),b=luminance(on);double contrast=(Math.max(a,b)+.05)/(Math.min(a,b)+.05);if(contrast<4.4)throw new AssertionError("Poor foreground contrast "+contrast);
            for(boolean dark:new boolean[]{false,true})for(String role:new String[]{"background","surface","primary","secondary","highlight","on_primary","on_secondary","on_background","on_surface","error"}){if(Palette.role(role,seed,dark)>>>24!=255)throw new AssertionError("Nonopaque role");}
            if((Palette.alpha(seed,50)>>>24)!=128)throw new AssertionError("Incorrect alpha");checked++;
        }
        if(Palette.mix(0xff000000,0xffffffff,0)!=0xff000000 || Palette.mix(0xff000000,0xffffffff,1)!=0xffffffff)throw new AssertionError("Mix endpoints");
        System.out.println("PASS: "+checked+" seed colors, foreground contrast, light/dark roles, opacity and mixing");
    }
}
