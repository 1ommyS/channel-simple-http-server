package main

import (
	"fmt"
	"io"
	"net/http"
	"os"
)

// Обработчик для главной страницы
func homePage(w http.ResponseWriter, r *http.Request) {
	if r.Method == http.MethodGet {
		fmt.Fprintln(w, "<html><body><h1>Welcome to the Home Page</h1></body></html>")
	} else {
		http.NotFound(w, r)
	}
}

// Обработчик для страницы "О нас"
func aboutPage(w http.ResponseWriter, r *http.Request) {
	if r.Method == http.MethodGet {
		fmt.Fprintln(w, "<html><body><h1>About Us</h1></body></html>")
	} else {
		http.NotFound(w, r)
	}
}

// Обработчик для POST-запросов
func postData(w http.ResponseWriter, r *http.Request) {
	if r.Method == http.MethodPost {
		body, err := io.ReadAll(r.Body)
		if err != nil {
			http.Error(w, "Unable to read request body", http.StatusInternalServerError)
			return
		}
		fmt.Println("POST data received: ", string(body))
		fmt.Fprintln(w, "<html><body><h1>POST data received</h1></body></html>")
	} else {
		http.NotFound(w, r)
	}
}

func main() {
	port := "8080"

	// Определение маршрутов
	http.HandleFunc("/", homePage)
	http.HandleFunc("/about", aboutPage)
	http.HandleFunc("/post", postData)

	// Запуск сервера
	fmt.Println("HTTP сервер запущен на порту " + port)
	err := http.ListenAndServe(":"+port, nil)
	if err != nil {
		fmt.Println("Ошибка запуска сервера:", err)
		os.Exit(1)
	}
}