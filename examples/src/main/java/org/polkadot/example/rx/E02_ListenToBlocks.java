package org.polkadot.example.rx;

import io.reactivex.Observable;
import org.polkadot.api.rx.ApiRx;
import org.polkadot.rpc.provider.ws.WsProvider;
import org.polkadot.types.type.Header;

public class E02_ListenToBlocks {

    //static String endPoint = "wss://poc3-rpc.polkadot.io/";
    //static String endPoint = "wss://substrate-rpc.parity.io/";
    //static String endPoint = "ws://45.76.157.229:9944/";
    static String endPoint = "ws://127.0.0.1:9944";

    static void initEndPoint(String[] args) {
        if (args != null && args.length >= 1) {
            endPoint = args[0];
            System.out.println(" connect to endpoint [" + endPoint + "]");
        } else {
            System.out.println(" connect to default endpoint [" + endPoint + "]");
        }
    }

    public static void main(String[] args) {
        initEndPoint(args);

        WsProvider wsProvider = new WsProvider(endPoint);

        Observable<ApiRx> apiRxObservable = ApiRx.create(wsProvider);

        apiRxObservable.flatMap((apiRx) -> {
                    return apiRx.rpc().chain().function("subscribeNewHead").invoke();
                }

        ).subscribe((result) -> {
            Header header = (Header) result;
            System.out.println("Chain is at block: " + header.getBlockNumber());
        });


        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.exit(0);
    }
}
