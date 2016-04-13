package wisc.drivesense.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import wisc.drivesense.R;

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        ListView listView = (ListView)findViewById(R.id.listView);
        //*****To be replaced by list of trips*****
        String[] trips= {"trip1","trip2","trip3","trip4","trip5","trip6"};
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,trips);
        listView.setAdapter(adapter);
    }
}
