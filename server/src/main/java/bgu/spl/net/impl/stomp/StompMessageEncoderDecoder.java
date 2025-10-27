package bgu.spl.net.impl.stomp;

import bgu.spl.net.api.MessageEncoderDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
 

public class StompMessageEncoderDecoder<T>  implements MessageEncoderDecoder<T>{
    private ArrayList<Byte> buffer = new ArrayList<>();

    @Override
    public T decodeNextByte(byte nextByte){
        if (nextByte == '\u0000') { // STOMP messages end with NULL character
            String message = new String(toByteArray(buffer), StandardCharsets.UTF_8);
            buffer.clear(); 
            return (T) message.split("\n");
            } else {
                buffer.add(nextByte); // Append byte to buffer
                return null;
            }
        }


    @Override
    public byte[] encode(T message){
       // String newMessage = String.join("\n",(String[]) message); 
        return ((String)message).getBytes(StandardCharsets.UTF_8);

    }

    private byte[] toByteArray(ArrayList<Byte> list) {
        byte[] arr = new byte[list.size()];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = list.get(i);
        }
        return arr;
    }
    
}



