package de.idvos.fastonlineidentification.sdk;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.crashlytics.android.Crashlytics;

import de.idvos.fastonlineidentification.activity.StartActivity;
import de.idvos.fastonlineidentification.config.AppConfig;

/**
 * Idvos SDK. Use to start identification.
 * You have to call {@link #initialize(Context, Mode)} before using the SDK.
 */
public class IdvosSDK {

    public static final String KEY_IDENTIFICATION_HASH = "identification_hash";

    private static final IdvosSDK INSTANCE = new IdvosSDK();

    private static Mode mode = null;

    private IdvosSDK() {
    }

    /**
     * @return instance of SDK. Must be called after {@link #initialize(Context, Mode)}
     */
    public static IdvosSDK getInstance() {
        if (mode == null) {
            throw new IllegalStateException("SDK is not initialized");
        }
        return INSTANCE;
    }

    /**
     * Initializes the SDK. Must be called only once, usually from {@link Application#onCreate()}
     *
     * @param mode - mode in which SDK will work
     */
    public static void initialize(Context context, Mode mode) {
        if (IdvosSDK.mode != null) {
            throw new IllegalStateException("SDK was already initialized");
        }

        if (mode == null) {
            throw new NullPointerException("Mode can't be null");
        }

        IdvosSDK.mode = mode;

        Crashlytics.start(context);
        AppConfig.initialize(context);
    }

    /**
     * Starts the identification progress. Result will be returned in {@link Activity#onActivityResult(int, int, Intent)}
     *
     * @param activity    The {@link Activity} starting the identification.
     * @param requestCode The request code for {@link Activity#onActivityResult(int, int, Intent)}
     * @param hash        The identification hash.
     */
    public void startIdentification(Activity activity, int requestCode, String hash) {
        Intent intent = new Intent(activity, StartActivity.class);
        intent.putExtra(KEY_IDENTIFICATION_HASH, hash);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * Starts the identification progress. Result will be returned in {@link android.app.Fragment#onActivityResult(int,
     * int, Intent) onActivityResult(int, int, Intent)}
     *
     * @param fragment    The {@link android.app.Fragment Fragment} starting the identification.
     * @param requestCode The request code for {@link android.app.Fragment#onActivityResult(int, int, Intent)
     *                    onActivityResult(int, int, Intent)}
     * @param hash        The identification hash.
     */
    @TargetApi(11)
    public void startIdentification(android.app.Fragment fragment, int requestCode, String hash) {
        Intent intent = new Intent(fragment.getActivity(), StartActivity.class);
        intent.putExtra(KEY_IDENTIFICATION_HASH, hash);
        fragment.startActivityForResult(intent, requestCode);
    }

    /**
     * Starts the identification progress. Result will be returned in {@link android.support.v4.app.Fragment#onActivityResult(
     * int, int, Intent) onActivityResult(int, int, Intent)}
     *
     * @param fragment    The {@link android.support.v4.app.Fragment Fragment} starting the identification.
     * @param requestCode The request code for {@link android.support.v4.app.Fragment#onActivityResult(int, int, Intent)
     *                    onActivityResult(int, int, Intent)}
     * @param hash        The identification hash.
     */
    public void startIdentification(android.support.v4.app.Fragment fragment, int requestCode, String hash) {
        Intent intent = new Intent(fragment.getActivity(), StartActivity.class);
        intent.putExtra(KEY_IDENTIFICATION_HASH, hash);
        fragment.startActivityForResult(intent, requestCode);
    }

    /**
     * @return current mode
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * Configuration for idvos SDK. Defines endpoints, keys, etc.
     */
    public enum Mode {
        PRODUCTION(
                "https://idvos.de/",
                "1e89dbf47e7ba14986f4",
                "44880212"
        ),
        TEST(
                "https://integration.idvos.de/",
                "18573e2f3ef8feec6159",
                "45083572"
        );

        private final String endpoint;
        private final String publicApiKey;
        private final String tokBoxApiKey;

        Mode(String endpoint, String publicApiKey, String tokBoxApiKey) {
            this.endpoint = endpoint;
            this.publicApiKey = publicApiKey;
            this.tokBoxApiKey = tokBoxApiKey;
        }

        /**
         * @return endpoint-url
         */
        public String getEndpoint() {
            return endpoint;
        }

        /**
         * @return public api-key
         */
        public String getPublicApiKey() {
            return publicApiKey;
        }

        /**
         * @return TokBox api-key
         */
        public String getTokBoxApiKey() {
            return tokBoxApiKey;
        }
    }
}
