library;

/// Glasses Camera SDK — Strongly-typed models, states, and enums
/// Derived from official HeyCyan Android SDK + QCSDK iOS SDK APIs

// ─── Connection States ───

enum GlassesConnectionState {
  initializing,
  bluetoothOff,
  permissionRequired,
  scanning,
  deviceFound,
  connecting,
  connected,
  disconnecting,
  disconnected,
  reconnecting,
  error;

  String get displayName {
    switch (this) {
      case GlassesConnectionState.initializing:
        return 'Initializing SDK...';
      case GlassesConnectionState.bluetoothOff:
        return 'Bluetooth Disabled';
      case GlassesConnectionState.permissionRequired:
        return 'Permissions Required';
      case GlassesConnectionState.scanning:
        return 'Scanning for Glasses...';
      case GlassesConnectionState.deviceFound:
        return 'Glasses Discovered';
      case GlassesConnectionState.connecting:
        return 'Connecting...';
      case GlassesConnectionState.connected:
        return 'Connected';
      case GlassesConnectionState.disconnecting:
        return 'Disconnecting...';
      case GlassesConnectionState.disconnected:
        return 'Disconnected';
      case GlassesConnectionState.reconnecting:
        return 'Reconnecting...';
      case GlassesConnectionState.error:
        return 'Connection Error';
    }
  }

  bool get isConnected => this == GlassesConnectionState.connected;
  bool get isScanning => this == GlassesConnectionState.scanning;
  bool get isConnecting =>
      this == GlassesConnectionState.connecting ||
      this == GlassesConnectionState.reconnecting;
}

enum GlassesBluetoothState {
  unknown,
  resetting,
  unsupported,
  unauthorized,
  poweredOff,
  poweredOn;

  bool get isPoweredOn => this == GlassesBluetoothState.poweredOn;
}

// ─── Camera & Work Modes ───

enum GlassesCameraState {
  unavailable,
  ready,
  capturing,
  recording,
  recordingAudio,
  error;

  bool get isReady => this == GlassesCameraState.ready;
  bool get isRecording =>
      this == GlassesCameraState.recording ||
      this == GlassesCameraState.recordingAudio;
}

/// Work-type codes matching SDK byte responses (0=idle, 1=photo, 2=video, 4=transfer, 5=ota, 6=aiPhoto, 7=aiChat, 8=audio)
enum GlassesWorkType {
  idle(0),
  photo(1),
  video(2),
  transfer(4),
  ota(5),
  aiPhoto(6),
  aiChat(7),
  audio(8);

  final int code;
  const GlassesWorkType(this.code);

  static GlassesWorkType fromCode(int code) {
    return GlassesWorkType.values.firstWhere(
      (e) => e.code == code,
      orElse: () => GlassesWorkType.idle,
    );
  }
}

// ─── Device Data Models ───

class GlassesDevice {
  final String name;
  final String identifier; // MAC Address on Android, Peripheral UUID on iOS
  final String? mac;
  final int rssi;
  final bool isPaired;

  const GlassesDevice({
    required this.name,
    required this.identifier,
    this.mac,
    this.rssi = 0,
    this.isPaired = false,
  });

  factory GlassesDevice.fromMap(Map<dynamic, dynamic> map) {
    return GlassesDevice(
      name: map['name'] as String? ?? 'Unknown Glasses',
      identifier: map['identifier'] as String? ?? '',
      mac: map['mac'] as String?,
      rssi: (map['rssi'] as int?) ?? 0,
      isPaired: (map['isPaired'] as bool?) ?? false,
    );
  }
}

class GlassesBatteryInfo {
  final int level; // 0-100%
  final bool isCharging;

  const GlassesBatteryInfo({required this.level, required this.isCharging});

  factory GlassesBatteryInfo.fromMap(Map<dynamic, dynamic> map) {
    return GlassesBatteryInfo(
      level: (map['level'] as int?) ?? 0,
      isCharging: (map['isCharging'] as bool?) ?? false,
    );
  }
}

class GlassesVersionInfo {
  final String hardwareVersion;
  final String firmwareVersion;
  final String wifiHardwareVersion;
  final String wifiFirmwareVersion;

  const GlassesVersionInfo({
    this.hardwareVersion = '',
    this.firmwareVersion = '',
    this.wifiHardwareVersion = '',
    this.wifiFirmwareVersion = '',
  });

  factory GlassesVersionInfo.fromMap(Map<dynamic, dynamic> map) {
    return GlassesVersionInfo(
      hardwareVersion: map['hardwareVersion'] as String? ?? '',
      firmwareVersion: map['firmwareVersion'] as String? ?? 'v1.0.0',
      wifiHardwareVersion: map['wifiHardwareVersion'] as String? ?? '',
      wifiFirmwareVersion: map['wifiFirmwareVersion'] as String? ?? '',
    );
  }
}

