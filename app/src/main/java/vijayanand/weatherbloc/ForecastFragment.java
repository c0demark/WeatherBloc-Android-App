package vijayanand.weatherbloc;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    private ListView listView;
    ArrayAdapter<String> mForecastAdapter;
    ArrayList<String> weekForecast;

    public ForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        String[] data = {

                "Mon 6/23 - Sunny - 31/17",

                "Tue 6/24 - Foggy - 21/8",

                "Wed 6/25 - Cloudy - 22/17",

                "Thurs 6/26 - Rainy - 18/11",

                "Fri 6/27 - Foggy - 21/10",

                "Sat 6/28 - TRAPPED IN WEATHERSTATION - 23/18",

                "Sun 6/29 - Sunny - 20/7"

        };

        List<String> weekForecast = new ArrayList<String>(Arrays.asList(data));
        mForecastAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                weekForecast);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ListView listview = (ListView) rootView.findViewById(R.id.list_view_forecast);
        listview.setAdapter(mForecastAdapter);

        return rootView;
    }
    public class FetchWeatherTask{
        HttpURLConnection urlconnection = null;
        BufferedReader reader = null;
        String forecastJsonStr = null;

        try{
            URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=600073mode=json&units=metric&cnt=7");
            urlconnection = (HttpURLConnection) url.openConnection();
            urlconnection.setRequestMethod("GET");
            urlconnection.connect();

            InputStream inputStream = urlconnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if(inputStream == null){
                forecastJsonStr = null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while((line = reader.readLine()) != null){
                buffer.append(line + '\n');
            }
            if(buffer.length() == 0){
                forecastJsonStr = null;
            }
            forecastJsonStr = buffer.toString();
        }
        catch(IOException e){
            Log.e("ForecastFragment", "Error", e);
            forecastJsonStr = null;
        }
        finally {
            if (urlconnection != null){
                urlconnection.disconnect();
            }
            if(reader != null){
                try{
                    reader.close();
                }
                catch(final IOException e){
                    Log.e("ForecastFragment", "Error closing stream",e);
                }
            }
        }

    }
}
