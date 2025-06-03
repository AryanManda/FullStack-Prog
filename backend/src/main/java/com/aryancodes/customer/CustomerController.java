package com.aryancodes.customer;

import com.aryancodes.jwt.JWTUtil;
import com.aryancodes.customer.CustomerDTO;
import com.aryancodes.customer.CustomerRegistrationRequest;
import com.aryancodes.customer.CustomerService;
import com.aryancodes.customer.CustomerUpdateRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("api/v1/customers")
@CrossOrigin(origins = "*")
public class CustomerController {

    private final CustomerService customerService;
    private final JWTUtil jwtUtil;

    public CustomerController(CustomerService customerService,
                              JWTUtil jwtUtil) {
        this.customerService = customerService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public List<CustomerDTO> getCustomers() {
        return customerService.getAllCustomers();
    }

    @GetMapping("{customerId}")
    public CustomerDTO getCustomer(
            @PathVariable("customerId") Long customerId) {
        return customerService.getCustomer(customerId);
    }

    @PostMapping
    public ResponseEntity<?> registerCustomer(
            @RequestBody CustomerRegistrationRequest request) {
        customerService.addCustomer(request);
        String jwtToken = jwtUtil.issueToken(request.email(), "ROLE_USER");
        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, jwtToken)
                .build();
    }

    @DeleteMapping("{customerId}")
    public void deleteCustomer(
            @PathVariable("customerId") Long customerId) {
        customerService.deleteCustomerById(customerId);
    }

    @PutMapping("{customerId}")
    public void updateCustomer(
            @PathVariable("customerId") Long customerId,
            @RequestBody CustomerUpdateRequest updateRequest) {
        customerService.updateCustomer(customerId, updateRequest);
    }

    @PostMapping(
            value = "{customerId}/profile-image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public void uploadCustomerProfileImage(
            @PathVariable("customerId") Long customerId,
            @RequestParam("file") MultipartFile file) {
        customerService.uploadCustomerProfileImage(customerId, file);
    }

    @GetMapping(
            value = "{customerId}/profile-image",
            produces = MediaType.IMAGE_JPEG_VALUE
    )
    public byte[] getCustomerProfileImage(
            @PathVariable("customerId") Long customerId) {
        return customerService.getCustomerProfileImage(customerId);
    }

}
