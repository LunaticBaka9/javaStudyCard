package com.lunabaka.javastudycard.controller;

import com.lunabaka.javastudycard.service.QuestionBankService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bank")
public class BankApiController {

    private final QuestionBankService questionBankService;

    public BankApiController(QuestionBankService questionBankService) {
        this.questionBankService = questionBankService;
    }

    @PostMapping("/switch")
    public ResponseEntity<Void> switchBank(@RequestParam String name) {
        questionBankService.switchBank(name);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/import")
    public ResponseEntity<Void> importBank(@RequestParam String name, @RequestParam String content) {
        questionBankService.importBank(name, content);
        return ResponseEntity.ok().build();
    }
}
