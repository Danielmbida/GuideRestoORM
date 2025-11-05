package ch.hearc.ig.guideresto.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class AbstractService {
    protected static final Logger logger = LogManager.getLogger();
    protected Connection connection;
    public AbstractService() {
        try{
            // Chargement du fichier de configuration depuis le classpath
            Properties props = new Properties();
            InputStream input = getClass().getClassLoader().getResourceAsStream("database.properties");

            if (input == null) {
                throw new RuntimeException("Fichier database.properties introuvable dans le classpath !");
            }

            props.load(input);

            // Ouverture de la connexion JDBC
            this.connection = DriverManager.getConnection(
                    props.getProperty("database.url"),
                    props.getProperty("database.username"),
                    props.getProperty("database.password")
            );
            this.connection.setAutoCommit(false);

        }catch(Exception e){
            logger.error("Erreur lors de la connection à la bd : " + e.getMessage());
        }

    }

    /**
     * Ferme proprement la connexion JDBC (rend au pool si pool, sinon fermeture physique).
     * À appeler depuis la couche de présentation via un shutdown hook.
     */
    public void close() {
        try {
            if (this.connection != null && !this.connection.isClosed()) {
                this.connection.close();
                logger.info("Connexion JDBC fermée proprement.");
            }
        } catch (Exception e) {
            logger.warn("Erreur lors de la fermeture de la connexion JDBC: " + e.getMessage());
        }
    }
}
