package com.freeboard04.api.user;

import com.freeboard04.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserApiController {

    private final HttpSession httpSession;
    private final UserService userService;

    @PostMapping
    private void join(@RequestBody UserForm user){
        userService.join(user);
    }

    @PostMapping(params = {"type=LOGIN"})
    private ResponseEntity<UserDto> login(@RequestBody UserForm user){
        UserDto userDto = userService.login(user);
        httpSession.setAttribute("USER", user);
        return ResponseEntity.ok(userDto);
    }
}
