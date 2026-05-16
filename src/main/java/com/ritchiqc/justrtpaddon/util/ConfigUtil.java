package com.ritchiqc.justrtpaddon.util;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

public class ConfigUtil {

    private final File dataFolder;
    private final Class<?> clazz;
    private final Logger logger;

    public ConfigUtil(File dataFolder, Class<?> clazz, Logger logger) {
        this.dataFolder = dataFolder;
        this.clazz = clazz;
        this.logger = logger;
    }

    /**
     * Charge une ressource directement depuis le JAR de l'addon.
     * Cela évite les conflits avec le plugin principal JustRTP qui partage le même ClassLoader.
     */
    private InputStream getResourceFromAddonJar(String resourcePath) {
        try {
            URL jarUrl = clazz.getProtectionDomain().getCodeSource().getLocation();
            JarFile jarFile = new JarFile(new File(jarUrl.toURI()));
            JarEntry entry = jarFile.getJarEntry(resourcePath);
            if (entry != null) {
                // Wrapper qui ferme aussi le JarFile quand le stream est fermé
                return new FilterInputStream(jarFile.getInputStream(entry)) {
                    @Override
                    public void close() throws IOException {
                        super.close();
                        jarFile.close();
                    }
                };
            }
            jarFile.close();
        } catch (Exception e) {
            logger.warning("Could not load resource from addon JAR: " + resourcePath + " - " + e.getMessage());
        }
        return null;
    }

    /**
     * Copie une ressource du JAR de l'addon vers le dossier de données
     * si le fichier n'existe pas encore.
     */
    public void saveDefaultResource(String resourcePath) {
        if (resourcePath == null || resourcePath.isEmpty()) {
            throw new IllegalArgumentException("Resource path cannot be null or empty");
        }

        File outFile = new File(dataFolder, resourcePath);
        if (outFile.exists()) {
            return;
        }

        InputStream in = getResourceFromAddonJar(resourcePath);
        if (in == null) {
            logger.warning("Could not find default resource: " + resourcePath);
            return;
        }

        try {
            File parent = outFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            Files.copy(in, outFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            in.close();
        } catch (IOException e) {
            logger.warning("Could not save default resource: " + resourcePath + " - " + e.getMessage());
        }
    }

    /**
     * Charge un fichier YAML depuis le dossier de données.
     * Si le fichier n'existe pas, retourne une configuration vide.
     */
    public FileConfiguration loadYamlConfig(String resourcePath) {
        File file = new File(dataFolder, resourcePath);
        if (!file.exists()) {
            saveDefaultResource(resourcePath);
        }
        return YamlConfiguration.loadConfiguration(file);
    }
}
