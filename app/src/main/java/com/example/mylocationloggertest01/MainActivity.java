package com.example.mylocationloggertest01;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements OnMapReadyCallback {

    private Button btnShowLocation;
    private TextView txtLat;
    private TextView txtLon;

    // GPSTracker class
    private GpsInfo gps;

    // 저장시 사용
    public int saveNum = 0;

    // 타이머 사용
    private boolean GpsOn = false;
    private Handler handler;
    public int GpsCount = 144; // 10분마다 이므로 하루에 144번만 저장하면 됨.

    // Google Map
    private boolean GoogleMapOn = false;
    static LatLng SEOUL = new LatLng(37.56, 126.97);
    private GoogleMap googleMap;
    private PolylineOptions polylineOptions;
    private List<LatLng> arrayPoints = new ArrayList<LatLng>();

    //checking
    private RadioButton radio_Rest;
    private RadioButton radio_Stdy;
    private RadioButton radio_Eatt;

    private int checking = 0;

    private EditText edit_Name;

    private int R_Num;
    private int S_Num;
    private int E_Num;

    private TextView txt_Rrate;
    private TextView txt_Srate;
    private TextView txt_Erate;

    public void onMapReady(final GoogleMap map) {
        googleMap = map;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        GoogleMapOn = true;
        googleMap.setMyLocationEnabled(true);

        //Marker seoul = googleMap.addMarker(new MarkerOptions().position(SEOUL).title("Seoul"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom( SEOUL, 15));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);

        SaveTestting();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        radio_Rest = (RadioButton) findViewById(R.id.radioButton_Rest);
        radio_Stdy = (RadioButton) findViewById(R.id.radioButton_Study);
        radio_Eatt = (RadioButton) findViewById(R.id.radioButton_Eat);

        edit_Name = (EditText) findViewById(R.id.editText_Name);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        GpsCount = pref.getInt("GpsCount", 144);
        saveNum = pref.getInt("saveNum", 0);

        R_Num = pref.getInt("RNum", 0);
        S_Num = pref.getInt("SNum", 0);
        E_Num = pref.getInt("ENum", 0);

        txt_Rrate = (TextView) findViewById(R.id.textView_Rrate);
        txt_Srate = (TextView) findViewById(R.id.textView_Srate);
        txt_Erate = (TextView) findViewById(R.id.textView_Erate);

        Toast.makeText(
                getApplicationContext(),
                "카운트 초기화",
                Toast.LENGTH_LONG).show();

        btnShowLocation = (Button) findViewById(R.id.But_GPS);
        txtLat = (TextView) findViewById(R.id.Latitude);
        txtLon = (TextView) findViewById(R.id.Longitude);
    }

    public void ReadGPS(View v){
        gps = new GpsInfo(MainActivity.this);
        // GPS 사용유무 가져오기
        if (gps.isGetLocation()) {
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            txtLat.setText(String.valueOf(latitude));
            txtLon.setText(String.valueOf(longitude));

            // 밑으로 저장하는 코드
            SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putFloat("Lat" + saveNum, (float) latitude);
            editor.commit();
            editor.putFloat("Lon" + saveNum, (float) longitude);
            editor.commit();
            editor.putString("Nam" + saveNum, edit_Name.getText().toString());
            editor.commit();
            saveNum++;
            editor.putInt("saveNum", saveNum);
            editor.commit();

            if (checking == 0){
                R_Num++;
                editor.putInt("RNum", R_Num);
                editor.commit();
                Toast.makeText(
                        getApplicationContext(),
                        "휴식 저장되었습니다.",
                        Toast.LENGTH_LONG).show();
            }
            else if (checking == 1){
                S_Num++;
                editor.putInt("SNum", S_Num);
                editor.commit();
                Toast.makeText(
                        getApplicationContext(),
                        "공부 저장되었습니다.",
                        Toast.LENGTH_LONG).show();
            }
            else if (checking == 2){
                E_Num++;
                editor.putInt("ENum", E_Num);
                editor.commit();
                Toast.makeText(
                        getApplicationContext(),
                        "식사 저장되었습니다.",
                        Toast.LENGTH_LONG).show();
            }

            SaveTestting();
        } else {
            // GPS 를 사용할수 없으므로
            gps.showSettingsAlert();
        }
    }

    public void SaveTestting(){
        googleMap.clear();
        arrayPoints.clear();

        EditText edt_Name = (EditText) findViewById(R.id.editText_Name);

        if (GoogleMapOn){
            SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
            double latitude; double longitude; String name;
            for (int i = 0; i < saveNum; i++) {
                latitude = pref.getFloat("Lat" + i, -1);
                longitude = pref.getFloat("Lon" + i, -1);
                name = pref.getString("Nam" + i, "NoName");
                if (latitude > 0 && longitude > 0) {
                    SEOUL = new LatLng(latitude, longitude);
                    arrayPoints.add(SEOUL);
                    Marker seoul = googleMap.addMarker(new MarkerOptions().position(SEOUL).title(name));
                    if (i == 0) {
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SEOUL, 15));
                        googleMap.animateCamera(CameraUpdateFactory.zoomTo(20), 2000, null);
                    }
                }
            }

            polylineOptions = new PolylineOptions();
            polylineOptions.color(Color.RED);
            polylineOptions.width(5);

            polylineOptions.addAll(arrayPoints);
            googleMap.addPolyline(polylineOptions);
        }
        else{
            Toast.makeText(
                    getApplicationContext(),
                    "구글맵이 로드 중..",
                    Toast.LENGTH_LONG).show();
        }
    }

    public void radioCheckR(View v){
        radio_Rest.setChecked(true);
        radio_Eatt.setChecked(false);
        radio_Stdy.setChecked(false);

        checking = 0;
    }
    public void radioCheckS(View v){
        radio_Rest.setChecked(false);
        radio_Eatt.setChecked(false);
        radio_Stdy.setChecked(true);

        checking = 1;
    }
    public void radioCheckE(View v){
        radio_Rest.setChecked(false);
        radio_Stdy.setChecked(false);
        radio_Eatt.setChecked(true);

        checking = 2;
    }

    public void showRate(View v){
        int totalrate = R_Num + S_Num + E_Num;

        if (totalrate <= 0){
            return;
        }

        float R_rating = (float) R_Num / (float)totalrate * 100;
        txt_Rrate.setText("휴식 : " + String.format("%.2f", R_rating) + "%");

        float S_rating = (float)S_Num / (float)totalrate * 100;
        txt_Srate.setText("공부 : " + String.format("%.2f", S_rating) + "%");

        float E_rating = (float)E_Num / (float)totalrate * 100;
        txt_Erate.setText("식사 : " + String.format("%.2f", E_rating) + "%");
    }

}
