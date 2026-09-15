import 'package:flutter/material.dart';

import '../glasses_camera_service.dart';
import '../theme/app_theme.dart';

/// Sound / Volume Control Card for Glasses Audio
class VolumeControlCard extends StatefulWidget {
  final GlassesCameraService service;

  const VolumeControlCard({super.key, required this.service});

  @override
  State<VolumeControlCard> createState() => _VolumeControlCardState();
}

class _VolumeControlCardState extends State<VolumeControlCard> {
  late double _currentVolume;

  @override
  void initState() {
    super.initState();
    _currentVolume = widget.service.volume;
    widget.service.volumeStream.listen((vol) {
      if (mounted) setState(() => _currentVolume = vol);
    });
  }

  IconData get _volumeIcon {
    if (_currentVolume <= 0.0) return Icons.volume_off;
    if (_currentVolume < 0.5) return Icons.volume_down;
    return Icons.volume_up;
  }

  @override
  Widget build(BuildContext context) {
    final percentage = (_currentVolume * 100).toInt();

    return Container(
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: AppRadii.borderRadius24,
        border: Border.all(color: AppColors.border),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                padding: const EdgeInsets.all(10),
                decoration: const BoxDecoration(
                  color: AppColors.primaryGlow,
                  shape: BoxShape.circle,
                ),
                child: Icon(_volumeIcon, color: AppColors.primary, size: 20),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text('Glasses Audio Volume', style: AppTypography.h3.copyWith(fontSize: 14)),
                    const SizedBox(height: 2),
                    Text(
                      'Speaker level output ($percentage%)',
                      style: AppTypography.caption,
                    ),
                  ],
                ),
              ),
              Text(
                '$percentage%',
                style: AppTypography.h3.copyWith(color: AppColors.primary, fontSize: 16),
              ),
            ],
          ),
          const SizedBox(height: 14),

          // Slider & Step Controls
          Row(
            children: [
              // Decrease Volume (-)
              IconButton(
                icon: const Icon(Icons.remove_circle_outline, color: AppColors.textSecondary),
                onPressed: () => widget.service.decreaseVolume(),
                tooltip: 'Decrease volume',
              ),

              // Volume Slider
              Expanded(
                child: SliderTheme(
                  data: SliderThemeData(
                    trackHeight: 4,
                    thumbShape: const RoundSliderThumbShape(enabledThumbRadius: 7),
                    overlayShape: const RoundSliderOverlayShape(overlayRadius: 14),
                    activeTrackColor: AppColors.primary,
                    inactiveTrackColor: AppColors.surfaceElevated,
                    thumbColor: AppColors.primary,
                  ),
                  child: Slider(
                    value: _currentVolume,
                    min: 0.0,
                    max: 1.0,
                    onChanged: (val) {
                      setState(() => _currentVolume = val);
                      widget.service.setVolume(val);
                    },
                  ),
                ),
              ),

              // Increase Volume (+)
              IconButton(
                icon: const Icon(Icons.add_circle_outline, color: AppColors.primary),
                onPressed: () => widget.service.increaseVolume(),
                tooltip: 'Increase volume',
              ),
            ],
          ),
        ],
      ),
    );
  }
}
