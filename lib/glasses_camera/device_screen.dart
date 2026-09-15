import 'dart:async';

import 'package:flutter/material.dart';

import 'glasses_camera_service.dart';
import 'models.dart';
import 'permission_dialog_helper.dart';
import 'theme/app_theme.dart';
import 'widgets/empty_state_card.dart';
import 'widgets/glass_device_card.dart';

class DeviceScreen extends StatefulWidget {
  final GlassesCameraService service;

  const DeviceScreen({super.key, required this.service});

  @override
  State<DeviceScreen> createState() => _DeviceScreenState();
}

class _DeviceScreenState extends State<DeviceScreen> {
  List<GlassesDevice> _discoveredDevices = [];
  String? _connectingIdentifier;
  GlassesBatteryInfo? _batteryInfo;
  GlassesVersionInfo? _versionInfo;
  GlassesMediaInfo? _mediaInfo;

  StreamSubscription? _scanSub;
  StreamSubscription? _connSub;
  StreamSubscription? _btSub;
  StreamSubscription? _eventsSub;

  @override
  void initState() {
    super.initState();

    _scanSub = widget.service.scanResultsStream.listen((devices) {
      if (mounted) setState(() => _discoveredDevices = devices);
    });

    _connSub = widget.service.connectionStateStream.listen((state) {
      if (mounted) {
        setState(() {});
        if (state == GlassesConnectionState.connected) {
          _loadDeviceInfo();
        }
      }
    });

    _btSub = widget.service.bluetoothStateStream.listen((_) {
      if (mounted) setState(() {});
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
    _scanSub?.cancel();
    _connSub?.cancel();
    _btSub?.cancel();
    _eventsSub?.cancel();
    super.dispose();
  }

  Future<void> _loadDeviceInfo() async {
    try {
      final bat = await widget.service.getBatteryInfo().timeout(
        const Duration(seconds: 3),
        onTimeout: () => _batteryInfo ?? const GlassesBatteryInfo(level: 0, isCharging: false),
      );
      final ver = await widget.service.getVersionInfo().timeout(
        const Duration(seconds: 3),
        onTimeout: () => _versionInfo ?? const GlassesVersionInfo(firmwareVersion: 'v1.0.0'),
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

  Future<void> _handleStartDiscovery() async {
    final granted = await widget.service.requestPermissions();
    if (!granted) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Permissions required to scan glasses'),
            backgroundColor: AppColors.connecting,
          ),
        );
      }
      return;
    }

    if (!widget.service.bluetoothState.isPoweredOn) {
      final enabled = await widget.service.requestEnableBluetooth();
      if (!enabled) return;
    }

    final locEnabled = await widget.service.isLocationServiceEnabled();
    if (!locEnabled && mounted) {
      final allow = await PermissionDialogHelper.showLocationServiceDialog(
        context,
      );
      if (allow) {
        await widget.service.openLocationSettings();
      }
      return;
    }

    setState(() => _discoveredDevices.clear());
    await widget.service.startScan(timeoutSeconds: 15);
  }

  @override
  Widget build(BuildContext context) {
    final state = widget.service.connectionState;

    return Scaffold(
      backgroundColor: AppColors.background,
      body: CustomScrollView(
        slivers: [
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.all(20.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  GlassDeviceCard(
                    connectionState: state,
                    deviceIdentifier: widget.service.lastConnectedIdentifier,
                    batteryInfo: _batteryInfo,
                    versionInfo: _versionInfo,
                    mediaInfo: _mediaInfo,
                  ),
                  const SizedBox(height: 24),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      const Text(
                        'Discovered Glasses',
                        style: AppTypography.h2,
                      ),
                      ElevatedButton.icon(
                        onPressed: state.isScanning
                            ? () => widget.service.stopScan()
                            : _handleStartDiscovery,
                        icon: Icon(
                          state.isScanning ? Icons.stop : Icons.radar,
                          size: 18,
                        ),
                        label: Text(state.isScanning ? 'Stop' : 'Scan Glasses'),
                        style: ElevatedButton.styleFrom(
                          backgroundColor: state.isScanning
                              ? AppColors.error
                              : AppColors.primary,
                          foregroundColor: Colors.black,
                          shape: RoundedRectangleBorder(
                            borderRadius: AppRadii.borderRadius20,
                          ),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 12),
                ],
              ),
            ),
          ),
          if (_discoveredDevices.isEmpty)
            SliverFillRemaining(
              hasScrollBody: false,
              child: EmptyStateCard(
                icon: state.isScanning
                    ? Icons.bluetooth_searching
                    : Icons.devices_other,
                title: state.isScanning
                    ? 'Searching for nearby Smart Glasses...'
                    : 'No Glasses Discovered',
                message: state.isScanning
                    ? 'Ensure your glasses are turned on, powered, and in pairing mode.'
                    : 'Tap "Scan Glasses" above to discover nearby hardware devices.',
              ),
            )
          else
            SliverList(
              delegate: SliverChildBuilderDelegate((context, index) {
                final dev = _discoveredDevices[index];
                final isThisConnected =
                    state == GlassesConnectionState.connected &&
                    widget.service.lastConnectedIdentifier == dev.identifier;
                final isThisConnecting =
                    state.isConnecting &&
                    (widget.service.lastConnectedIdentifier == dev.identifier ||
                        _connectingIdentifier == dev.identifier);

                return Container(
                  margin: const EdgeInsets.symmetric(
                    horizontal: 20,
                    vertical: 6,
                  ),
                  decoration: BoxDecoration(
                    color: AppColors.surface,
                    borderRadius: AppRadii.borderRadius16,
                    border: Border.all(
                      color: isThisConnected
                          ? AppColors.connected
                          : (isThisConnecting
                                ? AppColors.connecting
                                : AppColors.border),
                      width: (isThisConnected || isThisConnecting) ? 1.5 : 1,
                    ),
                  ),
                  child: Material(
                    color: Colors.transparent,
                    borderRadius: AppRadii.borderRadius16,
                    child: ListTile(
                      contentPadding: const EdgeInsets.symmetric(
                        horizontal: 16,
                        vertical: 8,
                      ),
                      leading: Container(
                        padding: const EdgeInsets.all(10),
                        decoration: const BoxDecoration(
                          color: AppColors.primaryGlow,
                          shape: BoxShape.circle,
                        ),
                        child: const Icon(
                          Icons.remove_red_eye,
                          color: AppColors.primary,
                          size: 20,
                        ),
                      ),
                      title: Text(
                        dev.name,
                        style: AppTypography.h3.copyWith(fontSize: 15),
                      ),
                      subtitle: Text(
                        'ID: ${dev.identifier} • Signal: ${dev.rssi} dBm',
                        style: AppTypography.caption,
                      ),
                      trailing: isThisConnected
                          ? OutlinedButton(
                              onPressed: () => widget.service.disconnect(),
                              style: OutlinedButton.styleFrom(
                                foregroundColor: AppColors.error,
                                side: const BorderSide(color: AppColors.error),
                                shape: RoundedRectangleBorder(
                                  borderRadius: AppRadii.borderRadius16,
                                ),
                              ),
                              child: const Text('Disconnect'),
                            )
                          : ElevatedButton(
                              onPressed: state.isConnecting
                                  ? null
                                  : () {
                                      setState(
                                        () => _connectingIdentifier =
                                            dev.identifier,
                                      );
                                      widget.service.connect(dev.identifier);
                                    },
                              style: ElevatedButton.styleFrom(
                                backgroundColor: isThisConnecting
                                    ? AppColors.connecting
                                    : AppColors.primary,
                                foregroundColor: Colors.black,
                                shape: RoundedRectangleBorder(
                                  borderRadius: AppRadii.borderRadius16,
                                ),
                              ),
                              child: Text(
                                isThisConnecting ? 'Connecting...' : 'Connect',
                                style: const TextStyle(fontWeight: FontWeight.bold),
                              ),
                            ),
                    ),
                  ),
                );
              }, childCount: _discoveredDevices.length),
            ),
        ],
      ),
    );
  }
}
