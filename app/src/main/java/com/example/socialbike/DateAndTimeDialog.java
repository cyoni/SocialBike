package com.example.socialbike;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DateAndTimeDialog extends Dialog {

    private final int layout;
    private final boolean isDateLayout;
    private final AddNewEventActivity callerClass;
    private final TextView view;
    private final TextView view2;
    private DatePicker date_picker;
    private TimePicker time_picker;

    Button cancelButton, applyButton;

    public DateAndTimeDialog(@NonNull AddNewEventActivity context, int layout, boolean isDateLayout, TextView view, TextView view2) {
        super(context);
        this.layout = layout;
        this.view = view;
        this.view2 = view2;
        this.isDateLayout = isDateLayout;
        callerClass = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(layout);

        date_picker = findViewById(R.id.date_picker);
        time_picker = findViewById(R.id.time_picker);

        if (isDateLayout) {
            date_picker.setVisibility(View.VISIBLE);
            time_picker.setVisibility(View.GONE);
        }
        else {
            date_picker.setVisibility(View.GONE);
            time_picker.setVisibility(View.VISIBLE);
        }


        cancelButton = findViewById(R.id.cancelButton);
        applyButton = findViewById(R.id.applyButton);

        applyButton.setOnClickListener(view -> apply());
        cancelButton.setOnClickListener(view -> dismiss());
    }

    private void apply() {
        if (isDateLayout) {
            String date = String.format(Locale.US,"%d/%d/%d", date_picker.getDayOfMonth(), (date_picker.getMonth() + 1), date_picker.getYear());
            view.setText(Date.convertDateToDay(date));
        }
        else {
            String time = String.format("%d.%d", time_picker.getHour(), time_picker.getMinute());
            view.setText(Date.convertTime(time));
        }

        if (view.getId() == R.id.time || view.getId() == R.id.date){
            view2.setText(view.getText().toString());
        }
        dismiss();
    }


}
