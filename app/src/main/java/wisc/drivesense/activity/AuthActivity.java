package wisc.drivesense.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import wisc.drivesense.R;
import wisc.drivesense.fragment.AuthLandingFragment;

public class AuthActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_auth);

        Fragment landingFragment = AuthLandingFragment.newInstance();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_fragment_content, landingFragment)
                .commit();
    }

}
