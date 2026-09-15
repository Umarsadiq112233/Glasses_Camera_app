import 'package:flutter/material.dart';

import 'glasses_camera/camera_screen.dart';
import 'glasses_camera/device_screen.dart';
import 'glasses_camera/glasses_camera_service.dart';
import 'glasses_camera/media_gallery_screen.dart';
import 'glasses_camera/settings_screen.dart';
import 'glasses_camera/theme/app_theme.dart';
import 'glasses_camera/widgets/connection_status_badge.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const GlassesCameraApp());
}

class GlassesCameraApp extends StatelessWidget {
  const GlassesCameraApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Glasses Companion',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.darkTheme,
      home: const MainHomeScreen(),
    );
  }
}

class MainHomeScreen extends StatefulWidget {
  const MainHomeScreen({super.key});

  @override
  State<MainHomeScreen> createState() => _MainHomeScreenState();
}

class _MainHomeScreenState extends State<MainHomeScreen> {
  final GlassesCameraService _service = GlassesCameraService();
  int _currentIndex = 0;

  @override
  void initState() {
    super.initState();
    _initSDK();
  }

  Future<void> _initSDK() async {
    try {
      await _service.initialize();
      if (mounted) setState(() {});
      _service.connectionStateStream.listen((_) {
        if (mounted) setState(() {});
      });
      _service.cameraStateStream.listen((_) {
        if (mounted) setState(() {});
      });
    } catch (_) {}
  }

  @override
  void dispose() {
    _service.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final screens = [
      DeviceScreen(service: _service),
      CameraScreen(service: _service),
      MediaGalleryScreen(service: _service),
      SettingsScreen(service: _service),
    ];

    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        backgroundColor: AppColors.background,
        elevation: 0,
        centerTitle: false,
        title: Row(
          children: [
            Container(
              padding: const EdgeInsets.all(8),
              decoration: const BoxDecoration(
                color: AppColors.primaryGlow,
                shape: BoxShape.circle,
              ),
              child: const Icon(
                Icons.visibility,
                color: AppColors.primary,
                size: 20,
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Text(
                    'HeyCyan Companion',
                    overflow: TextOverflow.ellipsis,
                    style: AppTypography.h3.copyWith(fontSize: 16),
                  ),
                  Text(
                    'Smart Glasses Companion',
                    overflow: TextOverflow.ellipsis,
                    style: AppTypography.caption,
                  ),
                ],
              ),
            ),
          ],
        ),
        actions: [
          Padding(
            padding: const EdgeInsets.only(right: 16),
            child: ConnectionStatusBadge(
              state: _service.connectionState,
              compact: true,
            ),
          ),
        ],
      ),
      body: IndexedStack(index: _currentIndex, children: screens),
      bottomNavigationBar: NavigationBar(
        selectedIndex: _currentIndex,
        onDestinationSelected: (index) {
          setState(() => _currentIndex = index);
        },
        backgroundColor: AppColors.surface,
        indicatorColor: AppColors.primaryGlow,
        destinations: const [
          NavigationDestination(
            icon: Icon(Icons.bluetooth_searching, color: AppColors.textMuted),
            selectedIcon: Icon(Icons.bluetooth_connected, color: AppColors.primary),
            label: 'Device',
          ),
          NavigationDestination(
            icon: Icon(Icons.camera_alt_outlined, color: AppColors.textMuted),
            selectedIcon: Icon(Icons.camera_alt, color: AppColors.primary),
            label: 'Camera',
          ),
          NavigationDestination(
            icon: Icon(Icons.photo_library_outlined, color: AppColors.textMuted),
            selectedIcon: Icon(Icons.photo_library, color: AppColors.primary),
            label: 'Gallery',
          ),
          NavigationDestination(
            icon: Icon(Icons.settings_outlined, color: AppColors.textMuted),
            selectedIcon: Icon(Icons.settings, color: AppColors.primary),
            label: 'Settings',
          ),
        ],
      ),
    );
  }
}
