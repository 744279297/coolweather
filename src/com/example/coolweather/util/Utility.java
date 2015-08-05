package com.example.coolweather.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.example.coolweather.db.CoolWeatherDB;
import com.example.coolweather.model.City;
import com.example.coolweather.model.County;
import com.example.coolweather.model.Province;

public class Utility {
	public synchronized static boolean handleProvinceResponse(CoolWeatherDB coolweatherDB,
			String response) {
		if(!TextUtils.isEmpty(response)){
			String[] allProvinces=response.split(",");
			if(allProvinces!=null&&allProvinces.length>0){
				for(String p:allProvinces){
					String[] array=p.split("\\|");
					Province province=new Province();
					province.setProvinceCode(array[0]);
					province.setProvinceName(array[1]);
					coolweatherDB.saveProvince(province);
				}
				return true;
			}
		}
				return false;
		
	}
	public synchronized static boolean handleCityResponse(CoolWeatherDB coolweatherDB,
			String response,int provinceId) {
		if(!TextUtils.isEmpty(response)){
			String[] allCities=response.split(",");
			if(allCities!=null&&allCities.length>0){
				for(String p:allCities){
					String[] array=p.split("\\|");
					City city=new City();
					city.setCityCode(array[0]);
					city.setCityName(array[1]);
					city.setProvinceId(provinceId);
					coolweatherDB.saveCity(city);
				}
				return true;
			}
		}
				return false;
		
	}
	public synchronized static boolean handleCountyResponse(CoolWeatherDB coolweatherDB,
			String response,int cityId) {
		if(!TextUtils.isEmpty(response)){
			String[] allCounties=response.split(",");
			if(allCounties!=null&&allCounties.length>0){
				for(String p:allCounties){
					String[] array=p.split("\\|");
					County county=new County();
					county.setCountyCode(array[0]);
					county.setCountyName(array[1]);
					county.setCityId(cityId);
					coolweatherDB.savecounty(county);
				}
				return true;
			}
		}
				return false;
		
	}
	public static void handleWeatherResponse(Context context,String response) {
		try{
		JSONObject jsonobject=new JSONObject(response);
		JSONObject weatherInfo=jsonobject.getJSONObject("weatherinfo");
		String cityName=weatherInfo.getString("city");
		String weatherCode=weatherInfo.getString("cityid");
		String temp1=weatherInfo.getString("temp1");
		String temp2=weatherInfo.getString("temp2");
		String weatherDesp=weatherInfo.getString("weather");
		String publishTime=weatherInfo.getString("ptime");
		saveWeatherInfo(context,cityName,weatherCode,temp1,temp2,weatherDesp,publishTime);		
		}catch(JSONException e){
			e.printStackTrace();
		}
		
	}
	private static void saveWeatherInfo(Context context, String cityName,
			String weatherCode, String temp1, String temp2, String weatherDesp,
			String publishTime) {
		SimpleDateFormat sdf=new SimpleDateFormat("yyyyƒÍM‘¬d»’", Locale.CHINA);
		SharedPreferences.Editor editor=PreferenceManager.
				getDefaultSharedPreferences(context).edit();
		editor.putBoolean("city_selected", true);
		editor.putString("city_name", cityName);
		editor.putString("weather_code", weatherCode);
		editor.putString("temp1", temp1);
		editor.putString("temp2", temp2);
		editor.putString("weather_desp", weatherDesp);
		editor.putString("publish_time", publishTime);
		editor.putString("current_date", sdf.format(new Date()));
		editor.commit();
	}
}
