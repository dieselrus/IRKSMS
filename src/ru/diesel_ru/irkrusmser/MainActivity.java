package ru.diesel_ru.irkrusmser;

import java.io.File;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.diesel_ru.irkrusmser.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class MainActivity extends Activity {
	protected static final int PICK_RESULT = 0;
	protected static final int ReqCodeContact = 0;
	static final private int PHONE_NUMBER = 3;
	static String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:10.0.2) Gecko/20100101 Firefox/10.0.2"; 
	
	TextView txtPhoneNumber;
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
//	static ImageView imgStatus;
	LinearLayout mainView;
	
	AdView adView;
	
    private static String _cookie = "";
    private static String strCaptcha0 = "";
    private String strMyName = "";
    private static String strTheme = "1";
    private static boolean blClean = false;
    private boolean blCleaningCache = false;
    private static int MAX_LENGTH_SMS = 120;
    
    //���������� ���� �������� ����
    final int PROGRESS_DLG_ID = 666;
    //������ ��������
//    private static ProgressDialog pd;
//    private static ProgressDialog pdSMS;
//    private static ProgressDialog pdPNum;
    
    //��� ���������� ��������
    SharedPreferences sp;
    final String LOG_TAG = "myLogs";
    
	// ���������� ���������� �� � ���������
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
	
	// �������� ���������� ����
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
	
	// ��������� ����� ���� ����������
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
        
        /*  ��������� ���������. ���� �������� � ����� ������ ���� - 
        ���������� ������ ��������. � ������ ������ ������.  */		
        
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        
        strMyName = sp.getString("Name","");
        blClean = sp.getBoolean("Clean", false);
        blCleaningCache = sp.getBoolean("CleaningCache", false);
        
        strTheme = sp.getString("Theme", "1");
        
        // ������� ��� ����������
        if(blCleaningCache)     
	        clearApplicationData();
        
        new ConnectivityReceiver();
        
        //�������� adView ca-app-pub-9670568035952143/5674883316
        //adView = new AdView(this, AdSize.BANNER, "a1510fa3b8c4d5e");
        adView = new AdView(this, AdSize.BANNER, "ca-app-pub-9670568035952143/5674883316");
        										    
        // ����� � LinearLayout (��������������, ��� ��� ��������
        // ������� android:id="@+id/mainLayout"
        LinearLayout layout = (LinearLayout)findViewById(R.id.admobLayout);

        // ���������� adView
        layout.addView(adView);

        // ������������� ������ ������� �� �������� ������ � �����������
        adView.loadAd(new AdRequest());
        
        //isOnline();
        // ������ View-��������
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
//        imgStatus = (ImageView) findViewById(R.id.imageStatus); 
        mainView = (LinearLayout) findViewById(R.id.mainView);
        
        // ��������� ������������ ����� ������ ��� ������ ���
        int maxLength = MAX_LENGTH_SMS - strMyName.length();
        
        setTheme(strTheme);
        
        Intent localIntent = getIntent();
        if (localIntent.getAction().contains("android.intent.action.SENDTO")){
        	txtPhoneNumber.setText(Uri.decode(localIntent.getData().toString()).replace("smsto:", "").replace("sms:", "").replace("+7", "8").replace("-", "").replace(" ", ""));
        	//txtPhoneNumber.setText(Uri.decode(localIntent.getData().toString()).replace("sms:", ""));
        }
        
        InputFilter[] fArray = new InputFilter[1];
        fArray[0] = new InputFilter.LengthFilter(maxLength);
        txtSMSText.setFilters(fArray);
        
        // ������� ��� ����������
        //clearApplicationData();
        
        // �������� �� ����������� � ��������
        if (isOnline() == true){
//        	pd = ProgressDialog.show(MainActivity.this, "���������...", "��������� ���-����", true, false);
        	setError("��������� ���-����...");
        	// �������� �����
        	//new DownloadImageTask().execute("http://irk.ru/sms");
        	new DownloadImageATask().execute("http://irk.ru/sms");
        }
        else{
//        	txtError.setText("�� �� ���������� � ���� ��������.");
        	setError("�� �� ���������� � ���� ��������.");
        }  
        // ���������� ������� �� ������ ���������
        buttonSend.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		if (txtPhoneNumber.length() < 1)
        		{
        			Toast.makeText(getApplicationContext(), "������� ����� ��������!", Toast.LENGTH_SHORT).show();
        			return;
        		}
        		
        		if (txtSMSText.length() < 1)
        		{
        			Toast.makeText(getApplicationContext(), "������� ����� ���", Toast.LENGTH_SHORT).show();
        			return;
        		}
        		
        		if (txtCaptcha1.length() < 1)
        		{
        			Toast.makeText(getApplicationContext(), "������� ���-���!", Toast.LENGTH_SHORT).show();
        			return;
        		}
        		
        		if (isOnline() == true){
	        		String data_s = "csrfmiddlewaretoken=" + GetToken(_cookie) + "&number=" + txtPhoneNumber.getText() + "&message=" + txtSMSText.getText() + "\n" + strMyName + "&captcha_0=" + strCaptcha0 + "&captcha_1=" + txtCaptcha1.getText();
//	        		imgStatus.setVisibility(View.INVISIBLE);
//	        		pdSMS = ProgressDialog.show(MainActivity.this, "���������...", "�������� ���", true, false);
	        		setError("�������� ���...");
	        		//new SendSMSTask().execute("http://irk.ru/sms/?", data_s);
	        		new SendSMSATask().execute("http://irk.ru/sms/?", data_s);
	        		//new SendSMSTask().execute("http://irk.ru/sms/?", GetToken(_cookie), txtPhoneNumber.getText().toString(), txtSMSText.getText().toString() + "\n" + strMyName, strCaptcha0, txtCaptcha1.getText().toString());
	        			//Log.v("status", "SEND");
	        		//pd = ProgressDialog.show(MainActivity.this, "���������...", "��������� ���-����", true, false);
	        		// �������� �����
	        		//new DownloadImageTask().execute("http://irk.ru/sms");
	        		setError("��������� ���-����...");
	        		new DownloadImageATask().execute("http://irk.ru/sms");
	        		
        		}
        		else{
        			Toast.makeText(getApplicationContext(), "�� �� ���������� � ���� ��������.", Toast.LENGTH_SHORT).show();
        		}
        	}
        });
        
        // ���������� ������� �� ����� (���������� �����)
        imgCaptcha.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		if (isOnline() == true){
//        			pd = ProgressDialog.show(MainActivity.this, "���������...", "��������� ���-����", true, false);
	        		// �������� �����
        			//new DownloadImageTask().execute("http://irk.ru/sms");
        			setError("��������� ���-����...");
        			new DownloadImageATask().execute("http://irk.ru/sms");
        		}
        		else{
        			Toast.makeText(getApplicationContext(), "�� �� ���������� � ���� ��������.", Toast.LENGTH_SHORT).show();
        		}
        	}
        });
        
        // ���������� ������ ��������
        buttonSelectContact.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		// ����� ������ ��������� ������� (��� ��������)
                Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT);
                pickIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                startActivityForResult(pickIntent, PICK_RESULT);
