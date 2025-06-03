package com.aryancodes.repositorytest;

import com.aryancodes.AbstractTestcontainers;
import com.aryancodes.customer.CustomerRepository;
import com.aryancodes.customer.Customer;
import com.aryancodes.customer.Gender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.ApplicationContext;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(excludeAutoConfiguration = com.aryancodes.Main.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CustomerRepositoryTest extends AbstractTestcontainers {

    @Autowired
    private CustomerRepository underTest;

    @Autowired
    private ApplicationContext applicationContext;

    @BeforeEach
    void setUp() {
        underTest.deleteAll();
        System.out.println(applicationContext.getBeanDefinitionCount());
    }

    @Test
    void existsCustomerByEmail() {
        // Given
        String email = FAKER.internet().safeEmailAddress() + "-" + UUID.randomUUID();
        Customer customer = new Customer(
                FAKER.name().fullName(),
                email,
                "password",
                20,
                Gender.MALE);

        underTest.save(customer);

        // When
        var actual = underTest.existsCustomerByEmail(email);

        // Then
        assertThat(actual).isTrue();
    }

    @Test
    void existsCustomerByEmailFailsWhenEmailNotPresent() {
        // Given
        String email = FAKER.internet().safeEmailAddress() + "-" + UUID.randomUUID();

        // When
        var actual = underTest.existsCustomerByEmail(email);

        // Then
        assertThat(actual).isFalse();
    }

    @Test
    void existsCustomerById() {
        // Given
        String email = FAKER.internet().safeEmailAddress() + "-" + UUID.randomUUID();
        Customer customer = new Customer(
                FAKER.name().fullName(),
                email,
                "password", 20,
                Gender.MALE);

        underTest.save(customer);

        Long id = underTest.findAll()
                .stream()
                .filter(c -> c.getEmail().equals(email))
                .map(Customer::getId)
                .findFirst()
                .orElseThrow();

        // When
        boolean actual = underTest.existsCustomerById(id);

        // Then
        assertThat(actual).isTrue();
    }

    @Test
    void existsCustomerByIdFailsWhenIdNotPresent() {
        // Given
        Long id = 0L;

        // When
        boolean actual = underTest.existsCustomerById(id);

        // Then
        assertThat(actual).isFalse();
    }

    @Test
    void canUpdateProfileImageId() {
        // Given
        String email = "email";

        Customer customer = new Customer(
                FAKER.name().fullName(),
                email,
                "password", 20,
                Gender.MALE);

        underTest.save(customer);

        Long id = underTest.findAll()
                .stream()
                .filter(c -> c.getEmail().equals(email))
                .map(Customer::getId)
                .findFirst()
                .orElseThrow();

        // When
        underTest.updateProfileImageId("2222", id);

        // Then
        Optional<Customer> customerOptional = underTest.findById(id);
        assertThat(customerOptional)
                .isPresent()
                .hasValueSatisfying(
                        c -> assertThat(c.getProfileImageId()).isEqualTo("2222")
                );
    }

    @Test
    void selectCustomerById() {
        // Given
        String email = FAKER.internet().safeEmailAddress() + "-" + UUID.randomUUID();
        Customer customer = new Customer(
                FAKER.name().fullName(),
                email,
                "password",
                20,
                Gender.MALE);

        underTest.save(customer);

        Long id = underTest.findAll()
                .stream()
                .filter(c -> c.getEmail().equals(email))
                .map(Customer::getId)
                .findFirst()
                .orElseThrow();

        // When
        Optional<Customer> actual = underTest.findById(id);

        // Then
        assertThat(actual).isPresent();
    }

    @Test
    void existsCustomerByEmailExists() {
        // Given
        String email = "foo@amigoscode.com";
        Customer customer = new Customer(
                1L,
                FAKER.name().fullName(),
                email,
                "password",
                20,
                Gender.MALE);

        underTest.save(customer);

        // When
        boolean actual = underTest.existsCustomerByEmail(email);

        // Then
        assertThat(actual).isTrue();
    }

    @Test
    void existsCustomerByEmailNotExists() {
        // Given
        String email = "foo@amigoscode.com";

        // When
        boolean actual = underTest.existsCustomerByEmail(email);

        // Then
        assertThat(actual).isFalse();
    }

    @Test
    void existsCustomerByIdExists() {
        // Given
        Long id = 1L;
        Customer customer = new Customer(
                id,
                FAKER.name().fullName(),
                FAKER.internet().safeEmailAddress(),
                "password",
                20,
                Gender.MALE);

        underTest.save(customer);

        // When
        boolean actual = underTest.existsCustomerById(id);

        // Then
        assertThat(actual).isTrue();
    }

    @Test
    void existsCustomerByIdNotExists() {
        // Given
        Long id = 1L;

        // When
        boolean actual = underTest.existsCustomerById(id);

        // Then
        assertThat(actual).isFalse();
    }

    @Test
    void deleteCustomerById() {
        // Given
        Long id = 1L;
        Customer customer = new Customer(
                id,
                FAKER.name().fullName(),
                FAKER.internet().safeEmailAddress(),
                "password",
                20,
                Gender.MALE);

        underTest.save(customer);

        // When
        underTest.deleteById(id);

        // Then
        Optional<Customer> actual = underTest.findById(id);
        assertThat(actual).isEmpty();
    }

    @Test
    void findCustomerByEmail() {
        // Given
        String email = "foo@amigoscode.com";
        Customer customer = new Customer(
                1L,
                FAKER.name().fullName(),
                email,
                "password",
                20,
                Gender.MALE);

        underTest.save(customer);

        // When
        Optional<Customer> actual = underTest.findCustomerByEmail(email);

        // Then
        assertThat(actual).isPresent();
    }

    @Test
    void updateProfileImageId() {
        // Given
        String profileImageId = "s3-bucket-name";
        Long customerId = 1L;
        Customer customer = new Customer(
                customerId,
                FAKER.name().fullName(),
                FAKER.internet().safeEmailAddress(),
                "password",
                20,
                Gender.MALE);

        underTest.save(customer);

        // When
        int result = underTest.updateProfileImageId(profileImageId, customerId);

        // Then
        assertThat(result).isEqualTo(1);
    }
}