package dev.dov.onetint;
import android.content.*;
import android.content.res.*;
import android.util.TypedValue;
import org.json.*;

/** Fresh process: read effective target resources instead of reusing pre-commit caches. */
public final class ResourceVerifier {
    public static void main(String[] args) {
        try {
            if(android.os.Process.myUid()!=0)throw new SecurityException("Root required");
            Context context=RootBridge.context();
            JSONArray groups=new JSONArray(RootBridge.read(System.in)),reports=new JSONArray();boolean all=true;
            for(int i=0;i<groups.length();i++){
                JSONObject group=groups.getJSONObject(i);String target=group.getString("target");
                if(!target.equals("com.android.systemui")&&!target.equals("com.samsung.android.honeyboard")&&!target.equals("dev.dov.onetint"))throw new IllegalArgumentException("Unexpected target");
                Context pkg=context.createPackageContext(target,Context.CONTEXT_IGNORE_SECURITY);
                int matched=0,total=0;JSONArray samples=new JSONArray(),mismatches=new JSONArray();
                for(boolean night:new boolean[]{false,true}){
                    Configuration config=new Configuration(pkg.getResources().getConfiguration());
                    config.uiMode=(config.uiMode&~Configuration.UI_MODE_NIGHT_MASK)|(night?Configuration.UI_MODE_NIGHT_YES:Configuration.UI_MODE_NIGHT_NO);
                    Resources res=pkg.createConfigurationContext(config).getResources();
                    JSONArray entries=group.getJSONArray("entries");
                    for(int j=0;j<entries.length();j++){
                        JSONObject entry=entries.getJSONObject(j);String name=entry.getString("name"),type=entry.getString("type");Object expected=entry.get(night?"night":"day");
                        int id=res.getIdentifier(name,type,target);TypedValue value=new TypedValue();res.getValue(id,value,true);
                        Object actual=type.equals("string")?res.getString(id):Integer.valueOf(value.data);
                        boolean same=expected.toString().equals(actual.toString());total++;if(same)matched++;
                        boolean anchor=name.equals("preview_bubble_background_color_open_theme")||name.equals("icon_accent")||name.equals("keyboard_background_color_open_theme")||name.equals("normal_key_background_color_open_theme")||name.equals("qs_tile_round_background_on")||name.equals("open_theme_qp_bg_color")||name.equals("volume_seekbar_progress_color")||type.equals("string");
                        if((anchor&&samples.length()<12)||(!same&&mismatches.length()<6)){
                            JSONObject sample=new JSONObject().put("resource",type+"/"+name).put("mode",night?"night":"day").put("expected",display(expected,type)).put("actual",display(actual,type)).put("match",same);
                            if(anchor&&samples.length()<12)samples.put(sample);if(!same&&mismatches.length()<6)mismatches.put(sample);
                        }
                    }
                }
                all&=matched==total&&total>0;reports.put(new JSONObject().put("name",group.getString("name")).put("matched",matched).put("total",total).put("samples",samples).put("mismatches",mismatches));
            }
            System.out.println("VERIFY:"+new JSONObject().put("verified",all).put("groups",reports));System.exit(0);
        }catch(Throwable e){e.printStackTrace();System.exit(1);}
    }
    static String display(Object value,String type){return type.equals("color")?String.format(java.util.Locale.US,"#%08X",((Number)value).intValue()):value.toString();}
}
