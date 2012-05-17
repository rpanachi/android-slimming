package com.slimming.widget;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.DatePicker;

public class DatePickerPreference extends DialogPreference implements
    DatePicker.OnDateChangedListener {
	
    private Date mDate;
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    private static final String DEFAULT_VALUE = "null";
	
	@Override
    protected View onCreateDialogView() {
        DatePicker picker = new DatePicker(getContext());
        mDate = getDate();
                
        Calendar calendar = Calendar.getInstance();
        if (mDate != null) {
        	calendar.setTime(mDate);
        }
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        picker.init(year, month, day, this);
        return picker;
    }

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		if (positiveResult) {
			setDate(mDate == null ? new Date() : mDate);
			updateSummary();
		}
	}
	
    public void onDateChanged(DatePicker view, int year, int monthOfYear,  
            int dayOfMonth) {
        mDate = (new Date(year - 1900, monthOfYear, dayOfMonth));
    }
    
    public DatePickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DatePickerPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    
    public Date getDate() {
    	try {
    		return formatter.parse(getPersistedString(DEFAULT_VALUE));
    	} catch (Exception ex) {
    		return null;
    	}
    }
    
    public void setDate(Date date) {
    	Calendar now = Calendar.getInstance();
    	Calendar newDate = Calendar.getInstance();
    	newDate.setTime(date);
    	
    	if (newDate.after(now)) {
    		persistString(formatter.format(date));
    	}
    }
    
    public void updateSummary() {
    	Date date = getDate();
    	if (date != null) {
    		SimpleDateFormat summaryFormatter = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy");
    		setSummary(summaryFormatter.format(date));
    	}
    }
    
    @Override
    public CharSequence getSummary() {
    	updateSummary();
		return super.getSummary();
    }
    
    public void init() {
    	setPersistent(true);
    }
}
