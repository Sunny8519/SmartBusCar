package test.yang.com.smartbuscar.websocket;

/**
 * Created by wangqi on 2017/4/20.
 */

public interface WebSocketMessageParser<T> {
    T onParseMessage(String topic, String message);
}
