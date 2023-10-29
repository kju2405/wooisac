package com.lovely4k.backend.member;

import com.lovely4k.backend.common.jpa.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "couple_id")
    private Long coupleId;

    @Column(length = 7, name = "sex")
    @Enumerated(EnumType.STRING)
    private Sex sex;

    @Column(length = 15, name = "name")
    private String name;

    @Column(name = "nick_name")
    private String nickname;

    @Column(name = "birthday")
    private LocalDate birthday;

    @Column(length = 7, name = "mbti")
    private String mbti;

    @Column(length = 31, name = "calendar_color")
    private String calendarColor;

    @Column(name = "image_url")
    private String imageUrl;

    @Builder
    private Member(Long coupleId, Sex sex, String name, String nickname, LocalDate birthday, String mbti, String calendarColor, String imageUrl) { // NOSONAR
        this.coupleId = coupleId;
        this.sex = sex;
        this.name = name;
        this.nickname = nickname;
        this.birthday = birthday;
        this.mbti = mbti;
        this.calendarColor = calendarColor;
        this.imageUrl = imageUrl;
    }

    public void updateProfile(Sex sex, String imageUrl, String name, String nickname, LocalDate birthday, String mbti, String calendarColor) {
        this.sex = sex;
        this.name = name;
        this.nickname = nickname;
        this.birthday = birthday;
        this.mbti = mbti;
        this.calendarColor = calendarColor;
        this.imageUrl = imageUrl;
        Member.builder().build();
    }

    public void registerCoupleId(Long id) {
        this.coupleId = id;
    }
}
