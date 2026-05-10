package com.vyg.eis.CRM.repository.CRM;

import com.vyg.eis.CRM.domain.CRM.UserEntity;
import com.vyg.eis.CRM.domain.CRM.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, UUID id);

    Page<UserEntity> findByRoleAndIsActive(UserRole role, boolean active, Pageable pageable);

    Page<UserEntity> findByIsActive(boolean active, Pageable pageable);

    Optional<UserEntity> findByPasswordResetToken(String token);

    List<UserEntity> findByRoleIn(List<UserRole> roles);

    long countByRoleAndIsActiveTrue(UserRole role);

    @Query("SELECT u FROM UserEntity u WHERE " +
           "LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<UserEntity> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
