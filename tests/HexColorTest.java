import dev.dov.onetint.*;

public final class HexColorTest {
    static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
    public static void main(String[] args){
        check(ThemeOptions.rgb("86ABF2")==0xff86abf2,"RGB must be opaque");
        check(ThemeOptions.rgb("8086ABF2")==0x8086abf2,"Reported ARGB rejected or alpha lost");
        check(ThemeOptions.rgb(" #8086abf2 ")==0x8086abf2,"Optional hash, whitespace and lowercase");
        check(ThemeOptions.rgb("0086ABF2")==0x0086abf2,"Zero alpha must stay transparent");
        check(ThemeOptions.hex(0x8086abf2).equals("#8086ABF2"),"Display loses alpha");
        check(Palette.alpha(0x8086abf2,50)==0x4086abf2,"Catalog opacity must multiply alpha");
        check(Palette.over(0x8086abf2,0xff000000)==0xff435679,"Translucent preview mismatch");
        check(Palette.on(0x8086abf2)==0xffffffff,"Contrast must use visible color");
        for(int alpha=0;alpha<=255;alpha++){
            int color=(alpha<<24)|0x86abf2;
            check(ThemeOptions.rgb(ThemeOptions.wire(color))==color,"Wire roundtrip "+alpha);
            String[] request={"apply","0","app.apk",ThemeOptions.wire(color),"theme-state.json","white",ThemeOptions.wire(color),"black",ThemeOptions.wire(color),ThemeOptions.wire(color),ThemeOptions.wire(color)};
            ThemeOptions o=ThemeOptions.fromArgs(request);
            check(o.inactive==color&&o.notificationBackground==color&&o.notificationText==color&&o.notificationIcons==color,"Root args lose alpha");
            check(AmoledTheme.theme("QuickPanel","4-2-4","secondary",o).equals(ThemeOptions.hex(color)),"Overlay color loses alpha");
        }
        for(String bad:new String[]{"","#","ABC","1234567","123456789","GG86ABF2","##8086ABF2","80 86ABF2","$(id)"}){
            try{ThemeOptions.rgb(bad);throw new AssertionError("Accepted "+bad);}catch(IllegalArgumentException expected){}
        }
        System.out.println("PASS: RGB/ARGB input, all 256 alpha values, root wire format, opacity and preview compositing");
    }
}
