package com.example.repo.jdbc;

import com.example.model.MoonMission;
import com.example.repo.MoonMissionRepository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcMoonMissionRepository implements MoonMissionRepository {

    private final DataSource dataSource;

    public JdbcMoonMissionRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<String> listSpacecraft() {
        String query = "select spacecraft from moon_mission";
        List<String> listResult = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet result = preparedStatement.executeQuery()) {

            while (result.next()) {
                listResult.add(result.getString("spacecraft"));
            }

            return listResult;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<MoonMission> findById(int id) {
        String query = "select * from moon_mission where mission_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, id);
            try (ResultSet result = preparedStatement.executeQuery()) {
                if (!result.next()) return Optional.empty();

                MoonMission mission = new MoonMission();
                mission.id = result.getInt("mission_id");
                mission.spacecraft = result.getString("spacecraft");
                mission.launchDate = result.getDate("launch_date").toLocalDate();
                return Optional.of(mission);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<MoonMission> findAll() {
        List<MoonMission> missions = new ArrayList<>();

        String sql = "SELECT mission_id, spacecraft, launch_date FROM moon_mission";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                MoonMission mission = new MoonMission();
                mission.id = resultSet.getInt("mission_id");
                mission.spacecraft = resultSet.getString("spacecraft");
                mission.launchDate = resultSet.getDate("launch_date").toLocalDate();
                missions.add(mission);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return missions;
    }

    @Override
    public int countByYear(int year) {
        String query = "select count(*) from moon_mission where year(launch_date) = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, year);

            try (ResultSet result = preparedStatement.executeQuery()) {
                result.next();
                return result.getInt(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
