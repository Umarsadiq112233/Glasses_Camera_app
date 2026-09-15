# 👓 HeyCyan Smart Glasses Companion

A premium, modern Flutter companion application engineered for Smart Glasses consumer hardware. Connect your smart glasses via Bluetooth & Wi-Fi P2P to control camera capture, record high-definition video, record voice audio clips, monitor battery status, transcode media, and manage your local gallery seamlessly.

---

## ✨ Key Features

- **📱 Glasses Dashboard & Device Status**
  - Real-time Bluetooth Low Energy (BLE) discovery and pairing.
  - Live battery level gauge with charging indicators & low-battery alerts.
  - Device telemetry: Firmware version, MAC/UUID identifier, RSSI signal strength meter, and total stored media item counters.
  - Automatic reconnection logic for previously paired hardware.

- **📸 Hardware Viewfinder & Camera Experience**
  - Dynamic HUD Viewfinder overlay with corner bracket framing.
  - Segmented mode switcher tabs: **Photo**, **Video**, and **Voice Audio**.
  - Tactile Shutter Button with press scale micro-interactions, inner core glow, and recording pulse ring.
  - Real-time video & audio recording timers with active status indicators.

- **⚡ Wi-Fi P2P Media Sync & Import**
  - High-speed media transfer over local Wi-Fi P2P connection.
  - Multi-stage human-readable import progress card (*"Transferring media... 12 of 15 items"*).
  - Built-in retry and diagnostic handling for Wi-Fi prompts and timeouts.

- **🖼️ Media Gallery & Storage Management**
  - Category pill tabs: **Photos**, **Videos**, and **Audio Clips**.
  - Interactive photo viewer with pinch-to-zoom and detailed file metadata.
  - In-app Floating Audio Player with live equalizer waveform animation, seek slider, and 5s rewind/fast-forward controls.
  - Integrated Video Player dialog powered by native `MediaExtractor` / `MediaMuxer` video transcoding.
  - Safe storage management: Clear app local storage or format glasses hardware storage securely.

- **⚙️ Settings & System Permissions**
  - Grouped setting cards for device specifications and app preferences (*Auto-Reconnect*, *Auto-Sync*).
  - OTA Firmware update URL prompt and hardware restart triggers.
  - Pre-permission explanatory screens for Bluetooth, Location Services, Camera, Microphone, and Media Storage access.

---

## 🎨 Tech Stack & Architecture

- **Framework**: [Flutter](https://flutter.dev/) (Dart SDK `^3.13.1`)
- **State & Communication**: Stream Controllers, Broadcast Event Channels, Method Channels (`glasses_camera/methods`).
- **Design System**: Centralized `AppTheme` with hardware space-dark color tokens (`#0B0F19`), Electric Cyan accent (`#00E5FF`), crisp typography, and rounded radii.
- **Media Playback & Transcoding**: `video_player`, custom audio stream handler, native video remuxer/transcoder (`VideoTranscoder`).

---

## 📂 Project Structure

```text
lib/
├── main.dart                      # App entry point, MaterialApp theme & main navigation
└── glasses_camera/
    ├── camera_screen.dart         # Camera viewfinder UI & controls
    ├── device_screen.dart         # Dashboard, battery status & BLE discovery
    ├── glasses_camera_service.dart # Service layer interfacing with native SDK platform channels
    ├── media_gallery_screen.dart  # Photos, Videos, Audio tabs & Floating Audio Player
    ├── models.dart                # Strongly-typed models, enums & event telemetry
    ├── permission_dialog_helper.dart # Location & permission dialog prompts
    ├── permissions_screen.dart    # System permissions explanation list
    ├── settings_screen.dart       # App preferences, firmware OTA & device specs
    ├── video_player_dialog.dart   # Full-screen video player dialog
    ├── video_transcoder.dart      # Native video remuxing/transcoding helper
    ├── theme/
    │   └── app_theme.dart         # Global design system (AppColors, AppTypography, AppTheme)
    └── widgets/
        ├── camera_viewfinder.dart # Viewfinder frame & HUD graphics
        ├── connection_status_badge.dart # Pulsing LED connection state badge
        ├── empty_state_card.dart  # Contextual empty state component
        ├── error_state_card.dart  # Human-readable error card
        ├── glass_device_card.dart # Hero status card displaying battery & specs
        ├── import_progress_card.dart # Media import progress card
        └── shutter_button.dart    # Tactile shutter button with press feedback
```

---

## 🚀 Getting Started

### Prerequisites

- [Flutter SDK](https://docs.flutter.dev/get-started/install) (v3.13.1 or later)
- Android Studio / Xcode for device simulation & physical hardware testing
- Physical Smart Glasses (or native SDK mock environment)

### Installation

1. **Clone the repository**:
   ```bash
   git clone https://github.com/Umarsadiq112233/Glasses_Camera_app.git
   cd Glasses_Camera_app
   ```

2. **Install dependencies**:
   ```bash
   flutter pub get
   ```

3. **Run Static Code Analysis**:
   ```bash
   flutter analyze
   ```

4. **Run Unit Tests**:
   ```bash
   flutter test
   ```

5. **Launch App**:
   ```bash
   flutter run
   ```

---

## 📄 License

This project is released under the **MIT License**.
