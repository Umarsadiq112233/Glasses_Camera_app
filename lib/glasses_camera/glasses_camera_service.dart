import 'dart:async';
import 'dart:developer' as developer;

import 'package:flutter/services.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'models.dart';
import 'video_transcoder.dart';

/// Service wrapping native HeyCyan (Android) / QCSDK (iOS) glasses SDK
/// via platform channels.
///
/// Architecture:
///   Flutter UI → GlassesCameraService → MethodChannel/EventChannel
///     → Android (BleOperateManager, LargeDataHandler, GlassesControl)
///     → iOS (QCCentralManager, QCSDKManager, QCSDKCmdCreator)
///     → Hardware Glasses
class GlassesCameraService {
  static const String _tag = 'GLASSES';

  // Platform channels
  static const MethodChannel _method = MethodChannel('glasses_camera/methods');
  static const EventChannel _connectionStateChannel = EventChannel(
    'glasses_camera/connection_state',
  );
  static const EventChannel _bluetoothStateChannel = EventChannel(
    'glasses_camera/bluetooth_state',
  );
  static const EventChannel _scanResultsChannel = EventChannel(
    'glasses_camera/scan_results',
  );
  static const EventChannel _eventsChannel = EventChannel(
    'glasses_camera/events',
  );

  // Singleton
  static final GlassesCameraService _instance = GlassesCameraService._();
  factory GlassesCameraService() => _instance;
  GlassesCameraService._();

  // State
  bool _initialized = false;
  GlassesConnectionState _connectionState = GlassesConnectionState.disconnected;
  GlassesBluetoothState _bluetoothState = GlassesBluetoothState.unknown;
  GlassesCameraState _cameraState = GlassesCameraState.unavailable;

  String? _lastConnectedIdentifier;
  bool _autoReconnectEnabled = true;
  int _reconnectAttempts = 0;
  static const int maxReconnectAttempts = 3;

  // Stream controllers
  final _connectionStateController =
      StreamController<GlassesConnectionState>.broadcast();
  final _bluetoothStateController =
      StreamController<GlassesBluetoothState>.broadcast();
  final _cameraStateController =
      StreamController<GlassesCameraState>.broadcast();
  final _scanResultsController =
      StreamController<List<GlassesDevice>>.broadcast();
  final _eventsController = StreamController<GlassesEvent>.broadcast();
  final _downloadProgressController =
      StreamController<MediaDownloadProgress>.broadcast();
  final _logController = StreamController<String>.broadcast();

  // Volume State
  double _volume = 0.8;
  final _volumeController = StreamController<double>.broadcast();

  StreamSubscription? _connectionSub;
  StreamSubscription? _bluetoothSub;
  StreamSubscription? _scanSub;
  StreamSubscription? _eventsSub;

  // ─── Public Getters ───

  bool get isInitialized => _initialized;
  GlassesConnectionState get connectionState => _connectionState;
  GlassesBluetoothState get bluetoothState => _bluetoothState;
  GlassesCameraState get cameraState => _cameraState;
  bool get isConnected => _connectionState == GlassesConnectionState.connected;
  String? get lastConnectedIdentifier => _lastConnectedIdentifier;
  bool get autoReconnectEnabled => _autoReconnectEnabled;
  double get volume => _volume;

  // ─── Streams ───

  Stream<GlassesConnectionState> get connectionStateStream =>
      _connectionStateController.stream;
  Stream<GlassesConnectionState> get bleStatusStream => connectionStateStream;
  Stream<GlassesBluetoothState> get bluetoothStateStream =>
      _bluetoothStateController.stream;
  Stream<GlassesCameraState> get cameraStateStream =>
      _cameraStateController.stream;
  Stream<List<GlassesDevice>> get scanResultsStream =>
      _scanResultsController.stream;
  Stream<GlassesEvent> get eventsStream => _eventsController.stream;
  Stream<MediaDownloadProgress> get downloadProgressStream =>
      _downloadProgressController.stream;
  Stream<String> get logStream => _logController.stream;
  Stream<double> get volumeStream => _volumeController.stream;

