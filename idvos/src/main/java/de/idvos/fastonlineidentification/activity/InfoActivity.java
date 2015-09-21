package de.idvos.fastonlineidentification.activity;

import de.idvos.fastonlineidentification.sdk.R;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class InfoActivity extends BaseActivity implements OnClickListener {

	public static Intent getIntent(Context context) {
		Intent intent = new Intent(context, InfoActivity.class);
		return intent;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_info);
		setMenuButton(R.drawable.ic_action_bac, true);
		
		findViewById(R.id.button_call).setOnClickListener(this);
//		findViewById(R.id.button_facebook).setOnClickListener(this);
//		findViewById(R.id.button_twitter).setOnClickListener(this);
//		findViewById(R.id.button_linkedin).setOnClickListener(this);
	}

	@Override
	protected void onLeftMenuButtonClicked() {
		onBackPressed();
	}
	
	@Override
	public void onClick(View v) {
		int id = v.getId();
		if(id == R.id.button_call) {
			startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(getString(R.string.idvos_phone))));
		}
	}
}
