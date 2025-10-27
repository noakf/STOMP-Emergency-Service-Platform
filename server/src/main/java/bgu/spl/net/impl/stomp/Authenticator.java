package bgu.spl.net.impl.stomp;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Authenticator {
    private static final Map<String , String> users = new ConcurrentHashMap<>();
    private static final Map<Integer , String> activesUsers = new ConcurrentHashMap<>();
    

    public static synchronized String authenticate( String login, String passcode, int connectionId) {
        boolean found=false;
        for (String value : activesUsers.values()){
            if(value.equals(login)){
               found=true;}
        }

        if( found){
            return "ALREADY_LOGGED_IN";
        }
        if(users.containsKey(login) ) {
            if(users.get(login).equals(passcode)) {

                return "SUCCESS_EXISTING_USER";
            }
            else{
                return "WRONG_PASSCODE";
            }
        }else {
            activesUsers.put(connectionId, login);
            users.put(login, passcode);
            return "SUCCESS_NEW_USER";
        }
    }
    public static synchronized void logOut( int connectionId) {
        activesUsers.remove(connectionId);
    }
}
