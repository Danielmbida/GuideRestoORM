package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.Grade;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.TypedQuery;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Mapper JPA pour `Grade` (table NOTES).
 * Ne gère pas les transactions ; c'est au service appelant de les contrôler.
 */
public class GradeMapper extends AbstractMapper<Grade> {
    private final EntityManager em;

    /**
     * Constructeur du mapper.
     *
     * @param em EntityManager utilisé pour les opérations JPA (ne gère pas les transactions)
     */
    public GradeMapper(EntityManager em) {
       this.em = em;
    }

    /**
     * Recherche une note par son identifiant métier.
     *
     * Utilise {@code EntityManager.find}.
     *
     * @param id identifiant métier de la note
     * @return l'entité {@link Grade} si trouvée, sinon {@code null}
     */
    @Override
    public Grade findById(int id) {
        return em.find(Grade.class, id);
    }

    /**
     * Récupère toutes les notes en base.
     *
     * Utilise la NamedQuery {@code Grade.findAll} définie sur l'entité {@link Grade}.
     *
     * @return un {@link Set} contenant toutes les instances de {@link Grade} (vide si aucune)
     */
    @Override
    public Set<Grade> findAll() {
        TypedQuery<Grade> q = em.createNamedQuery("Grade.findAll", Grade.class);
        List<Grade> list = q.getResultList();
        return new HashSet<>(list);
    }


    /**
     * Persiste une nouvelle note en base.
     *
     * Remarque : la gestion transactionnelle doit être effectuée par le code appelant.
     *
     * @param grade instance à persister
     * @return la même instance persistée
     */
    @Override
    public Grade create(Grade grade) {
        em.persist(grade);
        return grade;
    }

    /**
     * Met à jour une note existante (merge).
     *
     * Peut lancer une {@link OptimisticLockException} si un conflit de version est détecté.
     *
     * @param grade instance à mettre à jour
     * @return l'entité résultante du merge
     * @throws OptimisticLockException si conflit de version détecté
     */
    @Override
    public Grade update(Grade grade) {
        try {
            return em.merge(grade);
        } catch (OptimisticLockException e) {
            logger.error("Conflit de version détecté lors de la mise à jour de la note ID={}: {}",
                grade.getId(), e.getMessage());
            throw e;
        }
    }

    /**
     * Supprime une note de la base.
     *
     * Cherche d'abord l'entité gérée via em.find puis appelle em.remove si elle existe.
     *
     * @param grade instance à supprimer (doit contenir un id)
     * @return true si l'entité a été trouvée et supprimée, false si elle n'existait pas
     * @throws OptimisticLockException si conflit de version détecté lors de la suppression
     */
    @Override
    public boolean delete(Grade grade) {
        try {
            Grade managed = em.find(Grade.class, grade.getId());
            if (managed != null) {
                em.remove(managed);
                return true;
            }
            return false;
        } catch (OptimisticLockException e) {
            logger.error("Conflit de version détecté lors de la suppression de la note ID={}: {}",
                grade.getId(), e.getMessage());
            throw e;
        }
    }

}
