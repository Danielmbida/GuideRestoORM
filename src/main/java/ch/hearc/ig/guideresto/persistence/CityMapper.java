package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.City;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Mapper responsable de la persistance des objets {@link City}.
 * <p>
 * Implémente les opérations CRUD sur la table VILLES et gère le cache d'identité.
 */
public class CityMapper extends AbstractMapper<City> {

    private Connection connection; // Connexion JDBC active

    /**
     * Constructeur du mapper.
     *
     * @param connection Connexion JDBC utilisée pour exécuter les requêtes SQL.
     */
    public CityMapper(Connection connection) {
        this.connection = connection;
    }

    /**
     * Recherche une ville dans la base de données par son identifiant.
     *
     * @param id Identifiant unique (NUMERO) de la ville.
     * @return L’objet {@link City} correspondant, ou null s’il n’existe pas.
     */
    @Override
    public City findById(int id) {
        // Vérifie si la ville est déjà présente dans le cache d'identité
        if (cache.containsKey(id)) {
            return cache.get(id);
        }
        String query = "SELECT * FROM VILLES WHERE numero = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Construction de l'objet métier City à partir du ResultSet
                City city = new City(
                        rs.getInt("numero"),
                        rs.getString("code_postal"),
                        rs.getString("nom_ville")
                );
                addToCache(city); // Mise en cache pour préserver l'identité d'objet
                return city;
            }
        } catch (Exception e) {
            logger.error("Erreur dans CityMapper.findById", e);
        }
        return null;
    }

    /**
     * Recherche une ville dans la base de données par son nom.
     *
     * @param name Nom de la ville à rechercher.
     * @return L’objet {@link City} correspondant, ou null s’il n’existe pas.
     */
    public City findByName(String name) {
        // Vérifie si une ville portant ce nom est déjà dans le cache
        for (City cachedCity : cache.values()) {
            if (cachedCity.getCityName().equalsIgnoreCase(name)) {
                return cachedCity;
            }
        }

        String query = "SELECT * FROM VILLES WHERE upper(NOM_VILLE) LIKE ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "%" + name.toUpperCase() + "%");
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Construction de l'objet métier City à partir du ResultSet
                City city = new City(
                        rs.getInt("numero"),
                        rs.getString("code_postal"),
                        rs.getString("nom_ville")
                );
                addToCache(city); // Mise en cache pour préserver l'identité d'objet
                return city;
            }
        } catch (Exception e) {
            logger.error("Erreur dans CityMapper.findByName", e);
        }
        return null;
    }

    /**
     * Recherche une ville dans la base de données par son code postal.
     *
     * @param zipCode Code postal de la ville à rechercher.
     * @return L’objet {@link City} correspondant, ou null s’il n’existe pas.
     */
    public City findByZipCode(String zipCode) {
        // Vérifie si une ville avec ce code postal est déjà dans le cache
        for (City cachedCity : cache.values()) {
            if (cachedCity.getZipCode().equalsIgnoreCase(zipCode)) {
                return cachedCity;
            }
        }

        String query = "SELECT * FROM VILLES WHERE CODE_POSTAL = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, zipCode);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Construction de l'objet métier City à partir du ResultSet
                City city = new City(
                        rs.getInt("numero"),
                        rs.getString("code_postal"),
                        rs.getString("nom_ville")
                );
                addToCache(city); // Mise en cache pour préserver l'identité d'objet
                return city;
            }
        } catch (Exception e) {
            logger.error("Erreur dans CityMapper.findByZipCode", e);
        }
        return null;
    }


    /**
     * Récupère toutes les villes enregistrées dans la base.
     *
     * @return Un ensemble contenant toutes les instances de {@link City}.
     */
    @Override
    public Set<City> findAll() {
        String query = "SELECT * FROM VILLES";
        Set<City> cities = new HashSet<>();
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("numero");
                City city = cache.get(id);

                if (city == null) {
                    // Création d’un nouvel objet City si non présent dans le cache
                    city = new City(
                            id,
                            rs.getString("code_postal"),
                            rs.getString("nom_ville")
                    );
                    addToCache(city);
                }
                cities.add(city);
            }
            return cities;

        } catch (Exception e) {
            logger.error("Erreur dans CityMapper.findAll", e);
            return new HashSet<>();
        }
    }

    /**
     * Insère une nouvelle ville dans la base de données.
     *
     * @param object L’objet {@link City} à insérer.
     * @return La ville créée (relue depuis la base), ou null en cas d’erreur.
     */
    @Override
    public City create(City object) {
        try {
            // Insertion d’une nouvelle ville (code postal + nom)
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO VILLES(code_postal, nom_ville) VALUES(?, ?)"
            );
            preparedStatement.setString(1, object.getZipCode());
            preparedStatement.setString(2, object.getCityName());
            preparedStatement.executeUpdate();

            connection.commit(); // Validation explicite de la transaction

            // Relecture de la ville insérée à partir de son code postal
            PreparedStatement preparedStatement2 = connection.prepareStatement(
                    "SELECT * FROM VILLES WHERE code_postal = ?"
            );
            preparedStatement2.setString(1, object.getZipCode());
            ResultSet resultSet = preparedStatement2.executeQuery();

            if (resultSet.next()) {
                return new City(
                        resultSet.getInt("numero"),
                        resultSet.getString("code_postal"),
                        resultSet.getString("nom_ville")
                );
            }

            resetCache(); // Nettoyage du cache si la relecture échoue
            return null;

        } catch (Exception e) {
            logger.error("Error en CityMapper.create", e);
            return null;
        }
    }

    /**
     * Met à jour une ville existante.
     *
     * @param object L’objet {@link City} à mettre à jour.
     * @return true si la mise à jour a réussi, false sinon.
     */
    @Override
    public boolean update(City object) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "UPDATE VILLES SET nom_ville = ?, code_postal = ? WHERE numero = ?"
            );
            preparedStatement.setString(1, object.getCityName());
            preparedStatement.setString(2, object.getZipCode());
            preparedStatement.setInt(3, object.getId());
            preparedStatement.executeUpdate();

            removeFromCache(object.getId()); // Invalidation du cache pour forcer une relecture propre
            return true;

        } catch (Exception e) {
            logger.error("Error en CityMapper.update", e);
            return false;
        }
    }

    /**
     * Supprime une ville de la base de données.
     *
     * @param object L’objet {@link City} à supprimer.
     * @return true si la suppression a réussi, false sinon.
     */
    @Override
    public boolean delete(City object) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "DELETE FROM VILLES WHERE numero = ?"
            );
            preparedStatement.setInt(1, object.getId());
            preparedStatement.executeUpdate();

            removeFromCache(object.getId()); // Suppression du cache associée
            return true;

        } catch (Exception e) {
            logger.error("Error en CityMapper.delete", e);
            return false;
        }
    }

    /**
     * Supprime une ville par son identifiant.
     *
     * @param id Identifiant de la ville à supprimer.
     * @return true si la suppression a réussi, false sinon.
     */
    @Override
    public boolean deleteById(int id) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "DELETE FROM VILLES WHERE numero = ?"
            );
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();

            removeFromCache(id); // Nettoyage du cache
            return true;

        } catch (Exception e) {
            logger.error("Error en CityMapper.deleteById", e);
            return false;
        }
    }

    @Override
    protected String getSequenceQuery() {
        return "";
    }

    @Override
    protected String getExistsQuery() {
        return "";
    }

    @Override
    protected String getCountQuery() {
        return "";
    }
}
