package com.nemo.oceanAcademy.domain.user.application.controller;

import com.nemo.oceanAcademy.domain.user.application.dto.UserCreateDTO;
import com.nemo.oceanAcademy.domain.user.application.dto.UserResponseDTO;
import com.nemo.oceanAcademy.domain.user.application.dto.UserUpdateDTO;
import com.nemo.oceanAcademy.domain.user.application.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 사용자 정보 조회
    @GetMapping
    public ResponseEntity<UserResponseDTO> getUserInfo(@RequestParam String userId) {
        UserResponseDTO userDTO = userService.getUserInfoById(userId);
        return ResponseEntity.ok(userDTO);
    }

    // 사용자 생성
  /*  @PostMapping
    public ResponseEntity<String> createUser(@RequestBody @Valid UserCreateDTO userCreateDTO) {
        userService.createUser(userCreateDTO);
        return ResponseEntity.ok("사용자가 생성되었습니다.");
    }*/

    // 사용자 프로필 업데이트
    @PatchMapping
    public ResponseEntity<String> updateUserProfile(
            @RequestParam("userId") String userId,
            @RequestBody @Valid UserUpdateDTO userUpdateDTO,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        userService.updateUserProfile(userId, userUpdateDTO, file);
        return ResponseEntity.ok("회원 정보가 수정되었습니다.");
    }

    // 닉네임 중복 확인
    @GetMapping("/checkNickname")
    public ResponseEntity<String> checkNickname(@RequestParam("nickname") String nickname) {
        boolean isAvailable = userService.isNicknameAvailable(nickname);
        if (isAvailable) {
            return ResponseEntity.ok("사용 가능한 닉네임입니다.");
        } else {
            return ResponseEntity.status(409).body("중복된 닉네임입니다.");
        }
    }
}
