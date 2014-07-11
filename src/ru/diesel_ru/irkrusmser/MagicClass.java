package ru.diesel_ru.irkrusmser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;

public class MagicClass extends AsyncTask<String, Void, String> {
	private static String strCaptcha0 = null;
	private static String strCsrfmiddlewaretoken = null;
	private static String strCaptcha1 = null;
	
	protected String doInBackground(String... _urls) {
		try {  			
   			//String url = "http://www.irk.ru/sms/check/?number=" + _phone;
   			
   			HttpClient client = new DefaultHttpClient();
   			HttpGet request = new HttpGet(_urls[0]);
   		 
   			// add request header
   			request.addHeader("User-Agent", MainActivity.USER_AGENT);
   			HttpResponse response = client.execute(request);
   		 
//   			System.out.println("Response Code : "  + response.getStatusLine().getStatusCode());
   		 
   			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
   		 
   			StringBuffer result = new StringBuffer();
   			String line = "";
   			while ((line = rd.readLine()) != null) {
   				result.append(line);
   			}
   			
   			//Log.d(LOG_TAG, "allpage = " + allpage.toString());
   			//return allpage.toString();
   			//System.out.println(result.toString());
   			return result.toString();
   			
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				//Log.d(LOG_TAG, "e1 = " + e.getMessage());
				return e.getMessage();
			} catch (IOException e) {
				e.printStackTrace();
				//Log.d(LOG_TAG, "e2 = " + e.getMessage());
				return e.getMessage();
			}
	}

	/*
	@Override
   protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
        // Показать диалог
        showDialog(PROGRESS_DLG_ID);
   }
	*/
   protected void onPostExecute(String result) {	   
	   MainActivity.setCaptcha0(result);	   
   }
   
   public static String getCaptcha0(){
	   return strCaptcha0;
   }
   
   public static String getCaptcha1(){
	   return strCaptcha1;
   }
   
   public static String getToken(){
	   return strCsrfmiddlewaretoken;
   }
}
