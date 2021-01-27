package com.example.pataqazi;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;



import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
//import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EmployerMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private Button mLogout, mRequest, mSettings, mHistory;
    //private Button pay;
    // private LinearLayout payLL;
    private LatLng pickupLocation;
    private Boolean requestBol = false;
    private Marker pickupMarker;
    private SupportMapFragment mapFragment;
    private String destination, requestService;
    private LatLng destinationLatLng;
    private LinearLayout mprofessionalInfo;
    private ImageView mprofessionalProfileImage;
    private TextView mprofessionalName, mprofessionalPhone, mSkill;
    private RadioGroup mRadioGroup;
    private RatingBar mRatingBar;
    private static int AUTOCOMPLETE_REQUEST_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employer_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        destinationLatLng = new LatLng(0.0, 0.0);
        mprofessionalInfo = findViewById(R.id.professionaInfo);
        mprofessionalProfileImage = findViewById(R.id.professionalProfileImage);
        mprofessionalName = findViewById(R.id.professionalname);
        mprofessionalPhone = findViewById(R.id.professionalPhone);
        mSkill = findViewById(R.id.Skill);
        mRatingBar = findViewById(R.id.ratingBar);
        mRadioGroup = findViewById(R.id.radioGroup);
        mRadioGroup.check(R.id.maintenance);
        mLogout = findViewById(R.id.logout);
        mRequest = findViewById(R.id.request);
        mSettings = findViewById(R.id.settings);
        mHistory = findViewById(R.id.history);
        //pay = findViewById(R.id.pay2BT);
        //payLL = findViewById(R.id.payLL);

        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(EmployerMapActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });


        mRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (requestBol) {
                    endRide();
                } else {
                    int selectId = mRadioGroup.getCheckedRadioButtonId();
                    final RadioButton radioButton = findViewById(selectId);
                    if (radioButton.getText() == null) {
                        return;
                    }
                    requestService = radioButton.getText().toString();
                    requestBol = true;
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("employerRequest");
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.setLocation(userId, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            Toast.makeText(EmployerMapActivity.this, "Completed", Toast.LENGTH_SHORT).show();
                        }
                    });

                    pickupLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLocation).title("professional Here").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_skills_foreground)));
                    mRequest.setText("Getting your professional....");
                    mprofessionalProfileImage.setImageResource(R.mipmap.ic_download);
                    getClosestprofessional();
                }
            }
        });
        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmployerMapActivity.this, EmployerSettingActivity.class);
                startActivity(intent);
                return;
            }
        });

        mHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmployerMapActivity.this, HistoryActivity.class);
                intent.putExtra("employerOrprofessional", "employer");
                startActivity(intent);
                return;
            }
        });


        // TODO: Handle the error.
//            }
//        });

        if (!Places.isInitialized()) {
            // Places.initialize(getApplicationContext(), "AIzaSyC17biH5r44WIRZSybGGj0L0qAlJ46sCK4", Locale.Kenya);
            // Initialize Places.
            Places.initialize(getApplicationContext(), "AIzaSyC17biH5r44WIRZSybGGj0L0qAlJ46sCK4");
        }


        // Set the fields to specify which types of place data to return.
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME);

        // Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields)
                .build(this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));
