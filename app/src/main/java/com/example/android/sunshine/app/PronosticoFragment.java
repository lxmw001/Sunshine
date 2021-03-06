package com.example.android.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A placeholder fragment containing a simple view.
 */
public class PronosticoFragment extends Fragment {

    private ArrayAdapter<String> pronosticoAdapter;

    public PronosticoFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootview= inflater.inflate(R.layout.fragment_main, container, false);

        String[] suns= {};
        ArrayList<String> sunweek= new ArrayList<String>(Arrays.asList(suns));
        pronosticoAdapter= new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_text_view, sunweek);

        ListView lista= (ListView) rootview.findViewById(R.id.list_item_forecast);
        lista.setAdapter(pronosticoAdapter);
        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String clima = pronosticoAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), DetallePronostico.class).putExtra(Intent.EXTRA_TEXT, clima);
                startActivity(intent);
            }
        });

        return rootview;
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_pronostico, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            actualizarCLima();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void actualizarCLima(){
        FetchWeatherTask task= new FetchWeatherTask();
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location= prefs.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
        task.execute(location);
    }

    @Override
    public void onStart() {
        super.onStart();
        actualizarCLima();
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]>{
        @Override
        protected String[] doInBackground(String... params) {

            if(params.length==0){
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            //parametros para la busqueda de pronostico
            String format= "json";
            String units= "metric";
            int numberDays= 7;

            try {
                final String pronostico_base_url= "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM= "q";
                final String FORMAT_PARAM= "mode";
                final String UNITS_PARAM= "units";
                final String DAYS_PARAM= "cnt";
                final String APPID_PARAM= "appid";

                Uri biuldUri= Uri.parse(pronostico_base_url).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, params[0])
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, units)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numberDays))
                        //.appendQueryParameter(APPID_PARAM, appID)
                        .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_WEATHER_MAP_API_KEY)
                        .build();

                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                //URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=Ecuador,Loja&mode=json&units=metric&cnt=7&appid=76bba07b7b54a898540c0b903d55169a");
                URL url = new URL(biuldUri.toString());
                Log.v("Peticion internet", biuldUri.toString());
                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
                Log.v("Respuesta internet", forecastJsonStr);
            } catch (IOException e) {
                Log.e("PronostricoFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PronostricoFragment", "Error closing stream", e);
                    }
                }
            }
            try{
                return getWeatherDataFromJson(forecastJsonStr, numberDays);
            }catch (JSONException e){
                Log.e("PronosticoFragment", e.getMessage(), e);
                e.printStackTrace();

            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            //super.onPostExecute(result);
            if(result!=null){
                pronosticoAdapter.clear();
                for (String dias:result){
                    pronosticoAdapter.add(dias);
                }
            }
        }
    }

    /* The date/time conversion code is going to be moved outside the asynctask later,
        * so for convenience we're breaking it out into its own method now.
        */
    private String getReadableDateString(long time){
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        //Date date= new Date(time*1000);
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("E, MMM d");
        return shortenedDateFormat.format(time);
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        // For presentation, assume the user doesn't care about tenths of a degree.

        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(getActivity());
        String unitType= sharedPreferences.getString(getString(R.string.pref_units_key), getString(R.string.pref_units_metric));
        String symbolUnit=" °C";
        if(unitType.equals("imperial")){
            high= (high*1.8)+32;
            low= (low*1.8)+32;
            symbolUnit= " °F";
        }else if(!unitType.equals("metric")){
            Log.d("Pronostico Fragment", "Unidad no encontrada");
        }

        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = "Día:"+roundedHigh+symbolUnit + " - Noche:" + roundedLow+symbolUnit;
        return highLowStr;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DESCRIPTION = "main";

        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

        // OWM returns daily forecasts based upon the local time of the city that is being
        // asked for, which means that we need to know the GMT offset to translate this data
        // properly.

        // Since this data is also sent in-order and the first day is always the
        // current day, we're going to take advantage of that to get a nice
        // normalized UTC date for all of our weather.

        Time dayTime = new Time();
        dayTime.setToNow();

        // we start at the day returned by local time. Otherwise this is a mess.
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

        // now we work exclusively in UTC
        dayTime = new Time();

        String[] resultStrs = new String[numDays];
        for(int i = 0; i < weatherArray.length(); i++) {
            // For now, using the format "Day, description, hi/low"
            String day;
            String description;
            String highAndLow;

            // Get the JSON object representing the day
            JSONObject dayForecast = weatherArray.getJSONObject(i);

            // The date/time is returned as a long.  We need to convert that
            // into something human-readable, since most people won't read "1400356800" as
            // "this saturday".
            long dateTime;
            // Cheating to convert this to UTC time, which is what we want anyhow
            dateTime = dayTime.setJulianDay(julianStartDay+i);
            day = getReadableDateString(dateTime);

            // description is in a child array called "weather", which is 1 element long.
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);

            // Temperatures are in a child object called "temp".  Try not to name variables
            // "temp" when working with temperature.  It confuses everybody.
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            double high = temperatureObject.getDouble(OWM_MAX);
            double low = temperatureObject.getDouble(OWM_MIN);

            highAndLow = formatHighLows(high, low);
            resultStrs[i] = day + " - " + description + " - " + highAndLow;
        }

        for (String s : resultStrs) {
            Log.v("PronosticoFragment", "Forecast entry: " + s);
        }
        return resultStrs;

    }

}
