import 'dart:async';

import 'package:flutter/material.dart';

import 'glasses_camera_service.dart';
import 'models.dart';
import 'onboarding_pairing_screen.dart';
import 'theme/app_theme.dart';
import 'widgets/glass_device_card.dart';
import 'widgets/volume_control_card.dart';

class DeviceScreen extends StatefulWidget {
  final GlassesCameraService service;

  const DeviceScreen({super.key, required this.service});

  @override
  State<DeviceScreen> createState() => _DeviceScreenState();
}

class _DeviceScreenState extends State<DeviceScreen> {
  GlassesBatteryInfo? _batteryInfo;
  GlassesVersionInfo? _versionInfo;
  GlassesMediaInfo? _mediaInfo;

  StreamSubscription? _connSub;
  StreamSubscription? _eventsSub;

  @override
  void initState() {
    super.initState();

    _connSub = widget.service.connectionStateStream.listen((state) {
      if (mounted) {
        setState(() {});
        if (state == GlassesConnectionState.connected) {
          _loadDeviceInfo();
        }
      }
    });

    _eventsSub = widget.service.eventsStream.listen((event) {
      if (!mounted) return;
      if (event.type == GlassesEventType.batteryUpdate) {
        setState(() {
          _batteryInfo = GlassesBatteryInfo.fromMap(event.data);
        });
      } else if (event.type == GlassesEventType.mediaCountUpdate) {
        if (event.data.isNotEmpty) {
          setState(() {
            _mediaInfo = GlassesMediaInfo.fromMap(event.data);
          });
        } else {
          _loadDeviceInfo();
        }
      } else if (event.type == GlassesEventType.workTypeChanged ||
          event.type == GlassesEventType.photoThumbnail ||
          event.type == GlassesEventType.mediaDownloadComplete) {
        _loadDeviceInfo();
      }
    });

    if (widget.service.isConnected) {
      _loadDeviceInfo();
    }
  }

  @override
  void dispose() {
    _connSub?.cancel();
    _eventsSub?.cancel();
    super.dispose();
  }

  Future<void> _loadDeviceInfo() async {
    try {
      final bat = await widget.service.getBatteryInfo().timeout(
        const Duration(seconds: 3),
        onTimeout: () =>
            _batteryInfo ??
            const GlassesBatteryInfo(level: 0, isCharging: false),
      );
      final ver = await widget.service.getVersionInfo().timeout(
        const Duration(seconds: 3),
        onTimeout: () =>
            _versionInfo ?? const GlassesVersionInfo(firmwareVersion: 'v1.0.0'),
      );
      final med = await widget.service.getMediaInfo().timeout(
        const Duration(seconds: 3),
        onTimeout: () => _mediaInfo ?? const GlassesMediaInfo(),
      );
      if (mounted) {
        setState(() {
          if (bat.level > 0 || _batteryInfo == null) {
            _batteryInfo = bat;
          }
          if (ver.firmwareVersion.isNotEmpty) {
            _versionInfo = ver;
          }
          _mediaInfo = med;
        });
      }
    } catch (_) {}
  }

  @override
  Widget build(BuildContext context) {
    final state = widget.service.connectionState;
    final isConnected = state == GlassesConnectionState.connected;

    return Scaffold(
      backgroundColor: AppColors.background,
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(20.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Primary Device Card
            GlassDeviceCard(
              connectionState: state,
              deviceIdentifier: widget.service.lastConnectedIdentifier,
              batteryInfo: _batteryInfo,
              versionInfo: _versionInfo,
              mediaInfo: _mediaInfo,
            ),
            const SizedBox(height: 20),

            // Disconnected CTA Card -> Navigates to Onboarding Radar Scanner
            if (!isConnected) ...[
              Container(
                padding: const EdgeInsets.all(20),
                decoration: BoxDecoration(
                  color: AppColors.surface,
                  borderRadius: AppRadii.borderRadius24,
                  border: Border.all(color: AppColors.border),
                ),
                child: Column(
                  children: [
                    Container(
                      padding: const EdgeInsets.all(16),
                      decoration: const BoxDecoration(
                        color: AppColors.primaryGlow,
                        shape: BoxShape.circle,
                      ),
                      child: const Icon(
                        Icons.bluetooth_searching,
                        color: AppColors.primary,
                        size: 36,
                      ),
                    ),
                    const SizedBox(height: 16),
                    Text(
                      'No Glasses Connected',
                      style: AppTypography.h2.copyWith(fontSize: 18),
                    ),
                    const SizedBox(height: 6),
                    const Text(
                      'Connect your Smart Glasses via Bluetooth to enable camera controls, video recording, and media sync.',
                      style: AppTypography.body2,
                      textAlign: TextAlign.center,
                    ),
                    const SizedBox(height: 20),
                    SizedBox(
                      width: double.infinity,
                      child: ElevatedButton.icon(
                        onPressed: () => OnboardingPairingScreen.start(
                          context,
                          widget.service,
                        ),
                        icon: const Icon(Icons.radar, size: 20),
                        label: const Text('Connect Glasses'),
                        style: ElevatedButton.styleFrom(
                          backgroundColor: AppColors.primary,
                          foregroundColor: Colors.black,
                          padding: const EdgeInsets.symmetric(vertical: 14),
                          shape: RoundedRectangleBorder(
                            borderRadius: AppRadii.borderRadius20,
                          ),
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ] else ...[
              // Connected -> Sound / Volume Control Card
              VolumeControlCard(service: widget.service),

              const SizedBox(height: 20),

              // Action Options Card
              Container(
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: AppColors.surface,
                  borderRadius: AppRadii.borderRadius20,
                  border: Border.all(color: AppColors.border),
                ),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    const Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text('Pairing Options', style: AppTypography.h3),
                        Text(
                          'Scan for another pair of glasses',
                          style: AppTypography.caption,
                        ),
                      ],
                    ),
                    OutlinedButton.icon(
                      onPressed: () => OnboardingPairingScreen.start(
                        context,
                        widget.service,
                      ),
                      icon: const Icon(Icons.radar, size: 16),
                      label: const Text('Pair Device'),
                      style: OutlinedButton.styleFrom(
                        foregroundColor: AppColors.primary,
                        side: const BorderSide(color: AppColors.primary),
                        shape: RoundedRectangleBorder(
                          borderRadius: AppRadii.borderRadius16,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }
}
