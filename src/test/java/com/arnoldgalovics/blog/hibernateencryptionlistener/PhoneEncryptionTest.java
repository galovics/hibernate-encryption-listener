package com.arnoldgalovics.blog.hibernateencryptionlistener;

import com.arnoldgalovics.blog.hibernateencryptionlistener.repository.Phone;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.Query;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PhoneEncryptionTest {
    @Autowired
    private TransactionalRunner txRunner;

    @Test
    public void testInsertionWorks() {
        String expectedPhoneNumber = "00361234567";
        // Persisting a phone entity through JPA, this should decrypt the phone number column
        UUID phoneId = txRunner.doInTransaction(em -> {
            Phone newPhone = new Phone(expectedPhoneNumber);
            em.persist(newPhone);
            return newPhone.getId();
        });

        // Checks if the database has the phone number value in an encrypted form
        txRunner.doInTransaction(em -> {
            Query query = em.createNativeQuery("SELECT phone_number FROM Phone where id = :phoneId");
            query.setParameter("phoneId", phoneId);
            String nativePhoneNumber = (String) query.getSingleResult();
            assertThat(nativePhoneNumber).isNotEqualTo(expectedPhoneNumber);
        });

        // Checks if the decryption happened automatically when getting the row through JPA
        txRunner.doInTransaction(em -> {
            Phone phone = em.find(Phone.class, phoneId);
            assertThat(phone.getPhoneNumber()).isEqualTo(expectedPhoneNumber);
        });
    }

    @Test
    public void testUpdateWorks() {
        String oldPhoneNumber = "0987654321";
        String expectedPhoneNumber = "00361234567";
        // Persisting a phone entity through JPA, this should decrypt the phone number column
        UUID phoneId = txRunner.doInTransaction(em -> {
            Phone newPhone = new Phone(oldPhoneNumber);
            em.persist(newPhone);
            return newPhone.getId();
        });

        // Checks if the database has the phone number value in an encrypted form
        txRunner.doInTransaction(em -> {
            Query query = em.createNativeQuery("SELECT phone_number FROM Phone where id = :phoneId");
            query.setParameter("phoneId", phoneId);
            String nativePhoneNumber = (String) query.getSingleResult();
            assertThat(nativePhoneNumber).isNotEqualTo(oldPhoneNumber);
        });

        // Update the phone number
        txRunner.doInTransaction(em -> {
            Phone phone = em.find(Phone.class, phoneId);
            phone.setPhoneNumber(expectedPhoneNumber);
        });

        // Checks if the database has the phone number value in an encrypted form
        txRunner.doInTransaction(em -> {
            Query query = em.createNativeQuery("SELECT phone_number FROM Phone where id = :phoneId");
            query.setParameter("phoneId", phoneId);
            String nativePhoneNumber = (String) query.getSingleResult();
            assertThat(nativePhoneNumber).isNotEqualTo(expectedPhoneNumber);
        });

        // Checks if the decryption happened automatically when getting the row through JPA
        txRunner.doInTransaction(em -> {
            Phone phone = em.find(Phone.class, phoneId);
            assertThat(phone.getPhoneNumber()).isEqualTo(expectedPhoneNumber);
        });
    }
}
