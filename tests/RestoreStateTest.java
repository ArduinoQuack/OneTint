import dev.dov.onetint.RestoreState;
import org.json.*;
public class RestoreStateTest {
    public static void main(String[] args) throws Exception {
        RestoreState first=new RestoreState(new JSONObject());first.record("ThemePark_Keypad");first.record("ThemePark_VolumePanel");
        RestoreState second=new RestoreState(new JSONObject(first.toJson().toString()));second.record("ThemePark_NotificationPanel");second.record("ThemePark_Keypad");
        if(second.names().length()!=3||!second.names().getString(0).equals("ThemePark_Keypad"))throw new AssertionError("Earlier restore state was lost or duplicated");
        String[] invalid={"{\"disabled\":[\"com.qualcomm.overlay.target\"]}","{\"disabled\":[1]}","{\"disabled\":\"ThemePark_Keypad\"}"};
        for(String data:invalid){boolean rejected=false;try{new RestoreState(new JSONObject(data));}catch(IllegalArgumentException expected){rejected=true;}if(!rejected)throw new AssertionError("Unrelated or malformed restore entry was accepted");}
        System.out.println("PASS: restore state survives repeated applies and rejects unrelated or malformed overlay identifiers");
    }
}
