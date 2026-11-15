package dev.dov.onetint;

/** User choices shared by the preview and the root resource mapping. */
public final class ThemeOptions {
    public final java.util.Map<String,Integer> overrides=new java.util.LinkedHashMap<>();
    public Integer override(String group,String uid){return overrides.get(group+":"+uid);}
    public boolean quickIconsWhite=true,volumeIconsWhite=true;
    public int inactive=0xff757575,notificationBackground=0xff171717,
        notificationText=0xffffffff,notificationIcons=0xff757575;
    public ThemeOptions copy(){ThemeOptions o=new ThemeOptions();o.quickIconsWhite=quickIconsWhite;o.volumeIconsWhite=volumeIconsWhite;o.inactive=inactive;o.notificationBackground=notificationBackground;o.notificationText=notificationText;o.notificationIcons=notificationIcons;o.overrides.putAll(overrides);return o;}
    /** Android hex order: RRGGBB (opaque) or AARRGGBB, optionally prefixed with #. */
    public static int rgb(String value){String s=value.trim();if(s.startsWith("#"))s=s.substring(1);if(!s.matches("(?i)([0-9a-f]{6}|[0-9a-f]{8})"))throw new IllegalArgumentException("Use RRGGBB or AARRGGBB");long color=Long.parseLong(s,16);return (int)(s.length()==6?0xff000000L|color:color);}
    public static String wire(int color){return String.format(java.util.Locale.US,"%08X",color);}
    public static String hex(int color){return "#"+wire(color);}
    public static boolean white(String value){if(!value.equals("white")&&!value.equals("black"))throw new IllegalArgumentException("Expected white or black");return value.equals("white");}
    public static ThemeOptions fromArgs(String[] args){if(args.length!=11&&args.length!=12)throw new IllegalArgumentException("Expected theme color options");rgb(args[3]);ThemeOptions o=new ThemeOptions();o.quickIconsWhite=white(args[5]);o.inactive=rgb(args[6]);o.volumeIconsWhite=white(args[7]);o.notificationBackground=rgb(args[8]);o.notificationText=rgb(args[9]);o.notificationIcons=rgb(args[10]);return o;}
}
