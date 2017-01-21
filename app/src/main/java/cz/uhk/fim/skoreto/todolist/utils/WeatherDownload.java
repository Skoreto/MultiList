package cz.uhk.fim.skoreto.todolist.utils;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import cz.uhk.fim.skoreto.todolist.TaskDetailActivity;

/**
 * Trida pro vraceni soucasne predpovedi pocasi.
 * Created by Tomas.
 */
public class WeatherDownload extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground(String... urls) {

        String result = "";
        URL url;
        HttpURLConnection urlConnection;

        try {
            url = new URL(urls[0]);
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = urlConnection.getInputStream();
            InputStreamReader reader = new InputStreamReader(inputStream);

            int data = reader.read();
            while (data != -1) {
                char current = (char) data;
                result += current;
                data = reader.read();
            }

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Connection k pocasi",
                    "Nepodarilo se ziskat connection k pocasi na OpenWeatherMapAPI.");
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        try {
            // Cely objekt dat o pocasi
            JSONObject overalWeatherObject = new JSONObject(result);
            String name = overalWeatherObject.getString("name");

            // "weather":[{"id":701,"main":"Mist","description":"mist","icon":"50d"}]
            JSONArray weatherArray = new JSONArray(overalWeatherObject.getString("weather"));
            JSONObject weatherObject = weatherArray.getJSONObject(0);
            String main = weatherObject.getString("main");
            String description = weatherObject.getString("description");
            String icon = weatherObject.getString("icon");

            // "main":{"temp","pressure","humidity","temp_min","temp_max","sea_level","grnd_level"
            JSONObject mainObject = new JSONObject(overalWeatherObject.getString("main"));
            Double pressure = Double.parseDouble(mainObject.getString("pressure"));
            Double humidity = Double.parseDouble(mainObject.getString("humidity"));

            // Prepocet teplony z Kelvinu na Celsius
            Double tempKelvin = Double.parseDouble(mainObject.getString("temp"));
            double tempCelsius = tempKelvin - 273.15;

            // "wind":{"speed":1}
            JSONObject windObject = new JSONObject(overalWeatherObject.getString("wind"));
            Double windSpeed = Double.parseDouble(windObject.getString("speed"));

            // Predani udaju do inicializovaneho statickeho objektu pocasi v TaskDetailActivity
            TaskDetailActivity.weather.setMain(main);
            TaskDetailActivity.weather.setDescription(description);
            TaskDetailActivity.weather.setIcon(icon);
            TaskDetailActivity.weather.setTemp(tempCelsius);
            TaskDetailActivity.weather.setPressure(pressure);
            TaskDetailActivity.weather.setHumidity(humidity);
            TaskDetailActivity.weather.setWindSpeed(windSpeed);
            TaskDetailActivity.weather.setName(name);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("Tvorba JSON pocasi",
                    "Nepodarilo se naparsovat udaje o pocasi z JSON OpenWeatherMapAPI.");
        }
    }

}
