package edu.perphy.translateapp;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private String[] words;
    private AutoCompleteTextView actv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        words = new String[OxfordHelper.WORD_COUNT];
        actv = (AutoCompleteTextView) findViewById(R.id.actv);
        actv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String word = ((TextView) view).getText().toString();
                Toast.makeText(getApplicationContext(), word, Toast.LENGTH_LONG).show();
            }
        });
        new OxfordTask().execute();
    }

    private class OxfordTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog pb;

        @Override
        protected void onPreExecute() {
            pb = new ProgressDialog(MainActivity.this);
            pb.setMessage("Loading...");
            pb.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            OxfordHelper helper = new OxfordHelper(MainActivity.this);
            SQLiteDatabase reader = helper.getReadableDatabase();
            reader.beginTransaction();
            try (Cursor c = reader.query(OxfordHelper.DATABASE_NAME,
                    new String[]{"word"},
                    null, null, null, null, null)) {
                int i = 0;
                while (c.moveToNext()) {
                    words[i++] = c.getString(c.getColumnIndex(OxfordHelper.COL_WORD));
                }
                reader.setTransactionSuccessful();
            } catch (Exception e) {
                Log.e("PY", "OxfordTask.doInBackground: ", e);
                e.printStackTrace();
            } finally {
                reader.endTransaction();
                reader.close();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (pb != null) pb.dismiss();

            ArrayAdapter<String> wordAdapter = new ArrayAdapter<>(MainActivity.this,
                    android.R.layout.simple_list_item_1,
                    android.R.id.text1,
                    words);
            actv.setAdapter(wordAdapter);
            actv.showDropDown();
        }
    }
}
