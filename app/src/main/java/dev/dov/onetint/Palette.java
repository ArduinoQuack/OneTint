package dev.dov.onetint;

/** AMOLED black and neutral surfaces, with accessible foregrounds and seed-colored accents. */
public final class Palette {
    public static int mix(int a, int b, float t) {
        return 0xff000000 | (Math.round(((a>>16)&255)*(1-t)+((b>>16)&255)*t)<<16)
            | (Math.round(((a>>8)&255)*(1-t)+((b>>8)&255)*t)<<8)
            | Math.round((a&255)*(1-t)+(b&255)*t);
    }
    public static int alpha(int c,int percent) {return (c&0xffffff) | (Math.round(((c>>>24)&255)*percent/100f)<<24);}
    public static int on(int c) {
        double l=0; double[] w={.2126,.7152,.0722};
        for(int i=0;i<3;i++){double x=((c>>(16-i*8))&255)/255.;l+=w[i]*(x<=.04045?x/12.92:Math.pow((x+.055)/1.055,2.4));}
        return l>.179 ? 0xff000000 : 0xffffffff;
    }
    public static int role(String role,int seed,boolean dark) {
        switch(role.trim()) {
            case "background": return 0xff000000;
            case "surface": return 0xff181818;
            case "primary": case "secondary": case "highlight": return seed;
            case "on_primary": case "on_secondary": return on(seed);
            case "on_background": case "on_surface": return 0xfff5f4fa;
            case "error": return dark?0xffffb4ab:0xffb3261e;
            default: throw new IllegalArgumentException("Unknown color role: "+role);
        }
    }
}
