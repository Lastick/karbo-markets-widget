package club.karbo.karbomarketswidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.widget.RemoteViews;

public class MainWidget extends AppWidgetProvider {

  private final String LOG_TAG = "KarboWidget";

  @Override
  public void onEnabled(Context ctx){
    super.onEnabled(ctx);
    Log.d(LOG_TAG, "onEnabled");
    ctx.startService(new Intent(ctx, WidgetService.class));
  }

  @Override
  public void onUpdate(Context ctx, AppWidgetManager appWidgetManager, int[] appWidgetIds){
    super.onUpdate(ctx, appWidgetManager, appWidgetIds);
    Log.d(LOG_TAG, "onUpdate");
  }

  @Override
  public void onReceive(Context ctx, Intent intent){
    super.onReceive(ctx, intent);
    if (intent.getAction().equalsIgnoreCase("club.karbo.action.tickers")){
      ComponentName thisAppWidget = new ComponentName(ctx.getPackageName(), getClass().getName());
      AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(ctx);
      int appWidgetIds[] = appWidgetManager.getAppWidgetIds(thisAppWidget);
      RemoteViews widgetView = new RemoteViews(ctx.getPackageName(), R.layout.widget);
      appWidgetManager.updateAppWidget(appWidgetIds, this.showTickers(intent, widgetView, ctx));
      Log.d(LOG_TAG, "onReceive service");
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
    ctx.stopService(new Intent(ctx, WidgetService.class));
  }

  private RemoteViews showTickers(Intent intent, RemoteViews widgetView, Context ctx){
    Resources res = ctx.getResources();
    boolean service_status = intent.getBooleanExtra("service_status", false);
    String[] ticker_text = intent.getStringArrayExtra("ticker_text");
    boolean[] ticker_status = intent.getBooleanArrayExtra("ticker_status");
    widgetView.setTextViewText(R.id.ticker_one, ticker_text[0]);
    widgetView.setTextViewText(R.id.ticker_two, ticker_text[1]);
    widgetView.setTextViewText(R.id.ticker_three, ticker_text[2]);
    widgetView.setTextViewText(R.id.ticker_four, ticker_text[3]);
    widgetView.setTextViewText(R.id.ticker_five, ticker_text[4]);
    if (ticker_status[0]){
      widgetView.setTextColor(R.id.ticker_one, res.getColor(R.color.ticker_active));
      } else {
      widgetView.setTextColor(R.id.ticker_one, res.getColor(R.color.ticker_unactive));
    }
    if (ticker_status[1]){
      widgetView.setTextColor(R.id.ticker_two, res.getColor(R.color.ticker_active));
      } else {
      widgetView.setTextColor(R.id.ticker_two, res.getColor(R.color.ticker_unactive));
    }
    if (ticker_status[2]){
      widgetView.setTextColor(R.id.ticker_three, res.getColor(R.color.ticker_active));
      } else {
      widgetView.setTextColor(R.id.ticker_three, res.getColor(R.color.ticker_unactive));
    }
    if (ticker_status[3]){
      widgetView.setTextColor(R.id.ticker_four, res.getColor(R.color.ticker_active));
      } else {
      widgetView.setTextColor(R.id.ticker_four, res.getColor(R.color.ticker_unactive));
    }
    if (ticker_status[4]){
      widgetView.setTextColor(R.id.ticker_five, res.getColor(R.color.ticker_active));
      } else {
      widgetView.setTextColor(R.id.ticker_five, res.getColor(R.color.ticker_unactive));
    }
    return widgetView;
  } 
 
}