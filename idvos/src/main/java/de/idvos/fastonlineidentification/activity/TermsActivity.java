package de.idvos.fastonlineidentification.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

import java.util.Locale;

import de.idvos.fastonlineidentification.sdk.R;

public class TermsActivity extends BaseActivity {

    private static final String TERMS_FILE_NAME = "bitten";
    private static final String PRIVACY_FILE_NAME = "daten";
    private static final String FILE_ANDROID_ASSET = "file:///android_asset/";
    private static final String HTML = ".html";
    private static final String DEFAULT_LOCALE_CODE = "en";


    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context, TermsActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);
        setMenuButton(R.drawable.ic_action_bac, true);

        String localeCode = Locale.getDefault().getLanguage();
        String languagePostfix = localeCode.equals(DEFAULT_LOCALE_CODE)
                ? ""
                : "-" + localeCode;

        String urlTerms = FILE_ANDROID_ASSET + TERMS_FILE_NAME + languagePostfix + HTML;
        String urlPrivacy = FILE_ANDROID_ASSET + PRIVACY_FILE_NAME + languagePostfix + HTML;

        if (getIntent().getIntExtra("text", -1) > 0) {
            ((WebView) findViewById(R.id.webview)).loadUrl(urlPrivacy);
            setTitle(getString(R.string.idvos_privacy_title));
        } else {
            ((WebView) findViewById(R.id.webview)).loadUrl(urlTerms);
            setTitle(getString(R.string.idvos_terms_and_conditions));
        }


    }

    @Override
    protected void onLeftMenuButtonClicked() {
        onBackPressed();
    }

}
