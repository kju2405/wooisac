package com.lovely4k.backend.calendar.repository;

import com.lovely4k.backend.QueryTestSupport;
import com.lovely4k.backend.calendar.ScheduleType;
import com.lovely4k.backend.calendar.repository.response.FindCalendarsWithDateResponse;
import com.lovely4k.backend.calendar.repository.response.FindRecentCalendarsResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class CalendarQueryRepositoryTest extends QueryTestSupport {

    @Autowired
    CalendarQueryRepository calendarQueryRepository;

    @Sql(scripts = "/calendar.sql")
    @DisplayName("커플 id를 가지고 최근 일정, 커플의 구성원의 (id, 색상)을 조회할 수 있다.")
    @Test
    void findRecentCalendars() {
        // given -> calendar.sql 로그인 유저는 남자임
        int limit = 5;
        long coupleId = 1;
        long loginUserId = 1L;

        // when
        List<FindRecentCalendarsResponse> result = calendarQueryRepository.findRecentCalendarsWithColors(coupleId, limit, loginUserId);

        // then
        assertAll("Recent Calendars",
            () -> assertThat(result).hasSize(limit),
            () -> assertThat(result.get(0).scheduleType()).isEqualTo(ScheduleType.PERSONAL),
            () -> assertThat(result.get(0).myCalendarColor()).isEqualTo("RED"),
            () -> assertThat(result.get(0).opponentCalendarColor()).isEqualTo("BLUE"),
            () -> assertThat(result.get(0).ownerId()).isEqualTo(1L)
        );
    }

    @Sql(scripts = "/calendar.sql")
    @DisplayName("커플 id, 일정을 검색할 날짜(from ~ to)를 받아 일정, 커플의 구성원의 (id, 색상)을 조회할 수 있다.")
    @Test
    void findRecentCalendarsWithColors() {
        // given -> calendar.sql
        FindCalendarsWithDateRepositoryRequest request = new FindCalendarsWithDateRepositoryRequest(
            LocalDate.of(2123, 11, 1),
            LocalDate.of(2123, 11, 30)
        );
        Long loginUserId = 1L;

        // when
        List<FindCalendarsWithDateResponse> result = calendarQueryRepository.findCalendarsWithDate(request, 1L, loginUserId);

        // then
        assertAll("Find Calendars Between Start Date",
            () -> assertThat(result).hasSize(8),
            () -> assertThat(result.get(0).scheduleType()).isEqualTo(ScheduleType.PERSONAL),
            () -> assertThat(result.get(0).myCalendarColor()).isEqualTo("RED"),
            () -> assertThat(result.get(0).opponentCalendarColor()).isEqualTo("BLUE"),
            () -> assertThat(result.get(0).ownerId()).isEqualTo(1L)
        );
    }
}