package vijayanand.weatherbloc;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    private void bindPreferenceSummaryToValue(Preference preference){
        onPreferenceChange(preference, PreferenceManager
                .getDefaultSharedPreferences(preference.getContext())
                .getString(preference.getKey(),""));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String stringValue=newValue.toString();
        if (preference instanceof ListPreference){
            ListPreference listPreference=(ListPreference) preference;
            int PrefIndex=listPreference.findIndexOfValue(stringValue);
            if (PrefIndex>=0){
                preference.setSummary(stringValue);
            }
        }

        return true;
    }
}
