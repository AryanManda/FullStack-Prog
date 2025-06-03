package com.aryancodes.customer;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

import com.aryancodes.customer.Customer;

@Transactional
public interface CustomerRepository
        extends JpaRepository<Customer, Long> {

    boolean existsCustomerByEmail(String email);
    boolean existsCustomerById(Long id);
    Optional<Customer> findCustomerByEmail(String email);
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Customer c SET c.profileImageId = ?1 WHERE c.id = ?2")
    int updateProfileImageId(String profileImageId, Long customerId);
}
