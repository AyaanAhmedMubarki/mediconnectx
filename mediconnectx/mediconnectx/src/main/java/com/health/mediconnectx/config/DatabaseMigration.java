package com.health.mediconnectx.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Idempotent schema patches that run AFTER Hibernate has applied ddl-auto=update.
 *
 * Hibernate's ddl-auto=update adds new columns but never alters existing
 * column types or constraints.  Each migration below is wrapped in its own
 * try/catch so a failure in one patch never blocks the others.
 *
 * Patches (all idempotent — safe to run on every startup):
 *
 *  1. doctor_shifts.day_of_week  — make nullable (legacy NOT NULL → NULL).
 *     Needed because the new date-based shift model never populates this column.
 *
 *  2. appointments.status        — widen from MySQL ENUM to VARCHAR(30).
 *     The original ENUM only contained ('PENDING_PAYMENT','BOOKED','COMPLETED',
 *     'CANCELLED').  Adding MISSED (or any future status) to a MySQL ENUM
 *     requires an explicit ALTER — so we convert once to VARCHAR, which is what
 *     Hibernate's @Enumerated(EnumType.STRING) expects anyway.  All existing
 *     ENUM values are preserved verbatim during the conversion.
 */
@Component
public class DatabaseMigration implements ApplicationRunner {

    private final DataSource dataSource;

    public DatabaseMigration(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        runPatch(
            "day_of_week nullable",
            "ALTER TABLE doctor_shifts MODIFY COLUMN day_of_week VARCHAR(20) NULL"
        );
        runPatch(
            "appointments.status → VARCHAR(30)",
            "ALTER TABLE appointments MODIFY COLUMN status VARCHAR(30) NOT NULL"
        );
    }

    /** Executes a single DDL statement and logs success or skip. */
    private void runPatch(String name, String sql) {
        try (Connection conn = dataSource.getConnection();
             Statement  stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("[DatabaseMigration] ✓ " + name + " — OK");
        } catch (Exception e) {
            // Column already in target state, or does not exist — safe to ignore
            System.out.println("[DatabaseMigration] Skipped '" + name + "': " + e.getMessage());
        }
    }
}
