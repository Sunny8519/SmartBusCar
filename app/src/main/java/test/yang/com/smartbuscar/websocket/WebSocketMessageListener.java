package test.yang.com.smartbuscar.websocket;

import java.util.List;

/**
 * Created by LiuYiLing on 2017/3/3.
 */

public interface WebSocketMessageListener<T> {
    void onMessage(List<T> messages);
}
