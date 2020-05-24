package com.example.workoutapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import androidx.appcompat.app.AppCompatDialogFragment;

public class DurationDialog extends AppCompatDialogFragment {

    private NumberPicker numberPickerMinutes;
    private NumberPicker numberPickerSeconds;
    private DurationDialogListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_activityduration, null);

        numberPickerMinutes = view.findViewById(R.id.minutes_picker);
        numberPickerSeconds = view.findViewById(R.id.seconds_picker);

        numberPickerMinutes.setMaxValue(59);
        numberPickerMinutes.setMinValue(0);
        numberPickerSeconds.setMaxValue(59);
        numberPickerSeconds.setMinValue(0);

        builder.setView(view)
                .setTitle("Set Duration")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                          int durationInput = numberPickerMinutes.getValue()*60
                                  + numberPickerSeconds.getValue();

                          listener.applyDurationInput(durationInput);
                  }
                });

        return builder.create();
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (DurationDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "must implement DurationDialogListener");
        }
    }

    public interface DurationDialogListener {
        void applyDurationInput(int durationInput);
    }
}
