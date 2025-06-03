package com.aryancodes.customer;

import com.aryancodes.customer.Customer;
import com.aryancodes.customer.CustomerDao;
import com.aryancodes.customer.CustomerDTOMapper;
import com.aryancodes.customer.CustomerRegistrationRequest;
import com.aryancodes.customer.CustomerService;
import com.aryancodes.customer.CustomerUpdateRequest;
import com.aryancodes.exception.DuplicateResourceException;
import com.aryancodes.exception.RequestValidationException;
import com.aryancodes.exception.ResourceNotFoundException;
import com.aryancodes.s3.S3Buckets;
import com.aryancodes.s3.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    @Mock
    private CustomerDao customerDao;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private CustomerDTOMapper customerDTOMapper;
    @Mock
    private S3Service s3Service;
    @Mock
    private S3Buckets s3Buckets;
    private CustomerService underTest;
    private final CustomerDTOMapper customerDTOMapperReal = new CustomerDTOMapper();

    @BeforeEach
    void setUp() {
        underTest = new CustomerService(
                customerDao,
                customerDTOMapperReal,
                passwordEncoder,
                s3Service,
                s3Buckets
        );
    }

    @Test
    void getAllCustomers() {
        // Given
        Customer customer = new Customer(
                1L,
                "Alex",
                "alex@aryancodes.com",
                "password", 19,
                Gender.MALE
        );
        List<Customer> customers = List.of(customer);
        when(customerDao.selectAllCustomers()).thenReturn(customers);

        // When
        List<CustomerDTO> actual = underTest.getAllCustomers();

        // Then
        assertThat(actual).hasSize(customers.size());
        verify(customerDao).selectAllCustomers();
    }

    @Test
    void canGetCustomer() {
        // Given
        Long id = 10L;
        Customer customer = new Customer(id, "Alex", "alex@gmail.com", "password", 19, Gender.MALE);
        when(customerDao.selectCustomerById(id)).thenReturn(Optional.of(customer));

        CustomerDTO expected = customerDTOMapperReal.apply(customer);

        // When
        CustomerDTO actual = underTest.getCustomer(id);

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void willThrowWhenGetCustomerReturnEmptyOptional() {
        // Given
        Long id = 10L;

        when(customerDao.selectCustomerById(id)).thenReturn(Optional.empty());

        // When
        // Then
        assertThatThrownBy(() -> underTest.getCustomer(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("customer with id [%s] not found".formatted(id));
    }

    @Test
    void addCustomer() {
        // Given
        String email = "alex@gmail.com";

        when(customerDao.existsCustomerWithEmail(email)).thenReturn(false);

        CustomerRegistrationRequest request = new CustomerRegistrationRequest("Alex", email, "password", 19, Gender.MALE);

        String passwordHash = "¢5554ml;f;lsd";

        when(passwordEncoder.encode(request.password())).thenReturn(passwordHash);

        // When
        underTest.addCustomer(request);

        // Then
        ArgumentCaptor<Customer> customerArgumentCaptor = ArgumentCaptor.forClass(Customer.class);

        verify(customerDao).insertCustomer(customerArgumentCaptor.capture());

        Customer capturedCustomer = customerArgumentCaptor.getValue();

        assertThat(capturedCustomer.getId()).isNull();
        assertThat(capturedCustomer.getName()).isEqualTo(request.name());
        assertThat(capturedCustomer.getEmail()).isEqualTo(request.email());
        assertThat(capturedCustomer.getAge()).isEqualTo(request.age());
        assertThat(capturedCustomer.getPassword()).isEqualTo(passwordHash);
    }

    @Test
    void willThrowWhenEmailExistsWhileAddingACustomer() {
        // Given
        String email = "alex@gmail.com";

        when(customerDao.existsCustomerWithEmail(email)).thenReturn(true);

        CustomerRegistrationRequest request = new CustomerRegistrationRequest("Alex", email, "password", 19, Gender.MALE);

        // When
        assertThatThrownBy(() -> underTest.addCustomer(request)).isInstanceOf(DuplicateResourceException.class).hasMessage("email already taken");

        // Then
        verify(customerDao, never()).insertCustomer(any());
    }

    @Test
    void deleteCustomerById() {
        // Given
        Long id = 10L;

        when(customerDao.existsCustomerById(id)).thenReturn(true);

        // When
        underTest.deleteCustomerById(id);
        // Then
        verify(customerDao).deleteCustomerById(id);
    }

    @Test
    void willThrowDeleteCustomerByIdNotExists() {
        // Given
        Long id = 10L;

        when(customerDao.existsCustomerById(id)).thenReturn(false);

        // When
        assertThatThrownBy(() -> underTest.deleteCustomerById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("customer with id [%s] not found".formatted(id));

        // Then
        verify(customerDao, never()).deleteCustomerById(id);
    }

    @Test
    void canUpdateAllCustomersProperties() {
        // Given
        Long id = 10L;
        Customer customer = new Customer(id, "Alex", "alex@gmail.com", "password", 19, Gender.MALE);
        when(customerDao.selectCustomerById(id)).thenReturn(Optional.of(customer));

        String newEmail = "alexandro@aryancodes.com";

        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest("Alexandro", newEmail, 23);

        when(customerDao.existsCustomerWithEmail(newEmail)).thenReturn(false);

        // When
        underTest.updateCustomer(id, updateRequest);

        // Then
        ArgumentCaptor<Customer> customerArgumentCaptor = ArgumentCaptor.forClass(Customer.class);

        verify(customerDao).updateCustomer(customerArgumentCaptor.capture());
        Customer capturedCustomer = customerArgumentCaptor.getValue();

        assertThat(capturedCustomer.getName()).isEqualTo(updateRequest.name());
        assertThat(capturedCustomer.getEmail()).isEqualTo(updateRequest.email());
        assertThat(capturedCustomer.getAge()).isEqualTo(updateRequest.age());
    }

    @Test
    void canUpdateOnlyCustomerName() {
        // Given
        Long id = 10L;
        Customer customer = new Customer(id, "Alex", "alex@gmail.com", "password", 19, Gender.MALE);
        when(customerDao.selectCustomerById(id)).thenReturn(Optional.of(customer));

        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest("Alexandro", null, null);

        // When
        underTest.updateCustomer(id, updateRequest);

        // Then
        ArgumentCaptor<Customer> customerArgumentCaptor = ArgumentCaptor.forClass(Customer.class);

        verify(customerDao).updateCustomer(customerArgumentCaptor.capture());
        Customer capturedCustomer = customerArgumentCaptor.getValue();

        assertThat(capturedCustomer.getName()).isEqualTo(updateRequest.name());
        assertThat(capturedCustomer.getAge()).isEqualTo(customer.getAge());
        assertThat(capturedCustomer.getEmail()).isEqualTo(customer.getEmail());
    }

    @Test
    void canUpdateOnlyCustomerEmail() {
        // Given
        Long id = 10L;
        Customer customer = new Customer(id, "Alex", "alex@gmail.com", "password", 19, Gender.MALE);
        when(customerDao.selectCustomerById(id)).thenReturn(Optional.of(customer));

        String newEmail = "alexandro@aryancodes.com";

        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest(null, newEmail, null);

        when(customerDao.existsCustomerWithEmail(newEmail)).thenReturn(false);

        // When
        underTest.updateCustomer(id, updateRequest);

        // Then
        ArgumentCaptor<Customer> customerArgumentCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerDao).updateCustomer(customerArgumentCaptor.capture());
        Customer capturedCustomer = customerArgumentCaptor.getValue();

        assertThat(capturedCustomer.getName()).isEqualTo(customer.getName());
        assertThat(capturedCustomer.getAge()).isEqualTo(customer.getAge());
        assertThat(capturedCustomer.getEmail()).isEqualTo(updateRequest.email());
    }

    @Test
    void canUpdateOnlyCustomerAge() {
        // Given
        Long id = 10L;
        Customer customer = new Customer(id, "Alex", "alex@gmail.com", "password", 19, Gender.MALE);
        when(customerDao.selectCustomerById(id)).thenReturn(Optional.of(customer));

        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest(null, null, 23);

        // When
        underTest.updateCustomer(id, updateRequest);

        // Then
        ArgumentCaptor<Customer> customerArgumentCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerDao).updateCustomer(customerArgumentCaptor.capture());
        Customer capturedCustomer = customerArgumentCaptor.getValue();

        assertThat(capturedCustomer.getName()).isEqualTo(customer.getName());
        assertThat(capturedCustomer.getAge()).isEqualTo(updateRequest.age());
        assertThat(capturedCustomer.getEmail()).isEqualTo(customer.getEmail());
    }

    @Test
    void willThrowWhenTryingToUpdateCustomerEmailWhenAlreadyTaken() {
        // Given
        Long id = 10L;
        Customer customer = new Customer(id, "Alex", "alex@gmail.com", "password", 19, Gender.MALE);
        when(customerDao.selectCustomerById(id)).thenReturn(Optional.of(customer));

        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest(null, "alexandro@aryancodes.com", null);

        when(customerDao.existsCustomerWithEmail(updateRequest.email())).thenReturn(true);

        // When
        assertThatThrownBy(() -> underTest.updateCustomer(id, updateRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("email already taken");

        // Then
        verify(customerDao, never()).updateCustomer(any());
    }

    @Test
    void willThrowWhenCustomerUpdateHasNoChanges() {
        // Given
        Long id = 10L;
        Customer customer = new Customer(id, "Alex", "alex@gmail.com", "password", 19, Gender.MALE);
        when(customerDao.selectCustomerById(id)).thenReturn(Optional.of(customer));

        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest(
                customer.getName(),
                customer.getEmail(),
                customer.getAge()
        );

        // When
        assertThatThrownBy(() -> underTest.updateCustomer(id, updateRequest))
                .isInstanceOf(RequestValidationException.class)
                .hasMessage("no data changes found");

        // Then
        verify(customerDao, never()).updateCustomer(any());
    }

    @Test
    void canUploadProfileImage() {
        // Given
        Long customerId = 10L;

        when(customerDao.existsCustomerById(customerId)).thenReturn(true);

        byte[] bytes = "test image".getBytes();
        MultipartFile file = new MockMultipartFile("file", bytes);

        String s3TestBucket = "test-bucket";
        when(s3Buckets.getCustomer()).thenReturn(s3TestBucket);

        String profileImageId = UUID.randomUUID().toString();
        doNothing().when(s3Service).putObject(
                eq(s3TestBucket),
                anyString(),
                eq(bytes)
        );

        doNothing().when(customerDao).updateCustomerProfileImageId(anyString(), eq(customerId));

        // When
        underTest.uploadCustomerProfileImage(customerId, file);

        // Then
        ArgumentCaptor<String> profileImageIdArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> customerIdArgumentCaptor = ArgumentCaptor.forClass(Long.class);

        verify(customerDao).updateCustomerProfileImageId(
                profileImageIdArgumentCaptor.capture(),
                customerIdArgumentCaptor.capture()
        );

        String capturedProfileImageId = profileImageIdArgumentCaptor.getValue();
        Long capturedCustomerId = customerIdArgumentCaptor.getValue();

        assertThat(capturedProfileImageId).isNotNull();
        assertThat(capturedCustomerId).isEqualTo(customerId);
    }

    @Test
    void cannotUploadProfileImageWhenCustomerDoesNotExists() {
        // Given
        Long customerId = 10L;

        when(customerDao.existsCustomerById(customerId)).thenReturn(false);

        byte[] bytes = "test image".getBytes();
        MultipartFile file = new MockMultipartFile("file", bytes);

        // When
        assertThatThrownBy(() -> underTest.uploadCustomerProfileImage(customerId, file))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("customer with id [%s] not found".formatted(customerId));

        // Then
        verify(customerDao, never()).updateCustomerProfileImageId(any(), any());
        verify(s3Service, never()).putObject(any(), any(), any());
    }

    @Test
    void cannotUploadProfileImageWhenExceptionIsThrown() throws IOException {
        // Given
        Long customerId = 10L;

        when(customerDao.existsCustomerById(customerId)).thenReturn(true);

        byte[] bytes = "test image".getBytes();
        MultipartFile file = mock(MultipartFile.class);

        when(file.getBytes()).thenThrow(IOException.class);

        String s3TestBucket = "test-bucket";
        when(s3Buckets.getCustomer()).thenReturn(s3TestBucket);

        // When
        assertThatThrownBy(() -> underTest.uploadCustomerProfileImage(customerId, file))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("failed to upload profile image");

        // Then
        verify(customerDao, never()).updateCustomerProfileImageId(any(), any());
        verify(s3Service, never()).putObject(any(), any(), any());
    }

    @Test
    void canDownloadProfileImage() {
        // Given
        Long customerId = 10L;
        String profileImageId = "2222";
        Customer customer = new Customer(
                customerId,
                "Alex",
                "alex@gmail.com",
                "password",
                19,
                Gender.MALE,
                profileImageId
        );
        when(customerDao.selectCustomerById(customerId)).thenReturn(Optional.of(customer));

        byte[] s3Image = "image bytes".getBytes();
        String s3TestBucket = "test-bucket";
        when(s3Buckets.getCustomer()).thenReturn(s3TestBucket);
        when(s3Service.getObject(
                s3TestBucket,
                "profile-images/%s/%s".formatted(customerId, profileImageId)
        )).thenReturn(s3Image);

        // When
        byte[] actual = underTest.getCustomerProfileImage(customerId);

        // Then
        assertThat(actual).isEqualTo(s3Image);
    }

    @Test
    void cannotDownloadWhenNoProfileImageId() {
        // Given
        Long customerId = 10L;
        Customer customer = new Customer(
                customerId,
                "Alex",
                "alex@gmail.com",
                "password",
                19,
                Gender.MALE
        );

        when(customerDao.selectCustomerById(customerId)).thenReturn(Optional.of(customer));

        // When
        assertThatThrownBy(() -> underTest.getCustomerProfileImage(customerId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("customer with id [%s] profile image not found".formatted(customerId));

        // Then
        verify(s3Service, never()).getObject(any(), any());
    }

    @Test
    void cannotDownloadProfileImageWhenCustomerDoesNotExists() {
        // Given
        Long customerId = 10L;

        when(customerDao.selectCustomerById(customerId)).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> underTest.getCustomerProfileImage(customerId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("customer with id [%s] not found".formatted(customerId));

        // Then
        verify(customerDao, never()).existsCustomerById(any());
        verify(s3Service, never()).getObject(any(), any());
    }
}