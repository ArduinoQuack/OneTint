package dev.dov.onetint;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Looper;
import org.json.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.zip.*;

/** Executed only through the user's root manager, never inside the UI process. */
public final class RootBridge {
    static final String PREFIX="dev_dov_onetint_";
    static final String CURRENT_PREFIX="ThemePark_"+PREFIX;
    static final String[] NAMES={"Keyboard","QuickPanel","Volume","Refresh","AppIcon"};
    static final String[] TARGETS={"com.samsung.android.honeyboard","com.android.systemui","com.android.systemui","android","dev.dov.onetint"};
    static Class<?> txBuilder, txClass, overlayClass, identifierClass, iface;
    static Object manager;
    static int user;
    static Object legacyId(String target,String name) throws Exception {return identifierClass.getConstructor(String.class,String.class).newInstance(target,PREFIX+name);}
    static Object id(String target,String name) throws Exception {return identifierClass.getConstructor(String.class,String.class).newInstance("android",CURRENT_PREFIX+name);}
    static final String[] CONFLICTS=RestoreState.ALLOWED;
    static Object conflictId(String name) throws Exception {return identifierClass.getConstructor(String.class,String.class).newInstance("android",name);}
    static boolean knownConflict(String name){return RestoreState.allowed(name);}
    static void enable(Object tx,Object identifier,boolean active) throws Exception {txBuilder.getMethod("setEnabled",identifierClass,boolean.class,int.class).invoke(tx,identifier,active,user);}
    static File stateFile;
    static RestoreState saved;
    static void save() throws Exception {
        File temp=new File(stateFile.getPath()+".tmp");
        try(FileOutputStream out=new FileOutputStream(temp)){out.write(saved.toJson().toString().getBytes("UTF-8"));out.getFD().sync();}
        if(!temp.renameTo(stateFile))throw new IOException("Could not save theme restore state");
    }
    static JSONArray disableConflicts() throws Exception {
        Object tx=txBuilder.getConstructor().newInstance();JSONArray changed=new JSONArray();
        for(String n:CONFLICTS){Object ident=conflictId(n);if(enabled(info(ident))){saved.record(n);enable(tx,ident,false);changed.put(n);}}
        save();commit(tx);return changed;
    }
    static void restore() throws Exception {
        Object tx=txBuilder.getConstructor().newInstance();
        for(int i=0;i<NAMES.length;i++)for(Object ident:new Object[]{id(TARGETS[i],NAMES[i]),legacyId(TARGETS[i],NAMES[i])})if(info(ident)!=null)enable(tx,ident,false);
        JSONArray prior=saved.names();if(prior!=null)for(int i=0;i<prior.length();i++){String n=prior.getString(i);if(!knownConflict(n))throw new SecurityException("Invalid restore identifier");Object ident=conflictId(n);if(info(ident)!=null)enable(tx,ident,true);}
        commit(tx);
        for(int i=0;i<NAMES.length;i++)for(Object ident:new Object[]{id(TARGETS[i],NAMES[i]),legacyId(TARGETS[i],NAMES[i])})if(enabled(info(ident)))throw new IOException("OneTint overlay is still active after restore");
        if(prior!=null)for(int i=0;i<prior.length();i++){Object ident=conflictId(prior.getString(i));if(info(ident)!=null&&!enabled(info(ident)))throw new IOException("An earlier Theme Park overlay could not be restored");}
        saved=new RestoreState(new JSONObject());save();
    }
    static Object info(Object id) throws Exception {return iface.getMethod("getOverlayInfoByIdentifier",identifierClass,int.class).invoke(manager,id,user);}
    static void commit(Object builder) throws Exception {iface.getMethod("commit",txClass).invoke(manager,txBuilder.getMethod("build").invoke(builder));}
    static boolean enabled(Object info) throws Exception {return info!=null&&(Boolean)info.getClass().getMethod("isEnabled").invoke(info);}
    static String read(InputStream input) throws Exception {try(InputStream in=input;ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)out.write(b,0,n);return out.toString("UTF-8");}}
    static Context context() throws Exception {
        Looper.prepareMainLooper();
        Class<?> at=Class.forName("android.app.ActivityThread");
        Object thread=at.getMethod("systemMain").invoke(null);
        return (Context)at.getMethod("getSystemContext").invoke(thread);
    }
    static void init() throws Exception {
        try {Class<?> vm=Class.forName("dalvik.system.VMRuntime");Object runtime=vm.getDeclaredMethod("getRuntime").invoke(null);vm.getDeclaredMethod("setHiddenApiExemptions",String[].class).invoke(runtime,(Object)new String[]{"L"});} catch(Exception ignored) {}
        txClass=Class.forName("android.content.om.OverlayManagerTransaction");txBuilder=Class.forName(txClass.getName()+"$Builder");
        overlayClass=Class.forName("android.content.om.FabricatedOverlay");identifierClass=Class.forName("android.content.om.OverlayIdentifier");
        Class<?> sm=Class.forName("android.os.ServiceManager");Object binder=sm.getMethod("getService",String.class).invoke(null,"overlay");
        iface=Class.forName("android.content.om.IOverlayManager");
        manager=Class.forName(iface.getName()+"$Stub").getMethod("asInterface",android.os.IBinder.class).invoke(null,binder);
        if(manager==null)throw new IllegalStateException("Android overlay service unavailable");
    }
    // Samsung resource_map aliases are resolved on the device, rather than guessing names.
    static Map<String,List<String>> aliases(Resources res,String target) {
        Map<String,List<String>> out=new HashMap<>();
        int resource=res.getIdentifier("resource_map","xml",target);if(resource==0)return out;
        try(XmlResourceParser p=res.getXml(resource)) {
            String match=null;ArrayList<String> originals=new ArrayList<>();
            while(p.next()!=XmlResourceParser.END_DOCUMENT) {
                if(p.getEventType()==XmlResourceParser.START_TAG) {
                    if(p.getName().equals("match")){match=p.getAttributeValue(null,"overlay");originals=new ArrayList<>();}
                    if(p.getName().equals("original")){String name=p.nextText().trim();if(!name.isEmpty())originals.add(name);}
                } else if(p.getEventType()==XmlResourceParser.END_TAG && p.getName().equals("match")&&match!=null) out.computeIfAbsent(match,k->new ArrayList<>()).addAll(originals);
            }
        }catch(Exception e){System.err.println("resource_map: "+e.getMessage());}
        return out;
    }
    static int color(String spec,int seed,boolean night) {
        String[] pair=spec.split(",");String s=pair[night&&pair.length>1?1:0].trim();
        return s.startsWith("#")?android.graphics.Color.parseColor(s):Palette.role(s,seed,night);
    }
    static JSONObject verify(String apk,JSONArray expected) throws Exception {
        ProcessBuilder command=new ProcessBuilder("/system/bin/app_process","/system/bin","dev.dov.onetint.ResourceVerifier");command.environment().put("CLASSPATH",apk);command.redirectErrorStream(true);
        java.lang.Process process=command.start();
        try(OutputStream out=process.getOutputStream()){out.write(expected.toString().getBytes("UTF-8"));}
        if(!process.waitFor(20,java.util.concurrent.TimeUnit.SECONDS)){process.destroyForcibly();throw new IOException("Resource verification timed out");}
        String output=read(process.getInputStream());
        for(String line:output.split("\n"))if(line.startsWith("VERIFY:"))return new JSONObject(line.substring(7));
        throw new IOException("Resource verifier exit "+process.exitValue()+": "+output);
    }
    static boolean applyAppIcon(String apk,int seed) {
        try {
            String target="dev.dov.onetint";Class<?> builderClass=Class.forName("android.content.om.FabricatedOverlay$Builder");
            Object builder=builderClass.getConstructor(String.class,String.class,String.class).newInstance("android",CURRENT_PREFIX+"AppIcon",target);
            builderClass.getMethod("setTargetOverlayable",String.class).invoke(builder,"OneTintIcon");
            Method set=builderClass.getMethod("setResourceValue",String.class,int.class,int.class,String.class);
            for(String config:new String[]{null,"night"})set.invoke(builder,target+":color/icon_accent",28,seed,config);
            Object tx=txBuilder.getConstructor().newInstance();txBuilder.getMethod("registerFabricatedOverlay",overlayClass).invoke(tx,builderClass.getMethod("build").invoke(builder));enable(tx,id(target,"AppIcon"),true);commit(tx);
            JSONArray entries=new JSONArray().put(new JSONObject().put("name","icon_accent").put("type","color").put("day",seed).put("night",seed));
            JSONObject check=verify(apk,new JSONArray().put(new JSONObject().put("name","AppIcon").put("target",target).put("entries",entries)));
            diagnostics.put("appIcon",check);if(check.optBoolean("verified"))return true;
        }catch(Exception error){try{diagnostics.put("appIconError",error.toString());}catch(Exception ignored){}}
        try{Object tx=txBuilder.getConstructor().newInstance();Object ident=id("dev.dov.onetint","AppIcon");if(info(ident)!=null)enable(tx,ident,false);commit(tx);}catch(Exception ignored){}
        return false;
    }
    static JSONArray overlayState(String target) throws Exception {
        List<?> list=(List<?>)iface.getMethod("getOverlayInfosForTarget",String.class,int.class).invoke(manager,target,user);
        JSONArray state=new JSONArray();
        for(Object item:list){
            Class<?> c=item.getClass();String identifier=c.getMethod("getOverlayIdentifier").invoke(item).toString();
            boolean active=enabled(item);if(!active&&!identifier.contains(PREFIX))continue;
            if(target.equals("android")&&!identifier.contains("ThemePark_")&&!identifier.contains(PREFIX))continue;
            JSONObject entry=new JSONObject().put("id",identifier).put("enabled",active);
            for(String field:new String[]{"baseCodePath","category","priority","isFabricated"})try{entry.put(field,c.getField(field).get(item));}catch(Exception ignored){}
            state.put(entry);if(state.length()>=60)break;
        }
        return state;
    }
    static JSONObject states() throws Exception {return new JSONObject().put("Keyboard",overlayState(TARGETS[0])).put("SystemUI",overlayState(TARGETS[1])).put("Framework",overlayState("android"));}
    static final JSONObject diagnostics=new JSONObject();
    static String phase="starting";
    public static void main(String[] args) {
        try {
            if(android.os.Process.myUid()!=0)throw new SecurityException("Root access was not granted");
            ThemeOptions options=ThemeOptions.fromArgs(args);
            user=Integer.parseInt(args[1]);if(user!=0)throw new IllegalArgumentException("This experimental build currently supports the main Android user only");init();
            stateFile=new File(args[4]).getCanonicalFile();
            if(!stateFile.getName().equals("theme-state.json")||!(stateFile.getParent().equals("/data/user/0/dev.dov.onetint/files")||stateFile.getParent().equals("/data/data/dev.dov.onetint/files")))throw new SecurityException("Invalid private restore state path");
            saved=new RestoreState(stateFile.exists()?new JSONObject(read(new FileInputStream(stateFile))):new JSONObject());
            diagnostics.put("before",states());
            Object transaction=txBuilder.getConstructor().newInstance();
            JSONArray report=new JSONArray(),verification=new JSONArray();
            if(args[0].equals("restore")) {
                phase="restoring previous theme";restore();diagnostics.put("after",states());
                System.out.println("ONETINT:"+new JSONObject().put("ok",true).put("restored",true).put("diagnostics",diagnostics));System.exit(0);return;
            }
            if(!args[0].equals("apply")||!args[3].matches("[0-9a-fA-F]{6}"))throw new IllegalArgumentException("Invalid color request");
            phase="building Android-owned Theme Park overlays";
            int seed=(int)(0xff000000L|Long.parseLong(args[3],16));
            Context ctx=context();
            int oneui=0;try {oneui=(Integer)Class.forName("android.os.SystemProperties").getMethod("getInt",String.class,int.class).invoke(null,"ro.build.version.oneui",0);}catch(Exception ignored){}
            if(oneui<60000||oneui>=100000)throw new IllegalStateException("Unsupported One UI version: "+oneui);
            String version=oneui>=90000?"90000":oneui>=80000?"80000":oneui>=70000?"70000":"60000";
            JSONObject catalog;try(ZipFile apk=new ZipFile(args[2])){catalog=new JSONObject(read(apk.getInputStream(apk.getEntry("assets/catalog.json"))));}
            JSONArray groups=catalog.getJSONArray(version);
            Class<?> builderClass=Class.forName("android.content.om.FabricatedOverlay$Builder");
            Method set=builderClass.getMethod("setResourceValue",String.class,int.class,int.class,String.class);
            for(int i=0;i<groups.length();i++) {
                JSONObject group=groups.getJSONObject(i);String target=group.getString("target"),name=group.getString("name");
                Resources res=ctx.getPackageManager().getResourcesForApplication(target);
                Map<String,List<String>> alias=aliases(res,target);
                // Match ownership and naming observed in the device's working Theme Park overlays.
                Object builder=builderClass.getConstructor(String.class,String.class,String.class).newInstance("android",CURRENT_PREFIX+name,target);
                JSONArray rows=group.getJSONArray("rows"),expectedEntries=new JSONArray(),strings=new JSONArray();Set<String> seen=new HashSet<>();int missing=0;
                for(int j=0;j<rows.length();j++) {
                    JSONObject row=rows.getJSONObject(j);String type=row.getString("type"),uid=row.getString("uid"),theme=AmoledTheme.theme(name,uid,row.getString("theme"),options);
                    int opacity=AmoledTheme.opacity(name,uid,row.getInt("opacity"));
                    ArrayList<String> resources=new ArrayList<>();JSONArray ns=row.getJSONArray("names");
                    for(int k=0;k<ns.length();k++){String n=ns.getString(k);resources.add(n);if(alias.containsKey(n))resources.addAll(alias.get(n));}
                    for(String n:resources) {
                        if(res.getIdentifier(n,type,target)==0){missing++;continue;}
                        if(!seen.add(type+"/"+n))continue;
                        if(type.equals("string")) {
                            String marker=String.format(java.util.Locale.US,"#%06X",seed&0xffffff);
                            strings.put(new JSONObject().put("name",n).put("value",marker));
                            expectedEntries.put(new JSONObject().put("name",n).put("type",type).put("day",marker).put("night",marker));
                            continue;
                        }
                        JSONObject expected=new JSONObject().put("name",n).put("type",type);
                        for(boolean night:new boolean[]{false,true}) {
                            int value=type.equals("bool")?(Boolean.parseBoolean(theme)?1:0):Palette.alpha(color(theme,seed,night),opacity);
                            set.invoke(builder,target+":"+type+"/"+n,type.equals("bool")?18:28,value,night?"night":null);
                            expected.put(night?"night":"day",value);
                        }
                        expectedEntries.put(expected);
                    }
                }
                if(seen.isEmpty())throw new IllegalStateException("No compatible "+name+" resources found on this firmware");
                Object overlay=builderClass.getMethod("build").invoke(builder);
                if(strings.length()>0){
                    Method setString=overlayClass.getMethod("setResourceValue",String.class,int.class,String.class,String.class);
                    for(int j=0;j<strings.length();j++){JSONObject item=strings.getJSONObject(j);for(String config:new String[]{null,"night"})setString.invoke(overlay,target+":string/"+item.getString("name"),3,item.getString("value"),config);}
                }
                verification.put(new JSONObject().put("name",name).put("target",target).put("entries",expectedEntries));
                txBuilder.getMethod("registerFabricatedOverlay",overlayClass).invoke(transaction,overlay);
                txBuilder.getMethod("setEnabled",identifierClass,boolean.class,int.class).invoke(transaction,id(target,name),true,user);
                report.put(new JSONObject().put("name",name).put("resources",seen.size()).put("missing",missing));
            }
            boolean refreshAdded=false;
            if(oneui>=80500){
                Resources framework=ctx.getPackageManager().getResourcesForApplication("android");
                if(framework.getIdentifier("sem_index_scroll_preview_text_color","color","android")!=0){
                    Object refresh=builderClass.getConstructor(String.class,String.class,String.class).newInstance("android",CURRENT_PREFIX+"Refresh","android");
                    set.invoke(refresh,"android:color/sem_index_scroll_preview_text_color",28,0xfffafaff,null);
                    txBuilder.getMethod("registerFabricatedOverlay",overlayClass).invoke(transaction,builderClass.getMethod("build").invoke(refresh));
                    txBuilder.getMethod("setEnabled",identifierClass,boolean.class,int.class).invoke(transaction,id("android","Refresh"),true,user);refreshAdded=true;
                }
            }
            for(int i=0;i<NAMES.length;i++){Object legacy=legacyId(TARGETS[i],NAMES[i]);if(info(legacy)!=null)enable(transaction,legacy,false);}
            phase="applying Android-owned overlays";
            commit(transaction);
            for(int i=0;i<(refreshAdded?4:3);i++)if(!enabled(info(id(TARGETS[i],NAMES[i])))) {
                Object undo=txBuilder.getConstructor().newInstance();
                for(int j=0;j<NAMES.length;j++){Object ident=id(TARGETS[j],NAMES[j]);if(info(ident)!=null)txBuilder.getMethod("setEnabled",identifierClass,boolean.class,int.class).invoke(undo,ident,false,user);}
                commit(undo);
                throw new IllegalStateException(NAMES[i]+" overlay was rejected by this firmware. OneTint overlays were disabled; copy apply details.");
            }
            phase="reading effective resource colors";
            JSONObject readback=verify(args[2],verification);
            if(!readback.optBoolean("verified")){
                phase="resolving competing Theme Park overlays";
                diagnostics.put("temporarilyDisabled",disableConflicts());
                Object priority=txBuilder.getConstructor().newInstance();for(int i=0;i<(refreshAdded?4:3);i++)enable(priority,id(TARGETS[i],NAMES[i]),true);commit(priority);
                readback=verify(args[2],verification);
            }
            boolean verified=readback.optBoolean("verified");
            if(!verified){phase="rolling back unverified apply";restore();}
            boolean appIconUpdated=verified&&applyAppIcon(args[2],seed);
            diagnostics.put("after",states()).put("restoreState",saved.toJson());
            JSONObject result=new JSONObject().put("ok",verified).put("backend","Android-owned Theme Park overlays").put("quickPanelIcons",args[5]).put("inactiveTileColor",AmoledTheme.hex(options.inactive)).put("volumeIcons",args[7]).put("notificationBackground",AmoledTheme.hex(options.notificationBackground)).put("notificationText",AmoledTheme.hex(options.notificationText)).put("notificationIcons",AmoledTheme.hex(options.notificationIcons)).put("oneui",oneui).put("groups",report).put("frameworkRefresh",refreshAdded).put("readback",readback).put("diagnostics",diagnostics);
            result.put("appIconUpdated",appIconUpdated);
            if(!verified)result.put("error","Requested colors did not pass readback. OneTint overlays were disabled and saved Theme Park overlays re-enabled.");
            System.out.println("ONETINT:"+result);System.exit(verified?0:1);
        } catch(Throwable e) {
            while(e instanceof InvocationTargetException&&e.getCause()!=null)e=e.getCause();
            e.printStackTrace(System.err);
            try{if(stateFile!=null&&saved!=null){try{restore();diagnostics.put("rollback","completed");}catch(Exception rollback){diagnostics.put("rollbackError",rollback.toString());}}System.out.println("ONETINT:"+new JSONObject().put("ok",false).put("phase",phase).put("error",e.toString()).put("diagnostics",diagnostics));}catch(Exception ignored){}
            System.exit(1);
        }
    }
}
