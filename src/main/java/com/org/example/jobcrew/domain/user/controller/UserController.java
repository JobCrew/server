package com.org.example.jobcrew.domain.user.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "회원 관리", description = "사용자 계정 및 프로필 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping(
        value = "/api/users",
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class UserController {
}
