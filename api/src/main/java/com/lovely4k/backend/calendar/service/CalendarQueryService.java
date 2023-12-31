package com.lovely4k.backend.calendar.service;

import com.lovely4k.backend.calendar.repository.CalendarQueryRepository;
import com.lovely4k.backend.calendar.repository.FindCalendarsWithDateRepositoryRequest;
import com.lovely4k.backend.calendar.repository.response.FindCalendarsWithDateResponse;
import com.lovely4k.backend.calendar.repository.response.FindRecentCalendarsResponse;
import com.lovely4k.backend.calendar.service.response.FindCalendarsWithDateServiceResponse;
import com.lovely4k.backend.calendar.service.response.FindRecentCalendarsServiceResponse;
import com.lovely4k.backend.common.cache.CacheConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class CalendarQueryService {

    private final CalendarQueryRepository calendarQueryRepository;

    @Cacheable(value = CacheConstants.CALENDAR_WITH_DATE, key = "#coupleId + '_' + #loginUserId + '_' + #request.from() + '_' + #request.to()")
    public FindCalendarsWithDateServiceResponse findCalendarsWithDate(FindCalendarsWithDateRepositoryRequest request, Long coupleId, Long loginUserId) {
        List<FindCalendarsWithDateResponse> responses = calendarQueryRepository.findCalendarsWithDate(request, coupleId, loginUserId);
        return FindCalendarsWithDateServiceResponse.from(responses);
    }

    @Cacheable(value = CacheConstants.RECENT_CALENDAR, key = "#coupleId + '_' + #loginUserId")
    public FindRecentCalendarsServiceResponse findRecentCalendars(Long coupleId, Integer limit, Long loginUserId) {

        List<FindRecentCalendarsResponse> result = calendarQueryRepository.findRecentCalendarsWithColors(coupleId, limit, loginUserId);
        return FindRecentCalendarsServiceResponse.from(result);
    }
}