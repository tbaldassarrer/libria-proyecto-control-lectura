package es.prw.controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import es.prw.dao.MySqlConnection;

@Controller
public class SearchBookController {

    @GetMapping("/searchBooks")
    @ResponseBody
    public List<String> searchBooks(@RequestParam("query") String query) {
        List<String> books = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            return books; // Devuelve lista vacía si no hay consulta
        }

        try (MySqlConnection db = new MySqlConnection()) {
            db.open();
            Connection connection = db.connection;
            String sql = "SELECT titulo FROM libros WHERE titulo LIKE ? ORDER BY titulo ASC";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, query + "%"); // Filtra solo libros que empiezan con la letra ingresada
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    books.add(rs.getString("titulo"));
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error al buscar libros: " + e.getMessage());
        }

        return books;
    }

    @GetMapping("/getBookDetails")
    @ResponseBody
    public Map<String, String> getBookDetails(@RequestParam("title") String title) {
        Map<String, String> bookDetails = new HashMap<>();

        try (MySqlConnection db = new MySqlConnection()) {
            db.open();
            Connection connection = db.connection;
            String sql = "SELECT titulo, autor, genero, anioEdicion, sinopsis, puntuacion, cover_image FROM libros WHERE titulo = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, title);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    bookDetails.put("titulo", rs.getString("titulo"));
                    bookDetails.put("autor", rs.getString("autor"));
                    bookDetails.put("genero", rs.getString("genero"));
                    bookDetails.put("anioEdicion", rs.getString("anioEdicion"));
                    bookDetails.put("sinopsis", rs.getString("sinopsis"));
                    bookDetails.put("puntuacion", rs.getString("puntuacion"));
                    bookDetails.put("cover_image", rs.getString("cover_image")); // URL de la imagen
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error al obtener detalles del libro: " + e.getMessage());
        }

        return bookDetails;
    }
}
