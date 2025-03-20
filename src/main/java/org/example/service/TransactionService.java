package org.example.service;

import org.example.model.Transaction;
import org.example.model.TransactionRequest;
import org.example.repository.TransactionRepository;
import org.example.service.exception.DuplicateTransactionException;
import org.example.service.exception.TransactionNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionService {

    @Autowired
    private TransactionRepository repository;

    @CachePut(value = "transactions", key = "#result.id")
    public Transaction create(TransactionRequest request) {
        if (repository.existsDuplicate(request)) {
            throw new DuplicateTransactionException();
        }
        return repository.save(request.toEntity());
    }

    @Cacheable(value = "transactions", key = "#id")
    public Transaction getById(Long id) {
        return repository.findById(id)
                .orElseThrow(TransactionNotFoundException::new);
    }

    @CacheEvict(value = "transactions", key = "#id")
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new TransactionNotFoundException();
        }
        repository.delete(id);
    }

    public Page<Transaction> list(int page, int size) {
        return repository.findAll(page, size);
    }

    public void deleteAll() {
        repository.deleteAll();
    }
}