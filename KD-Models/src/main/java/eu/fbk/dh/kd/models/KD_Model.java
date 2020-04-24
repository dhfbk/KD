package eu.fbk.dh.kd.models;


import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * <p>KD_Model class.</p>
 *
 * @author Giovanni Moretti - DH Group FBK.
 * @version $Id: $Id
 */
public class KD_Model {


    private Path current_language_path = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(KD_Model.class);

    /**
     * This method checks if the path is a valid KD language folder
     *
     * @param path a {@link java.nio.file.Path} object.
     * @return a boolean.
     */
    public static boolean checkLanguageFolder(Path path) {
     //   LOGGER.info("Check if path " + path.toString() + " is a valid KD language folder...");
        //System.out.println("Check if path " + path.toString() + " is a valid KD language folder...");
        if (Files.exists(path)) {
            if (Files.isDirectory(path)) {
                if (Files.exists(Paths.get(path.toString(), ".valid_kd_languages_folder"))) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * This method returns the current model folder path
     *
     * @return current model language path
     */
    public String getCurrent_language_path() {
        return current_language_path.toString();
    }


    /**
     * The constructor initializes the model with the submitted path parameter.
     * If path not exists or if path is not a valid KD folder, the constructor tries to build a new one at the specified location.
     *
     * @param path a {@link java.nio.file.Path} object.
     */
    public KD_Model(Path path) {

        this.current_language_path = path;

        if (!checkLanguageFolder(path)) {
           // System.out.println("Attention -- language folder not exists... I try to rebuild a new one");
            LOGGER.warn("Attention -- language folder not exists... I try to rebuild a new one");
            try {
                Files.createDirectories(path);

                File marker_file = new File(path.toString() + File.separator + ".valid_kd_languages_folder");
                marker_file.createNewFile();


                Path new_lang_zip = Paths.get(path.toString(), "languages.zip");


                byte[] bytes = Resources.toByteArray(Resources.getResource("languages.zip"));

                Files.write(new_lang_zip, bytes);
                ZipFile zipFile = new ZipFile(new_lang_zip.toFile());
                Enumeration<?> enu = zipFile.entries();
                while (enu.hasMoreElements()) {
                    ZipEntry zipEntry = (ZipEntry) enu.nextElement();

                    String name = zipEntry.getName();
                    // long size = zipEntry.getSize();
                    // long compressedSize = zipEntry.getCompressedSize();
                    // System.out.printf("name: %-20s | size: %6d | compressed size: %6d\n", name, size, compressedSize);

                    File file = new File(path.toString() + File.separator + name);
                    if (name.endsWith("/")) {
                        file.mkdirs();
                        continue;
                    }

                    File parent = file.getParentFile();
                    if (parent != null) {
                        parent.mkdirs();
                    }

                    InputStream is = zipFile.getInputStream(zipEntry);
                    FileOutputStream fos = new FileOutputStream(file);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = is.read(buffer)) >= 0) {
                        fos.write(buffer, 0, length);
                    }
                    is.close();
                    fos.close();

                }
                zipFile.close();
                this.current_language_path = path;
            } catch (Exception e) {
                e.printStackTrace();
            }

        }else{
            LOGGER.info("KD configuration folder:"+ path);
        }
    }
}
