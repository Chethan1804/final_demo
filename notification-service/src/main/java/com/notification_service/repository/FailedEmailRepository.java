package com.notification_service.repository;

import com.notification_service.entity.FailedEmail;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FailedEmailRepository extends JpaRepository<FailedEmail, Long> {
}