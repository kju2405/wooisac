package com.lovely4k.backend.couple.service;

import com.lovely4k.backend.couple.Couple;
import com.lovely4k.backend.couple.repository.CoupleQueryRepository;
import com.lovely4k.backend.couple.CoupleCreatedEvent;
import com.lovely4k.backend.couple.CoupleUpdatedEvent;
import com.lovely4k.backend.couple.repository.CoupleRepository;
import com.lovely4k.backend.couple.repository.response.FindCoupleProfileResponse;
import com.lovely4k.backend.couple.service.request.CoupleProfileEditServiceRequest;
import com.lovely4k.backend.couple.service.response.CoupleProfileGetResponse;
import com.lovely4k.backend.couple.service.response.InvitationCodeCreateResponse;
import com.lovely4k.backend.member.Member;
import com.lovely4k.backend.member.Sex;
import com.lovely4k.backend.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CoupleService {

    private final CoupleRepository coupleRepository;
    private final MemberRepository memberRepository;
    private final CoupleQueryRepository coupleQueryRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public InvitationCodeCreateResponse createInvitationCode(Long requestedMemberId, String sex) {
        String invitationCode = UUID.randomUUID().toString();

        Couple couple = Couple.create(requestedMemberId, Sex.valueOf(sex), invitationCode);
        Couple savedCouple = coupleRepository.save(couple);
        eventPublisher.publishEvent(new CoupleCreatedEvent(couple));
        return new InvitationCodeCreateResponse(savedCouple.getId(), invitationCode);
    }

    @Transactional
    public void registerCouple(String invitationCode, Long receivedMemberId) {
        Couple couple = validateInvitationCode(invitationCode);
        couple.registerPartnerId(receivedMemberId);

        registerCoupleId(couple);
        eventPublisher.publishEvent(new CoupleUpdatedEvent(couple));
    }

    public CoupleProfileGetResponse findCoupleProfile(Long memberId) {

        FindCoupleProfileResponse response = coupleQueryRepository.findCoupleProfile(memberId);
        return CoupleProfileGetResponse.from(response);
    }

    @Transactional
    public void updateCoupleProfile(CoupleProfileEditServiceRequest request, Long memberId) {
        Long coupleId = findMember(memberId).getCoupleId();
        Couple couple = findCouple(coupleId);

        couple.update(request.meetDay());
    }

    @Transactional
    public void increaseTemperature(Long coupleId) {
        Couple couple = coupleRepository.findByIdWithOptimisticLock(coupleId)
            .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 커플 id 입니다."));  // NOSONAR
        couple.increaseTemperature();
    }

    @Transactional
    public void deleteCouple(Long coupleId, Long memberId) {
        Couple couple = findCouple(coupleId);
        couple.checkAuthority(memberId);
        coupleRepository.delete(couple);
    }

    @Transactional
    public void reCouple(LocalDate requestedDate, Long coupleId, Long memberId) {
        Couple couple = findDeletedCouple(coupleId);
        Long opponentId = couple.getOpponentId(memberId);
        findMember(opponentId).checkReCoupleCondition(coupleId);

        couple.recouple(memberId, requestedDate);
    }

    private Optional<Member> findMemberOptional(Long memberId) {
        return memberRepository.findById(memberId);
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원 id 입니다."));
    }

    private Couple findCouple(Long coupleId) {
        return coupleRepository.findById(coupleId)
            .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 커플 id 입니다."));
    }

    private Couple findDeletedCouple(Long coupleId) {
        return coupleRepository.findDeletedById(coupleId)
            .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 커플 id 입니다."));
    }

    private Couple validateInvitationCode(String invitationCode) {
        return coupleRepository.findByInvitationCode(invitationCode)
            .orElseThrow(() -> new EntityNotFoundException("유효하지 않은 초대코드 입니다."));
    }

    private void registerCoupleId(Couple couple) {
        Member boy = findMember(couple.getBoyId());
        Member girl = findMember(couple.getGirlId());
        boy.registerCoupleId(couple.getId());
        girl.registerCoupleId(couple.getId());
    }
}