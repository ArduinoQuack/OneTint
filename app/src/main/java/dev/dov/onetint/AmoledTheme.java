package dev.dov.onetint;
/** Fixed neutral surfaces; the chosen color is reserved for interactive accents. */
public final class AmoledTheme {
    public static String theme(String component,String uid,String original,ThemeOptions options) {
        if(component.equals("Keyboard")) {
            if(uid.equals("75-5-1"))return "#80171717";
            if(uid.equals("75-5-3"))return "#FFFFFFFF";
        }
        if(component.equals("QuickPanel")) {
            if(uid.equals("4-1-2")||uid.equals("4-2-6"))return "background";
            if(uid.equals("4-2-4"))return hex(options.inactive);
            if(notificationBackground(uid))return hex(options.notificationBackground);
            if(notificationIcon(uid))return hex(options.notificationIcons);
            if((uid.startsWith("4-22-")||uid.startsWith("4-99-"))&&original.equals("on_surface"))return hex(options.notificationText);
            if(uid.equals("4-2-1")||uid.equals("4-2-2"))return options.quickIconsWhite?"#ffffffff":"#ff000000";
        }
        if(component.equals("Volume")) {
            if(uid.equals("2001-1"))return "primary";
            if(uid.equals("2001-2"))return options.volumeIconsWhite?"#FFFFFFFF":"#FF000000";
            if(uid.equals("2001-3"))return "surface";
        }
        return original;
    }
    static String hex(int color){return String.format(java.util.Locale.US,"#%08X",color);}
    static boolean notificationIcon(String uid){return uid.equals("4-22-4")||uid.equals("4-22-21")||uid.equals("4-99-2");}
    static boolean notificationText(String uid){return uid.equals("4-22-1")||uid.equals("4-22-2")||uid.equals("4-22-3")||uid.equals("4-22-5")||uid.equals("4-22-9")||uid.equals("4-22-11")||uid.equals("4-22-20")||uid.equals("4-99-3")||uid.equals("4-99-4");}
    static boolean notificationBackground(String uid){return uid.equals("4-4-1")||uid.equals("4-4-2")||uid.equals("4-22-10")||uid.equals("4-22-15")||uid.equals("4-99-5")||uid.equals("4-99-6");}
    public static int opacity(String component,String uid,int original) {
        if(component.equals("QuickPanel")&&(notificationBackground(uid)||notificationIcon(uid)||notificationText(uid)))return 100;
        if(component.equals("QuickPanel")&&(uid.equals("4-1-2")||uid.equals("4-2-6")||uid.equals("4-2-4")))return 100;
        return original;
    }
}
