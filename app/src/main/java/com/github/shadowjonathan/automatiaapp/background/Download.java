package com.github.shadowjonathan.automatiaapp.background;

import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.channels.FileChannel;

public class Download {
    public static String DOWNLOAD_COMPLETE = "Download.complete";
    public static String DOWNLOAD_COMPLETE_EXTRA_ID = "Download.ID";
    public static String DOWNLOAD_COMPLETE_EXTRA_FILE = "Download.File";
    private static DownloadManager dm;
    private static Context context;
    private Uri link;
    private String filename;
    private String to;

    public Download(URL url) {
        register(url.toString());
    }

    public Download(URI uri) {
        register(uri.toString());
    }

    public Download(String url) {
        register(url);
    }

    public static void bindContext(Context context) {
        Download.context = context;
        dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
    }

    public static Download fromAutomatia(String appendpath) {
        return new Download(Comms.AUTOMATIA_URL.resolve(appendpath.replace("\\", "/").replace(" ", "%20")));
    }

    public static void move(Uri src, File dst, Context context) throws IOException {

        dst.getParentFile().mkdirs();
        ContentResolver cr = context.getContentResolver();
        InputStream istr = cr.openInputStream(src);
        OutputStream out = new FileOutputStream(dst);
        byte[] buffer = new byte[1024]; // Adjust if you want
        int bytesRead;
        while ((bytesRead = istr.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        istr.close();
        out.flush();
        out.close();
    }

    public static void copy(File src, File dst) throws IOException {
        FileChannel inChannel = null;
        FileChannel outChannel = null;

        try {
            inChannel = new FileInputStream(src).getChannel();
            outChannel = new FileOutputStream(dst).getChannel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
    }

    private void register(String pointer) {
        link = Uri.parse(pointer);
        filename = new File(link.toString()).getName();
    }

    private String to() {
        return to != null ? to : "files";
    }

    public Download setTo(String subfolder) {
        if (subfolder.endsWith("/"))
            to = subfolder;
        else
            to = subfolder + "/";
        return this;
    }

    public long start() {
        return dm.enqueue(
                new DownloadManager.Request(link)
                        .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
                        .setDestinationInExternalFilesDir(context, null, "files/" + to() + filename)
        );
    }
}
