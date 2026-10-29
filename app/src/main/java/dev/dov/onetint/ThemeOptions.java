package dev.dov.onetint;

/** User choices shared by the preview and the root resource mapping. */
public final class ThemeOptions {
    public boolean quickIconsWhite=true,volumeIconsWhite=true;
    public int inactive=0xff757575,notificationBackground=0xff171717,
        notificationText=0xffffffff,notificationIcons=0xff757575;
    public ThemeOptions copy(){ThemeOptions o=new ThemeOptions();o.quickIconsWhite=quickIconsWhite;o.volumeIconsWhite=volumeIconsWhite;o.inactive=inactive;o.notificationBackground=notificationBackground;o.notificationText=notificationText;o.notificationIcons=notificationIcons;return o;}
    public static int rgb(String value){if(!value.matches("[0-9a-fA-F]{6}"))throw new IllegalArgumentException("Expected six hex digits");return (int)(0xff000000L|Long.parseLong(value,16));}
    public static boolean white(String value){if(!value.equals("white")&&!value.equals("black"))throw new IllegalArgumentException("Expected white or black");return value.equals("white");}
    public static ThemeOptions fromArgs(String[] args){if(args.length!=11)throw new IllegalArgumentException("Expected theme color options");ThemeOptions o=new ThemeOptions();o.quickIconsWhite=white(args[5]);o.inactive=rgb(args[6]);o.volumeIconsWhite=white(args[7]);o.notificationBackground=rgb(args[8]);o.notificationText=rgb(args[9]);o.notificationIcons=rgb(args[10]);return o;}
}
