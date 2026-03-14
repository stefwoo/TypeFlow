package main

import (
	"encoding/json"
	"flag"
	"fmt"
	"log"
	"net/http"
	"os"
	"runtime"
	"time"

	"github.com/atotto/clipboard"
	"golang.org/x/sys/windows"
)

var (
	host string
	port int
)

func init() {
	flag.StringVar(&host, "host", "0.0.0.0", "Host to bind")
	flag.IntVar(&port, "port", 9527, "Port to listen")
	flag.Parse()
}

type Request struct {
	Text string `json:"text"`
}

type Response struct {
	Status string `json:"status"`
}

func main() {
	if runtime.GOOS == "windows" {
		hideConsole()
	}

	addr := fmt.Sprintf("%s:%d", host, port)

	http.HandleFunc("/", handleRequest)

	log.Printf("TypeFlow Server started on http://%s", addr)
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

	if err := simulatePaste(); err != nil {
		log.Printf("[Warning] Paste failed: %v", err)
	} else {
		log.Println("[Pasted] Text pasted successfully")
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(Response{Status: "ok"})
}

func simulatePaste() error {
	switch runtime.GOOS {
	case "windows":
		return windowsPaste()
	case "linux":
		return linuxPaste()
	case "darwin":
		return darwinPaste()
	default:
		return fmt.Errorf("unsupported platform: %s", runtime.GOOS)
	}
}

func windowsPaste() error {
	// Use user32.dll for keybd_event
	user32 := windows.MustLoadDLL("user32.dll")
	defer user32.Release()

	keybdEvent := user32.MustFindProc("keybd_event")
	
	// VK_CONTROL = 0x11
	// VK_V = 0x56
	// KEYEVENTF_KEYUP = 0x0002

	// Press Ctrl
	keybdEvent.Call(0x11, 0, 0, 0)
	time.Sleep(50)
	// Press V
	keybdEvent.Call(0x56, 0, 0, 0)
	time.Sleep(50)
	// Release V
	keybdEvent.Call(0x56, 0, 0x0002, 0)
	time.Sleep(50)
	// Release Ctrl
	keybdEvent.Call(0x11, 0, 0x0002, 0)

	return nil
}

func linuxPaste() error {
	return nil
}

func darwinPaste() error {
	return nil
}

func hideConsole() {
	devNull, err := os.OpenFile(os.DevNull, os.O_WRONLY, 0)
	if err != nil {
		return
	}
	os.Stdout = devNull
	os.Stderr = devNull
}
