package com.bookmap.python.api.addon.services;

import java.io.File;
import java.io.IOException;

public interface ContentFileSaver {
    void save(File file) throws IOException;
}
