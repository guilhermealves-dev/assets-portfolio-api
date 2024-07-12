package dev.guilhermealves.assets.portfolio.api.app.domain.model;

import com.google.cloud.Timestamp;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class Transaction {

    private String id;
    private String type;
    private Asset asset;
    private Wallet wallet;
    private BigDecimal price;
    private BigDecimal amount;
    private BigDecimal quantity;
    private BigDecimal fee;
    private LocalDateTime datetime;
}
