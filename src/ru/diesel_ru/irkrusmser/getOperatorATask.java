package ru.diesel_ru.irkrusmser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import org.json.JSONException;
import org.json.JSONObject;


import android.annotation.SuppressLint;
import android.os.AsyncTask;

public class getOperatorATask extends AsyncTask<String, Void, String> {
    /** The system calls this to perform work in a worker thread and
     * delivers it the parameters given to AsyncTask.execute() 
     * Љак использовать этот класс
     * public void onClick(View v) {
     * 	new DownloadImageTask().execute("http://example.com/image.png");
     * }*/
	
	final String LOG_TAG = "myLogs";
	
   protected String doInBackground(String... String) {
       // Џолучаем изображение капчи в отдельном потоке
		//String strPhoneNumber = getOperator(String[0]);
		//return getOperator(strPhoneNumber);
		return getOperator(String[0]);
   }

   private String getOperator(String _phone) {
		try {
   			
   			String url = "http://www.irk.ru/sms/check/?number=" + _phone;
   			 
   			HttpClient client = new DefaultHttpClient();
   			HttpGet request = new HttpGet(url);
   		 
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
        // Џоказать диалог
        showDialog(PROGRESS_DLG_ID);
   }
	*/
@SuppressLint("DefaultLocale")
protected void onPostExecute(String result) {
       //Log.d(LOG_TAG, "result = " + result);
       
       JSONObject json = new JSONObject();
	   try {

		   String strLimit = "0";
		   String strOperator = "none";
		   
		   json = new JSONObject(result);
		   strOperator = json.getString("operator").toUpperCase();
		   strLimit = json.getString("limit").toLowerCase();
           
           MainActivity.setMaxLeghtSMS(Integer.parseInt(strLimit));
	       MainActivity.setError("Оператор: " + strOperator + ". Лимит: " + strLimit + " символов.");
	        
	    }
	    catch (JSONException e) {
	        e.printStackTrace();
	        //MainActivity.setError(e.getMessage());
	        //Log.i(LOG_TAG,e.toString());
	    }
	    
	    try {

	        json = new JSONObject(result);
	        String strError = json.getString("error").toLowerCase();
	        //Log.d(LOG_TAG, "Error = " + strError);
	        MainActivity.setError(strError);
	    }
	    catch (JSONException e) {
	        e.printStackTrace();
	        //MainActivity.setError(e.getMessage());
	    }
   }
}
