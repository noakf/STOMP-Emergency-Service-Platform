package bgu.spl.net.srv;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class ConnectionImpl<T> implements Connections<T> {
    private final Map<Integer, ConnectionHandler<T>> connections=new ConcurrentHashMap<>(); // connectionId per client
    private final Map<String, Set<Integer>> channels=new ConcurrentHashMap<>();// channel to sets of connectionId
    private final Map<Integer,Map<String, String>> subscriptions=new ConcurrentHashMap<>();//connectionid -> channel to subid
    private static Connections<?> instance = null; 
    private static final Object lock = new Object(); // Lock for thread safety
    private volatile int messagesId=1;

    public static <T> Connections<T> getInstance() {
        if (instance == null) {
            synchronized (lock) { 
                if (instance == null) {  
                    instance = new ConnectionImpl<>();
                    
                }
            }
        }
        return (Connections<T>) instance;
    }


    @Override
    public boolean send(int connectionId, T msg) {
        ConnectionHandler<T> handler =connections.get(connectionId);
        if (handler != null) {
            handler.send(msg);
            return true;
        }
     return false;
    }
    @Override
    public void send(String channel, T msg){
        Set<Integer> subscribers =channels.get(channel);
        if (subscribers != null) {
            for(Integer subscriber : subscribers) {
                Map<String,String> map= subscriptions.get(subscriber);
                if(map!=null){
                String subscription=map.get(channel);
                send(subscriber,(T)("MESSAGE\n"+"subscription:"+subscription+"\n"+"message-id:"+messagesId+"\n"+"destination:/"+channel+"\n"+ msg+"\n\u0000"));
                messagesId++;
                }
                else{System.out.println("the map is null");}
            }
        }
    }
    @Override
    public void disconnect(int connectionId) {
        connections.remove(connectionId); 
    
        Map<String, String> userSubscriptions = subscriptions.get(connectionId);
        if (userSubscriptions != null) { 
            for (String channel : userSubscriptions.keySet()) {
                Set<Integer> channelSubscribers = channels.get(channel);
                if (channelSubscribers != null) {
                    channelSubscribers.remove(connectionId);
                    if (channelSubscribers.isEmpty()) {
                        channels.remove(channel); 
                    }
                }
            }
            subscriptions.remove(connectionId); 
        }
    }
    
    public void register(ConnectionHandler<T> handler, int connectionId){
        connections.put(connectionId, handler);
    }
    public void subscribe(String channel, int connectionId , String SubscriptionId){
        channels.computeIfAbsent(channel, k -> new CopyOnWriteArraySet<>()).add(connectionId);
        subscriptions.putIfAbsent(connectionId, new ConcurrentHashMap<>());
        subscriptions.get(connectionId).put(channel, SubscriptionId);
    }


    public void unsubscribe(int connectionId, String SubscriptionId){
        if(subscriptions.containsKey(connectionId)) {
            Map<String, String> userSubscriptions = subscriptions.get(connectionId);
            //find the correct channel
            String channelToRemove = null;
            for(Map.Entry<String, String> entry : userSubscriptions.entrySet()) {
                if(entry.getValue().equals(SubscriptionId)) {
                    channelToRemove = entry.getKey();
                    break;
                }
            }
            //Remove connectionId from the corrent channel
            if(channelToRemove != null) {
                userSubscriptions.remove(channelToRemove);
                if(channels.containsKey(channelToRemove)) {
                    channels.get(channelToRemove).remove(connectionId);
                }
                if(userSubscriptions.isEmpty()) {
                    subscriptions.remove(connectionId);
                }
            }
        }

    }


public  boolean isExistInChannel(String channel, int connectionId){
        if(channels.containsKey(channel)) {
            if(channels.get(channel).contains(connectionId)){
                return true;
            }
        }
return false;
    }

}
