package com.slimming.widget;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.widget.EditText;

public class EditTextPreference extends android.preference.EditTextPreference {

	public EditTextPreference(Context context) {
		super(context);
	}
	
	public EditTextPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		if (positiveResult) {
			updateSummary();
		}
	}
	
	public void updateSummary() {
		String text = getText();
		if (text != null && !"".equals(text.toString().trim())) {
			setSummary(text + " quilos");
		}
	}
	
	@Override
	public void setText(String text) {
		try {
			double value = Double.parseDouble(text);
			if (value > 0 && value < 200) {
				super.setText(text);
			}
		} catch (Exception e) {
			super.setText(getText());
		}
	}
	
	@Override
	public CharSequence getSummary() {
		updateSummary();
		return super.getSummary();
	}

	@Override
	public EditText getEditText() {
		EditText mEditText = super.getEditText();
		mEditText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
		return mEditText;
	}
}
