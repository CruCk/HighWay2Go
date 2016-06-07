package app.com.example.cruck.h2g;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.HashMap;
import java.util.Map;

public class ServiceData extends AppCompatActivity {

    protected EditText usernameEditText;
    protected EditText fullnameEditText;
    protected EditText servicenameEditText;
    protected EditText servicedescriptionEditText;
    protected EditText servicephoneEditText;
    protected Spinner categorySpinner;
    protected NumberPicker highwayNum;
    protected Button createButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_data);

        usernameEditText = (EditText) findViewById(R.id.usernamefield);
        fullnameEditText = (EditText) findViewById(R.id.fullnamefield);
        servicenameEditText = (EditText) findViewById(R.id.servicename);
        servicedescriptionEditText = (EditText) findViewById(R.id.servicedescription);
        servicephoneEditText = (EditText) findViewById(R.id.servicephone);
        categorySpinner = (Spinner) findViewById(R.id.Spinner01);
        highwayNum = (NumberPicker) findViewById(R.id.numberPicker);
        createButton = (Button) findViewById(R.id.createbutton);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(ServiceData.this,
                R.array.categories_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        highwayNum.setMinValue(1);
        highwayNum.setMaxValue(100);
        highwayNum.setWrapSelectorWheel(false);

        final Firebase ref = new Firebase(Config.FIREBASE_URL);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = usernameEditText.getText().toString().trim();
                final String fullname = fullnameEditText.getText().toString().trim();
                final String servicename = servicenameEditText.getText().toString().trim();
                final String servicedescription = servicedescriptionEditText.getText().toString().trim();
                final String servicephone = servicephoneEditText.getText().toString().trim();
                final String category = categorySpinner.getSelectedItem().toString();
                final String highwayNumber = Integer.toString(highwayNum.getValue());
                if(username.isEmpty() || fullname.isEmpty() || servicename.isEmpty() || servicedescription.isEmpty() || servicephone.isEmpty() || category.isEmpty() || highwayNumber.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ServiceData.this);
                    builder.setMessage(R.string.empty_message)
                            .setTitle(R.string.signup_error_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();

                } else {
                    String email = getIntent().getExtras().getString("email");
                    String password = getIntent().getExtras().getString("password");

                    final String emailAddress = email;
                    ref.authWithPassword(email, password, new Firebase.AuthResultHandler() {
                        @Override
                        public void onAuthenticated(AuthData authData) {
                            // Authenticated successfully with payload authData
                            Map<String, Object> map = new HashMap<String, Object>();
                            map.put("email", emailAddress);
                            map.put("username", username);
                            map.put("name", fullname);
                            map.put("serviceName", servicename);
                            map.put("serviceDescription", servicedescription);
                            map.put("servicePhone", servicephone);
                            map.put("category", category);
                            map.put("highwayNumber", highwayNumber);
                            System.out.println(emailAddress+" "+username+" "+fullname+" "+servicename+" "+servicedescription+" "+servicephone+" "+category+" "+highwayNumber);
                            ref.child("services").child(authData.getUid()).setValue(map);

                            Intent intent = new Intent(ServiceData.this, ServiceLandingActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }

                        @Override
                        public void onAuthenticationError(FirebaseError firebaseError) {
                            // Authenticated failed with error firebaseError
                            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ServiceData.this);
                            builder.setMessage(firebaseError.getMessage())
                                    .setTitle(R.string.login_error_title)
                                    .setPositiveButton(android.R.string.ok, null);
                            android.app.AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    });

                }
            }
        });
    }
}

