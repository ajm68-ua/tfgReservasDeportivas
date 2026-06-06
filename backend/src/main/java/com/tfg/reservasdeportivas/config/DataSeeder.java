package com.tfg.reservasdeportivas.config;

import com.tfg.reservasdeportivas.model.CentroDeportivo;
import com.tfg.reservasdeportivas.model.Pista;
import com.tfg.reservasdeportivas.model.Usuario;
import com.tfg.reservasdeportivas.model.enums.Deporte;
import com.tfg.reservasdeportivas.model.enums.Nivel;
import com.tfg.reservasdeportivas.model.enums.RolUsuario;
import com.tfg.reservasdeportivas.repository.CentroDeportivoRepository;
import com.tfg.reservasdeportivas.repository.PistaRepository;
import com.tfg.reservasdeportivas.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner initData(
            CentroDeportivoRepository centroRepo,
            PistaRepository pistaRepo,
            UsuarioRepository usuarioRepo) {
        return args -> {
            if (centroRepo.count() > 0) {
                System.out.println("La base de datos ya contiene datos.");
                return;
            }

            // CENTROS DEPORTIVOS
            CentroDeportivo centro1 = new CentroDeportivo();
            centro1.setNombre("CENTRO DEPORTIVO 1");
            centro1.setCiudad("Elche");
            centro1.setDireccion("Calle ejemplo 1");
            centro1.setTelefono("123456789");
            centro1.setHorarioApertura(LocalTime.of(8, 0));
            centro1.setHorarioCierre(LocalTime.of(22, 0));
            centroRepo.save(centro1);

            CentroDeportivo centro2 = new CentroDeportivo();
            centro2.setNombre("CENTRO DEPORTIVO 2");
            centro2.setCiudad("Alicante");
            centro2.setDireccion("Calle ejemplo 2");
            centro2.setTelefono("987654321");
            centro2.setHorarioApertura(LocalTime.of(9, 0));
            centro2.setHorarioCierre(LocalTime.of(23, 0));
            centroRepo.save(centro2);

            // PISTAS
            Pista pista1 = new Pista();
            pista1.setCentro(centro1);
            pista1.setNombre("Pista Fútbol");
            pista1.setDeporte(Deporte.FUTBOL);
            pista1.setPrecioPorHora(new BigDecimal("30.00"));
            pista1.setCapacidadMaxima(22);
            pista1.setDisponible(true);
            pistaRepo.save(pista1);

            Pista pista2 = new Pista();
            pista2.setCentro(centro1);
            pista2.setNombre("Pista Baloncesto");
            pista2.setDeporte(Deporte.BALONCESTO);
            pista2.setPrecioPorHora(new BigDecimal("20.00"));
            pista2.setCapacidadMaxima(12);
            pista2.setDisponible(true);
            pistaRepo.save(pista2);

            Pista pista3 = new Pista();
            pista3.setCentro(centro2);
            pista3.setNombre("Pista Pádel");
            pista3.setDeporte(Deporte.PADEL);
            pista3.setPrecioPorHora(new BigDecimal("15.00"));
            pista3.setCapacidadMaxima(4);
            pista3.setDisponible(true);
            pistaRepo.save(pista3);

            Pista pista4 = new Pista();
            pista4.setCentro(centro2);
            pista4.setNombre("Pista Tenis");
            pista4.setDeporte(Deporte.TENIS);
            pista4.setPrecioPorHora(new BigDecimal("10.00"));
            pista4.setCapacidadMaxima(4);
            pista4.setDisponible(true);
            pistaRepo.save(pista4);

            // USUARIOS
            Usuario admin1 = new Usuario();
            admin1.setNombre("Admin");
            admin1.setApellidos("Elche");
            admin1.setEmail("admin1@ejemplo.com");
            admin1.setPassword("1234"); 
            admin1.setRol(RolUsuario.ADMINISTRADOR_CENTRO);
            admin1.setCentro(centro1);
            usuarioRepo.save(admin1);

            Usuario admin2 = new Usuario();
            admin2.setNombre("Admin");
            admin2.setApellidos("Alicante");
            admin2.setEmail("admin2@ejemplo.com");
            admin2.setPassword("1234");
            admin2.setRol(RolUsuario.ADMINISTRADOR_CENTRO);
            admin2.setCentro(centro2);
            usuarioRepo.save(admin2);

            Usuario deportista1 = new Usuario();
            deportista1.setNombre("Alejandro");
            deportista1.setApellidos("Jiménez");
            deportista1.setEmail("alejandro@ejemplo.com");
            deportista1.setPassword("1234");
            deportista1.setRol(RolUsuario.DEPORTISTA);
            deportista1.setNivel(Nivel.INTERMEDIO);
            deportista1.setCiudad("Elche");
            usuarioRepo.save(deportista1);

            Usuario deportista2 = new Usuario();
            deportista2.setNombre("Miguel");
            deportista2.setApellidos("Martínez");
            deportista2.setEmail("miguel@ejemplo.com");
            deportista2.setPassword("1234");
            deportista2.setRol(RolUsuario.DEPORTISTA);
            deportista2.setNivel(Nivel.AVANZADO);
            deportista2.setCiudad("Alicante");
            usuarioRepo.save(deportista2);

            System.out.println("DATOS DE PRUEBA INTRODUCIDOS");
        };
    }
}
