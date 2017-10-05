package club.karbo.karbomarketswidget;

import java.util.Timer;
import java.util.TimerTask;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import club.karbo.karbomarketswidget.MarketsClient;

public class WidgetService extends Service {

  final String LOG_TAG = "KarboWidget";
  private Timer mTimer;
  private MyTimerTask mMyTimerTask;
  private Boolean client_status = false;
  private String markets_service_name = null;
  private String markets_service_ver = null;
  private Object[][] markets_pairs = null;
  private int markets_service_time = 0;
  private Boolean server_status = false;
  private long t = 100;

  public void onCreate(){
    super.onCreate();
	Log.d(LOG_TAG, "Service onCreate");
  }

  public int onStartCommand(Intent intent, int flags, int startId){
    Log.d(LOG_TAG, "Service onStartCommand");
    this.timer_init();
	return super.onStartCommand(intent, flags, startId);
  }

  public void onDestroy(){
    super.onDestroy();
    this.mTimer.cancel();
    Log.d(LOG_TAG, "Service onDestroy");
  }

  public IBinder onBind(Intent intent){
    Log.d(LOG_TAG, "Service onBind");
    return null;
  }

  private String formatDecimal(Double n){
    String res = "";
    res = String.format("%1.4f", n);
    return res;
  }

  private void sendTickers(){
    String[] ticker_text = new String[5];
    boolean[] ticker_status = new boolean[5];
    boolean service_status = false;
    Intent intent = new Intent();
    for (int i=0; i<5; i++){
      if (this.client_status){
        ticker_text[i] = this.markets_pairs[i][1] + ": ";
        ticker_text[i] += this.formatDecimal((Double) this.markets_pairs[i][2]);
        ticker_text[i] += "/" + this.formatDecimal((Double) this.markets_pairs[i][4]);
        ticker_status[i] = (boolean) (Boolean) this.markets_pairs[i][6];
        service_status = true;
        } else {
        ticker_text[i] = "- - -";
        ticker_status[i] = false;
      }
    }
    intent.setAction("club.karbo.action.tickers");
    intent.putExtra("ticker_text", ticker_text);
    intent.putExtra("ticker_status", ticker_status);
    intent.putExtra("service_status", service_status);
    sendBroadcast(intent);
  }

  private void getInfo(){
    MarketsClient client = new MarketsClient(null, null, null);
    client.doLoad();
    this.client_status = client.getStatus();
    this.markets_service_name = client.getName();
    this.markets_service_ver = client.getVer();
    this.markets_pairs = client.getPairs();
    this.markets_service_time = client.getTime();
    this.sendTickers();
  }

  private void timer_init(){
    this.mTimer = new Timer();
    this.mMyTimerTask = new MyTimerTask();
    this.mTimer.schedule(mMyTimerTask, this.t);
  }

  private class MyTimerTask extends TimerTask {
    @Override
	public void run(){
      Log.d(LOG_TAG, "Service loop");
      getInfo();
      t = 120000;
      timer_init();
    }
  }
	
}
