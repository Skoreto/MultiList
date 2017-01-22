package cz.uhk.fim.skoreto.todolist.utils;

import cz.uhk.fim.skoreto.todolist.R;

/**
 * Nezarazene pomocne utilitky.
 * Created by Tomas.
 */
public class OtherUtils {

    /**
     * Pomocna metoda pro vraceni odpovidajiciho ID Weather font ikony.
     */
    public static int getAppropriateWeatherIcon(String icon) {
        int iconR;

        switch (icon) {
            // day - clear sky
            case "01d": iconR = R.string.weather_day_sunny;
                break;
            // day - few clouds
            case "02d": iconR = R.string.weather_day_cloudy;
                break;
            // day - scattered clouds
            case "03d": iconR = R.string.weather_cloud;
                break;
            // day - broken clouds
            case "04d": iconR = R.string.weather_cloudy;
                break;
            // day - shower rain
            case "09d": iconR = R.string.weather_day_showers;
                break;
            // day - rain
            case "10d": iconR = R.string.weather_day_rain;
                break;
            // day - thunderstorm
            case "11d": iconR = R.string.weather_day_thunderstorm;
                break;
            // day - snow
            case "13d": iconR = R.string.weather_day_snow;
                break;
            // day - mist
            case "50d": iconR = R.string.weather_day_fog;
                break;
            // night - clear sky
            case "01n": iconR = R.string.weather_day_fog;
                break;
            // night - few clouds
            case "02n": iconR = R.string.weather_night_cloudy;
                break;
            // night - scattered clouds
            case "03n": iconR = R.string.weather_cloud;
                break;
            // night - broken clouds
            case "04n": iconR = R.string.weather_cloudy;
                break;
            // night - shower rain
            case "09n": iconR = R.string.weather_night_showers;
                break;
            // night - rain
            case "10n": iconR = R.string.weather_night_rain;
                break;
            // night - thunderstorm
            case "11n": iconR = R.string.weather_night_thunderstorm;
                break;
            // night - snow
            case "13n": iconR = R.string.weather_night_snow;
                break;
            // night - mist
            case "50n": iconR = R.string.weather_night_fog;
                break;
            default:    iconR = R.string.weather_day_sunny_overcast;
                break;
        }

        return iconR;
    }

}
