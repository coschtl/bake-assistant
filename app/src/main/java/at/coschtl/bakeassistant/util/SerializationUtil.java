package at.coschtl.bakeassistant.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class SerializationUtil {
    private SerializationUtil() {
    }

    public static byte[] serialize(Serializable serializable) {
        if (serializable == null) {
            return null;
        }
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oout = new ObjectOutputStream(bout);
            oout.writeObject(serializable);
            oout.flush();
            return bout.toByteArray();
        } catch (IOException e) {
            throw new SerializationException("can not serialize " + serializable.getClass().getName(), e);
        }
    }

    public static <T> T deserialize(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
        try {
            ObjectInputStream oin = new ObjectInputStream(bin);
            return (T) oin.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new SerializationException("can not deserialize byte[] of size + " + bytes.length + " to given Object type", e);
        }
    }

    public static class SerializationException extends RuntimeException {
        public SerializationException(String message, Exception e) {
            super(message, e);
        }

    }
}
