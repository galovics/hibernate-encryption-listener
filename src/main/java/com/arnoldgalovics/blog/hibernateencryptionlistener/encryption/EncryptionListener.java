package com.arnoldgalovics.blog.hibernateencryptionlistener.encryption;

import org.hibernate.event.spi.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EncryptionListener implements PreInsertEventListener, PreUpdateEventListener, PreLoadEventListener {
    @Autowired
    private FieldEncrypter fieldEncrypter;

    @Autowired
    private FieldDecrypter fieldDecrypter;

    @Override
    public boolean onPreInsert(PreInsertEvent event) {
        Object[] state = event.getState();
        String[] propertyNames = event.getPersister().getPropertyNames();
        Object entity = event.getEntity();
        fieldEncrypter.encrypt(state, propertyNames, entity);
        return false;
    }

    @Override
    public boolean onPreUpdate(PreUpdateEvent event) {
        Object[] state = event.getState();
        String[] propertyNames = event.getPersister().getPropertyNames();
        Object entity = event.getEntity();
        fieldEncrypter.encrypt(state, propertyNames, entity);
        return false;
    }

    @Override
    public void onPreLoad(PreLoadEvent event) {
        Object[] state = event.getState();
        String[] propertyNames = event.getPersister().getPropertyNames();
        Object entity = event.getEntity();
        fieldDecrypter.decrypt(state, propertyNames, entity);
    }
}
