package com.oneweather.app.activity;

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

import com.oneweather.app.R;
import com.oneweather.app.service.AutoUpdateService;
import com.oneweather.app.util.HttpCallbackListener;
import com.oneweather.app.util.HttpUtil;
import com.oneweather.app.util.Utility;


public class WeatherActivity extends Activity implements OnClickListener {
	private LinearLayout weatherInfoLayout;
	/**
	 * 用于显示城市名
	 */
	private TextView cityNameText;
	/**
	 * 用于显示发布时间
	 */
	private TextView publishText;
	/**
	 * 用于显示天气描述信息
	 */
	private TextView weatherDespText;
	/**
	 * 用于显示气温1
	 */
	private TextView temp1Text;
	/**
	 * 用于显示气温2
	 */
	private TextView temp2Text;
	/**
	 * 用于显示当前日期
	 */
	private TextView currentDateText1;

	private TextView weatherDespText1;
	/**
	 * 用于显示气温1
	 */
	private TextView temp1Text1;
	/**
	 * 用于显示气温2
	 */
	private TextView temp2Text1;
	/**
	 * 用于显示当前日期
	 */
	private TextView currentDateText;

	// 切换城市
	private Button switchCity;
	// 更新天气
	private Button refreshWeather;
	private Button updateSet;
	private Button camOpen;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		// 初始化各控件
		weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
		cityNameText = (TextView) findViewById(R.id.city_name);
		publishText = (TextView) findViewById(R.id.publish_text);
		weatherDespText = (TextView) findViewById(R.id.weather_desp);
		temp1Text = (TextView) findViewById(R.id.temp1);
		temp2Text = (TextView) findViewById(R.id.temp2);
		currentDateText = (TextView) findViewById(R.id.current_date);

		weatherDespText1 = (TextView) findViewById(R.id.weather_desp1);
		temp1Text1 = (TextView) findViewById(R.id.temp11);
		temp2Text1 = (TextView) findViewById(R.id.temp21);
		currentDateText1 = (TextView) findViewById(R.id.current_date1);

		switchCity = (Button) findViewById(R.id.switch_city);
		refreshWeather = (Button) findViewById(R.id.refresh_weather);
		updateSet = (Button) findViewById(R.id.set_update);
		camOpen = (Button) findViewById(R.id.cam_open);
		switchCity.setOnClickListener(this);
		refreshWeather.setOnClickListener(this);
		updateSet.setOnClickListener(this);
		camOpen.setOnClickListener(this);
		String countyCode = getIntent().getStringExtra("county_code");
		if (!TextUtils.isEmpty(countyCode)) {
			// 有县级代号时就去查询天气
			publishText.setText("同步中...");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			//queryWeatherCode(countyCode);
			queryWeatherInfo(countyCode);
		} else {
			// 没有县级代号时就直接显示本地天气
			showWeather();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.switch_city:
				//Log.v("MainActivity", "choose");
				Intent intent = new Intent(this, ChooseAreaActivity.class);
				intent.putExtra("from_weather_activity", true);
				startActivity(intent);
				finish();
				break;
			case R.id.refresh_weather:
				publishText.setText("同步中...");
				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(this);
				//String weatherCode = prefs.getString("weather_code", "");
				String weatherCode = prefs.getString("city_name", "");

				if (!TextUtils.isEmpty(weatherCode)) {
					queryWeatherInfo(weatherCode);
				}
				break;
			case R.id.set_update:
				Intent intent1 = new Intent(this, UpdateSetActivity.class);
				startActivity(intent1);
				finish();
				break;
			case R.id.cam_open:
				Intent intent2 = new Intent(this, CamOpenActivity.class);
				startActivity(intent2);
				finish();
				break;
			default:
				break;
		}

	}

	/**
	 * 查询县级天气
	 */

	private void queryWeatherCode(String countyCode) {
		// TODO Auto-generated method stub
		String address = "http://www.weather.com.cn/data/list3/city"
				+ countyCode + ".xml";
		queryFromServer(address, "countyCode");
	}

	/**
	 * 查询天气代号所对应的天气。
	 */
	private void queryWeatherInfo(String weatherCode)  {

//		String address ="http://www.weather.com.cn/adat/cityinfo/101190103.html";throws UnsupportedEncodingException
//		String address ="http://www.weather.com.cn/data/cityinfo/"http://wthrcdn.etouch.cn/weather_mini?city=%E6%9D%AD%E5%B7%9E
//				+ weatherCode +".html";
//		String value = URLEncoder.encode("宁波");
//		String address ="http://wthrcdn.etouch.cn/weather_mini?city="+ value;
		String address ="http://wthrcdn.etouch.cn/weather_mini?city="+java.net.URLEncoder.encode(weatherCode);//中文需要编码
		queryFromServer(address, "weatherCode");
	}


	/**
	 * 根据传入的地址和类型去向服务器查询天气代号或者天气信息。
	 */
	private void queryFromServer(final String address, final String type) {
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			public void onFinish(final String response) {
				if ("countyCode".equals(type)) {
					if (!TextUtils.isEmpty(response)) {
						// 从服务器返回的数据中解析出天气代号
						String[] array = response.split("\\|");
						if (array != null && array.length == 2) {
							String weatherCode = array[1];
							queryWeatherInfo(weatherCode);
						}
					}
				} else if ("weatherCode".equals(type)) {
					// 处理服务器返回的天气信息
					Utility.handleWeatherResponse(WeatherActivity.this,
							response);
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
					@Override
					public void run() {
						publishText.setText("同步失败");
					}
				});
			}
		});
	}

	/**
	 * 从SharedPreferences文件中读取存储的天气信息，并显示到界面上。
	 */
	private void showWeather() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		cityNameText.setText(prefs.getString("city_name", ""));
		temp1Text.setText(prefs.getString("temp1", ""));
		temp2Text.setText(prefs.getString("temp2", ""));
		weatherDespText.setText(prefs.getString("weather_desp", ""));
		publishText.setText(prefs.getString("publish_time", "") + "发布");
		currentDateText.setText(prefs.getString("current_date", ""));

		temp1Text1.setText(prefs.getString("temp11", ""));
		temp2Text1.setText(prefs.getString("temp21", ""));
		weatherDespText1.setText(prefs.getString("weather_desp1", ""));
		currentDateText1.setText(prefs.getString("current_date1", ""));

		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
		//激活服务
		Intent intent = new Intent(this,AutoUpdateService.class);
		startService(intent);
		Log.v("MainActivity", "ServiceStart");
	}
}
