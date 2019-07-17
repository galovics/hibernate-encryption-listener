package com.arnoldgalovics.blog.hibernateencryptionlistener;

import com.arnoldgalovics.blog.hibernateencryptionlistener.repository.Phone;
import net.ttddyy.dsproxy.QueryCountHolder;
import org.junit.After;
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

    @After
    public void tearDown() {
        txRunner.doInTransaction(em -> {
            em.createQuery("DELETE FROM Phone").executeUpdate();
        });
        QueryCountHolder.clear();
    }

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
        assertThat(QueryCountHolder.getGrandTotal().getInsert()).isEqualTo(1);
        assertThat(QueryCountHolder.getGrandTotal().getSelect()).isEqualTo(2);
        assertThat(QueryCountHolder.getGrandTotal().getUpdate()).isEqualTo(0);
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
        assertThat(QueryCountHolder.getGrandTotal().getInsert()).isEqualTo(1);
        assertThat(QueryCountHolder.getGrandTotal().getSelect()).isEqualTo(4);
        assertThat(QueryCountHolder.getGrandTotal().getUpdate()).isEqualTo(1);
    }

    @Test
    public void testNullWorks() {
        // Persisting a phone entity with a null field value
        UUID phoneId = txRunner.doInTransaction(em -> {
            Phone newPhone = new Phone(null);
            em.persist(newPhone);
            return newPhone.getId();
        });

        // Checks if the database has the null value
        txRunner.doInTransaction(em -> {
            Query query = em.createNativeQuery("SELECT phone_number FROM Phone where id = :phoneId");
            query.setParameter("phoneId", phoneId);
            String nativePhoneNumber = (String) query.getSingleResult();
            assertThat(nativePhoneNumber).isNull();
        });

        // Checks if the decryption handles null value from db
        txRunner.doInTransaction(em -> {
            Phone phone = em.find(Phone.class, phoneId);
            assertThat(phone.getPhoneNumber()).isNull();
        });
    }
}
