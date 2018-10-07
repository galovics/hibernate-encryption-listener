package com.arnoldgalovics.blog.hibernateencryptionlistener.encryption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

@Component
public class FieldDecrypter {
    @Autowired
    private Decrypter decrypter;

    public void decrypt(Object entity) {
        ReflectionUtils.doWithFields(entity.getClass(), field -> decryptField(field, entity), EncryptionUtils::isFieldEncrypted);
    }

    private void decryptField(Field field, Object entity) {
        field.setAccessible(true);
        Object value = ReflectionUtils.getField(field, entity);
        if (!(value instanceof String)) {
            throw new IllegalStateException("Encrypted annotation was used on a non-String field");
        }
        ReflectionUtils.setField(field, entity, decrypter.decrypt(value.toString()));
    }
}
