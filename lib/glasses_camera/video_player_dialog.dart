import 'dart:io';
import 'dart:math' as math;

import 'package:flutter/material.dart';
import 'package:video_player/video_player.dart';

import 'glasses_camera_service.dart';
import 'theme/app_theme.dart';
import 'video_transcoder.dart';

class VideoPlayerDialog extends StatefulWidget {
  final String videoPath;
  final String videoName;
  final GlassesCameraService service;

  const VideoPlayerDialog({
    super.key,
    required this.videoPath,
    required this.videoName,
    required this.service,
  });

  static Future<void> show(
    BuildContext context, {
    required String videoPath,
    required String videoName,
    required GlassesCameraService service,
  }) {
    return showDialog(
      context: context,
      barrierColor: const Color(0xE6000000),
      builder: (ctx) => VideoPlayerDialog(
        videoPath: videoPath,
        videoName: videoName,
        service: service,
      ),
    );
  }

  @override
  State<VideoPlayerDialog> createState() => _VideoPlayerDialogState();
}

class _VideoPlayerDialogState extends State<VideoPlayerDialog> {
  VideoPlayerController? _controller;
  bool _isProcessing = true;
  bool _isInitialized = false;
  String? _errorMessage;
  String _currentVideoPath = '';
  bool _showControls = true;

  @override
  void initState() {
    super.initState();
    _currentVideoPath = widget.videoPath;
    _prepareAndPlayVideo();
  }

  Future<void> _prepareAndPlayVideo() async {
    setState(() {
      _isProcessing = true;
      _errorMessage = null;
    });

    String playPath = widget.videoPath;
    try {
      playPath = await VideoTranscoder.transcodeVideo(widget.videoPath);
    } catch (e) {
      debugPrint(
        '[VideoPlayerDialog] Transcoding error (proceeding with original file): $e',
      );
      playPath = widget.videoPath;
    }

    _currentVideoPath = playPath;
    final file = File(playPath);
    if (!file.existsSync()) {
      if (mounted) {
        setState(() {
          _isProcessing = false;
          _errorMessage = 'Video file not found at $playPath';
        });
      }
      return;
    }

    final controller = VideoPlayerController.file(file);
    try {
      await controller.initialize();
      controller.addListener(_videoListener);
      if (mounted) {
        setState(() {
          _controller = controller;
          _isProcessing = false;
          _isInitialized = true;
        });
        controller.play();
      } else {
        controller.dispose();
      }
    } catch (e) {
      debugPrint('[VideoPlayerDialog] Controller init error: $e');
      if (mounted) {
        setState(() {
          _isProcessing = false;
          _errorMessage = 'Unable to play video: $e';
        });
      }
      controller.dispose();
    }
  }

  void _videoListener() {
    if (mounted) setState(() {});
  }

  @override
  void dispose() {
    _controller?.removeListener(_videoListener);
    _controller?.dispose();
    super.dispose();
  }

  String _formatTime(Duration duration) {
    final minutes = duration.inMinutes.remainder(60).toString().padLeft(2, '0');
    final seconds = duration.inSeconds.remainder(60).toString().padLeft(2, '0');
    return '$minutes:$seconds';
  }

