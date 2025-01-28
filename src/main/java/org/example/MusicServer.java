package org.example;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MusicServer {

    private static final int PORT = 12345;
    private static final List<Song> songs = new ArrayList<>();

    public static void main(String[] args) {
        loadSongs();
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadSongs() {
        File folder = new File("/home/emil/Musik/");
        if (!folder.exists()){
            folder.mkdirs();
        }
        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3"));
        if (files != null) {
            int i = 1;
            for (File file : files) {
                try {
                    // Read metadata and picture (simplified)
                    String title = file.getName();
                    String artist = "Unknown Artist"; // You can use a library to read metadata
                    byte[] picture = new byte[0]; // You can use a library to read embedded pictures

                    Song song = new Song(String.valueOf(UUID.nameUUIDFromBytes(title.getBytes())), title, artist, file.getAbsolutePath(), picture);
                    songs.add(song);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                i++;
            }
        }
    }

    private static byte[] readFileBytes(String filePath) throws IOException {
        return Files.readAllBytes(Paths.get(filePath));
    }

    private static byte[] convertMp3ToWav(String mp3FilePath) throws IOException, JavaLayerException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        FileInputStream fileInputStream = new FileInputStream(mp3FilePath);
        AdvancedPlayer player = new AdvancedPlayer(fileInputStream);
        player.setPlayBackListener(new PlaybackListener(out));
        player.play();
        return out.toByteArray();
    }

    private static class PlaybackListener extends javazoom.jl.player.advanced.PlaybackListener {
        private final ByteArrayOutputStream out;

        public PlaybackListener(ByteArrayOutputStream out) {
            this.out = out;
        }

        @Override
        public void playbackFinished(javazoom.jl.player.advanced.PlaybackEvent evt) {
            super.playbackFinished(evt);
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void playbackStarted(javazoom.jl.player.advanced.PlaybackEvent evt) {
            super.playbackStarted(evt);
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                while (true) {
                    String request = (String) in.readObject();
                    if (request.equals("GET_SONGS")) {
                        out.writeObject(songs);
                        out.flush();
                    } else if (request.startsWith("GET_SONG ")) {
                        String songId = request.split(" ")[1];
                        Song song = songs.stream().filter(s -> s.getId().equals(songId)).findFirst().orElse(null);
                        if (song != null) {
                            byte[] songBytes = convertMp3ToWav(song.getFilePath());
                            out.writeObject(songBytes);
                            out.flush();
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException | JavaLayerException e) {
                e.printStackTrace();
            }
        }
    }
}