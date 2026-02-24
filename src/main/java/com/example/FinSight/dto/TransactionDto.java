    package com.example.FinSight.dto;

    import java.math.BigDecimal;
    import java.time.LocalDateTime;

    import lombok.Data;

    @Data
    public class TransactionDto {
        private Long id;
        private Long userId;
        private BigDecimal amount;
        private String type;
        private String category;
        private String description;
        private String location;
        private LocalDateTime transactionDate;
        private boolean fraudulent;
        private Double fraudScore;
    }


    @Data
    class TransactionResponse {
        private Long id;
        private BigDecimal amount;
        private String type;
        private String category;
        private String description;
        private String location;
        private LocalDateTime transactionDate;
        private boolean fraudulent;
        private Double fraudScore;
        private String status;
    }
