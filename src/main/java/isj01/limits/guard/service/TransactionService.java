package isj01.limits.guard.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import isj01.limits.guard.db.entity.Transaction;
import isj01.limits.guard.db.repository.TransactionRepository;
import isj01.limits.guard.dto.TransactionCreateDto;
import isj01.limits.guard.mapper.TransactionMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionRepository repository;
    private final TransactionMapper mapper;

    @Transactional
    public Long create(TransactionCreateDto createDto) {
        Transaction entity = mapper.toEntity(createDto);
        return repository.save(entity).getId();
    }

}
