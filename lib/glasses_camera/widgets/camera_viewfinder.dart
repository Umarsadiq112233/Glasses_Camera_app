import 'package:flutter/material.dart';

import '../models.dart';
import '../theme/app_theme.dart';

/// Camera Viewfinder preview area with corner brackets and hardware HUD
class CameraViewfinder extends StatelessWidget {
  final GlassesCameraState cameraState;
  final bool isConnected;
  final int recordDurationSeconds;
  final String activeModeName;

  const CameraViewfinder({
    super.key,
    required this.cameraState,
    required this.isConnected,
    required this.recordDurationSeconds,
    required this.activeModeName,
  });

  String _formatTimer(int totalSeconds) {
    final minutes = (totalSeconds ~/ 60).toString().padLeft(2, '0');
    final seconds = (totalSeconds % 60).toString().padLeft(2, '0');
    return '$minutes:$seconds';
  }

  Color get _stateColor {
    switch (cameraState) {
      case GlassesCameraState.recording:
        return AppColors.videoAccent;
      case GlassesCameraState.recordingAudio:
        return AppColors.audioAccent;
      case GlassesCameraState.capturing:
        return AppColors.connected;
      default:
        return AppColors.primary;
    }
  }

  IconData get _stateIcon {
    switch (cameraState) {
      case GlassesCameraState.recording:
        return Icons.videocam;
      case GlassesCameraState.recordingAudio:
        return Icons.mic;
      case GlassesCameraState.capturing:
        return Icons.camera_alt;
      default:
        return Icons.camera;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: AppRadii.borderRadius32,
        border: Border.all(
          color: cameraState.isRecording ? AppColors.videoAccent : AppColors.border,
          width: cameraState.isRecording ? 2.0 : 1.0,
        ),
        boxShadow: cameraState.isRecording
            ? [
                BoxShadow(
                  color: AppColors.videoAccent.withValues(alpha: 0.2),
                  blurRadius: 24,
                  spreadRadius: 2,
                ),
              ]
            : null,
      ),
      child: ClipRRect(
        borderRadius: AppRadii.borderRadius32,
        child: Stack(
          alignment: Alignment.center,
          children: [
            // Subtly styled background texture / grid
            CustomPaint(
              size: Size.infinite,
              painter: _GridPainter(color: AppColors.borderSubtle.withValues(alpha: 0.3)),
            ),

            // Top HUD Overlay Row
            Positioned(
              top: 16,
              left: 16,
              right: 16,
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                    decoration: BoxDecoration(
                      color: AppColors.background.withValues(alpha: 0.7),
                      borderRadius: AppRadii.borderRadius12,
                      border: Border.all(color: AppColors.borderSubtle),
                    ),
                    child: Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Container(
                          width: 6,
                          height: 6,
                          decoration: BoxDecoration(
                            shape: BoxShape.circle,
                            color: isConnected ? AppColors.connected : AppColors.error,
                          ),
                        ),
                        const SizedBox(width: 6),
                        Text(
                          activeModeName.toUpperCase(),
                          style: AppTypography.tag.copyWith(fontSize: 10),
                        ),
                      ],
                    ),
                  ),

                  // Recording Badge
                  if (cameraState.isRecording)
                    AnimatedContainer(
                      duration: const Duration(milliseconds: 300),
                      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                      decoration: BoxDecoration(
                        color: AppColors.videoAccent,
                        borderRadius: AppRadii.borderRadius16,
                        boxShadow: [
                          BoxShadow(
                            color: AppColors.videoAccent.withValues(alpha: 0.4),
                            blurRadius: 8,
                          ),
                        ],
                      ),
                      child: Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          const Icon(
                            Icons.fiber_manual_record,
                            color: Colors.white,
                            size: 10,
                          ),
                          const SizedBox(width: 6),
                          Text(
                            'REC ${_formatTimer(recordDurationSeconds)}',
                            style: const TextStyle(
                              color: Colors.white,
                              fontWeight: FontWeight.bold,
                              fontSize: 12,
                              letterSpacing: 0.5,
                            ),
                          ),
                        ],
                      ),
                    ),
                ],
              ),
            ),

            // Viewfinder Corner Framing Brackets
            Positioned.fill(
              child: Padding(
                padding: const EdgeInsets.all(24.0),
                child: CustomPaint(
                  painter: _CornerBracketPainter(color: _stateColor.withValues(alpha: 0.5)),
                ),
              ),
            ),

            // Center Camera Status Content
            Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Container(
                  padding: const EdgeInsets.all(20),
                  decoration: BoxDecoration(
                    color: _stateColor.withValues(alpha: 0.15),
                    shape: BoxShape.circle,
                    border: Border.all(color: _stateColor.withValues(alpha: 0.4)),
                  ),
                  child: Icon(
                    _stateIcon,
                    size: 54,
                    color: _stateColor,
                  ),
                ),
                const SizedBox(height: 16),
                Text(
                  _getStatusText(cameraState),
                  style: AppTypography.h3,
                ),
                const SizedBox(height: 6),
                Text(
                  isConnected
                      ? 'Controls trigger camera action on physical glasses'
                      : 'Glasses disconnected. Connect on Device screen.',
                  style: AppTypography.body2,
                  textAlign: TextAlign.center,
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  String _getStatusText(GlassesCameraState state) {
    switch (state) {
      case GlassesCameraState.recording:
        return 'Recording Video...';
      case GlassesCameraState.recordingAudio:
        return 'Recording Voice Audio...';
      case GlassesCameraState.capturing:
        return 'Capturing Photo...';
      case GlassesCameraState.ready:
        return 'Camera Viewfinder Ready';
      default:
        return 'Camera Standby';
    }
  }
}

class _CornerBracketPainter extends CustomPainter {
  final Color color;
  _CornerBracketPainter({required this.color});

  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = color
      ..strokeWidth = 2.0
      ..style = PaintingStyle.stroke;

    const len = 20.0;
    // Top-Left
    canvas.drawLine(const Offset(0, 0), const Offset(len, 0), paint);
    canvas.drawLine(const Offset(0, 0), const Offset(0, len), paint);

    // Top-Right
    canvas.drawLine(Offset(size.width, 0), Offset(size.width - len, 0), paint);
    canvas.drawLine(Offset(size.width, 0), Offset(size.width, len), paint);

    // Bottom-Left
    canvas.drawLine(Offset(0, size.height), Offset(len, size.height), paint);
    canvas.drawLine(Offset(0, size.height), Offset(0, size.height - len), paint);

    // Bottom-Right
    canvas.drawLine(Offset(size.width, size.height), Offset(size.width - len, size.height), paint);
    canvas.drawLine(Offset(size.width, size.height), Offset(size.width, size.height - len), paint);
  }

  @override
  bool shouldRepaint(covariant _CornerBracketPainter oldDelegate) =>
      oldDelegate.color != color;
}

class _GridPainter extends CustomPainter {
  final Color color;
  _GridPainter({required this.color});

  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = color
      ..strokeWidth = 1.0;

    const step = 40.0;
    for (double x = 0; x < size.width; x += step) {
      canvas.drawLine(Offset(x, 0), Offset(x, size.height), paint);
    }
    for (double y = 0; y < size.height; y += step) {
      canvas.drawLine(Offset(0, y), Offset(size.width, y), paint);
    }
  }

  @override
  bool shouldRepaint(covariant _GridPainter oldDelegate) => false;
}
