package com.tickerBell.domain.member.service;

import com.tickerBell.domain.member.dtos.JoinSmsValidationRequest;
import com.tickerBell.domain.member.dtos.JoinSmsValidationResponse;
import com.tickerBell.domain.member.dtos.RefreshTokenRequest;
import com.tickerBell.domain.member.entity.AuthProvider;
import com.tickerBell.domain.member.entity.Role;
import com.tickerBell.domain.member.dtos.LoginResponse;

public interface MemberService {

    Long join(String username, String password, String phone, Role role, AuthProvider authProvider);
    LoginResponse regenerateToken(RefreshTokenRequest refreshTokenRequest);
    LoginResponse login(String username, String password);
    JoinSmsValidationResponse joinSmsValidation(JoinSmsValidationRequest request);
}