class GlassesMediaInfo {
  final int photoCount;
  final int videoCount;
  final int audioCount;

  const GlassesMediaInfo({
    this.photoCount = 0,
    this.videoCount = 0,
    this.audioCount = 0,
  });

  int get totalCount => photoCount + videoCount + audioCount;

  factory GlassesMediaInfo.fromMap(Map<dynamic, dynamic> map) {
    return GlassesMediaInfo(
      photoCount: (map['photoCount'] as int?) ?? 0,
      videoCount: (map['videoCount'] as int?) ?? 0,
      audioCount: (map['audioCount'] as int?) ?? 0,
    );
  }
}

// ─── Event Telemetry ───

enum GlassesEventType {
  batteryUpdate,
  mediaCountUpdate,
  photoThumbnail,
  mediaDownloadProgress,
  mediaDownloadComplete,
  mediaDownloadError,
  deviceNotification,
  workTypeChanged,
  audioPlaybackState,
  error,
}

class GlassesEvent {
  final GlassesEventType type;
  final Map<String, dynamic> data;
  final String? message;

  const GlassesEvent({required this.type, this.data = const {}, this.message});

  factory GlassesEvent.fromMap(Map<dynamic, dynamic> map) {
    return GlassesEvent(
      type: GlassesEventType.values.firstWhere(
        (e) => e.name == (map['type'] as String?),
        orElse: () => GlassesEventType.deviceNotification,
      ),
      data: Map<String, dynamic>.from(map['data'] as Map? ?? {}),
      message: map['message'] as String?,
    );
  }
}

class MediaDownloadProgress {
  final int receivedSize;
  final int expectedSize;
  final double progress;
  final int currentIndex;
  final int totalFiles;
  final String? fileName;
  final String? speed;

  const MediaDownloadProgress({
    this.receivedSize = 0,
    this.expectedSize = 0,
    this.progress = 0.0,
    this.currentIndex = 0,
    this.totalFiles = 0,
    this.fileName,
    this.speed,
  });

  factory MediaDownloadProgress.fromMap(Map<dynamic, dynamic> map) {
    return MediaDownloadProgress(
      receivedSize: (map['receivedSize'] as int?) ?? 0,
      expectedSize: (map['expectedSize'] as int?) ?? 0,
      progress: (map['progress'] as num?)?.toDouble() ?? 0.0,
      currentIndex: (map['currentIndex'] as int?) ?? 0,
      totalFiles: (map['totalFiles'] as int?) ?? 0,
      fileName: map['fileName'] as String?,
      speed: map['speed'] as String?,
    );
  }
}

class GlassesStatusInfo {
  final GlassesBatteryInfo battery;
  final GlassesVersionInfo version;
  final GlassesMediaInfo media;

  const GlassesStatusInfo({
    required this.battery,
    required this.version,
    required this.media,
  });

  factory GlassesStatusInfo.fromMap(Map<dynamic, dynamic> map) {
    return GlassesStatusInfo(
      battery: GlassesBatteryInfo.fromMap(map['battery'] as Map? ?? {}),
      version: GlassesVersionInfo.fromMap(map['version'] as Map? ?? {}),
      media: GlassesMediaInfo.fromMap(map['media'] as Map? ?? {}),
    );
  }
}

class LocalMediaFile {
  final String path;
  final String name;
  final int size;
  final int lastModified;
  final String type; // "photo", "video", "audio", "other"

  const LocalMediaFile({
    required this.path,
    required this.name,
    required this.size,
    required this.lastModified,
    required this.type,
  });

  factory LocalMediaFile.fromMap(Map<dynamic, dynamic> map) {
    return LocalMediaFile(
      path: map['path'] as String? ?? '',
      name: map['name'] as String? ?? '',
      size: (map['size'] as int?) ?? 0,
      lastModified: (map['lastModified'] as int?) ?? 0,
      type: map['type'] as String? ?? 'other',
    );
  }

  String get formattedSize {
    if (size < 1024) return '$size B';
    if (size < 1024 * 1024) return '${(size / 1024).toStringAsFixed(1)} KB';
    return '${(size / (1024 * 1024)).toStringAsFixed(1)} MB';
  }

  DateTime get dateTime => DateTime.fromMillisecondsSinceEpoch(lastModified);

  bool get isPhoto =>
      type == 'photo' ||
      name.endsWith('.jpg') ||
      name.endsWith('.jpeg') ||
      name.endsWith('.png');
  bool get isVideo =>
      type == 'video' || name.endsWith('.mp4') || name.startsWith('video');
  bool get isAudio =>
      type == 'audio' ||
      name.endsWith('.opus') ||
      name.endsWith('.pcm') ||
      name.endsWith('.mp3') ||
      name.endsWith('.wav');
}
