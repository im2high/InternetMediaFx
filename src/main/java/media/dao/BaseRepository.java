package media.dao;

import media.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseRepository<T> implements Repository<T> {

    protected Connection getConnection() throws SQLException {
        return DatabaseManager.getConnection();
    }

    protected abstract T mapRow(ResultSet rs) throws SQLException;

    @FunctionalInterface
    protected interface PSSetter {
        void set(PreparedStatement ps) throws SQLException;
    }

    protected List<T> queryList(String sql, PSSetter setter) throws SQLException {
        List<T> list = new ArrayList<>();
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (setter != null) setter.set(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }
}