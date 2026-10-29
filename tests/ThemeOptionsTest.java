import dev.dov.onetint.*;

public final class ThemeOptionsTest {
    static void equal(String actual,String expected){if(!actual.equalsIgnoreCase(expected))throw new AssertionError(actual+" != "+expected);}
    static void rejects(String[] args){try{ThemeOptions.fromArgs(args);throw new AssertionError("Invalid input accepted");}catch(IllegalArgumentException expected){}}
    public static void main(String[] ignored){
        String[] args={"apply","0","app.apk","E9088B","theme-state.json","black","757575","white","171717","FFFFFF","757575"};
        ThemeOptions o=ThemeOptions.fromArgs(args);
        for(String id:new String[]{"4-4-1","4-4-2","4-22-10","4-22-15","4-99-5","4-99-6"})equal(AmoledTheme.theme("QuickPanel",id,"surface",o),"#FF171717");
        for(String id:new String[]{"4-22-1","4-22-2","4-22-3","4-22-20","4-99-3","4-99-4"}){equal(AmoledTheme.theme("QuickPanel",id,"on_surface",o),"#FFFFFFFF");if(AmoledTheme.opacity("QuickPanel",id,70)!=100)throw new AssertionError("Text opacity");}
        equal(AmoledTheme.theme("QuickPanel","4-2-1","on_primary",o),"#FF000000");
        equal(AmoledTheme.theme("Volume","2001-2","on_primary",o),"#FFFFFFFF");
        o.notificationBackground=0xff20383f;o.notificationText=0xffeadbc2;o.notificationIcons=0xffabedc2;o.inactive=0xff343434;o.volumeIconsWhite=false;
        equal(AmoledTheme.theme("QuickPanel","4-4-1","surface",o),"#FF20383F");
        equal(AmoledTheme.theme("QuickPanel","4-22-3","on_surface",o),"#FFEADBC2");
        for(String id:new String[]{"4-22-4","4-22-21","4-99-2"}){equal(AmoledTheme.theme("QuickPanel",id,"on_surface",o),"#FFABEDC2");if(AmoledTheme.opacity("QuickPanel",id,80)!=100)throw new AssertionError("Icon opacity");}
        equal(AmoledTheme.theme("QuickPanel","4-2-4","secondary",o),"#FF343434");
        equal(AmoledTheme.theme("Volume","2001-2","on_primary",o),"#FF000000");
        equal(AmoledTheme.theme("Keyboard","75-5-1","surface",o),"#80171717");
        if(Palette.alpha(0x80171717,100)!=0x80171717)throw new AssertionError("Popup alpha was changed");
        equal(AmoledTheme.theme("Keyboard","75-5-3","on_surface",o),"#FFFFFFFF");
        ThemeOptions snapshot=o.copy();o.notificationText=0xff000000;if(snapshot.notificationText!=0xffeadbc2)throw new AssertionError("Mutable apply snapshot");
        String[] invalid=args.clone();invalid[7]="auto";rejects(invalid);invalid=args.clone();invalid[8]="17171";rejects(invalid);invalid=args.clone();invalid[9]="$(id)";rejects(invalid);rejects(new String[7]);
        System.out.println("PASS: custom notification colors, independent volume icons, opacity, immutable snapshot and root input validation");
    }
}
