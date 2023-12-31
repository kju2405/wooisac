package com.lovely4k.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lovely4k.backend.authentication.CustomSuccessHandler;
import com.lovely4k.backend.authentication.MyOAuth2Member;
import com.lovely4k.backend.authentication.OAuth2UserService;
import com.lovely4k.backend.authentication.OAuthAttributes;
import com.lovely4k.backend.authentication.exception.AccessDeniedHandlerException;
import com.lovely4k.backend.authentication.exception.AuthenticationEntryPointException;
import com.lovely4k.backend.authentication.token.TokenProvider;
import com.lovely4k.backend.authentication.token.UserDetailsServiceImpl;
import com.lovely4k.backend.calendar.controller.CalendarController;
import com.lovely4k.backend.calendar.service.CalendarCommandService;
import com.lovely4k.backend.calendar.service.CalendarQueryService;
import com.lovely4k.backend.couple.controller.CoupleController;
import com.lovely4k.backend.couple.repository.CoupleRepository;
import com.lovely4k.backend.couple.service.CoupleService;
import com.lovely4k.backend.diary.controller.DiaryController;
import com.lovely4k.backend.diary.service.DiaryQueryService;
import com.lovely4k.backend.diary.service.DiaryService;
import com.lovely4k.backend.member.Member;
import com.lovely4k.backend.member.Role;
import com.lovely4k.backend.member.Sex;
import com.lovely4k.backend.member.controller.MemberController;
import com.lovely4k.backend.member.service.MemberService;
import com.lovely4k.backend.question.controller.QuestionController;
import com.lovely4k.backend.question.service.QuestionQueryService;
import com.lovely4k.backend.question.service.QuestionService;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.Collections;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@ActiveProfiles("test")
@Import(ControllerTestSupport.SecurityConfig.class)
@WebMvcTest(controllers = {
    DiaryController.class,
    MemberController.class,
    QuestionController.class,
    CoupleController.class,
    CalendarController.class
})
public abstract class ControllerTestSupport {

    @Autowired
    WebApplicationContext context;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    protected DiaryService diaryService;

    @MockBean
    protected DiaryQueryService diaryQueryService;

    @MockBean
    protected MemberService memberService;

    @MockBean
    protected QuestionService questionService;

    @MockBean
    protected CoupleService coupleService;

    @MockBean
    protected QuestionQueryService questionQueryService;

    @MockBean
    OAuth2UserService oAuth2UserService;

    @MockBean
    protected SecurityContext securityContext;

    @MockBean
    protected CoupleRepository coupleRepository;

    @MockBean
    protected CustomSuccessHandler customSuccessHandler;

    @MockBean
    protected TokenProvider tokenProvider;

    @MockBean
    protected UserDetailsServiceImpl userDetailsServiceImpl;

    @MockBean
    AuthenticationEntryPointException authenticationEntryPointException;

    @MockBean
    AccessDeniedHandlerException accessDeniedHandlerException;

    @Mock
    protected Authentication authentication;

    @MockBean
    protected CalendarCommandService calendarCommandService;

    @MockBean
    protected CalendarQueryService calendarQueryService;

    @TestConfiguration
    static class SecurityConfig {

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

            http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .headers(header -> header.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
            ;

            // Authorize of url
            http
                .authorizeHttpRequests(
                    authorize -> authorize.anyRequest().permitAll()
                );

            return http.build();
        }
    }

    @BeforeEach
    void setUp() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        Member member = mock(Member.class);
        given(member.getId()).willReturn(1L);
        given(member.getCoupleId()).willReturn(1L);
        given(member.getSex()).willReturn(Sex.MALE);
        given(member.getNickname()).willReturn("깜찍이");
        given(member.getRole()).willReturn(Role.USER);
        given(member.getImageUrl()).willReturn("imageUrl");
        given(authentication.getName()).willReturn(Role.USER.name());
        OAuthAttributes oAuthAttributes = new OAuthAttributes(
            Collections.emptyMap(),
            "test",
            "nickName",
            "email",
            "imageUrl",
            "ageRange",
            "sex"
        );
        MyOAuth2Member myOAuth2Member = new MyOAuth2Member(Collections.singleton(new SimpleGrantedAuthority(member.getRole().getKey())), oAuthAttributes.nameAttributeKey(), member);

        when(authentication.getPrincipal()).thenReturn(myOAuth2Member);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(Role.USER.name());
        securityContext.setAuthentication(authentication);

        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .addFilter(new CharacterEncodingFilter("UTF-8", true))
            .apply(springSecurity())
            .build();
    }
}