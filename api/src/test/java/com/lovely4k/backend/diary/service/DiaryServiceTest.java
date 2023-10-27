package com.lovely4k.backend.diary.service;

import com.lovely4k.backend.IntegrationTestSupport;
import com.lovely4k.backend.common.imageuploader.ImageUploader;
import com.lovely4k.backend.diary.Diary;
import com.lovely4k.backend.diary.DiaryRepository;
import com.lovely4k.backend.diary.service.request.DiaryCreateRequest;
import com.lovely4k.backend.diary.service.response.DiaryDetailResponse;
import com.lovely4k.backend.location.Category;
import com.lovely4k.backend.location.Location;
import com.lovely4k.backend.location.LocationRepository;
import com.lovely4k.backend.member.Member;
import com.lovely4k.backend.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

class DiaryServiceTest extends IntegrationTestSupport {

    @Autowired
    DiaryService diaryService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    DiaryRepository diaryRepository;

    @Autowired
    LocationRepository locationRepository;

    @MockBean
    ImageUploader imageUploader;

    @AfterEach
    void tearDown() {
        memberRepository.deleteAllInBatch();
        diaryRepository.deleteAllInBatch();
        locationRepository.deleteAllInBatch();
    }

    @DisplayName("createDiary 메서드를 통해 다이어리를 생성할 수 있다.")
    @Test
    void createDiary() {
        // given
        Member member = Member.builder()
                .name("tommy")
                .sex("boy")
                .build();
        memberRepository.save(member);

        MockMultipartFile firstImage = new MockMultipartFile("images", "image1.png", "image/png", "some-image".getBytes());
        MockMultipartFile secondImage = new MockMultipartFile("images", "image2.png", "image/png", "some-image".getBytes());
        List<MultipartFile> multipartFileList = List.of(firstImage, secondImage);

        DiaryCreateRequest diaryCreateRequest =
                new DiaryCreateRequest(1L, "경기도 일산", 4, LocalDate.of(2023, 10, 20), "ACCOMODATION", "테스트 다이어리");

        // stubbing
        given(imageUploader.upload(any(String.class), any())
        ).willReturn(List.of("first-image-url", "second-image-url"));

        // when
        Long diaryId = diaryService.createDiary(multipartFileList, diaryCreateRequest, member.getId());

        // then
        Diary findDiary = diaryRepository.findById(diaryId).orElseThrow();
        Location findLocation = locationRepository.findById(findDiary.getLocation().getId()).orElseThrow();

        assertAll(
                () -> assertThat(findDiary.getId()).isEqualTo(diaryId),
                () -> assertThat(findDiary.getBoyText()).isNotNull().isEqualTo("테스트 다이어리"),
                () -> assertThat(findLocation.getKakaoMapId()).isEqualTo(1L),
                () -> assertThat(findDiary.getPhotos()).isNotNull(),
                () -> assertThat(findDiary.getPhotos().getFirstImage()).isEqualTo("first-image-url"),
                () -> assertThat(findDiary.getPhotos().getSecondImage()).isEqualTo("second-image-url")
        );

    }

    @DisplayName("유효하지 않은 Member Id로 다이어리 생성하려고 할 경우 EntityNotFoundException이 발생한다.")
    @Test
    void createDiaryInvalidMemberId() {
        // given
        Member member = Member.builder()
                .name("tommy")
                .sex("boy")
                .build();
        memberRepository.save(member);
        Long invalidMemberId = member.getId() + 1;

        MockMultipartFile firstImage = new MockMultipartFile("images", "image1.png", "image/png", "some-image".getBytes());
        MockMultipartFile secondImage = new MockMultipartFile("images", "image2.png", "image/png", "some-image".getBytes());
        List<MultipartFile> multipartFileList = List.of(firstImage, secondImage);

        // stubbing
        given(imageUploader.upload(any(String.class), any())
        ).willReturn(List.of("first-image-url", "second-image-url"));

        DiaryCreateRequest diaryCreateRequest =
                new DiaryCreateRequest(1L, "경기도 일산", 4, LocalDate.of(2023, 10, 20), "ACCOMODATION", "테스트 다이어리");

        // when && then
        assertThatThrownBy(
                () -> diaryService.createDiary(multipartFileList, diaryCreateRequest, invalidMemberId)
        ).isInstanceOf(EntityNotFoundException.class)
                .hasMessage("invalid member id");
    }

