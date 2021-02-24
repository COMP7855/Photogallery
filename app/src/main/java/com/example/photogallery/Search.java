package com.example.photogallery;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent; import android.os.Bundle;
import android.view.View; import android.widget.EditText;
import java.text.DateFormat; import java.text.SimpleDateFormat;
import java.util.Calendar; import java.util.Date;
import java.util.Locale;

public class Search extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        try {
            Calendar calendar = Calendar.getInstance();
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date now = calendar.getTime();
            String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now);
            Date today = format.parse((String) todayStr);
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            String tomorrowStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format( calendar.getTime());
            Date tomorrow = format.parse((String) tomorrowStr);
            ((EditText) findViewById(R.id.editTextStartTime)).setText(new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(today));
            ((EditText) findViewById(R.id.editTextEndTime)).setText(new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(tomorrow));
        } catch (Exception ex) { }
    }
    public void cancel(final View v) {
        finish();
    }
    public void go(final View v) {
        Intent i = new Intent();
        EditText from = (EditText) findViewById(R.id.editTextStartTime);
        EditText to = (EditText) findViewById(R.id.editTextEndTime);
        EditText keywords = (EditText) findViewById(R.id.editTextKeyword);
        EditText latMin = (EditText) findViewById(R.id.etLatMin);
        EditText latMax = (EditText) findViewById(R.id.etLatMax);
        EditText longMin = (EditText) findViewById(R.id.etLongMin);
        EditText longMax = (EditText) findViewById(R.id.etLongMax);
        i.putExtra("STARTTIMESTAMP", from.getText() != null ? from.getText().toString() : "");
        i.putExtra("ENDTIMESTAMP", to.getText() != null ? to.getText().toString() : "");
        i.putExtra("KEYWORDS", keywords.getText() != null ? keywords.getText().toString() : "");
        i.putExtra("LATMIN", latMin.getText() != null ? latMin.getText().toString() : "");
        i.putExtra("LATMAX", latMax.getText() != null ? latMax.getText().toString() : "");
        i.putExtra("LONGMIN", longMin.getText() != null ? longMin.getText().toString() : "");
        i.putExtra("LONGMAX", longMax.getText() != null ? longMax.getText().toString() : "");
        setResult(RESULT_OK, i);
        finish();
    }
}