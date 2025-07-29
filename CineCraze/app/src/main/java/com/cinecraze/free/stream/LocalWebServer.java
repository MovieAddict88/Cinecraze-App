package com.cinecraze.free.stream;

import android.content.Context;
import fi.iki.elonen.NanoHTTPD;
import java.io.IOException;
import java.io.InputStream;

public class LocalWebServer extends NanoHTTPD {
    private Context context;

    public LocalWebServer(Context context, int port) {
        super(port);
        this.context = context;
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        if (uri.equals("/shaka_player.html")) {
            try {
                InputStream is = context.getAssets().open("shaka_player.html");
                return newFixedLengthResponse(Response.Status.OK, "text/html", is, is.available());
            } catch (IOException e) {
                return newFixedLengthResponse(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Not Found");
            }
        }
        return newFixedLengthResponse(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Not Found");
    }
}