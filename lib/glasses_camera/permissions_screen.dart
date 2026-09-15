import 'package:flutter/material.dart';

import 'glasses_camera_service.dart';
import 'theme/app_theme.dart';

class PermissionsScreen extends StatefulWidget {
  final GlassesCameraService service;

  const PermissionsScreen({super.key, required this.service});

  @override
  State<PermissionsScreen> createState() => _PermissionsScreenState();
}

class _PermissionsScreenState extends State<PermissionsScreen> {
  bool _isLoading = false;

  Future<void> _requestPermissions() async {
    setState(() => _isLoading = true);
    final ok = await widget.service.requestPermissions();
    if (mounted) {
      setState(() {
        _isLoading = false;
      });
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            ok ? 'All permissions granted!' : 'Permission request submitted',
          ),
          backgroundColor: ok ? AppColors.connected : AppColors.connecting,
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        title: const Text('System Permissions', style: AppTypography.h3),
        backgroundColor: AppColors.surface,
        elevation: 0,
      ),
      body: Padding(
        padding: const EdgeInsets.all(20.0),
        child: Column(
          children: [
            Text('Required Companion Permissions', style: AppTypography.h2),
            const SizedBox(height: 8),
            Text(
              'Bluetooth, Location, Camera, Microphone, and Media Storage permissions are required to scan, pair, record, and transfer media with smart glasses.',
              style: AppTypography.body2,
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 24),
            Expanded(
              child: ListView(
                children: [
                  _buildItem('Bluetooth Scanning & Control', Icons.bluetooth),
                  _buildItem('Location Services (BLE & Wi-Fi Discovery)', Icons.location_on),
                  _buildItem('Camera & Video Capture', Icons.camera_alt),
                  _buildItem('Microphone Recording', Icons.mic),
                  _buildItem('Photo Library / Storage Access', Icons.folder),
                ],
              ),
            ),
            SizedBox(
              width: double.infinity,
              child: ElevatedButton(
                onPressed: _isLoading ? null : _requestPermissions,
                style: ElevatedButton.styleFrom(
                  backgroundColor: AppColors.primary,
                  foregroundColor: Colors.black,
                  padding: const EdgeInsets.symmetric(vertical: 16),
                  shape: RoundedRectangleBorder(
                    borderRadius: AppRadii.borderRadius20,
                  ),
                ),
                child: _isLoading
                    ? const SizedBox(
                        width: 20,
                        height: 20,
                        child: CircularProgressIndicator(color: Colors.black, strokeWidth: 2),
                      )
                    : const Text(
                        'Request Permissions',
                        style: TextStyle(
                          fontSize: 16,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildItem(String title, IconData icon) {
    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: AppRadii.borderRadius16,
        border: Border.all(color: AppColors.border),
      ),
      child: Material(
        color: Colors.transparent,
        borderRadius: AppRadii.borderRadius16,
        child: ListTile(
          leading: Icon(icon, color: AppColors.primary),
          title: Text(title, style: AppTypography.body1),
          trailing: Container(
            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
            decoration: const BoxDecoration(
              color: AppColors.primaryGlow,
              borderRadius: AppRadii.borderRadius8,
            ),
            child: Text('REQUIRED', style: AppTypography.tag.copyWith(fontSize: 10)),
          ),
        ),
      ),
    );
  }
}
