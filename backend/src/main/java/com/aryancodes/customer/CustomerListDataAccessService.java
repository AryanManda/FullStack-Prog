package com.aryancodes.customer;

import com.aryancodes.customer.Customer;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository("list")
public class CustomerListDataAccessService implements CustomerDao {

    // db
    private static final List<Customer> customers;

    static {
        customers = new ArrayList<>();

        Customer alex = new Customer(
                1L,
                "Alex",
                "alex@gmail.com",
                "password",
                21,
                Gender.MALE);
        customers.add(alex);

        Customer jamila = new Customer(
                2L,
                "Jamila",
                "jamila@gmail.com",
                "password",
                19,
                Gender.MALE);
        customers.add(jamila);
    }

    @Override
    public List<Customer> selectAllCustomers() {
        return customers;
    }

    @Override
    public Optional<Customer> selectCustomerById(Long id) {
        return customers.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst();
    }

    @Override
    public void insertCustomer(Customer customer) {
        customers.add(customer);
    }

    @Override
    public boolean existsCustomerWithEmail(String email) {
        return customers.stream()
                .anyMatch(c -> c.getEmail().equals(email));
    }

    @Override
    public boolean existsCustomerById(Long customerId) {
        return customers.stream()
                .anyMatch(c -> c.getId().equals(customerId));
    }

    @Override
    public void deleteCustomerById(Long customerId) {
        customers.stream()
                .filter(c -> c.getId().equals(customerId))
                .findFirst()
                .ifPresent(customers::remove);
    }

    @Override
    public void updateCustomer(Customer customer) {
        customers.add(customer);
    }

    @Override
    public Optional<Customer> selectUserByEmail(String email) {
        return customers.stream()
                .filter(c -> c.getUsername().equals(email))
                .findFirst();
    }

    @Override
    public void updateCustomerProfileImageId(String profileImageId, Long customerId) {
        customers.stream()
                .filter(c -> c.getId().equals(customerId))
                .findFirst()
                .ifPresent(customer -> customer.setProfileImageId(profileImageId));
    }
}
