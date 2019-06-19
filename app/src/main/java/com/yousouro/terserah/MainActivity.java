package com.yousouro.terserah;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tv_nama_restoran;
    private TextView tv_lokasi;
    private TextView user_location;
    private Button btn_detail;
    private Button btn_terserah;
    private RequestQueue request_queue;
    private JSONObject result;

    private static final String BASE_URL = "https://api.foursquare.com/v2/venues/search";
    private Context ctx;
    private String id;
    static final int REQUEST_LOCATION = 1;
    LocationManager locationManager;

    double longitude, latitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();

        ctx = this;
        setContentView(R.layout.activity_main);
        user_location = findViewById(R.id.user_location);
        tv_nama_restoran = findViewById(R.id.tv_nama_restoran);
        btn_detail = findViewById(R.id.btn_detail);
        btn_terserah = findViewById(R.id.btn_terserah);
        tv_lokasi = findViewById(R.id.tv_lokasi);
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        longitude = 1;
        latitude = -1;
//        longitude = 106.781682;
//        latitude = -6.191286;
        if (Build.VERSION.SDK_INT >= 23){


            if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION,getApplicationContext(),this)) {
                //You fetch the Location here
                fetchLocationData();
                //code to use the
            }
            else
            {
                requestPermission(Manifest.permission.ACCESS_FINE_LOCATION,REQUEST_LOCATION,getApplicationContext(),this);
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchLocationData();
    }

    protected void terserah() {
        btn_detail.setVisibility(View.INVISIBLE);
        request_queue = Volley.newRequestQueue(this);
        request_queue.add(getFood());
    }

    protected void detailMenu(View view) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("id", id);
        startActivity(intent);
    }

    private JsonObjectRequest getFood() {

        String latlong = latitude + "," + longitude;
        Uri uri = Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter("client_id", getString(R.string.foursquare_client_id))
                .appendQueryParameter("client_secret", getString(R.string.foursquare_client_secret))
                .appendQueryParameter("ll", latlong)
                .appendQueryParameter("radius", "1000")
                .appendQueryParameter("limit", "50")
                .appendQueryParameter("categoryId", "4d4b7105d754a06374d81259")
                .appendQueryParameter("v", "20190605")
                .build();
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, uri.toString(), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray arrResult = response.getJSONObject("response").getJSONArray("venues");
                    int sz = arrResult.length();
                    if(sz == 0) {
                        btn_detail.setVisibility(View.INVISIBLE);
                        tv_nama_restoran.setText("Tidak ada restoran sekitar area Anda yang buka");
                    } else {
                        int idx = (int) (Math.random() * sz);
                        JSONObject objresult = arrResult.getJSONObject(idx);
                        String name = objresult.getString("name");
                        id = objresult.getString("id");
                        JSONArray alamatJSON = objresult.getJSONObject("location").getJSONArray("formattedAddress");
                        String alamat = "";
                        for(int i = 0;i < alamatJSON.length();i++) {
                            if(i != 0) alamat += ", ";
                            String alamat_tmp = alamatJSON.getString(i);
                            alamat += alamat_tmp;
                        }
                        tv_nama_restoran.setText(name);
                        tv_lokasi.setText(alamat);
                        result = objresult;
                        btn_detail.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        return req;
    }

    //https://hafizahusairi.com/2017/08/26/tutorial-get-current-latitude-longitude-android-studio-2017/
    void fetchLocationData() {
        GPSTracker gps = new GPSTracker(this);
		Geocoder geocoder = new Geocoder(this, Locale.getDefault());
		Address result;
		String city = "", state = "", country = "";
        user_location.setText("Fetching...");

        if(gps.canGetLocation()){
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
            try {
				result = geocoder.getFromLocation(latitude, longitude, 1).get(0);
				city = result.getLocality();
				state = result.getAdminArea();
				country = result.getCountryName();
			} catch (Exception e){ }

            if(city.length() == 0 || state.length() == 0 || country.length() == 0){
            	user_location.setText("Failed to Fetch Location...");
				btn_terserah.setText("Reload Location");
				btn_terserah.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) { fetchLocationData(); }
				});
			} else{
				user_location.setText(String.format("%s,\n%s,\n%s", city, state, country));
				btn_terserah.setText("Terserah");
				btn_terserah.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) { terserah(); }
				});
			}
        } else{
            user_location.setText("Location Not Found...");
            btn_terserah.setText("Reload Location");
            btn_terserah.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) { fetchLocationData(); }
			});
            gps.showSettingsAlert();
        }
    }

    public void requestPermission(String strPermission, int perCode, Context _c, Activity _a){
        if (ActivityCompat.shouldShowRequestPermissionRationale(_a,strPermission)){
            Toast.makeText(getApplicationContext(),"GPS permission allows us to access location data. Please allow in App Settings for additional functionality.",Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(_a,new String[]{strPermission},perCode);
        }
    }

    public static boolean checkPermission(String strPermission, Context _c, Activity _a){
        int result = ContextCompat.checkSelfPermission(_c, strPermission);
        return (result == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fetchLocationData();
                } else {
                    Toast.makeText(getApplicationContext(),"Permission Denied, You cannot access location data.",Toast.LENGTH_LONG).show();
                }
                break;

        }
    }
}