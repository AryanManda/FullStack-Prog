package com.aryancodes.customer;

import com.aryancodes.customer.Customer;

import java.util.List;
import java.util.Optional;

public interface CustomerDao {
    List<Customer> selectAllCustomers();

    Optional<Customer> selectCustomerById(Long customerId);

    void insertCustomer(Customer customer);
    
    boolean existsCustomerWithEmail(String email);
    
    boolean existsCustomerById(Long customerId);
    
    void deleteCustomerById(Long customerId);

    void updateCustomer(Customer update);
    
    Optional<Customer> selectUserByEmail(String email);
    
    void updateCustomerProfileImageId(String profileImageId, Long customerId);
}