  @override
  Widget build(BuildContext context) {
    final controller = _controller;

    return Dialog.fullscreen(
      backgroundColor: Colors.black,
      child: Stack(
        alignment: Alignment.center,
        children: [
          // Background & Video Display
          GestureDetector(
            onTap: () {
              setState(() {
                _showControls = !_showControls;
              });
            },
            child: Container(
              color: Colors.black,
              width: double.infinity,
              height: double.infinity,
              child: _isProcessing
                  ? _buildProcessingState()
                  : (_errorMessage != null
                        ? _buildErrorState()
                        : (controller != null && _isInitialized
                              ? Center(
                                  child: AspectRatio(
                                    aspectRatio:
                                        controller.value.aspectRatio > 0
                                        ? controller.value.aspectRatio
                                        : 16 / 9,
                                    child: VideoPlayer(controller),
                                  ),
                                )
                              : const SizedBox())),
            ),
          ),

          // Top Header Bar
          if (_showControls)
            Positioned(
              top: 0,
              left: 0,
              right: 0,
              child: Container(
                decoration: const BoxDecoration(
                  gradient: LinearGradient(
                    colors: [Colors.black87, Colors.transparent],
                    begin: Alignment.topCenter,
                    end: Alignment.bottomCenter,
                  ),
                ),
                padding: const EdgeInsets.symmetric(
                  horizontal: 16,
                  vertical: 40,
                ),
                child: Row(
                  children: [
                    IconButton(
                      icon: const Icon(
                        Icons.close,
                        color: Colors.white,
                        size: 28,
                      ),
                      onPressed: () => Navigator.of(context).pop(),
                    ),
                    const SizedBox(width: 8),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Text(
                            widget.videoName,
                            style: AppTypography.h3,
                            overflow: TextOverflow.ellipsis,
                          ),
                          const Text(
                            'Glasses Video Clip • High Definition',
                            style: AppTypography.caption,
                          ),
                        ],
                      ),
                    ),
                    IconButton(
                      icon: const Icon(
                        Icons.open_in_new,
                        color: AppColors.primary,
                      ),
                      tooltip: 'Open with system player',
                      onPressed: () {
                        widget.service.openMediaFile(_currentVideoPath);
                      },
                    ),
                  ],
                ),
              ),
            ),

          // Center Play / Pause Overlay
          if (_showControls &&
              controller != null &&
              _isInitialized &&
              !_isProcessing)
            Center(
              child: CircleAvatar(
                radius: 36,
                backgroundColor: Colors.black54,
                child: IconButton(
                  iconSize: 42,
                  icon: Icon(
                    controller.value.isPlaying
                        ? Icons.pause_circle_filled
                        : Icons.play_circle_filled,
                    color: AppColors.primary,
                  ),
                  onPressed: () {
                    setState(() {
                      if (controller.value.isPlaying) {
                        controller.pause();
                      } else {
                        controller.play();
                      }
                    });
                  },
                ),
              ),
            ),

          // Bottom Controls Bar
          if (_showControls &&
              controller != null &&
              _isInitialized &&
              !_isProcessing)
            Positioned(
              bottom: 0,
              left: 0,
              right: 0,
              child: Container(
                decoration: BoxDecoration(
                  gradient: LinearGradient(
                    colors: [
                      Colors.transparent,
                      Colors.black.withValues(alpha: 0.9),
                    ],
                    begin: Alignment.topCenter,
                    end: Alignment.bottomCenter,
                  ),
                ),
                padding: const EdgeInsets.all(20),
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Row(
                      children: [
                        Text(
                          _formatTime(controller.value.position),
                          style: AppTypography.caption.copyWith(color: AppColors.textPrimary),
                        ),
                        Expanded(
                          child: SliderTheme(
                            data: SliderThemeData(
                              trackHeight: 3,
                              thumbShape: const RoundSliderThumbShape(
                                enabledThumbRadius: 6,
                              ),
                              overlayShape: const RoundSliderOverlayShape(
                                overlayRadius: 14,
                              ),
                              activeTrackColor: AppColors.primary,
                              inactiveTrackColor: AppColors.border,
                              thumbColor: AppColors.primary,
                            ),
                            child: Slider(
                              value: controller.value.position.inMilliseconds
                                  .toDouble()
                                  .clamp(
                                    0.0,
                                    math.max(
                                      1.0,
                                      controller.value.duration.inMilliseconds
                                          .toDouble(),
                                    ),
                                  ),
                              min: 0.0,
                              max: math.max(
                                1.0,
                                controller.value.duration.inMilliseconds
                                    .toDouble(),
                              ),
                              onChanged: (val) {
                                controller.seekTo(
                                  Duration(milliseconds: val.toInt()),
                                );
                              },
                            ),
                          ),
                        ),
                        Text(
                          _formatTime(controller.value.duration),
                          style: AppTypography.caption.copyWith(color: AppColors.textPrimary),
                        ),
                      ],
                    ),
                    const SizedBox(height: 8),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                      children: [
                        IconButton(
                          icon: const Icon(Icons.replay_5, color: Colors.white),
                          onPressed: () {
                            final current = controller.value.position;
                            final target = current - const Duration(seconds: 5);
                            controller.seekTo(
                              target < Duration.zero ? Duration.zero : target,
                            );
                          },
                        ),
                        CircleAvatar(
                          backgroundColor: AppColors.primary,
                          radius: 24,
                          child: IconButton(
                            icon: Icon(
                              controller.value.isPlaying
                                  ? Icons.pause
                                  : Icons.play_arrow,
                              color: Colors.black,
                              size: 26,
                            ),
                            onPressed: () {
                              setState(() {
                                if (controller.value.isPlaying) {
                                  controller.pause();
                                } else {
                                  controller.play();
                                }
                              });
                            },
                          ),
                        ),
                        IconButton(
                          icon: const Icon(
                            Icons.forward_5,
                            color: Colors.white,
                          ),
                          onPressed: () {
                            final current = controller.value.position;
                            final target = current + const Duration(seconds: 5);
                            final maxDur = controller.value.duration;
                            controller.seekTo(
                              target > maxDur ? maxDur : target,
                            );
                          },
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ),
        ],
      ),
    );
  }

  Widget _buildProcessingState() {
    return Column(
      mainAxisAlignment: MainAxisAlignment.center,
      children: const [
        CircularProgressIndicator(color: AppColors.primary),
        SizedBox(height: 20),
        Text('Optimizing Video for Playback...', style: AppTypography.h3),
        SizedBox(height: 8),
        Text(
          'Transcoding glasses media stream to standard MP4 format',
          style: AppTypography.caption,
        ),
      ],
    );
  }

  Widget _buildErrorState() {
    return Padding(
      padding: const EdgeInsets.all(24.0),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const Icon(Icons.error_outline, color: AppColors.error, size: 60),
          const SizedBox(height: 16),
          Text(
            _errorMessage ?? 'Failed to load video',
            style: AppTypography.body1,
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 24),
          ElevatedButton.icon(
            onPressed: _prepareAndPlayVideo,
            icon: const Icon(Icons.refresh),
            label: const Text('Retry'),
            style: ElevatedButton.styleFrom(
              backgroundColor: AppColors.primary,
              foregroundColor: Colors.black,
            ),
          ),
        ],
      ),
    );
  }
}
