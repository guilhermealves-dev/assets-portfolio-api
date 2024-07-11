package dev.guilhermealves.assets.portfolio.api.app.domain.entity;

import java.math.BigDecimal;

import com.google.cloud.firestore.DocumentReference;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionDocument {

    private String id;
    private String type;
    private DocumentReference asset;
    private DocumentReference wallet;
    private BigDecimal price;
    private BigDecimal amount;
    private BigDecimal quantity;
    private BigDecimal fee;
    private Long datetime;
}
