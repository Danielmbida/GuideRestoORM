package ch.hearc.ig.guideresto.service;

import ch.hearc.ig.guideresto.persistence.jpa.JpaUtils;
import jakarta.persistence.EntityManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;

/**
 * Classe de base abstraite pour tous les services métier.
 * <p>
 * Fournit un accès à l'EntityManager JPA pour les opérations de persistance.
 * Les classes filles héritent de cet EntityManager pour interagir avec la base de données.
 */
public class AbstractService {
    protected static final Logger logger = LogManager.getLogger();
    protected EntityManager entityManager;

    /**
     * Constructeur : initialise l'EntityManager via JpaUtils.
     */
    public AbstractService() {
        try {
            entityManager = JpaUtils.getEntityManager();
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération de l'EntityManager : " + e.getMessage());
        }
    }

    /**
     * Ferme proprement l'EntityManager JPA.
     * À appeler depuis la couche de présentation via un shutdown hook.
     */
    public void close() {
        try {
            if (this.entityManager != null && this.entityManager.isOpen()) {
                this.entityManager.close();
                logger.info("EntityManager fermé proprement.");
            }
        } catch (Exception e) {
            logger.warn("Erreur lors de la fermeture de l'EntityManager: " + e.getMessage());
        }
    }

    /**
     * Exécute une action dans une transaction en déléguant à JpaUtils.inTransaction.
     * Utile si on veut que la logique de transaction soit centralisée dans JpaUtils
     * plutôt que dans le service.
     *
     * @param consumer action transactionnelle recevant un EntityManager
     */
    protected void inJpaUtilsTransaction(Consumer<EntityManager> consumer) {
        JpaUtils.inTransaction(consumer);
    }
}
