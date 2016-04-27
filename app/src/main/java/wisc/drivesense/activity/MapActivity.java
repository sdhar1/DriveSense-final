package wisc.drivesense.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import wisc.drivesense.R;
import wisc.drivesense.utility.Trace;
import wisc.drivesense.utility.Trip;

public class MapActivity extends Activity implements OnMapReadyCallback {

    static final LatLng madison = new LatLng(43.073052 , -89.401230);
    //private GoogleMap map;
    private Trip trip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent intent = getIntent();
        trip = (Trip) intent.getSerializableExtra("Current Trip");
        Toolbar ratingToolbar = (Toolbar) findViewById(R.id.tool_bar_rating);

        ratingToolbar.setTitle("Your Trip");
        ratingToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        ratingToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        TextView ratingView = (TextView) findViewById(R.id.rating);
        ratingView.setText(String.format("%.1f", trip.getScore()));

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    @Override
    public void onMapReady(GoogleMap map) {
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.setMyLocationEnabled(true);
        map.setTrafficEnabled(true);
        map.setIndoorEnabled(true);
        map.setBuildingsEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);

        LatLng destination;
        if(trip.getGPSPoints().size() >= 2) {
            destination = new LatLng(trip.getEndPoint().values[0], trip.getEndPoint().values[0]);
        } else {
            destination = madison;
        }
        CameraPosition position = CameraPosition.builder()
                .target(destination)
                .zoom( 15f )
                .bearing( 0.0f )
                .tilt( 0.0f )
                .build();

        map.moveCamera(CameraUpdateFactory.newCameraPosition(position));


        plotRoute(map);
    }


    private void plotRoute(GoogleMap map) {
        List<Trace> gps = trip.getGPSPoints();
        for (int i = 0; i < gps.size(); i++) {
            Trace point = gps.get(i);
            map.addMarker(new MarkerOptions().position(new LatLng(point.values[0], point.values[1])));
        }
    }

}
