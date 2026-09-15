import 'package:flutter/material.dart';

import '../theme/app_theme.dart';

/// Tactile hardware shutter capture button
class ShutterButton extends StatefulWidget {
  final VoidCallback? onPressed;
  final bool isTakingPhoto;
  final bool isRecording;
  final Color color;
  final IconData icon;

  const ShutterButton({
    super.key,
    required this.onPressed,
    this.isTakingPhoto = false,
    this.isRecording = false,
    this.color = AppColors.primary,
    this.icon = Icons.camera_alt,
  });

  @override
  State<ShutterButton> createState() => _ShutterButtonState();
}

class _ShutterButtonState extends State<ShutterButton> {
  bool _isPressed = false;

  @override
  Widget build(BuildContext context) {
    final enabled = widget.onPressed != null;

    return GestureDetector(
      onTapDown: enabled ? (_) => setState(() => _isPressed = true) : null,
      onTapUp: enabled ? (_) => setState(() => _isPressed = false) : null,
      onTapCancel: enabled ? () => setState(() => _isPressed = false) : null,
      onTap: widget.onPressed,
      child: AnimatedScale(
        scale: _isPressed ? 0.92 : 1.0,
        duration: const Duration(milliseconds: 100),
        child: Container(
          width: 78,
          height: 78,
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            border: Border.all(
              color: enabled ? Colors.white : AppColors.textDisabled,
              width: 4,
            ),
            color: Colors.transparent,
            boxShadow: enabled && widget.isRecording
                ? [
                    BoxShadow(
                      color: widget.color.withValues(alpha: 0.5),
                      blurRadius: 16,
                      spreadRadius: 2,
                    ),
                  ]
                : null,
          ),
          padding: const EdgeInsets.all(5),
          child: AnimatedContainer(
            duration: const Duration(milliseconds: 200),
            decoration: BoxDecoration(
              shape: widget.isRecording ? BoxShape.rectangle : BoxShape.circle,
              borderRadius: widget.isRecording ? AppRadii.borderRadius12 : null,
              color: enabled
                  ? (widget.isRecording ? AppColors.videoAccent : widget.color)
                  : AppColors.surfaceElevated,
            ),
            child: Center(
              child: widget.isTakingPhoto
                  ? const SizedBox(
                      width: 28,
                      height: 28,
                      child: CircularProgressIndicator(
                        color: Colors.black,
                        strokeWidth: 3,
                      ),
                    )
                  : Icon(
                      widget.isRecording ? Icons.stop : widget.icon,
                      color: enabled ? Colors.black : AppColors.textDisabled,
                      size: widget.isRecording ? 28 : 32,
                    ),
            ),
          ),
        ),
      ),
    );
  }
}
