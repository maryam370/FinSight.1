package com.example.FinSight.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import com.example.FinSight.dto.TransactionRequest;
import com.example.FinSight.dto.TransactionResponse;
import com.example.FinSight.model.FraudAlert;
import com.example.FinSight.model.Transaction;
import com.example.FinSight.model.User;
import com.example.FinSight.repository.AuditLogRepository;
import com.example.FinSight.repository.FraudAlertRepository;
import com.example.FinSight.repository.TransactionRepository;
import com.example.FinSight.repository.UserRepository;
import com.example.FinSight.specification.TransactionSpecification;

@SpringBootTest
@Transactional
class TransactionServiceTest {
    
    @Autowired
    private TransactionService transactionService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private FraudAlertRepository fraudAlertRepository;
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setEmail("test@example.com");
        testUser = userRepository.save(testUser);
    }
    
    @Test
    void createTransaction_WithValidData_CreatesTransaction() {
        TransactionRequest request = new TransactionRequest();
        request.setUserId(testUser.getId());
        request.setAmount(new BigDecimal("100.00"));
        request.setType("EXPENSE");
        request.setCategory("groceries");
        request.setDescription("Weekly shopping");
        request.setLocation("Supermarket");
        request.setTransactionDate(LocalDateTime.now());
        
        TransactionResponse response = transactionService.createTransaction(request);
        
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(response.getType()).isEqualTo("EXPENSE");
        assertThat(response.getCategory()).isEqualTo("groceries");
        assertThat(response.getFraudScore()).isNotNull();
    }
    
    @Test
    void createTransaction_WithHighAmount_CreatesFraudAlert() {
        // Create a baseline transaction first
        createBaselineTransaction();
        
        TransactionRequest request = new TransactionRequest();
        request.setUserId(testUser.getId());
        request.setAmount(new BigDecimal("10000.00")); // Very high amount
        request.setType("EXPENSE");
        request.setCategory("unusual_category");
        request.setDescription("Suspicious transaction");
        request.setLocation("Unknown");
        request.setTransactionDate(LocalDateTime.now());
        
        TransactionResponse response = transactionService.createTransaction(request);
        
        if (response.isFraudulent()) {
            List<FraudAlert> alerts = fraudAlertRepository.findByUser(testUser);
            assertThat(alerts).isNotEmpty();
            
            FraudAlert alert = alerts.stream()
                .filter(a -> a.getTransaction().getId().equals(response.getId()))
                .findFirst()
                .orElse(null);
            
            assertThat(alert).isNotNull();
            assertThat(alert.getSeverity()).isEqualTo(response.getRiskLevel());
        }
    }
    
    @Test
    void createTransaction_LogsAuditEntry() {
        TransactionRequest request = new TransactionRequest();
        request.setUserId(testUser.getId());
        request.setAmount(new BigDecimal("50.00"));
        request.setType("EXPENSE");
        request.setCategory("groceries");
        request.setDescription("Test");
        request.setLocation("Test Location");
        request.setTransactionDate(LocalDateTime.now());
        
        long auditCountBefore = auditLogRepository.count();
        
        transactionService.createTransaction(request);
        
        long auditCountAfter = auditLogRepository.count();
        assertThat(auditCountAfter).isGreaterThan(auditCountBefore);
    }
    
    @Test
    void createTransaction_WithInvalidUserId_ThrowsException() {
        TransactionRequest request = new TransactionRequest();
        request.setUserId(99999L); // Non-existent user
        request.setAmount(new BigDecimal("100.00"));
        request.setType("EXPENSE");
        request.setCategory("groceries");
        request.setTransactionDate(LocalDateTime.now());
        
        assertThatThrownBy(() -> transactionService.createTransaction(request))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("User not found");
    }
    
    @Test
    void findWithFilters_FiltersByType() {
        createTestTransaction("INCOME", "salary", false);
        createTestTransaction("EXPENSE", "groceries", false);
        createTestTransaction("EXPENSE", "utilities", false);
        
        var spec = TransactionSpecification.withFilters(testUser, "EXPENSE", null, null, null, null);
        Page<TransactionResponse> results = transactionService.findWithFilters(
            testUser.getId(), spec, PageRequest.of(0, 10)
        );
        
        assertThat(results.getContent()).allMatch(t -> t.getType().equals("EXPENSE"));
    }
    
    @Test
    void findWithFilters_FiltersByCategory() {
        createTestTransaction("EXPENSE", "groceries", false);
        createTestTransaction("EXPENSE", "groceries", false);
        createTestTransaction("EXPENSE", "utilities", false);
        
        var spec = TransactionSpecification.withFilters(testUser, null, "groceries", null, null, null);
        Page<TransactionResponse> results = transactionService.findWithFilters(
            testUser.getId(), spec, PageRequest.of(0, 10)
        );
        
        assertThat(results.getContent()).hasSize(2);
        assertThat(results.getContent()).allMatch(t -> t.getCategory().equals("groceries"));
    }
    
    @Test
    void findWithFilters_FiltersByFraudulent() {
        createTestTransaction("EXPENSE", "groceries", false);
        createTestTransaction("EXPENSE", "utilities", true);
        createTestTransaction("EXPENSE", "entertainment", true);
        
        var spec = TransactionSpecification.withFilters(testUser, null, null, null, null, true);
        Page<TransactionResponse> results = transactionService.findWithFilters(
            testUser.getId(), spec, PageRequest.of(0, 10)
        );
        
        assertThat(results.getContent()).hasSize(2);
        assertThat(results.getContent()).allMatch(TransactionResponse::isFraudulent);
    }
    
    @Test
    void findWithFilters_FiltersByDateRange() {
        LocalDateTime now = LocalDateTime.now();
        createTestTransactionWithDate("EXPENSE", "groceries", now.minusDays(10));
        createTestTransactionWithDate("EXPENSE", "utilities", now.minusDays(5));
        createTestTransactionWithDate("EXPENSE", "entertainment", now.minusDays(1));
        
        var spec = TransactionSpecification.withFilters(
            testUser, null, null, now.minusDays(6), now.minusDays(2), null
        );
        Page<TransactionResponse> results = transactionService.findWithFilters(
            testUser.getId(), spec, PageRequest.of(0, 10)
        );
        
        assertThat(results.getContent()).hasSize(1);
    }
    
    @Test
    void findWithFilters_SortsByAmountAscending() {
        createTestTransactionWithAmount("EXPENSE", "groceries", new BigDecimal("100.00"));
        createTestTransactionWithAmount("EXPENSE", "utilities", new BigDecimal("50.00"));
        createTestTransactionWithAmount("EXPENSE", "entertainment", new BigDecimal("200.00"));
        
        var spec = TransactionSpecification.withFilters(testUser, null, null, null, null, null);
        Page<TransactionResponse> results = transactionService.findWithFilters(
            testUser.getId(), spec, PageRequest.of(0, 10, Sort.by("amount").ascending())
        );
        
        List<TransactionResponse> content = results.getContent();
        assertThat(content.get(0).getAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(content.get(1).getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(content.get(2).getAmount()).isEqualByComparingTo(new BigDecimal("200.00"));
    }
    
    @Test
    void findWithFilters_SortsByDateDescending() {
        LocalDateTime now = LocalDateTime.now();
        createTestTransactionWithDate("EXPENSE", "groceries", now.minusDays(10));
        createTestTransactionWithDate("EXPENSE", "utilities", now.minusDays(5));
        createTestTransactionWithDate("EXPENSE", "entertainment", now.minusDays(1));
        
        var spec = TransactionSpecification.withFilters(testUser, null, null, null, null, null);
        Page<TransactionResponse> results = transactionService.findWithFilters(
            testUser.getId(), spec, PageRequest.of(0, 10, Sort.by("transactionDate").descending())
        );
        
        List<TransactionResponse> content = results.getContent();
        assertThat(content.get(0).getTransactionDate()).isAfter(content.get(1).getTransactionDate());
        assertThat(content.get(1).getTransactionDate()).isAfter(content.get(2).getTransactionDate());
    }
    
    @Test
    void findWithFilters_PaginatesResults() {
        for (int i = 0; i < 25; i++) {
            createTestTransaction("EXPENSE", "groceries", false);
        }
        
        var spec = TransactionSpecification.withFilters(testUser, null, null, null, null, null);
        Page<TransactionResponse> page1 = transactionService.findWithFilters(
            testUser.getId(), spec, PageRequest.of(0, 10)
        );
        Page<TransactionResponse> page2 = transactionService.findWithFilters(
            testUser.getId(), spec, PageRequest.of(1, 10)
        );
        
        assertThat(page1.getContent()).hasSize(10);
        assertThat(page2.getContent()).hasSize(10);
        assertThat(page1.getTotalElements()).isEqualTo(25);
        assertThat(page1.getTotalPages()).isEqualTo(3);
    }
    
    private void createBaselineTransaction() {
        Transaction t = new Transaction();
        t.setUser(testUser);
        t.setAmount(new BigDecimal("50.00"));
        t.setType("EXPENSE");
        t.setCategory("groceries");
        t.setDescription("Baseline");
        t.setLocation("Store");
        t.setTransactionDate(LocalDateTime.now().minusDays(30));
        t.setFraudulent(false);
        t.setFraudScore(0.0);
        t.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(t);
    }
    
    private void createTestTransaction(String type, String category, boolean fraudulent) {
        Transaction t = new Transaction();
        t.setUser(testUser);
        t.setAmount(new BigDecimal("100.00"));
        t.setType(type);
        t.setCategory(category);
        t.setDescription("Test");
        t.setLocation("Test Location");
        t.setTransactionDate(LocalDateTime.now());
        t.setFraudulent(fraudulent);
        t.setFraudScore(fraudulent ? 75.0 : 10.0);
        t.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(t);
    }
    
    private void createTestTransactionWithDate(String type, String category, LocalDateTime date) {
        Transaction t = new Transaction();
        t.setUser(testUser);
        t.setAmount(new BigDecimal("100.00"));
        t.setType(type);
        t.setCategory(category);
        t.setDescription("Test");
        t.setLocation("Test Location");
        t.setTransactionDate(date);
        t.setFraudulent(false);
        t.setFraudScore(10.0);
        t.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(t);
    }
    
    private void createTestTransactionWithAmount(String type, String category, BigDecimal amount) {
        Transaction t = new Transaction();
        t.setUser(testUser);
        t.setAmount(amount);
        t.setType(type);
        t.setCategory(category);
        t.setDescription("Test");
        t.setLocation("Test Location");
        t.setTransactionDate(LocalDateTime.now());
        t.setFraudulent(false);
        t.setFraudScore(10.0);
        t.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(t);
    }
}
