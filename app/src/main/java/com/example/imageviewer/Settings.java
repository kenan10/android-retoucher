package com.example.imageviewer;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;


public class Settings extends AppCompatActivity {
    private Spinner languageSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        languageSpinner = findViewById(R.id.spinner);

        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String[] languageList = getResources().getStringArray(R.array.languages);
                String newLanguage = languageList[i];

                switch (newLanguage) {
                    case "English":
                        LocaleHelper.setLocale(getApplicationContext(), "en");
                        break;
                    case "Ukrainian":
                        LocaleHelper.setLocale(getApplicationContext(), "uk");
                        break;
                    case "Chinese":
                        LocaleHelper.setLocale(getApplicationContext(), "zh");
                        break;
                }

                Toast toast = Toast.makeText(getApplicationContext(),
                        "Ваш выбор: " + LocaleHelper.getCurrentLanguage(getApplicationContext()), Toast.LENGTH_SHORT);
                toast.show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                LocaleHelper.setLocale(getApplicationContext(), "uk");
            }
        });
    }
}
