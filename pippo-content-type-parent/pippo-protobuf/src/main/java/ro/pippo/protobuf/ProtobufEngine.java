package ro.pippo.protobuf;

import com.google.protobuf.AbstractMessage;
import com.google.protobuf.ByteString;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.Application;
import ro.pippo.core.ContentTypeEngine;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;

/**
 * A Protobuf Engine
 *
 * @author Denys Vitali
 */

@MetaInfServices
public class ProtobufEngine implements ContentTypeEngine {
    private static final Logger log =
        LoggerFactory.getLogger(ProtobufInitializer.class);

    @Override
    public void init(Application application) {

    }

    @Override
    public String getContentType() {
        return "application/protobuf";
    }

    @Override
    public String toString(Object msg) {
        if(msg instanceof AbstractMessage){
            try {
                return (String)
                    msg.getClass()
                        .getMethod("getMessage")
                        .invoke(msg);
            } catch (IllegalAccessException |
                InvocationTargetException |
                NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public <T> T fromString(String content, Class<T> classOfT) {
        ByteString bs = ByteString.copyFrom(content, StandardCharsets.UTF_8);

        try {
            return (T) classOfT.getMethod("fromString", ByteString.class)
                .invoke(bs);
        } catch (NoSuchMethodException |
            IllegalAccessException |
            InvocationTargetException e) {
            log.error("Unable to deserialize protobuf object", e);
        }
        return null;
    }

    @Override
    public byte[] toByteArray(Object o) {
        if(o instanceof AbstractMessage){
            try {
                return(byte[])
                    o.getClass()
                        .getMethod("toByteArray")
                        .invoke(o);
            } catch (IllegalAccessException |
                InvocationTargetException |
                NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        return new byte[]{};
    }
}
