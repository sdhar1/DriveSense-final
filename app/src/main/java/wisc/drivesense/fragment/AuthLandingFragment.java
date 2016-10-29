package wisc.drivesense.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import wisc.drivesense.R;
import wisc.drivesense.httpPayloads.LoginPayload;
import wisc.drivesense.uploader.GsonRequest;
import wisc.drivesense.uploader.RequestQueueSingleton;
import wisc.drivesense.utility.Constants;
import wisc.drivesense.utility.DriveSenseToken;
import wisc.drivesense.utility.User;

public class AuthLandingFragment extends Fragment {
    private final String TAG = "AuthLandingFragment";
    private EditText mEmailText;
    private EditText mPasswordText;

    public static AuthLandingFragment newInstance() {
        return new AuthLandingFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_auth_landing, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mEmailText = (EditText) view.findViewById(R.id.email);
        mPasswordText = (EditText) view.findViewById(R.id.password);

        //for replacing with other fragment
        view.findViewById(R.id.sign_up_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.activity_fragment_content, SignupFragment.newInstance())
                        .addToBackStack(null)
                        .commit();
            }
        });

        view.findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInClicked(view);
            }
        });

    }

    // region Sign in
    /**
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     *
     *
     * */
    public void signInClicked(View v) {
        Log.d(TAG, "Sign in clicked");

        // Reset errors.
        mEmailText.setError(null);
        mPasswordText.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailText.getText().toString();
        String password = mPasswordText.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordText.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordText;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailText.setError(getString(R.string.error_field_required));
            focusView = mEmailText;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            attemptSignIn(email, password);
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.

     * @param email Email of the user to login
     * @param password Password of the user to login
     */
    private void attemptSignIn(String email, String password) {
        // Request a string response from the provided URL.
        final Context ctx = this.getContext();

        LoginPayload login = new LoginPayload();
        login.email = email;
        login.password = password;


        GsonRequest<LoginPayload> loginReq = new GsonRequest<LoginPayload>(Request.Method.POST, Constants.kSignInURL,
                login, LoginPayload.class,
                new Response.Listener<LoginPayload>() {
                    @Override
                    public void onResponse(LoginPayload response) {
                        // Display the first 500 characters of the response string.
                        Log.d(TAG,response.token);
                        handleDrivesenseLoginToken(response.token);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ctx, R.string.login_failed, Toast.LENGTH_SHORT).show();
            }
        });
        // Add the request to the RequestQueue.
        RequestQueueSingleton.getInstance(this.getContext()).getRequestQueue().add(loginReq);
    }
    // endregion

    public void handleDrivesenseLoginToken(String token) {
        DriveSenseToken dsToken = new DriveSenseToken(token);
    }
}
