-- V25: Additional roles held by a user, on top of the primary role stored in users.role.
CREATE TABLE IF NOT EXISTS user_roles (
    user_id CHAR(36)    NOT NULL,
    role    VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