  // ─── Volume Control ───

  Future<void> setVolume(double level) async {
    _volume = level.clamp(0.0, 1.0);
    _volumeController.add(_volume);
    try {
      await _method.invokeMethod('setVolume', {'volume': _volume});
    } catch (_) {}
  }

  Future<void> increaseVolume() async {
    await setVolume((_volume + 0.1).clamp(0.0, 1.0));
  }

  Future<void> decreaseVolume() async {
    await setVolume((_volume - 0.1).clamp(0.0, 1.0));
  }

  // ─── Device Filtering ───

  bool _isGlassesDevice(GlassesDevice dev) {
    final name = dev.name.trim().toLowerCase();
    if (name.isEmpty ||
        name == 'unknown' ||
        name == 'null' ||
        name == 'unknown device') {
      return false;
    }
    if (name.contains('tv') ||
        name.contains('printer') ||
        name.contains('band') ||
        name.contains('watch')) {
      return false;
    }
    return name.contains('w660') ||
        name.contains('cyan') ||
        name.contains('qc') ||
        name.contains('glass') ||
        name.contains('smart') ||
        name.contains('frame') ||
        name.contains('lens') ||
        name.contains('audio') ||
        name.contains('camera') ||
        dev.identifier.toLowerCase().contains('w660');
  }

  // ─── Structured Logging ───

  void logSDK(String msg) => _log('[GLASSES][SDK] $msg');
  void logBT(String msg) => _log('[GLASSES][BLUETOOTH] $msg');
  void logScan(String msg) => _log('[GLASSES][SCAN] $msg');
  void logConnect(String msg) => _log('[GLASSES][CONNECT] $msg');
  void logCamera(String msg) => _log('[GLASSES][CAMERA] $msg');
  void logMedia(String msg) => _log('[GLASSES][MEDIA] $msg');
  void logPermission(String msg) => _log('[GLASSES][PERMISSION] $msg');
  void logError(String msg) => _log('[GLASSES][ERROR] $msg');

  void _log(String formattedMessage) {
    developer.log(formattedMessage, name: _tag);
    _logController.add(formattedMessage);
  }

  // ─── Lifecycle ───

  Future<void> initialize() async {
    if (_initialized) return;
    _setConnectionState(GlassesConnectionState.initializing);
    logSDK('Initializing official native glasses SDK...');

    try {
      _setupEventChannels();
      await _method.invokeMethod('initialize');
      _initialized = true;
      logSDK('Native SDK initialized successfully');

      final prefs = await SharedPreferences.getInstance();
      _lastConnectedIdentifier = prefs.getString('lastConnectedIdentifier');
      _autoReconnectEnabled = prefs.getBool('autoReconnectEnabled') ?? true;

      if (_autoReconnectEnabled && _lastConnectedIdentifier != null) {
        logConnect(
          'Auto-connecting to previously saved device: $_lastConnectedIdentifier',
        );
        _setConnectionState(GlassesConnectionState.connecting);
        try {
          await connect(_lastConnectedIdentifier!);
        } catch (e) {
          logError('Auto-connect on init failed: $e');
          _setConnectionState(GlassesConnectionState.disconnected);
        }
      } else {
        _setConnectionState(GlassesConnectionState.disconnected);
      }
    } on PlatformException catch (e) {
      logError('SDK init failed: ${e.message}');
      _setConnectionState(GlassesConnectionState.error);
      rethrow;
    }
  }

