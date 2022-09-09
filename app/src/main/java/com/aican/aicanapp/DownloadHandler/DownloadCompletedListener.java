package com.aican.aicanapp.DownloadHandler;

import java.io.File;

public interface DownloadCompletedListener {
    boolean onComplete(File file);

}
