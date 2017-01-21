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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cz.uhk.fim.skoreto.todolist.TaskDetailActivity;

/**
 * Trida pro vraceni az 5-ti denni predpovedi pocasi pro kazde 3 hodiny.
 * Priklad zdroje dat pro Podebrady:
 * http://api.openweathermap.org/data/2.5/forecast?lat=
 * 50.145197499999966&lon=15.137113281249997&appid=792b095348cf903a77b8ee3f2bc8251e
 * Created by Tomas.
 */
public class WeatherHourForecast extends AsyncTask<String, Void, String> {

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
            // Cely objekt dat o pocasi (5 dni po 3 hodinach)
            JSONObject overalWeatherObject = new JSONObject(result);

            // "city":{"id","name","coord":{"lon","lat"},"country",...}
            JSONObject cityObject = new JSONObject(overalWeatherObject.getString("city"));
            String name = cityObject.getString("name");

            // Seznam objektu s predpovedi pocasi pro kazde 3 hodiny po dobu 5-ti dni = 40 objektu
            // "list":[{"dt":1406106000,"main":{},"weather":[{}],"clouds":{},"wind":{},"sys":{},
            // "dt_txt":"2014-07-23 09:00:00"}]}
            JSONArray listArray = new JSONArray(overalWeatherObject.getString("list"));

            // Jednotlivy objekt pocasi s predpovedi pro dane 3 hodiny
            JSONObject singleObject = listArray.getJSONObject(0);

            // Parsovane datum casu predpovedi z JSON formatu "yyyy-MM-dd HH:mm:ss"
            String sDate = singleObject.getString("dt_txt");
            Date date = null;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                date = sdf.parse(sDate);
            } catch (ParseException e) {
                Log.e("Parsovani datumu",
                        "Nepodarilo se naparsovat datum u predpovedi pocasi.");
            }

            // "main":{"temp","temp_min","temp_max","pressure","sea_level","humidity",...}
            JSONObject mainObject = new JSONObject(singleObject.getString("main"));
            Double pressure = Double.parseDouble(mainObject.getString("pressure"));
            Double humidity = Double.parseDouble(mainObject.getString("humidity"));

            // Prepocet teplony z Kelvinu na Celsius
            Double tempKelvin = Double.parseDouble(mainObject.getString("temp"));
            double tempCelsius = tempKelvin - 273.15;

            // "weather":[{"id":800,"main":"Clear","description":"clear sky","icon":"02d"}]
            JSONArray weatherArray = new JSONArray(singleObject.getString("weather"));
            JSONObject weatherObject = weatherArray.getJSONObject(0);
            String main = weatherObject.getString("main");
            String description = weatherObject.getString("description");
            String icon = weatherObject.getString("icon");

            // "wind":{"speed":1.31,"deg":195.501}
            JSONObject windObject = new JSONObject(singleObject.getString("wind"));
            Double windSpeed = Double.parseDouble(windObject.getString("speed"));

            // Predani udaju do inicializovaneho statickeho objektu pocasi v TaskDetailActivity
            TaskDetailActivity.weatherHour.setMain(main);
            TaskDetailActivity.weatherHour.setDescription(description);
            TaskDetailActivity.weatherHour.setIcon(icon);
            TaskDetailActivity.weatherHour.setTemp(tempCelsius);
            TaskDetailActivity.weatherHour.setPressure(pressure);
            TaskDetailActivity.weatherHour.setHumidity(humidity);
            TaskDetailActivity.weatherHour.setWindSpeed(windSpeed);
            TaskDetailActivity.weatherHour.setName(name);
            TaskDetailActivity.weatherHour.setDate(date);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("Tvorba hour pocasi",
                    "Nepodarilo se naparsovat udaje o hour pocasi z JSON OpenWeatherMapAPI.");
        }
    }

}