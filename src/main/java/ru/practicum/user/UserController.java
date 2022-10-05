package ru.practicum.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/email")
    public List<UserShortWithIP> getAllUsersByEmail(@RequestParam String email) {
        return userService.getUsersEmailWithIp(email);
    }

    @PostMapping
    //@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public UserDto saveNewUser(@RequestBody UserDto user) {
        return userService.saveUser(user);
    }
}