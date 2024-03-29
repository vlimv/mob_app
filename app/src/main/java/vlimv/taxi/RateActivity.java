package vlimv.taxi;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

public class RateActivity extends AppCompatActivity {
    RatingBar ratingBar;
    Button button;
    EditText writeComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate);
        Intent intent = getIntent();
        final String tripId = intent.getExtras().getString("TRIP_ID");
        writeComment = findViewById(R.id.write_comment);
        ratingBar = findViewById(R.id.ratingBar);
        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ServerSocket.getInstance(view.getContext()).sendReportTrip(tripId, writeComment.getText().toString(), ratingBar.getRating());
                //Toast.makeText(getBaseContext(), "" + ratingBar.getRating(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(RateActivity.this, PassengerMainActivity.class);
                startActivity(intent);
                //Toast.makeText(getBaseContext(), "" + ratingBar.getRating(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}
