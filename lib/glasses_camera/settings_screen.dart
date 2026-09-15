import 'dart:async';

import 'package:flutter/material.dart';

import 'glasses_camera_service.dart';
import 'models.dart';
import 'permissions_screen.dart';
import 'theme/app_theme.dart';

class SettingsScreen extends StatefulWidget {
  final GlassesCameraService service;

  const SettingsScreen({super.key, required this.service});

  @override
  State<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends State<SettingsScreen> {
  bool _autoConnect = true;
  bool _autoSync = false;
  GlassesVersionInfo? _versionInfo;
  GlassesBatteryInfo? _batteryInfo;
  GlassesMediaInfo? _mediaInfo;

  StreamSubscription? _connectionSub;
  StreamSubscription? _eventsSub;

  @override
  void initState() {
    super.initState();
    _autoConnect = widget.service.autoReconnectEnabled;
    if (widget.service.isConnected) {
      _loadInfo();
    }

    _connectionSub = widget.service.connectionStateStream.listen((state) {
      if (mounted) {
        setState(() {});
        if (state.isConnected) {
          _loadInfo();
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
          _loadInfo();
        }
      } else if (event.type == GlassesEventType.workTypeChanged ||
          event.type == GlassesEventType.photoThumbnail ||
          event.type == GlassesEventType.mediaDownloadComplete) {
        _loadInfo();
      }
    });
  }

  @override
  void dispose() {
    _connectionSub?.cancel();
    _eventsSub?.cancel();
    super.dispose();
  }

  Future<void> _loadInfo() async {
    try {
      final v = await widget.service.getVersionInfo().timeout(
        const Duration(seconds: 3),
        onTimeout: () =>
            _versionInfo ?? const GlassesVersionInfo(firmwareVersion: 'v1.0.0'),
      );
      final b = await widget.service.getBatteryInfo().timeout(
        const Duration(seconds: 3),
        onTimeout: () =>
            _batteryInfo ??
            const GlassesBatteryInfo(level: 0, isCharging: false),
      );
      final m = await widget.service.getMediaInfo().timeout(
        const Duration(seconds: 3),
        onTimeout: () => _mediaInfo ?? const GlassesMediaInfo(),
      );
      if (mounted) {
        setState(() {
          if (v.firmwareVersion.isNotEmpty) {
            _versionInfo = v;
          }
          if (b.level > 0 || _batteryInfo == null) {
            _batteryInfo = b;
          }
          _mediaInfo = m;
        });
      }
    } catch (_) {}
  }

  Future<void> _handleUpdateFirmware(BuildContext context) async {
    final messenger = ScaffoldMessenger.of(context);
    final TextEditingController urlController = TextEditingController(
      text: 'https://example.com/firmware.bin',
    );

    final bool? shouldUpdate = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: AppColors.surface,
        shape: RoundedRectangleBorder(borderRadius: AppRadii.borderRadius20),
        title: const Text('Update Firmware OTA', style: AppTypography.h3),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'Enter the OTA firmware package download link:',
              style: AppTypography.body2,
            ),
            const SizedBox(height: 12),
            TextField(
              controller: urlController,
              style: const TextStyle(color: AppColors.textPrimary),
              decoration: InputDecoration(
                filled: true,
                fillColor: AppColors.surfaceElevated,
                border: OutlineInputBorder(
                  borderRadius: AppRadii.borderRadius12,
                  borderSide: const BorderSide(color: AppColors.border),
                ),
                hintText: 'https://...',
                hintStyle: const TextStyle(color: AppColors.textMuted),
              ),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx, false),
            child: const Text('Cancel', style: TextStyle(color: AppColors.textMuted)),
          ),
          ElevatedButton(
            onPressed: () => Navigator.pop(ctx, true),
            style: ElevatedButton.styleFrom(
              backgroundColor: AppColors.primary,
              foregroundColor: Colors.black,
              shape: RoundedRectangleBorder(borderRadius: AppRadii.borderRadius12),
            ),
            child: const Text('Update'),
          ),
        ],
      ),
    );

    if (shouldUpdate == true && urlController.text.isNotEmpty) {
      try {
        messenger.showSnackBar(
          const SnackBar(content: Text('Starting firmware update...')),
        );

        await widget.service.updateFirmware(urlController.text);

        if (mounted) {
          messenger.showSnackBar(
            const SnackBar(
              content: Text('Firmware update started. Please wait for completion.'),
              backgroundColor: AppColors.connected,
            ),
          );
        }
      } catch (e) {
        if (mounted) {
          messenger.showSnackBar(
            SnackBar(
              content: Text('Failed to update firmware: $e'),
              backgroundColor: AppColors.error,
            ),
          );
        }
      }
    }
  }

  Future<void> _handleRestart(BuildContext context) async {
    final messenger = ScaffoldMessenger.of(context);
    try {
      messenger.showSnackBar(
        const SnackBar(
          content: Text('Restarting glasses hardware...'),
          duration: Duration(seconds: 2),
        ),
      );

      await widget.service.restartGlasses();

      if (mounted) {
        messenger.showSnackBar(
          const SnackBar(
            content: Text('Glasses restarted successfully.'),
            backgroundColor: AppColors.connected,
          ),
        );
      }
    } catch (e) {
      if (mounted) {
        messenger.showSnackBar(
          SnackBar(
            content: Text('Failed to restart glasses: $e'),
            backgroundColor: AppColors.error,
          ),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final isConnected = widget.service.isConnected;

    return Scaffold(
      backgroundColor: AppColors.background,
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.all(20),
          children: [
            Text('Settings & Information', style: AppTypography.h1),
            const SizedBox(height: 20),
            _buildSectionTitle('Connected Hardware'),
            Container(
              decoration: BoxDecoration(
                color: AppColors.surface,
                borderRadius: AppRadii.borderRadius20,
                border: Border.all(color: AppColors.border),
              ),
              child: Material(
                color: Colors.transparent,
                borderRadius: AppRadii.borderRadius20,
                child: Column(
                  children: [
                    ListTile(
                      leading: const Icon(Icons.bluetooth, color: AppColors.primary),
                      title: Text(
                        isConnected ? 'Glasses Connected' : 'No Glasses Connected',
                        style: AppTypography.h3.copyWith(fontSize: 14),
                      ),
                      subtitle: Text(
                        isConnected
                            ? 'Device ready for camera & media operations'
                            : 'Pair glasses on the Device tab',
                        style: AppTypography.caption,
                      ),
                    ),
                    const Divider(color: AppColors.borderSubtle, height: 1),
                    ListTile(
                      leading: const Icon(Icons.battery_charging_full, color: AppColors.connected),
                      title: const Text('Battery Level', style: AppTypography.body1),
                      trailing: Text(
                        '${_batteryInfo?.level ?? 0}%${_batteryInfo?.isCharging == true ? ' (Charging)' : ''}',
                        style: AppTypography.h3.copyWith(fontSize: 13, color: AppColors.connected),
                      ),
                    ),
                    const Divider(color: AppColors.borderSubtle, height: 1),
                    ListTile(
                      leading: const Icon(Icons.folder, color: AppColors.audioAccent),
                      title: const Text('Media Records', style: AppTypography.body1),
                      trailing: Text(
                        '${_mediaInfo?.totalCount ?? 0} items',
                        style: AppTypography.body2,
                      ),
                    ),
                    const Divider(color: AppColors.borderSubtle, height: 1),
                    ListTile(
                      leading: const Icon(Icons.memory, color: AppColors.firmwareAccent),
                      title: const Text('Firmware Version', style: AppTypography.body1),
                      trailing: Text(
                        _versionInfo?.firmwareVersion.isNotEmpty == true
                            ? _versionInfo!.firmwareVersion
                            : 'v1.0.0',
                        style: AppTypography.body2,
                      ),
                    ),
                    const Divider(color: AppColors.borderSubtle, height: 1),
                    ListTile(
                      leading: const Icon(Icons.system_update_alt, color: AppColors.secondary),
                      title: const Text('Update Firmware (OTA)', style: AppTypography.body1),
                      trailing: const Icon(Icons.chevron_right, color: AppColors.textMuted),
                      onTap: isConnected ? () => _handleUpdateFirmware(context) : null,
                    ),
                    const Divider(color: AppColors.borderSubtle, height: 1),
                    ListTile(
                      leading: const Icon(Icons.restart_alt, color: AppColors.connecting),
                      title: const Text('Restart Glasses', style: AppTypography.body1),
                      trailing: const Icon(Icons.chevron_right, color: AppColors.textMuted),
                      onTap: isConnected ? () => _handleRestart(context) : null,
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 24),
            _buildSectionTitle('App Preferences'),
            Container(
              decoration: BoxDecoration(
                color: AppColors.surface,
                borderRadius: AppRadii.borderRadius20,
                border: Border.all(color: AppColors.border),
              ),
              child: Material(
                color: Colors.transparent,
                borderRadius: AppRadii.borderRadius20,
                child: Column(
                  children: [
                    SwitchListTile(
                      secondary: const Icon(Icons.autorenew, color: AppColors.primary),
                      title: const Text('Auto-Reconnect', style: AppTypography.body1),
                      subtitle: const Text(
                        'Automatically reconnect when paired glasses are nearby',
                        style: AppTypography.caption,
                      ),
                      value: _autoConnect,
                      activeThumbColor: AppColors.primary,
                      onChanged: (val) {
                        setState(() => _autoConnect = val);
                        widget.service.setAutoConnect(val);
                      },
                    ),
                    const Divider(color: AppColors.borderSubtle, height: 1),
                    SwitchListTile(
                      secondary: const Icon(Icons.cloud_sync, color: AppColors.primary),
                      title: const Text('Auto-Sync Media', style: AppTypography.body1),
                      subtitle: const Text(
                        'Import photos & videos automatically upon connection',
                        style: AppTypography.caption,
                      ),
                      value: _autoSync,
                      activeThumbColor: AppColors.primary,
                      onChanged: (val) => setState(() => _autoSync = val),
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 24),
            _buildSectionTitle('Permissions & Security'),
            Container(
              decoration: BoxDecoration(
                color: AppColors.surface,
                borderRadius: AppRadii.borderRadius20,
                border: Border.all(color: AppColors.border),
              ),
              child: Material(
                color: Colors.transparent,
                borderRadius: AppRadii.borderRadius20,
                child: ListTile(
                  leading: const Icon(Icons.security, color: AppColors.primary),
                  title: const Text('System Permissions', style: AppTypography.body1),
                  subtitle: const Text(
                    'Bluetooth, Location, Camera, Microphone & Storage',
                    style: AppTypography.caption,
                  ),
                  trailing: const Icon(Icons.chevron_right, color: AppColors.textMuted),
                  onTap: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (context) => PermissionsScreen(service: widget.service),
                      ),
                    );
                  },
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSectionTitle(String title) {
    return Padding(
      padding: const EdgeInsets.only(left: 4, bottom: 8),
      child: Text(
        title,
        style: AppTypography.tag.copyWith(color: AppColors.textSecondary),
      ),
    );
  }
}
