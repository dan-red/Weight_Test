package mdash.cs.swarthmore.edu.weighttest;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


public class WeightActivity extends BaseActivity {

    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new DatabaseHelper(this);

        //if (getNewData == 0) {
        //    new HttpAsyncTask().execute(wPath);
        //}
        if ((db.getNewestEpoch() == JSONEpoch) && (JSONEpoch != 0)) {
            Log.d("Note: ", "Database is up to date; right before getFirstEntry()");
            Log.d("JSON Epoch is ", Integer.toString(JSONEpoch));
            Log.d("Db epoch is ", Integer.toString(db.getNewestEpoch()));
            // database is already updated, so just show the most recent entry
            Entry e = db.getFirstEntry();
            weight_view = e.getWeight();
            date_view = e.getDate();
            Intent i = new Intent();
            i.putExtra("weight_view", weight_view);
            i.putExtra("date_view", date_view);
            setResult(RESULT_OK, i);
            finish();
            //mWeightView.setText("Last recorded weight is: " + e.getWeight());
            //mDateView.setText("On date: " + e.getDate());
            // if ((db.getRowCount() == JSONLength) && (db.getNewestEpoch() == JSONEpoch)) {
        } else {
            Log.d("db epoch is ", Integer.toString(db.getNewestEpoch()));
            Log.d("JSON epoch is ", Integer.toString(JSONEpoch));
            new HttpAsyncTask().execute(wPath);
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

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String...urls) {
            return GET(urls[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.d("Place: ", "in onPostExecute()");

            try {
                JSONObject main = new JSONObject(result);
                JSONObject body = main.getJSONObject("body");
                JSONArray measuregrps = body.getJSONArray("measuregrps");
                JSONLength = measuregrps.length();

                db.dropAndCreateTable();
                // get measurements for date, unit, and value (weight)
                for (int i = 0; i < measuregrps.length(); i++) {
                    JSONObject row = measuregrps.getJSONObject(i);
                    raw_date = row.getInt("date");
                    if (i == 0) {
                        JSONEpoch = raw_date;
                    }
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm a");
                    format.setTimeZone(TimeZone.getTimeZone("GMT-04:00"));
                    date = format.format(new Date(raw_date * 1000L));
                    JSONArray measures = row.getJSONArray("measures");

                    JSONObject entry = measures.getJSONObject(0);
                    int raw_val = entry.getInt("value");
                    int unit = entry.getInt("unit");
                    // parse data
                    double m = Math.pow(10, unit);
                    double val = raw_val * m * 2.20462262;
                    BigDecimal bd = new BigDecimal(val);
                    bd = bd.setScale(2, RoundingMode.HALF_UP);
                    weight = Double.toString(bd.doubleValue());

                    if (measures.length() > 1) {
                        JSONObject entry2 = measures.getJSONObject(1);
                        int raw_val2 = entry2.getInt("value");
                        int unit2 = entry2.getInt("unit");
                        // parse data
                        double m2 = Math.pow(10, unit2);
                        double val2 = raw_val2 * m2 * 2.20462262;
                        BigDecimal bd2 = new BigDecimal(val2);
                        bd2 = bd2.setScale(2, RoundingMode.HALF_UP);
                        fat = Double.toString(bd2.doubleValue());

                        JSONObject entry3 = measures.getJSONObject(2);
                        int raw_val3 = entry3.getInt("value");
                        int unit3 = entry3.getInt("unit");
                        // parse data
                        double m3 = Math.pow(10, unit3);
                        double val3 = raw_val3 * m3 * 2.20462262;
                        BigDecimal bd3 = new BigDecimal(val3);
                        bd3 = bd3.setScale(2, RoundingMode.HALF_UP);
                        lean = Double.toString(bd3.doubleValue());
                    }
                    db.addEntry(new Entry(raw_date, date, weight, fat, lean));
                }

                Entry e = db.getFirstEntry();
                weight_view = e.getWeight();
                date_view = e.getDate();

                db.close();
                copyFile();

                Intent i = new Intent();
                i.putExtra("weight_view", weight_view);
                i.putExtra("date_view", date_view);
                setResult(RESULT_OK, i);
                finish();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    // Copies database from internal memory to sd card
    public void copyFile() {
        Log.d("Place: ", "in copyFile()");
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "//data//" + PACKAGE_NAME + "//databases//" + DB_NAME;
                String destDBPath = "/Download/" + "Withings.db";
                File currentDB = new File(data, currentDBPath);
                File destDB = new File(sd, destDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(destDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
        }
    }
}
