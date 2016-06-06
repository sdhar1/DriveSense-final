package wisc.drivesense.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import wisc.drivesense.R;
import wisc.drivesense.uploader.HttpClient;
import wisc.drivesense.utility.Constants;


/**
 * A login screen that offers login via email/password.
 */
public class UserActivity extends Activity {

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private RegisterTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private EditText mFirstNameView;
    private EditText mLastNameView;
    private EditText mRepeatView;

    private Button mEmailSignInButton;
    private Button mEmailSignUpButton;

    private String TAG = "UserActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);

        mFirstNameView  = (EditText) findViewById(R.id.first_name);
        mLastNameView = (EditText) findViewById(R.id.last_name);
        mRepeatView = (EditText) findViewById(R.id.repeat);
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);


        mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSignIn();
            }
        });
        mEmailSignUpButton = (Button) findViewById(R.id.email_sign_up_button);
        mEmailSignUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mRepeatView.getVisibility() == View.GONE) {
                    displaySignUp();
                } else {
                    attemptSignUp();
                }
            }
        });



        displaySignIn();
    }

    private void displaySignUp() {
        mLoginFormView.setVisibility(View.VISIBLE);
        mEmailView.setVisibility(View.VISIBLE);
        mPasswordView.setVisibility(View.VISIBLE);
        mFirstNameView.setVisibility(View.VISIBLE);
        mLastNameView.setVisibility(View.VISIBLE);
        mRepeatView.setVisibility(View.VISIBLE);

        mEmailSignInButton.setVisibility(View.GONE);
        mProgressView.setVisibility(View.GONE);
    }
    private void displaySignIn() {
        mLoginFormView.setVisibility(View.VISIBLE);
        mEmailView.setVisibility(View.VISIBLE);
        mPasswordView.setVisibility(View.VISIBLE);
        mFirstNameView.setVisibility(View.GONE);
        mLastNameView.setVisibility(View.GONE);
        mRepeatView.setVisibility(View.GONE);

        mProgressView.setVisibility(View.GONE);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptSignIn() {

        Log.d(TAG, "attempt to sign in");
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        email = "kanglei1130@gmail.com";
        password = "kanglei";

        Log.d(TAG, mPasswordView.getText().toString());

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new RegisterTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }


    private void attemptSignUp() {

        Log.d(TAG, "attempt to sign up");
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mFirstNameView.setError(null);
        mLastNameView.setError(null);
        mRepeatView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String repeat = mRepeatView.getText().toString();

        String firstname = mFirstNameView.getText().toString();
        String lastname = mLastNameView.getText().toString();

        email = "kanglei1130@gmail.com";
        password = "kanglei";
        repeat = "kanglei";
        firstname = "lei";
        lastname = "kang";

        Log.d(TAG, mPasswordView.getText().toString());

        boolean cancel = false;
        View focusView = null;


        if (TextUtils.isEmpty(firstname)) {
            mFirstNameView.setError(getString(R.string.error_field_required));
            focusView = mFirstNameView;
            cancel = true;
        }
        if (TextUtils.isEmpty(lastname)) {
            mLastNameView.setError(getString(R.string.error_field_required));
            focusView = mLastNameView;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (!password.equals(repeat)) {
            mPasswordView.setError(getString(R.string.error_mismatch_password));
            focusView = mPasswordView;
            cancel = true;
        }
        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new RegisterTask(email, password, firstname, lastname);
            mAuthTask.execute((Void) null);
        }
    }
    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }



    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class RegisterTask extends AsyncTask<Void, Void, Boolean> {

        private String email_;
        private String password_;
        private String firstname_;
        private String lastname_;
        private boolean signin = true;

        RegisterTask(String email, String password) {
            email_ = email;
            password_ = password;
            signin = true;
        }

        RegisterTask(String email, String password, String first, String last) {
            email_ = email;
            password_ = password;
            firstname_ = first;
            lastname_ = last;
            signin = false;
        }


        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            Log.d(TAG, "registering");
            String res = "";
            try {
                // Simulate network access.
                if(signin) {
                    String url = Constants.kSignInURL;
                    HttpClient client = new HttpClient(url);
                    client.connectForMultipart();
                    client.addFormPart("email", email_);
                    client.addFormPart("password", password_);
                    client.finishMultipart();
                    res = client.getResponse();
                } else {
                    String url = Constants.kSignUpURL;
                    HttpClient client = new HttpClient(url);
                    client.connectForMultipart();
                    client.addFormPart("email", email_);
                    client.addFormPart("password", password_);
                    client.addFormPart("firstname", firstname_);
                    client.addFormPart("lastname", lastname_);
                    client.finishMultipart();
                    res = client.getResponse();
                }
                Log.d(TAG, res);
            } catch (Throwable t) {
                t.printStackTrace();
                return false;
            }

            if(res == null || !res.contains("okay")) {
                return false;
            } else {
                return true;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
                if(signin) {
                    //attempted to sign in
                } else {
                    //attempted to sign up

                }
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

