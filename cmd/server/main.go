package main

import (
	"encoding/json"
	"flag"
	"fmt"
	"log"
	"net/http"
	"os"
	"os/exec"
	"runtime"
	"time"

	"github.com/atotto/clipboard"
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
	// Windows 下隐藏控制台窗口
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
	var cmd *exec.Cmd

	switch runtime.GOOS {
	case "windows":
		cmd = exec.Command("powershell", "-Command", "Add-Type -AssemblyName System.Windows.Forms; [System.Windows.Forms.SendKeys]::SendWait('^v')")
	case "linux":
		cmd = exec.Command("xdotool", "key", "ctrl+v")
	case "darwin":
		cmd = exec.Command("osascript", "-e", "tell application \"System Events\" to keystroke \"v\" using command down")
	default:
		return fmt.Errorf("unsupported platform: %s", runtime.GOOS)
	}

	return cmd.Run()
}

// hideConsole hides the console window on Windows
func hideConsole() {
	// Windows API to hide console - requires syscall
	// For simplicity, we just redirect stdout/stderr to null
	devNull, err := os.OpenFile(os.DevNull, os.O_WRONLY, 0)
	if err != nil {
		return
	}
	os.Stdout = devNull
	os.Stderr = devNull
}
