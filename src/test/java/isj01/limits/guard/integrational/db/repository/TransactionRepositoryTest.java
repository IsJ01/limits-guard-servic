package isj01.limits.guard.integrational.db.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import isj01.limits.guard.db.entity.Transaction;
import isj01.limits.guard.db.repository.TransactionRepository;
import isj01.limits.guard.integrational.TestContainersConfig;
import jakarta.persistence.EntityManager;

@DataJpaTest
@Import(TestContainersConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository repository;
    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Should correctly save and retrieve transaction from Database")
    void shouldSaveAndFindTransaction() {
        Transaction newTransaction = new Transaction("user-123", 1000L, "electronics-shop");

        Transaction saved = repository.save(newTransaction);

        entityManager.flush();
        entityManager.clear();

        Optional<Transaction> retrieved = repository.findById(saved.getId());

        assertThat(retrieved)
                .isPresent()
                .get()
                .satisfies(transaction -> {
                    assertThat(transaction.getUserId()).isEqualTo("user-123");
                    assertThat(transaction.getAmount()).isEqualTo(1000L);
                });
    }

    @Test
    @DisplayName("Should return empty optional when transaction not found")
    void shouldReturnEmptyWhenNotFound() {
        Optional<Transaction> result = repository.findById(999999L);

        assertThat(result).isEmpty();
    }

}
