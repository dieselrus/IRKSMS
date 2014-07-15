package ru.diesel_ru.irksms;

import java.io.File;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.diesel_ru.irksms.R;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class MainActivity extends Activity implements AdListener {
	protected static final int PICK_RESULT = 0;
	protected static final int ReqCodeContact = 0;
	static final private int PHONE_NUMBER = 3;
	static String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:10.0.2) Gecko/20100101 Firefox/10.0.2";
	static Account[] AccountList;
	
	static TextView txtPhoneNumber;
	TextView txtSMSText;
	static TextView txtCaptcha1;
	static TextView txtError;
	TextView tvMessageText;
	ImageButton buttonSend;
	ImageButton buttonClean;
	ImageButton buttonSelectContact;
	ImageButton buttonSelectFavoritesContact;
//	ImageButton buttonSendFriend;
	static ImageView imgCaptcha;
	static ImageView imgStatus;
	LinearLayout mainView;
	
	private AdView adView;
	
    private static String _cookie = "";
    private static String strCaptcha0 = "";
    private static String strCsrfmiddlewaretoken = "";
    
    private String strMyName = "";
    private static String strTheme = "1";
    
    private static boolean blMagic = false;
    private static long longMagicDate = 0;
    
    private static boolean blClean = false;
    private boolean blCleaningCache = false;
    private static int MAX_LENGTH_SMS = 120;
    
    //Диалоговое окно прогресс бара
    final int PROGRESS_DLG_ID = 666;
    
    //ДлЯ сохранениЯ настроек
    SharedPreferences sp;
    final String LOG_TAG = "myLogs";
    
	// Определяем подключены ли к интернету
	public boolean isOnline() {
	    ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo nInfo = cm.getActiveNetworkInfo();
	    if (nInfo != null && nInfo.isConnected()) {
//	        Log.i(LOG_TAG, "ONLINE");
	        return true;
	    }
	    else {
//	        Log.i(LOG_TAG, "OFFLINE");
	        return false;
	    }
	}
	
	// Удаление дириктории кэша
	public static boolean deleteDir(File dir) {
		if (dir != null && dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}

		return dir.delete();
	}
	
	// Получение папки кэша приложения
	public void clearApplicationData() {
		File cache = getCacheDir();
		File appDir = new File(cache.getParent());
		if (appDir.exists()) {
			String[] children = appDir.list();
			for (String s : children) {
				if (!s.equals("lib") & !s.equals("shared_prefs")) {
					deleteDir(new File(appDir, s));
					//Log.i(LOG_TAG, "**************** File /data/data/APP_PACKAGE/" + s + " DELETED appDir = " + appDir);
				}
			}
		}
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);       
        
        /*  Аагружаем настройки. Если настроек с таким именем нету - 
        возвращаем второй аргумент. ‚ данном случае пробел.  */		
        
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        
        strMyName = sp.getString("Name","");
        
        blClean = sp.getBoolean("Clean", false);
        blCleaningCache = sp.getBoolean("CleaningCache", false);
        
        blMagic = sp.getBoolean("Magic", false);
        longMagicDate = sp.getLong("MagicDate",0);
        
        strTheme = sp.getString("Theme", "1");
        
        AccountManager manager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
        try {
        	AccountList = manager.getAccounts();
		} catch (Exception e) {
			AccountList = null;
		}
        
        // Очищаем кэш приложения
        if(blCleaningCache)     
	        clearApplicationData();
        
        new ConnectivityReceiver();
        
        //Создание adView ca-app-pub-9670568035952143/5674883316
        //adView = new AdView(this, AdSize.BANNER, "a1510fa3b8c4d5e");
        //adView = new AdView(this, AdSize.BANNER, "ca-app-pub-6935822903770431/6554126304"); dieselsoft38
        adView = new AdView(this, AdSize.BANNER, "ca-app-pub-9766418574743996/6181139065"); // dsoft38	
        
        // Set the AdListener.
        adView.setAdListener(this);
        
        // Поиск в LinearLayout (предполагается, что был назначен
        // атрибут android:id="@+id/mainLayout"
        LinearLayout layout = (LinearLayout)findViewById(R.id.admobLayout);

        // Добавление adView
        layout.addView(adView);

        AdRequest adRequest = new AdRequest();
        adRequest.addTestDevice(AdRequest.TEST_EMULATOR);         // Эмулятор
        adRequest.addTestDevice("064b623e0acc9b4c");              // Тестовое устройство Android
        adRequest.addTestDevice("E2C29F0145A0BFFBC0EF9BF36D436253"); // nexus 5
        
        // Инициирование общего запроса на загрузку вместе с объявлением
        adView.loadAd(new AdRequest());
        
        //isOnline();
        // найдем View-элементы
        txtPhoneNumber = (TextView) findViewById(R.id.editPhoneNumber);
        txtSMSText = (TextView) findViewById(R.id.editMessage);
        //txtName = (TextView) findViewById(R.id.editName);
        txtCaptcha1 = (TextView) findViewById(R.id.editCaptcha1);
        txtError = (TextView) findViewById(R.id.txtError);
        tvMessageText = (TextView) findViewById(R.id.textView2);
        
        buttonSend = (ImageButton) findViewById(R.id.btnSend);
        buttonSelectContact = (ImageButton) findViewById(R.id.btnContacts);
        buttonSelectFavoritesContact = (ImageButton) findViewById(R.id.btnFContacts);
        buttonClean = (ImageButton) findViewById(R.id.btnClean);
