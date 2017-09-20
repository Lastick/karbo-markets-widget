package club.karbo.karbomarketswidget;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.widget.RemoteViews;

public class MainWidget extends AppWidgetProvider {
	
  final String LOG_TAG = "KarboWidget";
  private Timer mTimer;
  private MyTimerTask mMyTimerTask;
  final String UPDATE_ALL_WIDGETS = "update_all_widgets";

  @Override
  public void onEnabled(Context ctx){
    super.onEnabled(ctx);
    this.WriteData("Wait...");
    Log.d(LOG_TAG, "onEnabled");
    Intent intent = new Intent(ctx, MainWidget.class);
    intent.setAction(UPDATE_ALL_WIDGETS);
    PendingIntent pIntent = PendingIntent.getBroadcast(ctx, 0, intent, 0);
    AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
    alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), 120000, pIntent);
  }

  @Override
  public void onUpdate(Context ctx, AppWidgetManager appWidgetManager, int[] appWidgetIds){
    super.onUpdate(ctx, appWidgetManager, appWidgetIds);
    RemoteViews widgetView = new RemoteViews(ctx.getPackageName(), R.layout.widget);
    widgetView.setTextViewText(R.id.MyText, this.ReadData());
    appWidgetManager.updateAppWidget(appWidgetIds, widgetView);
    Log.d(LOG_TAG, "onUpdate");
  }

  @Override
  public void onReceive(Context ctx, Intent intent){
    super.onReceive(ctx, intent);
    if (intent.getAction().equalsIgnoreCase(UPDATE_ALL_WIDGETS)){
      ComponentName thisAppWidget = new ComponentName(ctx.getPackageName(), getClass().getName());
      AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(ctx);
      int appWidgetIds[] = appWidgetManager.getAppWidgetIds(thisAppWidget);
      RemoteViews widgetView = new RemoteViews(ctx.getPackageName(), R.layout.widget);
      widgetView.setTextViewText(R.id.MyText, this.ReadData());
      appWidgetManager.updateAppWidget(appWidgetIds, widgetView);
      this.timer_init();
      Log.d(LOG_TAG, "onReceive");
    }
  }

  @Override
  public void onDeleted(Context ctx, int[] appWidgetIds){
    super.onDeleted(ctx, appWidgetIds);
    Log.d(LOG_TAG, "onDeleted");
  }

  @Override
  public void onDisabled(Context ctx){
    super.onDisabled(ctx);
    Log.d(LOG_TAG, "onDisabled");
    Intent intent = new Intent(ctx, MainWidget.class);
    intent.setAction(UPDATE_ALL_WIDGETS);
    PendingIntent pIntent = PendingIntent.getBroadcast(ctx, 0, intent, 0);
    AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
    alarmManager.cancel(pIntent);
    this.shutdown();
  }
  
  private void WriteData(String show){
	File StoragePath = Environment.getExternalStorageDirectory();
	File FileDat = new File(StoragePath, "karbomarketswidget.txt");
    try {
      BufferedWriter bw = new BufferedWriter(new FileWriter(FileDat));
      bw.write(show);
      bw.close();
      } catch (IOException e){
        e.printStackTrace();
    }
  }
  
  private String ReadData(){
	File StoragePath = Environment.getExternalStorageDirectory();
	File FileDat = new File(StoragePath, "karbomarketswidget.txt");
	String str = "";
	String line = "";
    try {
      BufferedReader br = new BufferedReader(new FileReader(FileDat));
      while ((line = br.readLine()) != null){
        str += line + "\n";
      }
      br.close();
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
    }
    str = str.substring(0, str.length() - 1);
    return str;
  }
  
  private void shutdown(){
    Log.d(LOG_TAG, "shutdown app");
    int pid = android.os.Process.myPid();
	android.os.Process.killProcess(pid);
  }

  private void timer_init(){
    this.mTimer = new Timer();
    this.mMyTimerTask = new MyTimerTask();
    this.mTimer.schedule(mMyTimerTask, 1000);
  }

  private class MyTimerTask extends TimerTask {
    @Override
    public void run(){
      Log.d(LOG_TAG, "loop");
      Socket_init();
    }
  }

  private void Socket_init(){
    String url = "http://stats.karbovanets.org/services/karbo-markets/api.php";
    String buff = "";
    HttpResponse response;
    HttpClient myClient = new DefaultHttpClient();
    HttpGet myConnection = new HttpGet(url);
    try {
      response = myClient.execute(myConnection);
      buff = EntityUtils.toString(response.getEntity(), "UTF-8");
      } catch (ClientProtocolException e){
        e.printStackTrace();
      } catch (IOException e){
        e.printStackTrace();
    }

    try {
    	JSONObject JsonObj = new JSONObject(buff);
    	//String name = JsonObj.getString("name");
    	//String version = JsonObj.getString("version");
    	Boolean status = JsonObj.getBoolean("status");
    	if (status){
    	  JSONObject tickers = JsonObj.getJSONObject("tickers");
    	  //int time = tickers.getInt("time");
    	  JSONArray pairs = tickers.getJSONArray("pairs");
    	  String target_name = pairs.getJSONObject(3).getString("name"); 
    	  String target_sell = String.format("%1.4f", pairs.getJSONObject(3).getDouble("sell_active"));
    	  String target_buy = String.format("%1.4f", pairs.getJSONObject(3).getDouble("buy_active"));
    	  this.WriteData(target_name + "\n" +  target_sell + "/" + target_buy);
    	}
    } catch ( JSONException e) {
       e.printStackTrace();
    }

  }
 
}