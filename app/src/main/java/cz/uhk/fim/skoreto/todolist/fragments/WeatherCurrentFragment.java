package cz.uhk.fim.skoreto.todolist.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;

import cz.uhk.fim.skoreto.todolist.R;
import cz.uhk.fim.skoreto.todolist.model.Weather;
import cz.uhk.fim.skoreto.todolist.utils.OtherUtils;

import static cz.uhk.fim.skoreto.todolist.TaskDetailActivity.weatherFont;

/**
 * Fragment aktualniho pocasi. Umisten v DetailFragmentPageru detailu ukolu.
 */
public class WeatherCurrentFragment extends Fragment {
    private TextView tvCurrentDate;
    private TextView tvName;
    private TextView tvMainIcon;
    private TextView tvTemp;
    private TextView tvPressureIcon;
    private TextView tvPressure;
    private TextView tvHumidityIcon;
    private TextView tvHumidity;
    private TextView tvWindIcon;
    private TextView tvWind;

    public static WeatherCurrentFragment newInstance(Weather weatherCurrent) {
        WeatherCurrentFragment f = new WeatherCurrentFragment();
        Bundle args = new Bundle();
        args.putString("name", weatherCurrent.getName());
        args.putString("icon", weatherCurrent.getIcon());
        args.putDouble("temp", weatherCurrent.getTemp());
        args.putString("main", weatherCurrent.getMain());
        args.putDouble("pressure", weatherCurrent.getPressure());
        args.putDouble("humidity", weatherCurrent.getHumidity());
        args.putDouble("windSpeed", weatherCurrent.getWindSpeed());

        // Parsovani datumu predpovedi
        SimpleDateFormat sdf = new SimpleDateFormat("d.M.yyyy H:mm");
        String sCurrentDate = sdf.format(weatherCurrent.getDate());
        args.putString("currentDate", sCurrentDate);

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
        View view = inflater.inflate(R.layout.fragment_pager_weather_current, container, false);
        Bundle args = getArguments();

        tvCurrentDate = (TextView) view.findViewById(R.id.tvCurrentDate);
        tvCurrentDate.setText(args.getString("currentDate"));

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
