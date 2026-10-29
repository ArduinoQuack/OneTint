package dev.dov.onetint;

import android.app.*;
import android.os.*;
import android.content.*;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.view.accessibility.AccessibilityManager;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.util.Locale;
import java.util.concurrent.*;

public final class MainActivity extends Activity {
    static final int BACK=0xff0d0f14, TEXT=0xfff4f0ff, MUTED=0xffaaa7b6;
    int seed=0xffb49aff;boolean live,busy;Integer pending;
    ThemeOptions options=new ThemeOptions();
    static final String[] COLOR_NAMES={"Inactive bubbles","Background","Text","Icons"};
    static final String[] COLOR_KEYS={"inactiveTileColor","notificationBackground","notificationText","notificationIcons"};
    static final int[] COLOR_DEFAULTS={0xff757575,0xff171717,0xffffffff,0xff757575};
    Button[] colorChoices=new Button[4];
    IconColorSlider quickIconSlider,volumeIconSlider;
    static final class RetainedApply {MainActivity owner;boolean busy,live;String action="";Integer pending;int mode;}
    RetainedApply retained=new RetainedApply();
    String busyAction="";
    android.media.AudioManager audio;boolean volumeReceiverRegistered;
    final BroadcastReceiver volumeReceiver=new BroadcastReceiver(){public void onReceive(Context context,Intent intent){refreshVolume();}};
    Button[] previewTabs=new Button[3];LinearLayout quickOptions,volumeOptions;int previewMode=0;
    ColorPicker picker;Preview preview;TextView hex,status;Button activate,restore;
    Handler handler=new Handler(Looper.getMainLooper());ExecutorService worker=Executors.newSingleThreadExecutor();
    String details="No apply attempted yet.\nOneTint 0.1.11 • "+Build.MODEL+" • Android "+Build.VERSION.RELEASE;
    android.content.SharedPreferences prefs;
    final android.content.SharedPreferences.OnSharedPreferenceChangeListener savedDetails=(preferences,key)->{if("lastDetails".equals(key))details=preferences.getString("lastDetails",details);};
    int dp(float x){return Math.round(x*getResources().getDisplayMetrics().density);}
    TextView label(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextColor(color);t.setTextSize(size);t.setFontFeatureSettings("kern");return t;}
    GradientDrawable rounded(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    void space(LinearLayout l,int height){l.addView(new View(this),new LinearLayout.LayoutParams(1,dp(height)));}
    LinearLayout.LayoutParams full(int height){return new LinearLayout.LayoutParams(-1,height<0?height:dp(height));}
    public void onCreate(Bundle saved){super.onCreate(saved);Object previous=getLastNonConfigurationInstance();if(previous instanceof RetainedApply){retained=(RetainedApply)previous;busy=retained.busy;live=retained.live;busyAction=retained.action;pending=retained.pending;previewMode=retained.mode;}retained.owner=this;prefs=getSharedPreferences("onetint",0);seed=prefs.getInt("seed",seed);options.quickIconsWhite=prefs.getBoolean("quickIconsWhite",true);options.volumeIconsWhite=prefs.getBoolean("volumeIconsWhite",Palette.on(seed)==0xffffffff);prefs.edit().putBoolean("volumeIconsWhite",options.volumeIconsWhite).apply();for(int i=0;i<4;i++)setOptionColor(i,prefs.getInt(COLOR_KEYS[i],COLOR_DEFAULTS[i]));details=prefs.getString("lastDetails",details);prefs.registerOnSharedPreferenceChangeListener(savedDetails);
        getWindow().setStatusBarColor(BACK);getWindow().setNavigationBarColor(BACK);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(BACK);
        LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(24),dp(22),dp(24),dp(24));scroll.addView(root);
        root.setOnApplyWindowInsetsListener((v,insets)->{android.graphics.Insets b=insets.getInsets(WindowInsets.Type.systemBars());v.setPadding(dp(24),dp(22)+b.top,dp(24),dp(24)+b.bottom);return insets;});
        TextView title=label("OneTint",28,TEXT);title.setTypeface(Typeface.create("sans-serif-medium",0));root.addView(title);space(root,12);
        LinearLayout pickerBox=new LinearLayout(this);pickerBox.setGravity(Gravity.CENTER);pickerBox.setOrientation(1);root.addView(pickerBox,full(-2));
        picker=new ColorPicker(this);picker.setColor(seed);int screen=getResources().getDisplayMetrics().widthPixels-dp(48);pickerBox.addView(picker,new LinearLayout.LayoutParams(Math.min(screen,dp(264)),Math.min(screen,dp(264))));
        hex=label("",17,TEXT);hex.setTypeface(Typeface.MONOSPACE);hex.setGravity(Gravity.CENTER);hex.setPadding(0,dp(8),0,dp(8));hex.setContentDescription("Edit exact hex color");hex.setBackground(rounded(0xff1b1d25,12));hex.setOnClickListener(v->editHex());pickerBox.addView(hex,new LinearLayout.LayoutParams(dp(154),dp(44)));space(root,18);
        AccessibilityManager am=(AccessibilityManager)getSystemService(ACCESSIBILITY_SERVICE);
        if(am.isTouchExplorationEnabled())addAccessibleControls(root);
        LinearLayout tabs=new LinearLayout(this);tabs.setPadding(dp(4),dp(4),dp(4),dp(4));tabs.setBackground(rounded(0xff191b22,16));
        String[] names={"Keyboard","Quick Panel","Volume"};
        for(int i=0;i<3;i++){final int mode=i;Button tab=plainButton(names[i],12);previewTabs[i]=tab;tab.setOnClickListener(v->selectPreview(mode));tabs.addView(tab,new LinearLayout.LayoutParams(0,dp(40),1));}
        root.addView(tabs,full(-2));space(root,8);
        LinearLayout previewCard=new LinearLayout(this);previewCard.setOrientation(1);previewCard.setPadding(dp(8),dp(8),dp(8),dp(8));GradientDrawable border=rounded(0xff000000,22);border.setStroke(dp(1),0xff25272f);previewCard.setBackground(border);
        preview=new Preview(this);previewCard.addView(preview,full(-2));
        quickOptions=new LinearLayout(this);quickOptions.setOrientation(1);
        LinearLayout tiles=settingsSection(quickOptions,"Tiles");
        quickIconSlider=iconRow(tiles,"Icons",options.quickIconsWhite,"Quick Panel icon color");quickIconSlider.listener=white->{options.quickIconsWhite=white;settingsChanged();};
        colorRow(tiles,0);LinearLayout notifications=settingsSection(quickOptions,"Notifications");for(int i=1;i<4;i++)colorRow(notifications,i);
        previewCard.addView(quickOptions,full(-2));
        volumeOptions=new LinearLayout(this);volumeOptions.setOrientation(1);LinearLayout volumeSettings=settingsSection(volumeOptions,"Volume");
        volumeIconSlider=iconRow(volumeSettings,"Icons",options.volumeIconsWhite,"Volume icon color");volumeIconSlider.listener=white->{options.volumeIconsWhite=white;settingsChanged();};previewCard.addView(volumeOptions,full(-2));
        root.addView(previewCard,full(-2));selectPreview(previewMode);refreshOptions();space(root,18);
        status=label("",13,MUTED);status.setVisibility(View.GONE);status.setPadding(0,0,0,dp(12));status.setGravity(Gravity.CENTER);status.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);root.addView(status,full(-2));
        activate=new Button(this);activate.setAllCaps(false);activate.setTextSize(16);activate.setTypeface(Typeface.create("sans-serif-medium",0));activate.setText("Apply color");activate.setBackground(rounded(seed,18));activate.setOnClickListener(v->{if(busy)return;if(live){live=false;pending=null;refreshButton();showStatus("Live color paused");}else{live=true;apply(seed);}});root.addView(activate,full(56));space(root,8);
        restore=new Button(this);restore.setAllCaps(false);restore.setText("Restore previous colors");restore.setTextColor(TEXT);restore.setBackground(rounded(0xff20222c,18));restore.setOnClickListener(v->{if(busy)return;live=false;pending=null;run("restore",seed);});root.addView(restore,full(50));space(root,12);
        picker.listener=(color,finished)->{update(color);if(finished&&live)apply(color);};
        setContentView(scroll);update(seed);refreshButton();if(busy)showStatus(busyAction.equals("restore")?"Restoring…":"Applying…");
    }
    Button plainButton(String title,int size){Button b=new Button(this);b.setText(title);b.setTextSize(size);b.setTextColor(MUTED);b.setAllCaps(false);b.setMinWidth(0);b.setMinimumWidth(0);b.setMinHeight(0);b.setMinimumHeight(0);b.setPadding(dp(4),0,dp(4),0);b.setBackground(rounded(Color.TRANSPARENT,11));return b;}
    LinearLayout settingsSection(LinearLayout parent,String title){
        LinearLayout.LayoutParams margin=full(-2);margin.topMargin=dp(8);
        LinearLayout section=new LinearLayout(this);section.setOrientation(1);section.setPadding(dp(12),dp(12),dp(12),dp(8));section.setBackground(rounded(0xff17191f,16));parent.addView(section,margin);
        TextView heading=label(title,12,MUTED);heading.setTypeface(Typeface.create("sans-serif-medium",0));heading.setPadding(0,0,0,dp(4));section.addView(heading,full(-2));return section;
    }
    LinearLayout settingRow(LinearLayout parent,String title){LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);TextView name=label(title,13,TEXT);row.addView(name,new LinearLayout.LayoutParams(0,dp(52),1));parent.addView(row,full(-2));return row;}
    IconColorSlider iconRow(LinearLayout parent,String title,boolean white,String description){LinearLayout row=settingRow(parent,title);IconColorSlider slider=new IconColorSlider(this);slider.setContentDescription(description);slider.setWhite(white);row.addView(slider,new LinearLayout.LayoutParams(dp(148),dp(48)));return slider;}
    void colorRow(LinearLayout parent,int index){LinearLayout row=settingRow(parent,COLOR_NAMES[index]);Button chip=plainButton("",12);chip.setTypeface(Typeface.MONOSPACE);chip.setContentDescription((index==0?"Inactive bubble":("Notification "+COLOR_NAMES[index].toLowerCase(Locale.US)))+" color");chip.setOnClickListener(v->editColor(index));row.addView(chip,new LinearLayout.LayoutParams(dp(126),dp(42)));colorChoices[index]=chip;}
    void selectPreview(int mode){previewMode=mode;preview.setMode(mode);if(mode==2)refreshVolume();quickOptions.setVisibility(mode==1?View.VISIBLE:View.GONE);volumeOptions.setVisibility(mode==2?View.VISIBLE:View.GONE);for(int i=0;i<previewTabs.length;i++){boolean selected=i==mode;previewTabs[i].setSelected(selected);previewTabs[i].setTextColor(selected?TEXT:MUTED);previewTabs[i].setBackground(rounded(selected?0xff30333e:Color.TRANSPARENT,12));}}
    int optionColor(int index){return index==0?options.inactive:index==1?options.notificationBackground:index==2?options.notificationText:options.notificationIcons;}
    void setOptionColor(int index,int color){if(index==0)options.inactive=color;else if(index==1)options.notificationBackground=color;else if(index==2)options.notificationText=color;else options.notificationIcons=color;}
    void settingsChanged(){android.content.SharedPreferences.Editor editor=prefs.edit().putBoolean("quickIconsWhite",options.quickIconsWhite).putBoolean("volumeIconsWhite",options.volumeIconsWhite);for(int i=0;i<4;i++)editor.putInt(COLOR_KEYS[i],optionColor(i));editor.apply();refreshOptions();if(live)apply(seed);}
    void refreshOptions(){
        if(preview==null)return;preview.options=options.copy();preview.invalidate();
        for(int i=0;i<4;i++){Button chip=colorChoices[i];if(chip==null)continue;int color=optionColor(i);chip.setText(String.format(Locale.US,"#%06X",color&0xffffff));chip.setTextColor(TEXT);chip.setBackground(rounded(0xff272a33,11));GradientDrawable swatch=rounded(color,5);swatch.setStroke(dp(1),0xff777981);swatch.setBounds(0,0,dp(18),dp(18));chip.setCompoundDrawablePadding(dp(6));chip.setCompoundDrawables(swatch,null,null,null);}
    }
    void update(int color){seed=color;prefs.edit().putInt("seed",seed).apply();if(hex!=null)hex.setText(String.format(Locale.US,"#%06X",seed&0xffffff));if(preview!=null){preview.seed=seed;preview.options=options.copy();preview.invalidate();}if(activate!=null){activate.setBackground(rounded(seed,18));activate.setTextColor(Palette.on(seed));}}
    void showStatus(String text){status.setText(text);status.setVisibility(View.VISIBLE);}
    void refreshButton(){for(Button chip:colorChoices)chip.setEnabled(!busy);quickIconSlider.setEnabled(!busy);volumeIconSlider.setEnabled(!busy);activate.setEnabled(!busy);activate.setText(busy&&busyAction.equals("apply")?"Applying…":live?"Pause live color":"Apply color");restore.setEnabled(!busy);restore.setText(busy&&busyAction.equals("restore")?"Restoring…":"Restore previous colors");}
    void editColor(int index){
        if(busy)return;int initial=optionColor(index);
        LinearLayout content=new LinearLayout(this);content.setOrientation(1);content.setGravity(Gravity.CENTER);content.setPadding(dp(18),dp(8),dp(18),dp(8));
        ColorPicker colorPicker=new ColorPicker(this);colorPicker.setColor(initial);content.addView(colorPicker,new LinearLayout.LayoutParams(dp(248),dp(248)));
        EditText exact=new EditText(this);exact.setSingleLine();exact.setText(String.format(Locale.US,"%06X",initial&0xffffff));exact.setSelectAllOnFocus(true);exact.setGravity(Gravity.CENTER);exact.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);content.addView(exact,new LinearLayout.LayoutParams(dp(160),dp(48)));
        colorPicker.listener=(color,finished)->exact.setText(String.format(Locale.US,"%06X",color&0xffffff));
        String title=index==0?"Inactive bubbles":"Notification "+COLOR_NAMES[index].toLowerCase(Locale.US);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle(title).setView(content).setNegativeButton("Cancel",null).setNeutralButton("Reset",(v,w)->{setOptionColor(index,COLOR_DEFAULTS[index]);settingsChanged();}).setPositiveButton("Use color",null).create();
        dialog.setOnShowListener(v->dialog.getButton(-1).setOnClickListener(w->{String value=exact.getText().toString().trim().replaceFirst("^#","");if(!value.matches("(?i)[0-9a-f]{6}")){exact.setError("Enter six hex digits");return;}setOptionColor(index,ThemeOptions.rgb(value));dialog.dismiss();settingsChanged();}));dialog.show();
    }
    void editHex(){EditText input=new EditText(this);input.setSingleLine();input.setText(String.format(Locale.US,"%06X",seed&0xffffff));input.setSelectAllOnFocus(true);input.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);input.setPadding(dp(24),dp(16),dp(24),dp(16));AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Exact color").setView(input).setNegativeButton("Cancel",null).setPositiveButton("Use color",null).create();dialog.setOnShowListener(v->dialog.getButton(-1).setOnClickListener(w->{String s=input.getText().toString().trim().replaceFirst("^#","");if(!s.matches("(?i)[0-9a-f]{6}")){input.setError("Enter six hex digits, e.g. A78BFA");return;}int c=(int)(0xff000000L|Long.parseLong(s,16));update(c);picker.setColor(c);if(live)apply(c);dialog.dismiss();}));dialog.show();}
    void addAccessibleControls(LinearLayout root){String[] names={"Hue","Saturation","Brightness"};for(int i=0;i<3;i++){final int channel=i;root.addView(label(names[i],12,MUTED));SeekBar bar=new SeekBar(this);bar.setMax(i==0?359:100);float[] hsv=new float[3];Color.colorToHSV(seed,hsv);bar.setProgress(Math.round(hsv[i]*(i==0?1:100)));bar.setContentDescription(names[i]);bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar b,int value,boolean user){if(!user)return;float[] h=new float[3];Color.colorToHSV(seed,h);h[channel]=value/(channel==0?1f:100f);update(Color.HSVToColor(h));picker.setColor(seed);}public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){if(live)apply(seed);}});root.addView(bar,full(48));}}
    void apply(int c){if(busy){pending=c;return;}run("apply",c);}
    static String rgb(int color){return String.format(Locale.US,"%06X",color&0xffffff);}
    static String quote(String s){return "'"+s.replace("'","'\\''")+"'";}
    void run(String action,int color){
        busy=true;busyAction=action;refreshButton();showStatus(action.equals("restore")?"Restoring…":"Applying…");
        final String apk=getApplicationInfo().sourceDir;
        final ThemeOptions applied=options.copy();
        final int user=android.os.Process.myUid()/100000;
        worker.execute(()->{
            String output;JSONObject result=null;int exit=-1;
            try {
                File payload=new File(getFilesDir(),"theme-state.json");
                if(!payload.exists())try(FileOutputStream init=new FileOutputStream(payload)){init.write("{}".getBytes("UTF-8"));}
                String command="CLASSPATH="+quote(apk)+" /system/bin/app_process /system/bin dev.dov.onetint.RootBridge "+action+" "+user+" "+quote(apk)+" "+String.format(Locale.US,"%06X",color&0xffffff)+" "+quote(payload.getAbsolutePath())+" "+(applied.quickIconsWhite?"white":"black")+" "+rgb(applied.inactive)+" "+(applied.volumeIconsWhite?"white":"black")+" "+rgb(applied.notificationBackground)+" "+rgb(applied.notificationText)+" "+rgb(applied.notificationIcons);
                java.lang.Process process=new ProcessBuilder("su","-c",command).redirectErrorStream(true).start();
                final ByteArrayOutputStream buffer=new ByteArrayOutputStream();
                Thread reader=new Thread(()->{try(InputStream in=process.getInputStream()){byte[] b=new byte[4096];int n;while((n=in.read(b))!=-1){synchronized(buffer){if(buffer.size()<262144)buffer.write(b,0,Math.min(n,262144-buffer.size()));}}}catch(IOException ignored){}});reader.setDaemon(true);reader.start();
                if(!process.waitFor(90,TimeUnit.SECONDS)){process.destroyForcibly();throw new IOException("Root request timed out. Open your root manager and grant OneTint access.");}
                exit=process.exitValue();reader.join(2000);synchronized(buffer){output=buffer.toString("UTF-8");}
                for(String line:output.split("\n"))if(line.startsWith("ONETINT:"))result=new JSONObject(line.substring(8));
                if(result==null)throw new IOException("Root helper did not return a result (exit "+exit+").\n"+output);
            }catch(Exception e){output=e.toString();}
            final JSONObject response=result;final String log=output;final int code=exit;
            handler.post(()->retained.owner.finishApply(action,color,applied,response,log,code));
        });
    }
    void finishApply(String action,int color,ThemeOptions applied,JSONObject response,String log,int code){
                busy=false;busyAction="";boolean ok=response!=null&&response.optBoolean("ok")&&code==0;
                details="OneTint 0.1.11\n"+Build.MANUFACTURER+" "+Build.MODEL+"\nAndroid "+Build.VERSION.RELEASE+"\nBuild "+Build.DISPLAY+"\nAction: "+action+"\nSelected: "+String.format(Locale.US,"#%06X",color&0xffffff)+"\nQuick Panel icons: "+(applied.quickIconsWhite?"White":"Black")+"\nInactive tiles: "+AmoledTheme.hex(applied.inactive)+"\nVolume icons: "+(applied.volumeIconsWhite?"White":"Black")+"\nNotification background: "+AmoledTheme.hex(applied.notificationBackground)+"\nNotification text: "+AmoledTheme.hex(applied.notificationText)+"\nNotification icons: "+AmoledTheme.hex(applied.notificationIcons)+"\nExit: "+code+"\n"+log;
                prefs.edit().putString("lastDetails",details).apply();
                if(ok){
                    if(action.equals("restore")){showStatus("Previous colors restored");live=false;refreshLauncher();}
                    else {JSONObject readback=response.optJSONObject("readback");boolean verified=readback!=null&&readback.optBoolean("verified");showStatus(verified?"Applied":"Could not verify color");prefs.edit().putInt("lastApplied",color).apply();if(response.optBoolean("appIconUpdated"))refreshLauncher();}
                } else {live=false;pending=null;showStatus("Could not apply color");new AlertDialog.Builder(this).setTitle("Apply needs attention").setMessage("The color could not be applied. You can copy the error for troubleshooting.").setPositiveButton("OK",null).setNeutralButton("Copy error",(a,b)->copy()).show();}
                refreshButton();if(pending!=null&&live){int next=pending;pending=null;if(next!=color)apply(next);}
    }
    public Object onRetainNonConfigurationInstance(){retained.busy=busy;retained.live=live;retained.action=busyAction;retained.pending=pending;retained.mode=previewMode;return retained;}
    void refreshVolume(){if(preview==null)return;try{if(audio==null)audio=(android.media.AudioManager)getSystemService(AUDIO_SERVICE);preview.mediaLevel=audio.getStreamVolume(android.media.AudioManager.STREAM_MUSIC);preview.mediaMax=audio.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC);preview.invalidate();}catch(RuntimeException ignored){preview.mediaLevel=0;preview.mediaMax=0;}}
    void refreshLauncher(){
        try{android.content.pm.PackageManager pm=getPackageManager();ComponentName a=new ComponentName(this,getPackageName()+".LauncherA"),b=new ComponentName(this,getPackageName()+".LauncherB");boolean useB=pm.getComponentEnabledSetting(b)==android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
            java.util.List<android.content.pm.PackageManager.ComponentEnabledSetting> settings=new java.util.ArrayList<>();settings.add(new android.content.pm.PackageManager.ComponentEnabledSetting(useB?a:b,android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED,android.content.pm.PackageManager.DONT_KILL_APP));settings.add(new android.content.pm.PackageManager.ComponentEnabledSetting(useB?b:a,android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED,android.content.pm.PackageManager.DONT_KILL_APP));pm.setComponentEnabledSettings(settings);
        }catch(RuntimeException error){android.util.Log.w("OneTint","Launcher icon refresh",error);}
    }
    protected void onResume(){super.onResume();refreshVolume();if(!volumeReceiverRegistered){registerReceiver(volumeReceiver,new IntentFilter("android.media.VOLUME_CHANGED_ACTION"),Context.RECEIVER_NOT_EXPORTED);volumeReceiverRegistered=true;}}
    protected void onPause(){if(volumeReceiverRegistered){unregisterReceiver(volumeReceiver);volumeReceiverRegistered=false;}super.onPause();}
    protected void onDestroy(){if(prefs!=null)prefs.unregisterOnSharedPreferenceChangeListener(savedDetails);super.onDestroy();}
    void copy(){((ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("OneTint apply details",details));Toast.makeText(this,"Error copied",Toast.LENGTH_SHORT).show();}
}
