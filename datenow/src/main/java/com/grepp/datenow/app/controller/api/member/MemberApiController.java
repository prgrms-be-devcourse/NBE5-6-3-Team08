package com.grepp.datenow.app.controller.api.member;

import com.grepp.datenow.app.controller.web.member.payload.MemberUpdateRequest;
import com.grepp.datenow.app.controller.web.member.payload.OAuthSignupRequest;
import com.grepp.datenow.app.controller.web.member.payload.SignupRequest;
import com.grepp.datenow.app.model.auth.code.Role;
import com.grepp.datenow.app.model.auth.domain.Principal;
import com.grepp.datenow.app.model.like.dto.FavoriteCourseResponse;
import com.grepp.datenow.app.model.like.service.FavoriteService;
import com.grepp.datenow.app.model.member.dto.MemberDto;
import com.grepp.datenow.app.model.member.entity.Member;
import com.grepp.datenow.app.model.member.repository.MemberRepository;
import com.grepp.datenow.app.model.member.service.MemberService;
import com.grepp.datenow.infra.auth.oauth2.user.OAuth2UserInfo;
import com.grepp.datenow.infra.error.exception.CommonException;
import com.grepp.datenow.infra.response.ApiResponse;
import com.grepp.datenow.infra.response.ResponseCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/members")
public class MemberApiController {

    private final MemberService memberService;
    private final ModelMapper modelMapper;
    private final FavoriteService favoriteService;


    @GetMapping("/exists")
    public ResponseEntity<ApiResponse<Boolean>> checkUserId(@RequestParam String userId) {
        boolean exists = memberService.isExistsId(userId);
        return ResponseEntity.ok(ApiResponse.success(exists));
    }

    @GetMapping("/check/email")
    public ResponseEntity<ApiResponse<Boolean>> checkEmail(@RequestParam String email) {
        boolean exists = memberService.isExistsEmail(email);
        return ResponseEntity.ok(ApiResponse.success(exists));
    }

    @GetMapping("/check/nickname")
    public ResponseEntity<ApiResponse<Boolean>> checkNickname(@RequestParam String nickname) {
        boolean exists = memberService.isExistsNickname(nickname);
        return ResponseEntity.ok(ApiResponse.success(exists));
    }

    // 회원 가입 요청
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<?>> unifiedSignup(
        @RequestBody @Valid MemberDto dto,
        HttpSession session
    ) {
        OAuth2UserInfo userInfo = (OAuth2UserInfo) session.getAttribute("oauth2_user_info");

        // userInfo가 null이면 일반 회원가입
        memberService.signup(dto, Role.ROLE_USER, session, userInfo);

        // OAuth일 경우 세션 삭제
        if (userInfo != null) {
            session.removeAttribute("oauth2_user_info");
        }

        return ResponseEntity
            .status(ResponseCode.OK.status())
            .body(ApiResponse.success(Map.of("message", "회원가입 인증 메일이 전송되었습니다.")));
    }

    @PutMapping("/edit/{user_id}")
    public ResponseEntity<ApiResponse<?>> updateMember(
        @PathVariable("user_id") String userId,
        @RequestBody MemberUpdateRequest request) {

        memberService.updateMember(userId, request);
        return ResponseEntity
            .status(ResponseCode.OK.status())
            .body(ApiResponse.success(Map.of("message", "회원정보가 성공적으로 수정되었습니다.")));
    }

    @GetMapping("/favorites")
    public ResponseEntity<List<FavoriteCourseResponse>> getMyFavorites(
        @AuthenticationPrincipal Principal principal
    ) {
        String userId = principal.getUsername();
        List<FavoriteCourseResponse> favorites = favoriteService.getFavoriteCourses(userId);

        return ResponseEntity.ok(favorites);
    }

    @PatchMapping("/favorites/{favoriteCourseId}")
    public ResponseEntity<Void> deleteFavorite(@PathVariable Long favoriteCourseId) {
        favoriteService.deactivateFavorite(favoriteCourseId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/deactivate")
    public ResponseEntity<Void> deactivateMember(
        @AuthenticationPrincipal Principal principal,
        HttpServletRequest request,
        HttpServletResponse response) throws ServletException {

        String userId = principal.getUsername();
        memberService.deactivateMember(userId);

        request.logout();
        return ResponseEntity.ok().build();
    }

    // 회원 정보 수정 페이지 데이터 전달 + 마이페이지 사이드바
    @GetMapping("/info")
    public ResponseEntity<MemberDto> getMemberInfo(@AuthenticationPrincipal Principal principal) {
        String userId = principal.getUsername();
        Member member = memberService.findByUserId(userId);
        MemberDto dto = MemberDto.from(member);

        return ResponseEntity.ok(dto);
    }
}
