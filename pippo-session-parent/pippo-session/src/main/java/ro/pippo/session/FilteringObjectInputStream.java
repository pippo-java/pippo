package ro.pippo.session;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;

/**
 * @author idealzh
 */
public class FilteringObjectInputStream extends ObjectInputStream {
    public FilteringObjectInputStream(InputStream in) throws IOException {
        super(in);
    }

    protected Class<?> resolveClass(java.io.ObjectStreamClass descriptor) throws ClassNotFoundException, IOException {
        String className = descriptor.getName();
        ClassFilter classFilter = new ClassFilter();
        if(className != null && className.length() > 0 && !classFilter.isWhiteListed(className)) {
            throw new InvalidClassException("Unauthorized deserialization attempt", descriptor.getName());
        } else {
            return super.resolveClass(descriptor);
        }
    }
}
