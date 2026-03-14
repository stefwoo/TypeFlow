//go:build windows
// +build windows

package main

import (
	"time"

	"golang.org/x/sys/windows"
)

func simulatePaste() error {
	user32 := windows.MustLoadDLL("user32.dll")
	defer user32.Release()

	keybdEvent := user32.MustFindProc("keybd_event")

	keybdEvent.Call(0x11, 0, 0, 0)
	time.Sleep(50)
	keybdEvent.Call(0x56, 0, 0, 0)
	time.Sleep(50)
	keybdEvent.Call(0x56, 0, 0x0002, 0)
	time.Sleep(50)
	keybdEvent.Call(0x11, 0, 0x0002, 0)

	return nil
}
