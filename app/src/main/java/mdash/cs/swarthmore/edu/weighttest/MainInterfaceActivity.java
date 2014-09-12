package mdash.cs.swarthmore.edu.weighttest;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.androidplot.Plot;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by dredelm1 on 6/30/14.
 */
public class MainInterfaceActivity extends BaseActivity implements View.OnTouchListener {

    String query;
    private static final int GET_VIEW = 1;

    DatabaseHelper dbHelper = new DatabaseHelper(this);

    // Graphing variables
    private int series_size;
    private XYPlot mySimpleXYPlot;
    private SimpleXYSeries[] series = null;
    private PointF minXY;
    private PointF maxXY;
    String var_names[] = {"Weight (lb)", "Fat mass (%)", "Lean mass (%)"}; // placeholder, should make this general based on first line of database
    String date_string = "yyyy-MM-dd"; // how the date should be parsed, could incorporate Entry.getEpoch() instead

    // touch-zoom variables
    static final int NONE = 0;
    static final int ONE_FINGER_DRAG = 1;
    static final int TWO_FINGERS_DRAG = 2;
    int mode = NONE;
    PointF firstFinger;
    float distBetweenFingers;
    boolean stopThread = false;

    Entry[] newEntries;


    // Setting action bar items
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_interface_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // action bar logic
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.table_button:
                // start table display activity
                Intent j = new Intent(MainInterfaceActivity.this, ListInterfaceActivity.class);
                final ArrayList<Entry> arrayListEntries = new ArrayList<Entry>(Arrays.asList(newEntries));
                Bundle bundle = new Bundle();
                bundle.putSerializable("edu.swarthmore.cs.mdash.entries", arrayListEntries);
                j.putExtras(bundle);
                startActivity(j);
                return true;
            case R.id.reset_button:
                // reset graph
                minXY.x = series[0].getX(0).floatValue();
                maxXY.x = series[0].getX(series_size - 1).floatValue();
                mySimpleXYPlot.setDomainBoundaries(minXY.x, maxXY.x, BoundaryMode.FIXED);
                mySimpleXYPlot.redraw();
                return true;
            case R.id.settings_button:
                // start settings activity
            case R.id.sync_button:
                // sync new data
                Intent i = new Intent(this, WeightActivity.class);
                i.putExtra("weight_view", weight_view);
                i.putExtra("date_view", date_view);
                startActivityForResult(i, GET_VIEW);
                return true;
            case R.id.text_update_button:
                // start text update activity
                Intent k = new Intent(this, TextUpdateActivity.class);
                startActivity(k);
                return true;
            case R.id.manual_entry_button:
                // start manual entry
                Intent l = new Intent(this, ManualEntryActivity.class);
                startActivity(l);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Setting layout and initializing activity
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWeightView = (EditText) findViewById(R.id.weight);
        mDateView = (EditText) findViewById(R.id.date);
        // query = (String) getIntent().getSerializableExtra("query");

        canSync = false;
        new HttpAsyncTask2().execute(wPath);

        // sync new data (just in case)
        Intent in = new Intent(this, WeightActivity.class);
        in.putExtra("weight_view", weight_view);
        in.putExtra("date_view", date_view);
        startActivityForResult(in, GET_VIEW);

        // --------- CREATING THE GRAPH ---------

        series_size = dbHelper.getRowCount();
        Log.d("MainInterfaceActivity:", "series_size = " + dbHelper.getRowCount());

        mySimpleXYPlot = (XYPlot) findViewById(R.id.mySimpleXYPlot);
        mySimpleXYPlot.setOnTouchListener(this);
        mySimpleXYPlot.getGraphWidget().setTicksPerRangeLabel(2);
        mySimpleXYPlot.getGraphWidget().setTicksPerDomainLabel(2);
        mySimpleXYPlot.getGraphWidget().getBackgroundPaint().setColor(Color.TRANSPARENT);
        mySimpleXYPlot.getGraphWidget().setRangeValueFormat(new DecimalFormat("#####"));
        mySimpleXYPlot.getGraphWidget().setRangeLabelWidth(25);
        // no axis labels
        mySimpleXYPlot.setRangeLabel("");
        mySimpleXYPlot.setDomainLabel("");

        Paint paint = new Paint();
        paint.setARGB(255, 0, 0, 0);
        mySimpleXYPlot.getTitleWidget().setLabelPaint(paint);
        mySimpleXYPlot.setBorderStyle(Plot.BorderStyle.NONE, null, null);

        mySimpleXYPlot.getLegendWidget().setVisible(false);

