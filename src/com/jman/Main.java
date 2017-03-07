package com.jman;

import com.beust.jcommander.JCommander;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.FileContent;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisher.Edits;
import com.google.api.services.androidpublisher.AndroidPublisher.Edits.Apks.Upload;
import com.google.api.services.androidpublisher.AndroidPublisher.Edits.Insert;
import com.google.api.services.androidpublisher.AndroidPublisher.Edits.Tracks.Update;
import com.google.api.services.androidpublisher.model.Apk;
import com.google.api.services.androidpublisher.model.AppEdit;
import com.google.api.services.androidpublisher.model.Track;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.beust.jcommander.Parameter;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final Log log = LogFactory.getLog(Main.class);

    private static final String TRACK_ALPHA = "alpha";

    @Parameter(names={"--appname", "-a"}, required=true)
    String ApplicationName;

    @Parameter(names={"--package", "-p"}, required=true)
    String PackageName;

    @Parameter(names={"--credentials", "-c"}, required=true)
    String CredentialsFile;

    @Parameter(names={"--apk", "-f"}, required=true)
    List<String> apkFiles;

    public void run () {

        try {
            AndroidPublisher service = AndroidPublisherHelper.init(
                    ApplicationName, CredentialsFile);

            final Edits edits = service.edits();

            // Create a new edit to make changes to your listing.
            Insert editRequest = edits
                    .insert(PackageName, null /** no content */);
            AppEdit edit = editRequest.execute();
            final String editId = edit.getId();
            log.info(String.format("Created edit with id: %s", editId));

            // Upload new apk to developer console

            List<Integer> apkVersionCodes = new ArrayList<Integer>();

            for (String apkFilename : apkFiles) {
                AbstractInputStreamContent apkContent =  new FileContent(
                        AndroidPublisherHelper.MIME_TYPE_APK,
                        new File(apkFilename));

                log.info("Uploading: " + apkFilename);
                Apk apk = edits.apks()
                            .upload(PackageName, editId, apkContent)
                            .execute();

                apkVersionCodes.add(apk.getVersionCode());

                log.info(String.format("Version code %d has been uploaded",
                        apk.getVersionCode()));
            }

            Update updateTrackRequest = edits
                    .tracks()
                    .update(PackageName,
                            editId,
                            TRACK_ALPHA,
                            new Track().setVersionCodes(apkVersionCodes));
            Track updatedTrack = updateTrackRequest.execute();
            log.info(String.format("Track %s has been updated.", updatedTrack.getTrack()));

            // Commit changes for edit.
            Edits.Commit commitRequest = edits.commit(PackageName, editId);
            AppEdit appEdit = commitRequest.execute();

            log.info(String.format("App edit with id %s has been comitted", appEdit.getId()));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Main main = new Main();
        new JCommander(main, args);
        main.run();
    }
}
