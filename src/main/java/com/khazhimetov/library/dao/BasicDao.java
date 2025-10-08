package com.khazhimetov.library.dao;

import java.util.List;
import java.util.Optional;

/**
 * Base DAO interface with common CRUD operations
 * @param <T> Entity type
 * @param <ID> Primary key type
 */
public interface BasicDao<T, ID> {

    /**
     * Find entity by ID
     * @param id entity ID
     * @return Optional containing entity if found
     */
    Optional<T> findById(ID id);

    /**
     * Find all entities
     * @return list of all entities
     */
    List<T> findAll();

    /**
     * Save new entity
     * @param entity entity to save
     * @return saved entity
     */
    T save(T entity);

    /**
     * Update existing entity
     * @param entity entity to update
     * @return updated entity
     */
    T update(T entity);

    /**
     * Delete entity by ID
     * @param id entity ID
     * @return true if deleted, false otherwise
     */
    boolean deleteById(ID id);
}
