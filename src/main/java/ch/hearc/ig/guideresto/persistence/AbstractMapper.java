package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.IBusinessObject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Classe abstraite de base pour tous les mappers.
 * Fournit un logger commun et un cache optionnel.
 * Définit le contrat que tous les mappers doivent implémenter.
 */
public abstract class AbstractMapper<T extends IBusinessObject> {

    protected static final Logger logger = LogManager.getLogger();


    protected Map<Integer, T> cache = new HashMap<>();

    /**
     * Recherche une entité par son identifiant.
     * @param id Identifiant unique de l'entité
     * @return L'entité trouvée ou null
     */
    public abstract T findById(int id);

    /**
     * Récupère toutes les entités.
     * @return Un ensemble de toutes les entités
     */
    public abstract Set<T> findAll();

    /**
     * Crée une nouvelle entité.
     * @param object L'entité à créer
     * @return L'entité créée avec son ID généré
     */
    public abstract T create(T object);

    /**
     * Met à jour une entité existante.
     * @param object L'entité à mettre à jour
     * @return L'entité mise à jour
     * @throws OptimisticLockException Si l'entité a été modifiée par un autre utilisateur
     */
    public abstract T update(T object) throws OptimisticLockException;

    /**
     * Supprime une entité.
     * @param object L'entité à supprimer
     * @return true si la suppression a réussi, false sinon
     * @throws OptimisticLockException Si l'entité a été modifiée par un autre utilisateur
     */
    public abstract boolean delete(T object) throws OptimisticLockException;

}
