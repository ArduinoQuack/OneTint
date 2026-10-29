package dev.dov.onetint;
import org.json.*;
import java.util.LinkedHashSet;
/** Only the three existing theme components requested by this app can be recorded for restoration. */
public final class RestoreState {
    public static final String[] ALLOWED={"ThemePark_Keypad","ThemePark_NotificationPanel","ThemePark_VolumePanel"};
    final LinkedHashSet<String> disabled=new LinkedHashSet<>();
    public RestoreState(JSONObject data) throws JSONException {
        if(data.has("disabled")&&!(data.get("disabled") instanceof JSONArray))throw new IllegalArgumentException("Invalid saved restore state");
        JSONArray names=data.optJSONArray("disabled");if(names!=null)for(int i=0;i<names.length();i++){Object value=names.get(i);if(!(value instanceof String))throw new IllegalArgumentException("Invalid restore identifier");record((String)value);}
    }
    public static boolean allowed(String name){for(String item:ALLOWED)if(item.equals(name))return true;return false;}
    public void record(String name){if(!allowed(name))throw new IllegalArgumentException("Refusing to record an unrelated overlay: "+name);disabled.add(name);}
    public JSONArray names(){JSONArray out=new JSONArray();for(String name:disabled)out.put(name);return out;}
    public JSONObject toJson() throws JSONException {return new JSONObject().put("disabled",names());}
}