  void _setupEventChannels() {
    _connectionSub?.cancel();
    _bluetoothSub?.cancel();
    _scanSub?.cancel();
    _eventsSub?.cancel();

    _connectionSub = _connectionStateChannel.receiveBroadcastStream().listen((
      data,
    ) {
      final stateStr = data as String;
      logConnect('Native connection callback state: $stateStr');
      final state = GlassesConnectionState.values.firstWhere(
        (e) => e.name == stateStr,
        orElse: () => GlassesConnectionState.disconnected,
      );

      if (state == GlassesConnectionState.connected) {
        _reconnectAttempts = 0;
        _updateCameraState(GlassesCameraState.ready);
      } else if (state == GlassesConnectionState.disconnected) {
        _updateCameraState(GlassesCameraState.unavailable);
        _handleUnexpectedDisconnect();
      }

      _setConnectionState(state);
    });

    _bluetoothSub = _bluetoothStateChannel.receiveBroadcastStream().listen((
      data,
    ) {
      final stateStr = data as String;
      logBT('Native BT state callback: $stateStr');
      final state = GlassesBluetoothState.values.firstWhere(
        (e) => e.name == stateStr,
        orElse: () => GlassesBluetoothState.unknown,
      );
      _bluetoothState = state;
      _bluetoothStateController.add(state);

      if (!state.isPoweredOn &&
          _connectionState != GlassesConnectionState.bluetoothOff) {
        _setConnectionState(GlassesConnectionState.bluetoothOff);
      }
    });

    _scanSub = _scanResultsChannel.receiveBroadcastStream().listen((data) {
      final list = (data as List)
          .map((e) => GlassesDevice.fromMap(e as Map))
          .where((dev) => _isGlassesDevice(dev))
          .toList();
      logScan('Discovery callback: ${list.length} glasses device(s) found');
      _scanResultsController.add(list);
      if (list.isNotEmpty &&
          _connectionState == GlassesConnectionState.scanning) {
        _setConnectionState(GlassesConnectionState.deviceFound);
      }
    });

    _eventsSub = _eventsChannel.receiveBroadcastStream().listen((data) {
      final event = GlassesEvent.fromMap(data as Map);
      _eventsController.add(event);

      if (event.type == GlassesEventType.mediaDownloadProgress) {
        _downloadProgressController.add(
          MediaDownloadProgress.fromMap(event.data),
        );
      }

      if (event.type == GlassesEventType.workTypeChanged) {
        final workCode = event.data['workType'] as int? ?? 0;
        final workType = GlassesWorkType.fromCode(workCode);
        logCamera('SDK work mode changed: ${workType.name}');
        switch (workType) {
          case GlassesWorkType.photo:
          case GlassesWorkType.aiPhoto:
            _updateCameraState(GlassesCameraState.capturing);
          case GlassesWorkType.video:
            _updateCameraState(GlassesCameraState.recording);
          case GlassesWorkType.audio:
            _updateCameraState(GlassesCameraState.recordingAudio);
          default:
            if (isConnected) _updateCameraState(GlassesCameraState.ready);
        }
      }
    });
  }

  void _setConnectionState(GlassesConnectionState state) {
    _connectionState = state;
    _connectionStateController.add(state);
  }

  void _updateCameraState(GlassesCameraState state) {
    _cameraState = state;
    _cameraStateController.add(state);
  }

  Future<void> setAutoReconnect(bool enable) async {
    _autoReconnectEnabled = enable;
    logConnect('Auto-reconnect preference set to: $enable');
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool('autoReconnectEnabled', enable);
  }

  Future<void> setAutoConnect(bool enable) => setAutoReconnect(enable);

  Future<void> _handleUnexpectedDisconnect() async {
    if (!_autoReconnectEnabled || _lastConnectedIdentifier == null) return;
    if (_reconnectAttempts >= maxReconnectAttempts) {
      logConnect(
        'Max reconnect attempts ($maxReconnectAttempts) reached. Stopping auto-reconnect.',
      );
      return;
    }

    _reconnectAttempts++;
    logConnect(
      'Unexpected disconnect detected. Attempting reconnect ($_reconnectAttempts/$maxReconnectAttempts)...',
    );
    _setConnectionState(GlassesConnectionState.reconnecting);

    await Future.delayed(const Duration(seconds: 3));
    if (_connectionState == GlassesConnectionState.reconnecting &&
        _lastConnectedIdentifier != null) {
      try {
        await connect(_lastConnectedIdentifier!);
      } catch (e) {
        logError('Auto-reconnect attempt failed: $e');
      }
    }
  }

