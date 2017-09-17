package test.yang.com.smartbuscar.module.activity;

import android.databinding.DataBindingUtil;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.ResponseBody;
import test.yang.com.smartbuscar.R;
import test.yang.com.smartbuscar.databinding.ActivityMainBinding;
import test.yang.com.smartbuscar.module.dialog.PushDialog;
import test.yang.com.smartbuscar.module.fragment.CardFragment;
import test.yang.com.smartbuscar.module.fragment.PersonalCenterFragment;
import test.yang.com.smartbuscar.network.AuthManager;
import test.yang.com.smartbuscar.network.ProgressSubscriber;
import test.yang.com.smartbuscar.network.RetrofitHandler;
import test.yang.com.smartbuscar.notification.NotificationMessageManager;
import test.yang.com.smartbuscar.service.LoginService;
import test.yang.com.smartbuscar.utils.ToastUtil;
import test.yang.com.smartbuscar.websocket.WebSocketClient;
import test.yang.com.smartbuscar.websocket.WebSocketMessageManager;
import test.yang.com.smartbuscar.websocket.WebSocketMessageParser;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    private ActivityMainBinding binding = null;
    private int[] bgSelector = {R.drawable.tab1_picture_selector, R.drawable.tab2_picture_selector};
    private List<Fragment> fragmentList = new ArrayList<>();
    private MainViewPagerAdapter adapter = null;
    private Timer timer = null;
    private String topicFail = "/user/" + AuthManager.getUserAuth().getUserId() + "/system/pay/fail";
    private String topicSuccess = "/user/" + AuthManager.getUserAuth().getUserId() + "/system/pay/success";
    private WebSocketMessageManager.OnMessageListener<String> failListener = new WebSocketMessageManager.OnMessageListener<String>() {
        @Override
        public void onMessage(String message) {
            NotificationMessageManager.getInstance().showNotification("刷卡失败", null);
            PushDialog pushDialog = new PushDialog(MainActivity.this);
            pushDialog.setContent("刷卡失败");
            pushDialog.show();
        }
    };

    private WebSocketMessageManager.OnMessageListener<String> successListener = new WebSocketMessageManager.OnMessageListener<String>() {
        @Override
        public void onMessage(String message) {
            NotificationMessageManager.getInstance().showNotification("刷卡成功", null);
            PushDialog pushDialog = new PushDialog(MainActivity.this);
            pushDialog.setContent("刷卡成功");
            pushDialog.show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        initFragment();
        initTabLayout();
        registerPush();
        this.binding.ivLogo.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void registerPush() {
        WebSocketClient.connect();
        this.timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                WebSocketMessageManager.addMessageListener(topicFail, new WebSocketMessageParser<String>() {
                    @Override
                    public String onParseMessage(String topic, String message) {
                        return WebSocketMessageManager.parseMessage(message, String.class);
                    }
                }, failListener);
                WebSocketMessageManager.addMessageListener(topicSuccess, new WebSocketMessageParser<String>() {
                    @Override
                    public String onParseMessage(String topic, String message) {
                        return WebSocketMessageManager.parseMessage(message, String.class);
                    }
                }, successListener);
            }
        }, 3000);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (this.timer != null) {
            this.timer.cancel();
        }
        WebSocketMessageManager.removeMessageListener(topicFail, failListener);
        WebSocketMessageManager.removeMessageListener(topicSuccess, successListener);
    }

    private void initFragment() {
        this.fragmentList.add(new CardFragment());
        this.fragmentList.add(new PersonalCenterFragment());
        this.adapter = new MainViewPagerAdapter(getSupportFragmentManager(), this.fragmentList);
        this.binding.viewPager.setAdapter(this.adapter);
        this.binding.tabLayout.setTabMode(TabLayout.MODE_FIXED);
        this.binding.tabLayout.setupWithViewPager(this.binding.viewPager);
    }

    private void initTabLayout() {
        for (int i = 0; i < bgSelector.length; i++) {
            this.binding.tabLayout.getTabAt(i).setIcon(ContextCompat.getDrawable(this, bgSelector[i]));
        }
    }

    @Override
    public void onClick(View v) {
        logout();
    }

    private void logout() {
        RetrofitHandler.getService(LoginService.class).logout().subscribe(new ProgressSubscriber<ResponseBody>(this) {
            @Override
            protected void onFail(Throwable e) {
                ToastUtil.showToast(e.getMessage());
            }

            @Override
            protected void onSuccess(ResponseBody responseBody) {
                ToastUtil.showToast("登出成功");
                finish();
                AuthManager.setToken("");
            }
        });
    }

    private static class MainViewPagerAdapter extends FragmentPagerAdapter {
        private List<Fragment> fragments = new ArrayList<>();

        public MainViewPagerAdapter(FragmentManager fragmentManager, List<Fragment> fragments) {
            super(fragmentManager);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return this.fragments.get(position);
        }

        @Override
        public int getCount() {
            return this.fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "";
        }
    }
}
