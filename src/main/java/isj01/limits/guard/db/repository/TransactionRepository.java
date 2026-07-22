package isj01.limits.guard.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import isj01.limits.guard.db.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
