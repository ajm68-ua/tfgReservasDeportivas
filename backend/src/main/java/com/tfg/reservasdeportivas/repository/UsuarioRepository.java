package com.tfg.reservasdeportivas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.tfg.reservasdeportivas.model.Usuario;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
    java.util.List<Usuario> findByCentroId(Integer centroId);
}