    @DisplayName("5개 이상의 이미지를 업로드 하려고 하는 경우 IllegalArgumentException이 발생한다.")
    @Test
    void createDiaryTooMuchImage() {
        // given
        Member member = Member.builder()
                .name("tommy")
                .sex("boy")
                .build();
        memberRepository.save(member);
        Long memberId = member.getId();

        List<MultipartFile> multipartFileList = getMultipartFiles();

        // stubbing
        given(imageUploader.upload(any(String.class), any())
        ).willReturn(List.of("first-image-url", "second-image-url","third-image-url", "fourth-image-url", "fifth-image-url", "sixth-image-url"));

        DiaryCreateRequest diaryCreateRequest =
                new DiaryCreateRequest(1L, "경기도 일산", 4, LocalDate.of(2023, 10, 20), "ACCOMODATION", "테스트 다이어리");

        // when && then
        assertThatThrownBy(
                () -> diaryService.createDiary(multipartFileList, diaryCreateRequest, memberId)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Image file can be uploaded maximum 5");
    }

    private static List<MultipartFile> getMultipartFiles() {
        MockMultipartFile firstImage = new MockMultipartFile("images", "image1.png", "image/png", "some-image".getBytes());
        MockMultipartFile secondImage = new MockMultipartFile("images", "image2.png", "image/png", "some-image".getBytes());
        MockMultipartFile thirdImage = new MockMultipartFile("images", "image3.png", "image/png", "some-image".getBytes());
        MockMultipartFile fourthImage = new MockMultipartFile("images", "image4.png", "image/png", "some-image".getBytes());
        MockMultipartFile fifthImage = new MockMultipartFile("images", "image5.png", "image/png", "some-image".getBytes());
        MockMultipartFile sixthImage = new MockMultipartFile("images", "image6.png", "image/png", "some-image".getBytes());
        return List.of(firstImage, secondImage, thirdImage, fourthImage, fifthImage, sixthImage);
    }

    @DisplayName("getDiaryDetail을 통해 다이어리 상세 정보를 조회할 수 있다.")
    @Test
    void getDiaryDetail() {
        // given

        Location location = Location.create(10L, "경기도 고양시", Category.FOOD);
        Diary diary = buildDiary(location, 1L);
        diaryRepository.save(diary);

        Member member = buildMember();
        memberRepository.save(member);

        // when
        DiaryDetailResponse diaryDetailResponse =
                diaryService.getDiaryDetail(diary.getId(), member.getId());

        // then
        assertAll(
                () -> assertThat(diaryDetailResponse.kakaoMapId()).isEqualTo(10L),
                () -> assertThat(diaryDetailResponse.boyText()).isEqualTo("hello"),
                () -> assertThat(diaryDetailResponse.girlText()).isEqualTo("hi"),
                () -> assertThat(diaryDetailResponse.score()).isEqualTo(4)
        );
    }

    @DisplayName("다이어리 상세 정보 조회 시 잘못된 diary id인 경우 EntityNotFoundException이 발생한다. ")
    @Test
    void getDiaryDetailInvalidDiaryId() {
        // given

        Location location = Location.create(10L, "경기도 고양시", Category.FOOD);
        Diary diary = buildDiary(location, 1L);
        diaryRepository.save(diary);

        Member member = buildMember();
        memberRepository.save(member);

        Long invalidDiaryId = diary.getId() + 1;
        Long memberId = member.getId();

        // when && then
        assertThatThrownBy(
                () -> diaryService.getDiaryDetail(invalidDiaryId, memberId)
        ).isInstanceOf(EntityNotFoundException.class)
                .hasMessage("invalid diary id");

    }

    @DisplayName("다이어리 상세 정보 조회 시 다른 커플이 작성한 다이어리는 조회할 수 없다. ")
    @Test
    void getDiaryDetailNoAuthority() {
        // given

        Location location = Location.create(10L, "경기도 고양시", Category.FOOD);
        Diary diary = buildDiary(location, 2L);
        diaryRepository.save(diary);

        Member member = buildMember();
        memberRepository.save(member);

        Long diaryId = diary.getId();
        Long memberId = member.getId();

        // when && then
        assertThatThrownBy(
                () -> diaryService.getDiaryDetail(diaryId, memberId)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("you can only manage your couple's diary");
    }

    @DisplayName("deleteDiary 메서드를 통해 다이어리를 삭제할 수 있다. ")
    @Test
    void deleteDiary() {
        // given
        Location location = Location.create(10L, "경기도 고양시", Category.FOOD);
        Diary diary = buildDiary(location, 1L);
        diaryRepository.save(diary);

        Member member = buildMember();
        memberRepository.save(member);

        Long diaryId = diary.getId();
        Long memberId = member.getId();

        // when
        diaryService.deleteDiary(diaryId, memberId);

        // then
        Optional<Diary> optionalDiary = diaryRepository.findById(diaryId);
        assertThat(optionalDiary).isEmpty();
    }

    @DisplayName("다른 커플의 다이어리를 삭제할 수 없다.")
    @Test
    void deleteDiaryNoAuthority() {
        // given
        Location location = Location.create(10L, "경기도 고양시", Category.FOOD);
        Diary diary = buildDiary(location, 2L);
        diaryRepository.save(diary);

        Member member = buildMember();
        memberRepository.save(member);

        Long diaryId = diary.getId();
        Long memberId = member.getId();

        // when && then
        assertThatThrownBy(
                () -> diaryService.deleteDiary(diaryId, memberId)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("you can only manage your couple's diary");

    }

    private static Diary buildDiary(Location location, long coupleId) {
        return Diary.builder()
                .coupleId(coupleId)
                .location(location)
                .boyText("hello")
                .girlText("hi")
                .score(4)
                .datingDay(LocalDate.of(2023, 10, 20))
                .build();
    }

    private static Member buildMember() {
        return Member.builder()
                .sex("boy")
                .coupleId(1L)
                .name("Tommy")
                .build();
    }
}
