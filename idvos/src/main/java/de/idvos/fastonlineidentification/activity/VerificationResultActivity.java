package de.idvos.fastonlineidentification.activity;

import de.idvos.fastonlineidentification.sdk.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

public class VerificationResultActivity extends Activity implements OnClickListener {

	protected static Intent getIntent(Context context, Progress progress) {
		Intent intent = new Intent(context, VerificationResultActivity.class);
		intent.putExtra(Progress.KEY_IDENTIFICATION_PROGRESS, progress);
		return intent;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(de.idvos.fastonlineidentification.sdk.R.layout.activity_verificationresult);
		
		Intent intent = getIntent();
		Progress progress = intent.getParcelableExtra(Progress.KEY_IDENTIFICATION_PROGRESS);
		
		((TextView) findViewById(R.id.text_result)).setText(
				progress.getVerificationResult()?R.string.idvos_verificationresult_success:R.string.idvos_verificationresult_failure
				);
		findViewById(R.id.button_confirm).setOnClickListener(this);


        ((ImageView)findViewById(R.id.image)).setImageResource(progress.getVerificationResult() ? R.drawable.confirmation : R.drawable.confirmationfailed);
	}
	
	@Override
	public void onClick(View v) {
        onBackPressed();
//		finish();
	}
	
}
