package dev.guilhermealves.assets.portfolio.api.app.domain.entity;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatchlistDocument {

    @DocumentId
    private String id;
    private String userId;
    private String assetId;
    private String assetSymbol;
    private Timestamp createdAt;
}
