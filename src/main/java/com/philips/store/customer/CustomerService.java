package com.philips.store.customer;

 

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CustomerService {

	private final CustomerRepository customerRepository;

	public CustomerService(CustomerRepository customerRepository) {
		this.customerRepository = customerRepository;
	}

	@Transactional
	public CustomerResponse createCustomer(CustomerRequest request) {

		String email = request.getEmail().trim().toLowerCase();

		if (customerRepository.existsByEmailIgnoreCase(email)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Customer email already exists");
		}

		CustomerEntity customer = new CustomerEntity();
		customer.setFullName(request.getFullName().trim());
		customer.setEmail(email);
		customer.setPhone(request.getPhone());

		CustomerEntity saved = customerRepository.save(customer);

		return toResponse(saved);
	}

	@Transactional(readOnly = true)
	public List<CustomerResponse> getAllCustomers() {

		return customerRepository.findAll().stream().filter(CustomerEntity::isActive).map(this::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public CustomerResponse getCustomerById(Long id) {

		CustomerEntity customer = findActiveCustomer(id);

		return toResponse(customer);
	}

	@Transactional
	public CustomerResponse updateCustomer(Long id, CustomerRequest request) {

		CustomerEntity customer = findActiveCustomer(id);

		String email = request.getEmail().trim().toLowerCase();

		boolean duplicate = customerRepository.existsByEmailIgnoreCase(email);

		if (duplicate && !customer.getEmail().equalsIgnoreCase(email)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Customer email already exists");
		}

		customer.setFullName(request.getFullName().trim());
		customer.setEmail(email);
		customer.setPhone(request.getPhone());

		return toResponse(customerRepository.save(customer));
	}

	@Transactional
	public void deactivateCustomer(Long id) {

		CustomerEntity customer = findActiveCustomer(id);
		customer.setActive(false);

		customerRepository.save(customer);
	}

	private CustomerEntity findActiveCustomer(Long id) {

		CustomerEntity customer = customerRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

		if (!customer.isActive()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
		}

		return customer;
	}

	private CustomerResponse toResponse(CustomerEntity customer) {

		return new CustomerResponse(customer.getId(), customer.getFullName(), customer.getEmail(), customer.getPhone(),
				customer.isActive(), customer.getCreatedAt());
	}
}
