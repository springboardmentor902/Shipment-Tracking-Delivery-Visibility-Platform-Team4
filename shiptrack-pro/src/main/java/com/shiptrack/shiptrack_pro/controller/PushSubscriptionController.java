package com.shiptrack.shiptrack_pro.controller;

import com.shiptrack.shiptrack_pro.dto.PushSubscriptionRequest;
import com.shiptrack.shiptrack_pro.entity.User;
import com.shiptrack.shiptrack_pro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/push")
@RequiredArgsConstructor
public class PushSubscriptionController {

    private final UserRepository userRepository;


    @PostMapping("/subscribe")
    public ResponseEntity<String> subscribe(
            @RequestBody PushSubscriptionRequest request,
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        user.setPushEndpoint(request.getEndpoint());
        user.setPushP256dh(request.getP256dh());
        user.setPushAuth(request.getAuth());

        userRepository.save(user);

        return ResponseEntity.ok(
                "Push subscription registered successfully"
        );
    }


    @DeleteMapping("/unsubscribe")
    public ResponseEntity<String> unsubscribe(
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        user.setPushEndpoint(null);
        user.setPushP256dh(null);
        user.setPushAuth(null);

        userRepository.save(user);

        return ResponseEntity.ok(
                "Push subscription removed successfully"
        );
    }


    private User getAuthenticatedUser(
            Authentication authentication) {

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Authenticated user not found"
                        )
                );
    }
}