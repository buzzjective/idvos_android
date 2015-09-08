package de.idvos.fastonlineidentification.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import de.idvos.fastonlineidentification.sdk.R;

public class BaseActivity extends FragmentActivity {

    private TextView mTextTitle;
    private ImageView mButtonLeft;
    private ImageView mButtonRight;

    private ViewGroup mContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_base);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mTextTitle = (TextView) findViewById(R.id.tabbar_title);
        mButtonLeft = (ImageView) findViewById(R.id.tabbar_button_left);
        mButtonRight = (ImageView) findViewById(R.id.tabbar_button_right);
        mContent = (ViewGroup) findViewById(R.id.content);

        mTextTitle.setText(getTitle());

        mButtonLeft.setOnClickListener(mMenuButtonListener);
        mButtonRight.setOnClickListener(mMenuButtonListener);

        wl = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "idvosLock");

        holdWakeLock();
    }


    @Override
    protected void onDestroy() {
        releaseWakeLock();
        super.onDestroy();
    }

    @Override
    public void setContentView(int layoutResID) {
        mContent.removeAllViews();
        getLayoutInflater().inflate(layoutResID, mContent);
    }

    @Override
    public void setTitle(int titleId) {
        super.setTitle(titleId);
        mTextTitle.setText(titleId);
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        mTextTitle.setText(title);
    }

    protected void setMenuButton(int iconResId, boolean isLeft) {
        ImageView button = isLeft ? mButtonLeft : mButtonRight;

        button.setImageResource(iconResId);
        button.setVisibility(iconResId == 0 ? View.INVISIBLE : View.VISIBLE);
    }

    private PowerManager.WakeLock wl;

    protected void holdWakeLock() {
        try {
            wl.acquire();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected void releaseWakeLock() {
        try {
            if (wl.isHeld())
                wl.release();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected void onLeftMenuButtonClicked() {

    }

    protected void onRightMenuButtonClicked() {

    }

    private OnClickListener mMenuButtonListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.tabbar_button_left) {
                onLeftMenuButtonClicked();
                return;
            }
            if (id == R.id.tabbar_button_right) {
                onRightMenuButtonClicked();
            }
        }
    };

}
