package de.idvos.fastonlineidentification.activity;

import de.idvos.fastonlineidentification.sdk.R;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

public class TermsActivity extends BaseActivity {

	public static Intent getIntent(Context context) {
		Intent intent = new Intent(context, TermsActivity.class);
		return intent;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_terms);
		setMenuButton(R.drawable.ic_action_bac, true);

        if(getIntent().getIntExtra("text",-1) > 0){
            ((WebView)findViewById(R.id.webview)).loadUrl("file:///android_asset/daten.html");
            setTitle(getString(R.string.privacy_title));
        }else{
            setTitle(getString(R.string.terms_and_conditions));
            ((WebView)findViewById(R.id.webview)).loadUrl("file:///android_asset/bitten.html");
        }


	}
	
	@Override
	protected void onLeftMenuButtonClicked() {
		onBackPressed();
	}
	
}
