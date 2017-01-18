package cz.uhk.fim.skoreto.todolist.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

import cz.uhk.fim.skoreto.todolist.R;

/**
 * Trida implementuje vlastni dialog pro vyber datumu a casu.
 */
public class DateTimeDialog extends Dialog implements android.view.View.OnClickListener {

    private TimePicker mTime;
    private DatePicker mDate;
    private String returnedDateTime;

    public DateTimeDialog(Context context) {
        super(context);
        setContentView(R.layout.dialog_date_time);

        mDate = (DatePicker)findViewById(R.id.datePicker);
        mTime = (TimePicker)findViewById(R.id.timePicker);
        // Nastav 24-hodinovy system (nezobrazuj AM/PM)
        mTime.setIs24HourView(true);

        Button done = (Button)findViewById(R.id.done);
        done.setOnClickListener(this);

        Button cancel = (Button)findViewById(R.id.cancel);
        cancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.done:
                dismiss();
            case R.id.cancel:
                dismiss();
        }
    }

    public void setTimeListener(TimePicker.OnTimeChangedListener time){
        mTime.setOnTimeChangedListener(time);
    }

    public void setDateListener(int year, int monthOfYear, int dayOfMonth, DatePicker.OnDateChangedListener date){
        mDate.init(year, monthOfYear, dayOfMonth, date);
    }
}
