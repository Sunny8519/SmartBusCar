package test.yang.com.smartbuscar.websocket.stomp;

import java.net.URI;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.java_websocket.WebSocket;
import org.java_websocket.client.DefaultSSLWebSocketClientFactory;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;

import android.util.Log;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;

/**
 * Created by naik on 05.05.16.
 */
public class WebSocketsConnectionProvider implements ConnectionProvider {

    private static final String TAG = WebSocketsConnectionProvider.class.getSimpleName();

    private final String mUri;
    private final Map<String, String> mConnectHttpHeaders;
    private WebSocketClient mWebSocketClient;
    private List<Subscriber<? super LifecycleEvent>> mLifecycleSubscribers;
    private List<Subscriber<? super String>> mMessagesSubscribers;
    private boolean haveConnection;
    private TreeMap<String, String> mServerHandshakeHeaders;

    /**
     * Support UIR scheme ws://host:port/path
     *
     * @param connectHttpHeaders
     *            may be null
     */
    public WebSocketsConnectionProvider(String uri, Map<String, String> connectHttpHeaders) {
        mUri = uri;
        mConnectHttpHeaders = connectHttpHeaders != null ? connectHttpHeaders : new HashMap<String, String>();
        mLifecycleSubscribers = new CopyOnWriteArrayList<>();
        mMessagesSubscribers = new CopyOnWriteArrayList<>();
    }

    @Override
    public Observable<String> messages() {

        Observable<String> observable = Observable.create(new Observable.OnSubscribe<String>() {

            @Override
            public void call(Subscriber<? super String> subscriber) {
                mMessagesSubscribers.add(subscriber);
            }
        }).doOnUnsubscribe(new Action0() {
            @Override
            public void call() {
                Iterator<Subscriber<? super String>> iterator = mMessagesSubscribers.iterator();
                while (iterator.hasNext()) {
                    if (iterator.next().isUnsubscribed())
                        iterator.remove();
                }

                if (mMessagesSubscribers.size() < 1)
                    mWebSocketClient.close();
            }
        });
        createWebSocketConnection();
        return observable;
    }

