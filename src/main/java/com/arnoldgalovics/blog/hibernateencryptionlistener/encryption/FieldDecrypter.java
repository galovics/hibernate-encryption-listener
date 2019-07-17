package com.arnoldgalovics.blog.hibernateencryptionlistener.encryption;

import java.lang.reflect.Field;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

@Component
public class FieldDecrypter {
    @Autowired
    private Decrypter decrypter;

    public void decrypt(Object[] state, String[] propertyNames, Object entity) {
        ReflectionUtils.doWithFields(entity.getClass(), field -> decryptField(field, state, propertyNames), EncryptionUtils::isFieldEncrypted);
    }

    private void decryptField(Field field, Object[] state, String[] propertyNames) {
        int propertyIndex = EncryptionUtils.getPropertyIndex(field.getName(), propertyNames);
        Object currentValue = state[propertyIndex];
        if (currentValue != null) {
            if (!(currentValue instanceof String)) {
                throw new IllegalStateException("Encrypted annotation was used on a non-String field");
            }
            state[propertyIndex] = decrypter.decrypt(currentValue.toString());
        }
    }
}
