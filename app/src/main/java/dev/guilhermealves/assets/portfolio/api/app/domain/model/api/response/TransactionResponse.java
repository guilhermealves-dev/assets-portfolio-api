package dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionResponse {

    private String id;
    private String type;
    private AssetResponse asset;
    private WalletResponse wallet;
    private BigDecimal price;
    private BigDecimal amount;
    private BigDecimal quantity;
    private BigDecimal fee;
    private LocalDateTime datetime;
}
