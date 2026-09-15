import 'dart:async';

import 'package:flutter/material.dart';

import 'glasses_camera_service.dart';
import 'models.dart';
import 'permission_dialog_helper.dart';
import 'theme/app_theme.dart';
import 'widgets/radar_scanner_widget.dart';

/// Dedicated Onboarding Pairing Screen with Radar scanning animation
class OnboardingPairingScreen extends StatefulWidget {
  final GlassesCameraService service;

  const OnboardingPairingScreen({super.key, required this.service});

  static Future<void> start(
    BuildContext context,
    GlassesCameraService service,
  ) {
    return Navigator.push(
      context,
      MaterialPageRoute(
        builder: (ctx) => OnboardingPairingScreen(service: service),
      ),
    );
  }

  @override
  State<OnboardingPairingScreen> createState() =>
      _OnboardingPairingScreenState();
}

class _OnboardingPairingScreenState extends State<OnboardingPairingScreen> {
  List<GlassesDevice> _discoveredDevices = [];
  String? _connectingIdentifier;

  StreamSubscription? _scanSub;
  StreamSubscription? _connSub;

  @override
  void initState() {
    super.initState();

    _scanSub = widget.service.scanResultsStream.listen((devices) {
      if (mounted) {
        setState(() => _discoveredDevices = devices);
      }
    });

    _connSub = widget.service.connectionStateStream.listen((state) {
      if (mounted) {
        setState(() {});
        if (state == GlassesConnectionState.connected) {
          // Successfully connected -> pop back to Home tab
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text('Smart Glasses paired successfully!'),
              backgroundColor: AppColors.connected,
            ),
          );
          Navigator.pop(context);
        }
      }
    });

    // Automatically initiate scanning when onboarding opens
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _startDiscovery();
    });
  }

  @override
  void dispose() {
    _scanSub?.cancel();
    _connSub?.cancel();
    super.dispose();
  }

  Future<void> _startDiscovery() async {
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
    await widget.service.startScan(timeoutSeconds: 20);
  }

  @override
  Widget build(BuildContext context) {
    final state = widget.service.connectionState;

    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        backgroundColor: AppColors.background,
        elevation: 0,
        title: const Text('Connect Glasses', style: AppTypography.h3),
        centerTitle: true,
      ),
      body: SafeArea(
        child: Column(
          children: [
            const SizedBox(height: 12),

            // Animated Radar Scanner Section
            RadarScannerWidget(isScanning: state.isScanning, size: 200),
            const SizedBox(height: 20),

            Text(
              state.isScanning
                  ? 'Searching for Smart Glasses...'
                  : (state.isConnecting
                        ? 'Establishing Secure Link...'
                        : 'Tap Scan to Search'),
              style: AppTypography.h2.copyWith(fontSize: 18),
            ),
            const SizedBox(height: 6),
            const Padding(
              padding: EdgeInsets.symmetric(horizontal: 32),
              child: Text(
                'Ensure your Smart Glasses are powered on, charged, and nearby. Showing glasses hardware devices only.',
                style: AppTypography.caption,
                textAlign: TextAlign.center,
              ),
            ),
            const SizedBox(height: 24),

            // Discovered Devices Title & Rescan Action
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text(
                    'Discovered Glasses (${_discoveredDevices.length})',
                    style: AppTypography.h3,
                  ),
                  TextButton.icon(
                    onPressed: state.isScanning
                        ? () => widget.service.stopScan()
                        : _startDiscovery,
                    icon: Icon(
                      state.isScanning ? Icons.stop : Icons.refresh,
                      size: 16,
                    ),
                    label: Text(state.isScanning ? 'Stop' : 'Rescan'),
                    style: TextButton.styleFrom(
                      foregroundColor: AppColors.primary,
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 8),

            // Discovered Devices List
            Expanded(
              child: _discoveredDevices.isEmpty
                  ? Center(
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          const Icon(
                            Icons.bluetooth_searching,
                            size: 40,
                            color: AppColors.textMuted,
                          ),
                          const SizedBox(height: 12),
                          Text(
                            state.isScanning
                                ? 'Scanning BLE frequencies...'
                                : 'No Smart Glasses discovered yet',
                            style: AppTypography.body2,
                          ),
                        ],
                      ),
                    )
                  : ListView.builder(
                      padding: const EdgeInsets.symmetric(
                        horizontal: 20,
                        vertical: 8,
                      ),
                      itemCount: _discoveredDevices.length,
                      itemBuilder: (context, index) {
                        final dev = _discoveredDevices[index];
                        final isThisConnecting =
                            state.isConnecting &&
                            (widget.service.lastConnectedIdentifier ==
                                    dev.identifier ||
                                _connectingIdentifier == dev.identifier);

                        return Container(
                          margin: const EdgeInsets.only(bottom: 10),
                          decoration: BoxDecoration(
                            color: AppColors.surface,
                            borderRadius: AppRadii.borderRadius16,
                            border: Border.all(
                              color: isThisConnecting
                                  ? AppColors.connecting
                                  : AppColors.border,
                              width: isThisConnecting ? 1.5 : 1,
                            ),
                          ),
                          child: ListTile(
                            contentPadding: const EdgeInsets.symmetric(
                              horizontal: 16,
                              vertical: 6,
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
                            trailing: ElevatedButton(
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
                                style: const TextStyle(
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                            ),
                          ),
                        );
                      },
                    ),
            ),
          ],
        ),
      ),
    );
  }
}
