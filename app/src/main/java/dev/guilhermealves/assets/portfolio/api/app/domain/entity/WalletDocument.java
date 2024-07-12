package dev.guilhermealves.assets.portfolio.api.app.domain.entity;

import com.google.cloud.firestore.DocumentReference;
import com.google.firebase.database.Exclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WalletDocument {

    private String id;
    private String name;

    @Exclude
    private String userId;
    private DocumentReference user;

    private String icon;
    private String color;
}
