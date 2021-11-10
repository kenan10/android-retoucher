package com.example.imageviewer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

public class SettingsDialog extends AppCompatDialogFragment {
    private EditText editTextOverBrighterThreshold;
    private EditText editTextAdaptiveGaussThreshold;
    private SettingsDialogListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_adaptive_gauss_dialog, null);

        builder.setView(view);
        builder.setTitle("Adaptive gauss settings");
        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String adaptiveGaussThreshold = editTextAdaptiveGaussThreshold.getText().toString();
                String overBrighterThreshold = editTextOverBrighterThreshold.getText().toString();
                listener.adaptiveGauss(adaptiveGaussThreshold, overBrighterThreshold);
            }
        });

        editTextAdaptiveGaussThreshold = view.findViewById(R.id.filter);
        editTextOverBrighterThreshold = view.findViewById(R.id.overBrighter);

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (SettingsDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement SettingsDialog");
        }
    }

    public interface SettingsDialogListener {
        void adaptiveGauss(String adaptiveGaussThreshold, String overBrighterThreshold);
    }
}
