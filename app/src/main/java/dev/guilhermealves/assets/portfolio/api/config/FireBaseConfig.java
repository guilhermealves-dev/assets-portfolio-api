package dev.guilhermealves.assets.portfolio.api.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;

@Slf4j
@Configuration
public class FireBaseConfig {

    @PostConstruct
    public void initialization() {

        try {
            FileInputStream serviceAccount = new FileInputStream("./src/main/resources/serviceAccountKey.json");

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);

        } catch (Exception e) {
            log.error("Error opening connection to firebase - {}", e.getMessage());
        }
    }

    @Bean
    public Firestore getFirestore(){
        return FirestoreClient.getFirestore();
    }
}
