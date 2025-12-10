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

    public CityMapper(EntityManager em) {
        this.em = em;
    }

    public City findById(int id) {
        try {
            TypedQuery<City> q = em.createNamedQuery("City.findById", City.class);
            q.setParameter("id", id);
            return q.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    public City findByName(String name) {
        try {
            TypedQuery<City> q = em.createNamedQuery("City.findByName", City.class);
            q.setParameter("cityName", name);
            return q.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    public City findByZipCode(String zipCode) {
        try {
            TypedQuery<City> q = em.createNamedQuery("City.findByZipCode", City.class);
            q.setParameter("zipCode", zipCode);
            return q.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    public Set<City> findAll() {
        TypedQuery<City> q = em.createNamedQuery("City.findAll", City.class);
        return new HashSet<>(q.getResultList());
    }

    @Override
    public City create(City city) {
        em.persist(city);
        return city;
    }

    /**
     * @throws OptimisticLockException Si la ville a été modifiée par un autre utilisateur
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
     * @throws OptimisticLockException Si la ville a été modifiée par un autre utilisateur
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
