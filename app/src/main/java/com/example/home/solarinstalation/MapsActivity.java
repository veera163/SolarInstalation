package com.example.home.solarinstalation;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.home.solarinstalation.Api.DlocationApi;
import com.example.home.solarinstalation.Api.LocationApi;
import com.example.home.solarinstalation.Model.LocationRes;
import com.example.home.solarinstalation.Modules.DirectionFinder;
import com.example.home.solarinstalation.Modules.DirectionFinderListener;
import com.example.home.solarinstalation.Modules.Route;
import com.example.home.solarinstalation.activity.Install;
import com.example.home.solarinstalation.activity.Repair;
import com.example.home.solarinstalation.activity.Signature;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Locale;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener,DirectionFinderListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    public GoogleApiClient mGoogleApiClient;
    double clat=0,clng,lat,log;
    Geocoder geocoder;
    List<Address> addresses;
    LatLng latLng;
    Marker mMarker;
    LatLng dlatlan;
    String url;
    String id,tab,trip,usetloc;
    Button start,end;
    LocationApi locApi;
    List<LocationRes> locRes;
    GPSTracker gps;
    String source,des,origin,dlatlng,type="insatll";
    String android_id,desloc;
    Button now;
    AlertDialog alertDialogBuilder;
    Button bt_install,bt_repair,bt_sign;
    String[] latlong;
    TextView veera;
    TextView tvdis, tvdur;
    String curlat,curlng;
    String add;
    String schoolid;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        start=(Button)findViewById(R.id.start);
        tvdur = (TextView) findViewById(R.id.tvDuration);
        tvdis = (TextView) findViewById(R.id.tvDistance);
        veera=(TextView)findViewById(R.id.veera);
        android_id = Settings.Secure.getString(MapsActivity.this.getContentResolver(), Settings.Secure.ANDROID_ID);
        mapFragment.getMapAsync(this);
        gps=new GPSTracker(MapsActivity.this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (ContextCompat.checkSelfPermission(MapsActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                //Location Permission already granted
                buildGoogleApiClient();


            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        }
        else {

            buildGoogleApiClient();

        }
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(clat==0){

                    curlat=String.valueOf(clat);
                    curlng=String.valueOf(clng);
                    addtrack();
                }
                else {
                    curlat=String.valueOf(gps.getLatitude());
                    curlng=String.valueOf(gps.getLongitude());
                    addtrack();
                }
                Thread t = new Thread() {
                    @Override
                    public void run() {
                        while(true) {
                            try {
                                Thread.sleep(1000*60*30);
                                dlatlng=String.valueOf(clat)+","+String.valueOf(clng);
                                ClientLoc(dlatlng,type,android_id);
                                //your code here...
                            } catch (InterruptedException ie) {
                            }
                        }
                    }
                };
                t.start();
            }
        });

    }

    private void ClientLoc(String dlatlng, String type, String android_id) {

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Interceptor.Chain chain) throws IOException {
                Request original = chain.request();
                Request request = original.newBuilder()
                        .header("content-type", "application/json")
                        .method(original.method(), original.body())
                        .build();
                return chain.proceed(request);

            }
        });
        OkHttpClient client = httpClient.build();
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AppController.BaseUrl)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();
        DlocationApi apiInterface = retrofit.create(DlocationApi.class);
        try {
            JSONObject paramObject = new JSONObject();
            paramObject.put("latlong",dlatlng);
            paramObject.put("type",type);
            paramObject.put("deviceId",android_id);
            Call<LocationRes> userCall = apiInterface.getUser(paramObject.toString());
            userCall.enqueue(new Callback<LocationRes>() {
                @Override
                public void onResponse(Call<LocationRes> call, Response<LocationRes> response) {

                    if(response.isSuccessful()){

                      //  Log.e("veera",dlatlng);

                    }
                }
                @Override
                public void onFailure(Call<LocationRes> call, Throwable t) {


                    // Toast.makeText(MapsActivity.this,t.getMessage(),Toast.LENGTH_LONG).show();

                }
            });
        } catch (JSONException e) {

            e.printStackTrace();
        }

    }

    private void addtrack() {

        String url="http://34.214.70.124//location/findByLocation/lat/"+curlat+"/lng/"+curlng;
        Log.e("url",url);
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AppController.BaseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        progressDialog = ProgressDialog.show(MapsActivity.this, "Please wait.",
                "Uploading Data..!", true);
        final LocationApi locApi = retrofit.create(LocationApi.class);
        Call<List<LocationRes>> listCall= locApi.getData(url);
        listCall.enqueue(new Callback<List<LocationRes>>() {
            @Override
            public void onResponse(Call<List<LocationRes>> call, Response<List<LocationRes>> response) {
                if(response.isSuccessful()){
                    progressDialog.dismiss();
                    locRes=response.body();
                    for(int i = 0 ; i < locRes.size() ; i++ ) {
                        latlong = locRes.get(i).getLatLng().split(",");
                        lat = Double.parseDouble(latlong[0]);
                        log = Double.parseDouble(latlong[1]);
                        schoolid=locRes.get(i).getSchoolCode();
                        usetloc=String.valueOf(gps.getLatitude())+","+String.valueOf(gps.getLongitude());
                        des=String.valueOf(lat)+","+String.valueOf(log);
                        geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
                        try {
                            addresses = geocoder.getFromLocation(lat,log, 1);
                            add=addresses.get(0).getAddressLine(0);
                            // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                        } catch (IOException e) {

                            e.printStackTrace();
                        }
                        // sendrequest(usetloc,des);
                        createMarker(lat,log,des,schoolid);


                    }
                }

            }

            @Override
            public void onFailure(Call<List<LocationRes>> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(MapsActivity.this,t.getMessage(),Toast.LENGTH_LONG).show();

            }
        });


    }

    private Marker createMarker(double lat, double log, String code, String schoolCode) {

        return mMarker= mMap.addMarker(new MarkerOptions()
                .position(new LatLng(lat, log))
                .title(code)
                .snippet(schoolCode)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.source)));


    }
    private synchronized void buildGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(MapsActivity.this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
        gps=new GPSTracker(MapsActivity.this);
        if(gps.canGetLocation()){
            geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
            try {
                addresses = geocoder.getFromLocation(gps.getLatitude(),gps.getLongitude(), 1);
                source=addresses.get(0).getAddressLine(0);
                // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            } catch (IOException e) {
                e.printStackTrace();
            }
            //lat.setText(String.valueOf(location.getLatitude()+"_"+location.getLongitude()));
        }
        else {
            gps.showSettingsAlert();
        }

    }

    private LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale((this),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MapsActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION );
                            }
                        })
                        .create()
                        .show();
                gps=new GPSTracker(MapsActivity.this);
                if(gps.canGetLocation()){

                    geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
                    try {
                        addresses = geocoder.getFromLocation(gps.getLatitude(),gps.getLongitude(), 1);
                        source=addresses.get(0).getAddressLine(0);
                        // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //lat.setText(String.valueOf(location.getLatitude()+"_"+location.getLongitude()));
                }
                else {

                    gps.showSettingsAlert();
                }

            }

            else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(MapsActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(MapsActivity.this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMarkerClickListener(this);

        // Add a marker in Sydney and move the camera
        latLng = new LatLng(gps.latitude,gps.longitude);
        // sendrequest(usetloc,des);
        // mMap.addMarker(new MarkerOptions().position(latLng).title(source));
        CameraPosition position = new CameraPosition.Builder()
                .target(latLng) // Sets the new camera position
                .zoom(8) // Sets the zoom
                .bearing(0) // Rotate the camera
                .tilt(30) // Set the camera tilt
                .build(); // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(position));
        mMap.setPadding(0, 320, 0, 0);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);

    }

    @Override
    public void onDirectionFinderStart() {

    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {

        for (Route route : routes) {
            //  mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));
            int distance=route.distance.value;
            tvdur.setText(route.duration.text);
            tvdis.setText(route.distance.text);
            veera.setText(route.endAddress);

            if(distance<=1000){

                showInputDialog();

            }

            Log.e("value", route.distance.text);
        }
    }

    private void showInputDialog() {
        LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());
        View promptView = layoutInflater.inflate(R.layout.proparties, null);
        alertDialogBuilder = new AlertDialog.Builder(this).create();
        alertDialogBuilder.setView(promptView);
        alertDialogBuilder.show();
        bt_install=(Button)promptView.findViewById(R.id.install);
        bt_repair=(Button)promptView.findViewById(R.id.repair);
        bt_sign=(Button)promptView.findViewById(R.id.sign);
        bt_install.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i= new Intent(MapsActivity.this,Install.class);
                i.putExtra("id",id);
                i.putExtra("deviceId",android_id);
                startActivity(i);
            }
        });
        bt_repair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i= new Intent(MapsActivity.this,Repair.class);
                i.putExtra("id",id);
                i.putExtra("deviceId",android_id);
                startActivity(i);
            }
        });
        bt_sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i= new Intent(MapsActivity.this, Signature.class);
                i.putExtra("id",id);
                i.putExtra("deviceId",android_id);
                startActivity(i);
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        LocationRequest mLocationRequest = createLocationRequest();
        if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        clat=location.getLatitude();
        clng=location.getLongitude();
    }



    @Override
    public boolean onMarkerClick(Marker marker) {
        desloc=marker.getTitle();
        id=marker.getSnippet();
        if(clat==0){
            usetloc=String.valueOf(gps.getLatitude())+","+String.valueOf(gps.getLongitude());
            sendrequest(usetloc,desloc);
        }
        else {
            usetloc=String.valueOf(clat)+","+String.valueOf(clng);
            sendrequest(usetloc,desloc);

        }

        Log.e("veera",des);

        return false;
    }

    private void sendrequest(String usetloc, String desloc) {

        try {
            new DirectionFinder((DirectionFinderListener) this, usetloc, desloc).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
