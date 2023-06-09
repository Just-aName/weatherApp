package com.vsb.kol0482.mobileweatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout homeRL;
    private ProgressBar loadingPB;
    private TextView temperatureTV;
    private RecyclerView weatherRV;
    private ImageView backIV;
    private ArrayList<WeatherRVModal> weatherRVModalArrayList;
    private WeatherRVAdapter weatherRVAdapter;
    private static final int SECOND_ACTIVITY_REQUEST_CODE = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WidgetSettings.initWidgetSettings(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.main_activity_toolbar);
        setSupportActionBar(toolbar);

        homeRL = findViewById(R.id.idRLHome);
        loadingPB = findViewById(R.id.idPBLoading);
        temperatureTV = findViewById(R.id.idTVTemperature);
        weatherRV = findViewById(R.id.idRVWeather);
        backIV = findViewById(R.id.idIVBack);
        weatherRVModalArrayList = new ArrayList<>();
        weatherRVAdapter = new WeatherRVAdapter(this, weatherRVModalArrayList);
        weatherRV.setAdapter(weatherRVAdapter);
        backIV.setImageResource(R.drawable.mainactivitybackground);
        Intent intent = new Intent(MainActivity.this, Login.class);
        if(WidgetSettings.GetConnectionString().equals("") || WidgetSettings.GetToken().equals("")) {
            startActivityForResult(intent, SECOND_ACTIVITY_REQUEST_CODE);
        }
        else {
            getWeatherInfo();
        }
    }

    private void getWeatherInfo()
    {
        String url = WidgetSettings.GetConnectionString() + "/api/RawData/GetCurrent";
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Request.Method.GET, url,
                null,
                new Response.Listener<JSONArray>(){
            @Override
            public void onResponse(JSONArray response) {
                loadingPB.setVisibility(View.GONE);
                homeRL.setVisibility(View.VISIBLE);
                weatherRVModalArrayList.clear();
                try {
                    JSONObject data = response.getJSONObject(0);
                    String temperature = data.getString("outTemp_C");
                    temperatureTV.setText(temperature + "°C");
                    WidgetOptions[] options = WidgetOptions.values();
                    for (int j = 0; j < options.length; j++) {
                        String apiDataName = ApiDataBinder.GetApiVersionFromEnum(options[j]);
                        if(apiDataName.equals(""))
                            continue;
                        String number = data.getString(apiDataName);
                        String unit = ApiDataBinder.GetUnitBaseOnEnum(options[j]);
                        String name = options[j].getValue();
                        weatherRVModalArrayList.add(new WeatherRVModal(number, name, unit));
                    }

                    /*
                    for(int i=1;i<response.length();i++){
                        JSONObject jData = response.getJSONObject(i);
                        // Get current json object
                        String time = jData.getString("time");
                        String temper = jData.getString("outTemp_C");
                        String wind = jData.getString("windSpeed_mps");


                    }
  */
                    weatherRVAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Api request Error", Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + WidgetSettings.GetToken());
                return headers;
            }
        };

        requestQueue.add(jsonObjectRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.popup_menu, menu);
        return true;    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.item1:
                startActivity(new Intent(MainActivity.this, MenuActivity.class));
                return true;
            case R.id.item2:
                startActivity(new Intent(MainActivity.this, Settings.class));
                return true;
            case R.id.item3:
                startActivity(new Intent(MainActivity.this, MultipleMenu.class));
                return true;
            case R.id.item4:
                WidgetSettings.SetToken("");
                Intent intent = getIntent();
                finish();
                startActivity(intent);
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check which request we're responding to
        if (requestCode == SECOND_ACTIVITY_REQUEST_CODE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // Do something with the data returned from the second activity
                getWeatherInfo();
            }
        }
    }
}
