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
    IconColorSlider quickIconSlider,volumeIconSlider;
    static final class RetainedApply {MainActivity owner;boolean busy,live;String action="";Integer pending;int mode;}
    RetainedApply retained=new RetainedApply();
    String busyAction="";
    android.media.AudioManager audio;boolean volumeReceiverRegistered;
    final BroadcastReceiver volumeReceiver=new BroadcastReceiver(){public void onReceive(Context context,Intent intent){refreshVolume();}};
    Button[] previewTabs=new Button[3];LinearLayout keyboardOptions,quickOptions,volumeOptions,volumeLayout,previewCard;int previewMode=0;
    ColorPicker picker;Preview preview;TextView hex,status;Button activate,restore;
    final java.util.List<Swatch> swatches=new java.util.ArrayList<>();
    static final class Swatch {LinearLayout view;TextView value;View dot;String title,group;String[] uids;int basic=-1;}
    final java.util.Map<String,JSONObject> colorRows=new java.util.LinkedHashMap<>();
    Handler handler=new Handler(Looper.getMainLooper());ExecutorService worker=Executors.newSingleThreadExecutor();
    String details="No apply attempted yet.\nOneTint 0.2.1 • "+Build.MODEL+" • Android "+Build.VERSION.RELEASE;
    android.content.SharedPreferences prefs;
    final android.content.SharedPreferences.OnSharedPreferenceChangeListener savedDetails=(preferences,key)->{if("lastDetails".equals(key))details=preferences.getString("lastDetails",details);};
    int dp(float x){return Math.round(x*getResources().getDisplayMetrics().density);}
    TextView label(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextColor(color);t.setTextSize(size);t.setFontFeatureSettings("kern");return t;}
    GradientDrawable rounded(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    void space(LinearLayout l,int height){l.addView(new View(this),new LinearLayout.LayoutParams(1,dp(height)));}
    LinearLayout.LayoutParams full(int height){return new LinearLayout.LayoutParams(-1,height<0?height:dp(height));}
    public void onCreate(Bundle saved){super.onCreate(saved);Object previous=getLastNonConfigurationInstance();if(previous instanceof RetainedApply){retained=(RetainedApply)previous;busy=retained.busy;live=retained.live;busyAction=retained.action;pending=retained.pending;previewMode=retained.mode;}retained.owner=this;prefs=getSharedPreferences("onetint",0);seed=prefs.getInt("seed",seed);options.quickIconsWhite=prefs.getBoolean("quickIconsWhite",true);options.volumeIconsWhite=prefs.getBoolean("volumeIconsWhite",Palette.on(seed)==0xffffffff);prefs.edit().putBoolean("volumeIconsWhite",options.volumeIconsWhite).apply();for(int i=0;i<4;i++)setOptionColor(i,prefs.getInt(COLOR_KEYS[i],COLOR_DEFAULTS[i]));details=prefs.getString("lastDetails",details);prefs.registerOnSharedPreferenceChangeListener(savedDetails);loadColorRows();
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
        previewCard=new LinearLayout(this);previewCard.setOrientation(1);previewCard.setPadding(dp(8),dp(8),dp(8),dp(8));GradientDrawable border=rounded(0xff000000,22);border.setStroke(dp(1),0xff25272f);previewCard.setBackground(border);
        preview=new Preview(this);previewCard.addView(preview,full(-2));
        root.addView(previewCard,full(-2));
        keyboardOptions=new LinearLayout(this);keyboardOptions.setOrientation(1);root.addView(keyboardOptions,full(-2));
        sectionHeading(keyboardOptions,"Key colors",0);
        paletteGrid(keyboardOptions,
            mappedSwatch("Keys","Keyboard","75-2-2","75-2-1","75-2-3"),
            mappedSwatch("Letters","Keyboard","75-3-2","75-4-1","75-4-2","75-4-4"),
            mappedSwatch("Pressed key","Keyboard","75-2-4"),
            mappedSwatch("Key pop-up","Keyboard","75-5-1"),
            mappedSwatch("Background","Keyboard","75-1-1","75-1-4","75-11-1","75-12-1"));
        quickOptions=new LinearLayout(this);quickOptions.setOrientation(1);root.addView(quickOptions,full(-2));
        sectionHeading(quickOptions,"Quick Panel colors",1);
        paletteGrid(quickOptions,mappedSwatch("Active bubbles","QuickPanel","4-2-3"),basicSwatch("Inactive bubbles",0),mappedSwatch("Background","QuickPanel","4-1-2","4-2-6"));
        quickIconSlider=iconRow(quickOptions,"Bubble icons",options.quickIconsWhite,"Quick Panel icon color");quickIconSlider.listener=white->{options.quickIconsWhite=white;clearMapped("QuickPanel","4-2-1","4-2-2");settingsChanged();};
        TextView notificationHeading=label("Notifications",12,MUTED);notificationHeading.setPadding(dp(2),dp(12),0,dp(8));quickOptions.addView(notificationHeading);
        paletteGrid(quickOptions,basicSwatch("Background",1),basicSwatch("Text",2),basicSwatch("Icons",3));
        // Put the color controls beside the single compact volume popup.
        volumeLayout=new LinearLayout(this);volumeLayout.setGravity(Gravity.CENTER_VERTICAL);volumeLayout.setPadding(dp(12),dp(4),dp(4),dp(4));volumeLayout.setBackground(rounded(Color.BLACK,22));root.addView(volumeLayout,full(-2));
        volumeOptions=new LinearLayout(this);volumeOptions.setOrientation(1);volumeLayout.addView(volumeOptions,new LinearLayout.LayoutParams(0,-2,1));
        sectionHeading(volumeOptions,"Volume colors",2);
        paletteGrid(volumeOptions,mappedSwatch("Fill","Volume","2001-1"),mappedSwatch("Track","Volume","2001-3"));
        TextView volumeIconLabel=label("Icon color",12,MUTED);volumeIconLabel.setPadding(dp(2),dp(14),0,0);volumeOptions.addView(volumeIconLabel);
        volumeIconSlider=new IconColorSlider(this);volumeIconSlider.setContentDescription("Volume icon color");volumeIconSlider.setWhite(options.volumeIconsWhite);volumeIconSlider.listener=white->{options.volumeIconsWhite=white;clearMapped("Volume","2001-2");settingsChanged();};volumeOptions.addView(volumeIconSlider,new LinearLayout.LayoutParams(dp(156),dp(48)));
        selectPreview(previewMode);refreshOptions();space(root,18);
        status=label("",13,MUTED);status.setVisibility(View.GONE);status.setPadding(0,0,0,dp(12));status.setGravity(Gravity.CENTER);status.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);root.addView(status,full(-2));
        activate=new Button(this);activate.setAllCaps(false);activate.setTextSize(16);activate.setTypeface(Typeface.create("sans-serif-medium",0));activate.setText("Apply color");activate.setBackground(rounded(seed,18));activate.setOnClickListener(v->{if(busy)return;if(live){live=false;pending=null;refreshButton();showStatus("Live color paused");}else{live=true;apply(seed);}});root.addView(activate,full(56));space(root,8);
        restore=new Button(this);restore.setAllCaps(false);restore.setText("Restore previous colors");restore.setTextColor(TEXT);restore.setBackground(rounded(0xff20222c,18));restore.setOnClickListener(v->{if(busy)return;live=false;pending=null;run("restore",seed);});root.addView(restore,full(50));space(root,12);
        picker.listener=(color,finished)->{update(color);if(finished&&live)apply(color);};
        setContentView(scroll);update(seed);refreshButton();if(busy)showStatus(busyAction.equals("restore")?"Restoring…":"Applying…");
    }
    Button plainButton(String title,int size){Button b=new Button(this);b.setText(title);b.setTextSize(size);b.setTextColor(MUTED);b.setAllCaps(false);b.setMinWidth(0);b.setMinimumWidth(0);b.setMinHeight(0);b.setMinimumHeight(0);b.setPadding(dp(4),0,dp(4),0);b.setBackground(rounded(Color.TRANSPARENT,11));return b;}
    void sectionHeading(LinearLayout parent,String title,int mode){
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);TextView name=label(title,13,MUTED);row.addView(name,new LinearLayout.LayoutParams(0,dp(44),1));
        Button reset=plainButton("Reset",12);reset.setContentDescription("Reset "+new String[]{"Keyboard","Quick Panel","Volume"}[mode]+" colors");reset.setOnClickListener(v->resetSection(mode));row.addView(reset,new LinearLayout.LayoutParams(dp(48),dp(44)));parent.addView(row,full(-2));
    }
    LinearLayout settingRow(LinearLayout parent,String title){LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);TextView name=label(title,13,TEXT);name.setGravity(Gravity.CENTER_VERTICAL);row.addView(name,new LinearLayout.LayoutParams(0,dp(52),1));parent.addView(row,full(-2));return row;}
    IconColorSlider iconRow(LinearLayout parent,String title,boolean white,String description){LinearLayout row=settingRow(parent,title);IconColorSlider slider=new IconColorSlider(this);slider.setContentDescription(description);slider.setWhite(white);row.addView(slider,new LinearLayout.LayoutParams(dp(148),dp(48)));return slider;}
    Swatch mappedSwatch(String title,String group,String...uids){Swatch swatch=new Swatch();swatch.title=title;swatch.group=group;swatch.uids=uids;return swatch;}
    Swatch basicSwatch(String title,int index){Swatch swatch=new Swatch();swatch.title=title;swatch.basic=index;return swatch;}
    void paletteGrid(LinearLayout parent,Swatch...items){
        for(int i=0;i<items.length;i+=2){LinearLayout row=new LinearLayout(this);LinearLayout.LayoutParams margin=full(-2);margin.bottomMargin=dp(7);parent.addView(row,margin);
            for(int j=i;j<Math.min(i+2,items.length);j++){Swatch item=items[j];LinearLayout card=new LinearLayout(this);card.setGravity(Gravity.CENTER_VERTICAL);card.setPadding(dp(11),dp(9),dp(10),dp(9));card.setBackground(rounded(0xff191b22,14));item.view=card;
                LinearLayout words=new LinearLayout(this);words.setOrientation(1);TextView name=label(item.title,12,TEXT);name.setSingleLine();words.addView(name);item.value=label("",10,MUTED);item.value.setTypeface(Typeface.MONOSPACE);item.value.setPadding(0,dp(4),0,0);words.addView(item.value);card.addView(words,new LinearLayout.LayoutParams(0,-2,1));
                item.dot=new View(this);LinearLayout.LayoutParams dot=new LinearLayout.LayoutParams(dp(24),dp(24));dot.leftMargin=dp(5);card.addView(item.dot,dot);card.setFocusable(true);card.setClickable(true);card.setOnClickListener(v->editSwatch(item));
                LinearLayout.LayoutParams cell=new LinearLayout.LayoutParams(0,dp(64),1);if(j==i&&i+1<items.length)cell.rightMargin=dp(7);row.addView(card,cell);swatches.add(item);
            }
        }
    }
    int swatchColor(Swatch item){if(item.basic>=0){String uid=new String[]{"4-2-4","4-4-1","4-22-2","4-99-2"}[item.basic];Integer custom=options.override("QuickPanel",uid);return custom==null?optionColor(item.basic):custom;}for(String uid:item.uids){Integer custom=options.override(item.group,uid);if(custom!=null)return custom;}for(String uid:item.uids)if(colorRows.containsKey(item.group+":"+uid))return currentColor(item.group+":"+uid);return 0xff181818;}
    void clearMapped(String group,String...uids){for(String uid:uids)options.overrides.remove(group+":"+uid);}
    void setSwatch(Swatch item,Integer color){
        if(item.basic>=0){int index=item.basic;setOptionColor(index,color==null?COLOR_DEFAULTS[index]:color);String[] ids=index==0?new String[]{"4-2-4"}:index==1?new String[]{"4-4-1","4-4-2","4-22-10","4-22-15","4-99-5","4-99-6"}:index==2?new String[]{"4-22-1","4-22-2","4-22-3","4-22-5","4-22-9","4-22-11","4-22-20","4-99-3","4-99-4"}:new String[]{"4-22-4","4-22-21","4-99-2"};clearMapped("QuickPanel",ids);}
        else for(String uid:item.uids){String key=item.group+":"+uid;if(color==null)options.overrides.remove(key);else options.overrides.put(key,color);}
        settingsChanged();
    }
    void resetSection(int mode){if(busy)return;String group=new String[]{"Keyboard","QuickPanel","Volume"}[mode];java.util.Iterator<String> it=options.overrides.keySet().iterator();while(it.hasNext())if(it.next().startsWith(group+":"))it.remove();if(mode==1){for(int i=0;i<4;i++)setOptionColor(i,COLOR_DEFAULTS[i]);options.quickIconsWhite=true;quickIconSlider.setWhite(true);}if(mode==2){options.volumeIconsWhite=true;volumeIconSlider.setWhite(true);}settingsChanged();}
    void selectPreview(int mode){
        previewMode=mode;android.view.ViewGroup old=(android.view.ViewGroup)preview.getParent();old.removeView(preview);
        LinearLayout card=previewCard;
        if(mode==2){volumeLayout.addView(preview,new LinearLayout.LayoutParams(dp(80),dp(230)));card.setVisibility(View.GONE);}else{card.addView(preview,full(-2));card.setVisibility(View.VISIBLE);}
        preview.setMode(mode);if(mode==2){preview.refreshOutputIcon();refreshVolume();}keyboardOptions.setVisibility(mode==0?View.VISIBLE:View.GONE);quickOptions.setVisibility(mode==1?View.VISIBLE:View.GONE);volumeLayout.setVisibility(mode==2?View.VISIBLE:View.GONE);
        for(int i=0;i<previewTabs.length;i++){boolean selected=i==mode;previewTabs[i].setSelected(selected);previewTabs[i].setTextColor(selected?TEXT:MUTED);previewTabs[i].setBackground(rounded(selected?0xff30333e:Color.TRANSPARENT,12));}
    }
    int optionColor(int index){return index==0?options.inactive:index==1?options.notificationBackground:index==2?options.notificationText:options.notificationIcons;}
    void setOptionColor(int index,int color){if(index==0)options.inactive=color;else if(index==1)options.notificationBackground=color;else if(index==2)options.notificationText=color;else options.notificationIcons=color;}
    void settingsChanged(){android.content.SharedPreferences.Editor editor=prefs.edit().putBoolean("quickIconsWhite",options.quickIconsWhite).putBoolean("volumeIconsWhite",options.volumeIconsWhite).putString("colorOverrides",overrideJson(options));for(int i=0;i<4;i++)editor.putInt(COLOR_KEYS[i],optionColor(i));editor.apply();refreshOptions();if(live)apply(seed);}
    void refreshOptions(){
        if(preview==null)return;preview.options=options.copy();preview.invalidate();
        for(Swatch item:swatches){int color=swatchColor(item);item.value.setText(ThemeOptions.hex(color));GradientDrawable dot=rounded(Palette.over(color,0xff000000),9);dot.setStroke(dp(1),0xff5a5d69);item.dot.setBackground(dot);item.view.setContentDescription(item.title+", "+ThemeOptions.hex(color)+". Edit color");}
    }
    void update(int color){seed=color;prefs.edit().putInt("seed",seed).apply();if(hex!=null)hex.setText(ThemeOptions.hex(seed));if(preview!=null){preview.seed=seed;preview.options=options.copy();preview.invalidate();}refreshOptions();if(activate!=null){activate.setBackground(rounded(seed,18));activate.setTextColor(Palette.on(Palette.over(seed,BACK)));}}
    void showStatus(String text){status.setText(text);status.setVisibility(View.VISIBLE);}
    void refreshButton(){for(Swatch item:swatches){item.view.setEnabled(!busy);item.view.setAlpha(busy?.45f:1f);}quickIconSlider.setEnabled(!busy);volumeIconSlider.setEnabled(!busy);activate.setEnabled(!busy);activate.setText(busy&&busyAction.equals("apply")?"Applying…":live?"Pause live color":"Apply color");restore.setEnabled(!busy);restore.setText(busy&&busyAction.equals("restore")?"Restoring…":"Restore previous colors");}
    void editSwatch(Swatch item){
        if(busy)return;int initial=swatchColor(item);
        LinearLayout content=new LinearLayout(this);content.setOrientation(1);content.setGravity(Gravity.CENTER);content.setPadding(dp(20),dp(16),dp(20),dp(12));
        TextView title=label(item.title,20,TEXT);title.setTypeface(Typeface.create("sans-serif-medium",0));content.addView(title);space(content,12);
        ColorPicker wheel=new ColorPicker(this);wheel.setColor(initial);content.addView(wheel,new LinearLayout.LayoutParams(dp(248),dp(248)));space(content,12);
        EditText exact=new EditText(this);exact.setSingleLine();exact.setText(ThemeOptions.wire(initial));exact.setTextColor(TEXT);exact.setTextSize(17);exact.setTypeface(Typeface.MONOSPACE);exact.setSelectAllOnFocus(true);exact.setGravity(Gravity.CENTER);exact.setBackground(rounded(0xff252731,12));exact.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);content.addView(exact,new LinearLayout.LayoutParams(dp(180),dp(48)));
        TextView opacityLabel=label("Opacity",12,MUTED);opacityLabel.setPadding(0,dp(14),0,0);content.addView(opacityLabel);
        SeekBar opacity=new SeekBar(this);opacity.setMax(255);opacity.setProgress(initial>>>24);opacity.setContentDescription(item.title+" opacity");content.addView(opacity,new LinearLayout.LayoutParams(-1,dp(40)));
        final boolean[] syncing={false};opacity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar bar,int alpha,boolean user){if(!user||syncing[0])return;try{int color=(ThemeOptions.rgb(exact.getText().toString())&0xffffff)|(alpha<<24);wheel.setColor(color);exact.setText(ThemeOptions.wire(color));}catch(Exception ignored){}}public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){}});
        exact.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int c,int f){}public void afterTextChanged(android.text.Editable e){}public void onTextChanged(CharSequence s,int a,int before,int count){try{int color=ThemeOptions.rgb(s.toString());wheel.setColor(color);syncing[0]=true;opacity.setProgress(color>>>24);syncing[0]=false;}catch(Exception ignored){}}});
        wheel.listener=(color,finished)->exact.setText(ThemeOptions.wire(color));space(content,12);
        LinearLayout actions=new LinearLayout(this);Button reset=plainButton("Reset",14);actions.addView(reset,new LinearLayout.LayoutParams(0,dp(48),1));Button use=plainButton("Use color",14);use.setTextColor(Palette.on(Palette.over(seed,BACK)));use.setBackground(rounded(Palette.over(seed,BACK),14));actions.addView(use,new LinearLayout.LayoutParams(0,dp(48),1));content.addView(actions,full(-2));
        ScrollView colorScroll=new ScrollView(this);colorScroll.addView(content);AlertDialog dialog=new AlertDialog.Builder(this).setView(colorScroll).create();dialog.setOnShowListener(v->{if(dialog.getWindow()!=null)dialog.getWindow().setBackgroundDrawable(rounded(BACK,24));});reset.setOnClickListener(v->{setSwatch(item,null);dialog.dismiss();});use.setOnClickListener(v->{int chosen;try{chosen=ThemeOptions.rgb(exact.getText().toString());}catch(IllegalArgumentException e){exact.setError("Use RRGGBB or AARRGGBB");return;}setSwatch(item,chosen);dialog.dismiss();});dialog.show();
    }
    void editHex(){EditText input=new EditText(this);input.setSingleLine();input.setText(ThemeOptions.wire(seed));input.setSelectAllOnFocus(true);input.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);input.setPadding(dp(24),dp(16),dp(24),dp(16));AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Exact color").setView(input).setNegativeButton("Cancel",null).setPositiveButton("Use color",null).create();dialog.setOnShowListener(v->dialog.getButton(-1).setOnClickListener(w->{String s=input.getText().toString().trim();int c;try{c=ThemeOptions.rgb(s);}catch(IllegalArgumentException error){input.setError("Use RRGGBB or AARRGGBB");return;}update(c);picker.setColor(c);if(live)apply(c);dialog.dismiss();}));dialog.show();}
    void addAccessibleControls(LinearLayout root){String[] names={"Hue","Saturation","Brightness"};for(int i=0;i<3;i++){final int channel=i;root.addView(label(names[i],12,MUTED));SeekBar bar=new SeekBar(this);bar.setMax(i==0?359:100);float[] hsv=new float[3];Color.colorToHSV(seed,hsv);bar.setProgress(Math.round(hsv[i]*(i==0?1:100)));bar.setContentDescription(names[i]);bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar b,int value,boolean user){if(!user)return;float[] h=new float[3];Color.colorToHSV(seed,h);h[channel]=value/(channel==0?1f:100f);update(Color.HSVToColor(Color.alpha(seed),h));picker.setColor(seed);}public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){if(live)apply(seed);}});root.addView(bar,full(48));}}
    void apply(int c){if(busy){pending=c;return;}run("apply",c);}
    static String rgb(int color){return ThemeOptions.wire(color);}
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
                String command="CLASSPATH="+quote(apk)+" /system/bin/app_process /system/bin dev.dov.onetint.RootBridge "+action+" "+user+" "+quote(apk)+" "+ThemeOptions.wire(color)+" "+quote(payload.getAbsolutePath())+" "+(applied.quickIconsWhite?"white":"black")+" "+rgb(applied.inactive)+" "+(applied.volumeIconsWhite?"white":"black")+" "+rgb(applied.notificationBackground)+" "+rgb(applied.notificationText)+" "+rgb(applied.notificationIcons)+" "+quote(overrideJson(applied));
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
                details="OneTint 0.2.1\n"+Build.MANUFACTURER+" "+Build.MODEL+"\nAndroid "+Build.VERSION.RELEASE+"\nBuild "+Build.DISPLAY+"\nAction: "+action+"\nSelected: "+ThemeOptions.hex(color)+"\nQuick Panel icons: "+(applied.quickIconsWhite?"White":"Black")+"\nInactive tiles: "+AmoledTheme.hex(applied.inactive)+"\nVolume icons: "+(applied.volumeIconsWhite?"White":"Black")+"\nNotification background: "+AmoledTheme.hex(applied.notificationBackground)+"\nNotification text: "+AmoledTheme.hex(applied.notificationText)+"\nNotification icons: "+AmoledTheme.hex(applied.notificationIcons)+"\nExit: "+code+"\n"+log;
                prefs.edit().putString("lastDetails",details).apply();
                if(ok){
                    if(action.equals("restore")){showStatus("Previous colors restored");live=false;refreshLauncher();}
                    else {JSONObject readback=response.optJSONObject("readback");boolean verified=readback!=null&&readback.optBoolean("verified");showStatus(verified?"Applied":"Could not verify color");prefs.edit().putInt("lastApplied",color).apply();if(response.optBoolean("appIconUpdated"))refreshLauncher();}
                } else {live=false;pending=null;showStatus("Could not apply color");new AlertDialog.Builder(this).setTitle("Apply needs attention").setMessage("The color could not be applied. You can copy the error for troubleshooting.").setPositiveButton("OK",null).setNeutralButton("Copy error",(a,b)->copy()).show();}
                refreshButton();if(pending!=null&&live){int next=pending;pending=null;if(next!=color)apply(next);}
    }
    public Object onRetainNonConfigurationInstance(){retained.busy=busy;retained.live=live;retained.action=busyAction;retained.pending=pending;retained.mode=previewMode;return retained;}
    String overrideJson(ThemeOptions settings){JSONObject json=new JSONObject();try{for(java.util.Map.Entry<String,Integer> entry:settings.overrides.entrySet())json.put(entry.getKey(),ThemeOptions.wire(entry.getValue()));}catch(JSONException ignored){}return json.toString();}
    final java.util.Map<String,android.content.res.Resources> targetResources=new java.util.HashMap<>();
    final java.util.Map<String,java.util.Map<String,java.util.List<String>>> targetAliases=new java.util.HashMap<>();
    boolean supportedColor(String target,JSONObject row){try{android.content.res.Resources res=targetResources.get(target);if(res==null){res=getPackageManager().getResourcesForApplication(target);targetResources.put(target,res);targetAliases.put(target,RootBridge.aliases(res,target));}JSONArray names=row.getJSONArray("names");for(int i=0;i<names.length();i++){String n=names.getString(i);if(res.getIdentifier(n,"color",target)!=0)return true;java.util.List<String> aliases=targetAliases.get(target).get(n);if(aliases!=null)for(String original:aliases)if(res.getIdentifier(original,"color",target)!=0)return true;}return false;}catch(Exception ignored){return false;}}
    void loadColorRows(){
        try{int oneui=80000;try{oneui=(Integer)Class.forName("android.os.SystemProperties").getMethod("getInt",String.class,int.class).invoke(null,"ro.build.version.oneui",80000);}catch(Exception ignored){}
            String version=oneui>=90000?"90000":oneui>=80000?"80000":oneui>=70000?"70000":"60000";
            String text;try(java.io.InputStream in=getAssets().open("catalog.json");java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream()){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)out.write(b,0,n);text=out.toString("UTF-8");}
            JSONArray groups=new JSONObject(text).getJSONArray(version);for(int i=0;i<groups.length();i++){JSONObject group=groups.getJSONObject(i);JSONArray rows=group.getJSONArray("rows");for(int j=0;j<rows.length();j++){JSONObject row=rows.getJSONObject(j);if(row.getString("type").equals("color")&&supportedColor(group.getString("target"),row))colorRows.putIfAbsent(group.getString("name")+":"+row.getString("uid"),row);}}
            JSONObject saved=new JSONObject(prefs.getString("colorOverrides","{}"));java.util.Iterator<String> keys=saved.keys();while(keys.hasNext()){String key=keys.next();if(colorRows.containsKey(key))try{options.overrides.put(key,ThemeOptions.rgb(saved.getString(key)));}catch(Exception ignored){}}
        }catch(Exception error){android.util.Log.w("OneTint","Color catalog",error);}
    }
    int currentColor(String key){try{JSONObject row=colorRows.get(key);String[] parts=key.split(":",2);String theme=AmoledTheme.theme(parts[0],parts[1],row.getString("theme"),options);boolean night=(getResources().getConfiguration().uiMode&48)==32;String[] pair=theme.split(",");String value=pair[night&&pair.length>1?1:0];return Palette.alpha(value.startsWith("#")?Color.parseColor(value):Palette.role(value,seed,night),AmoledTheme.opacity(parts[0],parts[1],row.getInt("opacity"),options));}catch(Exception ignored){return 0xff181818;}}
    void refreshVolume(){if(preview==null)return;try{if(audio==null)audio=(android.media.AudioManager)getSystemService(AUDIO_SERVICE);preview.mediaLevel=audio.getStreamVolume(android.media.AudioManager.STREAM_MUSIC);preview.mediaMax=audio.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC);preview.invalidate();}catch(RuntimeException ignored){preview.mediaLevel=0;preview.mediaMax=0;}}
    void refreshLauncher(){
        try{android.content.pm.PackageManager pm=getPackageManager();ComponentName a=new ComponentName(this,getPackageName()+".LauncherA"),b=new ComponentName(this,getPackageName()+".LauncherB");boolean useB=pm.getComponentEnabledSetting(b)==android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
            java.util.List<android.content.pm.PackageManager.ComponentEnabledSetting> settings=new java.util.ArrayList<>();settings.add(new android.content.pm.PackageManager.ComponentEnabledSetting(useB?a:b,android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED,android.content.pm.PackageManager.DONT_KILL_APP));settings.add(new android.content.pm.PackageManager.ComponentEnabledSetting(useB?b:a,android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED,android.content.pm.PackageManager.DONT_KILL_APP));pm.setComponentEnabledSettings(settings);
        }catch(RuntimeException error){android.util.Log.w("OneTint","Launcher icon refresh",error);}
    }
    protected void onResume(){super.onResume();preview.refreshOutputIcon();refreshVolume();if(!volumeReceiverRegistered){registerReceiver(volumeReceiver,new IntentFilter("android.media.VOLUME_CHANGED_ACTION"),Context.RECEIVER_NOT_EXPORTED);volumeReceiverRegistered=true;}}
    protected void onPause(){if(volumeReceiverRegistered){unregisterReceiver(volumeReceiver);volumeReceiverRegistered=false;}super.onPause();}
    protected void onDestroy(){if(prefs!=null)prefs.unregisterOnSharedPreferenceChangeListener(savedDetails);super.onDestroy();}
    void copy(){((ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("OneTint apply details",details));Toast.makeText(this,"Error copied",Toast.LENGTH_SHORT).show();}
}
