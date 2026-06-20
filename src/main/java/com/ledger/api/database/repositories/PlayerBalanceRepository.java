package com.ledger.api.database.repositories;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import com.j256.ormlite.dao.Dao;
import com.ledger.Ledger;
import com.ledger.api.database.LedgerDB;
import com.ledger.api.database.entities.HistoryType;
import com.ledger.api.database.entities.PlayerBalance;

public class PlayerBalanceRepository {
    private final Dao<PlayerBalance, String> dao;
    private static final long DAY = 24 * 60 * 60 * 1000;

    public PlayerBalanceRepository(LedgerDB db) throws SQLException {
        dao = db.getDao(PlayerBalance.class);
    }

    public void create(List<PlayerBalance> histories) {
        try {
            dao.create(histories);
        } catch (SQLException e) {
            Ledger.getCustomLogger().error("Failed to create player balances.", e);
        }
    }

    public void createOrUpdate(PlayerBalance pb) {
        try {
            dao.createOrUpdate(pb);
        } catch (SQLException e) {
            Ledger.getCustomLogger().error("Failed to create or update player balance.", e);
        }
    }

    public List<PlayerBalance> query(String playerId, HistoryType historyType) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        switch (historyType) {
            case DAILY -> cal.add(Calendar.DAY_OF_YEAR, -1);
            case WEEKLY -> cal.add(Calendar.DAY_OF_YEAR, -7);
            case MONTHLY -> cal.add(Calendar.MONTH, -1);
            default -> cal.add(Calendar.YEAR, -200);
        }

        long timestamp = cal.getTimeInMillis();

        try {
            return dao.queryBuilder().where().eq("historyType", historyType).and()
                    .ge("timestamp", timestamp).and()
                    .eq("playerId", playerId).query();
        } catch (SQLException e) {
            Ledger.getCustomLogger().error("Failed to query player balances.", e);
            return new ArrayList<>();
        }
    }

    public void purge() {
        purgeDailyEntries();
        purgeWeeklyEntries();
        purgeMonthlyEntries();
    }

    private void purgeDailyEntries() {
        try {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            cal.add(Calendar.DAY_OF_YEAR, -1);
            long timestamp = cal.getTimeInMillis();

            List<PlayerBalance> balances = dao.queryBuilder().where().eq("historyType", HistoryType.DAILY)
                    .and().lt("timestamp", timestamp).query();

            dao.delete(balances);
        } catch (SQLException e) {
            Ledger.getCustomLogger().error("Failed to purge daily player balances.", e);
        }
    }

    private void purgeWeeklyEntries() {
        try {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            cal.add(Calendar.DAY_OF_YEAR, -7);
            long timestamp = cal.getTimeInMillis();

            List<PlayerBalance> balances = dao.queryBuilder().where().eq("historyType", HistoryType.WEEKLY)
                    .and().lt("timestamp", timestamp).query();

            dao.delete(balances);
        } catch (SQLException e) {
            Ledger.getCustomLogger().error("Failed to purge weekly player balances.", e);
        }
    }

    private void purgeMonthlyEntries() {
        try {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            cal.add(Calendar.MONTH, -1);
            long timestamp = cal.getTimeInMillis();

            List<PlayerBalance> balances = dao.queryBuilder().where().eq("historyType", HistoryType.MONTHLY)
                    .and().lt("timestamp", timestamp).query();

            dao.delete(balances);
        } catch (SQLException e) {
            Ledger.getCustomLogger().error("Failed to purge monthly player balances.", e);
        }
    }
}
