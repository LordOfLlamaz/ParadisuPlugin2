package net.paradisu.paradisuplugin.velocity.locale;

import com.google.common.collect.Maps;
import net.paradisu.paradisuplugin.velocity.Paradisu;
import net.paradisu.paradisuplugin.velocity.utils.FileUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.kyori.adventure.translation.Translator;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class TranslationManager {
    public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
    public static final List<Locale> BUNDLED_LOCALES = List.of(
        new Locale("fi", "FI")
        );

    private final Paradisu paradisu;
    private final Set<Locale> installed = ConcurrentHashMap.newKeySet();
    private final Path translationsDirectory;
    private TranslationRegistry registry;

    public TranslationManager(Paradisu paradisu) {
        this.paradisu = paradisu;
        this.translationsDirectory = this.paradisu.dataDirectory().resolve("translations");

        try {
            FileUtils.createDirectoryIfNotExists(this.translationsDirectory);
        } catch (IOException ignored) {
        }
    }

    public static boolean isTranslationFile(Path path) {
        return path.getFileName().toString().endsWith(".properties");
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean isAdventureDuplicatesException(Exception e) {
        return e instanceof IllegalArgumentException && (e.getMessage().startsWith("Invalid key") || e.getMessage().startsWith("Translation already exists"));
    }

    public static Locale parseLocale(String locale) {
        return locale == null ? null : Translator.parseLocale(locale);
    }

    public void reload() {
        // remove any previous registry
        if (this.registry != null) {
            GlobalTranslator.translator().removeSource(this.registry);
        }

        // create a translation registry
        this.registry = TranslationRegistry.create(Key.key("paradisu", "main"));
        this.registry.defaultLocale(DEFAULT_LOCALE);

        // load custom translations first, then the base (built-in) translations after.
        loadFromFileSystem(translationsDirectory, false);
        loadFromResourceBundle();

        // register it to the global source, so our translations can be picked up by adventure-platform
        GlobalTranslator.translator().addSource(this.registry);
    }

    /**
     * Loads the bundled translations from the jar file.
     */
    private void loadFromResourceBundle() {
        ResourceBundle defaultBundle = ResourceBundle.getBundle("messages", DEFAULT_LOCALE);
        try {
            this.registry.registerAll(DEFAULT_LOCALE, defaultBundle, false);
            BUNDLED_LOCALES.forEach(locale -> {
                ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);
                this.registry.registerAll(locale, bundle, false);
            });
        } catch (IllegalArgumentException e) {
            this.paradisu.logger().warn("Error loading default locale file", e);
        }
    }

    /**
     * Loads any custom translations from the plugin configuration folder.
     */
    public void loadFromFileSystem(Path directory, boolean suppressDuplicatesError) {
        List<Path> translationFiles;
        try (Stream<Path> stream = Files.list(directory)) {
            translationFiles = stream.filter(TranslationManager::isTranslationFile).collect(Collectors.toList());
        } catch (IOException e) {
            translationFiles = Collections.emptyList();
        }

        if (translationFiles.isEmpty()) {
            return;
        }

        Map<Locale, ResourceBundle> loaded = new HashMap<>();
        for (Path translationFile : translationFiles) {
            try {
                Map.Entry<Locale, ResourceBundle> result = loadTranslationFile(translationFile);
                loaded.put(result.getKey(), result.getValue());
            } catch (Exception e) {
                if (!suppressDuplicatesError || !isAdventureDuplicatesException(e)) {
                    this.paradisu.logger().warn("Error loading locale file: " + translationFile.getFileName(), e);
                }
            }
        }

        // try registering the locale without a country code - if we don't already have a registration for that
        loaded.forEach((locale, bundle) -> {
            Locale localeWithoutCountry = new Locale(locale.getLanguage());
            if (!locale.equals(localeWithoutCountry) && !localeWithoutCountry.equals(DEFAULT_LOCALE) && this.installed.add(localeWithoutCountry)) {
                try {
                    this.registry.registerAll(localeWithoutCountry, bundle, false);
                } catch (IllegalArgumentException e) {
                    // ignore
                }
            }
        });
    }

    private Map.Entry<Locale, ResourceBundle> loadTranslationFile(Path translationFile) throws IOException {
        String fileName = translationFile.getFileName().toString();
        String localeString = fileName.substring(0, fileName.length() - ".properties".length());
        Locale locale = parseLocale(localeString);

        if (locale == null) {
            throw new IllegalStateException("Unknown locale '" + localeString + "' - unable to register.");
        }

        PropertyResourceBundle bundle;
        try (BufferedReader reader = Files.newBufferedReader(translationFile, StandardCharsets.UTF_8)) {
            bundle = new PropertyResourceBundle(reader);
        }

        this.registry.registerAll(locale, bundle, false);
        this.installed.add(locale);
        return Maps.immutableEntry(locale, bundle);
    }
}
