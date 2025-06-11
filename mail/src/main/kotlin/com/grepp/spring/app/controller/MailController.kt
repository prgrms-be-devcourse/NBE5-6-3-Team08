package com.grepp.spring.app.controller

import com.grepp.spring.app.controller.payload.SmtpRequest
import com.grepp.spring.app.model.MailService
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("mail")
@PreAuthorize("hasRole('SERVER')")
class MailController(
    private val mailService:MailService
) {

    @PostMapping("send")
    fun send(
        @RequestBody
        @Valid
        request:SmtpRequest
    ){
        mailService.send(request)
    }
}