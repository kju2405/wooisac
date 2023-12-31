package com.lovely4k.backend.diary.controller;

import com.lovely4k.backend.common.ApiResponse;
import com.lovely4k.backend.common.sessionuser.LoginUser;
import com.lovely4k.backend.common.sessionuser.SessionUser;
import com.lovely4k.backend.diary.controller.request.DiaryDeleteRequest;
import com.lovely4k.backend.diary.controller.request.WebDiaryCreateRequest;
import com.lovely4k.backend.diary.controller.request.WebDiaryEditRequest;
import com.lovely4k.backend.diary.service.DiaryQueryService;
import com.lovely4k.backend.diary.service.DiaryService;
import com.lovely4k.backend.diary.service.response.WebDiaryListByMarkerResponse;
import com.lovely4k.backend.diary.service.response.DiaryListInGridResponse;
import com.lovely4k.backend.diary.service.response.WebDiaryDetailResponse;
import com.lovely4k.backend.diary.service.response.WebDiaryListResponse;
import com.lovely4k.backend.location.Category;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
@RequestMapping(value = "/v1/diaries", produces = HAL_JSON_VALUE)
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;
    private final DiaryQueryService diaryQueryService;

    private static final String DETAIL = "get detail information of diary";
    private static final String EDIT = "edit diary";
    private static final String DELETE = "delete diary";


    @SneakyThrows
    @PostMapping(consumes = MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> createDiary(
        @RequestPart(value = "images", required = false) List<MultipartFile> multipartFileList,
        @RequestPart(value = "texts") @Valid WebDiaryCreateRequest request,
        @LoginUser SessionUser sessionUser
    ) {
        Long diaryId = diaryService.createDiary(multipartFileList, request.toServiceRequest(), sessionUser.memberId(), sessionUser.coupleId());

        return ApiResponse.created(diaryId,
            linkTo(methodOn(DiaryController.class).createDiary(multipartFileList, request, sessionUser)).withSelfRel(),
            linkTo(DiaryController.class.getMethod("getDiaryDetail", Long.class, SessionUser.class)).withRel(DETAIL),   // NOSONAR
            linkTo(DiaryController.class.getMethod("getDiaryList", SessionUser.class, Category.class, Pageable.class)).withRel("get list of diary"),
            linkTo(DiaryController.class).slash(diaryId).withRel(EDIT),
            linkTo(DiaryController.class).slash(diaryId).withRel(DELETE)
        );
    }

    @SneakyThrows
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WebDiaryDetailResponse>> getDiaryDetail(
            @PathVariable Long id,
            @LoginUser SessionUser sessionUser
    ) {

        return ApiResponse.ok(diaryQueryService.findDiaryDetail(id, sessionUser.coupleId(), sessionUser.memberId()),
                linkTo(methodOn(DiaryController.class).getDiaryDetail(id, sessionUser)).withSelfRel(),
                linkTo(DiaryController.class.getMethod("editDiary", Long.class, List.class, WebDiaryEditRequest.class, SessionUser.class)).withRel(EDIT),
                linkTo(DiaryController.class).slash(id).withRel(DELETE)
        );
    }
    @SneakyThrows
    @GetMapping
    public ResponseEntity<ApiResponse<Page<WebDiaryListResponse>>> getDiaryList(
            @LoginUser SessionUser sessionUser,
            @RequestParam(required = false) Category category,
            @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ApiResponse.ok(diaryQueryService.findDiaryList(sessionUser.coupleId(), category, pageable),
                linkTo(DiaryController.class.getMethod("getDiaryDetail", Long.class, SessionUser.class)).withRel(DETAIL),
                linkTo(DiaryController.class.getMethod("editDiary", Long.class, List.class, WebDiaryEditRequest.class, SessionUser.class)).withRel(EDIT),
                linkTo(DiaryController.class.getMethod("deleteDiary", Long.class, SessionUser.class)).withRel(DELETE)
        );
    }

    @SneakyThrows
    @GetMapping("/marker/{kakaoMapId}")
    public ResponseEntity<ApiResponse<WebDiaryListByMarkerResponse>> getDiaryListByMarker(
        @PathVariable Long kakaoMapId,
        @LoginUser SessionUser sessionUser
    ) {
        return ApiResponse.ok(diaryQueryService.findDiaryListByMarker(kakaoMapId, sessionUser.coupleId()),
            linkTo(DiaryController.class.getMethod("getDiaryDetail", Long.class, SessionUser.class)).withRel(DETAIL)    // NOSONAR
        );
    }

    @PatchMapping(path = "/{id}", consumes = MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> editDiary(
            @PathVariable Long id,
            @RequestPart(value = "images", required = false) List<MultipartFile> multipartFileList,
            @RequestPart(value = "texts") @Valid WebDiaryEditRequest request,
            @LoginUser SessionUser sessionUser
    ) {
        diaryService.editDiary(id, multipartFileList, request.toServiceRequest(), sessionUser.coupleId(), sessionUser.memberId());
        return ApiResponse.ok(
                linkTo(DiaryController.class).slash(id).withRel(DETAIL),
                linkTo(DiaryController.class).slash(id).withRel(DELETE)
        );
    }

    @GetMapping("/location")
    public ResponseEntity<ApiResponse<DiaryListInGridResponse>> getDiaryListInGrid(
        @RequestParam("right_latitude") BigDecimal rLatitude,
        @RequestParam("right_longitude") BigDecimal rLongitude,
        @RequestParam("left_latitude") BigDecimal lLatitude,
        @RequestParam("left_longitude") BigDecimal lLongitude,
        @LoginUser SessionUser sessionUser
    ) {

        return ApiResponse.ok(diaryQueryService.findDiaryListInGrid(rLatitude, rLongitude, lLatitude, lLongitude, sessionUser.coupleId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDiary(
            @PathVariable Long id,
            @LoginUser SessionUser sessionUser
    ) {
        diaryService.deleteDiary(id, sessionUser.coupleId());

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/delete")
    public ResponseEntity<Void> deleteDiaries(
        @RequestBody DiaryDeleteRequest request,
        @LoginUser SessionUser sessionUser
    ) {
        diaryService.deleteDiaries(request, sessionUser.coupleId());
        return ResponseEntity.noContent().build();
    }
}