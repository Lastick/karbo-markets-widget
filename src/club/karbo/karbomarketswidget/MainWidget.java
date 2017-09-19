package club.karbo.karbomarketswidget;

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

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;

public class MainWidget extends AppWidgetProvider {
	
  final String LOG_TAG = "KarboWidget";
  private Timer mTimer;
  private MyTimerTask mMyTimerTask;
  static private boolean l = false;
  static private String show = "Wait...";

  @Override
  public void onEnabled(Context ctx){
    super.onEnabled(ctx);
    this.init();
    Log.d(LOG_TAG, "onEnabled");
  }

  @Override
  public void onUpdate(Context ctx, AppWidgetManager appWidgetManager, int[] appWidgetIds){
    super.onUpdate(ctx, appWidgetManager, appWidgetIds);
    RemoteViews widgetView = new RemoteViews(ctx.getPackageName(), R.layout.widget);
    widgetView.setTextViewText(R.id.MyText, show);
    appWidgetManager.updateAppWidget(appWidgetIds, widgetView);
    Log.d(LOG_TAG, "onUpdate");
    this.init();
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
    this.shutdown();
  }
  
  private void init(){
    if (!MainWidget.l){
      MainWidget.l = true;
      this.timer_init();
    }
    if(MainWidget.l) Log.d(LOG_TAG, "Collision init loop");
  }
  
  private void shutdown(){
    Log.d(LOG_TAG, "shutdown app");
    int pid = android.os.Process.myPid();
	android.os.Process.killProcess(pid);
  }

  private void timer_init(){
    this.mTimer = new Timer();
    this.mMyTimerTask = new MyTimerTask();
    this.mTimer.schedule(mMyTimerTask, 300000);
  }

  private class MyTimerTask extends TimerTask {
    @Override
    public void run(){
      Log.d(LOG_TAG, "loop");
      timer_init();
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
    	  Double target_sell = pairs.getJSONObject(3).getDouble("sell_active");
    	  MainWidget.show = target_name + "\n" + String.format("%1.4f", target_sell);
    	}
    } catch ( JSONException e) {
       e.printStackTrace();
    }

  }
 
}