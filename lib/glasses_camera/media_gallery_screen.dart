import 'dart:async';
import 'dart:io';
import 'dart:math' as math;

import 'package:flutter/material.dart';

import 'glasses_camera_service.dart';
import 'models.dart';
import 'permission_dialog_helper.dart';
import 'theme/app_theme.dart';
import 'video_player_dialog.dart';
import 'video_transcoder.dart';
import 'widgets/empty_state_card.dart';
import 'widgets/error_state_card.dart';
import 'widgets/import_progress_card.dart';

class MediaGalleryScreen extends StatefulWidget {
  final GlassesCameraService service;

  const MediaGalleryScreen({super.key, required this.service});

  @override
  State<MediaGalleryScreen> createState() => _MediaGalleryScreenState();
}

class _MediaGalleryScreenState extends State<MediaGalleryScreen>
    with SingleTickerProviderStateMixin {
  late TabController _tabController;
  GlassesMediaInfo? _mediaInfo;
  List<LocalMediaFile> _localFiles = [];
  List<LocalMediaFile> _localPhotos = [];
  List<LocalMediaFile> _localVideos = [];
  List<LocalMediaFile> _localAudio = [];

  MediaDownloadProgress? _downloadProgress;
  bool _isDownloading = false;
  bool _isLoadingLocal = false;
  String? _lastError;
  String? _diagnosticMessage;
  StreamSubscription? _eventSub;

  // Audio Player State
  LocalMediaFile? _playingAudioFile;
  bool _isAudioPlaying = false;
  int _audioPositionMs = 0;
  int _audioDurationMs = 0;
  Timer? _audioProgressTimer;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 3, vsync: this);
    _loadMediaInfoAndLocalFiles();

    _eventSub = widget.service.eventsStream.listen((event) {
      if (event.type == GlassesEventType.mediaDownloadProgress) {
        if (mounted) {
          setState(() {
            final status = event.data['status'] as String?;
            if (status == 'diagnostic') {
              _diagnosticMessage =
                  event.message ??
                  (event.data['message'] as String?) ??
                  'Connecting to glasses over Wi-Fi...';
            } else if (status == 'downloading') {
              _downloadProgress = MediaDownloadProgress.fromMap(event.data);
              _diagnosticMessage = event.message;
            } else {
              _downloadProgress = MediaDownloadProgress.fromMap(event.data);
            }
            _isDownloading = true;
            _lastError = null;
          });
          _loadLocalFilesSilently();
        }
      } else if (event.type == GlassesEventType.mediaDownloadComplete) {
        if (mounted) {
          setState(() {
            _isDownloading = false;
            _lastError = null;
            _downloadProgress = null;
            _diagnosticMessage = null;
          });
          _loadMediaInfoAndLocalFiles();
        }
      } else if (event.type == GlassesEventType.mediaDownloadError) {
        if (mounted) {
          var errMsg =
              (event.data['hint'] as String?) ??
              (event.data['error'] as String?) ??
              event.message ??
              'Import failed. Please try again.';
          if (errMsg.contains('P2P_DISCOVERY_TIMEOUT')) {
            errMsg = 'Glasses Wi-Fi connection timed out. Tap "Sync Media" again and confirm system prompt.';
          } else if (errMsg.contains('WIFI_JOIN_UNAVAILABLE')) {
            errMsg = 'Glasses Wi-Fi connection prompt was not confirmed. Please retry.';
          }
          setState(() {
            _isDownloading = false;
            _lastError = errMsg;
            _downloadProgress = null;
            _diagnosticMessage = null;
          });
        }
      } else if (event.type == GlassesEventType.audioPlaybackState) {
        final status = event.data['status'] as String?;
        if (status == 'completed' && mounted) {
          _audioProgressTimer?.cancel();
          setState(() {
            _isAudioPlaying = false;
            _audioPositionMs = 0;
          });
        }
      } else if (event.type == GlassesEventType.mediaCountUpdate) {
        if (mounted && event.data.isNotEmpty) {
          setState(() {
            _mediaInfo = GlassesMediaInfo.fromMap(event.data);
          });
          _loadLocalFilesSilently();
        }
      } else if (event.type == GlassesEventType.workTypeChanged) {
        if (mounted) {
          _loadLocalFilesSilently();
        }
      }
    });
  }

  @override
  void dispose() {
    _audioProgressTimer?.cancel();
    widget.service.stopAudio();
    _tabController.dispose();
    _eventSub?.cancel();
    super.dispose();
  }

  Future<void> _loadMediaInfoAndLocalFiles() async {
    await _loadLocalFiles();
    if (widget.service.isConnected) {
      try {
        final info = await widget.service.getMediaInfo();
        if (mounted) setState(() => _mediaInfo = info);
      } catch (_) {}
    }
  }

  Future<void> _loadLocalFiles() async {
    if (mounted) setState(() => _isLoadingLocal = true);
    try {
      final files = await widget.service.getLocalMediaFiles();
      if (mounted) {
        setState(() {
          _localFiles = files;
          _localPhotos = files.where((f) => f.isPhoto).toList();
          _localVideos = files.where((f) => f.isVideo).toList();
          _localAudio = files.where((f) => f.isAudio).toList();
          _isLoadingLocal = false;
        });
        _autoPretranscodeMedia();
      }
    } catch (_) {
      if (mounted) setState(() => _isLoadingLocal = false);
    }
  }

  Future<void> _loadLocalFilesSilently() async {
    try {
      final files = await widget.service.getLocalMediaFiles();
      if (mounted && files.length != _localFiles.length) {
        setState(() {
          _localFiles = files;
          _localPhotos = files.where((f) => f.isPhoto).toList();
          _localVideos = files.where((f) => f.isVideo).toList();
          _localAudio = files.where((f) => f.isAudio).toList();
        });
        _autoPretranscodeMedia();
      }
    } catch (_) {}
  }

  void _autoPretranscodeMedia() {
    for (final video in _localVideos) {
      VideoTranscoder.transcodeVideo(video.path).catchError((e) {
        debugPrint('[Pretranscode] Background transcode skipped/failed: $e');
        return video.path;
      });
    }
    for (final audio in _localAudio) {
      if (audio.path.toLowerCase().endsWith('.opus')) {
        widget.service.convertOpusToMp3(audio.path).catchError((e) {
          debugPrint('[Pretranscode] Background opus audio conversion skipped: $e');
          return audio.path;
        });
      }
    }
  }

  // ─── Native Audio Control Functions ───

  Future<void> _playOrToggleAudio(LocalMediaFile audio) async {
    if (_playingAudioFile?.path == audio.path) {
      if (_isAudioPlaying) {
        await widget.service.pauseAudio();
        setState(() => _isAudioPlaying = false);
      } else {
        await widget.service.resumeAudio();
        setState(() => _isAudioPlaying = true);
      }
      return;
    }

    if (_playingAudioFile != null) {
      await widget.service.stopAudio();
    }

    try {
      String playTarget = audio.path;
      if (audio.path.toLowerCase().endsWith('.opus') || audio.isAudio) {
        final converted = await widget.service.convertOpusToMp3(audio.path);
        if (converted != null && converted.isNotEmpty && File(converted).existsSync()) {
          playTarget = converted;
        }
      }

      final res = await widget.service.playAudio(playTarget);
      final durationMs = (res['durationMs'] as int?) ?? 0;
      setState(() {
        _playingAudioFile = audio;
        _isAudioPlaying = true;
        _audioPositionMs = 0;
        _audioDurationMs = durationMs;
      });
      _startAudioTimer();
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Audio playback error: $e'),
            backgroundColor: AppColors.error,
          ),
        );
      }
    }
  }

  void _startAudioTimer() {
    _audioProgressTimer?.cancel();
    _audioProgressTimer = Timer.periodic(const Duration(milliseconds: 250), (
      timer,
    ) async {
      if (_playingAudioFile == null || !mounted) {
        timer.cancel();
        return;
      }

      final progress = await widget.service.getAudioProgress();
      final isPlaying = progress['isPlaying'] as bool? ?? false;
      final pos = progress['positionMs'] as int? ?? 0;
      final dur = progress['durationMs'] as int? ?? 0;

      if (mounted) {
        setState(() {
          _isAudioPlaying = isPlaying;
          _audioPositionMs = pos;
          if (dur > 0) _audioDurationMs = dur;
        });

        if (!isPlaying &&
            _audioDurationMs > 0 &&
            pos >= _audioDurationMs - 100) {
          timer.cancel();
          setState(() {
            _isAudioPlaying = false;
            _audioPositionMs = 0;
          });
        }
      }
    });
  }

  Future<void> _stopAudio() async {
    _audioProgressTimer?.cancel();
    await widget.service.stopAudio();
    if (mounted) {
      setState(() {
        _playingAudioFile = null;
        _isAudioPlaying = false;
        _audioPositionMs = 0;
        _audioDurationMs = 0;
      });
    }
  }

  Future<void> _seekAudio(double positionMs) async {
    await widget.service.seekAudio(positionMs.toInt());
    setState(() {
      _audioPositionMs = positionMs.toInt();
    });
  }

  String _formatTime(int ms) {
    if (ms <= 0) return '00:00';
    final seconds = (ms / 1000).truncate();
    final minutes = (seconds / 60).truncate();
    final remainingSec = seconds % 60;
    return '${minutes.toString().padLeft(2, '0')}:${remainingSec.toString().padLeft(2, '0')}';
  }

  // ─── Sync Download ───

  Future<void> _startDownload() async {
    final permissionsGranted = await widget.service.requestPermissions();
    if (!permissionsGranted) {
      if (mounted) {
        final allow = await PermissionDialogHelper.showPrePermissionExplanation(
          context: context,
          title: 'Permissions Required',
          description: 'Bluetooth, Location, and Nearby Wi-Fi permissions are required to import photos and videos from your glasses.',
          icon: Icons.security,
        );
        if (allow) {
          final reGranted = await widget.service.requestPermissions();
          if (!reGranted) {
            if (mounted) {
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(
                  content: Text('Permissions denied. Cannot sync media.'),
                  backgroundColor: AppColors.connecting,
                ),
              );
            }
            return;
          }
        } else {
          return;
        }
      }
    }

    final locEnabled = await widget.service.isLocationServiceEnabled();
    if (!locEnabled) {
      if (mounted) {
        final allow = await PermissionDialogHelper.showLocationServiceDialog(
          context,
        );
        if (allow) {
          await widget.service.openLocationSettings();
        }
      }
      return;
    }

    setState(() {
      _isDownloading = true;
      _lastError = null;
      _downloadProgress = null;
      _diagnosticMessage = 'Requesting glasses Wi-Fi...';
    });

    try {
      await widget.service.downloadMedia();
    } catch (e) {
      if (mounted) {
        setState(() {
          _isDownloading = false;
          _lastError = null;
        });
        final msg = e.toString();
        if (msg.contains('LOCATION_SERVICES_DISABLED')) {
          final allow = await PermissionDialogHelper.showLocationServiceDialog(
            context,
          );
          if (allow) {
            await widget.service.openLocationSettings();
          }
        } else {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text('Media import failed: $e'),
              backgroundColor: AppColors.error,
            ),
          );
        }
      }
    }
  }

  // ─── Deletion & Storage Management ───

  Future<void> _confirmAndDeleteSingleFile(LocalMediaFile file) async {
    final confirm = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: AppColors.surface,
        shape: RoundedRectangleBorder(borderRadius: AppRadii.borderRadius20),
        title: Row(
          children: [
            const Icon(Icons.delete_forever, color: AppColors.error, size: 28),
            const SizedBox(width: 10),
            Expanded(
              child: Text(
                'Delete ${file.isPhoto ? 'Photo' : (file.isVideo ? 'Video' : 'Audio')}?',
                style: AppTypography.h3,
              ),
            ),
          ],
        ),
        content: Text(
          'Are you sure you want to delete "${file.name}"? This file will be permanently removed from app storage.',
          style: AppTypography.body2,
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx, false),
            child: const Text('Cancel', style: TextStyle(color: AppColors.textMuted)),
          ),
          ElevatedButton.icon(
            onPressed: () => Navigator.pop(ctx, true),
            icon: const Icon(Icons.delete, size: 18),
            label: const Text('Delete'),
            style: ElevatedButton.styleFrom(
              backgroundColor: AppColors.error,
              foregroundColor: Colors.white,
              shape: RoundedRectangleBorder(borderRadius: AppRadii.borderRadius12),
            ),
          ),
        ],
      ),
    );

    if (confirm == true) {
      if (_playingAudioFile?.path == file.path) {
        await _stopAudio();
      }
      try {
        final f = File(file.path);
        if (await f.exists()) await f.delete();

        final fixedPath = file.path.replaceAll('.mp4', '_fixed.mp4');
        final fixedF = File(fixedPath);
        if (await fixedF.exists()) await fixedF.delete();

        final dotIdx = file.path.lastIndexOf('.');
        if (dotIdx > 0) {
          final swappedPath = '${file.path.substring(0, dotIdx)}_swapped.wav';
          final swappedF = File(swappedPath);
          if (await swappedF.exists()) await swappedF.delete();
        }

        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text('Deleted "${file.name}"'),
              backgroundColor: AppColors.error,
            ),
          );
        }
        _loadMediaInfoAndLocalFiles();
      } catch (e) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text('Failed to delete file: $e'),
              backgroundColor: AppColors.error,
            ),
          );
        }
      }
    }
  }

  Future<void> _confirmAndClearAllLocalStorage() async {
    final confirm = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: AppColors.surface,
        shape: RoundedRectangleBorder(borderRadius: AppRadii.borderRadius20),
        title: const Row(
          children: [
            Icon(Icons.cleaning_services, color: AppColors.audioAccent, size: 28),
            SizedBox(width: 10),
            Expanded(
              child: Text(
                'Clear App Storage?',
                style: AppTypography.h3,
              ),
            ),
          ],
        ),
        content: Text(
          'This will permanently delete all ${_localFiles.length} downloaded photos, videos, and audio clips from local app storage.',
          style: AppTypography.body2,
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx, false),
            child: const Text('Cancel', style: TextStyle(color: AppColors.textMuted)),
          ),
          ElevatedButton.icon(
            onPressed: () => Navigator.pop(ctx, true),
            icon: const Icon(Icons.delete_sweep, size: 18),
            label: const Text('Clear Storage'),
            style: ElevatedButton.styleFrom(
              backgroundColor: AppColors.audioAccent,
              foregroundColor: Colors.black,
              shape: RoundedRectangleBorder(borderRadius: AppRadii.borderRadius12),
            ),
          ),
        ],
      ),
    );

    if (confirm == true) {
      await _stopAudio();
      try {
        for (final file in _localFiles) {
          final f = File(file.path);
          if (await f.exists()) await f.delete();
        }
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text('All local app media cleared.'),
              backgroundColor: AppColors.audioAccent,
            ),
          );
        }
        _loadMediaInfoAndLocalFiles();
      } catch (e) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text('Clear storage failed: $e'),
              backgroundColor: AppColors.error,
            ),
          );
        }
      }
    }
  }

  Future<void> _confirmAndDeleteGlassesStorage() async {
    if (!widget.service.isConnected) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Glasses not connected. Please connect first.'),
          backgroundColor: AppColors.connecting,
        ),
      );
      return;
    }

    final confirm = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: AppColors.surface,
        shape: RoundedRectangleBorder(borderRadius: AppRadii.borderRadius20),
        title: const Row(
          children: [
            Icon(Icons.warning_amber_rounded, color: AppColors.error, size: 28),
            SizedBox(width: 10),
            Expanded(
              child: Text(
                'Delete Glasses Storage?',
                style: AppTypography.h3,
              ),
            ),
          ],
        ),
        content: const Text(
          'Are you sure you want to format/delete all photo and video records stored on the glasses hardware?',
          style: AppTypography.body2,
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx, false),
            child: const Text('Cancel', style: TextStyle(color: AppColors.textMuted)),
          ),
          ElevatedButton.icon(
            onPressed: () => Navigator.pop(ctx, true),
            icon: const Icon(Icons.delete_forever, size: 18),
            label: const Text('Delete Glasses Storage'),
            style: ElevatedButton.styleFrom(
              backgroundColor: AppColors.error,
              foregroundColor: Colors.white,
              shape: RoundedRectangleBorder(borderRadius: AppRadii.borderRadius12),
            ),
          ),
        ],
      ),
    );

    if (confirm == true) {
      try {
        final ok = await widget.service.deleteMediaFiles();
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text(ok ? 'Glasses storage formatted.' : 'Delete command sent.'),
              backgroundColor: AppColors.error,
            ),
          );
        }
        _loadMediaInfoAndLocalFiles();
      } catch (e) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text('Delete glasses media failed: $e'),
              backgroundColor: AppColors.error,
            ),
          );
        }
      }
    }
  }

  void _showPhotoViewer(BuildContext context, LocalMediaFile photo) {
    showDialog(
      context: context,
      builder: (ctx) => Dialog.fullscreen(
        backgroundColor: Colors.black,
        child: Stack(
          children: [
            Center(
              child: InteractiveViewer(
                minScale: 0.5,
                maxScale: 4.0,
                child: Image.file(
                  File(photo.path),
                  fit: BoxFit.contain,
                  errorBuilder: (context, error, stackTrace) => const Icon(
                    Icons.broken_image,
                    color: AppColors.textMuted,
                    size: 64,
                  ),
                ),
              ),
            ),
            Positioned(
              top: 40,
              left: 20,
              child: IconButton(
                icon: const Icon(Icons.close, color: Colors.white, size: 28),
                onPressed: () => Navigator.pop(ctx),
              ),
            ),
            Positioned(
              bottom: 40,
              left: 20,
              right: 20,
              child: Container(
                padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                decoration: BoxDecoration(
                  color: AppColors.surface.withValues(alpha: 0.9),
                  borderRadius: AppRadii.borderRadius20,
                  border: Border.all(color: AppColors.border),
                ),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Text(
                            photo.name,
                            style: AppTypography.h3.copyWith(fontSize: 14),
                            overflow: TextOverflow.ellipsis,
                          ),
                          Text(
                            'Size: ${photo.formattedSize}',
                            style: AppTypography.caption,
                          ),
                        ],
                      ),
                    ),
                    Row(
                      children: [
                        IconButton(
                          icon: const Icon(Icons.delete_outline, color: AppColors.error),
                          tooltip: 'Delete Photo',
                          onPressed: () {
                            Navigator.pop(ctx);
                            _confirmAndDeleteSingleFile(photo);
                          },
                        ),
                        IconButton(
                          icon: const Icon(Icons.open_in_new, color: AppColors.primary),
                          onPressed: () => widget.service.openMediaFile(photo.path),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final isConnected = widget.service.isConnected;
    final photoCount = _localPhotos.isNotEmpty ? _localPhotos.length : (_mediaInfo?.photoCount ?? 0);
    final videoCount = _localVideos.isNotEmpty ? _localVideos.length : (_mediaInfo?.videoCount ?? 0);
    final audioCount = _localAudio.isNotEmpty ? _localAudio.length : (_mediaInfo?.audioCount ?? 0);

    return Scaffold(
      backgroundColor: AppColors.background,
      body: SafeArea(
        child: Column(
          children: [
            // Gallery Top Header
            Padding(
              padding: const EdgeInsets.all(20.0),
              child: Row(
                children: [
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text('Media Gallery', style: AppTypography.h2),
                        Text(
                          'Photos, Videos & Audio Recordings',
                          style: AppTypography.caption,
                        ),
                      ],
                    ),
                  ),
                  IconButton(
                    onPressed: _loadMediaInfoAndLocalFiles,
                    icon: const Icon(Icons.refresh, color: AppColors.textSecondary),
                    tooltip: 'Refresh local gallery',
                  ),
                  PopupMenuButton<String>(
                    icon: const Icon(Icons.more_vert, color: AppColors.textSecondary),
                    tooltip: 'Storage & Media Options',
                    color: AppColors.surface,
                    shape: RoundedRectangleBorder(borderRadius: AppRadii.borderRadius16),
                    onSelected: (val) {
                      if (val == 'clear_local') {
                        _confirmAndClearAllLocalStorage();
                      } else if (val == 'delete_glasses') {
                        _confirmAndDeleteGlassesStorage();
                      }
                    },
                    itemBuilder: (ctx) => [
                      const PopupMenuItem<String>(
                        value: 'clear_local',
                        child: Row(
                          children: [
                            Icon(Icons.cleaning_services, color: AppColors.audioAccent, size: 18),
                            SizedBox(width: 10),
                            Text('Clear App Storage', style: TextStyle(fontSize: 13)),
                          ],
                        ),
                      ),
                      PopupMenuItem<String>(
                        value: 'delete_glasses',
                        enabled: isConnected,
                        child: Row(
                          children: [
                            Icon(
                              Icons.delete_forever,
                              color: isConnected ? AppColors.error : AppColors.textMuted,
                              size: 18,
                            ),
                            const SizedBox(width: 10),
                            Text(
                              'Delete Glasses Storage',
                              style: TextStyle(
                                color: isConnected ? AppColors.textPrimary : AppColors.textMuted,
                                fontSize: 13,
                              ),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(width: 6),
                  ElevatedButton.icon(
                    onPressed: isConnected && !_isDownloading ? _startDownload : null,
                    icon: _isDownloading
                        ? const SizedBox(
                            width: 14,
                            height: 14,
                            child: CircularProgressIndicator(strokeWidth: 2, color: Colors.black),
                          )
                        : const Icon(Icons.download, size: 16),
                    label: Text(_isDownloading ? 'Importing...' : 'Sync Media'),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: AppColors.primary,
                      foregroundColor: Colors.black,
                      shape: RoundedRectangleBorder(borderRadius: AppRadii.borderRadius20),
                    ),
                  ),
                ],
              ),
            ),

            // Import Error Banner
            if (_lastError != null)
              ErrorStateCard(
                errorMessage: _lastError!,
                onRetry: _startDownload,
                isDismissible: true,
                onDismiss: () => setState(() => _lastError = null),
              ),

            // Import Progress Indicator
            if (_isDownloading)
              ImportProgressCard(
                progress: _downloadProgress,
                diagnosticMessage: _diagnosticMessage,
              ),

            // Tab Bar
            TabBar(
              controller: _tabController,
              indicatorColor: AppColors.primary,
              labelColor: AppColors.primary,
              unselectedLabelColor: AppColors.textMuted,
              tabs: [
                Tab(text: 'Photos ($photoCount)'),
                Tab(text: 'Videos ($videoCount)'),
                Tab(text: 'Audio ($audioCount)'),
              ],
            ),

            Expanded(
              child: TabBarView(
                controller: _tabController,
                children: [
                  _buildTabContent(MediaTypeCategory.photos, isConnected),
                  _buildTabContent(MediaTypeCategory.videos, isConnected),
                  _buildTabContent(MediaTypeCategory.audio, isConnected),
                ],
              ),
            ),

            // In-App Floating Audio Player Bar
            if (_playingAudioFile != null) _buildAudioPlayerBar(),
          ],
        ),
      ),
    );
  }

  Widget _buildAudioPlayerBar() {
    final maxDur = math.max(1, _audioDurationMs).toDouble();
    final currentPos = math.min(_audioPositionMs.toDouble(), maxDur);

    return AnimatedContainer(
      duration: const Duration(milliseconds: 300),
      margin: const EdgeInsets.all(16),
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      decoration: BoxDecoration(
        color: AppColors.surfaceElevated,
        borderRadius: AppRadii.borderRadius20,
        border: Border.all(color: AppColors.audioAccent.withValues(alpha: 0.6), width: 1.5),
        boxShadow: [
          BoxShadow(
            color: AppColors.audioAccent.withValues(alpha: 0.2),
            blurRadius: 16,
            spreadRadius: 2,
          ),
        ],
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Row(
            children: [
              _buildEqualizerVisualizer(),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      _playingAudioFile!.name,
                      style: AppTypography.h3.copyWith(fontSize: 13),
                      overflow: TextOverflow.ellipsis,
                    ),
                    const SizedBox(height: 2),
                    Text(
                      '${_formatTime(_audioPositionMs)} / ${_formatTime(_audioDurationMs)} • ${_playingAudioFile!.formattedSize}',
                      style: AppTypography.caption.copyWith(color: AppColors.audioAccent),
                    ),
                  ],
                ),
              ),
              IconButton(
                onPressed: _stopAudio,
                icon: const Icon(Icons.close, color: AppColors.textMuted, size: 20),
                tooltip: 'Stop player',
              ),
            ],
          ),
          SliderTheme(
            data: SliderThemeData(
              trackHeight: 3,
              thumbShape: const RoundSliderThumbShape(enabledThumbRadius: 6),
              overlayShape: const RoundSliderOverlayShape(overlayRadius: 14),
              activeTrackColor: AppColors.audioAccent,
              inactiveTrackColor: AppColors.border,
              thumbColor: AppColors.audioAccent,
            ),
            child: Slider(
              value: currentPos,
              min: 0.0,
              max: maxDur,
              onChanged: (val) => _seekAudio(val),
            ),
          ),
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              IconButton(
                icon: const Icon(Icons.replay_5, color: AppColors.textPrimary),
                onPressed: () {
                  final newPos = math.max(0, _audioPositionMs - 5000);
                  _seekAudio(newPos.toDouble());
                },
              ),
              const SizedBox(width: 12),
              CircleAvatar(
                backgroundColor: AppColors.audioAccent,
                radius: 20,
                child: IconButton(
                  icon: Icon(
                    _isAudioPlaying ? Icons.pause : Icons.play_arrow,
                    color: Colors.black,
                    size: 22,
                  ),
                  onPressed: () => _playOrToggleAudio(_playingAudioFile!),
                ),
              ),
              const SizedBox(width: 12),
              IconButton(
                icon: const Icon(Icons.forward_5, color: AppColors.textPrimary),
                onPressed: () {
                  final newPos = math.min(_audioDurationMs, _audioPositionMs + 5000);
                  _seekAudio(newPos.toDouble());
                },
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildEqualizerVisualizer() {
    return SizedBox(
      width: 28,
      height: 20,
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
        crossAxisAlignment: CrossAxisAlignment.end,
        children: List.generate(4, (index) {
          final heights = [10.0, 18.0, 8.0, 16.0];
          final currentHeight = _isAudioPlaying
              ? (heights[(index + (_audioPositionMs ~/ 250)) % heights.length])
              : 4.0;

          return AnimatedContainer(
            duration: const Duration(milliseconds: 200),
            width: 3.5,
            height: currentHeight,
            decoration: BoxDecoration(
              color: _isAudioPlaying ? AppColors.audioAccent : AppColors.textMuted,
              borderRadius: BorderRadius.circular(2),
            ),
          );
        }),
      ),
    );
  }

  Widget _buildTabContent(MediaTypeCategory category, bool isConnected) {
    List<LocalMediaFile> localCategoryFiles = [];
    IconData icon;
    String label;

    switch (category) {
      case MediaTypeCategory.photos:
        localCategoryFiles = _localPhotos;
        icon = Icons.photo_library;
        label = 'Photos';
        break;
      case MediaTypeCategory.videos:
        localCategoryFiles = _localVideos;
        icon = Icons.video_library;
        label = 'Videos';
        break;
      case MediaTypeCategory.audio:
        localCategoryFiles = _localAudio;
        icon = Icons.library_music;
        label = 'Audio Clips';
        break;
    }

    if (_isLoadingLocal) {
      return const Center(child: CircularProgressIndicator(color: AppColors.primary));
    }

    if (localCategoryFiles.isNotEmpty) {
      if (category == MediaTypeCategory.photos) {
        return GridView.builder(
          padding: const EdgeInsets.all(16),
          gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
            crossAxisCount: 2,
            crossAxisSpacing: 12,
            mainAxisSpacing: 12,
            childAspectRatio: 1.0,
          ),
          itemCount: localCategoryFiles.length,
          itemBuilder: (context, index) {
            final photo = localCategoryFiles[index];
            return GestureDetector(
              onTap: () => _showPhotoViewer(context, photo),
              child: Container(
                decoration: BoxDecoration(
                  color: AppColors.surface,
                  borderRadius: AppRadii.borderRadius16,
                  border: Border.all(color: AppColors.border),
                ),
                child: ClipRRect(
                  borderRadius: AppRadii.borderRadius16,
                  child: Stack(
                    fit: StackFit.expand,
                    children: [
                      Image.file(
                        File(photo.path),
                        fit: BoxFit.cover,
                        errorBuilder: (context, error, stackTrace) => const Center(
                          child: Icon(Icons.broken_image, color: AppColors.textMuted),
                        ),
                      ),
                      Positioned(
                        top: 6,
                        right: 6,
                        child: CircleAvatar(
                          radius: 14,
                          backgroundColor: Colors.black87,
                          child: IconButton(
                            padding: EdgeInsets.zero,
                            iconSize: 16,
                            icon: const Icon(Icons.delete_outline, color: AppColors.error),
                            tooltip: 'Delete Photo',
                            onPressed: () => _confirmAndDeleteSingleFile(photo),
                          ),
                        ),
                      ),
                      Positioned(
                        bottom: 0,
                        left: 0,
                        right: 0,
                        child: Container(
                          padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                          color: Colors.black87,
                          child: Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              Expanded(
                                child: Text(
                                  photo.name,
                                  style: AppTypography.caption.copyWith(color: Colors.white, fontSize: 10),
                                  overflow: TextOverflow.ellipsis,
                                ),
                              ),
                              Text(
                                photo.formattedSize,
                                style: AppTypography.tag.copyWith(fontSize: 10),
                              ),
                            ],
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            );
          },
        );
      } else if (category == MediaTypeCategory.videos) {
        return ListView.builder(
          padding: const EdgeInsets.all(16),
          itemCount: localCategoryFiles.length,
          itemBuilder: (context, index) {
            final video = localCategoryFiles[index];
            return Container(
              margin: const EdgeInsets.only(bottom: 12),
              decoration: BoxDecoration(
                color: AppColors.surface,
                borderRadius: AppRadii.borderRadius16,
                border: Border.all(color: AppColors.border),
              ),
              child: ListTile(
                contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                leading: Container(
                  padding: const EdgeInsets.all(10),
                  decoration: BoxDecoration(
                    color: AppColors.videoAccent.withValues(alpha: 0.15),
                    shape: BoxShape.circle,
                  ),
                  child: const Icon(Icons.play_arrow, color: AppColors.videoAccent, size: 24),
                ),
                title: Text(video.name, style: AppTypography.h3.copyWith(fontSize: 14)),
                subtitle: Text(
                  'Size: ${video.formattedSize} • Glasses Video Clip',
                  style: AppTypography.caption,
                ),
                trailing: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    IconButton(
                      icon: const Icon(Icons.delete_outline, color: AppColors.error, size: 20),
                      tooltip: 'Delete Video',
                      onPressed: () => _confirmAndDeleteSingleFile(video),
                    ),
                    ElevatedButton.icon(
                      onPressed: () {
                        VideoPlayerDialog.show(
                          context,
                          videoPath: video.path,
                          videoName: video.name,
                          service: widget.service,
                        );
                      },
                      icon: const Icon(Icons.play_circle_fill, size: 16),
                      label: const Text('Play'),
                      style: ElevatedButton.styleFrom(
                        backgroundColor: AppColors.primary,
                        foregroundColor: Colors.black,
                        shape: RoundedRectangleBorder(borderRadius: AppRadii.borderRadius20),
                      ),
                    ),
                  ],
                ),
              ),
            );
          },
        );
      } else {
        // Audio Clips Tab
        return ListView.builder(
          padding: const EdgeInsets.all(16),
          itemCount: localCategoryFiles.length,
          itemBuilder: (context, index) {
            final audio = localCategoryFiles[index];
            final isPlayingThis = _playingAudioFile?.path == audio.path && _isAudioPlaying;
            final isSelectedThis = _playingAudioFile?.path == audio.path;

            return Container(
              margin: const EdgeInsets.only(bottom: 12),
              decoration: BoxDecoration(
                color: isSelectedThis ? AppColors.surfaceElevated : AppColors.surface,
                borderRadius: AppRadii.borderRadius16,
                border: Border.all(
                  color: isSelectedThis ? AppColors.audioAccent : AppColors.border,
                  width: isSelectedThis ? 1.5 : 1.0,
                ),
              ),
              child: ListTile(
                contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                leading: Container(
                  padding: const EdgeInsets.all(10),
                  decoration: BoxDecoration(
                    color: AppColors.audioAccent.withValues(alpha: 0.15),
                    shape: BoxShape.circle,
                  ),
                  child: Icon(
                    isPlayingThis ? Icons.graphic_eq : Icons.audiotrack,
                    color: AppColors.audioAccent,
                    size: 22,
                  ),
                ),
                title: Text(audio.name, style: AppTypography.h3.copyWith(fontSize: 14)),
                subtitle: Text(
                  'Size: ${audio.formattedSize} • Glasses Audio Recording',
                  style: AppTypography.caption,
                ),
                trailing: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    IconButton(
                      icon: const Icon(Icons.delete_outline, color: AppColors.error, size: 20),
                      tooltip: 'Delete Audio',
                      onPressed: () => _confirmAndDeleteSingleFile(audio),
                    ),
                    ElevatedButton.icon(
                      onPressed: () => _playOrToggleAudio(audio),
                      icon: Icon(
                        isPlayingThis ? Icons.pause : Icons.play_arrow,
                        size: 16,
                      ),
                      label: Text(isPlayingThis ? 'Pause' : 'Play'),
                      style: ElevatedButton.styleFrom(
                        backgroundColor: AppColors.audioAccent,
                        foregroundColor: Colors.black,
                        shape: RoundedRectangleBorder(borderRadius: AppRadii.borderRadius20),
                      ),
                    ),
                  ],
                ),
              ),
            );
          },
        );
      }
    }

    final countOnGlasses = category == MediaTypeCategory.photos
        ? (_mediaInfo?.photoCount ?? 0)
        : (category == MediaTypeCategory.videos
              ? (_mediaInfo?.videoCount ?? 0)
              : (_mediaInfo?.audioCount ?? 0));

    return EmptyStateCard(
      icon: icon,
      title: countOnGlasses > 0
          ? '$countOnGlasses $label Stored on Glasses'
          : 'No $label Downloaded',
      message: isConnected
          ? 'Tap "Sync Media" above to download $label from your glasses to app storage.'
          : 'Connect your glasses to view and import stored $label.',
      buttonLabel: isConnected ? 'Sync Media' : null,
      onButtonPressed: isConnected ? _startDownload : null,
    );
  }
}

enum MediaTypeCategory { photos, videos, audio }
