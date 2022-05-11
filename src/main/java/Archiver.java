import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.logging.Logger;

public class Archiver {

    public static void main(String[] args) {
        Logger logger = Logger.getLogger(Archiver.class.toString());

        if (args.length != 2) {
            logger.warning("2 paths must be defined! Exiting...");
            System.exit(-1);
        }

        Path sourcePath = null;
        Path destinationPath = null;

        if (Files.exists(Paths.get(args[0]))) {
            sourcePath = Paths.get(args[0]);
        } else {
            logger.warning("Error getting source path!");
            System.exit(-2);
        }

        if (Files.exists(Paths.get(args[1]))) {
            destinationPath = Paths.get(args[1]);
        } else {
            logger.warning("Error getting destination path!");
            System.exit(-3);
        }

        // File filter
        FilenameFilter fileFilter = (dir, name) -> name.endsWith(".jpg")
                || name.endsWith(".png")
                || name.endsWith(".jpeg")
                || name.endsWith(".bmp")
                || name.endsWith(".avi")
                || name.endsWith(".mp4")
                || name.endsWith(".mpg")
                || name.endsWith(".mpeg")
                || name.endsWith(".mov")
                || name.endsWith(".webm")
                || name.endsWith(".3gp");
        String[] extensionFilter = {
                "avi", "jpg", "jpeg", "png", "mov", "mp4", "bmp",
                "AVI", "JPG", "JPEG", "PNG", "MOV", "MP4", "BMP"
        };
        //File[] fileList = sourcePath.toFile().listFiles(fileFilter);
        Collection<File> fileList = FileUtils.listFiles(sourcePath.toFile(), extensionFilter, true);

        // System.out.println(fileList);
        if (fileList != null) {
            Integer progress = 0;
            String progressText = "";

            for (File file : fileList) {
                progress += 1;
                progressText = "[" + progress + "/" + fileList.size() + "] ";

                // Get file creation date
                Date creationDate = Helpers.getFileCreationDate(file);
                if (creationDate == null) {
                    logger.warning("Can´t get creation date: " + file.getName());
                    System.exit(-4);
                }

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(creationDate);
                Integer photoYear = calendar.get(Calendar.YEAR);
                Integer photoMonth = calendar.get(Calendar.MONTH) + 1;
                //System.out.println(photoYear + "/" + photoMonth);

                // Make path in destination dir
                Path archiveDir = Paths.get(destinationPath + "/" + photoYear + "/" + photoMonth);
                if (!Files.exists(archiveDir)) {
                    try {
                        Files.createDirectories(archiveDir);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                // Copy photo to folder
                String archiveFilename = file.getName();
                Integer i = 0;
                Boolean existsFileWithSameContent = false;
                while (Files.exists(Paths.get(archiveDir + "/" + archiveFilename))) {
                    try {
                        if (FileUtils.contentEquals(file, Paths.get(archiveDir + "/" + archiveFilename).toFile())) {
                            existsFileWithSameContent = true;
                            break;
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    i += 1;
                    archiveFilename = FilenameUtils.removeExtension(file.getName())
                            + "_" + i + "." + FilenameUtils.getExtension(file.getName());
                }

                if (!existsFileWithSameContent) {
                    Path destinationFilePath = Paths.get(archiveDir + "/" + archiveFilename);
                    try {
                        Files.copy(file.toPath(), destinationFilePath, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    logger.info(progressText + file.toPath() + " -> " + destinationFilePath);
                } else {
                    logger.info(progressText + "File already exists: " + file.toPath());
                }
            }
        }
    }
}

