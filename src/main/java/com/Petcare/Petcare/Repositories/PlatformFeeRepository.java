package com.Petcare.Petcare.Repositories;

import com.Petcare.Petcare.Models.PlatformFee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlatformFeeRepository extends JpaRepository<PlatformFee, Long> {
}