        // this isn't generalized for an "unknown" database, could possibly be improved
        series = new SimpleXYSeries[3];

        for (int i = 0; i < var_names.length; i++) {
            series[i] = new SimpleXYSeries(var_names[i]);
        }

        SimpleDateFormat format = new SimpleDateFormat(date_string);
        String date_to_add;
        Date date;
        Number[] dates_in_seconds = new Number[series_size];
        newEntries = new Entry[series_size];

        dbHelper.parseData(newEntries);

        Number weight, fatmass, leanmass;

        for (int i = 0; i < series_size; i++) {
            // Log.d("MainInterfaceActivity: ", "series_size = " + series_size);
            // Log.d("MainInterfaceActivity: ", "series size - 1 - i = " + (series_size - 1 - i));
            // Log.d("MainInterfaceActivity: ", "date_to_add = " + newEntries[series_size - 1 - i].getDate());
            // Log.d("Some epoch data:", newEntries[i].getEpoch().toString());
            date_to_add = newEntries[series_size - 1 - i].getDate();
            try {
                date = format.parse(date_to_add);
                dates_in_seconds[i] = date.getTime();
            } catch (Exception e) {
                Log.d("Getting date", e.getLocalizedMessage());
            }

            weight = Double.parseDouble(newEntries[i].getWeight());
            fatmass = Double.parseDouble(newEntries[i].getFat());
            leanmass = Double.parseDouble(newEntries[i].getLean());

            series[0].addLast(dates_in_seconds[i], weight);
            // Log.d("MainInterfaceActivity:", "dates_in_seconds[i], weight" + dates_in_seconds[i] + ", " + weight);
            series[1].addLast(dates_in_seconds[i], fatmass);
            series[2].addLast(dates_in_seconds[i], leanmass);

        }

        mySimpleXYPlot.setDomainValueFormat(new Format() {
            private SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy"); // graph date format

            @Override
            public StringBuffer format(Object object, StringBuffer buffer, FieldPosition field) {
                long timestamp = ((Number) object).longValue();
                Date date = new Date(timestamp);
                return format.format(date, buffer, field);
            }

            @Override
            public Object parseObject(String string, ParsePosition position) {
                return null;
            }
        });

        // Only adding weight-by-time series
        mySimpleXYPlot.addSeries(series[0], new LineAndPointFormatter(Color.rgb(50, 0 ,0), null, null, null));

        mySimpleXYPlot.redraw();
        mySimpleXYPlot.calculateMinMaxVals();
        minXY = new PointF(mySimpleXYPlot.getCalculatedMinX().floatValue(), mySimpleXYPlot.getCalculatedMaxY().floatValue());
        maxXY = new PointF(mySimpleXYPlot.getCalculatedMaxX().floatValue(), mySimpleXYPlot.getCalculatedMaxY().floatValue());
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (canSync) {
            menu.getItem(3).setEnabled(true);
        } else {
            menu.getItem(3).setEnabled(false);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //get result
        if (resultCode != Activity.RESULT_OK) return;
        if (requestCode == GET_VIEW) {
            String w = data.getStringExtra("weight_view");
            String d = data.getStringExtra("date_view");
            mWeightView.setText("Last recorded weight is: " + w);
            mDateView.setText("On date: " + d);
        }
    }

    public static String GET(String url) {
        InputStream is = null;
        String result = "";
        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet(url);
            HttpResponse httpResponse = client.execute(get);
            is = httpResponse.getEntity().getContent();
            if (is != null)
                result = convertInputStream(is);
            else
                result = "Did not work!";
        } catch (Exception e) {
            Log.d("input stream", e.getLocalizedMessage());
        }

