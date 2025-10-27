package bgu.spl.net.srv;
import java.util.function.Supplier;
import bgu.spl.net.impl.stomp.*;
import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.*;


public class TPCServer<T> extends BaseServer<T>{
        public TPCServer(int port,  Supplier<StompMessagingProtocol<T>> protocolFactory, Supplier<StompMessageEncoderDecoder<T>> encdecSupplier) {
            super(port, protocolFactory, encdecSupplier);
    }
 
 
@Override
    protected void execute(BlockingConnectionHandler<T>  handler) {
        Thread clientThread = new Thread(handler);
        clientThread.start();
    }
}


