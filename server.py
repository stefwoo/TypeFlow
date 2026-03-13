#!/usr/bin/env python3
# /// uv
# dependencies = ["pyperclip", "pyautogui"]
# ///

"""
Remote Input Method - PC Receiver Server

Run:
  uv run server.py
"""

import json
import threading
from http.server import HTTPServer, BaseHTTPRequestHandler
from urllib.parse import parse_qs

import pyperclip
import pyautogui

HOST = "0.0.0.0"
PORT = 9527


class InputRequestHandler(BaseHTTPRequestHandler):
    def do_POST(self):
        try:
            content_length = int(self.headers.get("Content-Length", 0))
            body = self.rfile.read(content_length).decode("utf-8")

            data = json.loads(body)
            text = data.get("text", "")

            print(f"[Received] {text[:50]}{'...' if len(text) > 50 else ''}")

            pyperclip.copy(text)

            threading.Timer(0.1, self._paste).start()

            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.end_headers()
            self.wfile.write(json.dumps({"status": "ok"}).encode())

        except json.JSONDecodeError:
            print("[Error] Invalid JSON")
            self._send_error("Invalid JSON")
        except Exception as e:
            print(f"[Error] {e}")
            self._send_error(str(e))

    def _paste(self):
        try:
            pyautogui.hotkey("ctrl", "v")
            print("[Pasted] Text pasted successfully")
        except Exception as e:
            print(f"[Paste Error] {e}")

    def _send_error(self, message):
        self.send_response(400)
        self.send_header("Content-Type", "application/json")
        self.end_headers()
        self.wfile.write(json.dumps({"status": "error", "message": message}).encode())

    def log_message(self, format, *args):
        pass


def main():
    server = HTTPServer((HOST, PORT), InputRequestHandler)
    print(f"Server started on http://{HOST}:{PORT}")
    print("Waiting for input...")
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\nServer stopped")
        server.shutdown()


if __name__ == "__main__":
    main()
