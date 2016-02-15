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

import org.apache.wink.json4j.OrderedJSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import de.idvos.fastonlineidentification.ResponseConstants;
import de.idvos.fastonlineidentification.config.AppConfig;
import de.idvos.fastonlineidentification.sdk.IdvosSDK;
import de.idvos.fastonlineidentification.sdk.R;

/**
 * Displays summary as a result of agent checks.
 */
public class SummaryActivity extends BaseActivity {


    TableLayout summaryTableLayout;
    ProgressBar summaryProgressBar;
    private static final HashMap<String, Integer> keyToStringResourceMap = new HashMap<>();
    private static final List<String> filterList = new ArrayList<>();

    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context, SummaryActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        setMenuButton(R.drawable.ic_action_bac, true);
        summaryProgressBar = (ProgressBar) findViewById(R.id.summary_progress);
        summaryTableLayout = (TableLayout) findViewById(R.id.summary_table);
        startAPICall(getIntent().getStringExtra("hash"));

        populate();
    }

    private void populate() {
        keyToStringResourceMap.put(ResponseConstants.MOBILE_PHONE, R.string.idvos_response_phone);
        keyToStringResourceMap.put(ResponseConstants.EMAIL, R.string.idvos_response_mail);
        keyToStringResourceMap.put(ResponseConstants.NAME, R.string.idvos_response_name);
        keyToStringResourceMap.put(ResponseConstants.FIRST_NAME, R.string.idvos_response_name);
        keyToStringResourceMap.put(ResponseConstants.SURNAME, R.string.idvos_response_surname);
        keyToStringResourceMap.put(ResponseConstants.DATE_OF_BIRTH, R.string.idvos_response_date_of_birth);
        keyToStringResourceMap.put(ResponseConstants.PLACE_OF_BIRTH, R.string.idvos_response_place_of_birth);
        keyToStringResourceMap.put(ResponseConstants.NATIONALITY, R.string.idvos_response_nationality);
        keyToStringResourceMap.put(ResponseConstants.ADDRESS, R.string.idvos_response_address);
        keyToStringResourceMap.put(ResponseConstants.CITY, R.string.idvos_response_city);
        keyToStringResourceMap.put(ResponseConstants.POSTAL_CODE, R.string.idvos_response_post_code);
        keyToStringResourceMap.put(ResponseConstants.GENDER, R.string.idvos_response_gender);
        keyToStringResourceMap.put(ResponseConstants.COUNTRY, R.string.idvos_response_country);
        keyToStringResourceMap.put(ResponseConstants.HONORIFIC, R.string.idvos_response_honorific);
        keyToStringResourceMap.put(ResponseConstants.SERIAL_NUMBER, R.string.idvos_response_serial_number);
        keyToStringResourceMap.put(ResponseConstants.PSEUDONYM, R.string.idvos_response_pseudonym);
        keyToStringResourceMap.put(ResponseConstants.ISSUED_AT, R.string.idvos_response_issued_at);
        keyToStringResourceMap.put(ResponseConstants.AUTHORITY, R.string.idvos_response_authority);

        filterList.add(ResponseConstants.FILTER_TITLE);
        filterList.add(ResponseConstants.FILTER_ADDRESS_TITLE);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }

    private void startAPICall(String idHash) {
        summaryProgressBar.setVisibility(View.VISIBLE);
        String serverUrl = IdvosSDK.getInstance().getMode().getEndpoint() + "api/v1/mobile/";
        StringRequest request = new StringRequest(
                Request.Method.GET,
                serverUrl + "identifications/" + idHash + "/summary",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            OrderedJSONObject jsonObject = new OrderedJSONObject(response);

                            for (Iterator iterator = jsonObject.getOrder(); iterator.hasNext(); ) {
                                String key = (String) iterator.next();
                                if (keyToStringResourceMap.containsKey(key)) {
                                    addRow(
                                            getString(keyToStringResourceMap.get(key)),
                                            jsonObject.getString(key)
                                    );
                                } else if (!filterList.contains(key)){
                                    addRow(key, jsonObject.getString(key));
                                }
                            }
                        } catch (org.apache.wink.json4j.JSONException e) {
                            e.printStackTrace();
                        }

                        summaryProgressBar.setVisibility(View.GONE);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();

                    }
                }
        );
        AppConfig.getInstance().addToRequestQueue(request);
    }

    private void addRow(String key, String value) {
        TableRow row = new TableRow(this);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT);
//        lp.topMargin = 200;
        row.setLayoutParams(lp);

        row.setBackgroundResource(R.drawable.table_row_bg);

        TextView keyView = new TextView(this);
        keyView.setPadding(5, 5, 5, 5);
        keyView.setTextAppearance(this, android.R.style.TextAppearance_Medium);
        keyView.setTextColor(getResources().getColor(R.color.color_accent));
        keyView.setText(key);

        TextView valueView = new TextView(this);
        valueView.setPadding(5, 5, 5, 5);
        valueView.setTextAppearance(this, android.R.style.TextAppearance_Medium);
        valueView.setTextColor(getResources().getColor(R.color.color_primary));
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