package com.rst.helloworld.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class EmployeeScheduler {

    private final Logger logger = LoggerFactory.getLogger(EmployeeScheduler.class);
    private ScheduledExecutorService scheduler;
    private final Random random = new Random();

    private final String[] noms = {"Dupont", "Martin", "Bernard", "Dubois", "Thomas", "Robert", "Richard", "Petit", "Durand", "Leroy"};
    private final String[] prenoms = {"Jean", "Marie", "Pierre", "Sophie", "Michel", "Catherine", "François", "Anne", "Jacques", "Isabelle"};
    private final String[] villes = {"Paris", "Lyon", "Marseille", "Toulouse", "Nice", "Nantes", "Strasbourg", "Montpellier", "Bordeaux", "Lille"};

    @PostConstruct
    public void init() {
        logger.info("Starting Employee Scheduler...");

        // Initialize database table
        initializeDatabase();

        // Schedule periodic insertion every 30 seconds
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::insertEmployee, 10, 30, TimeUnit.SECONDS);

        logger.info("Employee Scheduler started - inserting employees every 30 seconds");
    }

    @PreDestroy
    public void shutdown() {
        if (scheduler != null) {
            scheduler.shutdown();
            logger.info("Employee Scheduler stopped");
        }
    }

    private Connection getConnection() throws Exception {
        String host = System.getenv("DB_HOST") != null ? System.getenv("DB_HOST") : "localhost";
        String port = System.getenv("DB_PORT") != null ? System.getenv("DB_PORT") : "3306";
        String dbName = System.getenv("DB_NAME") != null ? System.getenv("DB_NAME") : "employeedb";
        String user = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "employee_user";
        String password = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "employee_pass";

        String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(url, user, password);
    }

    private void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            String createTable = "CREATE TABLE IF NOT EXISTS Employee (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "nom VARCHAR(100) NOT NULL, " +
                    "prenom VARCHAR(100) NOT NULL, " +
                    "ville VARCHAR(100) NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

            stmt.executeUpdate(createTable);
            logger.info("Employee table initialized successfully");

        } catch (Exception e) {
            logger.warn("Could not initialize database: " + e.getMessage());
        }
    }

    private void insertEmployee() {
        String nom = noms[random.nextInt(noms.length)];
        String prenom = prenoms[random.nextInt(prenoms.length)];
        String ville = villes[random.nextInt(villes.length)];

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO Employee (nom, prenom, ville) VALUES (?, ?, ?)")) {

            pstmt.setString(1, nom);
            pstmt.setString(2, prenom);
            pstmt.setString(3, ville);
            pstmt.executeUpdate();

            logger.info("Inserted employee: {} {} from {}", prenom, nom, ville);

        } catch (Exception e) {
            logger.error("Failed to insert employee: " + e.getMessage());
        }
    }
}
