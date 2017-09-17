package test.yang.com.smartbuscar.network;

import android.provider.Settings;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

import test.yang.com.smartbuscar.App;
import test.yang.com.smartbuscar.module.bean.UserAuth;
import test.yang.com.smartbuscar.stores.Pref;
import test.yang.com.smartbuscar.utils.LogUtil;

/**
 * @Author: NiYang
 * @Date: 2017/4/8.
 */
public class AuthManager {
    private static final String TAG = AuthManager.class.getSimpleName();
    private static UserAuth userAuth = null;
    private static String token = null;
    public static String clientId = Settings.Secure.getString(App.getContext().getContentResolver(), Settings.Secure.ANDROID_ID);

    static {
        String json = Pref.getString(Pref.Key.User.USER_AUTH, null);
        if (json != null) {
            try {
                userAuth = new ObjectMapper().readValue(json, UserAuth.class);
                setToken(userAuth.getToken());
            } catch (IOException e) {
                LogUtil.d(TAG, e.getMessage());
            }
        }
    }

    /**
     * 设置token信息，给AuthManager中的token字段初始化，并存储token信息
     * @param token
     */
    public static void setToken(String token) {
        AuthManager.token = token;
        Pref.putString(Pref.Key.Network.TOKEN, token);
    }

    public static String getAuthorization() {
        return "Basic " + clientId + ":" + (token == null ? "" : token);
    }

    public static void setUserAuth(UserAuth userAuth) {
        AuthManager.userAuth = userAuth;
        setToken(userAuth.getToken());
        try {
            String json = new ObjectMapper().writeValueAsString(userAuth);
            Pref.putString(Pref.Key.User.USER_AUTH, json);
        } catch (JsonProcessingException e) {
            LogUtil.e("JsonProcessingException", e.getMessage());
        }
    }

    public static void cacheFamilyId(String familyId) {
        Pref.putString(Pref.Key.Family.FAMILY, familyId);
    }

    public static String getCacheFamilyId() {
        return Pref.getString(Pref.Key.Family.FAMILY, null);
    }

    public static UserAuth getUserAuth() {
        return userAuth;
    }
}

