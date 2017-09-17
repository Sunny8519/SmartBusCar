package test.yang.com.smartbuscar.module.fragment;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import test.yang.com.smartbuscar.R;
import test.yang.com.smartbuscar.databinding.FragmentCardViewBinding;
import test.yang.com.smartbuscar.module.bean.Account;
import test.yang.com.smartbuscar.module.dialog.AddAccountDialog;
import test.yang.com.smartbuscar.module.dialog.OnUpdateDataListener;
import test.yang.com.smartbuscar.network.AuthManager;
import test.yang.com.smartbuscar.network.ProgressSubscriber;
import test.yang.com.smartbuscar.network.RetrofitHandler;
import test.yang.com.smartbuscar.service.AccountService;
import test.yang.com.smartbuscar.utils.ToastUtil;

/**
 * @author NiYang
 * @Description:
 * @date 2017/6/7 17:39
 */

public class CardFragment extends BaseFragment implements View.OnClickListener {
    private FragmentCardViewBinding binding = null;
    private OnUpdateDataListener onUpdateDataListener = new OnUpdateDataListener() {
        @Override
        public void onUpdateData() {
            getAccounts();
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.binding = DataBindingUtil.inflate(inflater, R.layout.fragment_card_view, container, false);
        initView();
        return this.binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        getAccounts();
    }

    private void initView() {
        this.binding.ivAddCard.setOnClickListener(this);
    }

    private void showAccounts(List<Account> accounts) {
        this.binding.container.removeAllViews();
        for (Account account : accounts) {
            ItemCardView itemCardView = new ItemCardView(getContext(), account);
            this.binding.container.addView(itemCardView);
        }
    }

    private void getAccounts() {
        if (AuthManager.getUserAuth() != null) {
            RetrofitHandler.getService(AccountService.class).getAccounts(AuthManager.getUserAuth().getUserId()).subscribe(new ProgressSubscriber<List<Account>>(getContext()) {
                @Override
                protected void onFail(Throwable e) {
                    ToastUtil.showToast(e.getMessage());
                }

                @Override
                protected void onSuccess(List<Account> accounts) {
                    if (accounts == null || accounts.size() == 0) {
                        return;
                    }
                    showAccounts(accounts);
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        AddAccountDialog addAccountDialog = new AddAccountDialog(getContext());
        addAccountDialog.setOnUpdateDataListener(this.onUpdateDataListener);
        addAccountDialog.show();
    }
}
