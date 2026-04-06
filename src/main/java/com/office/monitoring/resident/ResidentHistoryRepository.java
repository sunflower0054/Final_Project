package com.office.monitoring.resident;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
@RequiredArgsConstructor
/** 거주자 데이터 조회·등록·수정·삭제 흐름을 담당하는 구성 요소. */
public class ResidentHistoryRepository { //거주자 삭제 전에 관련 이력 데이터가 있는지 검사

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    /** 요청된 거주자 작업에 필요한 입력을 반영해 결과 값을 생성한다. */
    public boolean existsDailyActivityByResidentId(Long residentId) {
        if (!tableExists("daily_activity")) {
            return false;
        }

        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from daily_activity where resident_id = ?",
                Integer.class,
                residentId
        );

        return count != null && count > 0;
    }

    /** 요청된 거주자 작업에 필요한 입력을 반영해 결과 값을 생성한다. */
    private boolean tableExists(String tableName) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();

            return hasTable(metaData, tableName)
                    || hasTable(metaData, tableName.toLowerCase())
                    || hasTable(metaData, tableName.toUpperCase());
        } catch (SQLException exception) {
            throw new IllegalStateException("이력 테이블 조회에 실패했습니다.", exception);
        }
    }

    /** 요청된 거주자 작업에 필요한 입력을 반영해 결과 값을 생성한다. */
    private boolean hasTable(DatabaseMetaData metaData, String tableName) throws SQLException {
        try (ResultSet resultSet = metaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
            return resultSet.next();
        }
    }
}
