package wisc.drivesense.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import wisc.drivesense.R;
import wisc.drivesense.database.DatabaseHelper;
import wisc.drivesense.fragment.AuthLandingFragment;
import wisc.drivesense.fragment.UserProfileFragment;
import wisc.drivesense.httpPayloads.LoginPayload;
import wisc.drivesense.httpPayloads.TokenLoginPayload;
import wisc.drivesense.uploader.GsonRequest;
import wisc.drivesense.uploader.RequestQueueSingleton;
import wisc.drivesense.utility.Constants;
import wisc.drivesense.utility.DriveSenseToken;

public class UserActivity extends AppCompatActivity {
    private final String TAG = "UserActivity";
    public CallbackManager callbackManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        final AppCompatActivity self = this;
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_user);

        initFacebook();


        reland();
    }

    public void reland() {
        DatabaseHelper dbH = new DatabaseHelper();
        if(dbH.getCurrentUser() != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.activity_fragment_content, UserProfileFragment.newInstance())
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.activity_fragment_content, AuthLandingFragment.newInstance())
                    .commit();
        }
        dbH.closeDatabase();

    }

    public void handleDrivesenseLogin(String driveSenseJWT) {
        DriveSenseToken dsToken = DriveSenseToken.InstantiateFromJWT(driveSenseJWT);
        DatabaseHelper dbH = new DatabaseHelper();
        if(dbH.hasUser(dsToken.email)) {
            dbH.userLogin(dsToken.email);
        } else {
            dbH.newUser(dsToken.email, dsToken.firstname, dsToken.lastname);
        }
        dbH.closeDatabase();

        this.reland();
    }

    public void initFacebook() {
        final AppCompatActivity self = this;
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager,
            new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    String fbtoken = loginResult.getAccessToken().getToken();
                    Log.d(TAG, "Got facebook token: "+fbtoken);
                    TokenLoginPayload tokenLogin = new TokenLoginPayload();
                    tokenLogin.access_token = fbtoken;
                    GsonRequest<LoginPayload> loginReq = new GsonRequest<LoginPayload>(Request.Method.POST, Constants.kFacebookSignInURL,
                            tokenLogin, LoginPayload.class,
                            new Response.Listener<LoginPayload>() {
                                @Override
                                public void onResponse(LoginPayload response) {
                                    // Display the first 500 characters of the response string.
                                    Log.d(TAG,"Got drivesense token: "+response.token);
                                    handleDrivesenseLogin(response.token);
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplicationContext(), "Facebook token authentication with Drivesense Failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                    RequestQueueSingleton.getInstance(getApplicationContext()).getRequestQueue().add(loginReq);
                }

                @Override
                public void onCancel() {
                    // App code
                }

                @Override
                public void onError(FacebookException exception) {
                    Toast.makeText(self, "Error during facebook login.", Toast.LENGTH_SHORT).show();
                }
            });
    }
}
