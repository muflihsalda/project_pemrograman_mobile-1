package com.example.janganlupasholat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.List;
import java.util.Locale;

public class WelcomeActivity extends AppCompatActivity {

    FusedLocationProviderClient fused;
    double lat, lon;

    ImageView flagImage;
    TextView welcomeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_activity);

        fused = LocationServices.getFusedLocationProviderClient(this);

        flagImage = findViewById(R.id.flagImage);
        welcomeText = findViewById(R.id.welcomeText);

        getLastLocation();
    }

    private void getLastLocation() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        fused.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                lat = location.getLatitude();
                lon = location.getLongitude();

                setCountryUI(lat, lon);
                goNext();
            } else {
                goNext(); // tetap masuk ke main walau gagal
            }
        });
    }

    private void setCountryUI(double lat, double lon) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> list = geocoder.getFromLocation(lat, lon, 1);

            if (list != null && !list.isEmpty()) {

                String code = list.get(0).getCountryCode().toLowerCase();

                // SET TEXT SELAMAT DATANG SESUAI NEGARA
                switch (code) {
                    case "id":
                        welcomeText.setText("Selamat Datang");
                        flagImage.setImageResource(R.drawable.flag_id);
                        break;

                    case "my":
                        welcomeText.setText("Selamat Datang (Malaysia)");
                        flagImage.setImageResource(R.drawable.flag_my);
                        break;

                    case "sa":
                        welcomeText.setText("أهلاً وسهلاً");
                        flagImage.setImageResource(R.drawable.flag_sa);
                        break;

                    case "us":
                        welcomeText.setText("Welcome");
                        flagImage.setImageResource(R.drawable.flag_us);
                        break;

                    case "jp":
                        welcomeText.setText("ようこそ");
                        flagImage.setImageResource(R.drawable.flag_jp);
                        break;

                    default:
                        welcomeText.setText("Welcome");
                        flagImage.setImageResource(R.drawable.flag_us);
                        break;
                }
            }

        } catch (Exception ignored) { }
    }

    private void goNext() {
        new Handler().postDelayed(() -> {
            Intent i = new Intent(WelcomeActivity.this, MainActivity.class);
            i.putExtra("lat", lat);
            i.putExtra("lon", lon);
            startActivity(i);
            finish();
        }, 1500);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);  // <— FIX WARNING!!

        if (requestCode == 100 &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            getLastLocation();
        }
    }
}
