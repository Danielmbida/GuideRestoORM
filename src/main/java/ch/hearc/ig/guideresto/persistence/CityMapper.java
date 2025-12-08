package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.City;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.HashSet;
import java.util.Set;

/**
 * Mapper JPA pour `City`. Utilise les NamedQueries définies dans l'entité `City`.
 * Le mapper n'assure pas la gestion des transactions : c'est au service de le faire.
 */
public class CityMapper {

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

    public Set<City> findAll() {
        TypedQuery<City> q = em.createNamedQuery("City.findAll", City.class);
        return new HashSet<>(q.getResultList());
    }

    public City create(City city) {
        em.persist(city);
        return city;
    }

    public City update(City city) {
        return em.merge(city);
    }

    public boolean delete(City city) {
        City managed = em.find(City.class, city.getId());
        if (managed != null) {
            em.remove(managed);
            return true;
        }
        return false;
    }

}
