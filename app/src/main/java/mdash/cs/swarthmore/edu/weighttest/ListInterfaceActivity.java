package mdash.cs.swarthmore.edu.weighttest;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by dredelm1 on 7/3/14.
 */
public class ListInterfaceActivity extends BaseListActivity {

    int series_size;
    private static final int GET_VIEW = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_interface);

        ArrayList<Entry> entries = (ArrayList<Entry>) getIntent().getSerializableExtra("edu.swarthmore.cs.mdash.entries");
        Entry[] newEntries = new Entry[entries.size()];
        newEntries = entries.toArray(newEntries);

        SimpleArrayAdapter adapter = new SimpleArrayAdapter(this, newEntries);
        setListAdapter(adapter);
    }

    // Setting action bar items
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_interface_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // action bar logic
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.back_button:
                // go back
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public class SimpleArrayAdapter extends ArrayAdapter<Entry> {
        private final Context context;
        private final Entry[] values;

        public SimpleArrayAdapter(Context context, Entry[] values) {
            super(context, R.layout.list_interface_row, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.list_interface_row, parent, false);

            TextView dateTextView = (TextView) rowView.findViewById(R.id.table_date);
            dateTextView.setText(values[position].getDate());

            TextView weightTextView = (TextView) rowView.findViewById(R.id.table_weight);
            weightTextView.setText(values[position].getWeight());

            TextView fatTextView = (TextView) rowView.findViewById(R.id.table_fat);
            fatTextView.setText(values[position].getFat());

            TextView leanTextView = (TextView) rowView.findViewById(R.id.table_lean);
            leanTextView.setText(values[position].getLean());

            return rowView;
        }
    }
}
