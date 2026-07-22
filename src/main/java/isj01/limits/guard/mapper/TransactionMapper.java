package isj01.limits.guard.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import isj01.limits.guard.db.entity.Transaction;
import isj01.limits.guard.dto.TransactionCreateDto;
import isj01.limits.guard.dto.TransactionReadDto;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "id", ignore = true)
    Transaction toEntity(TransactionCreateDto createDto);

    TransactionReadDto toReadDto(Transaction entity);

}
