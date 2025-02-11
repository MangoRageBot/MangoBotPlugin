package org.mangorage.mangobot.website.servlet.file;

import java.util.HashMap;

public record UploadConfig
        (
                HashMap<String, TargetFile> targets
        )
{}
