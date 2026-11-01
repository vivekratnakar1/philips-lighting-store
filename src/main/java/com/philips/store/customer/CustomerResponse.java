package com.philips.store.customer;

import java.time.LocalDateTime;

public class CustomerResponse {

	private Long id;
	private String fullName;
	private String email;
	private String phone;
	private boolean active;
	private LocalDateTime createdAt;

	public CustomerResponse(Long id, String fullName, String email, String phone, boolean active,
			LocalDateTime createdAt) {

		this.id = id;
		this.fullName = fullName;
		this.email = email;
		this.phone = phone;
		this.active = active;
		this.createdAt = createdAt;
	}

	public Long getId() {
		return id;
	}

	public String getFullName() {
		return fullName;
	}

	public String getEmail() {
		return email;
	}

	public String getPhone() {
		return phone;
	}

	public boolean isActive() {
		return active;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
}
