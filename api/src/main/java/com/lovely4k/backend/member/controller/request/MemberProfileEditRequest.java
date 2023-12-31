package com.lovely4k.backend.member.controller.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lovely4k.backend.member.service.request.MemberProfileEditServiceRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record MemberProfileEditRequest(

    @NotBlank(message = "별명을 입력해주세요.")
    String nickname,

    @NotNull(message = "생일을 입력해주세요.")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate birthday,

    @NotBlank(message = "MBTI를 입력해주세요.")
    String mbti,

    @NotBlank(message = "개인 색상을 입력해주세요.")
    String calendarColor
) {
    public MemberProfileEditServiceRequest toServiceRequest() {
        return new MemberProfileEditServiceRequest(
            this.nickname,
            this.birthday,
            this.mbti,
            this.calendarColor
        );
    }
}
