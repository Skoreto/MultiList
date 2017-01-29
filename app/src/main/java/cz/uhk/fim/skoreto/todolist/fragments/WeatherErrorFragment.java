package cz.uhk.fim.skoreto.todolist.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cz.uhk.fim.skoreto.todolist.R;

/**
 * Fragment zobrazujici chybove hlaseni, pokud server nestihl vratit potrebna data pro pocasi.
 */
public class WeatherErrorFragment extends Fragment {
    private TextView tvErrorDescription;

    public static WeatherErrorFragment newInstance() {
        WeatherErrorFragment f = new WeatherErrorFragment();
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pager_weather_error, container, false);
        tvErrorDescription = (TextView) view.findViewById(R.id.tvErrorDescription);
        tvErrorDescription.setText("Počasí není dostupné");
        return view;
    }
}