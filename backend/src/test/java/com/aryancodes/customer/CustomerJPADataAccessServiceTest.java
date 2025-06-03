package com.aryancodes.customer;

import com.aryancodes.exception.ResourceNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerJPADataAccessServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    private CustomerJPADataAccessService underTest;

    @BeforeEach
    void setUp() {
        underTest = new CustomerJPADataAccessService(customerRepository);
    }

    @AfterEach
    void tearDown() {
        reset(customerRepository);
    }

    @Test
    void selectAllCustomers() {
        // Given
        Pageable pageable = PageRequest.of(0, 1000);
        List<Customer> customers = List.of(
            new Customer(1L, "Alex", "alex@aryancodes.com", "password", 19, Gender.MALE),
            new Customer(2L, "Jamila", "jamila@gmail.com", "password", 25, Gender.FEMALE)
        );
        Page<Customer> customerPage = new PageImpl<>(customers, pageable, customers.size());
        when(customerRepository.findAll(pageable)).thenReturn(customerPage);

        // When
        List<Customer> actual = underTest.selectAllCustomers();

        // Then
        assertThat(actual).hasSize(customers.size());
        ArgumentCaptor<Pageable> pageableArgumentCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(customerRepository).findAll(pageableArgumentCaptor.capture());
        assertThat(pageableArgumentCaptor.getValue()).isEqualTo(Pageable.ofSize(1000));
    }

    @Test
    void selectCustomerById() {
        // Given
        Long id = 1L;
        Customer customer = new Customer(id, "Alex", "alex@gmail.com", "password", 19, Gender.MALE);
        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));

        // When
        Customer actual = underTest.selectCustomerById(id).get();

        // Then
        assertThat(actual).isEqualTo(customer);
    }

    @Test
    void willThrowWhenSelectCustomerByIdReturnsEmptyOptional() {
        // Given
        Long id = 1L;
        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        // When
        Optional<Customer> actual = underTest.selectCustomerById(id);

        // Then
        assertThat(actual).isEmpty();
    }

    @Test
    void insertCustomer() {
        // Given
        Customer customer = new Customer(
                1L, "Ali", "ali@gmail.com", "password", 2,
                Gender.MALE);

        // When
        underTest.insertCustomer(customer);

        // Then
        verify(customerRepository).save(customer);
    }

    @Test
    void existsCustomerWithEmail() {
        // Given
        String email = "alex@gmail.com";
        when(customerRepository.existsCustomerByEmail(email)).thenReturn(true);

        // When
        boolean actual = underTest.existsCustomerWithEmail(email);

        // Then
        assertThat(actual).isTrue();
    }

    @Test
    void existsCustomerWithId() {
        // Given
        Long id = 1L;
        when(customerRepository.existsCustomerById(id)).thenReturn(true);

        // When
        boolean actual = underTest.existsCustomerById(id);

        // Then
        assertThat(actual).isTrue();
    }

    @Test
    void deleteCustomerById() {
        // Given
        Long id = 1L;

        // When
        underTest.deleteCustomerById(id);

        // Then
        verify(customerRepository).deleteById(id);
    }

    @Test
    void updateCustomer() {
        // Given
        Long id = 1L;
        Customer customer = new Customer(id, "Alex", "alex@gmail.com", "password", 19, Gender.MALE);
        when(customerRepository.save(customer)).thenReturn(customer);

        // When
        underTest.updateCustomer(customer);

        // Then
        verify(customerRepository).save(customer);
    }

    @Test
    void updateCustomerProfileImageId() {
        // Given
        String profileImageId = "2222";
        Long customerId = 1L;

        // When
        underTest.updateCustomerProfileImageId(profileImageId, customerId);

        // Then
        verify(customerRepository).updateProfileImageId(profileImageId, customerId);
    }
}