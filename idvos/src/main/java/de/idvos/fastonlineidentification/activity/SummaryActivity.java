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
                    addRow(getString(R.string.idvos_response_phone),jsonObject.getString("mobile_phone"));
                    addRow(getString(R.string.idvos_response_mail),jsonObject.getString("email"));
                    addRow(getString(R.string.idvos_response_name),jsonObject.getString("name"));
                    addRow(getString(R.string.idvos_response_surname),jsonObject.getString("surname"));

                    addRow(getString(R.string.idvos_response_date_of_birth),jsonObject.getString("date_of_birth"));
                    addRow(getString(R.string.idvos_response_place_of_birth),jsonObject.getString("place_of_birth"));
                    addRow(getString(R.string.idvos_response_nationality),jsonObject.getString("nationality"));
                    addRow(getString(R.string.idvos_response_address),jsonObject.getString("address"));
                    addRow(getString(R.string.idvos_response_city),jsonObject.getString("city"));
                    addRow(getString(R.string.idvos_response_post_code),jsonObject.getString("postal_code"));
                    addRow(getString(R.string.idvos_response_gender),jsonObject.getString("gender"));
                    addRow(getString(R.string.idvos_response_country),jsonObject.getString("country"));
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