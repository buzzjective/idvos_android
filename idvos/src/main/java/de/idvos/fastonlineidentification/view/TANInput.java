package de.idvos.fastonlineidentification.view;

import de.idvos.fastonlineidentification.sdk.R;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

public class TANInput extends FrameLayout {

	private static final String TAG = "TANInput";
	
	private static final int TAN_UNKNOWN_TEXT_COLOR = 0xff333333;
	private static final int TAN_UNKNOWN_BACKGROUND = 0x33333333;
	private static final int TAN_VALID_TEXT_COLOR = 0xff00aa33;
	private static final int TAN_VALID_BACKGROUND = 0x3300aa33;
	private static final int TAN_INVALID_TEXT_COLOR = 0xffaa3300;
	private static final int TAN_INVALID_BACKGROUND = 0x33aa3300;


    private int failTime = 0;
	public static interface OnCheckTANListener {
		
		public void onCheckTAN(String tan);
		
	}
	
	private EditText[] mDigits;
	private Button mTextStatus;
	private View mBackground;
	
	private boolean mIsTanValid = false;
	
	private OnCheckTANListener mOnCheckTANListener = null;
	
	public TANInput(Context context) {
		super(context);
		initialize(context);
	}

	public TANInput(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize(context);
	}


    public int getFailTime() {
        return failTime;
    }

    public void setFailTime(int failTime) {
        this.failTime = failTime;
    }

    public TANInput(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initialize(context);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public TANInput(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		initialize(context);
	}
	
	private void initialize(Context context) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.view_taninput, this, true);
		
		mDigits = new EditText[5];
		mDigits[0] = (EditText) findViewById(de.idvos.fastonlineidentification.sdk.R.id.digit_1);
		mDigits[1] = (EditText) findViewById(R.id.digit_2);
		mDigits[2] = (EditText) findViewById(R.id.digit_3);
		mDigits[3] = (EditText) findViewById(R.id.digit_4);
		mDigits[4] = (EditText) findViewById(R.id.digit_5);
		
		mTextStatus = (Button) findViewById(R.id.text_status);
		mTextStatus.setOnClickListener(mStatusClickListener);
		
		mBackground = findViewById(R.id.tan_background);
		
		for (int i=0; i<mDigits.length - 1; i++) {
			EditText digit = mDigits[i];
			digit.addTextChangedListener(new DigitWatcher(mDigits[i + 1]));
            final int currentIndex = i;
            if(i != 0)
                digit.setOnKeyListener(new OnKeyListener() {
                    @Override
                    public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                        if(keyCode == 67 && ((EditText)view).getText().toString().equals("")){
                            mDigits[currentIndex - 1].requestFocus();
                            return true;

                        }
                        return false;
                    }
                });

		}
		mDigits[mDigits.length - 1].addTextChangedListener(new DigitWatcher(null));
		mDigits[mDigits.length - 1].setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (keyCode == 67 && ((EditText) view).getText().toString().equals("")) {
                    mDigits[mDigits.length - 2].requestFocus();
                    return true;

                }
                return false;
            }
        });

		updateStatusText();
	}
	
	public void setOnCheckTANListener(OnCheckTANListener listener) {
		mOnCheckTANListener = listener;
	}
	
	public void setTanValidity(boolean isValid, boolean failed) {
        Log.e("setTanValidity","isvalid:" + isValid + ", failed:" + failed);
		if (!failed) {
			mBackground.setBackgroundColor(isValid?TAN_VALID_BACKGROUND:TAN_INVALID_BACKGROUND);
			mTextStatus.setTextColor(isValid?TAN_VALID_TEXT_COLOR:TAN_INVALID_TEXT_COLOR);
			mTextStatus.setText(isValid?R.string.taninput_valid:R.string.taninput_invalid);
//			mTextStatus.setEnabled(true);

            if(!isValid) {
                failTime++;
                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setInputEnabled(true);
                        mTextStatus.setText(R.string.taninput_invalid);
                    }
                }, 5000);
            }
		}
		else {
//			mBackground.setBackgroundColor(TAN_UNKNOWN_BACKGROUND);
			mTextStatus.setTextColor(TAN_UNKNOWN_TEXT_COLOR);
			mTextStatus.setText(R.string.taninput_failed);
			mTextStatus.setEnabled(true);
		}
		setInputEnabled(failed);
	}
	
	public void setLoading() {
		setInputEnabled(false);
		mBackground.setBackgroundColor(TAN_UNKNOWN_BACKGROUND);
		mTextStatus.setTextColor(TAN_UNKNOWN_TEXT_COLOR);
		mTextStatus.setText(R.string.taninput_verifying);
		mTextStatus.setEnabled(false);
	}
	
	public void clean() {
		for (EditText digit: mDigits) {
			digit.setText("");
		}
	}
	
	public String getTAN() {
		if (mIsTanValid) {
			String tan = "";
			for (EditText digit: mDigits) {
				tan = tan.concat(digit.getText().toString());
			}
			return tan;
		}
		else {
			return null;
		}
	}
	
	private void setInputEnabled(boolean enabled) {
		for (EditText digit: mDigits) {
			digit.setEnabled(enabled);
		}
		mTextStatus.setEnabled(enabled);
	}
	
	private OnClickListener mStatusClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			String tan = getTAN();
			if (mOnCheckTANListener!=null && tan!=null) {
				mOnCheckTANListener.onCheckTAN(tan);
			}
			else if (tan==null) {
				Log.wtf(TAG, "TAN request clicked but is null!!");
			}
		}
		
	};
	
	private Runnable mTANValidator = new Runnable() {
		
		@Override
		public void run() {
			mIsTanValid = true;
			
			for (EditText digit: mDigits) {
				mIsTanValid &= digit.getText().length()==1;
			}
			
			updateStatusText();
		}
		
	};
	
	private void updateStatusText() {
		mTextStatus.setEnabled(mIsTanValid);
		mTextStatus.setText(mIsTanValid?getResources().getString(R.string.taninput_verify):" ");
		
		if (!mIsTanValid) {
			mTextStatus.setTextColor(TAN_UNKNOWN_TEXT_COLOR);
			mBackground.setBackgroundColor(TAN_UNKNOWN_BACKGROUND);
		}
	}


	private class DigitWatcher implements TextWatcher {

		private EditText mNextDigit = null;
		
		public DigitWatcher(EditText nextDigit) {
			mNextDigit = nextDigit;
		}
		
		@Override
		public void afterTextChanged(Editable s) {
			if (s.length()==1 && mNextDigit!=null) {
				mNextDigit.requestFocus();
			}
			
			if (s.length()>1) {
				s.delete(1, s.length());
			}
			
			postDelayed(mTANValidator, 10);
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) { }
		
	}

}
