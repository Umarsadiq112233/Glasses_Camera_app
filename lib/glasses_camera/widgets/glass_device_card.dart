import 'package:flutter/material.dart';

import '../models.dart';
import '../theme/app_theme.dart';
import 'connection_status_badge.dart';

/// Premium Device Status Card displaying real device telemetry
class GlassDeviceCard extends StatelessWidget {
  final GlassesConnectionState connectionState;
  final String? deviceIdentifier;
  final GlassesBatteryInfo? batteryInfo;
  final GlassesVersionInfo? versionInfo;
  final GlassesMediaInfo? mediaInfo;

  const GlassDeviceCard({
    super.key,
    required this.connectionState,
    this.deviceIdentifier,
    this.batteryInfo,
    this.versionInfo,
    this.mediaInfo,
  });

  @override
  Widget build(BuildContext context) {
    final isConnected = connectionState == GlassesConnectionState.connected;
    final batteryLevel = batteryInfo?.level ?? 0;
    final isCharging = batteryInfo?.isCharging ?? false;

    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: AppRadii.borderRadius24,
        border: Border.all(
          color: isConnected
              ? AppColors.connected.withValues(alpha: 0.4)
              : AppColors.border,
          width: 1.5,
        ),
        boxShadow: [
          BoxShadow(
            color: isConnected ? AppColors.connectedGlow : Colors.transparent,
            blurRadius: 20,
            spreadRadius: 1,
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Header Row: Smart Glasses Icon + Title + Status Badge
          Row(
            children: [
              Container(
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: isConnected
                      ? AppColors.connectedGlow
                      : AppColors.surfaceElevated,
                  shape: BoxShape.circle,
                  border: Border.all(
                    color: isConnected ? AppColors.connected : AppColors.border,
                    width: 1,
                  ),
                ),
                child: Icon(
                  Icons.remove_red_eye,
                  color: isConnected ? AppColors.connected : AppColors.textMuted,
                  size: 24,
                ),
              ),
              const SizedBox(width: 14),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'SMART GLASSES',
                      style: AppTypography.tag.copyWith(
                        color: isConnected ? AppColors.primary : AppColors.textMuted,
                      ),
                    ),
                    const SizedBox(height: 2),
                    Text(
                      deviceIdentifier != null ? 'W660_EDFF' : 'Glasses Companion',
                      style: AppTypography.h3,
                      overflow: TextOverflow.ellipsis,
                    ),
                    Text(
                      deviceIdentifier != null
                          ? 'ID: $deviceIdentifier'
                          : 'Not Paired',
                      style: AppTypography.caption,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ],
                ),
              ),
              ConnectionStatusBadge(state: connectionState),
            ],
          ),

          if (isConnected) ...[
            const Padding(
              padding: EdgeInsets.symmetric(vertical: 16),
              child: Divider(color: AppColors.borderSubtle, height: 1),
            ),

            // Battery Status Bar
            Row(
              children: [
                Icon(
                  isCharging
                      ? Icons.battery_charging_full
                      : (batteryLevel > 20
                            ? Icons.battery_full
                            : Icons.battery_alert),
                  color: batteryLevel > 20
                      ? AppColors.connected
                      : AppColors.error,
                  size: 20,
                ),
                const SizedBox(width: 8),
                Text('Battery Status', style: AppTypography.body2),
                const Spacer(),
                Text(
                  '$batteryLevel%${isCharging ? ' (Charging)' : ''}',
                  style: AppTypography.h3.copyWith(
                    color: batteryLevel > 20
                        ? AppColors.textPrimary
                        : AppColors.error,
                    fontSize: 14,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 8),
            ClipRRect(
              borderRadius: AppRadii.borderRadius8,
              child: LinearProgressIndicator(
                value: (batteryLevel / 100).clamp(0.0, 1.0),
                minHeight: 8,
                backgroundColor: AppColors.surfaceElevated,
                valueColor: AlwaysStoppedAnimation<Color>(
                  batteryLevel > 20 ? AppColors.connected : AppColors.error,
                ),
              ),
            ),

            const SizedBox(height: 16),

            // Telemetry Grid: Media Files & Firmware
            Row(
              children: [
                Expanded(
                  child: _buildMetricTile(
                    icon: Icons.photo_library,
                    label: 'Media Files',
                    value: '${mediaInfo?.totalCount ?? 0} items',
                    color: AppColors.audioAccent,
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: _buildMetricTile(
                    icon: Icons.memory,
                    label: 'Firmware',
                    value: versionInfo?.firmwareVersion.isNotEmpty == true
                        ? versionInfo!.firmwareVersion
                        : 'v1.0.0',
                    color: AppColors.firmwareAccent,
                  ),
                ),
              ],
            ),
          ],
        ],
      ),
    );
  }

  Widget _buildMetricTile({
    required IconData icon,
    required String label,
    required String value,
    required Color color,
  }) {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: AppColors.surfaceElevated,
        borderRadius: AppRadii.borderRadius16,
        border: Border.all(color: AppColors.borderSubtle),
      ),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(8),
            decoration: BoxDecoration(
              color: color.withValues(alpha: 0.15),
              borderRadius: AppRadii.borderRadius12,
            ),
            child: Icon(icon, color: color, size: 18),
          ),
          const SizedBox(width: 10),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(label, style: AppTypography.caption),
                const SizedBox(height: 2),
                Text(
                  value,
                  style: AppTypography.body1.copyWith(
                    fontWeight: FontWeight.bold,
                    fontSize: 13,
                  ),
                  overflow: TextOverflow.ellipsis,
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
