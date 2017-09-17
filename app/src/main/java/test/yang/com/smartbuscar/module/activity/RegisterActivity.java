package test.yang.com.smartbuscar.module.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import okhttp3.ResponseBody;
import test.yang.com.smartbuscar.R;
import test.yang.com.smartbuscar.databinding.ActivityRegisterBinding;
import test.yang.com.smartbuscar.module.bean.UserInfo;
import test.yang.com.smartbuscar.network.ProgressSubscriber;
import test.yang.com.smartbuscar.network.RetrofitHandler;
import test.yang.com.smartbuscar.service.UserService;
import test.yang.com.smartbuscar.utils.ToastUtil;

/**
 * @author NiYang
 * @Description:
 * @date 2017/6/7 17:34
 */

public class RegisterActivity extends BaseActivity implements View.OnClickListener {
    private ActivityRegisterBinding binding = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_register);
        initView();
    }

    private void initView() {
        this.binding.btnRegister.setOnClickListener(this);
        this.binding.ivBack.setOnClickListener(this);
    }

    private void registerUser() {
        UserInfo userInfo = new UserInfo();
        userInfo.setUsername(this.binding.etAccountNumber.getText().toString());
        userInfo.setPassword(this.binding.etAccountPassword.getText().toString());
        userInfo.setName(this.binding.etName.getText().toString());
        userInfo.setGender("男".equals(this.binding.etGender.getText().toString()) ? 1 : 0);
        userInfo.setBirthday(Long.valueOf(this.binding.etAge.getText().toString()));
        userInfo.setPhone(this.binding.etTelephone.getText().toString());
        userInfo.setEmail(this.binding.etEmail.getText().toString());
        registerUser(userInfo);
    }

    private void registerUser(UserInfo userInfo) {
        RetrofitHandler.getService(UserService.class).registerUser(userInfo).subscribe(new ProgressSubscriber<ResponseBody>(this) {
            @Override
            protected void onFail(Throwable e) {
                ToastUtil.showToast(e.getMessage());
            }

            @Override
            protected void onSuccess(ResponseBody responseBody) {
                ToastUtil.showToast("注册成功,请登录");
                finish();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_register:
                registerUser();
                break;
            case R.id.iv_back:
                finish();
                break;
            default:break;
        }
    }
}