    private void createWebSocketConnection() {
        if (haveConnection) {
            return;
        }

        mWebSocketClient = new WebSocketClient(URI.create(mUri), new Draft_17(), mConnectHttpHeaders, 0) {

            @Override
            public void onWebsocketHandshakeReceivedAsClient(WebSocket conn, ClientHandshake request,
                    ServerHandshake response) throws InvalidDataException {
                Log.d(TAG, "onWebsocketHandshakeReceivedAsClient with response: " + response.getHttpStatus() + " "
                        + response.getHttpStatusMessage());
                mServerHandshakeHeaders = new TreeMap<>();
                Iterator<String> keys = response.iterateHttpFields();
                while (keys.hasNext()) {
                    String key = keys.next();
                    String value = response.getFieldValue(key);
                    Log.d(TAG, "onWebsocketHandshakeReceivedAsClient: key = " + key + " value = " + value);
                    mServerHandshakeHeaders.put(key, value);
                }
            }

            @Override
            public void onOpen(ServerHandshake handshakeData) {
                Log.d(TAG, "onOpen with handshakeData: " + handshakeData.getHttpStatus() + " "
                        + handshakeData.getHttpStatusMessage());
                LifecycleEvent openEvent = new LifecycleEvent(LifecycleEvent.Type.OPENED);
                openEvent.setHandshakeResponseHeaders(mServerHandshakeHeaders);
                emitLifecycleEvent(openEvent);
            }

            @Override
            public void onMessage(String message) {
                Log.d(TAG, "onMessage: " + message);
                emitMessage(message);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                Log.d(TAG, "onClose: code=" + code + " reason=" + reason + " remote=" + remote);
                haveConnection = false;
                emitLifecycleEvent(new LifecycleEvent(LifecycleEvent.Type.CLOSED));
            }

            @Override
            public void onError(Exception ex) {
                Log.e(TAG, "onError", ex);
                if (ex instanceof java.net.ConnectException) {
                    haveConnection = false;
                }
                emitLifecycleEvent(new LifecycleEvent(LifecycleEvent.Type.ERROR, ex));
            }
        };

        if (mUri.startsWith("wss")) {
            try {
                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null, new TrustManager[]{new AdditionalKeyStoresTrustManager()}, null);
                mWebSocketClient.setWebSocketFactory(new DefaultSSLWebSocketClientFactory(sc));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        mWebSocketClient.connect();
        haveConnection = true;
    }

    @Override
    public Observable<Void> send(final String stompMessage) {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                if (mWebSocketClient == null) {
                    subscriber.onError(new IllegalStateException("Not connected yet"));
                } else {
                    mWebSocketClient.send(stompMessage);
                    subscriber.onCompleted();
                }
            }
        });
    }

    private void emitLifecycleEvent(LifecycleEvent lifecycleEvent) {
        Log.d(TAG, "Emit lifecycle event: " + lifecycleEvent.getType().name());
        for (int i = 0; i < mLifecycleSubscribers.size(); i++) {
            mLifecycleSubscribers.get(i).onNext(lifecycleEvent);
        }
    }

    private void emitMessage(String stompMessage) {
        Log.d(TAG, "Emit STOMP message: " + stompMessage);
        for (Subscriber<? super String> subscriber : mMessagesSubscribers) {
            subscriber.onNext(stompMessage);
        }
    }

    @Override
    public Observable<LifecycleEvent> getLifecycleReceiver() {
        return Observable.create(new Observable.OnSubscribe<LifecycleEvent>() {
            @Override
            public void call(Subscriber<? super LifecycleEvent> subscriber) {
                mLifecycleSubscribers.add(subscriber);
            }
        }).doOnUnsubscribe(new Action0() {
            @Override
            public void call() {
                for(Subscription subscription : mLifecycleSubscribers){
                    if(subscription.isUnsubscribed()) {
                        mLifecycleSubscribers.remove(subscription);
                    }
                }
            }
        });
    }

    public static class AdditionalKeyStoresTrustManager implements X509TrustManager {

        protected ArrayList<X509TrustManager> x509TrustManagers = new ArrayList<>();

        protected AdditionalKeyStoresTrustManager(KeyStore... additionalkeyStores) {
            final ArrayList<TrustManagerFactory> factories = new ArrayList<TrustManagerFactory>();

            try {
                // The default Trustmanager with default keystore
                final TrustManagerFactory original =
                        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                original.init((KeyStore) null);
                factories.add(original);

                for (KeyStore keyStore : additionalkeyStores) {
                    final TrustManagerFactory additionalCerts =
                            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                    additionalCerts.init(keyStore);
                    factories.add(additionalCerts);
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            /* Iterate over the returned trust managers, and hold on to any that
             * are X509TrustManagers */
            for (TrustManagerFactory tmf : factories)
                for (TrustManager tm : tmf.getTrustManagers())
                    if (tm instanceof X509TrustManager)
                        x509TrustManagers.add((X509TrustManager) tm);

            if (x509TrustManagers.size() == 0)
                throw new RuntimeException("Couldn't find any X509TrustManagers");

        }

        /* Delegate to the default trust manager. */
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            final X509TrustManager defaultX509TrustManager = x509TrustManagers.get(0);
            defaultX509TrustManager.checkClientTrusted(chain, authType);
        }

        /* Loop over the trustmanagers until we find one that accepts our server */
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            // for( X509TrustManager tm : x509TrustManagers ) {
            // try {
            // tm.checkServerTrusted(chain,authType);
            // return;
            // } catch( CertificateException e ) {
            // // ignore
            // }
            // }
            // throw new CertificateException();
        }

        public X509Certificate[] getAcceptedIssuers() {
            final ArrayList<X509Certificate> list = new ArrayList<>();
            for (X509TrustManager tm : x509TrustManagers)
                list.addAll(Arrays.asList(tm.getAcceptedIssuers()));
            return list.toArray(new X509Certificate[list.size()]);
        }
    }
}
