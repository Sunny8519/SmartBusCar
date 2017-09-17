package test.yang.com.smartbuscar.service;

import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import rx.Observable;
import test.yang.com.smartbuscar.module.bean.UserInfo;

/**
 * @Author: NiYang
 * @Date: 2017/4/9.
 */
public interface UserService {

    /**
     * 获取用户详细信息
     * @param userId
     * @return
     */
    @GET("user/{userId}")
    Observable<UserInfo> getUserDetail(@Path("userId") Long userId);

    /**
     * 注册用户
     * @param userInfo
     * @return
     */
    @POST("user")
    Observable<ResponseBody> registerUser(@Body UserInfo userInfo);
}
