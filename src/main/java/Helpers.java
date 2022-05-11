import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.file.FileSystemDirectory;
import com.drew.metadata.mp4.Mp4Directory;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.Date;
import java.util.logging.Logger;

public class Helpers {
    private static Logger logger = Logger.getLogger(Helpers.class.toString());
    public static Date getFileCreationDate(File file) {
        Metadata metadata = null;
        try {
            metadata = ImageMetadataReader.readMetadata(file);
        } catch (ImageProcessingException e) {
            throw new RuntimeException(e + " - " + file.getName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String extension = FilenameUtils.getExtension(file.getName());
        Date creationDate = null;
        boolean hasDateFromMetadata = false;

        switch (extension.toUpperCase()) {

            case "JPG", "JPEG":
                for (Directory directory : metadata.getDirectories()) {
                    for (Tag tag : directory.getTags()) {
                        if ((tag.getTagType() == 36867 || tag.getTagType() == 36868) && creationDate == null) {
                            try {
                                creationDate = directory.getDate(tag.getTagType());
                                hasDateFromMetadata = true;
                            } catch (Exception e) {
                                logger.warning("Could not get creation date from metadata: " + file.getName());
                            }
                        }
                    }
                }
                break;

            case "PNG":
                FileSystemDirectory fileSystemDirectory = metadata.getFirstDirectoryOfType(FileSystemDirectory.class);
                creationDate = fileSystemDirectory.getDate(FileSystemDirectory.TAG_FILE_MODIFIED_DATE);
                hasDateFromMetadata = true;
                break;

            case "MP4":
                for (Directory directory : metadata.getDirectories()) {
                    for (Tag tag : directory.getTags()) {
                        if (tag.getTagType() == 101) {
                            try {
                                creationDate = directory.getDate(tag.getTagType());
                                hasDateFromMetadata = true;
                            } catch (Exception e) {
                                logger.warning("Could not get creation date from metadata: " + file.getName());
                            }
                        }
                    }
                }

            case "MOV":
                for (Directory directory : metadata.getDirectories()) {
                    for (Tag tag : directory.getTags()) {
                        if (tag.getTagType() == 256) {
                            try {
                                creationDate = directory.getDate(tag.getTagType());
                                hasDateFromMetadata = true;
                            } catch (Exception e) {
                                logger.warning("Could not get creation date from metadata: " + file.getName());
                            }
                        }
                    }
                }
                break;

            case "":
                for (Directory directories : metadata.getDirectories()) {
                    for (Tag tag : directories.getTags()) {
                        System.out.println(tag + " - " + tag.getTagType());
                    }
                }
                System.exit(0);
                break;
        }

        if(!hasDateFromMetadata) {
            FileTime fileTime = null;
            try {
                fileTime = Files.getLastModifiedTime(file.toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            creationDate = new Date(fileTime.toMillis());
        }
        return creationDate;
    }
}