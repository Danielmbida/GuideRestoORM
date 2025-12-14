package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.City;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.TypedQuery;

import java.util.HashSet;
import java.util.Set;

/**
 * Mapper JPA pour `City`. Utilise les NamedQueries définies dans l'entité `City`.
 * Le mapper n'assure pas la gestion des transactions : c'est au service de le faire.
 */
public class CityMapper extends AbstractMapper<City> {

    private final EntityManager em;

    /**
     * Constructeur du mapper.
     *
     * @param em EntityManager utilisé pour toutes les opérations JPA (ne gère pas la transaction)
     */
    public CityMapper(EntityManager em) {
        this.em = em;
    }

    /**
     * Recherche une ville par son identifiant métier.
     *
     * Utilise em.find ce qui renvoie l'entité gérée si elle existe, ou null sinon.
     *
     * @param id identifiant métier de la ville
     * @return la City trouvée ou null si introuvable
     */
    @Override
    public City findById(int id) {
        return em.find(City.class, id);
    }

    /**
     * Recherche une ville par son nom.
     *
     * Utilise la NamedQuery "City.findByName" définie dans l'entité `City`.
     * Si aucune ville ne correspond, la méthode retourne null.
     *
     * @param name nom de la ville à rechercher
     * @return la City correspondante ou null si introuvable
     */
    public City findByName(String name) {
        try {
            TypedQuery<City> q = em.createNamedQuery("City.findByName", City.class);
            q.setParameter("cityName", name);
            return q.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    /**
     * Recherche une ville par son code postal (zip code).
     *
     * Utilise la NamedQuery "City.findByZipCode" définie dans l'entité `City`.
     * Retourne null si aucune correspondance.
     *
     * @param zipCode code postal de la ville
     * @return la City correspondante ou null si introuvable
     */
    public City findByZipCode(String zipCode) {
        try {
            TypedQuery<City> q = em.createNamedQuery("City.findByZipCode", City.class);
            q.setParameter("zipCode", zipCode);
            return q.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    /**
     * Récupère toutes les villes présentes en base.
     *
     * Utilise la NamedQuery "City.findAll".
     *
     * @return un Set contenant toutes les City (vide si aucune trouvée)
     */
    @Override
    public Set<City> findAll() {
        TypedQuery<City> q = em.createNamedQuery("City.findAll", City.class);
        return new HashSet<>(q.getResultList());
    }

    /**
     * Persiste une nouvelle ville en base.
     *
     * Remarque : la gestion de la transaction (begin/commit/rollback) doit être effectuée par le service appelant.
     *
     * @param city instance à persister
     * @return la même instance persistée
     */
    @Override
    public City create(City city) {
        em.persist(city);
        return city;
    }

    /**
     * Met à jour une ville existante (merge).
     *
     * Peut lever une OptimisticLockException si la version en base a changé entre-temps.
     *
     * @param city instance à mettre à jour
     * @return l'entité résultante du merge
     * @throws OptimisticLockException en cas de conflit de version
     */
    @Override
    public City update(City city) {
        try {
            return em.merge(city);
        } catch (OptimisticLockException e) {
            logger.error("Conflit de version détecté lors de la mise à jour de la ville ID={}: {}",
                city.getId(), e.getMessage());
            throw e;
        }
    }

    /**
     * Supprime une ville de la base.
     *
     * Cherche d'abord l'entité gérée via em.find puis appelle em.remove si elle existe.
     *
     * @param city instance à supprimer (doit contenir un id)
     * @return true si l'entité a été trouvée et supprimée, false si elle n'existait pas
     * @throws OptimisticLockException en cas de conflit de version lors de la suppression
     */
    @Override
    public boolean delete(City city) {
        try {
            City managed = em.find(City.class, city.getId());
            if (managed != null) {
                em.remove(managed);
                return true;
            }
            return false;
        } catch (OptimisticLockException e) {
            logger.error("Conflit de version détecté lors de la suppression de la ville ID={}: {}",
                city.getId(), e.getMessage());
            throw e;
        }
    }

}
