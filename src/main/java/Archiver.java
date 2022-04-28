import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class Archiver {
    public static void main(String[] args) {
        Path sourcePath = Paths.get("source").toAbsolutePath();
        Path destinationPath = Paths.get("destination").toAbsolutePath();

        // Photo filter
        FilenameFilter photoFilter = (dir, name) -> name.endsWith(".jpg")
                || name.endsWith(".png")
                || name.endsWith(".jpeg")
                || name.endsWith(".bmp");

        // Video filter
        FilenameFilter videoFilter = (dir, name) -> name.endsWith(".avi")
                || name.endsWith(".mpg")
                || name.endsWith(".mpeg")
                || name.endsWith(".mov")
                || name.endsWith(".webm")
                || name.endsWith(".3gp");

        File[] listPhotos = sourcePath.toFile().listFiles(photoFilter);

        if (listPhotos != null) {
            for (File file : listPhotos) {
                Metadata metadata = null;
                try {
                    metadata = ImageMetadataReader.readMetadata(file);

                } catch (ImageProcessingException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                Date creationDate = null;
                ExifIFD0Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
                creationDate = directory.getDate(ExifIFD0Directory.TAG_DATETIME);

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(creationDate);
                Integer photoYear = calendar.get(Calendar.YEAR);
                Integer photoMonth = calendar.get(Calendar.MONTH) + 1;
                System.out.println(photoYear + "/" + photoMonth);
            }
        }
    }
}
