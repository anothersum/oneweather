package com.oneweather.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.oneweather.app.db.CoolWeatherDB;
import com.oneweather.app.model.City;
import com.oneweather.app.model.County;
import com.oneweather.app.model.Province;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utility {
	/*
	 * 解析和处理服务器返回的省级数据
	 */
	public synchronized static boolean handleProvincesResponse(
			CoolWeatherDB coolWeatherDB, String response) {
		if (!TextUtils.isEmpty(response)) {
			String[] allProvinces = response.split(",");
			if (allProvinces != null && allProvinces.length > 0) {
				for (String p : allProvinces) {
					String[] array = p.split("\\|");
					Province province = new Province();
					province.setProvinceCode(array[0]);
					province.setProvinceName(array[1]);
					// 将解析的数据存储到Province表
					coolWeatherDB.saveProvince(province);
				}
				return true;
			}

		}
		return false;
	}

	/*
	 * 解析和处理服务器返回的市级数据
	 */
	public synchronized static boolean handleCitiesResponse(
			CoolWeatherDB coolWeatherDB, String response, int provinceId) {
		if (!TextUtils.isEmpty(response)) {
			String[] allCities = response.split(",");
			if (allCities != null && allCities.length > 0) {
				for (String c : allCities) {
					String[] array = c.split("\\|");
					City city = new City();
					city.setCityCode(array[0]);
					city.setCityName(array[1]);
					city.setProvinceId(provinceId);
					coolWeatherDB.saveCity(city);
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * 解析和处理服务器返回的县级数据
	 */
	public synchronized static boolean handleCountiesResponse(
			CoolWeatherDB coolWeatherDB, String response, int cityId) {
		if (!TextUtils.isEmpty(response)) {
			String[] allCounties = response.split(",");
			if (allCounties != null && allCounties.length > 0) {
				for (String c : allCounties) {
					String[] array = c.split("\\|");
					County county = new County();
					county.setCountyCode(array[0]);
					county.setCountyName(array[1]);
					county.setCityId(cityId);

					coolWeatherDB.saveCounty(county);
				}
				return true;
			}
		}
		return false;

	}

	/**
	 * 解析服务器返回的JSON数据，并将解析出的数据存储到本地
	 *
	 */
	public static void handleWeatherResponse(Context context, String response) {
		try {

			//response ="{\"yesterday\":{\"fl\":\"微风\",\"fx\":\"北风\",\"high\":\"高温 8℃\",\"type\":\"中雨\",\"low\":\"低温 6℃\",\"date\":\"5日星期六\"}}";
			//response =" {\"desc\":\"OK\",\"status\":1000,\"data\":{\"wendu\":\"1\",\"ganmao\":\"天气较凉，较易发生感冒，请适当增加衣服。体质较弱的朋友尤其应该注意防护。\",\"forecast\":[{\"fengxiang\":\"东风\",\"fengli\":\"微风级\",\"high\":\"高温 7℃\",\"type\":\"中雨\",\"low\":\"低温 1℃\",\"date\":\"6日星期天\"},{\"fengxiang\":\"无持续风向\",\"fengli\":\"微风级\",\"high\":\"高温 7℃\",\"type\":\"阴\",\"low\":\"低温 3℃\",\"date\":\"7日星期一\"}],\"yesterday\":{\"fl\":\"微风\",\"fx\":\"东风\",\"high\":\"高温 7℃\",\"type\":\"中雨\",\"low\":\"低温 3℃\",\"date\":\"5日星期六\"},\"aqi\":\"54\",\"city\":\"宁波\"}}";
			JSONObject jsonObject = new JSONObject(response);
			JSONObject data = jsonObject.getJSONObject("data");
//			JSONObject weatherInfo = data.getJSONObject("yesterday");
			String cityName = data.getString("city");

			JSONArray jsonArray = data.getJSONArray("forecast");
			JSONObject  weatherInfo = jsonArray.getJSONObject(0);
		//	String weatherCode = weatherInfo.getString("type");
			String temp1 = weatherInfo.getString("high");
			String temp2 = weatherInfo.getString("low");
			String weatherDesp = weatherInfo.getString("type");
			String publishTime = weatherInfo.getString("date");

			JSONObject  weatherInfo1 = jsonArray.getJSONObject(1);
			String weatherDesp1 = weatherInfo1.getString("type");
			String temp11 = weatherInfo1.getString("high");
			String temp21 = weatherInfo1.getString("low");
		//	String weatherDesp = weatherInfo1.getString("type");

			saveWeatherInfo(context, cityName, temp1, temp2,
					weatherDesp, publishTime,weatherDesp1,temp11,temp21);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void saveWeatherInfo(Context context, String cityName,
									    String temp1, String temp2, String weatherDesp,
									   String publishTime, String weatherDesp1, String temp11, String temp21) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
		SharedPreferences.Editor editor = PreferenceManager
				.getDefaultSharedPreferences(context).edit();
		editor.putBoolean("city_selected", true);
		editor.putString("city_name", cityName);
		//editor.putString("weather_code", weatherCode);
		editor.putString("temp1", temp1);
		editor.putString("temp2", temp2);
		editor.putString("weather_desp", weatherDesp);
		editor.putString("publish_time", sdf.format(new Date()));
		editor.putString("current_date", "今天");
		editor.putString("temp11", temp11);
		editor.putString("temp21", temp21);
		editor.putString("weather_desp1", weatherDesp1);
		editor.putString("current_date1", "明天");
		editor.commit();
	}
}
