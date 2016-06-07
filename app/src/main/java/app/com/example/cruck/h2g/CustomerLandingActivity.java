package app.com.example.cruck.h2g;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class CustomerLandingActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private Firebase mRef;
    private String mUserId;
    private String itemsUrl;
    private TextView textView;
    private NumberPicker highwayNumbPicker;
    private Button searchButton;
    private ListView categoriesListView;
    private TextView tvLatlong;

    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_landing);

        textView = (TextView) findViewById(R.id.welcome);
        searchButton = (Button) findViewById(R.id.search);
        categoriesListView = (ListView) findViewById(R.id.listView);
        tvLatlong = (TextView) findViewById(R.id.textView14);

        buildGoogleApiClient();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        } else
            Toast.makeText(this, "Not connected...", Toast.LENGTH_SHORT).show();


        mRef = new Firebase(Config.FIREBASE_URL);
        try {
            mUserId = mRef.getAuth().getUid();
        } catch (Exception e) {
            loadLoginView();
        }

        highwayNumbPicker = (NumberPicker) findViewById(R.id.highwayNum);
        highwayNumbPicker.setMinValue(1);
        highwayNumbPicker.setMaxValue(100);
        highwayNumbPicker.setWrapSelectorWheel(false);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
        categoriesListView.setAdapter(adapter);


        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.clear();
                String highwaysUrl = Config.FIREBASE_URL + "/services";
                Firebase highwaySearch = new Firebase(highwaysUrl);
                final String highwayNumber = Integer.toString(highwayNumbPicker.getValue());

                highwaySearch.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        System.out.println("There are " + snapshot.getChildrenCount() + " blog posts");
                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            DataService post = postSnapshot.getValue(DataService.class);
                            if (post.getHighwayNumber().equals(highwayNumber)) {
                                adapter.add("Service Name: " + post.getServiceName() + "\n" + "Service Description: " + post.getServiceDescription());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        System.out.println("The read failed: " + firebaseError.getMessage());
                    }
                });
            }
        });


        itemsUrl = Config.FIREBASE_URL + "/users/" + mUserId + "/username";
        Firebase ref = new Firebase(itemsUrl);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                System.out.println(snapshot.getValue());
                textView.setText("Welcome " + snapshot.getValue().toString());
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });


    }

    private void loadLoginView() {
        Intent intent = new Intent(this, LoginActivityCustomer.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        Toast.makeText(this, "Failed to connect...", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onConnected(Bundle arg0) {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        if (mLastLocation != null) {
            tvLatlong.setText("Latitude: "+ String.valueOf(mLastLocation.getLatitude())+"Longitude: "+
                    String.valueOf(mLastLocation.getLongitude()));
        }

    }

    @Override
    public void onConnectionSuspended(int arg0) {
        Toast.makeText(this, "Connection suspended...", Toast.LENGTH_SHORT).show();

    }

}
