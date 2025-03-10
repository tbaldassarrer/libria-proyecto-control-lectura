package es.prw.controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import es.prw.dao.MySqlConnection;

@Controller
public class RegisterController {

    @PostMapping("/register")
    public String registerUser(
            @RequestParam("nombreUsuario") String nombreUsuario,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("reppassword") String reppassword,
            Model model) {

        System.out.println("⏳ Intentando registrar usuario...");

        try (MySqlConnection db = new MySqlConnection()) {
            db.open();
            Connection connection = db.connection;
            System.out.println("✅ Conexión a la base de datos establecida.");

            // Verificar si el nombre de usuario ya está registrado
            String checkUserQuery = "SELECT idUsuario FROM usuariolector WHERE nombreUsuario = ?";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkUserQuery)) {
                checkStmt.setString(1, nombreUsuario);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    model.addAttribute("registerError", "⚠️ Usuario ya registrado, elija otro nombre de usuario.");
                    model.addAttribute("nombreUsuario", nombreUsuario);
                    model.addAttribute("email", email);
                    return "login";
                }
            }

            // Verificar si el email ya está registrado
            String checkEmailQuery = "SELECT idUsuario FROM usuariolector WHERE email = ?";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkEmailQuery)) {
                checkStmt.setString(1, email);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    model.addAttribute("registerError", "⚠️ El correo ya está registrado.");
                    model.addAttribute("nombreUsuario", nombreUsuario);
                    model.addAttribute("email", email);
                    return "login";
                }
            }

            // Validación de la contraseña
            String passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&.])[A-Za-z\\d@$!%*?&.]{6,}$";
            if (!password.matches(passwordPattern)) {
                model.addAttribute("registerError", "⚠️ La contraseña debe tener al menos 6 caracteres, una letra, un número y un carácter especial.");
                model.addAttribute("nombreUsuario", nombreUsuario);
                model.addAttribute("email", email);
                return "login";
            }

            // Verificar que las contraseñas coincidan
            if (!password.equals(reppassword)) {
                model.addAttribute("registerError", "❌ Las contraseñas no coinciden.");
                model.addAttribute("nombreUsuario", nombreUsuario);
                model.addAttribute("email", email);
                return "login";
            }

            // Encriptar la contraseña antes de guardarla
            String insertQuery = "INSERT INTO usuariolector (nombreUsuario, email, password) VALUES (?, ?, ?)";
            try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                insertStmt.setString(1, nombreUsuario);
                insertStmt.setString(2, email);
                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                String hashedPassword = encoder.encode(password);
                insertStmt.setString(3, hashedPassword);
                int rowsInserted = insertStmt.executeUpdate();

                if (rowsInserted > 0) {
                    model.addAttribute("registerSuccess", "✅ Registro completado correctamente, ¡Bienvenido a LIBRIA!");
                    System.out.println("✅ Usuario registrado correctamente.");
                } else {
                    model.addAttribute("registerError", "❌ No se pudo registrar el usuario.");
                    model.addAttribute("nombreUsuario", nombreUsuario);
                    model.addAttribute("email", email);
                }
            }

            return "login";

        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                model.addAttribute("registerError", "⚠️ Usuario ya registrado, elija otro nombre de usuario.");
            } else {
                model.addAttribute("registerError", "❌ Error en la base de datos: " + e.getMessage());
            }
            model.addAttribute("nombreUsuario", nombreUsuario);
            model.addAttribute("email", email);
            System.out.println("❌ ERROR SQL: " + e.getMessage());
            return "login";
        }
    }
}
