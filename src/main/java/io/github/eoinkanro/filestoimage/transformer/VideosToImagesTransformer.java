package io.github.eoinkanro.filestoimage.transformer;

import io.github.eoinkanro.filestoimage.utils.CommandLineExecutor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

import static io.github.eoinkanro.filestoimage.conf.InputCLIArguments.VIDEOS_TO_IMAGES;
import static io.github.eoinkanro.filestoimage.conf.OutputCLIArguments.*;

@Component
@Log4j2
public class VideosToImagesTransformer extends Transformer {

    @Autowired
    private CommandLineExecutor commandLineExecutor;

    @Override
    public void transform() {
        if (Boolean.FALSE.equals(inputCLIArgumentsHolder.getArgument(VIDEOS_TO_IMAGES))) {
            return;
        }

        //TODO get path from CLI
        File file = new File(fileUtils.getResultPathForVideos());
        if (!file.exists()) {
            return;
        }

        if (file.isDirectory()) {
            processFolder(file.listFiles());
        } else {
            processFile(file);
        }
    }

    private void processFolder(File[] files) {
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                processFolder(file.listFiles());
            } else {
                processFile(file);
            }
        }
    }

    private void processFile(File file) {
        try {
            log.info("Processing {}...", file);
            String imagesPattern = fileUtils.getResultFilePatternForVideosToImages(file, fileUtils.getResultPathForVideos());
            boolean isWritten = commandLineExecutor.execute(
                    FFMPEG.getValue(),
                    DEFAULT_YES.getValue(),
                    INPUT.getValue(),
                    BRACKETS_PATTERN.formatValue(file.getAbsolutePath()),
                    BRACKETS_PATTERN.formatValue(imagesPattern),
                    HIDE_BANNER.getValue()
            );

            if (!isWritten) {
                log.error("Error while writing {}", imagesPattern);
            }
        } catch (Exception e) {
            throw new TransformException(COMMON_EXCEPTION_DESCRIPTION, e);
        }
    }
}
