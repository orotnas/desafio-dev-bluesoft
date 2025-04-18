package br.com.bluesoft.erp.testecandidatos.repository;

import java.util.List;
import java.util.Optional;

/**
 * Interface base para repositórios.
 * @param <T> Tipo da entidade
 * @param <ID> Tipo do identificador da entidade
 */
public interface BaseRepository<T, ID> {
    
    T save(T entity);
    
    Optional<T> findById(ID id);
    
    List<T> findAll();
    
    void delete(T entity);
    
    void deleteById(ID id);
    
    boolean existsById(ID id);
    
    long count();
}