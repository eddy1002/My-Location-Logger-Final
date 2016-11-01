package com.example.mylocationloggertest01;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

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
    public Marker mark[];

    public void onMapReady(final GoogleMap map) {
        googleMap = map;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        GoogleMapOn = true;
        googleMap.setMyLocationEnabled(true);

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom( SEOUL, 15));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        GpsCount = pref.getInt("GpsCount", 144);
        saveNum = pref.getInt("saveNum", 0);

        btnShowLocation = (Button) findViewById(R.id.But_GPS);
        txtLat = (TextView) findViewById(R.id.Latitude);
        txtLon = (TextView) findViewById(R.id.Longitude);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (GpsCount > 0){
                    GpsCount--;
                    SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putInt("GpsCount", GpsCount);
                    editor.commit();

                    ReadGPS();
                    this.sendEmptyMessageDelayed(0, 1000 * 60 * 10);
                }
                else if (GpsOn) {
                    GpsOn = false; GpsCount = 144; saveNum = 0;
                }
            }
        };

        // GPS 정보를 보여주기 위한 이벤트 클래스 등록
        btnShowLocation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                if (!GpsOn) {
                    GpsOn = true;
                    handler.sendEmptyMessage(1);
                }
            }
        });
    }

    public void ReadGPS(){
        gps = new GpsInfo(MainActivity.this);
        // GPS 사용유무 가져오기
        if (gps.isGetLocation()) {
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            txtLat.setText(String.valueOf(latitude));
            txtLon.setText(String.valueOf(longitude));
            Toast.makeText(
                    getApplicationContext(),
                    "남은 횟수 : " + GpsCount + ", " + saveNum,
                    Toast.LENGTH_LONG).show();
            // 밑으로 저장하는 코드
            SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putFloat("Lat" + saveNum, (float) latitude);
            editor.commit();
            editor.putFloat("Lon" + saveNum, (float) longitude);
            editor.commit();
            saveNum++;
            editor.putInt("saveNum", saveNum);
            editor.commit();
        } else {
            // GPS 를 사용할수 없으므로
            gps.showSettingsAlert();
        }
    }

    public void SaveTestting(View v){
        if (GoogleMapOn){
            SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
            double latitude; double longitude;
            for (int i = 0; i < 144; i++) {
                mark[i].remove();

                latitude = pref.getFloat("Lat" + i, -1);
                longitude = pref.getFloat("Lon" + i, -1);
                if (latitude > 0 && longitude > 0) {
                    SEOUL = new LatLng(latitude, longitude);

                    mark[i] = googleMap.addMarker(new MarkerOptions().position(SEOUL).title("You " + i));
                    if (i == 0) {
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SEOUL, 15));
                        googleMap.animateCamera(CameraUpdateFactory.zoomTo(20), 2000, null);
                    }
                }
            }
        }
        else{
            Toast.makeText(
                    getApplicationContext(),
                    "구글맵이 로드 중..",
                    Toast.LENGTH_LONG).show();
        }
    }
}
