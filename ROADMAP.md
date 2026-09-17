# Roadmap
- Input Only mode: no video capture/transfer.
- P1/P2 USB gamepad capture using WinMM/Raw Input on Windows 7.
- Per-emulator gamepad-to-touch mapping profiles.
- Investigate privileged/ADB virtual input path for emulators that require native Android gamepad devices.
- Screen-dark mode: attempt vendor-safe minimum-brightness/overlay while keeping projection and Wi-Fi awake; true panel-off behavior varies by Android/vendor and cannot be promised without elevated privileges.
- Real FPS / Mbps / latency telemetry.
- MPEG-1 experimental stream only if it materially beats MJPEG on Atom N550; never H.264 for the target netbook profile.

## v0.4 Input capture
- Input Only mouse capture: Windows cursor is hidden and raw relative mouse movement controls the Android cursor.
- Ctrl+Alt releases the mouse back to Windows.
- Keyboard routing works even when a child control in the Windows UI had focus.
- Android UI now separates enabling the LAN Remote IME from choosing it as the active keyboard.
- Persistent Win32 back buffer + bilinear scaling reduces per-frame CPU overhead.


### v0.5
- Robust Input Only mouse lock
- Adjustable mouse sensitivity
- Space key fix
- Experimental 60 FPS
- Stretch Screen

## v0.6 implemented
- Configurable port
- JPEG quality and unchanged-frame suppression
- Alt+F fullscreen / Esc restore
- Rotation-aware capture and pointer mapping
- IME-first text editing
