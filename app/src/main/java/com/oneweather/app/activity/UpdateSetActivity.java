package com.oneweather.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.oneweather.app.R;
import com.oneweather.app.service.AutoUpdateService;


/**
 * Created by xiaoshaobin on 15/12/7.
 */
public class UpdateSetActivity extends Activity {
    private EditText autoUpdateTime;
    private CheckBox autoUpdateBox;
    private Button setOk;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_update);
       final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        autoUpdateBox = (CheckBox) findViewById(R.id.update);
        autoUpdateTime = (EditText) findViewById(R.id.update_time);
        setOk = (Button) findViewById(R.id.set_ok);
        boolean autoUpdate = pref.getBoolean("auto_update", false);
        int updateTime = pref.getInt("auto_update_time", 8);
        if (autoUpdate) {
            autoUpdateBox.setChecked(true);
            autoUpdateTime.setText(Integer.toString(updateTime));
        }
        setOk.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
              //  SharedPreferences.Editor editor = PreferenceManager
                      //  .getDefaultSharedPreferences(context).edit();
                SharedPreferences.Editor editor = pref.edit();
                if (autoUpdateBox.isChecked()) {
                    editor.putBoolean("auto_update", true);
                    // 激活服务
                    int time = 8;
                    if (!TextUtils.isEmpty(autoUpdateTime.getText())) {
                        String updataTime = autoUpdateTime.getText().toString();
                        time = Integer.parseInt(updataTime);
                        editor.putInt("auto_update_time", time);
                    }
                    Intent StartIntent = new Intent(UpdateSetActivity.this,
                            AutoUpdateService.class);
                    StartIntent.putExtra("auto_update_time", time);
                    startService(StartIntent);
                    // Log.v("MainActivity", "ServiceStart");

                } else {
                    editor.putBoolean("auto_update", false);
                    Intent stopIntent = new Intent(UpdateSetActivity.this,
                            AutoUpdateService.class);
                    stopService(stopIntent);
                    // Log.v("MainActivity", "StopStart");
                }
                // 非常重要
                editor.commit();
                Intent intent1 = new Intent(UpdateSetActivity.this,
                        WeatherActivity.class);
                startActivity(intent1);
                finish();
            }
        });

    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, WeatherActivity.class);
        startActivity(intent);
        finish();
    }

}

