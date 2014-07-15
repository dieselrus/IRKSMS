package ru.diesel_ru.irksms;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.os.AsyncTask;

public class SendSMSATask extends AsyncTask<String, Void, String> {
    /** The system calls this to perform work in a worker thread and
      * delivers it the parameters given to AsyncTask.execute() 
      * Љак использовать этот класс
      * public void onClick(View v) {
      * 	new SendSMSTask().execute("http://example.com/image.png");
      * }*/

	private String _cookie = "";
	
	//MainActivity ma = new MainActivity();
	
    protected String doInBackground(String... urls) {
        try {
        	// Џолучаем изображение капчи в отдельном потоке
        	return SendPost(urls[0], urls[1]);
			//return SendPost(urls[0], urls[1], urls[2], urls[3], urls[4] ,urls[5]);
		} catch (MalformedURLException e){
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
    }
    
	// ЋтправлЯем SMS  
    // new SendSMSTask().execute("http://irk.ru/sms/?", GetToken(_cookie), txtPhoneNumber.getText(), txtSMSText.getText() + "\n" + strMyName, strCaptcha0, txtCaptcha1.getText());
	public String SendPost(String httpURL, String data) throws IOException  
	//public String SendPost(String httpURL, String csrfmiddlewaretoken, String number, String message, String captcha_0, String captcha_1) throws IOException 
	{
		//Log.v("Url", httpURL);
		//Log.v("Data", data);
		
		
		URL url = new URL(httpURL);
		
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		
		connection.setDoOutput(true);
		connection.setInstanceFollowRedirects(false);
		connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:10.0.2) Gecko/20100101 Firefox/10.0.2");
		connection.setRequestProperty("Host", "www.irk.ru");
		connection.setRequestProperty("Referer","http://www.irk.ru/sms/");
		connection.setRequestMethod("POST");
                
		// If cookie exists, then send cookie
		//System.out.println("Data: "+ data);
		//System.out.println("cookie1: "+ _cookie);
		_cookie = MainActivity.getCoockie();
		//System.out.println("cookie2: "+ _cookie);
		
		if (_cookie != "") 
		{
			//Log.v("Cookie", _cookie);
			connection.setRequestProperty("Cookie", _cookie);
			connection.connect();
		}
          
		//System.out.println("cookie3: "+ _cookie);
		//System.out.println("data: "+ _cookie);
		
		// If Post Data not empty, then send POST Data
		if (data != "") 
		{
			//Log.v("POST", "Data not null");
			OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
			out.write(data);
			out.flush();
			out.close();
		}
        
		int status = connection.getResponseCode();
		//System.out.println("status: "+status);
		
		// Save Cookie
		//BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String headerName = null;
		//_cookies.clear();
		if (_cookie == "") 
		{
			for (int i=1; (headerName = connection.getHeaderFieldKey(i))!=null; i++) 
			{
				if (headerName.equalsIgnoreCase("Set-Cookie")) 
				{    
					String cookie = connection.getHeaderField(i);
					_cookie += cookie.substring(0,cookie.indexOf(";")) + "; ";
				}
			}
			
			MainActivity.setCoockie(_cookie);
		}
        
		if(Integer.toString(status).compareToIgnoreCase("302") == 0){
			return "true";
		} else {
			return "false";
		}
	}

	@Override
    protected void onProgressUpdate(Void... values) {
         super.onProgressUpdate(values);
    }

	// Ћбработка результата работы нового потока и взаимодействие с элементами основного потока
    protected void onPostExecute(String result) {
    	if (result == "true")
    	{
    		MainActivity.setMessageStatus(true);
    	} else {
    		MainActivity.setMessageStatus(false);
    	}
    }
}  
