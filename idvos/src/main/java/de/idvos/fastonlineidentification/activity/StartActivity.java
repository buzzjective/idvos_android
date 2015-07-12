package de.idvos.fastonlineidentification.activity;

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

import de.idvos.fastonlineidentification.R;
import de.idvos.fastonlineidentification.sdk.IdentificationResult;
import de.idvos.fastonlineidentification.sdk.IdvosFOI;
import de.idvos.fastonlineidentification.sdk.IdvosSDK;

public class StartActivity extends BaseActivity implements OnClickListener {

    private static final int REQUEST_START_IDENTIFICATION = 0;
    private static final int REQUEST_LOGIN = 1;

    private Progress mIdentificationProgress;

    private CheckBox mFirstCheckbox;
    private CheckBox mSecondCheckbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        setTitle(R.string.start_title);
        setMenuButton(R.drawable.ic_action_helpbutton, false);

        Intent intent = getIntent();
        mIdentificationProgress = new Progress();

        if (intent.hasExtra(IdvosSDK.KEY_IDENTIFICATION_HASH)) {
            mIdentificationProgress.setIdentificationHash(intent.getStringExtra(IdvosFOI.KEY_IDENTIFICATION_HASH));
        }

//		TextView terms = (TextView) findViewById(R.id.text_terms);
//		terms.setText(Html.fromHtml("<a href=\"www.google.com\">" + getString(R.string.start_terms) + "</a>"));
//		terms.setOnClickListener(this);

        findViewById(R.id.button_start).setOnClickListener(this);


        String firstText = getString(R.string.first_checkbox);
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

        firstCheckboxSpannable.setSpan(agbClickableSpan, firstText.indexOf("AGB"), firstText.indexOf("AGB") + 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        firstCheckboxSpannable.setSpan(datenschutzbestimmungenClickableSpan, firstText.indexOf("Datenschutzbestimmungen"), firstText.indexOf("Datenschutzbestimmungen") + "Datenschutzbestimmungen".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

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
                Toast toast = Toast.makeText(StartActivity.this, R.string.agb_error, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 0, 0);
                toast.show();
            } else if (!mSecondCheckbox.isChecked()) {
                YoYo.with(Techniques.Tada).playOn(mSecondCheckbox);
                Toast toast = Toast.makeText(StartActivity.this, R.string.agb_error, Toast.LENGTH_LONG);
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
                            mIdentificationProgress.getTransactionId()
                    );
                    result.putExtra(IdentificationResult.IDENTIFICATION_RESULT, identificationResult);
                    setResult(RESULT_OK, result);
                    finish();
                }
                if (resultCode == RESULT_CANCELED){
                    finish();
                }
                return;
        }
    }

}
