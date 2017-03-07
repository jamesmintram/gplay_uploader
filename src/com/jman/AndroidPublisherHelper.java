package com.jman;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisherScopes;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;


public class AndroidPublisherHelper {

    private static final Log log = LogFactory.getLog(AndroidPublisherHelper.class);

    static final String MIME_TYPE_APK = "application/vnd.android.package-archive";

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT;

    private static Credential authorizeWithServiceAccountFile(InputStream serviceAccountStream)
            throws GeneralSecurityException, IOException {

        GoogleCredential credential = GoogleCredential
                .fromStream(serviceAccountStream)
                .createScoped(Collections.singleton(AndroidPublisherScopes.ANDROIDPUBLISHER));

        return credential;
    }

    protected static AndroidPublisher init(String applicationName,
                                           String credentialsFile)
            throws IOException, GeneralSecurityException {

        Preconditions.checkArgument(!Strings.isNullOrEmpty(applicationName),
                "applicationName cannot be null or empty!");

        Preconditions.checkArgument(!Strings.isNullOrEmpty(credentialsFile),
                "credentialsFile cannot be null or empty!");

        // Authorization.
        newTrustedTransport();
        Credential credential;

        credential = authorizeWithServiceAccountFile(new FileInputStream(credentialsFile));

        // Set up and return API client.
        return new AndroidPublisher.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(applicationName)
                .build();
    }

    private static void newTrustedTransport() throws GeneralSecurityException,
            IOException {
        if (null == HTTP_TRANSPORT) {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        }
    }

}
