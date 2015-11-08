package de.idvos.fastonlineidentification.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import de.idvos.fastonlineidentification.WaitingTimeTracker;
import de.idvos.fastonlineidentification.sdk.R;
import de.idvos.fastonlineidentification.sdk.IdentificationResult;
import de.idvos.fastonlineidentification.sdk.IdvosSDK;

public class StartActivity extends BaseActivity implements OnClickListener {

    private static final int REQUEST_START_IDENTIFICATION = 0;
    private static final int REQUEST_LOGIN = 1;

    private static final String SHOW_LOGIN = "show_login";

    private Progress mIdentificationProgress;

    private CheckBox mFirstCheckbox;
    private CheckBox mSecondCheckbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        if (intent.hasExtra(SHOW_LOGIN) && intent.getBooleanExtra(SHOW_LOGIN, false)){
            startActivityForResult(
                    LoginActivity.getIntent(this, mIdentificationProgress),
                    REQUEST_LOGIN
            );
        }

        setContentView(de.idvos.fastonlineidentification.sdk.R.layout.activity_start);
        setTitle(de.idvos.fastonlineidentification.sdk.R.string.idvos_start_title);
        setMenuButton(R.drawable.ic_action_helpbutton, false);

        mIdentificationProgress = new Progress();

        if (intent.hasExtra(IdvosSDK.KEY_IDENTIFICATION_HASH)) {
            mIdentificationProgress.setIdentificationHash(intent.getStringExtra(IdvosSDK.KEY_IDENTIFICATION_HASH));
        }

//		TextView terms = (TextView) findViewById(R.id.text_terms);
//		terms.setText(Html.fromHtml("<a href=\"www.google.com\">" + getString(R.string.idvos_start_terms) + "</a>"));
//		terms.setOnClickListener(this);

        findViewById(de.idvos.fastonlineidentification.sdk.R.id.button_start).setOnClickListener(this);


        String firstText = getString(
                R.string.idvos_first_checkbox,
                getString(R.string.idvos_terms_and_conditions),
                getString(R.string.idvos_privacy_policy)
        );

        Spannable firstCheckboxSpannable = Spannable.Factory.getInstance().newSpannable(firstText);
        ClickableSpan agbClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                startActivity(TermsActivity.getIntent(StartActivity.this));
            }
        };
        ClickableSpan datenschutzbestimmungenClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                startActivity(TermsActivity.getIntent(StartActivity.this).putExtra("text", 100));

//                Toast.makeText(StartActivity.this,"datenschutzbestimmungenClickableSpan",Toast.LENGTH_SHORT).show();

            }
        };
//        firstCheckboxSpannable.removeSpan(datenschutzbestimmungenClickableSpan);

        String termsAndConditionsString = getString(R.string.idvos_terms_and_conditions);
        firstCheckboxSpannable.setSpan(
                agbClickableSpan,
                firstText.indexOf(termsAndConditionsString),
                firstText.indexOf(termsAndConditionsString) + termsAndConditionsString.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        String privacyPolicyString = getString(R.string.idvos_privacy_policy);
        firstCheckboxSpannable.setSpan(
                datenschutzbestimmungenClickableSpan,
                firstText.indexOf(privacyPolicyString),
                firstText.indexOf(privacyPolicyString) + privacyPolicyString.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        mFirstCheckbox = ((CheckBox) findViewById(R.id.first_checkbox));
        mSecondCheckbox = ((CheckBox) findViewById(R.id.second_checkbox));
        mFirstCheckbox.setText(firstCheckboxSpannable);
        mFirstCheckbox.setMovementMethod(LinkMovementMethod.getInstance());

        if (mIdentificationProgress.hasIdentificationData()) {
//            mIdentificationProgress.updateState(Progress.STATE_IDENTIFICATION_RETRIEVED);
            startActivityForResult(
                    IdentificationActivity.getIntent(this, mIdentificationProgress),
                    REQUEST_START_IDENTIFICATION
            );
        }
    }

    public static Intent getIntent(Context context, boolean showLogin){
        Intent intent = new Intent(context, StartActivity.class);
        intent.putExtra(SHOW_LOGIN, showLogin);
        return intent;
    }

    @Override
    protected void onRightMenuButtonClicked() {
        startActivity(InfoActivity.getIntent(this));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.text_terms) {
            startActivity(TermsActivity.getIntent(this));
            return;
        }
        if (id == R.id.button_start) {
            if (!mFirstCheckbox.isChecked()) {
                YoYo.with(Techniques.Tada).playOn(mFirstCheckbox);
                Toast toast = Toast.makeText(StartActivity.this, R.string.idvos_agb_error, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 0, 0);
                toast.show();
            } else if (!mSecondCheckbox.isChecked()) {
                YoYo.with(Techniques.Tada).playOn(mSecondCheckbox);
                Toast toast = Toast.makeText(StartActivity.this, R.string.idvos_agb_error, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 0, 0);
                toast.show();
            } else {
                Intent intent = LoginActivity.getIntent(this, mIdentificationProgress);
                startActivityForResult(intent, REQUEST_LOGIN);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_LOGIN:
                if (resultCode == RESULT_OK) {
                    mIdentificationProgress = data.getParcelableExtra(Progress.KEY_IDENTIFICATION_PROGRESS);

                    if (mIdentificationProgress.hasIdentificationData()) {
                        Intent intent = IdentificationActivity.getIntent(this, mIdentificationProgress);
                        startActivityForResult(intent, REQUEST_START_IDENTIFICATION);
                    }
                }
                return;

            case REQUEST_START_IDENTIFICATION:
                if (resultCode == RESULT_OK) {
                    mIdentificationProgress = data.getParcelableExtra(Progress.KEY_IDENTIFICATION_PROGRESS);
                    Intent result = new Intent();
                    IdentificationResult identificationResult = new IdentificationResult(
                            mIdentificationProgress.getVerificationResult(),
                            mIdentificationProgress.getTransactionId(),
                            new WaitingTimeTracker(this).getWaitingTimeMillis()
                    );
                    result.putExtra(IdentificationResult.IDENTIFICATION_RESULT, identificationResult);
                    setResult(RESULT_OK, result);
                    finish();
                }
                if (resultCode == RESULT_CANCELED){
                    IdentificationResult identificationResult = new IdentificationResult(
                            false,
                            null,
                            new WaitingTimeTracker(this).getWaitingTimeMillis()
                    );

                    Intent result = new Intent();
                    result.putExtra(
                            IdentificationResult.IDENTIFICATION_RESULT,
                            identificationResult
                    );
                    setResult(RESULT_CANCELED, result);
                    finish();
                }
        }
    }

}
