package org.example.repository;

import org.example.SingletonConnection;
import org.example.dao.TopicRepository;
import org.example.exaption.*;
import org.example.model.Topic;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class TopicRepositoryImpl implements TopicRepository {
    private final Connection connection;
    private static final String SAVE_QUERY = "INSERT INTO topic (name, description, created_at) VALUES (?, ?, ?)";
    private static final String GET_QUERY = "SELECT * FROM topic WHERE id = ?";
    private static final String REMOVE_QUERY = "DELETE FROM topic WHERE id = ?";
    private static final String UPDATE_QUERY = "UPDATE topic SET name = ?, description = ? WHERE id = ?";
    private static final String GET_ALL_QUERY = "SELECT * FROM topic";

    public TopicRepositoryImpl() {
        connection = SingletonConnection.getInstance().getConnection();
    }

    @Override
    public Topic save(Topic topic) throws ObjectNotSavedException {
        try (PreparedStatement statement = connection.prepareStatement(SAVE_QUERY, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, topic.getName());
            statement.setString(2, topic.getDescription());
            statement.setTimestamp(3, topic.getCreatedAt());
            statement.execute();
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                topic.setId(generatedKeys.getInt(1));
                return topic;
            }
        } catch (SQLException e) {
            System.out.println("Error occurred while saving question: " + e.getMessage());
            throw new ObjectNotSavedException(e.getMessage());
        }
        throw new ObjectNotSavedException("Failed to save topic.");
    }



    @Override
    public Topic get(int id) throws NoSuchSQLIdException {
        try (PreparedStatement statement = connection.prepareStatement(GET_QUERY)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String name = resultSet.getString("name");
                    String description = resultSet.getString("description");
                    Timestamp createdAt = resultSet.getTimestamp("created_at");
                    return new Topic(id, name, description, createdAt);
                }
                return null;
            }
        } catch (SQLException e) {
            System.out.println("Error occurred while retrieving topic: " + e.getMessage());
            throw new NoSuchSQLIdException(e.getMessage());
        }
    }

    @Override
    public boolean remove(int id) throws RecordsNotDeletedException {
        try (PreparedStatement statement = connection.prepareStatement(REMOVE_QUERY)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RecordsNotDeletedException(e.getMessage());
        }
    }

    @Override
    public boolean update(Topic topic) throws RecordNotUpdatedException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_QUERY)) {
            statement.setString(1, topic.getName());
            statement.setString(2, topic.getDescription());
            statement.setInt(3, topic.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RecordNotUpdatedException(e.getMessage());
        }
    }
    @Override
    public List<Topic> getAll() throws TopicsNotRetrievedException {
        List<Topic> topics = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(GET_ALL_QUERY)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String name = resultSet.getString("name");
                    String description = resultSet.getString("description");
                    Timestamp createdAt = resultSet.getTimestamp("created_at");
                    topics.add(new Topic(id, name, description, createdAt));
                }
            }
        } catch (SQLException e) {
            throw new TopicsNotRetrievedException(e.getMessage());
        }
        return topics;
    }
}