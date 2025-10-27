package bgu.spl.net.impl.stomp;

import bgu.spl.net.srv.Connections;
import bgu.spl.net.srv.ConnectionImpl;

public class Handlers {
  //  private final StompProtocol<?> protocol;
   // public Handlers(StompProtocol<?> protocol){
       // this.protocol = protocol;
   // }

    private static String extractHeader (String [] lines , String key){
        
        for(String line : lines){
            if(line!=null && line.startsWith(key + ":")){
                return new String (line.substring((key + ":").length()).trim());

            }
        }
        return "";
    }
    private static String extractBody (String [] lines ){
        StringBuilder body = new StringBuilder();
        boolean bodyStarted=false;
        for(String line : lines){
         if(line.isEmpty()){
             bodyStarted=true;
             continue;
         }
         if(bodyStarted){
             body.append(line).append("\n");
         }
        }
        return body.toString().trim();

    }
    public static  void handleConnect (String [] msg,int connectionId, Connections connection, StompProtocol protocol) {
      String login=extractHeader(msg,"login");
      String passcode=extractHeader(msg,"passcode");
      String status=Authenticator.authenticate(login,passcode,connectionId);
      switch (status){
          case "ALREADY_LOGGED_IN":
          handleError("The client is already logged in,log out before trying", connectionId, connection, msg, protocol);
          break;
          case "SUCCESS_EXISTING_USER":
              connection.send(connectionId,"CONNECTED\nversion:1.2\n\n\u0000");
              break;
          case "WRONG_PASSCODE":
              handleError("Wrong password", connectionId, connection, msg, protocol);
              break;
          case "SUCCESS_NEW_USER":
              connection.send(connectionId,"CONNECTED\nversion:1.2\n\n\u0000");
              break;

      }

    }
    public static  void handleDisconnect (String [] msg,int connectionId, Connections connection, StompProtocol protocol) {
        String receiptId=extractHeader(msg,"receipt");
        connection.send(connectionId,"RECEIPT\nreceipt-id:" + receiptId + "\n\n\u0000");
        protocol.setshouldTerminate(true);
        Authenticator.logOut(connectionId);
        connection.disconnect(connectionId);


    }
    public static void handleSubscribe (String [] msg,int connectionId, Connections connection, StompProtocol protocol) {
        String destination=extractHeader(msg,"destination");
        if (destination.startsWith("/")) { 
              destination = destination.substring(1); 
            }
        String id=extractHeader(msg,"id");
        String receiptID=extractHeader(msg,"receipt");
        if(destination.isEmpty() || id.isEmpty()){
            handleError("Malformed SUBSCRIBE frame", connectionId, connection, msg, protocol);
            connection.disconnect(connectionId);
            return;
        }
        ((ConnectionImpl<?>)connection).subscribe(destination,connectionId, id);
        connection.send(connectionId,"RECEIPT\nreceipt-id:" + receiptID + "\n\n\u0000");

    }
    public static void handleUnsubscribe (String [] msg,int connectionId, Connections connection) {
        String id=extractHeader(msg,"id");
        String receiptID=extractHeader(msg,"receipt");
        ((ConnectionImpl<?>)connection).unsubscribe(connectionId,id);
        connection.send(connectionId,"RECEIPT\nreceipt-id:" + receiptID + "\n\n\u0000");

    }
    public static void handleSend (String [] msg,int connectionId, Connections connection, StompProtocol protocol) {
        String destination=extractHeader(msg,"destination");
        if (destination.startsWith("/")) { 
            destination = destination.substring(1); 
          }
        if(((ConnectionImpl<?>)connection).isExistInChannel(destination,connectionId)){
            connection.send(destination,extractBody(msg));
        }
        else {
            handleError("Not allowed to send message in unsbscribed channel", connectionId, connection, msg, protocol);
        }
    }
    public static void handleError (String  errorMsg,int connectionId, Connections connection,String[] originalMsg, StompProtocol protocol) {
        String receiptId= extractHeader(originalMsg,"receipt");
        StringBuilder errorFrame=new StringBuilder();
        errorFrame.append("ERROR\n");
        errorFrame.append("receipt-id:" + receiptId + "\n");
        errorFrame.append("message:" + errorMsg + "\n");
        errorFrame.append("\n");
        errorFrame.append("The message:\n");
        errorFrame.append("-----\n");
        if(originalMsg!= null){
            errorFrame.append(String.join("\n", originalMsg));
        }
        errorFrame.append("\n");
        errorFrame.append("-----\n");
        errorFrame.append("\u0000");
        protocol.setshouldTerminate(true);
        connection.send(connectionId,errorFrame.toString());
        connection.disconnect(connectionId);
    }

}
