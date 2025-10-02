package com.org.example.jobcrew.domain.user.dto.response;

import com.org.example.jobcrew.domain.user.entity.User;
import com.org.example.jobcrew.domain.user.entity.UserProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserNicknameResponse {
    private Long userId;
    private String nickname;

    public static UserNicknameResponse from(User user) {
        UserProfile profile = user.getProfile();
        return UserNicknameResponse.builder()
                .userId(user.getId())
                .nickname(profile != null ? profile.getNickname() : null)
                .build();
    }
}
