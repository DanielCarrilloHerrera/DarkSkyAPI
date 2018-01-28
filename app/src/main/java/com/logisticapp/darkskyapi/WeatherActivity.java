package com.logisticapp.darkskyapi;

import android.annotation.SuppressLint;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.logisticapp.darkskyapi.Classes.AppController;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;



public class WeatherActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    //Petición HTTP a esta URL, para recuperar condiciones del clima
    String weatherWebServiceURL;


    //Dialogo de carga
    ProgressBar pBar;

    //Textviews
    TextView tvTemperatura, tvViento, tvSentidoViento, tvHumedad, tvWindDaily, tvDate;
    TextView tvMaxTemperatura, tvMinTemperatura, tvProbabilidadLluvia;
    TextView tvCloudCover, tvPressure, tvMoonPhase;

    //Layout de la pantalla
    LinearLayout layoutPrincipal;

    //Icon
    ImageView icon, fondo;

    //Toolbar
    Toolbar toolbar;

    //Para la localización
    private GoogleApiClient googleApiClient;
    private LatLng latLng;

    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        icon = (ImageView) findViewById(R.id.ivIcon);
        fondo = (ImageView) findViewById(R.id.ivFondo);
        tvTemperatura = (TextView) findViewById(R.id.tvTemperatura);
        tvViento = (TextView) findViewById(R.id.tvViento);
        tvSentidoViento = (TextView) findViewById(R.id.tvSentidoViento);
        tvHumedad = (TextView) findViewById(R.id.tvHumedad);
        tvDate = (TextView) findViewById(R.id.tvDate);
        tvWindDaily = (TextView) findViewById(R.id.tvWindDaily);
        tvMaxTemperatura = (TextView) findViewById(R.id.tvMaximaTemperatura);
        tvMinTemperatura = (TextView) findViewById(R.id.tvMinimaTemperatura);
        tvProbabilidadLluvia = (TextView) findViewById(R.id.tvProbabilidadLluvia);
        tvMoonPhase = (TextView) findViewById(R.id.tvMoonPhase);
        tvPressure = (TextView) findViewById(R.id.tvPressure);
        tvCloudCover = (TextView) findViewById(R.id.tvCloudCover);

        layoutPrincipal = (LinearLayout) findViewById(R.id.llPrincipal);
        //layoutUno = (LinearLayout) findViewById(R.id.llUno);
        //layoutDos = (LinearLayout) findViewById(R.id.llDos);


        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


        if (googleApiClient != null) {
            googleApiClient.connect();
        }



        cargarBarraProgreso();
        realizarPeticionHttpTVSH();
    }

    public void cargarBarraProgreso() {
        pBar = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        layoutPrincipal.addView(pBar, params);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);

        //Barra de progreso
        if (pBar.getParent() != null)
            ((ViewGroup) pBar.getParent()).removeView(pBar);

        layoutPrincipal.addView(pBar, params);
        pBar.setVisibility(View.VISIBLE);  //Mostrar pBar
    }

    public void realizarPeticionHttpTVSH() {

        latLng = getLocation();

        weatherWebServiceURL = "https://api.darksky.net/forecast/8633461193a3a899b847a39b3b9a9fa4/"+ latLng.latitude+","+ latLng.longitude;

        JsonObjectRequest jsonObjRequest = new JsonObjectRequest(Request.Method.GET,
                weatherWebServiceURL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {


                    //La ciudad actual
                    TextView mTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
                    mTitle.setText(getCiudad());

                    //Parsing json object response
                    // response will be a json object

                    JSONObject hora = (JSONObject) response.get("hourly");
                    JSONObject data = (JSONObject) ((JSONArray) hora.get("data")).get(0);

                    SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMM DD", Locale.US);
                    Date dateFormat = new Date(data.getLong("time"));
                    String dayOfWeek = sdf.format(dateFormat);

                    switch (hora.getString("icon")){

                        case "clear-day":
                            icon.setImageResource(R.drawable.clear_day);
                            fondo.setImageResource(R.drawable.clear_day_big);
                        break;

                        case "clear-night":
                            icon.setImageResource(R.drawable.clear_night_big);
                            fondo.setImageResource(R.drawable.clear_night_big);
                        break;

                        case "cloudy":
                            icon.setImageResource(R.drawable.cloudy);
                            fondo.setImageResource(R.drawable.cloudy_big);
                        break;

                        case "fog":
                            icon.setImageResource(R.drawable.fog);
                            fondo.setImageResource(R.drawable.fog_big);
                        break;

                        case "hail":
                            icon.setImageResource(R.drawable.hail);
                            fondo.setImageResource(R.drawable.hail_big);
                        break;

                        case "partly-cloudy-day":
                            icon.setImageResource(R.drawable.partly_cloudy_day);
                            fondo.setImageResource(R.drawable.partly_cloudy_day_big);
                        break;

                        case "partly-cloudy-night":
                            icon.setImageResource(R.drawable.partly_cloudy_night);
                            fondo.setImageResource(R.drawable.partly_cloudy_night);
                        break;

                        case "rain":
                            icon.setImageResource(R.drawable.rain);
                            fondo.setImageResource(R.drawable.rain);
                        break;

                        case "sleet":
                            icon.setImageResource(R.drawable.sleet);
                            fondo.setImageResource(R.drawable.sleet_big);
                        break;

                        case "snow":
                            icon.setImageResource(R.drawable.snow);
                            fondo.setImageResource(R.drawable.snow_big);
                        break;

                        case "thunderstorm":
                            icon.setImageResource(R.drawable.thunderstorm);
                            fondo.setImageResource(R.drawable.thunderstorm);
                        break;

                        case "tornado":
                            icon.setImageResource(R.drawable.tornado);
                            fondo.setImageResource(R.drawable.tornado_big);
                            break;

                        case "wind":
                            icon.setImageResource(R.drawable.wind);
                            fondo.setImageResource(R.drawable.wind_big);
                        break;

                    }
                    tvDate.setText(dayOfWeek);
                    tvWindDaily.setText(String.valueOf(hora.get("summary")));

                    //Mostrar la temperatura
                    tvTemperatura.setText(String.valueOf(data.get("temperature") + " °"));

                    //Mostrar la velocidad del viento
                    tvViento.setText("Windspeed: "+String.valueOf(data.get("windSpeed")) + " MPH");

                    //Mostrar el sentido del viento
                    if (data.getInt("windBearing") > 0)
                        tvSentidoViento.setText("Windbearing: South");
                    else
                        tvSentidoViento.setText("Windbearing: North");

                    //Mostrar la humedad
                    tvHumedad.setText("Humidity: " + (data.getDouble("humidity") * 100) + "%");

                    //Show pressure
                    tvPressure.setText("Pressure: "+ (data.getDouble("pressure")) + " mlb");

                    //Show Cloud cover
                    tvCloudCover.setText("Cloud cover: "+ (data.getDouble("cloudCover")*100) + "%");


                    JSONObject diario = (JSONObject) response.get("daily");
                    data = (JSONObject) ((JSONArray) diario.get("data")).get(0);

                    //Temperatura y probabilidad de lluvia
                    tvMaxTemperatura.setText("Maxime temperature: " + data.getDouble("temperatureMax") + " °");
                    tvMinTemperatura.setText("Minimum temperature: " + data.getDouble("temperatureMin") + " °");
                    tvProbabilidadLluvia.setText("Chance of precipitation: " + (data.getDouble("precipProbability") * 100) + "%");

                    //Lunar phase
                    double moonPhase = data.getDouble("moonPhase");

                    if(moonPhase == 0 )
                        tvMoonPhase.setText("Moon phase: New moon");
                    if(moonPhase == 0.25 )
                        tvMoonPhase.setText("Moon phase: First quarter moon");
                    if(moonPhase == 0.5 )
                        tvMoonPhase.setText("Moon phase: Full moon");
                    if(moonPhase == 0.75 )
                        tvMoonPhase.setText("Moon phase: Last quarter moon");

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Error, try again !", Toast.LENGTH_LONG).show();
                    pBar.setVisibility(View.INVISIBLE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("tag", "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), "Error while loading ...", Toast.LENGTH_SHORT).show();
                //Ocultar la barra de progreso
                pBar.setVisibility(View.INVISIBLE);
            }
        });

        //Esconder el dialogo de carga
        pBar.setVisibility(View.INVISIBLE);

        //Agregar petición a la cola de petición
        AppController.getInstance(this).addToRequestQueue(jsonObjRequest);

    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private double lat,lon;
    @SuppressLint("MissingPermission")
    private LatLng getLocation()
    {
        // Get the location manager
        LocationManager locationManager =
                (LocationManager) getSystemService(LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, false);
        @SuppressLint("MissingPermission")
        Location location = locationManager.getLastKnownLocation(bestProvider);

        try {

            lat = location.getLatitude();
            lon = location.getLongitude();
            return new LatLng(lat, lon);
        }
        catch (NullPointerException e){
            e.printStackTrace();


            //Se obtiene la ultima localización almacenada
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Logic to handle location object
                                lat = location.getLatitude();
                                lon = location.getLongitude();

                            }
                        }
                    });

            return new LatLng(lat, lon);
        }
    }

    private String getCiudad(){

        //Get the current city
        LatLng location = getLocation();
        String locality = "";
        Geocoder gcd = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = gcd.getFromLocation(location.latitude, location.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses != null && addresses.size() > 0) {
            locality = addresses.get(0).getLocality();
        }
        return  locality;
    }

}