//        buttonSendFriend = (ImageButton) findViewById(R.id.sendFriend);
        imgCaptcha = (ImageView) findViewById(R.id.imageCaptcha1);
        imgStatus = (ImageView) findViewById(R.id.imageStatus); 
        mainView = (LinearLayout) findViewById(R.id.mainView);
        
        // Установка максимальной длины строки для текста СМС
        int maxLength = MAX_LENGTH_SMS - strMyName.length();
        
        imgStatus.setVisibility(View.INVISIBLE);
        
        setTheme(strTheme);
        
        Intent localIntent = getIntent();
        if (localIntent.getAction().contains("android.intent.action.SENDTO")){
        	txtPhoneNumber.setText(Uri.decode(localIntent.getData().toString()).replace("smsto:", "").replace("sms:", "").replace("+7", "8").replace("-", "").replace(" ", ""));
        	//txtPhoneNumber.setText(Uri.decode(localIntent.getData().toString()).replace("sms:", ""));
        }
        
        InputFilter[] fArray = new InputFilter[1];
        fArray[0] = new InputFilter.LengthFilter(maxLength);
        txtSMSText.setFilters(fArray);
        
        // Очищаем кэш приложения
        //clearApplicationData();
        
        // Проверка на подключение к Интернет
        if (isOnline() == true){
        	setError("Получение пин-кода...");
        	// Получаем капчу
        	new DownloadImageATask().execute("http://irk.ru/sms");
        }
        else{
//        	txtError.setText("Вы не подключены к сети Интернет.");
        	setError("Вы не подключены к сети Интернет.");
        }  
        // Обработчик нажатиЯ на кнопку отправить
        buttonSend.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		if (txtPhoneNumber.length() < 1)
        		{
        			Toast.makeText(getApplicationContext(), "Введите номер телефона!", Toast.LENGTH_SHORT).show();
        			return;
        		}
        		
        		if (txtSMSText.length() < 1)
        		{
        			Toast.makeText(getApplicationContext(), "Введите текст СМС", Toast.LENGTH_SHORT).show();
        			return;
        		}
        		
        		if (txtCaptcha1.length() < 1)
        		{
        			Toast.makeText(getApplicationContext(), "Введите пин-код!", Toast.LENGTH_SHORT).show();
        			return;
        		}
        		
        		if (isOnline() == true){
        			String data_s = "";
        			
        			// Состояние автораспознования
        			if(MainActivity.getBlMagic() && longMagicDate > System.currentTimeMillis() / 1000L){
        				data_s = "csrfmiddlewaretoken=" + strCsrfmiddlewaretoken + "&number=" + txtPhoneNumber.getText() + "&message=" + txtSMSText.getText() + "\n" + strMyName + "&captcha_0=" + strCaptcha0 + "&captcha_1=" + txtCaptcha1.getText();
        			} else {
        				data_s = "csrfmiddlewaretoken=" + GetToken(_cookie) + "&number=" + txtPhoneNumber.getText() + "&message=" + txtSMSText.getText() + "\n" + strMyName + "&captcha_0=" + strCaptcha0 + "&captcha_1=" + txtCaptcha1.getText();
        			}
        			
	        		imgStatus.setVisibility(View.INVISIBLE);
	        		setError("Отправка СМС...");
	        		new SendSMSATask().execute("http://irk.ru/sms/?", data_s);

	        		// Получаем капчу
	        		//new DownloadImageTask().execute("http://irk.ru/sms");
	        		setError("Получение пин-кода...");
	        		new DownloadImageATask().execute("http://irk.ru/sms");
	        		
	        		//InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE); 
	        		//inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
	        		
	        		// Прячем клавиатуру после отправки СМС
	        		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	        		imm.hideSoftInputFromWindow(txtCaptcha1.getWindowToken(), 0);
        		}
        		else{
        			Toast.makeText(getApplicationContext(), "Вы не подключены к сети Интернет.", Toast.LENGTH_SHORT).show();
        		}
        	}
        });
        
        // Обработчик нажатия на капчу (обновление капчи)
        imgCaptcha.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		imgStatus.setVisibility(View.INVISIBLE);
        		if (isOnline() == true){
	        		// Џолучаем капчу
        			setError("Получение пин-кода...");
        			new DownloadImageATask().execute("http://irk.ru/sms");
        		}
        		else{
        			Toast.makeText(getApplicationContext(), "Вы не подключены к сети Интернет.", Toast.LENGTH_SHORT).show();
        		}
        	}
        });
        
        // Обработчик выбора контакта
        buttonSelectContact.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		//imgStatus.setVisibility(View.INVISIBLE);
        		// Выбор только контактов звонков (без почтовых)
                Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT);
                pickIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                startActivityForResult(pickIntent, PICK_RESULT);
                imgStatus.setVisibility(View.INVISIBLE);
        	}
        });
        
        // Обработчик выбора избранного контакта
        buttonSelectFavoritesContact.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, FavContList.class);
			    startActivityForResult(intent, PHONE_NUMBER);
			    imgStatus.setVisibility(View.INVISIBLE);
        	}
        });
        
        // Обработчик нажатия на кнопку очистки
        buttonClean.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		txtSMSText.setText("");
        		imgStatus.setVisibility(View.INVISIBLE);
        	}
        });
         
        // Обработчик нажатия на кнопку отправки другу
