package de.idvos.fastonlineidentification.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import de.idvos.fastonlineidentification.sdk.R;
import de.idvos.fastonlineidentification.config.AppConfig;
import de.idvos.fastonlineidentification.sdk.IdvosSDK;

/**
 * Created by mohammadrezanajafi on 2/3/15.
 */
public class SummaryActivity extends BaseActivity {

    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context, SummaryActivity.class);
        return intent;
    }

    TableLayout summaryTableLayout;
    ProgressBar summaryProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        setMenuButton(R.drawable.ic_action_bac, true);
        summaryProgressBar = (ProgressBar)findViewById(R.id.summary_progress);
        summaryTableLayout = (TableLayout)findViewById(R.id.summary_table);
        startAPICall(getIntent().getStringExtra("hash"));
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }

    private void startAPICall(String idHash){
        summaryProgressBar.setVisibility(View.VISIBLE);
        String serverUrl = IdvosSDK.getInstance().getMode().getEndpoint() + "api/v1/mobile/";
        StringRequest request = new StringRequest(Request.Method.GET,serverUrl + "identifications/" + idHash + "/summary",new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);
//                    addRow("title",jsonObject.getString("title"));
//                    addRow("address_title",jsonObject.getString("address_title"));
                    addRow("Mobiltelefon",jsonObject.getString("mobile_phone"));
                    addRow("E-Mail",jsonObject.getString("email"));
                    addRow("Vorname(n)",jsonObject.getString("name"));
                    addRow("Nachname",jsonObject.getString("surname"));

                    addRow("Geburtstag",jsonObject.getString("date_of_birth"));
                    addRow("Geburtsort",jsonObject.getString("place_of_birth"));
                    addRow("Staatsangehörigkeit",jsonObject.getString("nationality"));
                    addRow("Adresse",jsonObject.getString("address"));
                    addRow("Wohnort",jsonObject.getString("city"));
                    addRow("Postleitzahl",jsonObject.getString("postal_code"));
                    addRow("Geschlecht",jsonObject.getString("gender"));
                    addRow("Land",jsonObject.getString("country"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                summaryProgressBar.setVisibility(View.GONE);

            }
        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();

            }
        });
        AppConfig.getInstance().addToRequestQueue(request);
    }
    private void addRow(String key,String value){
        TableRow row= new TableRow(this);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT);
//        lp.topMargin = 200;
        row.setLayoutParams(lp);

        row.setBackgroundResource(R.drawable.table_row_bg);

        TextView keyView = new TextView(this);
//        keyView.setBackgroundColor(Color.RED);
        keyView.setPadding(5,5,5,5);
        keyView.setTextAppearance(this,android.R.style.TextAppearance_Medium);
        keyView.setText(key);

        TextView valueView = new TextView(this);
//        valueView.setBackgroundColor(Color.GRAY);
        valueView.setPadding(5,5,5,5);
        valueView.setTextAppearance(this,android.R.style.TextAppearance_Medium);
        valueView.setText(value);

        row.addView(keyView);
        row.addView(valueView);
        summaryTableLayout.addView(row);
    }

    @Override
    protected void onLeftMenuButtonClicked() {
        onBackPressed();
    }
}