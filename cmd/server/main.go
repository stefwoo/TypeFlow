//go:build !windows
// +build !windows

package main

import (
	"fmt"
	"os/exec"
	"runtime"
)

func simulatePaste() error {
	switch runtime.GOOS {
	case "linux":
		return exec.Command("xdotool", "key", "ctrl+v").Run()
	case "darwin":
		return exec.Command("osascript", "-e", `tell application "System Events" to keystroke "v" using command down`).Run()
	default:
		return fmt.Errorf("unsupported platform: %s", runtime.GOOS)
	}
}
