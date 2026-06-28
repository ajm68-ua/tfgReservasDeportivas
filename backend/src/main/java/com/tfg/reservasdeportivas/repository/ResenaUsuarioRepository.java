package com.tfg.reservasdeportivas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.tfg.reservasdeportivas.model.ResenaUsuario;
import java.util.List;

public interface ResenaUsuarioRepository extends JpaRepository<ResenaUsuario, Integer> {
    List<ResenaUsuario> findByEvaluadoIdOrderByFechaCreacionDesc(Integer evaluadoId);
}
