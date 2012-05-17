package com.slimming.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class ViewModelBinder {
	
	private View view;
	private Object model;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	private Map<View, String> bindings;
	
	public ViewModelBinder(View view, Object model) {
		this.view = view;
		this.model = model;
		this.bindings = new HashMap<View, String>();
	}

	public ViewModelBinder bind(int viewId, String attribute) {
		View target = view.findViewById(viewId);
		if (target != null) {
			bindings.put(target,  attribute);
		}
		return this;
	}
	
	public void updateView() {
		for (Entry<View, String> entry : bindings.entrySet()) {
			View view = entry.getKey();
			String value = getModelValue(entry.getValue());

			if (view.getClass() == EditText.class) {
				((EditText)view).setText(value);
			} else if (view.getClass() == TextView.class) {
				((TextView)view).setText(value);
			} else { 
				try {
					Method method = view.getClass().getMethod("setText", String.class);
					method.invoke(view,  value);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public <MODEL> MODEL updateModel() {
		for (Entry<View, String> entry : bindings.entrySet()) {
			View view = entry.getKey();
			String value = "";
			
			if (view.getClass() == EditText.class) {
				value = ((TextView) view).getText().toString();
			}
			setModelValue(entry.getValue(), value);
		}
		
		return (MODEL) model;
	}
	
	public Object setModelValue(String attribute, String value) {
		
		Object valueTyped = null;
		
		try {
			Field field = model.getClass().getDeclaredField(attribute);
			Type fieldType = field.getType();
			
			if (fieldType == Date.class) {
				valueTyped = dateFormat.parse(value);
			} else if (fieldType == Double.class) {
				valueTyped = Double.parseDouble(value);
			} else {
				valueTyped = value;
			}
			field.set(model, valueTyped);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return valueTyped;
	}
	
	public String getModelValue(String attribute) {
		
		String value = "";
		
		try {
			Field field = model.getClass().getDeclaredField(attribute);
			Type fieldType = field.getType();
			Object rawValue = field.get(model);
			
			if (fieldType == Date.class) {
				value = dateFormat.format((Date) rawValue);
			} else if (fieldType == Double.class) {
				value = Double.toString((Double) rawValue);
			} else {
				value = rawValue.toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return value;
	}
	
}
