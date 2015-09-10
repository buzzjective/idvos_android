package de.idvos.fastonlineidentification.view;

import android.content.Context;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.CheckBox;

/**
 * CheckBox which changes its content after resuming parent activity
 */
public class NoPersistentTextCheckBox  extends CheckBox{

    public NoPersistentTextCheckBox(Context context) {
        super(context);
    }

    public NoPersistentTextCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NoPersistentTextCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        final CharSequence newText = getText();
        super.onRestoreInstanceState(state);
        setText(newText);
    }
}
