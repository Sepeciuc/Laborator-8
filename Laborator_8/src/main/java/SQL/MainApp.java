package SQL;

import java.sql.*;
import java.util.Scanner;

public class MainApp {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/lab8";
    private static final String USER = "root";
    private static final String PASSWORD = "student";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int optiune;

        do {
            System.out.println("\nMeniu:");
            System.out.println("1. Adăugare persoană");
            System.out.println("2. Adăugare excursie");
            System.out.println("3. Afișare persoane și excursii");
            System.out.println("4. Afișare excursii pentru o persoană");
            System.out.println("5. Afișare persoane care au vizitat o destinație");
            System.out.println("6. Afișare persoane cu excursii într-un an");
            System.out.println("7. Ștergere excursie");
            System.out.println("8. Ștergere persoană");
            System.out.println("0. Ieșire");
            System.out.print("Alegeți o opțiune: ");
            optiune = scanner.nextInt();

            try {
                switch (optiune) {
                    case 1 -> adaugaPersoana(scanner);
                    case 2 -> adaugaExcursie(scanner);
                    case 3 -> afiseazaPersoaneSiExcursii();
                    case 4 -> afiseazaExcursiiPentruPersoana(scanner);
                    case 5 -> afiseazaPersoaneCuDestinatie(scanner);
                    case 6 -> afiseazaPersoaneCuExcursiiInAn(scanner);
                    case 7 -> stergeExcursie(scanner);
                    case 8 -> stergePersoana(scanner);
                }
            } catch (Exception e) {
                System.out.println("Eroare: " + e.getMessage());
            }
        } while (optiune != 0);

        scanner.close();
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASSWORD);
    }

    private static void adaugaPersoana(Scanner scanner) throws SQLException {
        System.out.print("Introduceti numele: ");
        String nume = scanner.next();
        System.out.print("Introduceti varsta: ");
        int varsta = scanner.nextInt();

        if (varsta < 0 || varsta > 120) {
            System.out.println("Varsta trebuie sa fie intre 0 si 120");
            return;
        }

        String sql = "INSERT INTO persoane (nume, varsta) VALUES (?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nume);
            pstmt.setInt(2, varsta);
            pstmt.executeUpdate();
            System.out.println("Persoana a fost adaugata");
        }
    }

    private static void adaugaExcursie(Scanner scanner) throws SQLException {
        System.out.print("Introduceti ID-ul persoanei: ");
        int idPersoana = scanner.nextInt();
        System.out.print("Introduceti destinația: ");
        String destinatia = scanner.next();
        System.out.print("Introduceti anul excursiei: ");
        int anul = scanner.nextInt();

        if (anul < 1900 || anul > java.time.Year.now().getValue()) {
            System.out.println("Anul nu este valid");
            return;
        }

        String verificaSql = "SELECT * FROM persoane WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement verificaStmt = conn.prepareStatement(verificaSql)) {
            verificaStmt.setInt(1, idPersoana);
            ResultSet rs = verificaStmt.executeQuery();

            if (!rs.next()) {
                System.out.println("Persoana nu exista");
                return;
            }
        }

        String sql = "INSERT INTO excursii (id_persoana, destinatia, anul) VALUES (?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idPersoana);
            pstmt.setString(2, destinatia);
            pstmt.setInt(3, anul);
            pstmt.executeUpdate();
            System.out.println("Excursia a fost adaugata");
        }
    }

    private static void afiseazaPersoaneSiExcursii() throws SQLException {
        String sql = """
                SELECT p.id, p.nume, p.varsta, e.destinatia, e.anul
                FROM persoane p
                LEFT JOIN excursii e ON p.id = e.id_persoana
                """;
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") + ", Nume: " + rs.getString("nume") + ", Varsta: " + rs.getInt("varsta")
                        + ", Destinatie: " + rs.getString("destinatia") + ", An: " + rs.getInt("anul"));
            }
        }
    }

    private static void afiseazaExcursiiPentruPersoana(Scanner scanner) {
        System.out.print("Introduceti numele persoanei: ");
        String nume = scanner.next();

        String sql = """
                SELECT e.destinatia, e.anul
                FROM persoane p
                JOIN excursii e ON p.id = e.id_persoana
                WHERE p.nume = ?
                """;
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nume);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                System.out.println("Destinatie: " + rs.getString("destinatia") + ", An: " + rs.getInt("anul"));
            }
        } catch (SQLException e) {
            System.out.println("Eroare: " + e.getMessage());
        }
    }

    private static void afiseazaPersoaneCuDestinatie(Scanner scanner) {
        System.out.print("Introduceti destinatia: ");
        String destinatia = scanner.next();

        String sql = """
                SELECT DISTINCT p.nume, p.varsta
                FROM persoane p
                JOIN excursii e ON p.id = e.id_persoana
                WHERE e.destinatia = ?
                """;
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, destinatia);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                System.out.println("Nume: " + rs.getString("nume") + ", Varsta: " + rs.getInt("varsta"));
            }
        } catch (SQLException e) {
            System.out.println("Eroare: " + e.getMessage());
        }
    }

    private static void afiseazaPersoaneCuExcursiiInAn(Scanner scanner) {
        System.out.print("Introduceti anul: ");
        int anul = scanner.nextInt();

        String sql = """
                SELECT DISTINCT p.nume, p.varsta
                FROM persoane p
                JOIN excursii e ON p.id = e.id_persoana
                WHERE e.anul = ?
                """;
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, anul);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                System.out.println("Nume: " + rs.getString("nume") + ", Varsta: " + rs.getInt("varsta"));
            }
        } catch (SQLException e) {
            System.out.println("Eroare: " + e.getMessage());
        }
    }

    private static void stergeExcursie(Scanner scanner) {
        System.out.print("Introduceți ID-ul excursiei: ");
        int idExcursie = scanner.nextInt();

        String sql = "DELETE FROM excursii WHERE id_excursie = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idExcursie);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Excursia s-a sters");
            } else {
                System.out.println("Excursia este gasita");
            }
        } catch (SQLException e) {
            System.out.println("Eroare: " + e.getMessage());
        }
    }

    private static void stergePersoana(Scanner scanner) {
        System.out.print("Introduceti ID-ul persoanei: ");
        int idPersoana = scanner.nextInt();

        String stergeExcursiiSql = "DELETE FROM excursii WHERE id_persoana = ?";
        String stergePersoanaSql = "DELETE FROM persoane WHERE id = ?";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmtExcursii = conn.prepareStatement(stergeExcursiiSql);
                 PreparedStatement pstmtPersoana = conn.prepareStatement(stergePersoanaSql)) {

                pstmtExcursii.setInt(1, idPersoana);
                pstmtExcursii.executeUpdate();

                pstmtPersoana.setInt(1, idPersoana);
                int rowsAffected = pstmtPersoana.executeUpdate();

                if (rowsAffected > 0) {
                    conn.commit();
                    System.out.println("Persoana si excursiile au fost sterse");
                } else {
                    System.out.println("Persoana nu a fost gasita");
                    conn.rollback();
                }
            }
        } catch (SQLException e) {
            System.out.println("Eroare: " + e.getMessage());
        }
    }
}

