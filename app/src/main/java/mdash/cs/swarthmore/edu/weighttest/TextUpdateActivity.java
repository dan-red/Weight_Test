package mdash.cs.swarthmore.edu.weighttest;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

/**
 * Created by dredelm1 on 7/7/14.
 */
public class TextUpdateActivity extends BaseActivity {

    RealTimeUpdateDatabaseHelper dbHelper;
    TextView textView;
    Button changeButton, addButton;

    private Cursor cursor;

    private ListView listView;
    private CustomCursorAdapter customAdapter;

    private int time;
    private int i = 0;

    @Override
    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_update);
        dbHelper = new RealTimeUpdateDatabaseHelper(this);

        //time = (int) (System.currentTimeMillis());
        Calendar c = Calendar.getInstance();
        time = c.get(Calendar.MILLISECOND);

        listView = (ListView) findViewById(R.id.list_data);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                customAdapter = new CustomCursorAdapter(TextUpdateActivity.this, dbHelper.getData());
                listView.setAdapter(customAdapter);
            }
        });

        textView = (TextView) findViewById(R.id.text_update);
        changeButton = (Button) findViewById(R.id.change_button);
        addButton = (Button) findViewById(R.id.add_button);

        cursor = dbHelper.getData();
        startManagingCursor(cursor);

        /*adapter = new SimpleCursorAdapter(
                this,
                R.layout.text_update_list_row,
                cursor,
                new String[] {"Weight"},
                new int[] {R.id.text_update_display}
        );*/

        changeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                addButton.setEnabled(false);
                changeButton.setEnabled(false);

                // for (int i = 0; i < 20; i++) {
                // int i = 0;

                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm a");
                    String date = format.format(new Date(time));

                    Log.d("TextUpdateActivity", "Date: " + date);

                    Random random = new Random();
                    double weight = random.nextInt(200) / 2.0;
                    String weightString = new DecimalFormat("##.##").format(weight);

                    double fat = random.nextInt(200) / 2.0;
                    String fatString = new DecimalFormat("##.##").format(fat);

                    double lean = random.nextInt(200) / 2.0;
                    String leanString = new DecimalFormat("##.##").format(lean);

                    Entry entry = new Entry(time, date, weightString, fatString, leanString);

                    if (i == 0) {
                        Log.d("TextUpdateActivity, changeButton, i = 0", "Date: " + date + ", Weight: " + weightString);
                        dbHelper.addEntry(entry);
                        customAdapter.changeCursor(dbHelper.getData());
                        customAdapter.notifyDataSetChanged();
                        i = 1;


                    } else {
                        Log.d("TextUpdateActivity, changeButton, i != 0", "Date: " + date + ", Weight: " + weightString);
                        dbHelper.updateEntry(entry);
                        customAdapter.changeCursor(dbHelper.getData());
                        customAdapter.notifyDataSetChanged();
                    }

                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                // }

                addButton.setEnabled(true);
                changeButton.setEnabled(true);
            }
        });


        addButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                addButton.setEnabled(false);
                changeButton.setEnabled(false);

                int time;

                // for (int i = 0; i < 20; i++) {

                    // time = (int) (System.currentTimeMillis());
                    Calendar c = Calendar.getInstance();
                    time = c.get(Calendar.MILLISECOND);

                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm a");
                    format.setTimeZone(TimeZone.getDefault().getTimeZone("GMT-04:00"));
                    String date = format.format(new Date(time * 1000L));

                    Random random = new Random();
                    double weight = random.nextInt(200) / 2.0;
                    String weightString = new DecimalFormat("##.##").format(weight);

                    double fat = random.nextInt(200) / 2.0;
                    String fatString = new DecimalFormat("##.##").format(fat);

                    double lean = random.nextInt(200) / 2.0;
                    String leanString = new DecimalFormat("##.##").format(lean);

                    Entry entry = new Entry(time, date, weightString, fatString, leanString);

                    dbHelper.addEntry(entry);
                    Log.d("TextUpdateActivity", "Date: " + date + ", Weight: " + weightString);
                    customAdapter.changeCursor(dbHelper.getData());
                    customAdapter.notifyDataSetChanged();

                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                // }

                addButton.setEnabled(true);
                changeButton.setEnabled(true);
            }
        });


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cursor.close();
        dbHelper.close();
    }

    public class CustomCursorAdapter extends CursorAdapter {

        public CustomCursorAdapter(Context context, Cursor c) {
            super(context, c);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View retView = inflater.inflate(R.layout.text_update_list_row, parent, false);

            return retView;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView tv = (TextView) view.findViewById(R.id.text_update_display);
            tv.setText(cursor.getString(cursor.getColumnIndex(cursor.getColumnName(1))));
        }
    }
}
