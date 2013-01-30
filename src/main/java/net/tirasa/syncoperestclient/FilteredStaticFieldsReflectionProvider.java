package net.tirasa.syncoperestclient;

import com.thoughtworks.xstream.converters.reflection.ObjectAccessException;
import com.thoughtworks.xstream.converters.reflection.Sun14ReflectionProvider;
import java.lang.reflect.Field;

public class FilteredStaticFieldsReflectionProvider extends Sun14ReflectionProvider {

    @Override
    protected boolean fieldModifiersSupported(final Field field) {
        final boolean result;
        if ("_encryptor".equals(field.getName())
                && field.getType().equals(org.identityconnectors.common.security.Encryptor.class)) {

            result = true;
        } else {
            result = super.fieldModifiersSupported(field);
        }

        return result;
    }

    @Override
    public void writeField(final Object object, final String fieldName, final Object value, final Class definedIn) {
        Field field = fieldDictionary.field(object.getClass(), fieldName, definedIn);
        if ("_encryptor".equals(field.getName())
                && field.getType().equals(org.identityconnectors.common.security.Encryptor.class)) {

            try {
                field.setAccessible(true);
                field.set(null, value);
            } catch (Exception e) {
                throw new ObjectAccessException("While setting " + field.getName(), e);
            }
        } else {
            super.writeField(object, fieldName, value, definedIn);
        }
    }
}
