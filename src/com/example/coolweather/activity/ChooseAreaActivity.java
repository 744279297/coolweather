package com.example.coolweather.activity;

import java.util.ArrayList;
import java.util.List;

import com.example.coolweather.R;
import com.example.coolweather.db.CoolWeatherDB;
import com.example.coolweather.model.City;
import com.example.coolweather.model.County;
import com.example.coolweather.model.Province;
import com.example.coolweather.util.HttpCallbackListener;
import com.example.coolweather.util.HttpUntil;
import com.example.coolweather.util.Utility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends ActionBarActivity {
	
	public static final int LEVEL_PROVINCE=0;
	public static final int LEVEL_CITY=1;
	public static final int LEVEL_COUNTY=2;
	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private CoolWeatherDB coolWeatherDB;
	private List<String> dataList=new ArrayList<String>();
	private List<Province> provinceList;
	private List<City> cityList;
	private List<County> countyList;
	private Province selectedProvince;
	private City selectedCity;
	private int currentLevel;
	private boolean isFromWeatherActivity;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		isFromWeatherActivity=getIntent().getBooleanExtra("from_weather_activity", false);
		SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
		if(prefs.getBoolean("city_selected", false)&&!isFromWeatherActivity){
			Intent intent=new Intent(this,WeatherActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		listView=(ListView)findViewById(R.id.list_view);
		titleText=(TextView)findViewById(R.id.title_text);
		adapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,dataList);
		listView.setAdapter(adapter);
		coolWeatherDB=CoolWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int index,
					long arg3) {
				if (currentLevel==LEVEL_PROVINCE) {				
					selectedProvince=provinceList.get(index);
					queryCities();
				}else if(currentLevel==LEVEL_CITY){
					selectedCity=cityList.get(index);	
					queryCounties();
				}else if (currentLevel==LEVEL_COUNTY) {
					String countyCode=countyList.get(index).getCountyCode();
					Intent intent=new Intent
							(ChooseAreaActivity.this,WeatherActivity.class);
					intent.putExtra("county_code", countyCode);
					startActivity(intent);
					finish();
				}	
			}	
		});
		queryProvince();
	}
	private void queryProvince() {
		provinceList=coolWeatherDB.loadProvince();
		
		if(provinceList.size()>0){
			dataList.clear();
			for(Province province:provinceList){
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("中国");
			currentLevel=LEVEL_PROVINCE;
		}else{
			queryFromServer(null,"province");
		}
		
	}


	private void queryCounties() {
		countyList=coolWeatherDB.loadCounty(selectedCity.getId());
		if(countyList.size()>0){
			dataList.clear();
			for(County county:countyList){
				dataList.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLevel=LEVEL_COUNTY;
		}else{
			queryFromServer(selectedCity.getCityCode(),"county");
		}
		
	}

	private void queryCities() {
		Log.d("onclick", selectedProvince.getProvinceCode());
		cityList=coolWeatherDB.loadCity(selectedProvince.getId());
		if(cityList.size()>0){
			dataList.clear();
			for(City city:cityList){
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel=LEVEL_CITY;
		}else{
			queryFromServer(selectedProvince.getProvinceCode(),"city");
			
		}
		
	}

	private void queryFromServer(final String code, final String type) {
		String address;
		if(!TextUtils.isEmpty(code)){
			address="http://www.weather.com.cn/data/list3/city"+code+".xml";
		}else {
			address="http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		HttpUntil.sendHttpRequest(address, new HttpCallbackListener() {	
			@Override		
			public void onFinish(String response) {
				boolean result=false;
				if("province".equals(type)){
					result=Utility.handleProvinceResponse(coolWeatherDB, response);
				}else if("city".equals(type)){
					result=Utility.handleCityResponse(coolWeatherDB, response, selectedProvince.getId());
				}else if("county".equals(type)){
					result=Utility.handleCountyResponse(coolWeatherDB, response, selectedCity.getId());
				}
				if(result){
					runOnUiThread(new Runnable() {
						public void run() {
							closeProgressDialog();
							if("province".equals(type)){
								queryProvince();
							}else if("city".equals(type)){
								queryCities();
							}else if ("county".equals(type)) {
								queryCounties();
							}
						}
					});
				}
				
			}
			
			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT);
					}
				});
				
			}
		});
	}
	private void showProgressDialog() {
		if(progressDialog==null){
			progressDialog=new ProgressDialog(this);
			progressDialog.setMessage("正在加载");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	private void closeProgressDialog() {
		
		if(progressDialog!=null){
			progressDialog.dismiss();
		}
	}
	@Override
	public void onBackPressed() {
		if(currentLevel==LEVEL_COUNTY){
			queryCities();
		}else if (currentLevel==LEVEL_CITY) {
			queryProvince();	
		}else {
			if (isFromWeatherActivity) {
				Intent intent=new Intent(this,WeatherActivity.class);
				startActivity(intent);
			}
			finish();
		}
	}
}
