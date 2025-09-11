package com.demo.qlexpress.repository;

import com.demo.qlexpress.model.SecurityRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SecurityRuleRepository extends JpaRepository<SecurityRule, Long> {
    List<SecurityRule> findByEnabledTrue();
}