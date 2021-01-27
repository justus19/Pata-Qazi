package com.example.pataqazi;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import androidx.fragment.app.FragmentActivity;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Driver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfessionalMapActivity extends FragmentActivity implements OnMapReadyCallback, RoutingListener {
    private GoogleMap mMap;
    LocationRequest mLocationRequest;
    Location mLastLocation;
    private FusedLocationProviderClient mFusedLocationClient;
    private Button mLogout, mSettings, mRideStatus, mHistory;
    private Switch mWorkingSwitch;
    private int status = 0;
    private String employerId = "", destination;
    private LatLng destinationLatLng, pickupLatLng;
    private float rideDistance;
    private Boolean isLoggingOut = false;
    private SupportMapFragment mapFragment;

    private LinearLayout employerInfo;
    private FirebaseAuth mAuth;
    private ImageView memployerProfileImage;
    private TextView memployerName, memployerPhone, memployerDestination;
    private List<Polyline> polylines;
    Marker pickupMarker;
    private DatabaseReference assignedemployerPickupLocationRef;
    private ValueEventListener assignedemployerPickupLocationRefListener;
    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};
    private double wayLatitude = 0.0, wayLongitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professional_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        polylines = new ArrayList<>();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        employerInfo = findViewById(R.id.employerInfo);
        memployerProfileImage = findViewById(R.id.employerProfileImage);
        memployerName = findViewById(R.id.employerName);
        memployerPhone = findViewById(R.id.employerPhone);
        memployerDestination = findViewById(R.id.employerDestination);
        mWorkingSwitch = findViewById(R.id.workingSwitch);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        mWorkingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked){
                    connectprofessional();
                }else{
                    disconnectprofessional();
                }
            }
        });

        mSettings = findViewById(R.id.settings);
        mLogout = findViewById(R.id.logout);
        mRideStatus = findViewById(R.id.rideStatus);
        mHistory = findViewById(R.id.history);
        mRideStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(status){
                    case 1:
                        status=2;
                        erasePolylines();
                        if(destinationLatLng.latitude!=0.0 && destinationLatLng.longitude!=0.0){
                            getRouteToMarker(destinationLatLng);
                        }
                        mRideStatus.setText("drive completed");

                        break;
                    case 2:
                        recordRide();
                        endRide();
                        break;
                }
            }
        });

        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLoggingOut = true;
                disconnectprofessional();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(ProfessionalMapActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfessionalMapActivity.this, ProfessionalSettingActivity.class);
                startActivity(intent);
                return;
            }
        });
        mHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfessionalMapActivity.this, HistoryActivity.class);
                intent.putExtra("employerOrprofessional", "professional");
                startActivity(intent);
                return;
            }
        });
        getAssignedemployer();
    }
    LocationCallback  mLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Toast.makeText(ProfessionalMapActivity.this, "inside mLocationCallback ", Toast.LENGTH_SHORT).show();
            for(final Location location : locationResult.getLocations()){
                if(getApplicationContext()!=null){

                    if(!employerId.equals("") && mLastLocation!=null && location != null){
                        rideDistance += mLastLocation.distanceTo(location)/1000;
                    }
                    mLastLocation = location;
                    LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(21));

                    final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("professionalAvailable");
                    final DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("professionalWorking");
                    final GeoFire geoFireAvailable = new GeoFire(refAvailable);
                    final GeoFire geoFireWorking = new GeoFire(refWorking);

                    switch (employerId){
                        case "":
                            refWorking.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists()  ){
                                        geoFireWorking.removeLocation(userId, new GeoFire.CompletionListener() {
                                            @Override
                                            public void onComplete(String key, DatabaseError error) {
                                                Toast.makeText(ProfessionalMapActivity.this, "sucessfully removed "+ userId, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                            geoFireAvailable.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                                @Override
                                public void onComplete(String key, DatabaseError error) {
                                    Toast.makeText(ProfessionalMapActivity.this, "Completed", Toast.LENGTH_SHORT).show();
                                }
                            });
                            break;

                        default:
                            refAvailable.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists()){
                                        geoFireAvailable.removeLocation(userId, new GeoFire.CompletionListener() {
                                            @Override
                                            public void onComplete(String key, DatabaseError error) {
                                                Toast.makeText(ProfessionalMapActivity.this, "sucessfully removed "+ userId, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                            geoFireWorking.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                                @Override
                                public void onComplete(String key, DatabaseError error) {
                                    Toast.makeText(ProfessionalMapActivity.this, "Completed", Toast.LENGTH_SHORT).show();
                                }
                            });
                            break;
                    }
                }
            }
        }
    };
    private void getAssignedemployer(){
        Toast.makeText(ProfessionalMapActivity.this, "inside getAssignedemployer", Toast.LENGTH_SHORT).show();
        final String professionalId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedemployerRef = FirebaseDatabase.getInstance().getReference().child("users").child("professional").child(professionalId).child("employerRequest").child("employerRideId");
        assignedemployerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    status = 1;
                    employerId = dataSnapshot.getValue().toString();
                    getAssignedemployerPickupLocation();
                    getAssignedemployerDestination();
                    getAssignedemployerInfo();
                }else{
                    endRide();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getRouteToMarker(LatLng pickupLatLng) {
        if (pickupLatLng != null && mLastLocation != null){
            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(false)
                    .waypoints(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), pickupLatLng)
                    .build();
            routing.execute();
        }
    }

    private void getAssignedemployerPickupLocation(){
        Toast.makeText(ProfessionalMapActivity.this, "inside getAssignedemployerPickupLocation", Toast.LENGTH_SHORT).show();

        assignedemployerPickupLocationRef = FirebaseDatabase.getInstance().getReference().child("employerRequest").child(employerId).child("l");
        assignedemployerPickupLocationRefListener = assignedemployerPickupLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && !employerId.equals("")){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if(map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1) != null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    pickupLatLng = new LatLng(locationLat,locationLng);
                    pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLatLng).title("pickup location").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_professional)));
                    getRouteToMarker(pickupLatLng);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getAssignedemployerDestination(){
        Toast.makeText(ProfessionalMapActivity.this, "inside getAssignedDestination", Toast.LENGTH_SHORT).show();
        String professionalId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedemployerRef = FirebaseDatabase.getInstance().getReference().child("users").child("professional").child("professionalId").child("employerRequest");
        assignedemployerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("destination")!=null){
                        destination = map.get("destination").toString();
                        memployerDestination.setText("Destination: " + destination);
                    }
                    else{
                        memployerDestination.setText("Destination: --");
                    }

                    Double destinationLat = 0.0;
                    Double destinationLng = 0.0;
                    if(map.get("destinationLat") != null){
                        destinationLat = Double.valueOf(map.get("destinationLat").toString());
                    }
                    if(map.get("destinationLng") != null){
                        destinationLng = Double.valueOf(map.get("destinationLng").toString());
                        destinationLatLng = new LatLng(destinationLat, destinationLng);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getAssignedemployerInfo(){
        Toast.makeText(ProfessionalMapActivity.this, "inside getAssignedpatientInfo", Toast.LENGTH_SHORT).show();
        employerInfo.setVisibility(View.VISIBLE);
        DatabaseReference mEmployerDatabase = FirebaseDatabase.getInstance().getReference().child("users").child("employer").child(employerId);
        mEmployerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name")!=null){
                        memployerName.setText(map.get("name").toString());
                    }
                    if(map.get("phone")!=null){
                        memployerPhone.setText(map.get("phone").toString());
                    }
                    if(map.get("profileImageUrl")!=null){
                        Glide.with(getApplication()).load(map.get("profileImageUrl").toString()).into(memployerProfileImage);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    private void recordRide(){
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference professionalRef = FirebaseDatabase.getInstance().getReference().child("users").child("professional").child(userId).child("history");
        DatabaseReference employerRef = FirebaseDatabase.getInstance().getReference().child("users").child("employer").child(employerId).child("history");
        //DatabaseReference patientPayRef = FirebaseDatabase.getInstance().getReference().child("users").child("patient").child(patientId).child("latestRidePayment");
        DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference().child("history");
        String requestId = historyRef.push().getKey();
        professionalRef.child(requestId).setValue(true);
        employerRef.child(requestId).setValue(true);

        HashMap map = new HashMap();
        map.put("professional", userId);
        map.put("employer", employerId);
        map.put("rating", 0);
        map.put("timestamp", getCurrentTimestamp());
        map.put("destination", destination);
        map.put("location/from/lat", pickupLatLng.latitude);
        map.put("location/from/lng", pickupLatLng.longitude);
        map.put("location/to/lat", destinationLatLng.latitude);
        map.put("location/to/lng", destinationLatLng.longitude);
        map.put("distance", rideDistance);
        historyRef.child(requestId).updateChildren(map);


        HashMap map1 = new HashMap();
        map1.put("latestRideDBKEY", requestId);
        //patientPayRef.updateChildren(map1);

    }
    private Long getCurrentTimestamp() {
        Long timestamp = System.currentTimeMillis()/1000;
        return timestamp;
    }

    private void endRide(){
        Toast.makeText(ProfessionalMapActivity.this, "inside endRide", Toast.LENGTH_SHORT).show();
        mRideStatus.setText("picked employer");
        erasePolylines();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DatabaseReference professionalRef = FirebaseDatabase.getInstance().getReference().child("users").child("professional").child(userId).child("employerRequest");

        professionalRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.getChildrenCount()>0){
                    professionalRef.removeValue();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("employerRequest");
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.removeLocation(employerId);
                    employerId="";
                    rideDistance = 0;

                    if(pickupMarker != null){
                        pickupMarker.remove();
                    }
                    if (assignedemployerPickupLocationRefListener != null){
                        assignedemployerPickupLocationRef.removeEventListener(assignedemployerPickupLocationRefListener);
                    }
                    employerInfo.setVisibility(View.GONE);
                    memployerName.setText("");
                    memployerPhone.setText("");
                    memployerDestination.setText("Destination: --");
                    memployerProfileImage.setImageResource(R.mipmap.ic_launcher);
                }else{
                    Toast.makeText(ProfessionalMapActivity.this, "No Request found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void connectprofessional(){
        checkLocationPermission();
        Toast.makeText(ProfessionalMapActivity.this, "inside connectprofessional", Toast.LENGTH_SHORT).show();
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper() );
        mMap.setMyLocationEnabled(true);
    }
    private void disconnectprofessional(){
        if(mFusedLocationClient != null){
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            Toast.makeText(ProfessionalMapActivity.this, "inside disconnectprofessional", Toast.LENGTH_SHORT).show();
        }

        final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("professionalAvailable");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() ){
                    GeoFire geoFire = new GeoFire(ref);
                    //geoFire.removeLocation(userId);
                    geoFire.removeLocation(userId, new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            Toast.makeText(ProfessionalMapActivity.this, "suceessfully removed from available list "+ userId, Toast.LENGTH_SHORT).show();
                        }
                    });

                }else{
                    Toast.makeText(ProfessionalMapActivity.this, "professional not found in the records", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); //drain alot of battery

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
//                Toast.makeText(DriverMapActivity.this, "everything is good", Toast.LENGTH_SHORT).show();
//              mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
//              mMap.setMyLocationEnabled(true);
//               Toast.makeText(RescueMapsActivity.this, "inside requestLocationUpdates", Toast.LENGTH_SHORT).show();

            }else{
                checkLocationPermission();
            }
        }
    }

    @Override
    public void onRoutingFailure(RouteException e) {

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));


            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingCancelled() {

    }

    private void checkLocationPermission() {
        Toast.makeText(ProfessionalMapActivity.this, "inside checkLocationPermission", Toast.LENGTH_SHORT).show();
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("give permission message")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(ProfessionalMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            } else{
                ActivityCompat.requestPermissions(ProfessionalMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Toast.makeText(ProfessionalMapActivity.this, "inside onRequestPermissionsResult", Toast.LENGTH_SHORT).show();

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 1:{
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);


                    }
                } else{
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    private void erasePolylines(){
        for(Polyline line : polylines){
            line.remove();
        }
        polylines.clear();
    }

}