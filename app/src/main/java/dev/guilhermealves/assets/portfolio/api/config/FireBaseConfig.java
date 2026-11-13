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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

@Slf4j
@Configuration
public class FireBaseConfig {

    @PostConstruct
    public void initialization() {
        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }

        try {
            InputStream serviceAccount = null;

            // 1. Variável de ambiente (FIREBASE_KEY_PATH ou GOOGLE_APPLICATION_CREDENTIALS)
            String envPath = System.getenv("FIREBASE_KEY_PATH");
            if (envPath == null || envPath.isBlank()) {
                envPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
            }
            if (envPath != null && !envPath.isBlank()) {
                File file = new File(envPath);
                if (file.exists()) {
                    serviceAccount = new FileInputStream(file);
                    log.info("Firebase inicializado a partir da variável de ambiente: {}", envPath);
                }
            }

            // 2. Classpath (serviceAccountKey.json empacotado no JAR ou pasta target)
            if (serviceAccount == null) {
                serviceAccount = getClass().getClassLoader().getResourceAsStream("serviceAccountKey.json");
                if (serviceAccount != null) {
                    log.info("Firebase inicializado a partir do classpath (serviceAccountKey.json)");
                }
            }

            // 3. Fallbacks de caminhos locais de desenvolvimento
            if (serviceAccount == null) {
                String[] potentialPaths = {
                        "./src/main/resources/serviceAccountKey.json",
                        "./app/src/main/resources/serviceAccountKey.json",
                        "./serviceAccountKey.json"
                };
                for (String path : potentialPaths) {
                    File file = new File(path);
                    if (file.exists()) {
                        serviceAccount = new FileInputStream(file);
                        log.info("Firebase inicializado a partir do caminho local: {}", path);
                        break;
                    }
                }
            }

            if (serviceAccount == null) {
                throw new IllegalStateException("serviceAccountKey.json não encontrado no classpath, nas variáveis de ambiente ou nos caminhos locais");
            }

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
            log.info("FirebaseApp inicializado com sucesso.");

        } catch (Exception e) {
            log.error("Error opening connection to firebase - {}", e.getMessage());
        }
    }

    @Bean
    public Firestore getFirestore(){
        return FirestoreClient.getFirestore();
    }
}
