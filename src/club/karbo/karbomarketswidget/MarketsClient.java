package club.karbo.karbomarketswidget;

import java.io.IOException;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;

public class MarketsClient {

  private final String url_default = "http://stats.karbovanets.org/services/karbo-markets/api.php";
  private final String name_default = "MarketsWidget";
  private final String ver_default = "1.0";
  private final String LOG_TAG = "KarboWidget";
  private String url = null;
  private String name = null;
  private String ver = null;
  private Boolean client_status = false;
  private String markets_service_name = null;
  private String markets_service_ver = null;
  private Object[][] markets_pairs = null;
  private int markets_service_time = 0;

  public MarketsClient (String url, String name, String ver){
    if (url != null){
      this.url = url;
      } else {
      this.url = this.url_default;    	  
    }
    if (name != null && ver != null){
      this.name = name;
      this.ver = ver;
      } else {
      this.name = this.name_default;
      this.ver = this.ver_default;
    }
    this.markets_service_name = "";
    this.markets_service_ver = "";
    this.markets_service_time = 0;
    this.markets_pairs = null;
  }

  private void PairsValidator(JSONArray data) throws JSONException{
    this.markets_pairs = new Object[data.length()][7];
    for (int i = 0; i < data.length(); i++){
      this.markets_pairs[i][0] = (int) data.getJSONObject(i).getInt("id");
      this.markets_pairs[i][1] = new String(data.getJSONObject(i).getString("name"));
      this.markets_pairs[i][2] = (Double) data.getJSONObject(i).getDouble("buy_active");
      this.markets_pairs[i][3] = (Double) data.getJSONObject(i).getDouble("buy_effective");
      this.markets_pairs[i][4] = (Double) data.getJSONObject(i).getDouble("sell_active");
      this.markets_pairs[i][5] = (Double) data.getJSONObject(i).getDouble("sell_effective");
      this.markets_pairs[i][6] = (Boolean) data.getJSONObject(i).getBoolean("status");
    }
  }

  private void doClient(){
    String buff = "";
    int http_code = 0;
    Boolean client_status = false;
    HttpResponse response;
    HttpClient myClient = new DefaultHttpClient();
    HttpGet myConnection = new HttpGet(this.url);
    this.client_status = false;
    try {
      response = myClient.execute(myConnection);
      response.addHeader("User-Agent", this.name + "/" + this.ver);
      buff = EntityUtils.toString(response.getEntity(), "UTF-8");
      http_code = response.getStatusLine().getStatusCode();
      } catch (ClientProtocolException e){
        e.printStackTrace();
      } catch (IOException e){
        e.printStackTrace();
	}
    if (http_code == 200){
      try {
        JSONObject JsonObj = new JSONObject(buff);
        this.markets_service_name = JsonObj.getString("name");
        this.markets_service_ver = JsonObj.getString("version");
        Boolean status = JsonObj.getBoolean("status");
        if (status){
          client_status = true;
          this.client_status = true;
          JSONObject tickers = JsonObj.getJSONObject("tickers");
          this.markets_service_time = tickers.getInt("time");
          JSONArray pairs = tickers.getJSONArray("pairs");
          this.PairsValidator(pairs);
        }
      } catch (JSONException e){
        e.printStackTrace();
	  }
	}
    if (client_status){
      Log.d(LOG_TAG, "Client: success");
      } else {
      Log.d(LOG_TAG, "Client: fail");    	  
    }
  }
  
  public void doLoad(){
    this.markets_pairs = null;
    this.markets_service_name = "";
    this.markets_service_ver = "";
    this.markets_service_time = 0;
    this.doClient();
  }
  
  public String getName(){
    return this.markets_service_name;
  }

  public String getVer(){
    return this.markets_service_ver;
  }

  public Boolean getStatus(){
    return this.client_status;
  }

  public int getTime(){
    return this.markets_service_time;
  }

  public Object[][] getPairs(){
    return this.markets_pairs;
  }

}
