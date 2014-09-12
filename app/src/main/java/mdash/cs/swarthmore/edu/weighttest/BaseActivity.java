package mdash.cs.swarthmore.edu.weighttest;

import android.app.Activity;
import android.widget.EditText;

/**
 * Created by mborris1 on 7/1/14.
 */
public class BaseActivity extends Activity {
    public static final String PACKAGE_NAME = "edu.swarthmore.cs.mdash.withingsdatabase";
    public static final String DB_NAME = "Withings";
    public static final String DB_PATH = "/data/data/" + PACKAGE_NAME + "/databases/" + DB_NAME;
    public static final String TABLE_NAME = "Weight_Data";

    public static final String uID = "24335";
    public static final String wAPIKey = "593b5a2946b12a6a";
    public static final String wPath = "http://wbsapi.withings.net/measure?action=getmeas&userid=" +
            uID + "&publickey=" + wAPIKey;

    EditText mWeightView, mDateView;
    public static int JSONLength, JSONEpoch, raw_date, getNewData;
    public static String weight, fat, lean, date;
    public static String weight_view, date_view;
    DatabaseHelper db;

    public boolean canSync;

}
