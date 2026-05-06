package com.lunabaka.javastudycard.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lunabaka.javastudycard.model.QuestionBank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class QuestionBankManager {

    private static final String DEFAULT_BANK_NAME = "card";
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${banks.dir:#{systemProperties['user.dir']}/banks}")
    private String banksDirPath;
    
    private Path banksDir;
    private Path banksConfigPath;
    
    private Map<String, QuestionBank> loadedBanks = new ConcurrentHashMap<>();
    private String currentBankName = DEFAULT_BANK_NAME;

    @PostConstruct
    public void init() {
        // Resolve path - works on both Windows and Linux
        banksDir = Paths.get(banksDirPath).toAbsolutePath().normalize();
        banksConfigPath = banksDir.resolve("banks.json");
        
        try {
            // Ensure banks directory exists
            Files.createDirectories(banksDir);
            
            // Load current bank preference
            if (Files.exists(banksConfigPath)) {
                Map<String, Object> config = objectMapper.readValue(banksConfigPath.toFile(), new TypeReference<Map<String, Object>>() {});
                if (config.containsKey("currentBank")) {
                    String savedBank = (String) config.get("currentBank");
                    if (getAvailableBanks().contains(savedBank)) {
                        currentBankName = savedBank;
                    }
                }
            } else {
                saveConfig();
            }
            
            System.out.println("[QuestionBankManager] Banks directory: " + banksDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to init bank manager", e);
        }
    }

    public void loadBank(String name) {
        if (loadedBanks.containsKey(name)) return;
        
        try {
            // Load from banks directory
            Path bankFile = banksDir.resolve(name + ".json");
            if (!Files.exists(bankFile)) {
                throw new RuntimeException("Bank file not found: " + bankFile);
            }
            QuestionBank bank = objectMapper.readValue(bankFile.toFile(), QuestionBank.class);
            loadedBanks.put(name, bank);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load bank: " + name, e);
        }
    }

    public QuestionBank getCurrentBank() {
        if (!loadedBanks.containsKey(currentBankName)) {
            loadBank(currentBankName);
        }
        return loadedBanks.get(currentBankName);
    }

    public String getCurrentBankName() {
        return currentBankName;
    }

    public void setCurrentBank(String name) {
        if (!getAvailableBanks().contains(name)) {
            throw new RuntimeException("Bank not available: " + name);
        }
        currentBankName = name;
        if (!loadedBanks.containsKey(name)) {
            loadBank(name);
        }
        saveConfig();
    }

    public List<String> getAvailableBanks() {
        List<String> banks = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(banksDir, "*.json")) {
            for (Path entry : stream) {
                String fileName = entry.getFileName().toString();
                if (!"banks.json".equals(fileName)) {
                    banks.add(fileName.replace(".json", ""));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to list banks", e);
        }
        return banks;
    }

    public void importBank(String name, String jsonContent) {
        try {
            // Validate JSON
            QuestionBank bank = objectMapper.readValue(jsonContent, QuestionBank.class);
            
            // Save to file
            Path bankFile = banksDir.resolve(name + ".json");
            objectMapper.writeValue(bankFile.toFile(), bank);
            
            // Load into memory
            loadedBanks.put(name, bank);
            
            // Set as current
            currentBankName = name;
            saveConfig();
        } catch (IOException e) {
            throw new RuntimeException("Invalid question bank format", e);
        }
    }

    private void saveConfig() {
        try {
            Map<String, String> config = new LinkedHashMap<>();
            config.put("currentBank", currentBankName);
            objectMapper.writeValue(banksConfigPath.toFile(), config);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save config", e);
        }
    }
}
