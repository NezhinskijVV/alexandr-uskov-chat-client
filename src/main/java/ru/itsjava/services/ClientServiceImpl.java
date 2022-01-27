package ru.itsjava.services;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.PrintWriter;
import java.net.Socket;

@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {
    // создаем константы
    public final static int PORT = 8085; // порт подключения к серверу
    public final static String HOST = "localhost"; // строка подключения е серверу
    private PrintWriter serverWriter;
    private MessageInputService messageInputService;
    public Socket socket;

    @SneakyThrows
    @Override
    public void start() {
        // хотим подключиться к серверу
        // чтобы работать на стороне Клиента необходим Socket
        socket = new Socket(HOST, PORT); // в Socket необходимо передать HOST - где находится сервер и PORT - порт на котором он слушает

        MessageInputService serverReader = new MessageInputServiceImpl(socket.getInputStream());
        messageInputService = new MessageInputServiceImpl(System.in);
        serverWriter = new PrintWriter(socket.getOutputStream());

        // у socket'a проверить, если он подключился
        if (socket.isConnected()) {
            // как только подсоединились, создаем фоновый поток

            // у socket'a есть InputStream (что-то писать) и OutputStream (что-то получать)
            // в данном случае берем InputStream и отправляем что-то на сервер
            // чтобы что-то отправить нам подойдет PrintWriter оболочка над InputStream и OutputStream
            // теперь должны считать с консоли, слушать сообщения с сервера и делать это одновременно
            // реализуем с помощью потоков

            // считываем с консоли

            MenuService menuService = new MenuServiceImpl(this);
            menuService.menu();


            switch (menuService.getNumMenu()) {
                case 1: {
                    Thread thread = new Thread(
                            new AuthorizationRunnable(messageInputService, serverWriter, serverReader));
                    thread.start();
                    thread.join();
                    break;
                }
                case 2: {
                    registrationNewUser();
                    break;
                }
                case 3: {
                    exitChat();
                    break;
                }
            }
            SocketRunnable socketRunnable = new SocketRunnable(serverReader);
            new Thread(socketRunnable).start();

            boolean isExit = false; // создание переменной isExit для проверки ввода "exit" для выхода из чата
            // считывать в цикле и отправлять сообщения
            while (!isExit) {
//                System.out.println("Введите сообщение");
                // из messageInputService должны получать сообщение, которое написал пользователь
                String consoleMessage = messageInputService.getMessage();
                isExit = consoleMessage.equals("exit");
                // проверка ввода клиентом слова "exit", чтобы покинуть чат
                if (isExit) {
                    serverWriter.println("Всем пока!");
                    serverWriter.flush(); // скинуть буферизированные данные в поток
                    exitChat();
                }

                // кто-то ввел сообщение consoleMessage, и мы должны отправить - serverWriter
                serverWriter.println(consoleMessage);
                serverWriter.flush(); // скинуть буферизированные данные в поток
            }
        }
    }

    @SneakyThrows
    @Override
    public void registrationNewUser() {
        System.out.println("Введите свой логин:");
        String login = messageInputService.getMessage();
        System.out.println("Введите свой пароль:");
        String password = messageInputService.getMessage();
        // после ввода логина и пароля - их нужно отправить на сервер
        // !reg!login:password
        // теперь конкатенируем - все собираем
        // теперь отправляем все это на сервер
        serverWriter.println("!reg!" + login + ":" + password);
        serverWriter.flush();
    }

    @SneakyThrows
    @Override
    public void exitChat() {
        System.exit(0);
    }
}