package test.yang.com.smartbuscar.module.fragment;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import test.yang.com.smartbuscar.R;
import test.yang.com.smartbuscar.databinding.FragmentPersonalCenterViewBinding;
import test.yang.com.smartbuscar.module.bean.UserInfo;
import test.yang.com.smartbuscar.module.dialog.PushDialog;
import test.yang.com.smartbuscar.network.AuthManager;
import test.yang.com.smartbuscar.network.ProgressSubscriber;
import test.yang.com.smartbuscar.network.RetrofitHandler;
import test.yang.com.smartbuscar.service.UserService;
import test.yang.com.smartbuscar.utils.ToastUtil;

/**
 * @author NiYang
 * @Description:
 * @date 2017/6/7 17:40
 */

public class PersonalCenterFragment extends BaseFragment {
    private FragmentPersonalCenterViewBinding binding = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.binding = DataBindingUtil.inflate(inflater, R.layout.fragment_personal_center_view, container, false);
        getUserInfo();
        this.binding.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PushDialog pushDialog = new PushDialog(getContext());
                pushDialog.show();
            }
        });
        return this.binding.getRoot();
    }

    private void initUserInfo(UserInfo userInfo) {
        this.binding.txtUserName.setText(userInfo.getName());
        this.binding.txtAccountName.setText("账号：" + userInfo.getUsername());
        this.binding.txtSex.setText("性别：" + (userInfo.getGender() == 1 ? "男" : "女"));
        this.binding.txtAge.setText("年龄：" + String.valueOf(userInfo.getBirthday()));
        this.binding.txtPhone.setText("手机号：" + userInfo.getPhone());
        this.binding.txtEmail.setText("邮箱：" + userInfo.getEmail());
    }

    private void getUserInfo() {
        if (AuthManager.getUserAuth() != null) {
            RetrofitHandler.getService(UserService.class).getUserDetail(AuthManager.getUserAuth().getUserId()).subscribe(new ProgressSubscriber<UserInfo>(getContext()) {
                @Override
                protected void onFail(Throwable e) {
                    ToastUtil.showToast(e.getMessage());
                }

                @Override
                protected void onSuccess(UserInfo userInfo) {
                    if (userInfo == null) {
                        return;
                    }
                    initUserInfo(userInfo);
                }
            });
        }
    }
}