  Future<void> dispose() async {
    logSDK('Disposing Glasses SDK...');
    try {
      await _method.invokeMethod('dispose');
    } catch (_) {}
    _connectionSub?.cancel();
    _bluetoothSub?.cancel();
    _scanSub?.cancel();
    _eventsSub?.cancel();
    _initialized = false;
  }

  // ─── Scanning ───

  Future<void> startScan({int timeoutSeconds = 15}) async {
    logScan('Initiating BLE discovery (timeout: ${timeoutSeconds}s)...');
    _setConnectionState(GlassesConnectionState.scanning);
    try {
      await _method.invokeMethod('startScan', {'timeout': timeoutSeconds});
    } on PlatformException catch (e) {
      logError('Scan execution failed: ${e.message}');
      _setConnectionState(GlassesConnectionState.error);
      rethrow;
    }
  }

  Future<void> stopScan() async {
    logScan('Stopping BLE discovery');
    try {
      await _method.invokeMethod('stopScan');
    } catch (_) {}
    if (_connectionState == GlassesConnectionState.scanning) {
      _setConnectionState(GlassesConnectionState.disconnected);
    }
  }

  // ─── Connection ───

  Future<void> connect(String deviceIdentifier) async {
    logConnect('Connecting to glasses device identifier: $deviceIdentifier');
    _lastConnectedIdentifier = deviceIdentifier;

    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('lastConnectedIdentifier', deviceIdentifier);

    _setConnectionState(GlassesConnectionState.connecting);

    try {
      await _method.invokeMethod('connect', {'identifier': deviceIdentifier});
    } on PlatformException catch (e) {
      logError('Connection execution failed: ${e.message}');
      _setConnectionState(GlassesConnectionState.error);
      rethrow;
    }
  }

  Future<void> disconnect() async {
    logConnect('Disconnecting glasses');
    _setConnectionState(GlassesConnectionState.disconnecting);
    try {
      await _method.invokeMethod('disconnect');
      _setConnectionState(GlassesConnectionState.disconnected);
    } catch (e) {
      logError('Disconnect error: $e');
      _setConnectionState(GlassesConnectionState.disconnected);
    }
  }

  // ─── Camera Controls ───

  Future<void> takePhoto() async {
    logCamera('Sending takePhoto command to glasses...');
    _updateCameraState(GlassesCameraState.capturing);
    try {
      await _method.invokeMethod('takePhoto');
      logCamera('Photo capture command sent');
    } on PlatformException catch (e) {
      _updateCameraState(GlassesCameraState.ready);
      logError('Photo capture command failed: ${e.message}');
      rethrow;
    }
  }

  Future<void> startVideoRecording() async {
    logCamera('Sending startVideoRecording command...');
    _updateCameraState(GlassesCameraState.recording);
    try {
      await _method.invokeMethod('startVideoRecording');
      logCamera('Video recording started');
    } on PlatformException catch (e) {
      _updateCameraState(GlassesCameraState.ready);
      logError('Video record start failed: ${e.message}');
      rethrow;
    }
  }

  Future<void> stopVideoRecording() async {
    logCamera('Sending stopVideoRecording command...');
    try {
      await _method.invokeMethod('stopVideoRecording');
      _updateCameraState(GlassesCameraState.ready);
      logCamera('Video recording stopped');
    } on PlatformException catch (e) {
      logError('Video record stop failed: ${e.message}');
      rethrow;
    }
  }

