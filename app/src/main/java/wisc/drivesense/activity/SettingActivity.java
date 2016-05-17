package wisc.drivesense.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import wisc.drivesense.R;

public class SettingActivity extends AppCompatActivity {


    private String TAG = "SettingActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

    }

    public static boolean isAutoMode(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean cbValue = sharedPref.getBoolean("pref_auto", false);
        return cbValue;
    }


    public static class SettingsFragment extends PreferenceFragment {
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);

            EditTextPreference idPref = (EditTextPreference)findPreference("pre_id");
            String androidid = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);
            int len = androidid.length();
            androidid = androidid.substring(len - 6);
            idPref.setTitle("Device ID: " + androidid);
        }
    }
}