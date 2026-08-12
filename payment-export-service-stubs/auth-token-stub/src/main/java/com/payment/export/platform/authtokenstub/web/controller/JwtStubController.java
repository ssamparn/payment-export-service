package com.payment.export.platform.authtokenstub.web.controller;

import com.payment.export.platform.authtokenstub.service.JwtStubService;
import com.payment.export.platform.authtokenstub.web.dto.GenerateTokenResponse;
import com.payment.export.platform.authtokenstub.web.dto.VerifySignatureRequest;
import com.payment.export.platform.authtokenstub.web.dto.VerifySignatureResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("/stub/jwt")
public class JwtStubController {

    private final JwtStubService jwtStubService;

    public JwtStubController(JwtStubService jwtStubService) {
        this.jwtStubService = jwtStubService;
    }

    @GetMapping("/token")
    public GenerateTokenResponse generateToken(@RequestParam String userId,
                                               @RequestParam String customerName,
                                               @RequestParam(defaultValue = "") String customerAgreementId,
                                               @RequestParam(required = false) Long expirySeconds) {
        if (userId == null || userId.isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "userId is required");
        }

        if (customerName == null || customerName.isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "customerName is required");
        }

        JwtStubService.GeneratedJwt generatedJwt =
                jwtStubService.generateToken(userId, customerName, customerAgreementId, expirySeconds);
        return new GenerateTokenResponse(generatedJwt.token(), generatedJwt.expiresAtEpochSeconds());
    }

    @PostMapping("/verify-signature")
    public VerifySignatureResponse verifySignature(@RequestBody VerifySignatureRequest request) {
        boolean valid = jwtStubService.verifySignature(
                request.encodedHeader(),
                request.encodedPayload(),
                request.encodedSignature()
        );
        return new VerifySignatureResponse(valid);
    }
}

