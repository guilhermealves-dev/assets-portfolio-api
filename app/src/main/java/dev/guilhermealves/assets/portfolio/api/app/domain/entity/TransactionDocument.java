package dev.guilhermealves.assets.portfolio.api.app.domain.entity;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

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
    private Timestamp datetime;
}