//        autocompleteFragment.setLocationRestriction(RectangularBounds.newInstance(
//                new LatLng( -1.292100, 36.821900),
//                new LatLng( -1.292100, 36.821900)));
        autocompleteFragment.setTypeFilter(TypeFilter.ADDRESS);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                destination = place.getName();
                destinationLatLng = place.getLatLng();
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.i(String.valueOf(EmployerMapActivity.this), "An error occurred: " + status);
            }
        });

        // Set the fields to specify which types of place data to
        // return after the user has made a selection.

    }

    private int radius = 1;
    private Boolean professionalFound = false;
    private String professionalFoundID;
    GeoQuery geoQuery;

    private void getClosestprofessional() {
        DatabaseReference professionalLocation = FirebaseDatabase.getInstance().getReference().child("professionalAvailable");
        final GeoFire geoFire = new GeoFire(professionalLocation);

        professionalLocation.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    geoQuery = geoFire.queryAtLocation(new GeoLocation(pickupLocation.latitude, pickupLocation.longitude), radius);
                    geoQuery.removeAllListeners();
                    geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                        @Override
                        public void onKeyEntered(String key, GeoLocation location) {
                            if (!professionalFound && requestBol) {
                                DatabaseReference mEmployerDatabase = FirebaseDatabase.getInstance().getReference().child("users").child("professional").child(key);
                                mEmployerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                                            Map<String, Object> professionalMap = (Map<String, Object>) dataSnapshot.getValue();
                                            if (professionalFound) {
                                                return;
                                            }
//                                            rescue<'servuce', 'ertyu'>
                                            if(Objects.requireNonNull(professionalMap).get("service").equals(requestService)){
                                                professionalFound = true;
                                                professionalFoundID = dataSnapshot.getKey();
                                                DatabaseReference professionalRef = FirebaseDatabase.getInstance().getReference().child("users").child("professional").child(professionalFoundID).child("EmployerRequest");
                                                String employerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                                HashMap map = new HashMap();
                                                map.put("employerRideId", employerId);
                                                map.put("destination", destination);
                                                map.put("destinationLat", destinationLatLng.latitude);
                                                map.put("destinationLng", destinationLatLng.longitude);
                                                professionalRef.updateChildren(map);

                                                getprofessionalLocation();
                                                getprofessionalInfo();
                                                getHasRideEnded();
                                                mRequest.setText("Looking for professional Location....");
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    }
                                });
                            }
                        }

                        @Override
                        public void onKeyExited(String key) {

                        }

                        @Override
                        public void onKeyMoved(String key, GeoLocation location) {

                        }

                        @Override
                        public void onGeoQueryReady() {
                            if (!professionalFound) {
                                radius++;
                                getClosestprofessional();
                            }
                        }

                        @Override
                        public void onGeoQueryError(DatabaseError error) {

                        }
                    });

                } else {
                    Toast.makeText(EmployerMapActivity.this, "No professional found ", Toast.LENGTH_SHORT).show();
                    requestBol = false;
                    pickupLocation = null;
                    pickupMarker = null;

                    mRequest.setText("No professional....Try again");

                    //mRequest.setText("Try again....");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /*-------------------------------------------- Map specific functions -----*/

    private Marker mprofessionalMarker;
    private DatabaseReference professionalLocationRef;
    private ValueEventListener professionalLocationRefListener;

    private void getprofessionalLocation() {
        professionalLocationRef = FirebaseDatabase.getInstance().getReference().child("professionalWorking").child(professionalFoundID).child("l");
        professionalLocationRefListener = professionalLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && requestBol) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if (map.get(0) != null) {
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) != null) {
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng professionalLatLng = new LatLng(locationLat, locationLng);
                    if (mprofessionalMarker != null) {
                        mprofessionalMarker.remove();
                    }
                    Location loc1 = new Location("");
                    loc1.setLatitude(pickupLocation.latitude);
                    loc1.setLongitude(pickupLocation.longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(professionalLatLng.latitude);
                    loc2.setLongitude(professionalLatLng.longitude);

                    float distance = loc1.distanceTo(loc2);

                    if (distance < 100) {
                        mRequest.setText("professional's Here");
                    } else {
                        mRequest.setText("professional Found: " + distance);
                    }


                    mprofessionalMarker = mMap.addMarker(new MarkerOptions().position(professionalLatLng).title("your professional").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_skills)));
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }


    private void getprofessionalInfo() {
        mprofessionalInfo.setVisibility(View.VISIBLE);
        DatabaseReference mEmployerDatabase = FirebaseDatabase.getInstance().getReference().child("users").child("professional").child(professionalFoundID);
        mEmployerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    if (dataSnapshot.child("name") != null) {
                        mprofessionalName.setText(dataSnapshot.child("name").getValue().toString());
                    }
                    if (dataSnapshot.child("phone") != null) {
                        mprofessionalPhone.setText(dataSnapshot.child("phone").getValue().toString());
                    }
                    if (dataSnapshot.child("professionalSkills") != null) {
                        mSkill.setText(dataSnapshot.child("Skills").getValue().toString());
                    }
                    if (dataSnapshot.child("profileImageUrl").getValue() != null) {
                        Glide.with(getApplication()).load(dataSnapshot.child("profileImageUrl").getValue().toString()).into(mprofessionalProfileImage);
                    }

                    int ratingSum = 0;
                    float ratingsTotal = 0;
                    float ratingsAvg = 0;
                    for (DataSnapshot child : dataSnapshot.child("rating").getChildren()) {
                        ratingSum = ratingSum + Integer.valueOf(child.getValue().toString());
                        ratingsTotal++;
                    }
                    if (ratingsTotal != 0) {
                        ratingsAvg = ratingSum / ratingsTotal;
                        mRatingBar.setRating(ratingsAvg);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private DatabaseReference professionalHasEndedRef;
    private ValueEventListener professionalHasEndedRefListener;

    private void getHasRideEnded() {

        professionalHasEndedRef = FirebaseDatabase.getInstance().getReference().child("users").child("professional").child(professionalFoundID).child("employerRequest").child("employerRideId");
        professionalHasEndedRefListener = professionalHasEndedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                } else {

                    ArrivedInHospital();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void ArrivedInHospital() {


        requestBol = false;
        //geoQuery.removeAllListeners();
        professionalLocationRef.removeEventListener(professionalLocationRefListener);
        professionalHasEndedRef.removeEventListener(professionalHasEndedRefListener);


        professionalFound = false;
        radius = 1;
        final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("employerRequest");
        final GeoFire geoFire = new GeoFire(ref);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    geoFire.removeLocation(userId, new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            Toast.makeText(EmployerMapActivity.this, "successfully removed " + userId, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (pickupMarker != null) {
            pickupMarker.remove();
        }
        if (mprofessionalMarker != null) {
            mprofessionalMarker.remove();
        }
        mRequest.setText("Request  professional");
        mprofessionalInfo.setVisibility(View.GONE);
        mprofessionalName.setText("");
        mprofessionalPhone.setText("");
        mSkill.setText("Destination: --");
        mprofessionalProfileImage.setImageResource(R.mipmap.ic_download);



        if (professionalFoundID != null) {
            DatabaseReference professionalRef = FirebaseDatabase.getInstance().getReference().child("users").child("professional").child(professionalFoundID).child("employerRequest");
            professionalRef.removeValue();
            professionalFoundID = null;

        }

    }


    private void endRide() {
        requestBol = false;
        // geoQuery.removeAllListeners();
        professionalLocationRef.removeEventListener(professionalLocationRefListener);
        professionalHasEndedRef.removeEventListener(professionalHasEndedRefListener);

        if (professionalFoundID != null) {
            DatabaseReference professionalRef = FirebaseDatabase.getInstance().getReference().child("users").child("professional").child(professionalFoundID).child("employerRequest");
            professionalRef.removeValue();
            professionalFoundID = null;

        }
        professionalFound = false;
        radius = 1;
        final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("employerRequest");
        final GeoFire geoFire = new GeoFire(ref);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    geoFire.removeLocation(userId, new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            Toast.makeText(EmployerMapActivity.this, "successfully removed " + userId, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (pickupMarker != null) {
            pickupMarker.remove();
        }
        if (mprofessionalMarker != null) {
            mprofessionalMarker.remove();
        }
        mRequest.setText("Request  professional");
        mprofessionalInfo.setVisibility(View.GONE);
        mprofessionalName.setText("");
        mprofessionalPhone.setText("");
        mSkill.setText("Destination: --");
        mprofessionalProfileImage.setImageResource(R.mipmap.ic_download);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback,
                        Looper.myLooper());
                googleMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        } else {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback,
                    Looper.myLooper());
            googleMap.setMyLocationEnabled(true);
        }
    }



    LocationCallback mLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for(Location location : locationResult.getLocations()){
                if(getApplicationContext()!=null){
                    mLastLocation = location;
                    LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(16));

                    if(!getprofessionalAroundStarted)
                        getprofessionalAround();
                }
            }
        }
    };

    private void checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                new android.app.AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("give permission message")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(EmployerMapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            }
            else{
                ActivityCompat.requestPermissions(EmployerMapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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

    boolean getprofessionalAroundStarted = false;
    List<Marker> markers = new ArrayList<Marker>();
    private void getprofessionalAround(){
        getprofessionalAroundStarted = true;
        DatabaseReference professionalLocation = FirebaseDatabase.getInstance().getReference().child("professionalAvailable");
        GeoFire geoFire = new GeoFire(professionalLocation);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(mLastLocation.getLongitude(), mLastLocation.getLatitude()), 999999999);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                for(Marker markerIt : markers){
                    if(markerIt.getTag().equals(key))
                        return;
                }

                LatLng professionalLocation = new LatLng(location.latitude, location.longitude);

                Marker mprofessionalMarker = mMap.addMarker(new MarkerOptions().position(professionalLocation).title(key).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_skills_foreground)));
                mprofessionalMarker.setTag(key);

                markers.add(mprofessionalMarker);

            }

            @Override
            public void onKeyExited(String key) {
                for(Marker markerIt : markers){
                    if(markerIt.getTag().equals(key)){
                        markerIt.remove();
                    }
                }
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                for(Marker markerIt : markers){
                    if(markerIt.getTag().equals(key)){
                        markerIt.setPosition(new LatLng(location.latitude, location.longitude));
                    }
                }
            }

            @Override
            public void onGeoQueryReady() {
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }


}