//                imgStatus.setVisibility(View.INVISIBLE);
        	}
        });
        
        // ���������� ������ ���������� ��������
        buttonSelectFavoritesContact.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, FavContList.class);
			    startActivityForResult(intent, PHONE_NUMBER);
//			    imgStatus.setVisibility(View.INVISIBLE);
        	}
        });
        
        // ���������� ������� �� ������ �������
        buttonClean.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		txtSMSText.setText("");
//        		imgStatus.setVisibility(View.INVISIBLE);
        	}
        });
         
        // ���������� ������� �� ������ �������� �����
//        buttonSendFriend.setOnClickListener(new View.OnClickListener() {
//        	public void onClick(View v) {
//        		txtSMSText.setText("��������� ��� ���������! https://play.google.com/store/apps/details?id=ru.diesel_ru.irkrusmser");
//        	}
//        });
        
        //��������� ����� �������� � ��������� ���� ��� ������ ���
//		tvMessageText.setText("@+id/editPhoneNumber" + " (" + String.valueOf(200 - strMyName.length() - txtSMSText.length()) + ")");
        txtSMSText.addTextChangedListener(new TextWatcher()  {
			@Override
			public void afterTextChanged(Editable s) {
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
        // ��������� ������� ������ ����� ��� ������ ���
        txtSMSText.setOnFocusChangeListener(new OnFocusChangeListener() {		
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				//Log.i(LOG_TAG, "hasFocus = " + hasFocus);
				if ((txtPhoneNumber.length() > 0) & hasFocus){
//					pdPNum = ProgressDialog.show(MainActivity.this, "���������...", "���� ����������� ���������", true, false);
					setError("����������� ���������...");
					//new getOperatorTask().execute(txtPhoneNumber.getText().toString());
					new getOperatorATask().execute(txtPhoneNumber.getText().toString());
					//new getOperatorTask().execute("9149506721");
				}
//				imgStatus.setVisibility(View.INVISIBLE);
			}
		});
    }
  
    // �������� ����������
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
		super.onStart();
    }
    
    // ��������� ������ ��������
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
    	//Log.v("requestCode", String.valueOf(requestCode));
    	//Log.v("resultCode", String.valueOf(requestCode));
    	//Log.v("data", String.valueOf(requestCode));
    	// �������� ����� �� ��������� ���������
    	if (requestCode == PHONE_NUMBER) {
    		if (resultCode == RESULT_OK) {
    			String thiefname = data.getStringExtra(FavContList.PHONE_NUMBER);
    			txtPhoneNumber.setText(thiefname.replace("+7", "8").replace("-", "").replace(" ", ""));
    		}else {
    			txtPhoneNumber.setText(""); // ������� �����
    		}
    	}
    	// �������� ����� �� �������� ���������
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

    // ������������ �������� ������ �������� 
    public void showSelectedNumber(int type, String number) {
    	txtPhoneNumber.setText(number.replace("+7", "8").replace("-", "").replace(" ", ""));      
    }

	// �������� ����� �� ����
	public String GetToken(String data)
	{
		String matchtoken = "";
		// �������� � �����������
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
    
	// �������� ����	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.main, menu);
        //menu.add("menu1");
        //return true;
	      MenuItem mi = menu.add(0, 1, 0, "���������");
	      mi.setIntent(new Intent(this, PrefActivity.class));
	      mi = menu.add(0, 1, 0, "� ���������");
	      mi.setIntent(new Intent(this, Abaut.class));
	      
	      return super.onCreateOptionsMenu(menu);
    }
	
	// ������������� ����
	public static void setCoockie (String cookie){
		_cookie = cookie;		
	}
	
	// �������� ����
	public static String getCoockie (){
		return _cookie;		
	}
	
	// ��������� �������� �� ������� �����
	public static boolean getBlClean (){
		return blClean;		
	}
	
	// ������������� ����������� �����
	public static void setBitmapCaptcha (Bitmap img){
	   	if(blClean){
	   		imgCaptcha.setBackgroundColor(Color.WHITE);
	   		//imgCaptcha.setImageBitmap(invert(CleanImage(CleanImage(adjustedContrast(invert(result),300)))));
	   	}
	   	else{
	   		imgCaptcha.setBackgroundColor(Color.alpha(0));       		
	   	}
	   	
//	   	pd.dismiss();
	   	//setError("");
	   	imgCaptcha.setImageBitmap(img);	
	}
	
	/**
	 * ��������� ������������� ������ ������������� ���������
	 */
	public static void setMessageStatus(Boolean _status){
		// ���������� ���� �������
//    	pdSMS.dismiss();
    	
		if (_status)
		{    
			//matchtoken = matcher.group(1); 
//			imgStatus.setVisibility(View.VISIBLE);
			//Log.v("matcher", matchtoken);
			setError("��������� ���������� �� ������.");
		} 
		else
		{
			setError("�������� ��������� �� ���������� �� ������.");
//			imgStatus.setImageDrawable(getResources().getDrawable(R.drawable.error));
//			imgStatus.setVisibility(View.VISIBLE);
		}
		
    	txtCaptcha1.setText("");
	}

	// ���������� ���������� �����
	public static void setCaptcha0(String str) {
		strCaptcha0 = str;
	}
	
	public static void setMaxLeghtSMS(int parseInt) {
//		pdPNum.dismiss();
		MAX_LENGTH_SMS = parseInt;
	}
	// ������� ��������� �� �������
	public static void setError(String strError) {
		//txtError.setText(strError);
		txtError.append(strError + "\n");
	}
	// ��������� �����
	public static void setCaptcha(String str){
		txtCaptcha1.setText(str);
	}
	
	private void setTheme(String _theme){
		if(_theme.compareToIgnoreCase("1") == 0){
//			buttonClean.setImageResource(R.drawable.clean_blue);
//			buttonSelectContact.setImageResource(R.drawable.contact_blue);
//			buttonSelectFavoritesContact.setImageResource(R.drawable.star_blue);
//			buttonSend.setImageResource(R.drawable.send_blue);
			
			buttonClean.setBackgroundResource(R.drawable.clean_blue);
			buttonSelectContact.setBackgroundResource(R.drawable.contact_blue);
			buttonSelectFavoritesContact.setBackgroundResource(R.drawable.star_blue);
			buttonSend.setBackgroundResource(R.drawable.send_blue);
			imgCaptcha.setBackgroundResource(R.drawable.load_blue);
			
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
//			buttonClean.setImageResource(R.drawable.clean_white);
//			buttonSelectContact.setImageResource(R.drawable.contact_white);
//			buttonSelectFavoritesContact.setImageResource(R.drawable.star_white);
//			buttonSend.setImageResource(R.drawable.send_blue);
//			
			buttonClean.setBackgroundResource(R.drawable.clean_white);
			buttonSelectContact.setBackgroundResource(R.drawable.contact_white);
			buttonSelectFavoritesContact.setBackgroundResource(R.drawable.star_white);
			buttonSend.setBackgroundResource(R.drawable.send_white);
			imgCaptcha.setBackgroundResource(R.drawable.load_white);
			
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
}
