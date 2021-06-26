package com.example.socialbike;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.annotation.NonNull;

public class DateAndTimeDialog extends Dialog {

    private final int layout;
    private final boolean isDateLayout;
    private final AddNewEventActivity callerClass;
    DatePicker date_picker;
    TimePicker time_picker;

    Button cancelButton, applyButton;

    public DateAndTimeDialog(@NonNull AddNewEventActivity context, int layout, boolean isDateLayout) {
        super(context);
        this.layout = layout;
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
        if (isDateLayout)
            callerClass.setDate(date_picker.getDayOfMonth() + "/" + (date_picker.getMonth() + 1) + "/" + date_picker.getYear() + ", ");
        else
            callerClass.setTime(time_picker.getHour() + ":" + time_picker.getMinute());
        dismiss();
    }


}
