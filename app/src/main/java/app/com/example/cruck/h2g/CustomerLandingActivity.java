package app.com.example.cruck.h2g;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class CustomerLandingActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = CustomerLandingActivity.class.getSimpleName();
    private Firebase mRef;
    private String mUserId;
    private String itemsUrl;
    private TextView textView;
    private NumberPicker highwayNumbPicker;
    private Button searchButton;
    private ListView categoriesListView;
    private ArrayList<String> intentSending;
    private Item weather_data[];
    protected String latitudeSS;
    protected String longitudeSS;
    final private int PERMISSION_ACCESS_COARSE_LOCATION = 123;
    private GoogleApiClient mGoogleApiClient;
    private String url;
    private String distanceInfo;
    private ProgressDialog pDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_landing);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { android.Manifest.permission.ACCESS_COARSE_LOCATION },
                    PERMISSION_ACCESS_COARSE_LOCATION);
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


        textView = (TextView) findViewById(R.id.welcome1);
        searchButton = (Button) findViewById(R.id.search1);
        categoriesListView = (ListView) findViewById(R.id.listhigh);

        mRef = new Firebase(Config.FIREBASE_URL);
        try {
            mUserId = mRef.getAuth().getUid();
        } catch (Exception e) {
            loadLoginView();
        }

        intentSending = new ArrayList<String>();
        highwayNumbPicker = (NumberPicker) findViewById(R.id.highwayNum);
        highwayNumbPicker.setMinValue(1);
        highwayNumbPicker.setMaxValue(100);
        highwayNumbPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        highwayNumbPicker.setWrapSelectorWheel(false);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
//        categoriesListView.setAdapter(adapter);


        weather_data = new Item[] {};
        categoriesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // selected item
//                String product = ((TextView) view).getText().toString();
                if(weather_data.length == 0) System.out.println("IT IS EMPTY");
                else {
                    Intent i = new Intent(getApplicationContext(), Rating.class);
                    // sending data to new activity
                    i.putExtra("key", intentSending.get((int)id));
                    startActivity(i);
                }
                // Launching new Activity on selecting single List Item
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoriesListView.setAdapter(adapter);
                adapter.clear();
                intentSending.clear();
                String highwaysUrl = Config.FIREBASE_URL + "/services";
                Firebase highwaySearch = new Firebase(highwaysUrl);
                final String highwayNumber = Integer.toString(highwayNumbPicker.getValue());

                highwaySearch.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        //System.out.println("There are " + snapshot.getChildrenCount() + " blog posts");
                        ArrayList<Item> arr = new ArrayList<Item>();
                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {

                            DataService post = postSnapshot.getValue(DataService.class);
                            if (post.getHighwayNumber().equals(highwayNumber)) {
                                intentSending.add(postSnapshot.getKey().toString());
//                                arr.add(new (post.getCategory(), post.getServiceName()));

                                //Add the location distance Code here
                                distanceInfo = "N/A";

                                if(post.getLatitude() != null && latitudeSS != null) {
                                    url="http://maps.google.com/maps/api/distancematrix/json?origins="+post.getLatitude()
                                    +","+post.getLongitude()+"&destinations="+latitudeSS+","+longitudeSS
                                            +"&units=metric"; //&key=AIzaSyD__QgpE4k6Y5A2s5rqEKahul1Avf9mBWQ";
                                    distanceInfo = new GetdistancefromJSON().getDistance(url);
                                    Log.d(TAG, "Distance is "+distanceInfo);
                                }
                                distanceInfo = "3 kms";

                                //End of distance Code

                                if(post.getCategory().equals("Hospitals")) arr.add(new Item(R.mipmap.hospital, post.getServiceName(), distanceInfo));
                                else if(post.getCategory().equals("Washrooms")) arr.add(new Item(R.mipmap.washroom, post.getServiceName(), distanceInfo));
                                else if(post.getCategory().equals("Repairs")) arr.add(new Item(R.mipmap.repair, post.getServiceName(), distanceInfo));
                                else if(post.getCategory().equals("Restaurants")) arr.add(new Item(R.mipmap.food, post.getServiceName(), distanceInfo));
                                else if(post.getCategory().equals("Motels")) arr.add(new Item(R.mipmap.motel, post.getServiceName(), distanceInfo));
                                adapter.add("Service Name: " + post.getServiceName() + "\n" + "Service Description: " + post.getServiceDescription() + "\n"+ distanceInfo);
                            }
                        }

                        //My Custom
//                        weather_data = new Item[]
//                                {new Item(R.mipmap.log, post.getServiceName())};
                        weather_data = arr.toArray(new Item[arr.size()]);
                        ItemAdapter adapter1 = new ItemAdapter(CustomerLandingActivity.this, R.layout.listview_item_row, weather_data);
                        categoriesListView.setAdapter(adapter1);
                        Log.d(TAG, "testing");
                        //My Custom

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
//                System.out.println(snapshot.getValue());
                textView.setText("Welcome " + snapshot.getValue().toString());
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CustomerLandingActivity.this);
                builder.setMessage(R.string.no_internet)
                        .setTitle(R.string.login_error_title)
                        .setPositiveButton(android.R.string.ok, null);
                AlertDialog dialog = builder.create();
                dialog.show();
                //System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });
    }

    private void loadLoginView() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }

    private Boolean exit = false;
    @Override
    public void onBackPressed() {
        if (exit) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//***Change Here***
            startActivity(intent);
            finish();
            System.exit(0);
           // finish activity
        } else {
            Toast.makeText(this, "Press Back again to Exit.",
                    Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            Toast.makeText(this, "You have been logged out",
                    Toast.LENGTH_LONG).show();
            mRef.unauth();
            loadLoginView();
//            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d("location failed", "we entered this shit");
        switch (requestCode) {
            case PERMISSION_ACCESS_COARSE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // All good!
                } else {
                    Toast.makeText(this, "Need your location!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("location", "we Connected!");
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            Log.d("location", "we are in!!!");
            latitudeSS = Double.toString(lastLocation.getLatitude());
            longitudeSS = Double.toString(lastLocation.getLongitude());
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {}

    @Override
    public void onConnectionSuspended(int i) {}

}
