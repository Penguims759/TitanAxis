package com.titanaxis.repository;

import com.titanaxis.model.Cliente;
import java.util.List;

public interface ClienteRepository extends Repository<Cliente, Integer> {
    List<Cliente> findByNomeContaining(String nome);
}