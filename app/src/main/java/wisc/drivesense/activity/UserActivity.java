package wisc.drivesense.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import wisc.drivesense.R;
import wisc.drivesense.database.DatabaseHelper;
import wisc.drivesense.fragment.AuthLandingFragment;
import wisc.drivesense.fragment.UserProfileFragment;

public class UserActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper_;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_user);

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

}
