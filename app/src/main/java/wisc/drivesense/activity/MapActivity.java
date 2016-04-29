package wisc.drivesense.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.LevelListDrawable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import wisc.drivesense.R;
import wisc.drivesense.utility.Trace;
import wisc.drivesense.utility.Trip;

public class MapActivity extends Activity implements OnMapReadyCallback {

    static final LatLng madison_ = new LatLng(43.073052 , -89.401230);
    //private GoogleMap map;
    private Trip trip_;
    private static String TAG = "MapActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent intent = getIntent();
        trip_ = (Trip) intent.getSerializableExtra("Current Trip");
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
        ratingView.setText(String.format("%.1f", trip_.getScore()));

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

        LatLng start;
        int sz = trip_.getGPSPoints().size();
        if(sz >= 2) {
            start = new LatLng(trip_.getStartPoint().values[0], trip_.getStartPoint().values[1]);
        } else {
            start = madison_;
        }
        CameraPosition position = CameraPosition.builder()
                .target(start)
                .zoom( 15f )
                .bearing( 0.0f )
                .tilt( 0.0f )
                .build();

        map.moveCamera(CameraUpdateFactory.newCameraPosition(position));

        if(sz >= 2) {
            plotRoute(map);
        }
    }


    private void plotRoute(final GoogleMap map) {

        List<Trace> gps = trip_.getGPSPoints();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();


        Bitmap bmp = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);

        Paint paint = new Paint();
        paint.setColor(0xFFFF0000);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(4.5f);
        canvas.drawCircle(50, 50, 30, paint);



        for (int i = 0; i < gps.size(); i++) {
            Trace point = gps.get(i);
            //.icon(BitmapDescriptorFactory.fromResource(R.drawable.buttondefault)
            MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(point.values[0], point.values[1])).icon(BitmapDescriptorFactory.fromBitmap(bmp));
            Marker marker = map.addMarker(markerOptions);
            builder.include(marker.getPosition());
        }
        final LatLngBounds bounds = builder.build();
        map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            public void onMapLoaded() {
                int padding = 100;
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
            }
        });
    }
    protected void onDestroy() {
        super.onDestroy();
    }
}
