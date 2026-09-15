import 'package:flutter/material.dart';

/// Centralized Design System for Smart Glasses Companion App
abstract class AppColors {
  // Backgrounds
  static const Color background = Color(0xFF0B0F19);
  static const Color surface = Color(0xFF161F30);
  static const Color surfaceElevated = Color(0xFF1E293B);
  static const Color surfaceHighlight = Color(0xFF26334D);

  // Borders & Dividers
  static const Color border = Color(0xFF2E3D59);
  static const Color borderSubtle = Color(0xFF1E293B);

  // Primary Accent & Hardware Colors
  static const Color primary = Color(0xFF00E5FF); // Electric Cyan
  static const Color primaryGlow = Color(0x3300E5FF);
  static const Color secondary = Color(0xFF38BDF8); // Sky Blue

  // Status Colors
  static const Color connected = Color(0xFF10B981); // Emerald Green
  static const Color connectedGlow = Color(0x3310B981);
  static const Color connecting = Color(0xFFF59E0B); // Ambient Amber
  static const Color connectingGlow = Color(0x33F59E0B);
  static const Color disconnected = Color(0xFF64748B); // Slate Grey
  static const Color error = Color(0xFFEF4444); // Crimson Red
  static const Color errorGlow = Color(0x33EF4444);

  // Text Colors
  static const Color textPrimary = Color(0xFFF8FAFC);
  static const Color textSecondary = Color(0xFF94A3B8);
  static const Color textMuted = Color(0xFF64748B);
  static const Color textDisabled = Color(0xFF475569);

  // Category Colors
  static const Color photoAccent = Color(0xFF00E5FF);
  static const Color videoAccent = Color(0xFFEF4444);
  static const Color audioAccent = Color(0xFFF59E0B);
  static const Color firmwareAccent = Color(0xFF8B5CF6);
}

abstract class AppRadii {
  static const double r8 = 8.0;
  static const double r12 = 12.0;
  static const double r16 = 16.0;
  static const double r20 = 20.0;
  static const double r24 = 24.0;
  static const double r32 = 32.0;

  static const BorderRadius borderRadius8 = BorderRadius.all(Radius.circular(r8));
  static const BorderRadius borderRadius12 = BorderRadius.all(Radius.circular(r12));
  static const BorderRadius borderRadius16 = BorderRadius.all(Radius.circular(r16));
  static const BorderRadius borderRadius20 = BorderRadius.all(Radius.circular(r20));
  static const BorderRadius borderRadius24 = BorderRadius.all(Radius.circular(r24));
  static const BorderRadius borderRadius32 = BorderRadius.all(Radius.circular(r32));
}

abstract class AppTypography {
  static const TextStyle h1 = TextStyle(
    color: AppColors.textPrimary,
    fontSize: 24.0,
    fontWeight: FontWeight.bold,
    letterSpacing: -0.5,
  );

  static const TextStyle h2 = TextStyle(
    color: AppColors.textPrimary,
    fontSize: 20.0,
    fontWeight: FontWeight.bold,
    letterSpacing: -0.3,
  );

  static const TextStyle h3 = TextStyle(
    color: AppColors.textPrimary,
    fontSize: 16.0,
    fontWeight: FontWeight.w600,
  );

  static const TextStyle body1 = TextStyle(
    color: AppColors.textPrimary,
    fontSize: 14.0,
    fontWeight: FontWeight.normal,
  );

  static const TextStyle body2 = TextStyle(
    color: AppColors.textSecondary,
    fontSize: 13.0,
    fontWeight: FontWeight.normal,
  );

  static const TextStyle caption = TextStyle(
    color: AppColors.textMuted,
    fontSize: 11.0,
    fontWeight: FontWeight.normal,
  );

  static const TextStyle tag = TextStyle(
    color: AppColors.primary,
    fontSize: 11.0,
    fontWeight: FontWeight.bold,
    letterSpacing: 0.5,
  );

  static const TextStyle button = TextStyle(
    color: Colors.black,
    fontSize: 14.0,
    fontWeight: FontWeight.bold,
    letterSpacing: 0.2,
  );
}

class AppTheme {
  static ThemeData get darkTheme {
    return ThemeData.dark().copyWith(
      scaffoldBackgroundColor: AppColors.background,
      primaryColor: AppColors.primary,
      colorScheme: const ColorScheme.dark(
        primary: AppColors.primary,
        secondary: AppColors.secondary,
        surface: AppColors.surface,
        error: AppColors.error,
      ),
      appBarTheme: const AppBarTheme(
        backgroundColor: AppColors.background,
        elevation: 0,
        centerTitle: false,
        iconTheme: IconThemeData(color: AppColors.textPrimary),
      ),
      cardTheme: CardThemeData(
        color: AppColors.surface,
        shape: RoundedRectangleBorder(
          borderRadius: AppRadii.borderRadius20,
          side: const BorderSide(color: AppColors.border, width: 1.0),
        ),
        elevation: 0,
      ),
      snackBarTheme: SnackBarThemeData(
        backgroundColor: AppColors.surfaceElevated,
        contentTextStyle: const TextStyle(color: AppColors.textPrimary),
        shape: RoundedRectangleBorder(borderRadius: AppRadii.borderRadius12),
        behavior: SnackBarBehavior.floating,
      ),
      navigationBarTheme: NavigationBarThemeData(
        backgroundColor: AppColors.surface,
        indicatorColor: AppColors.primaryGlow,
        labelTextStyle: WidgetStateProperty.resolveWith((states) {
          if (states.contains(WidgetState.selected)) {
            return const TextStyle(
              color: AppColors.primary,
              fontSize: 12,
              fontWeight: FontWeight.bold,
            );
          }
          return const TextStyle(color: AppColors.textMuted, fontSize: 12);
        }),
      ),
    );
  }
}
