package com.socialhub.socialhub.repository;

import com.socialhub.socialhub.model.ModerationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModerationLogRepository extends JpaRepository<ModerationLog, Long> {
    List<ModerationLog> findAllByOrderByCreatedAtDesc();
}