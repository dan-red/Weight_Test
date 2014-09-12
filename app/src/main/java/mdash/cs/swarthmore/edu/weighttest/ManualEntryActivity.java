package mdash.cs.swarthmore.edu.weighttest;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by dredelm1 on 7/11/14.
 */
public class ManualEntryActivity extends BaseActivity {

    private DatePicker datePicker;
    private EditText weightEntry, fatEntry, leanEntry;
    private Button submitButton;

    private Date date;
    private String dateString;
    private String weight;
    private String fat;
    private String lean;

    private Entry newEntry = new Entry();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_entry);

        datePicker = (DatePicker) findViewById(R.id.manual_entry_date_picker);
        weightEntry = (EditText) findViewById(R.id.manual_entry_weight);
        fatEntry = (EditText) findViewById(R.id.manual_entry_fat_mass);
        leanEntry = (EditText) findViewById(R.id.manual_entry_lean_mass);
        submitButton = (Button) findViewById(R.id.manual_entry_submit_button);

        Calendar calendar = Calendar.getInstance();
        // calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        datePicker.init(year, month, day, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                date = new GregorianCalendar(year, month, day).getTime();

                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                dateString = format.format(date);

                int epochTime = (int) date.getTime();

                newEntry.setEpoch(epochTime);
                newEntry.setDate(dateString);
            }
        });

        weightEntry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                newEntry.setWeight(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // not needed
            }
        });

        fatEntry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                newEntry.setFat(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // not needed
            }
        });

        leanEntry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                newEntry.setLean(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // not needed
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseHelper dbHelper = new DatabaseHelper(ManualEntryActivity.this);
                Log.d("ManualEntryActivity:", "getRowCount(): " + dbHelper.getRowCount());
                dbHelper.addEntry(newEntry);
                Log.d("ManualEntryACtivity:", "getRowCounter() AFTER add: " + dbHelper.getRowCount());
                finish();
            }
        });


    }
}
