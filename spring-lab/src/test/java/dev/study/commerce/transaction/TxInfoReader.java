package dev.study.commerce.transaction;

import org.springframework.jdbc.core.JdbcTemplate;

final class TxInfoReader {
    private TxInfoReader() {
    }

    static TxInfo read(JdbcTemplate jdbc) {
        return jdbc.queryForObject(
                "SELECT pg_backend_pid(), pg_current_xact_id()::text::bigint",
                (row, rowNumber) -> new TxInfo(row.getInt(1), row.getLong(2)));
    }
}
