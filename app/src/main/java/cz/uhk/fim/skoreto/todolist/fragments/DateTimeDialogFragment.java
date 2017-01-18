package cz.uhk.fim.skoreto.todolist.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import cz.uhk.fim.skoreto.todolist.utils.DateTimeDialog;

/**
 * Fragment pro zobrazeni vlastniho dialogu pro vyber datumu a casu.
 */
public class DateTimeDialogFragment extends DialogFragment
        implements TimePicker.OnTimeChangedListener, DatePicker.OnDateChangedListener, DatePickerDialog.OnDateSetListener {

    Date newDueDate;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Pouzij aktualni cas jako vychozi datum v pickerech
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int monthOfYear = c.get(Calendar.MONTH);
        int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        DateTimeDialog myDialog = new DateTimeDialog(getActivity());
        myDialog.setTimeListener(this);
        myDialog.setDateListener(year, monthOfYear, dayOfMonth, this);
        return myDialog;
    }

    @Override
    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();

        // Sestaveni noveho datumu.
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthOfYear);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        newDueDate = calendar.getTime();

        Toast.makeText(getActivity(),
                "Rok" + year,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {

    }

}
