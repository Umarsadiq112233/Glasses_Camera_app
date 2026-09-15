import 'dart:async';
import 'package:flutter/material.dart';

import 'glasses_camera_service.dart';
import 'models.dart';
import 'theme/app_theme.dart';
import 'widgets/camera_viewfinder.dart';
import 'widgets/shutter_button.dart';

class CameraScreen extends StatefulWidget {
  final GlassesCameraService service;

  const CameraScreen({super.key, required this.service});

  @override
  State<CameraScreen> createState() => _CameraScreenState();
}

class _CameraScreenState extends State<CameraScreen> {
  bool _isTakingPhoto = false;
  Timer? _recordTimer;
  int _recordDurationSeconds = 0;
  int _selectedModeIndex = 0; // 0: Photo, 1: Video, 2: Audio

  @override
  void initState() {
    super.initState();
    widget.service.cameraStateStream.listen((state) {
      if (mounted) {
        setState(() {});
        if (state == GlassesCameraState.recording || state == GlassesCameraState.recordingAudio) {
          _startTimer();
        } else {
          _stopTimer();
        }
      }
    });
  }

  void _startTimer() {
    _recordTimer?.cancel();
    _recordDurationSeconds = 0;
    _recordTimer = Timer.periodic(const Duration(seconds: 1), (timer) {
      if (mounted) setState(() => _recordDurationSeconds++);
    });
  }

  void _stopTimer() {
    _recordTimer?.cancel();
    _recordTimer = null;
    _recordDurationSeconds = 0;
  }

  @override
  void dispose() {
    _recordTimer?.cancel();
    super.dispose();
  }

  String get _activeModeName {
    switch (_selectedModeIndex) {
      case 1:
        return 'Video Mode';
      case 2:
        return 'Voice Mode';
      case 0:
      default:
        return 'Photo Mode';
    }
  }

  @override
  Widget build(BuildContext context) {
    final cameraState = widget.service.cameraState;
    final isConnected = widget.service.isConnected;

    return Scaffold(
      backgroundColor: AppColors.background,
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(20.0),
          child: Column(
            children: [
              // Mode Selector Tabs (Photo / Video / Audio)
              Container(
                padding: const EdgeInsets.all(4),
                decoration: BoxDecoration(
                  color: AppColors.surface,
                  borderRadius: AppRadii.borderRadius20,
                  border: Border.all(color: AppColors.border),
                ),
                child: Row(
                  children: [
                    Expanded(
                      child: _buildModeTab(0, 'Photo', Icons.camera_alt),
                    ),
                    Expanded(
                      child: _buildModeTab(1, 'Video', Icons.videocam),
                    ),
                    Expanded(
                      child: _buildModeTab(2, 'Voice', Icons.mic),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 20),

              // Hardware Viewfinder Display
              Expanded(
                child: CameraViewfinder(
                  cameraState: cameraState,
                  isConnected: isConnected,
                  recordDurationSeconds: _recordDurationSeconds,
                  activeModeName: _activeModeName,
                ),
              ),
              const SizedBox(height: 24),

              // Shutter & Controls Area
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                children: [
                  _buildSideControlBtn(
                    icon: cameraState == GlassesCameraState.recordingAudio ? Icons.stop : Icons.mic,
                    label: cameraState == GlassesCameraState.recordingAudio ? 'Stop Voice' : 'Record Voice',
                    color: cameraState == GlassesCameraState.recordingAudio ? AppColors.error : AppColors.audioAccent,
                    onPressed: isConnected ? _toggleAudio : null,
                  ),

                  // Center Primary Shutter Button
                  ShutterButton(
                    onPressed: isConnected ? _handleShutterPress : null,
                    isTakingPhoto: _isTakingPhoto,
                    isRecording: cameraState.isRecording,
                    color: _selectedModeIndex == 1
                        ? AppColors.videoAccent
                        : (_selectedModeIndex == 2 ? AppColors.audioAccent : AppColors.primary),
                    icon: _selectedModeIndex == 1
                        ? Icons.videocam
                        : (_selectedModeIndex == 2 ? Icons.mic : Icons.camera_alt),
                  ),

                  _buildSideControlBtn(
                    icon: cameraState == GlassesCameraState.recording ? Icons.stop : Icons.videocam,
                    label: cameraState == GlassesCameraState.recording ? 'Stop Video' : 'Record Video',
                    color: cameraState == GlassesCameraState.recording ? AppColors.error : AppColors.videoAccent,
                    onPressed: isConnected ? _toggleVideo : null,
                  ),
                ],
              ),
              const SizedBox(height: 12),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildModeTab(int index, String title, IconData icon) {
    final isSelected = _selectedModeIndex == index;
    return GestureDetector(
      onTap: () => setState(() => _selectedModeIndex = index),
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 200),
        padding: const EdgeInsets.symmetric(vertical: 10),
        decoration: BoxDecoration(
          color: isSelected ? AppColors.primary : Colors.transparent,
          borderRadius: AppRadii.borderRadius16,
        ),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              icon,
              size: 16,
              color: isSelected ? Colors.black : AppColors.textMuted,
            ),
            const SizedBox(width: 6),
            Text(
              title,
              style: TextStyle(
                color: isSelected ? Colors.black : AppColors.textMuted,
                fontWeight: FontWeight.bold,
                fontSize: 13,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSideControlBtn({
    required IconData icon,
    required String label,
    required Color color,
    VoidCallback? onPressed,
  }) {
    return Column(
      children: [
        IconButton(
          onPressed: onPressed,
          icon: Icon(icon),
          color: color,
          iconSize: 24,
          style: IconButton.styleFrom(
            backgroundColor: color.withValues(alpha: 0.15),
            padding: const EdgeInsets.all(14),
            shape: const CircleBorder(),
          ),
        ),
        const SizedBox(height: 6),
        Text(
          label,
          style: AppTypography.caption.copyWith(color: AppColors.textSecondary),
        ),
      ],
    );
  }

  Future<void> _handleShutterPress() async {
    if (_selectedModeIndex == 1) {
      await _toggleVideo();
    } else if (_selectedModeIndex == 2) {
      await _toggleAudio();
    } else {
      await _takePhoto();
    }
  }

  Future<void> _takePhoto() async {
    setState(() => _isTakingPhoto = true);
    try {
      await widget.service.takePhoto();
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Photo capture command sent to glasses!'),
            backgroundColor: AppColors.connected,
          ),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Photo capture failed: $e'),
            backgroundColor: AppColors.error,
          ),
        );
      }
    } finally {
      if (mounted) setState(() => _isTakingPhoto = false);
    }
  }

  Future<void> _toggleVideo() async {
    final isRecording = widget.service.cameraState == GlassesCameraState.recording;
    try {
      if (isRecording) {
        await widget.service.stopVideoRecording();
      } else {
        await widget.service.startVideoRecording();
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Video command error: $e'),
            backgroundColor: AppColors.error,
          ),
        );
      }
    }
  }

  Future<void> _toggleAudio() async {
    final isRecording = widget.service.cameraState == GlassesCameraState.recordingAudio;
    try {
      if (isRecording) {
        await widget.service.stopAudioRecording();
      } else {
        await widget.service.startAudioRecording();
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Audio command error: $e'),
            backgroundColor: AppColors.error,
          ),
        );
      }
    }
  }
}
