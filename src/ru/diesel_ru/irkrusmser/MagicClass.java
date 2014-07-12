package ru.diesel_ru.irkrusmser;

import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.StringTokenizer;

import android.os.AsyncTask;

public class MagicClass extends AsyncTask<String, Void, String> {
	private static String strCaptcha0 = null;
	private static String strCsrfmiddlewaretoken = null;
	private static String strCaptcha1 = null;
	
	protected String doInBackground(String... _urls) {			
			try
			{
				strCsrfmiddlewaretoken = null;
				strCaptcha0 = null;
				strCaptcha1 = null;
						
				// загрузка страницы
				URL url = new URL("http://188.120.235.71/getIrkSMSData.php");
				URLConnection conn = url.openConnection();

				//MainActivity.setCoockie(_cookie);
	               
				InputStreamReader rd = new InputStreamReader(conn.getInputStream());
				StringBuilder allpage = new StringBuilder();
				int n = 0;
				char[] buffer = new char[40000];
				while (n >= 0)
				{
					n = rd.read(buffer, 0, buffer.length);
					if (n > 0)
					{
						allpage.append(buffer, 0, n);                    
					}
				}
				
				System.out.println("http captcha: "+ allpage.toString());
				
				String _str = allpage.toString();
				
				StringTokenizer tokens = new StringTokenizer(_str, "|");
				strCsrfmiddlewaretoken = tokens.nextToken();// this will contain "Fruit"
				strCaptcha0 = tokens.nextToken();// this will contain " they taste good"
				strCaptcha1 = tokens.nextToken();
				
				System.out.println("strCsrfmiddlewaretoken: "+ strCsrfmiddlewaretoken);
				System.out.println("strCaptcha0: "+ strCaptcha0);
				System.out.println("strCaptcha1: "+ strCaptcha1);
				
				return strCaptcha1;
			}
			catch (Exception e)
			{
				System.out.println("http captcha: "+ e.getMessage());
				return e.getMessage().toString();
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
	   //System.out.println("Magic result: "+ result);
	   //MainActivity.setCaptcha1(result);	   
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
