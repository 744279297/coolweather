package com.example.coolweather.activity;

import com.example.coolweather.R;

import com.example.coolweather.service.AutoUpdateService;
import com.example.coolweather.util.HttpCallbackListener;
import com.example.coolweather.util.HttpUntil;
import com.example.coolweather.util.Utility;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WeatherActivity extends Activity implements OnClickListener{
	
	private LinearLayout weatherInfoLayout;
	private TextView cityNameText;
	private TextView publishText;
	private TextView weatherDespText;
	private TextView temp1Text;
	private TextView temp2Text;
	private TextView currentDateText;
	private Button switchCity;
	private Button refreshWeather;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		weatherInfoLayout=(LinearLayout)findViewById(R.id.weather_info_layout);
		switchCity=(Button)findViewById(R.id.switch_city);
		refreshWeather=(Button)findViewById(R.id.refresh_weather);
		switchCity.setOnClickListener(this);
		refreshWeather.setOnClickListener(this);
		cityNameText=(TextView)findViewById(R.id.city_name);
		publishText=(TextView)findViewById(R.id.publish_text);
		weatherDespText=(TextView)findViewById(R.id.weather_desp);
		temp1Text=(TextView)findViewById(R.id.temp1);
		temp2Text=(TextView)findViewById(R.id.temp2);
		currentDateText=(TextView)findViewById(R.id.current_date);
		String countyCode=getIntent().getStringExtra("county_code");
		if(!TextUtils.isEmpty(countyCode)){
			publishText.setText("同步中...");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
			Log.d("oncreate", countyCode);
		}else {
			showWeather();
		}
	}
	private void showWeather() {
		SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
		cityNameText.setText(prefs.getString("city_name", ""));
		publishText.setText(prefs.getString("publish_time", ""));
		temp1Text.setText(prefs.getString("temp1", ""));
		temp2Text.setText(prefs.getString("temp2", ""));
		weatherDespText.setText(prefs.getString("weather_desp", ""));
		currentDateText.setText(prefs.getString("current_date", ""));
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
		Intent intent=new Intent(this,AutoUpdateService.class);
		startService(intent);
	}
	private void queryWeatherCode(String countyCode) {
		String address="http://www.weather.com.cn/data/list3/city"+countyCode+".xml";
		Log.d("queryweathercode", countyCode);
		queryFromSever(address,"countyCode");
	}
	private void queryWeatherInfo(String weatherCode) {
		String address="http://www.weather.com.cn/data/cityinfo/"+weatherCode+".html";
		Log.d("queryweatherinfo", weatherCode);
		queryFromSever(address, weatherCode);
	}
	private void queryFromSever(final String address, final String type) {
		HttpUntil.sendHttpRequest(address, new HttpCallbackListener() {	
			@Override
			public void onFinish(final String response) {
				if("countyCode".equals(type)){
					if(!TextUtils.isEmpty(response)){
						String[] arrary=response.split("\\|");
						if(arrary!=null&&arrary.length==2){
							String weatherCode=arrary[1];
							queryWeatherInfo(weatherCode);
						}
					}
				}else{
					Log.d("queryfromserver", "true");
					Utility.handleWeatherResponse(WeatherActivity.this, response);
				
					runOnUiThread(new Runnable() {
						public void run() {
							showWeather();
						}
					});
				}
			}
			
			

			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {
					public void run() {
						publishText.setText("同步失败");
					}
				});
			}
		});
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.switch_city:
			Intent intent=new Intent(this,ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			Log.d("onclick switch", "true");
			finish();
			break;
		case R.id.refresh_weather:
			publishText.setText("同步中...");
			SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
			String weatherCode=prefs.getString("weather_code", "");
			if(!TextUtils.isEmpty(weatherCode)){
				Log.d("onclick refresh", "true");
				queryWeatherInfo(weatherCode);
			}
			break;
		default:
			break;
		}
	}
}
