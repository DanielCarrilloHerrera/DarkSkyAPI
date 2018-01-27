package com.logisticapp.darkskyapi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.ViewGroup;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;



public class WeatherActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    //Petición HTTP a esta URL, para recuperar condiciones del clima
    String weatherWebServiceURL;

    //Objeto response
    JSONObject respuesta;

    //Dialogo de carga
    ProgressBar pBar;

    //Textview para mostrar temperatura y su descripción
    TextView tvTemperatura, tvViento, tvSentidoViento, tvHumedad, tvWindDaily, tvDate;
    //Imagen de fondo


    //Layout de la pantalla
    LinearLayout layoutPrincipal;

    //LinearLayout de los días de la semana
    LinearLayout layoutDiasSemana;

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

        tvTemperatura = (TextView) findViewById(R.id.tvTemperatura);
        tvViento = (TextView) findViewById(R.id.tvViento);
        tvSentidoViento = (TextView) findViewById(R.id.tvSentidoViento);
        tvHumedad = (TextView) findViewById(R.id.tvHumedad);
        tvDate = (TextView) findViewById(R.id.tvDate);
        tvWindDaily = (TextView) findViewById(R.id.tvWindDaily);

        layoutPrincipal = (LinearLayout) findViewById(R.id.llPrincipal);
        layoutDiasSemana = (LinearLayout) findViewById(R.id.llDiasSemana);


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
        realizarPeticionesHttpDiasSemana();
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

                    tvDate.setText(dayOfWeek);
                    tvWindDaily.setText(String.valueOf(hora.get("summary")));

                    //Mostrar la temperatura
                    tvTemperatura.setText(String.valueOf(data.get("temperature") + " °"));

                    //Mostrar la velocidad del viento
                    tvViento.setText(String.valueOf(data.get("windSpeed")));

                    //Mostrar el sentido del viento
                    if (data.getInt("windBearing") > 0)
                        tvSentidoViento.setText("South");
                    else
                        tvSentidoViento.setText("North");

                    //Mostrar la humedad
                    tvHumedad.setText(String.valueOf(data.get("humidity")));


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

    public void realizarPeticionesHttpDiasSemana() {

        ArrayList<String> fechas = calcularFechasDiasSemana();

        latLng = getLocation();

        //Se cargan todos los días

        //Dia uno
        LinearLayout llDia = findViewById(R.id.llDiaUno);
        cargarDia(latLng, fechas.get(0), llDia);

        //Dia dos
        llDia = findViewById(R.id.llDiaDos);
        cargarDia(latLng, fechas.get(1), llDia);

        //Dia tres
        llDia = findViewById(R.id.llDiaTres);
        cargarDia(latLng, fechas.get(2), llDia);

        //Dia cuatro
        llDia = findViewById(R.id.llDiaCuatro);
        cargarDia(latLng, fechas.get(3), llDia);

        //Dia cinco
        llDia = findViewById(R.id.llDiaCinco);
        cargarDia(latLng, fechas.get(4), llDia);




    }


    //Calcular las fechas de los días mostrados en pantalla(Tiempo en milisegundos UTC)
    //Se calculan cinco días: Antes de ayer, ayer, hoy, mañana y pasado mañana
    private ArrayList<String> calcularFechasDiasSemana() {

        ArrayList<String> dias = new ArrayList<String>();

        Date hoy = new Date();


        Calendar calendar = Calendar.getInstance();
        calendar.setTime(hoy);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy - MM - DD hh:mm:ss", Locale.US);
        Date hoyM = calendar.getTime();

        calendar.add(Calendar.DATE, -1);
        Date ayerM = calendar.getTime();

        calendar.add(Calendar.DATE, -1);
        Date antesDeAyerM = calendar.getTime();

        calendar.add(Calendar.DATE, 3);
        Date mananaM = calendar.getTime();

        calendar.add(Calendar.DATE, 1);
        Date pasadoMananaM = calendar.getTime();


        dias.add(sdf.format(antesDeAyerM));
        dias.add(sdf.format(ayerM));
        dias.add(sdf.format(hoyM));
        dias.add(sdf.format(mananaM));
        dias.add(sdf.format(pasadoMananaM));

        return dias;

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



    private void cargarDia(LatLng latLng, String fecha, final LinearLayout linearLayout) {

        weatherWebServiceURL = "https://api.darksky.net/forecast/8633461193a3a899b847a39b3b9a9fa4/" + latLng.latitude + ","
                                                                                                    + latLng.longitude + ","
                                                                                                    + fecha + ", "
                                                                                                    + "exclude=currently,flags,minutely,daily,alerts";

        JsonObjectRequest jsonObjRequest = new JsonObjectRequest(Request.Method.GET,
                weatherWebServiceURL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    JSONObject hora = (JSONObject) response.get("hourly");
                    JSONObject data = (JSONObject) ((JSONArray) hora.get("data")).get(0);

                    TextView textView = new TextView(getApplicationContext());
                    ViewGroup.LayoutParams layoutparams = new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    textView.setText(String.valueOf(data.get("temperature")));
                    textView.setLayoutParams(layoutparams);
                    textView.setVisibility(View.VISIBLE);
                    linearLayout.addView(textView);

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
                Toast.makeText(getApplicationContext(), "Error while loading day", Toast.LENGTH_SHORT).show();
                //Ocultar la barra de progreso
                pBar.setVisibility(View.INVISIBLE);
            }
        });

        //Agregar petición a la cola de petición
        AppController.getInstance(this).addToRequestQueue(jsonObjRequest);
    }
}
