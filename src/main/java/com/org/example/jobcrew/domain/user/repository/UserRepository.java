package com.org.example.jobcrew.domain.user.repository;


import com.org.example.jobcrew.domain.user.entity.User;
import com.org.example.jobcrew.domain.user.entity.UserProfile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long>{
    Optional<User> findById(Long id);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findBynickname(String nickname);

    /**
     * User 정보 fetch join 으로 조회
     * Lazy Initialization Exception 방지
     */
    @Query("Select u From User u Where u.id =:id")
    Optional<User> findByIdForAuth(@Param("id")Long id);

    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmailForAuth(@Param("email") String email);

    @Query("SELECT u.profile FROM User u WHERE u.profile.username = :username")
    Optional<UserProfile> findByUsernameForAuth(@Param("username") String username);

    @Query("SELECT u.profile FROM User u WHERE u.profile.nickname = :nickname")
    Optional<UserProfile> findByNickname(@Param("nickname") String nickname);

    // 중복된 nickname이 있는지
    boolean existsByNickname(String Nickname);


}

