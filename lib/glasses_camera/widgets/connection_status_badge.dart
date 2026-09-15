import 'package:flutter/material.dart';

import '../models.dart';
import '../theme/app_theme.dart';

/// Reusable status badge with pulsing LED dot indicator for glasses connection state
class ConnectionStatusBadge extends StatefulWidget {
  final GlassesConnectionState state;
  final bool compact;

  const ConnectionStatusBadge({
    super.key,
    required this.state,
    this.compact = false,
  });

  @override
  State<ConnectionStatusBadge> createState() => _ConnectionStatusBadgeState();
}

class _ConnectionStatusBadgeState extends State<ConnectionStatusBadge>
    with SingleTickerProviderStateMixin {
  late AnimationController _pulseController;
  late Animation<double> _pulseAnimation;

  @override
  void initState() {
    super.initState();
    _pulseController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 1200),
    );
    _pulseAnimation = Tween<double>(begin: 0.4, end: 1.0).animate(
      CurvedAnimation(parent: _pulseController, curve: Curves.easeInOut),
    );

    if (widget.state == GlassesConnectionState.connected ||
        widget.state.isConnecting ||
        widget.state.isScanning) {
      _pulseController.repeat(reverse: true);
    }
  }

  @override
  void didUpdateWidget(ConnectionStatusBadge oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.state != widget.state) {
      if (widget.state == GlassesConnectionState.connected ||
          widget.state.isConnecting ||
          widget.state.isScanning) {
        _pulseController.repeat(reverse: true);
      } else {
        _pulseController.stop();
      }
    }
  }

  @override
  void dispose() {
    _pulseController.dispose();
    super.dispose();
  }

  Color get _statusColor {
    switch (widget.state) {
      case GlassesConnectionState.connected:
        return AppColors.connected;
      case GlassesConnectionState.connecting:
      case GlassesConnectionState.reconnecting:
        return AppColors.connecting;
      case GlassesConnectionState.scanning:
        return AppColors.primary;
      case GlassesConnectionState.error:
        return AppColors.error;
      default:
        return AppColors.disconnected;
    }
  }

  Color get _statusGlowColor {
    switch (widget.state) {
      case GlassesConnectionState.connected:
        return AppColors.connectedGlow;
      case GlassesConnectionState.connecting:
      case GlassesConnectionState.reconnecting:
        return AppColors.connectingGlow;
      case GlassesConnectionState.scanning:
        return AppColors.primaryGlow;
      case GlassesConnectionState.error:
        return AppColors.errorGlow;
      default:
        return Colors.transparent;
    }
  }

  IconData get _statusIcon {
    switch (widget.state) {
      case GlassesConnectionState.connected:
        return Icons.bluetooth_connected;
      case GlassesConnectionState.connecting:
      case GlassesConnectionState.reconnecting:
        return Icons.bluetooth_searching;
      case GlassesConnectionState.scanning:
        return Icons.radar;
      case GlassesConnectionState.error:
        return Icons.error_outline;
      default:
        return Icons.bluetooth_disabled;
    }
  }

  @override
  Widget build(BuildContext context) {
    final isConnected = widget.state == GlassesConnectionState.connected;

    return Container(
      padding: EdgeInsets.symmetric(
        horizontal: widget.compact ? 8 : 12,
        vertical: widget.compact ? 4 : 6,
      ),
      decoration: BoxDecoration(
        color: _statusGlowColor,
        borderRadius: AppRadii.borderRadius12,
        border: Border.all(
          color: _statusColor.withValues(alpha: 0.5),
          width: 1,
        ),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          AnimatedBuilder(
            animation: _pulseAnimation,
            builder: (context, child) {
              return Container(
                width: 8,
                height: 8,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  color: _statusColor.withValues(
                    alpha: (isConnected || widget.state.isConnecting)
                        ? _pulseAnimation.value
                        : 0.6,
                  ),
                  boxShadow: (isConnected || widget.state.isConnecting)
                      ? [
                          BoxShadow(
                            color: _statusColor.withValues(alpha: 0.6),
                            blurRadius: 6,
                            spreadRadius: 1,
                          ),
                        ]
                      : null,
                ),
              );
            },
          ),
          const SizedBox(width: 6),
          if (!widget.compact) ...[
            Icon(_statusIcon, size: 13, color: _statusColor),
            const SizedBox(width: 4),
          ],
          Text(
            widget.state.displayName,
            style: TextStyle(
              color: _statusColor,
              fontSize: widget.compact ? 11 : 12,
              fontWeight: FontWeight.bold,
              letterSpacing: 0.2,
            ),
          ),
        ],
      ),
    );
  }
}
