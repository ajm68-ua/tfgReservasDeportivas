# 🏟️ Reservas Deportivas

Sistema web de reservas de pistas deportivas desarrollado como Trabajo de Fin de Grado (TFG).

Permite a los deportistas reservar pistas, unirse a partidas abiertas, chatear con otros jugadores y valorar a los participantes tras cada partida.

## Stack tecnológico

| Capa | Tecnología |
|---|---|
| Backend | Spring Boot |
| Frontend | Vue |
| Base de datos | H2 |
| Build | Maven |

## Esquema de base de datos

```mermaid
erDiagram
    Usuarios {
        int id PK
        int centro_id FK "null si DEPORTISTA"
        varchar nombre
        varchar apellidos
        varchar foto
        varchar email UK
        varchar password
        varchar telefono
        varchar ciudad
        enum nivel
        enum rol
        datetime fecha_registro
        decimal valoracion_media
        text descripcion
        boolean notificaciones_partidas
        boolean notificaciones_chat
    }

    Centros_Deportivos {
        int id PK
        varchar nombre
        varchar foto
        varchar direccion
        varchar ciudad
        varchar telefono
        time horario_apertura
        time horario_cierre
    }

    Pistas {
        int id PK
        int centro_id FK
        varchar nombre
        enum deporte
        decimal precio_por_hora
        int capacidad_maxima
        boolean disponible
    }

    Reservas {
        int id PK
        int pista_id FK
        int organizador_id FK
        date fecha
        time hora_inicio
        time hora_fin
        decimal precio_total
        enum estado_pago
        enum nivel
        boolean es_abierta
        datetime fecha_creacion
    }

    Participantes_Partida {
        int id PK
        int reserva_id FK
        int usuario_id FK
        datetime fecha_inscripcion
    }

    Mensajes_Chat {
        int id PK
        int reserva_id FK
        int usuario_id FK
        text mensaje
        datetime fecha_envio
    }

    Resenas_Usuarios {
        int id PK
        int reserva_id FK
        int evaluador_id FK
        int evaluado_id FK
        int puntuacion
        varchar comentario
        datetime fecha_creacion
    }

    Notificaciones {
        int id PK
        int usuario_id FK
        varchar titulo
        text mensaje
        boolean leido
        datetime fecha_creacion
    }

    Centros_Deportivos ||--o{ Pistas : "tiene"
    Centros_Deportivos ||--o{ Usuarios : "administrado por"
    Pistas ||--o{ Reservas : "se reserva en"
    Usuarios ||--o{ Reservas : "organiza"
    Reservas ||--o{ Participantes_Partida : "tiene"
    Usuarios ||--o{ Participantes_Partida : "participa en"
    Reservas ||--o{ Mensajes_Chat : "tiene"
    Usuarios ||--o{ Mensajes_Chat : "escribe"
    Reservas ||--o{ Resenas_Usuarios : "genera"
    Usuarios ||--o{ Resenas_Usuarios : "evalúa"
    Usuarios ||--o{ Resenas_Usuarios : "es evaluado"
    Usuarios ||--o{ Notificaciones : "recibe"
```

### Enums

| Enum | Valores |
|---|---|
| `RolUsuario` | `DEPORTISTA` · `ADMINISTRADOR_CENTRO` |
| `Deporte` | `PADEL` · `TENIS` · `FUTBOL` · `BALONCESTO` · `SQUASH` · `BADMINTON` |
| `EstadoPago` | `PENDIENTE` · `PAGADO` · `CANCELADO` |
| `Nivel` | `PRINCIPIANTE` · `INTERMEDIO` · `AVANZADO` · `PROFESIONAL` |
