package com.arnoldgalovics.blog.hibernateencryptionlistener.encryption;

import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Field;

public abstract class EncryptionUtils {
    public static boolean isFieldEncrypted(Field field) {
        return AnnotationUtils.findAnnotation(field, Encrypted.class) != null;
    }

    public static int getPropertyIndex(String name, String[] properties) {
        for (int i = 0; i < properties.length; i++) {
            if (name.equals(properties[i])) {
                return i;
            }
        }
        throw new IllegalArgumentException("No property was found for name " + name);
    }
}
