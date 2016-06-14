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

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import wisc.drivesense.R;
import wisc.drivesense.database.DatabaseHelper;
import wisc.drivesense.uploader.HttpClient;
import wisc.drivesense.utility.Constants;
import wisc.drivesense.utility.User;


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
    private TextView mUserDetailView;

    private Button mEmailSignInButton;
    private Button mEmailSignUpButton;
    private Button mEmailSignOutButton;


    private String TAG = "UserActivity";
    private DatabaseHelper dbHelper_;
    private User curUser_;

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
        mUserDetailView = (TextView) findViewById(R.id.userdetail);


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
        mEmailSignOutButton = (Button) findViewById(R.id.email_sign_out_button);
        mEmailSignOutButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                dbHelper_.userLogout(curUser_);
                displaySignIn();
            }
        });


        dbHelper_ = new DatabaseHelper();
        curUser_ = dbHelper_.getCurrentUser();
        if(curUser_ == null) {
            displaySignIn();
        } else {
            displaySignOut(curUser_);
        }
    }

    private void displaySignOut(User user) {
        Log.d(TAG, "Display Sign Out");

        mEmailView.setVisibility(View.GONE);
        mPasswordView.setVisibility(View.GONE);
        mFirstNameView.setVisibility(View.GONE);
        mLastNameView.setVisibility(View.GONE);
        mRepeatView.setVisibility(View.GONE);
        mProgressView.setVisibility(View.GONE);

        mEmailSignInButton.setVisibility(View.GONE);
        mEmailSignUpButton.setVisibility(View.GONE);

        mUserDetailView.setVisibility(View.VISIBLE);
        mEmailSignOutButton.setVisibility(View.VISIBLE);

        mUserDetailView.setText("Current User: \n" + user.email_ + "\n" /*+ user.firstname_ + " " + user.lastname_ + "\n"*/);
    }

    private void displaySignUp() {
        Log.d(TAG, "Display Sign Up");

        mLoginFormView.setVisibility(View.VISIBLE);
        mEmailView.setVisibility(View.VISIBLE);
        mPasswordView.setVisibility(View.VISIBLE);
        mFirstNameView.setVisibility(View.VISIBLE);
        mLastNameView.setVisibility(View.VISIBLE);
        mRepeatView.setVisibility(View.VISIBLE);

        mEmailSignInButton.setVisibility(View.GONE);
        mProgressView.setVisibility(View.GONE);
        mUserDetailView.setVisibility(View.GONE);
        mEmailSignOutButton.setVisibility(View.GONE);

    }
    private void displaySignIn() {
        Log.d(TAG, "Display Sign In");

        mLoginFormView.setVisibility(View.VISIBLE);
        mEmailView.setVisibility(View.VISIBLE);
        mPasswordView.setVisibility(View.VISIBLE);
        mFirstNameView.setVisibility(View.GONE);
        mLastNameView.setVisibility(View.GONE);
        mRepeatView.setVisibility(View.GONE);

        mEmailSignInButton.setVisibility(View.VISIBLE);
        mEmailSignUpButton.setVisibility(View.VISIBLE);


        mProgressView.setVisibility(View.GONE);
        mUserDetailView.setVisibility(View.GONE);
        mEmailSignOutButton.setVisibility(View.GONE);
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
            curUser_ = new User();
            curUser_.email_ = email;
            curUser_.password_ = password;

            mAuthTask = new RegisterTask(curUser_, true);
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
            curUser_ = new User();
            curUser_.email_ = email;
            curUser_.password_ = password;
            curUser_.firstname_ = firstname;
            curUser_.lastname_ = lastname;
            mAuthTask = new RegisterTask(curUser_, false);
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

        private User user_;
        private boolean signin_ = false;



        RegisterTask(User user, boolean issignin) {
            user_ = user;
            signin_ = issignin;
        }


        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            String res = "";
            try {
                // Simulate network access.
                String url = "";
                if(signin_) {
                    url = Constants.kSignInURL;
                } else {
                    url = Constants.kSignUpURL;
                }
                HttpClient client = new HttpClient(url);
                client.connectForMultipart();
                client.addFormPart("email", user_.email_);
                client.addFormPart("password", user_.password_);
                if(!signin_) {
                    client.addFormPart("firstname", user_.firstname_);
                    client.addFormPart("lastname", user_.lastname_);
                }
                client.finishMultipart();
                res = client.getResponse();
                Log.d(TAG, res);
            } catch (Throwable t) {
                t.printStackTrace();
                return false;
            }

            if(res == null || !res.contains("success")) {
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
                if(signin_) {
                    //attempted remotely to sign in
                    boolean res = dbHelper_.userLogin(user_);
                    if(false == res) {
                       //local failed
                        dbHelper_.newUser(user_);
                    }
                    curUser_ = dbHelper_.getCurrentUser();
                    if(curUser_ != null) {
                        displaySignOut(curUser_);
                    } else {
                        displaySignIn();
                    }
                } else {
                    //attempted to sign up
                    dbHelper_.newUser(user_);
                    displaySignOut(user_);
                }
                finish();
            } else {
                mEmailView.setError(getString(R.string.error_login_failed));
                mEmailView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

