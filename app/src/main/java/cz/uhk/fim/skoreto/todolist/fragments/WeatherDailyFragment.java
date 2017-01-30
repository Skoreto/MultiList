package cz.uhk.fim.skoreto.todolist.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cz.uhk.fim.skoreto.todolist.R;
import cz.uhk.fim.skoreto.todolist.model.Task;
import cz.uhk.fim.skoreto.todolist.model.Weather;
import cz.uhk.fim.skoreto.todolist.utils.OtherUtils;

import static cz.uhk.fim.skoreto.todolist.TaskDetailActivity.weatherFont;

/**
 * Fragment az 16-ti denni prumerne predpovedi pocasi. Umisten v DetailFragmentPageru detailu ukolu.
 */
public class WeatherDailyFragment extends Fragment {
    private TextView tvForecastDate;
    private TextView tvName;
    private TextView tvMainIcon;
    private TextView tvTemp;
    private TextView tvPressureIcon;
    private TextView tvPressure;
    private TextView tvHumidityIcon;
    private TextView tvHumidity;
    private TextView tvWindIcon;
    private TextView tvWind;

    public static WeatherDailyFragment newInstance(List<Weather> listWeatherDailyFrag, Task task,
                                            Weather weatherDailyFrag) {
        WeatherDailyFragment f = new WeatherDailyFragment();

        // Ziskani datumu splneni v 0:00:00:00
        Calendar calDueDateCleared = Calendar.getInstance();
        calDueDateCleared.setTime(task.getDueDate());
        calDueDateCleared.set(Calendar.HOUR_OF_DAY, 0);
        calDueDateCleared.set(Calendar.MINUTE, 0);
        calDueDateCleared.set(Calendar.SECOND, 0);
        calDueDateCleared.set(Calendar.MILLISECOND, 0);
        Date dueDateCleared = calDueDateCleared.getTime();

        Calendar calDayWeatherDateCleared = Calendar.getInstance();
        for (Weather dayWeather : listWeatherDailyFrag) {
            // Ziskani datumu predpovedi pocasi v 0:00:00:00
            calDayWeatherDateCleared.setTime(dayWeather.getDate());
            calDayWeatherDateCleared.set(Calendar.HOUR_OF_DAY, 0);
            calDayWeatherDateCleared.set(Calendar.MINUTE, 0);
            calDayWeatherDateCleared.set(Calendar.SECOND, 0);
            calDayWeatherDateCleared.set(Calendar.MILLISECOND, 0);
            Date dayWeatherDateCleared = calDayWeatherDateCleared.getTime();

            // Nalezeni odpovidajiciho pocasi k danemu datumu splneni
            if (dayWeatherDateCleared.compareTo(dueDateCleared) == 0) {
                weatherDailyFrag = dayWeather;
                break;
            }
        }

        Bundle args = new Bundle();
        args.putString("name", weatherDailyFrag.getName());
        args.putString("icon", weatherDailyFrag.getIcon());
        args.putDouble("temp", weatherDailyFrag.getTemp());
        args.putString("main", weatherDailyFrag.getMain());
        args.putDouble("pressure", weatherDailyFrag.getPressure());
        args.putDouble("humidity", weatherDailyFrag.getHumidity());
        args.putDouble("windSpeed", weatherDailyFrag.getWindSpeed());

        // Parsovani datumu predpovedi
        SimpleDateFormat sdf = new SimpleDateFormat("d.M.yyyy H:mm");
        String sForecastDate = sdf.format(weatherDailyFrag.getDate());
        args.putString("forecastDate", sForecastDate);

        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pager_weather_daily, container, false);
        Bundle args = getArguments();

        tvForecastDate = (TextView) view.findViewById(R.id.tvForecastDate);
        tvForecastDate.setText(args.getString("forecastDate"));

        tvName = (TextView) view.findViewById(R.id.tvName);
        tvName.setText(args.getString("name"));

        String icon = args.getString("icon");
        tvMainIcon = (TextView) view.findViewById(R.id.tvMainIcon);
        tvMainIcon.setTypeface(weatherFont);
        tvMainIcon.setText(OtherUtils.getAppropriateWeatherIcon(icon));

        tvTemp = (TextView) view.findViewById(R.id.tvTemp);
        tvTemp.setText(
                String.format("%.1f", args.getDouble("temp")) + " °C");

        tvPressureIcon = (TextView) view.findViewById(R.id.tvPressureIcon);
        tvPressureIcon.setTypeface(weatherFont);
        tvPressureIcon.setText(R.string.weather_barometer);

        tvPressure = (TextView) view.findViewById(R.id.tvPressure);
        tvPressure.setText(
                String.format("%.0f", args.getDouble("pressure")) + " hPa");

        tvHumidityIcon = (TextView) view.findViewById(R.id.tvHumidityIcon);
        tvHumidityIcon.setTypeface(weatherFont);
        tvHumidityIcon.setText(R.string.weather_humidity);

        tvHumidity = (TextView) view.findViewById(R.id.tvHumidity);
        tvHumidity.setText(
                String.format("%.0f", args.getDouble("humidity")) + " %");

        tvWindIcon = (TextView) view.findViewById(R.id.tvWindIcon);
        tvWindIcon.setTypeface(weatherFont);
        tvWindIcon.setText(R.string.weather_windy);

        tvWind = (TextView) view.findViewById(R.id.tvWind);
        tvWind.setText(
                String.format("%.1f", args.getDouble("windSpeed")) + " m/s");

        return view;
    }
}
