package com.lunabaka.javastudycard.controller;

import com.lunabaka.javastudycard.service.QuestionBankService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/bank")
public class BankApiController {

    private static final String SESSION_BANK_KEY = "currentBank";

    private final QuestionBankService questionBankService;

    public BankApiController(QuestionBankService questionBankService) {
        this.questionBankService = questionBankService;
    }

    @PostMapping("/switch")
    public ResponseEntity<Void> switchBank(HttpSession session, @RequestParam String name) {
        session.setAttribute(SESSION_BANK_KEY, name);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/import")
    public ResponseEntity<Void> importBank(HttpSession session, @RequestParam String name, @RequestParam String content) {
        questionBankService.importBank(name, content);
        session.setAttribute(SESSION_BANK_KEY, name);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/export")
    public void exportBank(HttpSession session, HttpServletResponse response) throws IOException {
        String bankName = (String) session.getAttribute(SESSION_BANK_KEY);
        if (bankName == null) {
            bankName = questionBankService.getDefaultBankName();
        }
        String json = questionBankService.getBankJson(bankName);
        response.setContentType("application/json");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + bankName + ".json\"");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    }
}
