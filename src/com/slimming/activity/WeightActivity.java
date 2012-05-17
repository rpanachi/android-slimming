package com.slimming.activity;

import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import com.slimming.model.Weight;
import com.slimming.util.ViewModelBinder;

public class WeightActivity extends Activity {
	
	public static final String ACTION_NEW = "new";
	public static final String ACTION_EDIT = "edit";

	private Weight weight;
	private ViewModelBinder binder;

	private static final int DATE_PICKER_DIALOG = 0x1;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.weight);
		
		weight = getWeight();
		
        binder = new ViewModelBinder(this.getWindow().getDecorView(), weight);
        binder.bind(R.id.txtDate, "date");
        binder.bind(R.id.txtValue, "value");
        binder.bind(R.id.txtComments, "comments");
		
		findViewById(R.id.btnPickDate).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(DATE_PICKER_DIALOG);
			}
		});
		findViewById(R.id.btnConfirm).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (saveWeight()) {
					Intent data = new Intent(WeightActivity.this, SlimmingActivity.class);
					setResult(SlimmingActivity.HANDLE_WEIGHT, data);
					finish();
			    	overridePendingTransition(R.anim.slide_left_to_right_enter, R.anim.slide_left_to_right_leave);
				}
			}
		});
		
		updateView();
	}
	
    protected Dialog onCreateDialog(int id) {
    	if (id == DATE_PICKER_DIALOG) {
			return new DatePickerDialog(WeightActivity.this,new DatePickerDialog.OnDateSetListener() {
		        public void onDateSet(DatePicker view, int year, int month, int day) {
		        	weight.date = new Date(year - 1900, month, day);
		        	updateView();
		        }
		    }, weight.date.getYear() + 1900, weight.date.getMonth(), weight.date.getDate());
    	}
    	return null;
    }
	
    protected void updateView() {
    	binder.updateView();
		((TextView)findViewById(R.id.lblHeader)).setText(weight.isNewRecord() ? "Editando peso" : "Novo peso");
    }
    
    protected boolean saveWeight() {
    	weight = binder.updateModel();
    	
    	if (!weight.isValid()) {
			
			AlertDialog alert = new AlertDialog.Builder(this).create();
			alert.setTitle("Oops!");
			alert.setMessage(weight.getErrors().toString());
			alert.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new AlertDialog.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) { 
					dialog.cancel();
				}
			});
			
			alert.show();
			return false;
			
    	} else {
    		
    		weight.insertOrUpdate();
    		return true;
    	}
    }
    
    protected Weight getWeight() {
		Intent intent 	= getIntent();
		String action 	= intent.getAction();
		
		if (ACTION_EDIT.equals(action)) {
			int id = intent.getIntExtra("id", 0);
			weight = Weight.find(id);
			
		} else if (ACTION_NEW.endsWith(action)) {
			weight = new Weight();
			weight.date = new Date();
		}
		return weight;
    }
    
    @Override
    public void onBackPressed() {
    	super.onBackPressed();
    	overridePendingTransition(R.anim.slide_left_to_right_enter, R.anim.slide_left_to_right_leave);
    }
}