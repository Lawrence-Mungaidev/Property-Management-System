package com.merlin.PropertManagmentSystem.Auth;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/auth")
@RestController
public class AuthController {

    private final AuthService authService;

    @PostMapping("/registerLandlord")
    public ResponseEntity<AuthRegisterResponse> registerLandlord(
            @Valid @RequestBody UserRegisterDto dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerLandLord(dto));
    }

    @PostMapping("/registerOrganisation")
    public ResponseEntity<AuthRegisterResponse> registerOrganisation(
            @Valid @RequestBody OrganisationRegisterDto dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerOrganisation(dto));
    }

    @PostMapping("/logIn")
    public ResponseEntity<LoginResponseDto> logIn(
            @Valid @RequestBody LoginDto dto
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.login(dto));
    }
}