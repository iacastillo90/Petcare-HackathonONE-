-- =====================================================
-- Petcare Platform - Initial Database Schema
-- Version: 1.0.0
-- Description: Initial migration with all core entities
-- =====================================================

-- -----------------------------------------------------
-- Table: users
-- Core user table with Spring Security integration
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    address TEXT NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'CLIENT',
    permission_level VARCHAR(20) NOT NULL DEFAULT 'BASIC',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    email_verified_at DATETIME,
    last_login_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_user_email (email),
    INDEX idx_user_role (role),
    INDEX idx_user_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- Table: accounts
-- Family/business account for grouping pets and billing
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_user_id BIGINT NOT NULL,
    account_number VARCHAR(50) NOT NULL UNIQUE,
    account_name VARCHAR(100) NOT NULL,
    balance DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_account_owner_user FOREIGN KEY (owner_user_id) 
        REFERENCES users(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    
    INDEX idx_account_owner_user_id (owner_user_id),
    INDEX idx_account_number (account_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- Table: account_users
-- Join table for multi-user account membership with permissions
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS account_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    can_manage_payments BOOLEAN NOT NULL DEFAULT FALSE,
    can_manage_pets BOOLEAN NOT NULL DEFAULT FALSE,
    can_make_bookings BOOLEAN NOT NULL DEFAULT TRUE,
    added_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_accountuser_account FOREIGN KEY (account_id) 
        REFERENCES accounts(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_accountuser_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT uk_account_user UNIQUE (account_id, user_id),
    
    INDEX idx_account_user_account_id (account_id),
    INDEX idx_account_user_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- Table: sitter_profiles
-- Professional profiles for pet sitters
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS sitter_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    bio TEXT,
    hourly_rate DECIMAL(8,2) NOT NULL,
    servicing_radius INT NOT NULL,
    profile_image_url VARCHAR(500),
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    average_rating DECIMAL(3,2),
    is_available_for_bookings BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_sitterprofile_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
    
    INDEX idx_sitterprofile_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- Table: sitter_work_experience
-- Work experience entries for sitters
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS sitter_work_experience (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sitter_profile_id BIGINT NOT NULL,
    company_name VARCHAR(250),
    job_title VARCHAR(250),
    responsibilities TEXT,
    start_date DATE NOT NULL,
    end_date DATE,
    
    CONSTRAINT fk_workexperience_sitterprofile FOREIGN KEY (sitter_profile_id) 
        REFERENCES sitter_profiles(id) ON DELETE CASCADE ON UPDATE CASCADE,
    
    INDEX idx_swe_sitter_profile_id (sitter_profile_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- Table: discount_coupon
-- System-wide discount coupons
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS discount_coupon (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    coupon_code VARCHAR(100) NOT NULL UNIQUE,
    discount_type VARCHAR(20) NOT NULL,
    discount_value DECIMAL(10,2) NOT NULL,
    expiry_date DATETIME NOT NULL,
    max_uses INT NOT NULL,
    used_count INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME,
    
    INDEX idx_coupon_code (coupon_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- Table: service_offering
-- Services offered by sitters
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS service_offering (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sitter_id BIGINT NOT NULL,
    service_type VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    duration_in_minutes INT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_service_offering_sitter (sitter_id),
    INDEX idx_service_offering_type (service_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- Table: pets
-- Pet information with medical and care details
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS pets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    species VARCHAR(50),
    breed VARCHAR(100),
    age INT,
    weight DECIMAL(5,2),
    gender VARCHAR(20),
    color VARCHAR(50),
    physical_description TEXT,
    medications TEXT,
    allergies TEXT,
    vaccinations TEXT,
    special_notes TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_pet_account FOREIGN KEY (account_id) 
        REFERENCES accounts(id) ON DELETE CASCADE ON UPDATE CASCADE,
    
    INDEX idx_pet_account_id (account_id),
    INDEX idx_pet_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- Table: bookings
-- Service reservation records
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id BIGINT NOT NULL,
    pet_id BIGINT NOT NULL,
    sitter_id BIGINT NOT NULL,
    service_offering_id BIGINT NOT NULL,
    booked_by_user_id BIGINT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    actual_start_time DATETIME,
    actual_end_time DATETIME,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    total_price DECIMAL(10,2) NOT NULL,
    notes TEXT,
    cancellation_reason TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_booking_account FOREIGN KEY (account_id) 
        REFERENCES accounts(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_booking_pet FOREIGN KEY (pet_id) 
        REFERENCES pets(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_booking_sitter FOREIGN KEY (sitter_id) 
        REFERENCES users(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_booking_service_offering FOREIGN KEY (service_offering_id) 
        REFERENCES service_offering(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_booking_booked_by_user FOREIGN KEY (booked_by_user_id) 
        REFERENCES users(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    
    INDEX idx_booking_account (account_id),
    INDEX idx_booking_status (status),
    INDEX idx_booking_sitter (sitter_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- Table: reviews
-- User reviews for pets/services
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    pet_id BIGINT NOT NULL,
    rating INT NOT NULL,
    comment VARCHAR(1000),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME,
    
    INDEX idx_review_user_id (user_id),
    INDEX idx_review_pet_id (pet_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- Table: invoices
-- Billing documents for completed services
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS invoices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id BIGINT NOT NULL,
    booking_id BIGINT NOT NULL,
    invoice_number VARCHAR(50) NOT NULL UNIQUE,
    issue_date DATETIME NOT NULL,
    due_date DATETIME NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    platform_fee DECIMAL(10,2) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    notes TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_invoice_account FOREIGN KEY (account_id) 
        REFERENCES accounts(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_invoice_booking FOREIGN KEY (booking_id) 
        REFERENCES bookings(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    
    INDEX idx_invoice_number (invoice_number),
    INDEX idx_invoice_account (account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- Table: invoice_items
-- Line items for invoice details
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS invoice_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    description VARCHAR(500) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    line_total DECIMAL(10,2) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_invoiceitem_invoice FOREIGN KEY (invoice_id) 
        REFERENCES invoices(id) ON DELETE CASCADE ON UPDATE CASCADE,
    
    INDEX idx_invoiceitem_invoice_id (invoice_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- Table: platform_fees
-- Fee breakdown per booking for auditing
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS platform_fees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL UNIQUE,
    base_amount DECIMAL(10,2) NOT NULL,
    fee_percentage DECIMAL(5,2) NOT NULL,
    fee_amount DECIMAL(10,2) NOT NULL,
    net_amount DECIMAL(10,2) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_platformfee_booking FOREIGN KEY (booking_id) 
        REFERENCES bookings(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- Table: payment_methods
-- Saved payment methods for accounts
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS payment_methods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id BIGINT NOT NULL,
    card_type VARCHAR(50) NOT NULL,
    last_four_digits VARCHAR(4) NOT NULL,
    expiry_date VARCHAR(7),
    gateway_token VARCHAR(255) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_paymentmethod_account FOREIGN KEY (account_id) 
        REFERENCES accounts(id) ON DELETE CASCADE ON UPDATE CASCADE,
    
    INDEX idx_paymentmethod_account_id (account_id),
    INDEX idx_paymentmethod_gateway_token (gateway_token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- Table: payments
-- Payment transactions via payment gateway
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    payment_method_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    transaction_id VARCHAR(255),
    gateway_response TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    processed_at DATETIME,
    authorization_code VARCHAR(50),
    failure_reason TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_payment_invoice FOREIGN KEY (invoice_id) 
        REFERENCES invoices(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_payment_method FOREIGN KEY (payment_method_id) 
        REFERENCES payment_methods(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    
    INDEX idx_payment_invoice_id (invoice_id),
    INDEX idx_payment_transaction_id (transaction_id),
    INDEX idx_payment_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- Table: applied_coupon
-- Coupon usage records per booking
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS applied_coupon (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    coupon_id BIGINT NOT NULL,
    discount_amount DECIMAL(12,2) NOT NULL,
    applied_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_appliedcoupon_discountcoupon FOREIGN KEY (coupon_id) 
        REFERENCES discount_coupon(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    
    INDEX idx_appliedcoupon_account_id (account_id),
    INDEX idx_appliedcoupon_booking_id (booking_id),
    INDEX idx_appliedcoupon_coupon_id (coupon_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
