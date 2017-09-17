package test.yang.com.smartbuscar.service;

import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.POST;
import rx.Observable;
import test.yang.com.smartbuscar.module.bean.LoginInfo;
import test.yang.com.smartbuscar.module.bean.UserAuth;

/**
 * @Author: NiYang
 * @Date: 2017/4/8.
 */
public interface LoginService {
    /**
     * 用户登录
     * @param loginInfo
     * @return
     */
    @POST("login")
    Observable<UserAuth> login(@Body LoginInfo loginInfo);

    /**
     * 用户注销
     * @return
     */
    @POST("logout")
    Observable<ResponseBody> logout();
}
