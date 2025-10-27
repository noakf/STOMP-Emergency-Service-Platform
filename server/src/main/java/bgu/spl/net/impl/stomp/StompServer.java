package bgu.spl.net.impl.stomp;
import bgu.spl.net.srv.ConnectionImpl;
import bgu.spl.net.srv.Server;

public class StompServer {

   
    public static void main(String[] args) {
        if (args.length < 2) 
        System.out.println("problem in args");
      else{
        int port =Integer.parseInt(args[0]);
        String serverType =args[1];

        ConnectionImpl<String> connectionImpl=new  ConnectionImpl<>();
        Authenticator authenticator = new Authenticator(); 
        Server<?> server;
        if ("tpc".equalsIgnoreCase(serverType)) {
            
            server = Server.threadPerClient(
                    port,
                    () ->  new StompProtocol<>(),
                    StompMessageEncoderDecoder::new);
                   
        } else if ("reactor".equalsIgnoreCase(serverType)) {
            server = Server.reactor(
                    Runtime.getRuntime().availableProcessors(),
                    port,
                    StompProtocol::new,
                    StompMessageEncoderDecoder::new);
        } else {
            System.out.println("Invalid server type. Use 'tpc' or 'reactor'.");
            return;
        }

        server.serve();
      }
   }
}
