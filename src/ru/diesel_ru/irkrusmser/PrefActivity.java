package ru.diesel_ru.irkrusmser;

import ru.diesel_ru.irkrusmser.R;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class PrefActivity extends PreferenceActivity {

  @SuppressWarnings("deprecation")
@Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.pref);
  }
}