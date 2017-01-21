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
import java.util.Date;

import cz.uhk.fim.skoreto.todolist.TaskDetailActivity;
import cz.uhk.fim.skoreto.todolist.model.Weather;

/**
 * Trida pro vraceni az 16-ti denni prumerne predpovedi pocasi.
 * Priklad zdroje dat pro Podebrady na 7 dni:
 * http://api.openweathermap.org/data/2.5/forecast/daily?lat=
 * 50.145197499999966&lon=15.137113281249997&cnt=7&mode=json&appid=792b095348cf903a77b8ee3f2bc8251e
 * Created by Tomas.
 */
public class WeatherDailyForecast extends AsyncTask<String, Void, String> {

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
            // Cely objekt dat o pocasi (X dni)
            JSONObject overalWeatherObject = new JSONObject(result);

            // "city":{"id","name","coord":{"lon","lat"},"country",...}
            JSONObject cityObject = new JSONObject(overalWeatherObject.getString("city"));
            String name = cityObject.getString("name");

            // Seznam objektu s predpovedi pro kazdy den do poctu X dle zadaneho "cnt=X"
            // "list":[{}, {}, ... ]}
            JSONArray listArray = new JSONArray(overalWeatherObject.getString("list"));

            // Napln udaji o pocasi kazdy den v predripravenem seznamu v TaskDetailActivity
            for (int i = 0; i < TaskDetailActivity.weatherDailyCount; i++) {
                // Jednotlivy objekt pocasi s predpovedi pro dany den
                // "dt":1484996400,"temp":{},"pressure","humidity","weather":[{}],"speed", ...
                JSONObject dayObject = listArray.getJSONObject(i);

                // Parsovane datum casu predpovedi z JSON formatu
                String sDate = dayObject.getString("dt");
                Date date = new Date(Long.valueOf(sDate) * 1000);

                Double pressure = Double.parseDouble(dayObject.getString("pressure"));
                Double humidity = Double.parseDouble(dayObject.getString("humidity"));
                Double windSpeed = Double.parseDouble(dayObject.getString("speed"));

                // "temp":{"day","min","max","night","eve","morn"}
                JSONObject tempObject = new JSONObject(dayObject.getString("temp"));
                // Prepocet teplony z Kelvinu na Celsius
                Double tempDayKelvin = Double.parseDouble(tempObject.getString("day"));
                double tempDayCelsius = tempDayKelvin - 273.15;

                // "weather":[{"id":800,"main":"Clear","description":"clear sky","icon":"01d"}]
                JSONArray weatherArray = new JSONArray(dayObject.getString("weather"));
                JSONObject weatherObject = weatherArray.getJSONObject(0);
                String main = weatherObject.getString("main");
                String description = weatherObject.getString("description");
                String icon = weatherObject.getString("icon");

                // Nastaveni udaju pro kazdy den pocasi v seznamu
                Weather dayWeather = TaskDetailActivity.listWeatherDaily.get(i);
                dayWeather.setName(name);
                dayWeather.setDate(date);
                dayWeather.setPressure(pressure);
                dayWeather.setHumidity(humidity);
                dayWeather.setWindSpeed(windSpeed);
                dayWeather.setTemp(tempDayCelsius);
                dayWeather.setMain(main);
                dayWeather.setDescription(description);
                dayWeather.setIcon(icon);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("Tvorba daily pocasi",
                    "Nepodarilo se naparsovat udaje o daily pocasi z JSON OpenWeatherMapAPI.");
        }
    }

}