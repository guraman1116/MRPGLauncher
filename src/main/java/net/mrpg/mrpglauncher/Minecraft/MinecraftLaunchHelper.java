package net.mrpg.mrpglauncher.Minecraft;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MinecraftLaunchHelper {

    public static Process launch(String installDir, LaunchConfig config, Auth auth) throws IOException {
        // 1. main.exeを一時ディレクトリにコピー
        Path executablePath = extractLauncher();

        // 2. ゲームディレクトリのパスを構築
        Path gameDir = Path.of(installDir, config.getEpisode());
        Files.createDirectories(gameDir); // ディレクトリが存在しない場合は作成

        // 3. 起動引数を構築
        List<String> command = new ArrayList<>();
        command.add(executablePath.toString());
        command.add("--mcversion");
        command.add(config.getMcVersion());
        command.add("--loader-type");
        command.add(config.getLoaderType());
        command.add("--loader-version");
        command.add(config.getLoaderVersion());
        command.add("--game-dir");
        command.add(gameDir.toString());

        command.add("--username");
        command.add(auth.getUsername());
        command.add("--token");
        command.add(auth.getToken());

        // 4. プロセスを開始
        System.out.println("Starting process with command: " + String.join(" ", command));
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        return processBuilder.start();
    }

    private static Path extractLauncher() throws IOException {
        String resourceName = "main.exe";
        try (InputStream in = MinecraftLaunchHelper.class.getResourceAsStream("/net/mrpg/mrpglauncher/" + resourceName)) {
            if (in == null) {
                throw new IOException("Cannot find " + resourceName + " in resources.");
            }
            //隣に置く
            Path executablePath = Path.of(System.getProperty("user.home"), ".mrpg-launcher", resourceName);
            if (Files.exists(executablePath)) {
                return executablePath;
            }
            Files.copy(in, executablePath, StandardCopyOption.REPLACE_EXISTING);
            executablePath.toFile().setExecutable(true);
            return executablePath;
        }
    }

    public static void handleStream(InputStream stream, Consumer<String> onLine) {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    onLine.accept(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}