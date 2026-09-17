# LAN Remote v0.3

**Give old computers a new job. / 让旧电脑重新发挥价值。**

LAN Remote connects a modern Android device to an older Windows PC over the local Wi‑Fi network. The Windows client is deliberately native Win32/x86 and targets Windows 7 Starter 32-bit-class hardware such as an Intel Atom N550.

## Modes

### Screen + Control
- Android screen streamed as JPEG frames over TCP (no H.264).
- 480 / 720 / 960 / 1200 maximum edge.
- 7 / 15 / 24 FPS.
- Double-buffered Win32 rendering to reduce flicker.
- Mouse coordinates are mapped to the exact displayed Android image rectangle, including letterboxing.
- Keyboard input is forwarded through the Android accessibility/IME bridge.

### Input Only
- Android's own screen remains the screen you look at.
- No video is transmitted or decoded.
- Windows mouse moves a visible accessibility cursor on Android.
- Left click taps; mouse wheel scrolls.
- Windows keyboard controls text/navigation.
- USB joystick/gamepad on Windows can move the Android cursor; button 1 taps, button 2 = Back, button 3 = Home.

> Gamepad support in v0.3 is a **remote touch/cursor mapping**, not native Android gamepad-device emulation. Native virtual gamepad injection generally needs a privileged/root/ADB-style input path and is intentionally not claimed here.

## Portable Windows 7 x86 build

GitHub Actions uses MSYS2 MINGW32 and `i686-w64-mingw32-g++`. The executable is linked with `-static -static-libgcc -static-libstdc++`. CI inspects PE imports and fails if `libwinpthread-1.dll` appears. This preserves the Win32 DLL fix established during testing.

## 中文

LAN Remote 让现代 Android 设备通过局域网 Wi‑Fi 与旧 Windows 电脑协同工作。Windows 客户端为原生 Win32/x86，目标包括 Windows 7 Starter 32 位和 Intel Atom N550 这类旧硬件。

**屏幕 + 控制模式：** 使用 JPEG/TCP 传输画面（不使用 H.264），支持 480/720/960/1200 和 7/15/24 FPS；Windows 端使用双缓冲减少闪烁，并按实际显示区域精确映射鼠标坐标。

**仅输入模式：** 不传输视频，继续直接看 Android 自己的屏幕；Windows 鼠标控制 Android 上可见的辅助功能光标，键盘实时输入，USB 手柄可作为触控/光标控制器。

## Build

Push to GitHub and open **Actions → Build LAN Remote**. Artifacts:
- `LANRemote-Android`
- `LANRemote-Windows-Win32-x86`


## v0.5
Adds robust VM-style mouse capture with Ctrl+Alt release, adjustable mouse sensitivity, Space key fix, experimental 60 FPS mode, and optional Stretch Screen. 60 FPS is workload-dependent; lower resolutions are recommended on older PCs.

## v0.6
- Configurable TCP port on both Android and Windows (7878 remains the default).
- JPEG quality selector (35/50/65/80) plus lightweight unchanged-frame suppression to reduce LAN traffic.
- 60 FPS capture path is now allowed end-to-end (experimental; 480p is recommended on Atom-class PCs).
- Alt+F toggles borderless fullscreen; Esc leaves fullscreen.
- Rotation-aware Android capture reconfigures when the real display dimensions change; mouse mapping follows the current received frame. Stretch continues to map against the stretched viewport.
- When LAN Remote Keyboard is active, text and editing keys use the IME first. Accessibility text editing is only a fallback, avoiding placeholder/hint text being mistaken for user text in apps such as WhatsApp.
