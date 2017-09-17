package test.yang.com.smartbuscar.module.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import test.yang.com.smartbuscar.R;
import test.yang.com.smartbuscar.databinding.ActivityLoginBinding;
import test.yang.com.smartbuscar.module.bean.LoginInfo;
import test.yang.com.smartbuscar.module.bean.UserAuth;
import test.yang.com.smartbuscar.network.AuthManager;
import test.yang.com.smartbuscar.network.ProgressSubscriber;
import test.yang.com.smartbuscar.network.RetrofitHandler;
import test.yang.com.smartbuscar.service.LoginService;
import test.yang.com.smartbuscar.utils.ToastUtil;

/**
 * @author NiYang
 * @Description:
 * @date 2017/6/7 17:33
 */

public class LoginActivity extends BaseActivity implements View.OnClickListener {
    private ActivityLoginBinding binding = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        initView();
    }

    private void initView() {
        this.binding.ivBack.setOnClickListener(this);
        this.binding.btnLogin.setOnClickListener(this);
    }

    private void login() {
        LoginInfo loginInfo = new LoginInfo();
        loginInfo.setClientId(AuthManager.clientId);
        if (TextUtils.isEmpty(this.binding.etAccountNumber.getText())) {
            ToastUtil.showToast("用户名不能为空");
            return;
        }
        if (TextUtils.isEmpty(this.binding.etPassword.getText())) {
            ToastUtil.showToast("密码不能为空");
            return;
        }
        loginInfo.setUsername(this.binding.etAccountNumber.getText().toString());
        loginInfo.setPassword(this.binding.etPassword.getText().toString());
        login(loginInfo);
    }

    private void login(LoginInfo loginInfo) {
        RetrofitHandler.getService(LoginService.class).login(loginInfo).subscribe(new ProgressSubscriber<UserAuth>(this) {
            @Override
            protected void onFail(Throwable e) {
                ToastUtil.showToast(e.getMessage());
            }

            @Override
            protected void onSuccess(UserAuth userAuth) {
                if (userAuth == null) {
                    return;
                }
                AuthManager.setToken(userAuth.getToken());
                AuthManager.setUserAuth(userAuth);
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                //关闭先前的任务栈，开启新的任务栈盛放MainActivity
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.btn_login:
                login();
                break;
            default:break;
        }
    }
}
