package com.yousouro.terserah;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class DetailActivity extends AppCompatActivity {
    private RequestQueue request_queue;
    private static String BASE_URL = "https://api.foursquare.com/v2/venues/";
    private Context ctx;
    private TextView detail_name, detail_location, detail_categories, detail_hours, detail_prices, detail_ratings, detail_contacts, detail_descriptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ctx = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Intent intent = getIntent();
        String id = intent.getStringExtra("id");
        detail_categories = findViewById(R.id.detail_categories);
        detail_contacts = findViewById(R.id.detail_contacts);
        detail_descriptions = findViewById(R.id.detail_descriptions);
        detail_hours = findViewById(R.id.detail_hours);
        detail_location = findViewById(R.id.detail_location);
        detail_prices = findViewById(R.id.detail_prices);
        detail_ratings = findViewById(R.id.detail_ratings);
        detail_name = findViewById(R.id.detail_name);
        request_queue = Volley.newRequestQueue(this);
        request_queue.add(getDetail(id));

        if(getSupportActionBar() != null){ getSupportActionBar().setDisplayHomeAsUpEnabled(true); }
    }

    @Override
    public boolean onSupportNavigateUp(){ finish(); return true; }

    private JsonObjectRequest getDetail(String id) {
        String URL = BASE_URL + id;
        Uri uri = Uri.parse(URL).buildUpon()
                .appendQueryParameter("client_id", getString(R.string.foursquare_client_id))
                .appendQueryParameter("client_secret", getString(R.string.foursquare_client_secret))
                .appendQueryParameter("v", "20190605")
                .build();
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, uri.toString(), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject responseFourSquare = response.getJSONObject("response").getJSONObject("venue");
                    String restaurant_name = responseFourSquare.getString("name");
                    String contact = "No contact";
                    String categories = "No info";
                    String location = "No info";
                    String description = "No description";
                    String hours = "No info";
                    String rating = "No info";
                    String price = "No info";
                    if(responseFourSquare.has("contact") && responseFourSquare.getJSONObject("contact").has("phone")) contact = responseFourSquare.getJSONObject("contact").getString("phone");
                    JSONArray alamatJSON = responseFourSquare.getJSONObject("location").getJSONArray("formattedAddress");
                    location = "";
                    for(int i = 0;i < alamatJSON.length();i++) {
                        if(i != 0) location += ", ";
                        String alamat_tmp = alamatJSON.getString(i);
                        location += alamat_tmp;
                    }


                    if(responseFourSquare.has("categories")) {
                        JSONArray categoriesJSON = responseFourSquare.getJSONArray("categories");
                        categories = "";
                        boolean first = true;
                        for(int i = 0;i < categoriesJSON.length();i++) {
                            if(categoriesJSON.getJSONObject(i).has("shortName")) {
                                if(!first) categories += "\n";
                                first = false;
                                String categories_tmp = categoriesJSON.getJSONObject(i).getString("shortName");
                                categories += categories_tmp;
                            }
                        }
                        if(first == true) categories = "No info";
                    }

                    if(responseFourSquare.has("description")) description = responseFourSquare.getString("description");
                    if(responseFourSquare.has("hours") && responseFourSquare.getJSONObject("hours").has("timeframes")) {
                        JSONArray openJSON = responseFourSquare.getJSONObject("hours").getJSONArray("timeframes");
                        hours = "";
                        boolean incomplete = false;
                        for(int i = 0;i < openJSON.length();i++) {
                            JSONObject time_frame = openJSON.getJSONObject(i);
                            if(!time_frame.has("days") || !time_frame.has("open") || !time_frame.getJSONArray("open").getJSONObject(0).has("renderedTime")) {
                                incomplete = true;
                            }
                            String days = time_frame.getString("days");
                            hours += days;
                            for(int j = days.length();j < 15;j++) hours += " ";
                            String open = time_frame.getJSONArray("open").getJSONObject(0).getString("renderedTime");
                            hours += open;
                            hours += "\n";
                        }
                        if(incomplete) hours = "No info";
                    }
                    if(responseFourSquare.has("rating")) {
                        double rate = responseFourSquare.getDouble("rating");
                        rating = rate + " / " + 10;
                    }
                    if(responseFourSquare.has("price")) {
                        price = responseFourSquare.getJSONObject("price").getString("message");
                    }
//                    Toast.makeText(ctx,
//                            "RESTAURANT NAME: " + restaurant_name +
//                                    "\nCONTACT: " + contact +
//                                    "\nLOCATION: " + location +
//                                    "\nCATEGORIES: " + categories +
//                                    "\nDESCRIPTION: " + description +
//                                    "\nOPENING HOURS: " + hours +
//                                    "\nRATING: " + rating +
//                                    "\nPRICE: " + price
//                            , Toast.LENGTH_LONG).show();
                    detail_categories.setText(categories);
                    detail_contacts.setText(contact);
                    detail_descriptions.setText(description);
                    detail_hours.setText(hours);
                    detail_location.setText(location);
                    detail_name.setText(restaurant_name);
                    detail_prices.setText(price);
                    detail_ratings.setText(rating);
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
}
