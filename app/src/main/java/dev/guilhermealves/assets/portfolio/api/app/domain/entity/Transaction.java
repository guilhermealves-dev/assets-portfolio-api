package dev.guilhermealves.assets.portfolio.api.app.domain.entity;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

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
    private Long datetime;
}
