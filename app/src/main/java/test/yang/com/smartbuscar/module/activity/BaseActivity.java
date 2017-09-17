package test.yang.com.smartbuscar.module.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import test.yang.com.smartbuscar.utils.StatusBarUtil;

/**
 * @author NiYang
 * @Description:
 * @date 2017/6/7 17:33
 */

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.fitSystemBarTextColor(this);
    }

    protected void switchActivity(Class activityName) {
        Intent intent = new Intent(this, activityName);
        startActivity(intent);
    }
}
