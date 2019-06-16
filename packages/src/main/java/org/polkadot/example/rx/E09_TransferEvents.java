package org.polkadot.example.rx;

import io.reactivex.Observable;
import org.polkadot.api.SubmittableExtrinsic;
import org.polkadot.api.rx.ApiRx;
import org.polkadot.common.keyring.Types;
import org.polkadot.example.TestingPairs;
import org.polkadot.rpc.provider.ws.WsProvider;
import org.polkadot.types.rpc.ExtrinsicStatus;
import org.polkadot.types.type.Event;
import org.polkadot.types.type.EventRecord;
import org.polkadot.utils.UtilsCrypto;

import java.util.List;

public class E09_TransferEvents {
    static String ALICE = "5GrwvaEF5zXb26Fz9rcQpDWS57CtERHpNehXCPcNoHGKutQY";
    static int AMOUNT = 10000;

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

    static {
        System.loadLibrary("jni");
        System.out.println("load ");
    }

    //-Djava.library.path=./libs
    public static void main(String[] args) throws InterruptedException {
        initEndPoint(args);

        WsProvider wsProvider = new WsProvider(endPoint);

        String BOB = "5FHneW46xGXgs5mUiveU4sbTyGBzmstUspZC92UhjJM694ty";

        Observable<ApiRx> apiRxObservable = ApiRx.create(wsProvider);

        Types.KeyringInstance keyring = TestingPairs.testKeyring();

        // find the actual keypair in the keyring
        Types.KeyringPair alicePair = keyring.getPair(ALICE);

        // create a new random recipient
        String recipient = keyring.addFromSeed(UtilsCrypto.randomAsU8a(32), null, null).address();

        apiRxObservable.flatMap((apiRx) -> {
            SubmittableExtrinsic<Observable> call = apiRx.tx().section("balances").function("transfer").call(recipient, AMOUNT);
            return call.signAndSendCb(alicePair, null);
        }).subscribe((result) -> {
            SubmittableExtrinsic.SubmittableResult submittableResult = (SubmittableExtrinsic.SubmittableResult) result;

            ExtrinsicStatus status = submittableResult.getStatus();
            List<EventRecord> events = submittableResult.getEvents();

            System.out.println("Transaction status:" + status.getType());

            if (status.isFinalized()) {
                System.out.println("Completed at block hash" + status.asFinalized().toHex());

                System.out.println("Events");

                for (EventRecord event : events) {
                    EventRecord.Phase phase = event.getPhase();
                    Event eventEvent = event.getEvent();
                    System.out.println("\t" + phase.toString()
                            + ": " + eventEvent.getSection() + "." + eventEvent.getMethod()
                            + " " + eventEvent.getData().toString());
                }
                System.exit(0);
            }
        });
    }
}
