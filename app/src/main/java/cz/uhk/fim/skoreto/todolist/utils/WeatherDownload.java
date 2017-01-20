package cz.uhk.fim.skoreto.todolist.utils;

import android.os.AsyncTask;

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
            // TODO Nepodarilo se nacist pocasi
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        try {
            JSONObject jsonObject = new JSONObject(result);
            // "main":{"temp":280.75,"pressure":1007.87,"humidity":100,"temp_min":280.75,"temp_max":280.75,"sea_level":1017.32,"grnd_level":1007.87
            JSONObject mainWeatherData = new JSONObject(jsonObject.getString("main"));

            Double tempKelvinDbl = Double.parseDouble(mainWeatherData.getString("temp"));

            double tempCelsius = tempKelvinDbl - 273.15;

            String weatherPlaceName = jsonObject.getString("name");

            TaskDetailActivity.tvTemperature.setText(String.format("%.1f", tempCelsius) + " °C");

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }
}