        return result;
    }

    private static String convertInputStream(InputStream is) throws IOException {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line = "";
        while((line = reader.readLine()) != null)
            builder.append(line);
        is.close();
        return builder.toString();
    }

    private class HttpAsyncTask2 extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String...urls) {
            return GET(urls[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject main = new JSONObject(result);
                JSONObject body = main.getJSONObject("body");
                JSONArray measuregrps = body.getJSONArray("measuregrps");
                JSONObject row0 = measuregrps.getJSONObject(0);
                JSONEpoch = row0.getInt("date");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            canSync = true;
        }
    }

    @Override
    public boolean onTouch(View arg0, MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: // Start gesture
                firstFinger = new PointF(event.getX(), event.getY());
                mode = ONE_FINGER_DRAG;
                stopThread = true;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;
            case MotionEvent.ACTION_POINTER_DOWN: // second finger
                distBetweenFingers = spacing(event);
                // the distance check is done to avoid false alarms
                if (distBetweenFingers > 5f) {
                    mode = TWO_FINGERS_DRAG;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == ONE_FINGER_DRAG) {
                    PointF oldFirstFinger = firstFinger;
                    firstFinger = new PointF(event.getX(), event.getY());
                    scroll(oldFirstFinger.x - firstFinger.x);
                    mySimpleXYPlot.setDomainBoundaries(minXY.x, maxXY.x, BoundaryMode.FIXED);
                    mySimpleXYPlot.redraw();
                }
                else if (mode == TWO_FINGERS_DRAG) {
                    float oldDist = distBetweenFingers;
                    distBetweenFingers = spacing(event);
                    zoom(oldDist / distBetweenFingers);
                    mySimpleXYPlot.setDomainBoundaries(minXY.x, maxXY.x, BoundaryMode.FIXED);
                    mySimpleXYPlot.redraw();
                }
                break;
        }
        return true;
    }

    private void zoom(float scale) {
        float domainSpan = maxXY.x - minXY.x;
        float domainMidPoint = maxXY.x - domainSpan / 2.0f;
        float offset = domainSpan * scale / 2.0f;

        minXY.x = domainMidPoint - offset;
        maxXY.x = domainMidPoint + offset;

        minXY.x = Math.min(minXY.x, series[0].getX(series[0].size() - 3).floatValue());
        maxXY.x = Math.max(maxXY.x, series[0].getX(1).floatValue());
        clampToDomainBounds(domainSpan);
    }

    private void scroll(float pan) {
        float domainSpan = maxXY.x - minXY.x;
        float step = domainSpan / mySimpleXYPlot.getWidth();
        float offset = pan * step;
        minXY.x = minXY.x + offset;
        maxXY.x = maxXY.x + offset;
        clampToDomainBounds(domainSpan);
    }

    private void clampToDomainBounds(float domainSpan) {
        float leftBoundary = series[0].getX(0).floatValue();
        float rightBoundary = series[0].getX(series[0].size() - 1).floatValue();
        // enforce left scroll boundary
        if (minXY.x < leftBoundary) {
            minXY.x = leftBoundary;
            maxXY.x = leftBoundary + domainSpan;
        }
        else if (maxXY.x > series[0].getX(series[0].size() - 1).floatValue()) {
            maxXY.x = rightBoundary;
            minXY.x = rightBoundary - domainSpan;
        }
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }

    // Updating things as necessary
    @Override
    public void onResume() {
        super.onResume();

        // updateActivity();
    }

    public void updateActivity() {

        // --------- CREATING THE GRAPH ---------

        series_size = dbHelper.getRowCount();
        Log.d("MainInterfaceActivity:", "series_size = " + dbHelper.getRowCount());

        mySimpleXYPlot = (XYPlot) findViewById(R.id.mySimpleXYPlot);
        mySimpleXYPlot.setOnTouchListener(this);
        mySimpleXYPlot.getGraphWidget().setTicksPerRangeLabel(2);
        mySimpleXYPlot.getGraphWidget().setTicksPerDomainLabel(2);
        mySimpleXYPlot.getGraphWidget().getBackgroundPaint().setColor(Color.TRANSPARENT);
        mySimpleXYPlot.getGraphWidget().setRangeValueFormat(new DecimalFormat("#####"));
        mySimpleXYPlot.getGraphWidget().setRangeLabelWidth(25);
        // no axis labels
        mySimpleXYPlot.setRangeLabel("");
        mySimpleXYPlot.setDomainLabel("");

        Paint paint = new Paint();
        paint.setARGB(255, 0, 0, 0);
        mySimpleXYPlot.getTitleWidget().setLabelPaint(paint);
        mySimpleXYPlot.setBorderStyle(Plot.BorderStyle.NONE, null, null);

        mySimpleXYPlot.getLegendWidget().setVisible(false);

        // this isn't generalized for an "unknown" database, could possibly be improved
        series = new SimpleXYSeries[3];

        for (int i = 0; i < var_names.length; i++) {
            series[i] = new SimpleXYSeries(var_names[i]);
        }

        SimpleDateFormat format = new SimpleDateFormat(date_string);
        String date_to_add;
        Date date;
        Number[] dates_in_seconds = new Number[series_size];
        newEntries = new Entry[series_size];

        dbHelper.parseData(newEntries);

        Number weight, fatmass, leanmass;

        for (int i = 0; i < series_size; i++) {
            // Log.d("MainInterfaceActivity: ", "series_size = " + series_size);
            // Log.d("MainInterfaceActivity: ", "series size - 1 - i = " + (series_size - 1 - i));
            // Log.d("MainInterfaceActivity: ", "date_to_add = " + newEntries[series_size - 1 - i].getDate());
            // Log.d("Some epoch data:", newEntries[i].getEpoch().toString());
            date_to_add = newEntries[series_size - 1 - i].getDate();
            try {
                date = format.parse(date_to_add);
                dates_in_seconds[i] = date.getTime();
            } catch (Exception e) {
                Log.d("Getting date", e.getLocalizedMessage());
            }

            weight = Double.parseDouble(newEntries[i].getWeight());
            fatmass = Double.parseDouble(newEntries[i].getFat());
            leanmass = Double.parseDouble(newEntries[i].getLean());

            series[0].addLast(dates_in_seconds[i], weight);
            // Log.d("MainInterfaceActivity:", "dates_in_seconds[i], weight" + dates_in_seconds[i] + ", " + weight);
            series[1].addLast(dates_in_seconds[i], fatmass);
            series[2].addLast(dates_in_seconds[i], leanmass);

        }

        mySimpleXYPlot.setDomainValueFormat(new Format() {
            private SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy"); // graph date format

            @Override
            public StringBuffer format(Object object, StringBuffer buffer, FieldPosition field) {
                long timestamp = ((Number) object).longValue();
                Date date = new Date(timestamp);
                return format.format(date, buffer, field);
            }

            @Override
            public Object parseObject(String string, ParsePosition position) {
                return null;
            }
        });

        // Only adding weight-by-time series
        mySimpleXYPlot.addSeries(series[0], new LineAndPointFormatter(Color.rgb(50, 0 ,0), null, null, null));

        mySimpleXYPlot.redraw();
        mySimpleXYPlot.calculateMinMaxVals();
        minXY = new PointF(mySimpleXYPlot.getCalculatedMinX().floatValue(), mySimpleXYPlot.getCalculatedMaxY().floatValue());
        maxXY = new PointF(mySimpleXYPlot.getCalculatedMaxX().floatValue(), mySimpleXYPlot.getCalculatedMaxY().floatValue());

        /*
        series_size = dbHelper.getRowCount();
        Log.d("MainInterfaceActivity:", "series_size = " + dbHelper.getRowCount());

        SimpleDateFormat format = new SimpleDateFormat(date_string);
        String date_to_add;
        Date date;
        Number[] dates_in_seconds = new Number[series_size];
        newEntries = new Entry[series_size];

        dbHelper.parseData(newEntries);

        Number weight, fatmass, leanmass;

        // this isn't generalized for an "unknown" database, could possibly be improved
        series = new SimpleXYSeries[3];

        for (int i = 0; i < var_names.length; i++) {
            series[i] = new SimpleXYSeries(var_names[i]);
        }

        for (int i = 0; i < series_size; i++) {
            // Log.d("MainInterfaceActivity: ", "series_size = " + series_size);
            // Log.d("MainInterfaceActivity: ", "series size - 1 - i = " + (series_size - 1 - i));
            // Log.d("MainInterfaceActivity: ", "date_to_add = " + newEntries[series_size - 1 - i].getDate());
            // Log.d("Some epoch data:", newEntries[i].getEpoch().toString());
            date_to_add = newEntries[series_size - 1 - i].getDate();
            try {
                date = format.parse(date_to_add);
                dates_in_seconds[i] = date.getTime();
            } catch (Exception e) {
                Log.d("Getting date", e.getLocalizedMessage());
            }

            weight = Double.parseDouble(newEntries[i].getWeight());
            fatmass = Double.parseDouble(newEntries[i].getFat());
            leanmass = Double.parseDouble(newEntries[i].getLean());

            series[0].addLast(dates_in_seconds[i], weight);
            // Log.d("MainInterfaceActivity:", "dates_in_seconds[i], weight" + dates_in_seconds[i] + ", " + weight);
            series[1].addLast(dates_in_seconds[i], fatmass);
            series[2].addLast(dates_in_seconds[i], leanmass);

        }

        // Only adding weight-by-time series
        mySimpleXYPlot.addSeries(series[0], new LineAndPointFormatter(Color.rgb(50, 0 ,0), null, null, null));

        mySimpleXYPlot.redraw();
        mySimpleXYPlot.calculateMinMaxVals();
        minXY = new PointF(mySimpleXYPlot.getCalculatedMinX().floatValue(), mySimpleXYPlot.getCalculatedMaxY().floatValue());
        maxXY = new PointF(mySimpleXYPlot.getCalculatedMaxX().floatValue(), mySimpleXYPlot.getCalculatedMaxY().floatValue());
        */
    }

}
