package com.Petcare.Petcare.Repositories;

import com.Petcare.Petcare.Models.PlatformFee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlatformFeeRepository extends JpaRepository<PlatformFee, Long> {

    Optional<PlatformFee> findTopByIsActiveOrderByIdDesc(boolean isActive);
}
