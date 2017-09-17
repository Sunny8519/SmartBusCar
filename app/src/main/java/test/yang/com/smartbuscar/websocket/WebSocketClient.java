package test.yang.com.smartbuscar.websocket;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;
import android.support.v4.util.ArrayMap;

import org.java_websocket.WebSocket;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import test.yang.com.smartbuscar.network.AuthManager;
import test.yang.com.smartbuscar.stores.Pref;
import test.yang.com.smartbuscar.utils.LogUtil;
import test.yang.com.smartbuscar.websocket.stomp.LifecycleEvent;
import test.yang.com.smartbuscar.websocket.stomp.Stomp;
import test.yang.com.smartbuscar.websocket.stomp.client.StompClient;
import test.yang.com.smartbuscar.websocket.stomp.client.StompMessage;

public class WebSocketClient {

    private static Subscription lifecycleSubscription;

    public interface OnMessageListener {
        void onMessage(String msg);
    }

    private static final String TAG = WebSocketClient.class.getSimpleName();
    private static final String HEADER_KEY = "Authorization";
    private static final String HEADER_VALUE = "123";
    private static final String URL = Pref.getWebSocketAddress();
    private static ArrayMap<String, Subscription> topicSubscriptionMapping = new ArrayMap<>();
    private static ArrayMap<String, OnMessageListener> topicCache = new ArrayMap<>();
    private static StompClient stompClient;
    private static boolean isConnecting = false;
    private static Handler handler = new Handler();

    private WebSocketClient() {
    }

    private static void innerConnect() {
        if (isConnecting || stompClient.isConnected()) {
            return;
        }
        isConnecting = true;
        LogUtil.i(TAG, "WebSocket connecting...");
        if (lifecycleSubscription != null) lifecycleSubscription.unsubscribe();
        lifecycleSubscription = stompClient.lifecycle().subscribe(new Subscriber<LifecycleEvent>() {
            @Override
            public void onCompleted() {
                isConnecting = false;
            }

            @Override
            public void onError(Throwable e) {
                LogUtil.e(TAG, e.getMessage());
                isConnecting = false;
            }

            @Override
            public void onNext(LifecycleEvent lifecycleEvent) {
                isConnecting = false;
                switch (lifecycleEvent.getType()) {
                    case OPENED:
                        LogUtil.i(TAG, "WebSocket Opened");
                        if (topicCache.size() > 0) {
                            for (String topic : topicCache.keySet()) {
                                subscribe(topic, topicCache.get(topic));
                            }
                            topicCache.clear();
                        }
                        break;
                    case CLOSED:
                        LogUtil.i(TAG, "WebSocket Closed");
                        connect();
                        break;
                    case ERROR:
                        LogUtil.i(TAG, "WebSocket Error");
                        connect();
                        break;
                }
            }
        });
        stompClient.connect();
    }

    private static Timer reconnectTimer = new Timer();

    public static void connect() {
        if (reconnectTimer != null) {
            reconnectTimer.cancel();
        }
        if (stompClient == null) {
            Map<String, String> header = new HashMap<>();
            header.put(HEADER_KEY, AuthManager.getAuthorization());
            stompClient = Stomp.over(WebSocket.class, URL, header);
        }
        reconnectTimer = new Timer();
        reconnectTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (stompClient != null && stompClient.isConnected()) {
                    reconnectTimer.cancel();
                } else {
                    LogUtil.i(TAG, "WebSocket connect");
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            innerConnect();
                        }
                    });
                }
            }
        }, 0, 5000);
    }

    public synchronized static void subscribe(String topic, final OnMessageListener listener) {
        topic = topic.replaceAll("//", "/");
        LogUtil.i(TAG, "subscribe is connected: " + stompClient.isConnected());
        if (!stompClient.isConnected()) {
            topicCache.put(topic, listener);
            connect();
            return;
        }
        LogUtil.i(TAG, "WebSocket subscribe: " + topic);
        if (topicSubscriptionMapping.get(topic) != null) {
            return;
        }
        Subscription subscription = stompClient.topic(topic).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<StompMessage>() {
                    @Override
                    public void call(final StompMessage stompMessage) {
                        LogUtil.i(TAG, "WebSocket message received: " + stompMessage.getPayload());
                        listener.onMessage(stompMessage.getPayload());
                    }
                });
        topicSubscriptionMapping.put(topic, subscription);
    }

    public static void unsubscribe(String topic) {
        if (topicSubscriptionMapping != null) {
            Subscription subscription = topicSubscriptionMapping.get(topic);
            if (subscription != null && !subscription.isUnsubscribed()) {
                subscription.unsubscribe();
                topicSubscriptionMapping.remove(topic);
            }
        }
    }

    public static void send(String destination, String message) {
        stompClient.send("/app/" + destination, message);
    }
}
