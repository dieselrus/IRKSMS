package ru.diesel_ru.irksms;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

public class Abaut extends Activity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.abaut); 
        TextView tv = (TextView) findViewById(R.id.abaut);
        
        tv.setText(Html.fromHtml("<html><body><h1>&nbsp;</h1><p style=\"text-align: center;\"><strong>� ���������</strong></p><p>��������� ������������� ��� ���������� �������� ��� ����� ������ ����� <a href=\"http://www.irk.ru/\">www.irk.ru</a>. ��������� �� ���������� �� ��������� ����� �������.</p><p>����� �� ����� ��������������� �� �������� ��� ����������.��� ��������������� �� ���������� ���������, ����� �������� � ��� �����, ����� ������������ ���������.</p><p>&nbsp;</p><p><strong>�����:</strong>����� �����.</p><p><strong>E-mail:</strong><a href=\"mailto:denis.gamza@gmail.com\">denis.gamza@gmail.com</a></p><p><strong>������:</strong>������ �����.</spanclass=spelle></p><p><strong>E-mail:</strong></p></body></html>"));
	
        tv.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		finish();
        	}
        });
	}
}
