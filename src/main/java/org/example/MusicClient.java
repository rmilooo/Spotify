package org.example;

import javax.sound.sampled.*;
import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

public class MusicClient {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            // Request song list
            out.writeObject("GET_SONGS");
            out.flush();
            List<Song> songs = (List<Song>) in.readObject();

            if (songs.isEmpty()) {
                System.out.println("No songs available.");
                return;
            }

            // Display song list
            for (Song song : songs) {
                System.out.println("ID: " + song.getId() + ", Title: " + song.getTitle() + ", Artist: " + song.getArtist());
            }

            // User input to select a song
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter song ID to play: ");
            String songId = scanner.nextLine();

            // Request the selected song
            out.writeObject("GET_SONG " + songId);
            out.flush();
            byte[] songBytes = (byte[]) in.readObject();

            // Play the received song bytes
            playSong(songBytes);

        } catch (IOException | ClassNotFoundException | LineUnavailableException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
    }

    private static void playSong(byte[] songBytes) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        InputStream byteArrayInputStream = new ByteArrayInputStream(songBytes);
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(byteArrayInputStream);
        AudioFormat format = audioInputStream.getFormat();
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();

        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = audioInputStream.read(buffer)) != -1) {
            line.write(buffer, 0, bytesRead);
        }

        line.drain();
        line.close();
        audioInputStream.close();
    }
}