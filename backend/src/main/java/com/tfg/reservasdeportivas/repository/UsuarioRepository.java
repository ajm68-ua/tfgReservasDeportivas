package com.tfg.reservasdeportivas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.tfg.reservasdeportivas.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
}
