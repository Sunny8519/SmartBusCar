package test.yang.com.smartbuscar.network;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import test.yang.com.smartbuscar.utils.LogUtil;

/**
 * OkHttpClient 生成器
 *
 * @Author: NiYang
 * @Date: 2017/4/8.
 */
public class OkHttpClientFactory {
    private static final String TAG = "OkHttpClientFactory";
    private final static int connectionTimeout = 10;
    private final static int readTimeout = 30;

    public static OkHttpClient create() {
        //添加信任管理器(实则不检查任何证书),仅用作Https连接
        final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            @Override
            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] chain,
                    String authType) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] chain,
                    String authType) throws CertificateException {
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[0];
            }
        }};

        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());
        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }

        OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor(new Interceptor() {

            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request().newBuilder()
                        .addHeader("Content-Type", "application/json; charset=UTF-8")
                        //频繁网络请求需要加入
                        .addHeader("Connection", "close")
                        .addHeader("Accept-Encoding", "gzip, deflate")
                        .addHeader("Connection", "keep-alive")
                        .addHeader("Accept", "*/*")
                        .addHeader("Authorization", AuthManager.getAuthorization())
                        .build();
                Response response = chain.proceed(request);
                return response;
            }
        }).connectTimeout(connectionTimeout, TimeUnit.SECONDS)// 连接超时
                .readTimeout(readTimeout, TimeUnit.SECONDS)// 读取超时
                .sslSocketFactory(sslContext.getSocketFactory())
                .hostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)
                .build();
        return okHttpClient;
    }
}
