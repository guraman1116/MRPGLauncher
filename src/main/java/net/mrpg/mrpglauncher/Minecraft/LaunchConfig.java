package net.mrpg.mrpglauncher.Minecraft;

public class LaunchConfig {
    private String name;
    private String episode;
    private String minecraftVersion;
    private String description;
    private String repository;
    private String mcVersion;
    private String loaderType;
    private String loaderVersion;
    private String gameDir;

    public String getMcVersion() {
        return mcVersion;
    }

    public String getLoaderType() {
        return loaderType;
    }

    public String getLoaderVersion() {
        return loaderVersion;
    }

    public String getGameDir() {
        return gameDir;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEpisode() {
        return episode;
    }

    public void setEpisode(String episode) {
        this.episode = episode;
    }

    public String getMinecraftVersion() {
        return minecraftVersion;
    }

    public void setMinecraftVersion(String minecraftVersion) {
        this.minecraftVersion = minecraftVersion;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }
}
