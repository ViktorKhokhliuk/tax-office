package epam.project.app.logic.repository;

import epam.project.app.logic.entity.dto.ClientRegistrationDto;
import epam.project.app.logic.entity.user.Client;
import epam.project.app.logic.exception.ClientException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@Log4j2
@RequiredArgsConstructor
public class ClientRepository {

    private final DataSource dataSource;
    private static final String INSERT_CLIENT = "INSERT INTO client (id,name,surname,itn) VALUES (?,?,?,?);";
    private static final String INSERT_USER = "INSERT INTO user (login,password,role) VALUES (?,?,?);";
    private static final String SELECT_CLIENT_BY_ID = "select * from user join client on user.id=client.id where user.id= ?;";
    private static final String SELECT_ALL_CLIENTS = "select * from client join user on user.id=client.id limit ?, 5;";
    private static final String DELETE_REPORTS_BY_CLIENT_ID = "delete from report where clientId = ?;";
    private static final String DELETE_CLIENT_BY_ID = "delete from client where id = ?;";
    private static final String DELETE_USER_BY_ID = "delete from user where id = ?;";
    private static final String SELECT_COUNT = "select count(*) from client join user on user.id=client.id;";

    @SneakyThrows
    public Optional<Client> getClientById(Long id) {
        Client client = new Client();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_CLIENT_BY_ID)) {
            preparedStatement.setLong(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                client.setId(id);
                client.setName(resultSet.getString("name"));
                client.setSurname(resultSet.getString("surname"));
                client.setItn(resultSet.getString("itn"));
                client.setLogin(resultSet.getString("login"));
                return Optional.of(client);
            }
        }
        return Optional.empty();
    }

    @SneakyThrows
    public List<Client> getAllClients(int index) {
        List<Client> clients = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_CLIENTS)) {
            preparedStatement.setInt(1, index);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    long id = resultSet.getLong("id");
                    String login = resultSet.getString("login");
                    String name = resultSet.getString("name");
                    String surname = resultSet.getString("surname");
                    String itn = resultSet.getString("itn");
                    Client client = new Client(name, surname, itn);
                    client.setId(id);
                    client.setLogin(login);
                    clients.add(client);
                }
            }
        }
        return clients;
    }

    @SneakyThrows
    public double getCountOfPage() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(SELECT_COUNT)) {
            if (resultSet.next())
                return resultSet.getDouble(1);
        }
        return 0;
    }

    @SneakyThrows
    public List<Client> getClientsByParameter(Map<String, String> parameters) {
        Iterator<Map.Entry<String, String>> iterator = parameters.entrySet().iterator();
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select * from client join user on user.id=client.id where ");
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            stringBuffer.append(entry.getKey()).append(" = ").append("'").append(entry.getValue()).append("'");
            if (iterator.hasNext()) {
                stringBuffer.append(" and ");
            } else {
                stringBuffer.append(";");
            }
        }
        String sql = stringBuffer.toString();
        List<Client> clients = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                long id = resultSet.getLong("id");
                String login = resultSet.getString("login");
                String name = resultSet.getString("name");
                String surname = resultSet.getString("surname");
                String itn = resultSet.getString("itn");
                Client client = new Client(name, surname, itn);
                client.setId(id);
                client.setLogin(login);
                clients.add(client);
            }
        }
        return clients;
    }


    public Optional<Client> insertClient(ClientRegistrationDto dto) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Client client = new Client();
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(INSERT_USER, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, dto.getLogin());
            preparedStatement.setString(2, dto.getPassword());
            preparedStatement.setString(3, dto.getUserRole().toString());
            preparedStatement.execute();
            resultSet = preparedStatement.getGeneratedKeys();
            if (resultSet.next()) {
                long id = resultSet.getLong(1);
                try (PreparedStatement preparedStatement1 = connection.prepareStatement(INSERT_CLIENT)) {
                    preparedStatement1.setLong(1, id);
                    preparedStatement1.setString(2, dto.getName());
                    preparedStatement1.setString(3, dto.getSurname());
                    preparedStatement1.setString(4, dto.getItn());
                    preparedStatement1.execute();
                    connection.commit();
                }
                client.setId(id);
                client.setLogin(dto.getLogin());
                client.setPassword(dto.getPassword());
                client.setName(dto.getName());
                client.setSurname(dto.getSurname());
                client.setItn(dto.getItn());
                return Optional.of(client);
            }
        } catch (SQLException e) {
            rollback(connection);
            log.error(e.getMessage());
            throw new ClientException("transaction failed");
        } finally {
            close(resultSet);
            close(preparedStatement);
            close(connection);
        }
        return Optional.empty();
    }


    public boolean deleteClientById(Long id) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(DELETE_REPORTS_BY_CLIENT_ID);
            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();
            try (PreparedStatement preparedStatement1 = connection.prepareStatement(DELETE_CLIENT_BY_ID)) {
                preparedStatement1.setLong(1, id);
                if (preparedStatement1.executeUpdate() > 0) {
                    try (PreparedStatement preparedStatement2 = connection.prepareStatement(DELETE_USER_BY_ID)) {
                        preparedStatement2.setLong(1, id);
                        if (preparedStatement2.executeUpdate() > 0) {
                            connection.commit();
                            return true;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            rollback(connection);
            log.error(e.getMessage());
            throw new ClientException("transaction failed");
        } finally {
            close(preparedStatement);
            close(connection);
        }
        return false;
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    @SneakyThrows
    private void close(AutoCloseable autoCloseable) {
        if (autoCloseable != null) {
            autoCloseable.close();

        }
    }

    @SneakyThrows
    private void rollback(Connection connection) {
        if (connection != null) {
            connection.rollback();
        }
    }
}
