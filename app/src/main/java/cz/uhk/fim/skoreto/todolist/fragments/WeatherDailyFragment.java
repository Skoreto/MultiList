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

/**
 * Fragment az 16-ti denni prumerne predpovedi pocasi. Umisten v DetailFragmentPageru detailu ukolu.
 */
public class WeatherDailyFragment extends Fragment {
    private TextView tvForecastDate;
    private TextView tvName;
    private ImageView ivMainIcon;
    private TextView tvTemp;
    private TextView tvMain;
    private TextView tvPressure;
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

        ivMainIcon = (ImageView) view.findViewById(R.id.ivMainIcon);
        String icon = args.getString("icon");
        String iconImage = String.format("http://openweathermap.org/img/w/%s.png", icon);
        Picasso.with(getContext()).load(iconImage).into(ivMainIcon);

        tvTemp = (TextView) view.findViewById(R.id.tvTemp);
        tvTemp.setText(
                String.format("%.1f", args.getDouble("temp")) + " °C");
        tvMain = (TextView) view.findViewById(R.id.tvMain);
        tvMain.setText(args.getString("main"));
        tvPressure = (TextView) view.findViewById(R.id.tvPressure);
        tvPressure.setText(
                String.format("%.0f", args.getDouble("pressure")) + " hPa");
        tvWind = (TextView) view.findViewById(R.id.tvWind);

        return view;
    }
}
