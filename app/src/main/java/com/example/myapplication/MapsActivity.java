package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;

import com.google.android.gms.common.api.GoogleApiActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback { //Callback is bcus it takes time to get the data from google

    private GoogleMap mMap;
    private String coordinates = "coordinates";

    FirebaseFirestore db = FirebaseFirestore.getInstance(); //Instance of firebasefirestore


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this); //waits asyncro. to ge the map, this class handles map
    }

    private void setNewMarker(final Context c) { //To be called in onMapReady

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() { //Long click map listener
            @Override
            public void onMapLongClick(final LatLng latLng) {
                final MarkerOptions markerOptions = new MarkerOptions(); //Creates marker
                markerOptions.position(latLng); //Sets pos for marker
                final EditText taskEditText = new EditText(c); //Edittext with c, context
                AlertDialog dialog = new AlertDialog.Builder(c) //Dialog builder
                        .setTitle("Enter a name for your mark!")
                        .setView(taskEditText)
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) { //What to do if success
                                Map<String, Object> location = new HashMap<>();
                                String title = String.valueOf(taskEditText.getText());
                                GeoPoint geoPoint = new GeoPoint(latLng.latitude,latLng.longitude);
                                location.put("title",title);
                                location.put("position",geoPoint);
                                markerOptions.title(title);
                                mMap.addMarker(markerOptions);
                                db.collection(coordinates).document().set(location);
                            }
                        })
                        .setNegativeButton("Cancel", null) //Negative button
                        .create(); //Creates dialog
                dialog.show(); // show dialog
            }
        });
    }

    private void readCoordinates() {
        db.collection(coordinates)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            String title = (String) documentSnapshot.get("title");
                            GeoPoint geoPoint = documentSnapshot.getGeoPoint("position");
                            LatLng latLng = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                            mMap.addMarker(new MarkerOptions().position(latLng).title(title));
                            //LatLng home = new LatLng();
                            //mMap.addMarker(new MarkerOptions().position(home).title("Mi casa"));


                            //mMap.addMarker(new MarkerOptions().position(latLng).title(title));
                        }
                    }
                });
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
    public void onMapReady(GoogleMap googleMap) { //Handles the map, called when map gets loaded
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151); //set coords
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney")); //add marker
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney)); //auto move map to marker

        //Home
        LatLng home = new LatLng(55.701381, 12.525205);
        mMap.addMarker(new MarkerOptions().position(home).title("Mi casa"));

        setNewMarker(MapsActivity.this); //Calls the set new marker method with this class as context
        readCoordinates();
    }

}
