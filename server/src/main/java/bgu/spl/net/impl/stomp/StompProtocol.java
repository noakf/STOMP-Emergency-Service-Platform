package bgu.spl.net.impl.stomp;

import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.srv.Connections;

public class StompProtocol<T> implements StompMessagingProtocol<T> {


    private boolean shouldTerminate;
    private int connectionId;
    private Connections<T> connections;
    //private Handlers handlers;

    @Override
    public void start(int connectionId, Connections<T> connections ) {
        this.shouldTerminate=false;
        this.connectionId = connectionId;
        this.connections =  connections;
        //this.handlers = new Handlers(this);
    }


    @Override
    public void process(T msg) {
        if (!(msg instanceof String[])) {
            throw new IllegalArgumentException("Expected a String[] message but got: " + msg.getClass().getName());
        }
        String[] parts =(String[]) msg;
        String command = parts[0];
        switch (command) {
            case "CONNECT":
                Handlers.handleConnect(parts, connectionId, connections,this);
                break;
            case "DISCONNECT":
                Handlers.handleDisconnect(parts, connectionId, connections, this);
                break;
            case "SUBSCRIBE":
                Handlers.handleSubscribe(parts, connectionId, connections,this);
                break;
            case "UNSUBSCRIBE":
                Handlers.handleUnsubscribe(parts, connectionId, connections);
                break;
            case "SEND":
                Handlers.handleSend(parts, connectionId, connections,this);
                break;
            default:
                Handlers.handleError("wrong headframe",connectionId,connections,null, this);

        }

    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;

    }

    public void setshouldTerminate(boolean shouldTerminate) {
        this.shouldTerminate=shouldTerminate;
    }
}


