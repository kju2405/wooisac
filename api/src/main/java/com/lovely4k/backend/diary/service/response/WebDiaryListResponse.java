package com.lovely4k.backend.diary.service.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lovely4k.backend.diary.response.DiaryListResponse;

import java.math.BigDecimal;
import java.time.LocalDate;

public record WebDiaryListResponse(
    Long diaryId,
    Long kakaoMapId,
    String imageUrl,
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate datingDay,
    String placeName,
    String address,
    BigDecimal latitude,
    BigDecimal longitude
) {

    public static WebDiaryListResponse from(DiaryListResponse diaryListResponse) {
        return new WebDiaryListResponse(
            diaryListResponse.diaryId(),
            diaryListResponse.kakaoMapId(),
            diaryListResponse.imageUrl(),
            diaryListResponse.datingDay(),
            diaryListResponse.placeName(),
            diaryListResponse.address(),
            diaryListResponse.latitude(),
            diaryListResponse.longitude());
    }
}