  Future<void> startAudioRecording() async {
    logCamera('Sending startAudioRecording command...');
    _updateCameraState(GlassesCameraState.recordingAudio);
    try {
      await _method.invokeMethod('startAudioRecording');
      logCamera('Audio recording started');
    } on PlatformException catch (e) {
      _updateCameraState(GlassesCameraState.ready);
      logError('Audio record start failed: ${e.message}');
      rethrow;
    }
  }

  Future<void> stopAudioRecording() async {
    logCamera('Sending stopAudioRecording command...');
    try {
      await _method.invokeMethod('stopAudioRecording');
      _updateCameraState(GlassesCameraState.ready);
      logCamera('Audio recording stopped');
    } on PlatformException catch (e) {
      logError('Audio record stop failed: ${e.message}');
      rethrow;
    }
  }

  Future<void> restartGlasses() async {
    logSDK('Sending restart (reset) command to glasses...');
    try {
      await _method.invokeMethod('restartGlasses');
      logSDK('Restart command sent successfully.');
    } on PlatformException catch (e) {
      logError('Restart command failed: ${e.message}');
      rethrow;
    }
  }

  Future<void> updateFirmware(String url) async {
    logSDK('Sending firmware update OTA link: $url...');
    try {
      await _method.invokeMethod('updateFirmware', {'url': url});
      logSDK('Firmware update command sent successfully.');
    } on PlatformException catch (e) {
      logError('Firmware update failed: ${e.message}');
      rethrow;
    }
  }

  // ─── Telemetry ───

  Future<GlassesBatteryInfo> getBatteryInfo() async {
    final result = await _method.invokeMethod('getBattery');
    return GlassesBatteryInfo.fromMap(result as Map);
  }

  Future<GlassesVersionInfo> getVersionInfo() async {
    final result = await _method.invokeMethod('getVersionInfo');
    return GlassesVersionInfo.fromMap(result as Map);
  }

  Future<GlassesMediaInfo> getMediaInfo() async {
    final result = await _method.invokeMethod('getMediaInfo');
    return GlassesMediaInfo.fromMap(result as Map);
  }

  Future<void> downloadMedia({
    Duration timeout = const Duration(seconds: 120),
  }) async {
    logMedia('Initiating glasses media import over WiFi AP / P2P...');
    try {
      await _method
          .invokeMethod('downloadMedia')
          .timeout(
            timeout,
            onTimeout: () {
              logError('Media import timed out after ${timeout.inSeconds}s');
              throw TimeoutException(
                'Media import timed out (${timeout.inSeconds}s). Please check glasses Wi-Fi and retry.',
              );
            },
          );
    } catch (e) {
      logError('Media import failed: $e');
      rethrow;
    }
  }

  // ─── Permissions & System Settings ───

  Future<bool> requestPermissions() async {
    logPermission('Requesting native system permissions...');
    final result = await _method.invokeMethod<bool>('requestPermissions');
    return result ?? false;
  }

  Future<bool> requestEnableBluetooth() async {
    logPermission('Prompting system dialog to enable Bluetooth...');
    final result = await _method.invokeMethod<bool>('requestEnableBluetooth');
    return result ?? false;
  }

  Future<bool> isLocationServiceEnabled() async {
    final result = await _method.invokeMethod<bool>('isLocationServiceEnabled');
    return result ?? false;
  }

  Future<bool> isLocationEnabled() async => isLocationServiceEnabled();

  Future<void> openLocationSettings() async {
    logPermission('Opening system location settings...');
    await _method.invokeMethod('openLocationSettings');
  }

  // ─── Unified SDK Upgrades ───

  Future<GlassesStatusInfo> getGlassesStatus() async {
    logSDK('Fetching unified status (battery, version, media)...');
    final result = await _method.invokeMethod('getGlassesStatus');
    return GlassesStatusInfo.fromMap(result as Map);
  }

  Future<void> startMediaSync() async {
    logMedia('Starting media sync task...');
    await downloadMedia();
  }

  Future<bool> cancelMediaSync() async {
    logMedia('Cancelling active media sync...');
    final result = await _method.invokeMethod<bool>('cancelMediaSync');
    return result ?? false;
  }

