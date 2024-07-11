package dev.guilhermealves.assets.portfolio.api.app.domain.entity;

import com.google.cloud.firestore.DocumentReference;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WalletDocument {

    private String id;
    private String name;
    private DocumentReference user_id;
    private String icon;
    private String color;
}
