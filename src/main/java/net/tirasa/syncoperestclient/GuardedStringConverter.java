package net.tirasa.syncoperestclient;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import java.lang.reflect.Field;
import org.identityconnectors.common.Base64;
import org.identityconnectors.common.security.EncryptorFactory;
import org.identityconnectors.common.security.GuardedString;

public class GuardedStringConverter implements Converter {

    @Override
    public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
        boolean readOnly = false;
        try {
            Field readOnlyField = GuardedString.class.getDeclaredField("_readOnly");
            readOnlyField.setAccessible(true);
            readOnly = readOnlyField.getBoolean(source);
        } catch (Exception e) {
            e.printStackTrace();
        }
        writer.startNode("readonly");
        writer.setValue(Boolean.toString(readOnly));
        writer.endNode();

        boolean disposed = false;
        try {
            Field disposedField = GuardedString.class.getDeclaredField("_disposed");
            disposedField.setAccessible(true);
            readOnly = disposedField.getBoolean(source);
        } catch (Exception e) {
            e.printStackTrace();
        }
        writer.startNode("disposed");
        writer.setValue(Boolean.toString(readOnly));
        writer.endNode();

        writer.startNode("encryptedBytes");
        final StringBuilder cleartext = new StringBuilder();
        ((GuardedString) source).access(new GuardedString.Accessor() {

            @Override
            public void access(final char[] clearChars) {
                cleartext.append(clearChars);
            }
        });
        final byte[] encryptedBytes =
                EncryptorFactory.getInstance().getDefaultEncryptor().encrypt(cleartext.toString().getBytes());
        writer.setValue(Base64.encode(encryptedBytes));
        writer.endNode();
    }

    @Override
    public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
        reader.moveDown();
        final boolean readOnly = Boolean.valueOf(reader.getValue());
        reader.moveUp();

        reader.moveDown();
        final boolean disposed = Boolean.valueOf(reader.getValue());
        reader.moveUp();

        reader.moveDown();
        final byte[] encryptedBytes = Base64.decode(reader.getValue());
        reader.moveUp();

        final byte[] clearBytes = EncryptorFactory.getInstance().getDefaultEncryptor().decrypt(encryptedBytes);

        GuardedString dest = new GuardedString(new String(clearBytes).toCharArray());

        try {
            Field readOnlyField = GuardedString.class.getDeclaredField("_readOnly");
            readOnlyField.setAccessible(true);
            readOnlyField.setBoolean(dest, readOnly);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Field readOnlyField = GuardedString.class.getDeclaredField("_disposed");
            readOnlyField.setAccessible(true);
            readOnlyField.setBoolean(dest, disposed);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dest;
    }

    @Override
    public boolean canConvert(final Class type) {
        return type.equals(GuardedString.class);
    }
}
