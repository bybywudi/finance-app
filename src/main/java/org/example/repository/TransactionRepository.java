package org.example.repository;

import org.example.model.Transaction;
import org.example.model.TransactionRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class TransactionRepository {
    private final ConcurrentHashMap<Long, Transaction> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    public Transaction save(Transaction transaction) {
        Long id = idGenerator.incrementAndGet();
        Transaction newTransaction = transaction.withId(id);
        storage.put(id, newTransaction);
        return newTransaction;
    }

    public Page<Transaction> findAll(int page, int size) {
        List<Transaction> values = new ArrayList<>(storage.values());
        int total = values.size();
        int start = page * size;
        int end = Math.min(start + size, total);

        return new PageImpl<>(
                values.subList(start, end),
                PageRequest.of(page, size),
                total
        );
    }

    public Optional<Transaction> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public boolean existsById(Long id) {
        return storage.containsKey(id);
    }

    public void delete(Long id) {
        storage.remove(id);
    }

    public void deleteAll() {
        storage.clear();
        idGenerator.set(0);
    }

    public boolean existsDuplicate(TransactionRequest request) {
        return storage.values().stream()
                .anyMatch(t -> t.amount().equals(request.amount())
                        && t.type() == request.type()
                        && t.description().equals(request.description()));
    }
}