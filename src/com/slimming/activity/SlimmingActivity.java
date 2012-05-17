package com.slimming.activity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.orman.dbms.sqliteandroid.SQLiteAndroid;
import org.orman.mapper.MappingSession;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.slimming.model.Weight;
import com.slimming.util.ViewModelBinder;

public class SlimmingActivity extends Activity {
	
	public static final int HANDLE_WEIGHT = 0x1;
	public static final String DATABASE_NAME = "slimming";
	private static final int CONTEXT_MENU_EDITAR = 0x1;
	private static final int CONTEXT_MENU_EXCLUIR = 0x2;
	
	@Override
	@SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        if (!MappingSession.isSessionStarted()) {
            MappingSession.registerDatabase(new SQLiteAndroid(this, DATABASE_NAME));
            MappingSession.registerEntity(Weight.class);
        	MappingSession.start();
        }
        
        ((Button)findViewById(R.id.btnSetup)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(SlimmingActivity.this, PreferencesActivity.class);
				startActivity(intent);
			}
		});

        ((Button)findViewById(R.id.btnAddWeight)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(SlimmingActivity.this, WeightActivity.class);
				intent.setAction(WeightActivity.ACTION_NEW);
				startActivityForResult(intent, HANDLE_WEIGHT);
				overridePendingTransition(R.anim.slide_right_to_left_enter, R.anim.slide_right_to_left_leave);	
			}
		});

		((Button)findViewById(R.id.btnShowGraphic)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
			    Intent intent = ChartFactory.getTimeChartIntent(SlimmingActivity.this, getWeightsDataset(), getWeightsRenderer(), null);
			    startActivity(intent);
				overridePendingTransition(R.anim.slide_right_to_left_enter, R.anim.slide_right_to_left_leave);
			}
		});
        
        populateWeights();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	if (resultCode == HANDLE_WEIGHT) {
    		populateWeights();
    	}
    }
    
    protected void populateWeights() {
    	List<Weight> weights = Weight.all();
    	((ListView)findViewById(R.id.lstWeights)).setAdapter(new WeightListAdapter(this, weights));
    	((Button)findViewById(R.id.btnShowGraphic)).setClickable(!weights.isEmpty());
    }
    
    protected class WeightListAdapter extends ArrayAdapter<Weight> {

    	private Context context;
    	private List<Weight> items;
    	
    	public WeightListAdapter(Context context, List<Weight> items) {
    		super(context, R.layout.weight_item, items);
    		this.context = context;
    		this.items = items;
    	}

		public View getView(final int i, View view, ViewGroup group) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View row = inflater.inflate(R.layout.weight_item, group, false);
			registerForContextMenu(row);
			
			Weight weight = items.get(i);
			row.setId(weight.id);

			ViewModelBinder binder = new ViewModelBinder(row, weight);
			binder.bind(R.id.txtDate, "date");
			binder.bind(R.id.txtValue, "value");
			binder.updateView();
			
			return row;
		}
    }
    
    @Override
    public boolean onContextItemSelected(final MenuItem item) {
    	if (item.getGroupId() == CONTEXT_MENU_EDITAR) {
			Intent intent = new Intent(SlimmingActivity.this, WeightActivity.class);
			intent.setAction(WeightActivity.ACTION_EDIT);
			intent.putExtra("id", item.getItemId());
			startActivityForResult(intent, HANDLE_WEIGHT);
			overridePendingTransition(R.anim.slide_right_to_left_enter, R.anim.slide_right_to_left_leave);
			
    	} else if (item.getGroupId() == CONTEXT_MENU_EXCLUIR) {
    		new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle("Tem certeza?")
            .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            	public void onClick(DialogInterface dialog, int which) {
            		Weight weight = Weight.find(item.getItemId());
            		weight.delete();
            		populateWeights();
            	}
            })
            .setNegativeButton("Não", null)
            .show();
    		
    	} else {
    		return false;
    	}
    	return true;
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	menu.add(CONTEXT_MENU_EDITAR, v.getId(), 0, "Editar");
    	menu.add(CONTEXT_MENU_EXCLUIR, v.getId(), 1, "Apagar");
    }
    
	private XYMultipleSeriesDataset getWeightsDataset() {
		
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		
		TimeSeries weightSeries = new TimeSeries("Peso x dia");
		List<Weight> weights = Weight.all();
		for (Weight weight : weights) {
			weightSeries.add(weight.date, weight.value);
		}
		dataset.addSeries(weightSeries);
		
		Weight firstWeight = weights.get(0);
		Weight lastWeight = weights.get(weights.size()-1);
		try {
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
			Double wishedWeight = Double.parseDouble(preferences.getString("wished_weight", "null"));
			Date wishedDate = new SimpleDateFormat("yyyy-MM-dd").parse(preferences.getString("wished_date", "2000-01-01"));
			lastWeight = new Weight(null, wishedDate, wishedWeight, "Ideal");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		TimeSeries baseSeries = new TimeSeries("Evolução");
		baseSeries.add(firstWeight.date, firstWeight.value);
		baseSeries.add(lastWeight.date, lastWeight.value);
		dataset.addSeries(baseSeries);
		
		return dataset;
	}

	private XYMultipleSeriesRenderer getWeightsRenderer() {
		
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		renderer.setAxisTitleTextSize(16);
		renderer.setChartTitleTextSize(20);
		renderer.setShowLegend(false);
		renderer.setLabelsTextSize(10);
		renderer.setPointSize(5f);
		renderer.setMargins(new int[] { 0, 20, 0, 0});
		renderer.setAxesColor(Color.DKGRAY);
		renderer.setLabelsColor(Color.LTGRAY);
		
		XYSeriesRenderer r = new XYSeriesRenderer();
		r.setColor(Color.BLUE);
		r.setPointStyle(PointStyle.CIRCLE);
		r.setFillBelowLine(true);
		r.setFillBelowLineColor(Color.WHITE);
		r.setFillPoints(true);
		r.setDisplayChartValues(true);
		r.setChartValuesTextSize(15f);
		renderer.addSeriesRenderer(r);
		
		r = new XYSeriesRenderer();
		r.setPointStyle(PointStyle.POINT);
		r.setColor(Color.RED);
		r.setFillPoints(true);
		renderer.addSeriesRenderer(r);
		
		return renderer;
	}    
}