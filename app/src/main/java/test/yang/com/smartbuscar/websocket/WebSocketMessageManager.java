package test.yang.com.smartbuscar.websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import android.text.TextUtils;
import android.util.ArrayMap;

import test.yang.com.smartbuscar.utils.LogUtil;

/**
 * Created by wangqi on 2017/4/14.
 */

public class WebSocketMessageManager {

    public interface OnMessageListener<T> {
        void onMessage(T message);
    }

    public static final String TAG = WebSocketMessageManager.class.getSimpleName();

    private static Map<String, Set<OnMessageListener>> msgListeners;
    private static Map<String, WebSocketMessageParser> msgParsers;
    private static Map<String, List<String>> msgCache;

    static {
        msgListeners = new ArrayMap<>();
        msgCache = new ArrayMap<>();
        msgParsers = new ArrayMap<>();
    }

    public static void addMessageListener(String topic, WebSocketMessageParser parser, OnMessageListener l) {
        if (l == null || TextUtils.isEmpty(topic) || parser == null) {
            throw new NullPointerException("WebSocketMessageManager.addMessageListener() params must not be null");
        }
        msgParsers.put(topic, parser);
        Set<OnMessageListener> listeners = msgListeners.get(topic);
        if (listeners == null) {
            listeners = new HashSet<>();
            msgListeners.put(topic, listeners);
        }
        listeners.add(l);
        startReceiveMessage(topic);
    }

    public static void removeMessageListener(String topic, OnMessageListener listener) {
        if (listener == null || topic == null) {
            return;
        }
        Set<OnMessageListener> listeners = msgListeners.get(topic);
        if (listeners != null) {
            listeners.remove(listener);
        }
        if (!isHasListeners(topic)) {
            WebSocketClient.unsubscribe(topic);
        }
    }

    private static void startReceiveMessage(final String topic) {
        WebSocketClient.subscribe(topic, new WebSocketClient.OnMessageListener() {
            @Override
            public void onMessage(String msg) {
                if (!isHasListeners(topic)) {
                    putMessageCache(topic, msg);
                } else {
                    List<String> messages = msgCache.get(topic);
                    if (messages == null || messages.size() <= 0) {
                        messages = new ArrayList<>();
                    }
                    messages.add(msg);
                    for (OnMessageListener l : msgListeners.get(topic)) {
                        for (String m : messages) {
                            l.onMessage(msgParsers.get(topic).onParseMessage(topic, m));
                        }
                    }
                }
            }
        });
    }

    private static void putMessageCache(String topic, String msg) {
        List<String> msgList = msgCache.get(topic);
        if (msgList == null) {
            msgList = new ArrayList<>();
            msgCache.put(topic, msgList);
        }
        msgList.add(msg);
    }

    private static boolean isHasListeners(String topic) {
        return msgListeners != null && msgListeners.get(topic) != null && msgListeners.get(topic).size() > 0;
    }

    public static <T> List<T> parseListMessage(String msg, Class clazz) {
        ObjectMapper mapper = new ObjectMapper();
        CollectionType ct = mapper.getTypeFactory().constructCollectionType(ArrayList.class, clazz);
        try {
            return (List<T>) mapper.readValue(msg, ct);
        } catch (IOException e) {
            LogUtil.e(TAG, e.getMessage());
        }
        return new ArrayList<>();
    }

    public static <T> T parseMessage(String msg, Class clazz) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return (T) mapper.readValue(msg, clazz);
        } catch (IOException e) {
            LogUtil.e(TAG, e.getMessage());
        }
        return null;
    }
}
