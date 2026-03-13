#!/usr/bin/env python3
# /// script
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
    def do_GET(self):
        self.send_response(200)
        self.send_header("Content-Type", "text/html; charset=utf-8")
        self.send_header("Access-Control-Allow-Origin", "*")
        self.end_headers()
        self.wfile.write(b"""<html><body>
        <h1>TypeFlow Server</h1>
        <p>Status: Running</p>
        <p>仅支持 POST 请求</p>
        <p>发送 JSON: {"text": "内容"}</p>
        </body></html>""")

    def do_POST(self):
        print(f"[Request] {self.command} {self.path} from {self.client_address}")
        print(f"[Headers] {dict(self.headers)}")
        
        try:
            content_length = int(self.headers.get("Content-Length", 0))
            body = self.rfile.read(content_length).decode("utf-8")
            print(f"[Body] {body}")

            data = json.loads(body)
            text = data.get("text", "")

            print(f"[Received] Text length: {len(text)} chars")
            print(f"[Text preview] {text[:100]}{'...' if len(text) > 100 else ''}")

            pyperclip.copy(text)
            print(f"[Clipboard] Copied to clipboard")

            threading.Timer(0.1, self._paste).start()

            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.send_header("Access-Control-Allow-Origin", "*")
            self.end_headers()
            self.wfile.write(json.dumps({"status": "ok"}).encode())

        except json.JSONDecodeError as e:
            print(f"[Error] Invalid JSON: {e}")
            self._send_error(f"Invalid JSON: {e}")
        except Exception as e:
            print(f"[Error] {e}")
            self._send_error(str(e))

    def do_OPTIONS(self):
        self.send_response(200)
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "POST, GET, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "Content-Type")
        self.end_headers()

    def _paste(self):
        try:
            pyautogui.hotkey("ctrl", "v")
            print("[Pasted] Text pasted successfully")
        except Exception as e:
            print(f"[Paste Error] {e}")

    def _send_error(self, message):
        self.send_response(400)
        self.send_header("Content-Type", "application/json")
        self.send_header("Access-Control-Allow-Origin", "*")
        self.end_headers()
        self.wfile.write(json.dumps({"status": "error", "message": message}).encode())

    def log_message(self, format, *args):
        print(f"[HTTP] {format % args}")


def main():
    server = HTTPServer((HOST, PORT), InputRequestHandler)
    print(f"=" * 50)
    print(f"TypeFlow Server")
    print(f"=" * 50)
    print(f"Server started on http://{HOST}:{PORT}")
    print(f"Waiting for input...")
    print(f"=" * 50)
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\nServer stopped")
        server.shutdown()


if __name__ == "__main__":
    main()
