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

import cz.uhk.fim.skoreto.todolist.R;
import cz.uhk.fim.skoreto.todolist.model.Weather;

/**
 * Fragment 5-ti denni / 3-hodinove predpovedi pocasi. Umisten v DetailFragmentPageru detailu ukolu.
 */
public class WeatherHourFragment extends Fragment {
    private TextView tvForecastDate;
    private ImageView ivMainIcon;
    private TextView tvTemp;
    private TextView tvMain;
    private TextView tvPressure;
    private TextView tvWind;

    public static WeatherHourFragment newInstance(Weather weatherHour) {
        WeatherHourFragment f = new WeatherHourFragment();
        Bundle args = new Bundle();
        args.putString("icon", weatherHour.getIcon());
        args.putDouble("temp", weatherHour.getTemp());
        args.putString("main", weatherHour.getMain());
        args.putDouble("pressure", weatherHour.getPressure());

        // Parsovani datumu predpovedi
        SimpleDateFormat sdf = new SimpleDateFormat("d.M.yyyy H:mm");
        String sForecastDate = sdf.format(weatherHour.getDate());
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
        View view = inflater.inflate(R.layout.fragment_pager_weather_hour, container, false);
        Bundle args = getArguments();

        tvForecastDate = (TextView) view.findViewById(R.id.tvForecastDate);
        tvForecastDate.setText(args.getString("forecastDate"));

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
