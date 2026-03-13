package main

import (
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"time"

	"github.com/atotto/clipboard"
	"github.com/go-vgo/robotgo"
)

const (
	Host = "0.0.0.0"
	Port = 9527
)

type Request struct {
	Text string `json:"text"`
}

type Response struct {
	Status string `json:"status"`
}

func main() {
	addr := fmt.Sprintf("%s:%d", Host, Port)
	
	http.HandleFunc("/", handleRequest)
	
	log.Printf("Server started on http://%s", addr)
	log.Println("Waiting for input...")

	if err := http.ListenAndServe(addr, nil); err != nil {
		log.Fatal(err)
	}
}

func handleRequest(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}

	var req Request
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		log.Printf("[Error] Invalid JSON: %v", err)
		http.Error(w, "Invalid JSON", http.StatusBadRequest)
		return
	}

	text := req.Text
	if len(text) > 50 {
		log.Printf("[Received] %s...", text[:50])
	} else {
		log.Printf("[Received] %s", text)
	}

	if err := clipboard.WriteAll(text); err != nil {
		log.Printf("[Error] Clipboard error: %v", err)
		http.Error(w, "Clipboard error", http.StatusInternalServerError)
		return
	}

	time.Sleep(100 * time.Millisecond)

	robotgo.KeyTap("v", "ctrl")
	log.Println("[Pasted] Text pasted successfully")

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(Response{Status: "ok"})
}
