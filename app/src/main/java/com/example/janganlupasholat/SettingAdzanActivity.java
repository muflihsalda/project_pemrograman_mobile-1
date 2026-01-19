package com.example.janganlupasholat;

import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Switch;

public class SettingAdzanActivity extends AppCompatActivity {

    Switch swSubuh, swDzuhur, swAshar, swMaghrib, swIsya;

    SharedPreferences sp;
    public static final String SP_NAME = "SETTINGS_ADZAN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_adzan);

        sp = getSharedPreferences(SP_NAME, MODE_PRIVATE);

        initView();
        loadStatus();
        setActions();
    }

    private void initView() {
        swSubuh = findViewById(R.id.swSubuh);
        swDzuhur = findViewById(R.id.swDzuhur);
        swAshar = findViewById(R.id.swAshar);
        swMaghrib = findViewById(R.id.swMaghrib);
        swIsya = findViewById(R.id.swIsya);
    }

    private void loadStatus() {
        swSubuh.setChecked(sp.getBoolean("SUBUH", true));
        swDzuhur.setChecked(sp.getBoolean("DZUHUR", true));
        swAshar.setChecked(sp.getBoolean("ASHAR", true));
        swMaghrib.setChecked(sp.getBoolean("MAGHRIB", true));
        swIsya.setChecked(sp.getBoolean("ISYA", true));
    }

    private void setActions() {

        swSubuh.setOnCheckedChangeListener((v, checked) ->
                sp.edit().putBoolean("SUBUH", checked).apply());

        swDzuhur.setOnCheckedChangeListener((v, checked) ->
                sp.edit().putBoolean("DZUHUR", checked).apply());

        swAshar.setOnCheckedChangeListener((v, checked) ->
                sp.edit().putBoolean("ASHAR", checked).apply());

        swMaghrib.setOnCheckedChangeListener((v, checked) ->
                sp.edit().putBoolean("MAGHRIB", checked).apply());

        swIsya.setOnCheckedChangeListener((v, checked) ->
                sp.edit().putBoolean("ISYA", checked).apply());
    }
}
