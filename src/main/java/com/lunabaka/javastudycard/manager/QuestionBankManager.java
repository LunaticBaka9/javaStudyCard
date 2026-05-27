package com.lunabaka.javastudycard.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lunabaka.javastudycard.model.QuestionBank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Component
public class QuestionBankManager {

    private static final String DEFAULT_BANK_NAME = "card";
    private static final String CONFIG_FILE_NAME = "banks.json";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${banks.dir:#{systemProperties['user.dir']}/banks}")
    private String banksDirPath;

    private Path banksDir;

    @PostConstruct
    public void init() {
        banksDir = Paths.get(banksDirPath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(banksDir);
            System.out.println("[QuestionBankManager] Banks directory: " + banksDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to init bank manager", e);
        }
    }

    public QuestionBank loadBank(String name) {
        try {
            Path bankFile = banksDir.resolve(name + ".json");
            if (!Files.exists(bankFile)) {
                QuestionBank emptyBank = new QuestionBank(new ArrayList<>());
                objectMapper.writeValue(bankFile.toFile(), emptyBank);
                return emptyBank;
            }
            return objectMapper.readValue(bankFile.toFile(), QuestionBank.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load bank: " + name, e);
        }
    }

    public List<String> getAvailableBanks() {
        List<String> banks = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(banksDir, "*.json")) {
            for (Path entry : stream) {
                String fileName = entry.getFileName().toString();
                if (!CONFIG_FILE_NAME.equals(fileName)) {
                    banks.add(fileName.replace(".json", ""));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to list banks", e);
        }
        return banks;
    }

    public String getDefaultBankName() {
        List<String> banks = getAvailableBanks();
        return banks.isEmpty() ? DEFAULT_BANK_NAME : banks.get(0);
    }

    public boolean bankExists(String name) {
        return Files.exists(banksDir.resolve(name + ".json"));
    }

    public void importBank(String name, String jsonContent) {
        try {
            QuestionBank bank = objectMapper.readValue(jsonContent, QuestionBank.class);
            Path bankFile = banksDir.resolve(name + ".json");
            objectMapper.writeValue(bankFile.toFile(), bank);
        } catch (IOException e) {
            throw new RuntimeException("Invalid question bank format", e);
        }
    }

    public String getBankJson(String name) {
        try {
            Path bankFile = banksDir.resolve(name + ".json");
            if (!Files.exists(bankFile)) {
                return objectMapper.writeValueAsString(new QuestionBank(new ArrayList<>()));
            }
            return Files.readString(bankFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read bank: " + name, e);
        }
    }
}