//        buttonSendFriend.setOnClickListener(new View.OnClickListener() {
//        	public void onClick(View v) {
//        		txtSMSText.setText("Отправляй СМС бесплатно! https://play.google.com/store/apps/details?id=ru.diesel_ru.irkrusmser");
//        	}
//        });
        
        //Обработка ввода символов в текстовое поле для текста СМС
        txtSMSText.addTextChangedListener(new TextWatcher()  {
			@Override
			public void afterTextChanged(Editable s) {
				//imgStatus.setVisibility(View.INVISIBLE);
				tvMessageText.setText(getResources().getString(R.string.MessageText) + " (" + String.valueOf(MAX_LENGTH_SMS - strMyName.length() - txtSMSText.length()) + ")");
			}
 
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
	
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

        });       
        // Обработка поления фокуса полем для текста СМС
        txtSMSText.setOnFocusChangeListener(new OnFocusChangeListener() {		
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				//Log.i(LOG_TAG, "hasFocus = " + hasFocus);
				if ((txtPhoneNumber.length() > 0) & hasFocus){
					setError("Определение оператора...");
					new getOperatorATask().execute(txtPhoneNumber.getText().toString());
					//new getOperatorTask().execute("9149506721");
				}
//				imgStatus.setVisibility(View.INVISIBLE);
			}
		});
    }
  
    // Закрытие приложениЯ
	@Override
    protected void onStop(){
       super.onStop();
    }
    
	@Override
    protected void onResume() {
		//Log.d(LOG_TAG, "onResume");
        strMyName = sp.getString("Name", "");
        blClean = sp.getBoolean("Clean", false);
        blCleaningCache = sp.getBoolean("CleaningCache", false);
        strTheme = sp.getString("Theme", "1");
        setTheme(strTheme);
        
        longMagicDate = sp.getLong("MagicDate", 0);
        blMagic = sp.getBoolean("Magic", false);
        
		super.onResume();
    }

    @Override
    protected void onPause() {
		//Log.d(LOG_TAG, "onPause");
		super.onPause();
    }
    
    @Override
    protected void onStart() {
		//Log.d(LOG_TAG, "onStart");
		strMyName = sp.getString("Name", "");
		blClean = sp.getBoolean("Clean", false);
		blCleaningCache = sp.getBoolean("CleaningCache", false);
		strTheme = sp.getString("Theme", "1");
        setTheme(strTheme);
        
        longMagicDate = sp.getLong("MagicDate", 0);
        blMagic = sp.getBoolean("Magic", false);
        
		super.onStart();
    }
    
    // Обработка выбора контакта
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
    	//Log.v("requestCode", String.valueOf(requestCode));
    	//Log.v("resultCode", String.valueOf(requestCode));
    	//Log.v("data", String.valueOf(requestCode));
    	// Получаем номер из избранных контактов
    	if (requestCode == PHONE_NUMBER) {
    		if (resultCode == RESULT_OK) {
    			String thiefname = data.getStringExtra(FavContList.PHONE_NUMBER);
    			txtPhoneNumber.setText(thiefname.replace("+7", "8").replace("-", "").replace(" ", ""));
    		}else {
    			txtPhoneNumber.setText(""); // стираем текст
    		}
    	}
    	// Получаем номер из активити контактов
        if (data != null) {
            Uri uri = data.getData();

            if (uri != null) {
                Cursor c = null;
                try {
                	c = getContentResolver().query(uri, new String[]{ 
                            ContactsContract.CommonDataKinds.Phone.NUMBER,
                            ContactsContract.CommonDataKinds.Phone.TYPE},
                        null, null, null);

                    if (c != null && c.moveToFirst()) {
                        String number = c.getString(0);
                        int type = c.getInt(1);
                        showSelectedNumber(type, number);
                    }
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }
            }
        }
    }

    // присваивание значениЯ номера телефона 
    public void showSelectedNumber(int type, String number) {
    	txtPhoneNumber.setText(number.replace("+7", "8").replace("-", "").replace(" ", ""));      
    }

	// Џолучаем токен из куки
	public String GetToken(String data)
	{
		String matchtoken = "";
		// работаем с регулЯрками
		//Log.v("coocies",data);
		final Pattern pattern = Pattern.compile ("csrftoken=([a-zA-Z0-9]+);");
		Matcher matcher = pattern.matcher(data);
		if (matcher.find())
		{    
			matchtoken = matcher.group(1);            
		} 
		//Log.v("coocies_t",matchtoken);
		return matchtoken;
	}
    
	// Создание меню	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.main, menu);
        //menu.add("menu1");
        //return true;
	      MenuItem mi = menu.add(0, 1, 0, "Настройки");
	      mi.setIntent(new Intent(this, PrefActivity.class));
	      mi = menu.add(0, 1, 0, "О программе");
	      mi.setIntent(new Intent(this, Abaut.class));
	      
	      return super.onCreateOptionsMenu(menu);
    }
	
	// Устанавливаем куки
	public static void setCoockie (String cookie){
		_cookie = cookie;		
	}
	
	// Получаем куки
	public static String getCoockie (){
		return _cookie;		
	}
	
	// Проверяем включена ли очистка капчи
	public static boolean getBlClean (){
		return blClean;		
	}
	
	// Проверяем включен ли режим автораспознования
	public static boolean getBlMagic (){
		return blMagic;		
	}
	
	// Проверяем время до которго разрешено автораспознование
	public static long getlongMagic (){
		return longMagicDate;		
	}
		
	// Устанавливаем изображение капчи
	public static void setBitmapCaptcha (Bitmap img){
	   	if(blClean){
	   		imgCaptcha.setBackgroundColor(Color.WHITE);
	   		//imgCaptcha.setImageBitmap(invert(CleanImage(CleanImage(adjustedContrast(invert(result),300)))));
	   	}
	   	else{
	   		imgCaptcha.setBackgroundColor(Color.alpha(0));       		
	   	}
	   	
	   	imgCaptcha.setImageBitmap(img);	
	}
	
	/**
	 * Процедура устанавливает статус отправленного сообщения
	 */
	public static void setMessageStatus(Boolean _status){
    	
		if (_status)
		{
			if(strTheme.compareToIgnoreCase("1") == 0)
			{
				imgStatus.setBackgroundResource(R.drawable.status_ok_blue);
			} else {
				imgStatus.setBackgroundResource(R.drawable.status_ok_white);
			}
			
			imgStatus.setVisibility(View.VISIBLE);
			setError("Сообщение для " + txtPhoneNumber.getText().toString() + " доставлено на сервер.");
		} 
		else if (!_status) 
		{
			if(strTheme.compareToIgnoreCase("1") == 0)
			{
				imgStatus.setBackgroundResource(R.drawable.status_no_blue);
			} else {
				imgStatus.setBackgroundResource(R.drawable.status_no_white);
			}
			setError("Возможно сообщение для " + txtPhoneNumber.getText().toString() + " не доставлено на сервер.");
			imgStatus.setVisibility(View.VISIBLE);
		}
		
    	txtCaptcha1.setText("");
	}

	// Установить переменную капчи
	public static void setCaptcha0(String str) {
		strCaptcha0 = str;
	}
	
	public static void setMaxLeghtSMS(int parseInt) {
//		pdPNum.dismiss();
		MAX_LENGTH_SMS = parseInt;
	}
	// Выводим сообщение об ошибках
	public static void setError(String strError) {
		//txtError.setText(strError);
		txtError.append(strError + "\n");
	}
	// Установка капчи
	public static void setCaptcha1(String str){
		txtCaptcha1.setText(str);
	}

	// Установка капчи
	public static void setCsrfmiddlewaretoken(String str) {
		strCsrfmiddlewaretoken = str;
	}
		
	private void setTheme(String _theme){
		if(_theme.compareToIgnoreCase("1") == 0){
			
			buttonClean.setBackgroundResource(R.drawable.clean_blue);
			buttonSelectContact.setBackgroundResource(R.drawable.contact_blue);
			buttonSelectFavoritesContact.setBackgroundResource(R.drawable.star_blue);
			buttonSend.setBackgroundResource(R.drawable.send_blue);
			//imgCaptcha.setBackgroundResource(R.drawable.load_blue);
			
			//mainView.setBackgroundColor(color.textColor);
			mainView.setBackgroundColor(Color.parseColor("#ffffff"));
			
			RelativeLayout rl =(RelativeLayout) findViewById(R.id.rl);
			rl.setBackgroundColor(Color.parseColor("#ffffff"));
			
			TextView tv1 = (TextView) findViewById(R.id.textView1);
			tv1.setTextColor(Color.parseColor("#414141"));
			
			TextView tv2 = (TextView) findViewById(R.id.textView2);
			tv2.setTextColor(Color.parseColor("#414141"));
			
			TextView tv3 = (TextView) findViewById(R.id.textView3);
			tv3.setTextColor(Color.parseColor("#414141"));
			
			TextView tv4 = (TextView) findViewById(R.id.txtError);
			tv4.setTextColor(Color.parseColor("#414141"));			
			
		} else {
	
			buttonClean.setBackgroundResource(R.drawable.clean_white);
			buttonSelectContact.setBackgroundResource(R.drawable.contact_white);
			buttonSelectFavoritesContact.setBackgroundResource(R.drawable.star_white);
			buttonSend.setBackgroundResource(R.drawable.send_white);
			//imgCaptcha.setBackgroundResource(R.drawable.load_white);
			
			mainView.setBackgroundColor(Color.parseColor("#414141"));
			
			RelativeLayout rl =(RelativeLayout) findViewById(R.id.rl);
			rl.setBackgroundColor(Color.parseColor("#414141"));
			
			TextView tv1 = (TextView) findViewById(R.id.textView1);
			tv1.setTextColor(Color.parseColor("#ffffff"));
			
			TextView tv2 = (TextView) findViewById(R.id.textView2);
			tv2.setTextColor(Color.parseColor("#ffffff"));
			
			TextView tv3 = (TextView) findViewById(R.id.textView3);
			tv3.setTextColor(Color.parseColor("#ffffff"));
			
			TextView tv4 = (TextView) findViewById(R.id.txtError);
			tv4.setTextColor(Color.parseColor("#ffffff"));
			
		}
	}
	
	  /** Called when an ad is clicked and about to return to the application. */
	@Override
	  public void onDismissScreen(Ad ad) {
	    //Log.d(LOG_TAG, "onDismissScreen");
	    //Toast.makeText(this, "onDismissScreen", Toast.LENGTH_SHORT).show();
	  }

	  /** Called when an ad was not received. */
	  @Override
	  public void onFailedToReceiveAd(Ad ad, AdRequest.ErrorCode error) {
	    //String message = "onFailedToReceiveAd (" + error + ")";
	    //Log.d(LOG_TAG, message);
	    //Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	  }

	  /**
	   * Called when an ad is clicked and going to start a new Activity that will
	   * leave the application (e.g. breaking out to the Browser or Maps
	   * application).
	   */
	  @Override
	  public void onLeaveApplication(Ad ad) {
	    //Log.d(LOG_TAG, "onLeaveApplication");
	    //Toast.makeText(this, "onLeaveApplication", Toast.LENGTH_SHORT).show();
	  }

	  /**
	   * Called when an Activity is created in front of the app (e.g. an
	   * interstitial is shown, or an ad is clicked and launches a new Activity).
	   */
	  @Override
	  public void onPresentScreen(Ad ad) {
	    //Log.d(LOG_TAG, "onPresentScreen");
	    //Toast.makeText(this, "onPresentScreen", Toast.LENGTH_SHORT).show();
	    
	    long unixTime = System.currentTimeMillis() / 1000L + 24 * 60 * 60;

		longMagicDate = unixTime;
		//System.out.println("longMagicDate: " + unixTime);
		Editor e = sp.edit();
		e.putLong("MagicDate", unixTime);
		e.commit();
	  }

	  /** Called when an ad is received. */
	  @Override
	  public void onReceiveAd(Ad ad) {
	    //Log.d(LOG_TAG, "onReceiveAd");
	    //Toast.makeText(this, "onReceiveAd", Toast.LENGTH_SHORT).show();
	  }
}
	