  Future<bool> deleteMediaFiles({List<String>? fileIds}) async {
    logMedia('Deleting media files from glasses...');
    final result = await _method.invokeMethod<bool>('deleteMediaFiles', {
      'fileIds': fileIds,
    });
    return result ?? false;
  }

  Future<String?> convertOpusToMp3(String filePath) async {
    logMedia('Converting Opus audio to MP3/PCM ($filePath)...');
    final result = await _method.invokeMethod<String>('convertOpusToMp3', {
      'filePath': filePath,
    });
    return result;
  }

  Future<List<LocalMediaFile>> getLocalMediaFiles() async {
    try {
      final List? result = await _method.invokeMethod('getLocalMediaFiles');
      if (result == null) return [];
      return result.map((e) => LocalMediaFile.fromMap(e as Map)).toList();
    } catch (e) {
      logError('Failed to list local media files: $e');
      return [];
    }
  }

  Future<String> transcodeVideo(String inputPath) async {
    logMedia('Transcoding/remuxing video file: $inputPath');
    try {
      final String? result = await _method.invokeMethod<String>(
        'transcodeVideo',
        {'inputPath': inputPath},
      );
      return result ?? inputPath;
    } catch (e) {
      logError('Failed to transcode video: $e');
      return inputPath;
    }
  }

  Future<bool> openMediaFile(String filePath) async {
    logMedia('Opening media file: $filePath');
    String targetPath = filePath;
    if (filePath.toLowerCase().endsWith('.mp4') ||
        filePath.toLowerCase().contains('video')) {
      try {
        targetPath = await VideoTranscoder.transcodeVideo(filePath);
      } catch (e) {
        logError('Pre-open transcoding failed (proceeding with original): $e');
        targetPath = filePath;
      }
    }
    try {
      final result = await _method.invokeMethod<bool>('openMediaFile', {
        'filePath': targetPath,
      });
      return result ?? false;
    } catch (e) {
      logError('Failed to open media file: $e');
      return false;
    }
  }

  Future<Map<String, dynamic>> playAudio(String filePath) async {
    logMedia('Playing audio file: $filePath');
    try {
      final Map? result = await _method.invokeMethod('playAudio', {
        'filePath': filePath,
      });
      return Map<String, dynamic>.from(result ?? {});
    } catch (e) {
      logError('Failed to play audio: $e');
      rethrow;
    }
  }

  Future<Map<String, dynamic>> pauseAudio() async {
    try {
      final Map? result = await _method.invokeMethod('pauseAudio');
      return Map<String, dynamic>.from(result ?? {});
    } catch (e) {
      logError('Failed to pause audio: $e');
      return {'status': 'stopped'};
    }
  }

  Future<Map<String, dynamic>> resumeAudio() async {
    try {
      final Map? result = await _method.invokeMethod('resumeAudio');
      return Map<String, dynamic>.from(result ?? {});
    } catch (e) {
      logError('Failed to resume audio: $e');
      return {'status': 'stopped'};
    }
  }

  Future<bool> stopAudio() async {
    try {
      final bool? result = await _method.invokeMethod<bool>('stopAudio');
      return result ?? true;
    } catch (e) {
      logError('Failed to stop audio: $e');
      return false;
    }
  }

  Future<bool> seekAudio(int positionMs) async {
    try {
      final bool? result = await _method.invokeMethod<bool>('seekAudio', {
        'positionMs': positionMs,
      });
      return result ?? false;
    } catch (e) {
      logError('Failed to seek audio: $e');
      return false;
    }
  }

  Future<Map<String, dynamic>> getAudioProgress() async {
    try {
      final Map? result = await _method.invokeMethod('getAudioProgress');
      return Map<String, dynamic>.from(result ?? {});
    } catch (e) {
      return {'isPlaying': false, 'positionMs': 0, 'durationMs': 0};
    }
  }
}
