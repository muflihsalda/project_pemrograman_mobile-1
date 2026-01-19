package com.example.janganlupasholat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_CODE = 100;
    private static final String PREF_NAME = "ADZAN_PREF";
    private static final String KEY_LAST_DATE = "LAST_DATE";

    private TextView txtLokasi, txtZona, txtJam;
    private TextView txtSubuh, txtDzuhur, txtAshar, txtMaghrib, txtIsya;
    private Button btnSettingAdzan;

    private double latitude = 0.0;
    private double longitude = 0.0;

    private Handler clockHandler = new Handler();
    private FusedLocationProviderClient fusedLocationClient;
    private RequestQueue requestQueue;

    // ====================== LIFECYCLE ======================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initService();
        requestNotificationPermission();
        checkLocationPermission();
    }

    // ====================== INIT ======================
    private void initView() {
        txtLokasi = findViewById(R.id.txtLokasi);
        txtZona = findViewById(R.id.txtZona);
        txtJam = findViewById(R.id.txtJam);

        txtSubuh = findViewById(R.id.txtSubuh);
        txtDzuhur = findViewById(R.id.txtDzuhur);
        txtAshar = findViewById(R.id.txtAshar);
        txtMaghrib = findViewById(R.id.txtMaghrib);
        txtIsya = findViewById(R.id.txtIsya);

        btnSettingAdzan = findViewById(R.id.btnSettingAdzan);
        btnSettingAdzan.setOnClickListener(v ->
                startActivity(new Intent(this, SettingAdzanActivity.class))
        );
    }

    private void initService() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestQueue = Volley.newRequestQueue(this);
    }

    // ====================== PERMISSION ======================
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(
                        this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    200
            );
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_CODE
            );
        } else {
            getLastLocation();
        }
    }

    // ====================== LOCATION ======================
    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        onLocationReady();
                    } else {
                        Toast.makeText(this,
                                "Lokasi tidak ditemukan",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void onLocationReady() {
        setCityName(latitude, longitude);
        setZonaWaktu();
        startClock();
        fetchJadwalSholat(latitude, longitude);
    }

    private void setCityName(double lat, double lon) {
        try {
            Geocoder g = new Geocoder(this, Locale.getDefault());
            List<Address> list = g.getFromLocation(lat, lon, 1);
            if (list != null && !list.isEmpty()) {
                Address a = list.get(0);
                txtLokasi.setText(a.getLocality() + ", " + a.getCountryName());
            }
        } catch (Exception e) {
            txtLokasi.setText("Lokasi tidak diketahui");
        }
    }

    // ====================== ZONA WAKTU ======================
    private void setZonaWaktu() {
        TimeZone tz = TimeZone.getDefault();
        String display = tz.getDisplayName(false, TimeZone.SHORT, Locale.getDefault());
        txtZona.setText(display); // WIB, JST, GMT+9, dll
    }

    // ====================== JAM ======================
    private void startClock() {
        clockHandler.post(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat sdf =
                        new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getDefault());

                txtJam.setText(sdf.format(new Date()));
                clockHandler.postDelayed(this, 1000);
            }
        });
    }

    // ====================== JADWAL SHOLAT ======================
    private void fetchJadwalSholat(double lat, double lon) {

        if (isAdzanScheduledToday()) return;

        String url = "https://api.aladhan.com/v1/timings?latitude=" + lat +
                "&longitude=" + lon + "&method=2";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONObject t = response
                                .getJSONObject("data")
                                .getJSONObject("timings");

                        String subuh = cleanTime(t.getString("Fajr"));
                        String dzuhur = cleanTime(t.getString("Dhuhr"));
                        String ashar = cleanTime(t.getString("Asr"));
                        String maghrib = cleanTime(t.getString("Maghrib"));
                        String isya = cleanTime(t.getString("Isha"));

                        txtSubuh.setText(subuh);
                        txtDzuhur.setText(dzuhur);
                        txtAshar.setText(ashar);
                        txtMaghrib.setText(maghrib);
                        txtIsya.setText(isya);

                        scheduleAdzan(subuh, "Subuh");
                        scheduleAdzan(dzuhur, "Dzuhur");
                        scheduleAdzan(ashar, "Ashar");
                        scheduleAdzan(maghrib, "Maghrib");
                        scheduleAdzan(isya, "Isya");

                        saveAdzanScheduledToday();

                    } catch (Exception e) {
                        Toast.makeText(this,
                                "Gagal memproses jadwal",
                                Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this,
                        "Gagal mengambil jadwal",
                        Toast.LENGTH_SHORT).show()
        );

        requestQueue.add(request);
    }

    private String cleanTime(String time) {
        return time.split(" ")[0]; // HH:mm
    }

    // ====================== NOTIF ADZAN ======================
    private void scheduleAdzan(String waktu, String nama) {
        try {
            String[] parts = waktu.split(":");
            if (parts.length < 2) return;

            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            Calendar now = Calendar.getInstance(TimeZone.getDefault());
            Calendar sholat = Calendar.getInstance(TimeZone.getDefault());

            sholat.set(Calendar.HOUR_OF_DAY, hour);
            sholat.set(Calendar.MINUTE, minute);
            sholat.set(Calendar.SECOND, 0);

            if (sholat.before(now)) {
                sholat.add(Calendar.DAY_OF_MONTH, 1);
            }

            long delay = sholat.getTimeInMillis() - now.getTimeInMillis();
            if (delay <= 0) return;

            Data data = new Data.Builder()
                    .putString("title", "Waktu Sholat " + nama)
                    .putString("message", "Sudah masuk waktu sholat " + nama)
                    .build();

            OneTimeWorkRequest work =
                    new OneTimeWorkRequest.Builder(AdzanWorker.class)
                            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                            .setInputData(data)
                            .build();

            WorkManager.getInstance(this)
                    .enqueueUniqueWork(
                            "ADZAN_" + nama,
                            ExistingWorkPolicy.REPLACE,
                            work
                    );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ====================== PREF ======================
    private boolean isAdzanScheduledToday() {
        SharedPreferences prefs =
                getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        String today =
                new SimpleDateFormat("yyyyMMdd",
                        Locale.getDefault()).format(new Date());

        return today.equals(prefs.getString(KEY_LAST_DATE, ""));
    }

    private void saveAdzanScheduledToday() {
        SharedPreferences prefs =
                getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        String today =
                new SimpleDateFormat("yyyyMMdd",
                        Locale.getDefault()).format(new Date());

        prefs.edit().putString(KEY_LAST_DATE, today).apply();
    }

    // ====================== PERMISSION RESULT ======================
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLastLocation();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clockHandler.removeCallbacksAndMessages(null);
    }
}
