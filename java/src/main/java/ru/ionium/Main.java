package ru.ionium;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Основной класс для запуска простого HTTP-сервера.
 * Сервер обрабатывает GET и POST запросы и возвращает соответствующие HTML-страницы.
 */
public class Main {

    /**
     * Точка входа в программу. Запускает сервер на порту 8080 и создает новый поток для обработки каждого подключения.
     *
     * @param args аргументы командной строки, не используются.
     */
    public static void main(String[] args) {
        final int port = 8080; // Порт для работы сервера

        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("HTTP сервер запущен на порту " + port);

            // Бесконечный цикл для прослушивания подключений
            while (true) {
                Socket clientSocket = serverSocket.accept(); // Ожидание клиента

                // Обработка клиента в новом потоке
                new Thread(() -> {
                    try {
                        handleClient(clientSocket); // Вызов метода обработки клиента
                    } catch (IOException e) {
                        System.err.println("Произошла ошибка при остановке сервера. " + e.getMessage());
                    }
                }).start();
            }
        } catch (IOException e) {
            System.err.println("Произошла ошибка при работе сервера. " + e.getMessage());
        }
    }

    /**
     * Обрабатывает подключение клиента: читает запрос, обрабатывает его и отправляет ответ.
     *
     * @param clientSocket сокет клиента для получения и отправки данных.
     * @throws IOException если возникает ошибка при работе с сокетом.
     */
    private static void handleClient(Socket clientSocket) throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             OutputStream out = clientSocket.getOutputStream()) {

            // Чтение первой строки запроса (например, "GET / HTTP/1.1")
            String requestLine = in.readLine();
            System.out.println("Запрос: " + requestLine);

            // Чтение всех заголовков запроса
            Map<String, String> headers = new HashMap<>();
            String line;
            while (!(line = in.readLine()).isEmpty()) {
                String[] headerParts = line.split(": ");
                headers.put(headerParts[0], headerParts.length > 1 ? headerParts[1] : "");
            }

            // Разбор первой строки запроса
            if (requestLine != null) {
                String[] requestParts = requestLine.split(" ");
                String method = requestParts[0]; // Метод запроса (GET или POST)
                String path = requestParts[1];   // Путь (например, / или /about)

                // Выбор обработки на основе метода запроса
                switch (method) {
                    case "GET" -> handleGetRequest(path, out);
                    case "POST" -> handlePostRequest(in, headers, out);
                    default -> sendNotFound(out);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            clientSocket.close(); // Закрытие сокета после завершения работы с клиентом
        }
    }

    /**
     * Обрабатывает GET-запросы и отправляет соответствующий ответ.
     *
     * @param path путь запроса (например, "/" или "/about").
     * @param out  поток для отправки ответа клиенту.
     * @throws IOException если возникает ошибка при отправке данных.
     */
    private static void handleGetRequest(String path, OutputStream out) throws IOException {
        if ("/".equals(path)) {
            createHttpResponse("HTTP/1.1 200 OK\r\n", "<html><body><h1>Welcome to the Home Page</h1></body></html>", out);
        } else if ("/about".equals(path)) {
            createHttpResponse("HTTP/1.1 200 OK\r\n", "<html><body><h1>About Us</h1></body></html>", out);
        } else {
            sendNotFound(out); // Если путь не распознан, отправляется ответ 404
        }
    }

    /**
     * Обрабатывает POST-запросы, извлекает тело запроса и отправляет ответ.
     *
     * @param in      входной поток для чтения тела запроса.
     * @param headers заголовки запроса.
     * @param out     поток для отправки ответа клиенту.
     * @throws IOException если возникает ошибка при чтении или отправке данных.
     */
    private static void handlePostRequest(BufferedReader in, Map<String, String> headers, OutputStream out) throws IOException {
        // Извлечение длины содержимого из заголовков
        int contentLength = Integer.parseInt(headers.getOrDefault("Content-Length", "0"));

        char[] body = new char[contentLength];
        in.read(body, 0, contentLength); // Чтение тела запроса

        String requestBody = new String(body);
        System.out.println("Данные POST-запроса: " + requestBody);

        createHttpResponse("HTTP/1.1 200 OK\r\n", "<html><body><h1>POST data received</h1></body></html>", out);
    }

    /**
     * Создает и отправляет HTTP-ответ с указанным содержимым.
     *
     * @param httpInformation строка с HTTP-информацией (например, "HTTP/1.1 200 OK\r\n").
     * @param htmlContent     HTML-контент, который будет отправлен в ответе.
     * @param out             поток для отправки ответа клиенту.
     * @throws IOException если возникает ошибка при отправке данных.
     */
    private static void createHttpResponse(String httpInformation, String htmlContent, OutputStream out) throws IOException {
        String response = httpInformation +
                "Content-Type: text/html\r\n" +
                "Connection: close\r\n" +
                "\r\n" +
                htmlContent;
        out.write(response.getBytes("UTF-8"));
    }

    /**
     * Отправляет ответ 404 "Not Found" клиенту.
     *
     * @param out поток для отправки ответа клиенту.
     * @throws IOException если возникает ошибка при отправке данных.
     */
    private static void sendNotFound(OutputStream out) throws IOException {
        createHttpResponse("HTTP/1.1 404 Not Found\r\n", "<html><body><h1>404 - Page Not Found</h1></body></html>", out);
    }
}