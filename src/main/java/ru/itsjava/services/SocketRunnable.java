package ru.itsjava.services;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class SocketRunnable implements Runnable {
    public final MessageInputService serverReader;

    @SneakyThrows
    @Override
    public void run() {
        // задаем объект serverReader для идентификации, считывания сообщения с сервера
        // serverReader зависит от socket.getInputStream()
        // получение сообщения от сервера
        while (true) {
            String messageFromServer = serverReader.getMessage();
            System.out.println(messageFromServer);
        }
    }

}
