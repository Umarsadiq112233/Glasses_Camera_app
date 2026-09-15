import 'dart:math' as math;
import 'package:flutter/material.dart';

import '../theme/app_theme.dart';

/// Animated Radar Scanner Widget for Glasses Pairing & Discovery
class RadarScannerWidget extends StatefulWidget {
  final bool isScanning;
  final double size;

  const RadarScannerWidget({
    super.key,
    this.isScanning = true,
    this.size = 220.0,
  });

  @override
  State<RadarScannerWidget> createState() => _RadarScannerWidgetState();
}

class _RadarScannerWidgetState extends State<RadarScannerWidget>
    with TickerProviderStateMixin {
  late AnimationController _rotationController;
  late AnimationController _pulseController;
  late Animation<double> _pulseAnimation;

  @override
  void initState() {
    super.initState();
    _rotationController = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 3),
    );

    _pulseController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 1800),
    );

    _pulseAnimation = Tween<double>(begin: 0.2, end: 1.0).animate(
      CurvedAnimation(parent: _pulseController, curve: Curves.easeOut),
    );

    if (widget.isScanning) {
      _rotationController.repeat();
      _pulseController.repeat();
    }
  }

  @override
  void didUpdateWidget(RadarScannerWidget oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.isScanning != oldWidget.isScanning) {
      if (widget.isScanning) {
        _rotationController.repeat();
        _pulseController.repeat();
      } else {
        _rotationController.stop();
        _pulseController.stop();
      }
    }
  }

  @override
  void dispose() {
    _rotationController.dispose();
    _pulseController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: widget.size,
      height: widget.size,
      child: Stack(
        alignment: Alignment.center,
        children: [
          // Concentric Pulsing Rings
          AnimatedBuilder(
            animation: _pulseAnimation,
            builder: (context, child) {
              return CustomPaint(
                size: Size(widget.size, widget.size),
                painter: _RadarGridPainter(
                  pulseValue: widget.isScanning ? _pulseAnimation.value : 0.5,
                  color: AppColors.primary,
                ),
              );
            },
          ),

          // Rotating Sweep Beam
          if (widget.isScanning)
            AnimatedBuilder(
              animation: _rotationController,
              builder: (context, child) {
                return Transform.rotate(
                  angle: _rotationController.value * 2 * math.pi,
                  child: CustomPaint(
                    size: Size(widget.size, widget.size),
                    painter: _RadarBeamPainter(color: AppColors.primary),
                  ),
                );
              },
            ),

          // Center Emblem
          Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              color: AppColors.surfaceElevated,
              border: Border.all(
                color: widget.isScanning ? AppColors.primary : AppColors.border,
                width: 2,
              ),
              boxShadow: [
                BoxShadow(
                  color: widget.isScanning
                      ? AppColors.primaryGlow
                      : Colors.transparent,
                  blurRadius: 20,
                  spreadRadius: 2,
                ),
              ],
            ),
            child: Icon(
              Icons.remove_red_eye,
              color: widget.isScanning ? AppColors.primary : AppColors.textMuted,
              size: 32,
            ),
          ),
        ],
      ),
    );
  }
}

class _RadarGridPainter extends CustomPainter {
  final double pulseValue;
  final Color color;

  _RadarGridPainter({required this.pulseValue, required this.color});

  @override
  void paint(Canvas canvas, Size size) {
    final center = Offset(size.width / 2, size.height / 2);
    final maxRadius = size.width / 2;

    final linePaint = Paint()
      ..color = color.withValues(alpha: 0.25)
      ..strokeWidth = 1.0
      ..style = PaintingStyle.stroke;

    // Draw static concentric circles
    for (int i = 1; i <= 3; i++) {
      canvas.drawCircle(center, (maxRadius / 3) * i, linePaint);
    }

    // Draw crosshair axes
    canvas.drawLine(
      Offset(center.dx, 0),
      Offset(center.dx, size.height),
      linePaint,
    );
    canvas.drawLine(
      Offset(0, center.dy),
      Offset(size.width, center.dy),
      linePaint,
    );

    // Dynamic Pulsing Ring
    final pulseRadius = maxRadius * pulseValue;
    final pulsePaint = Paint()
      ..color = color.withValues(alpha: (1.0 - pulseValue).clamp(0.0, 0.6))
      ..strokeWidth = 2.0
      ..style = PaintingStyle.stroke;

    canvas.drawCircle(center, pulseRadius, pulsePaint);
  }

  @override
  bool shouldRepaint(covariant _RadarGridPainter oldDelegate) {
    return oldDelegate.pulseValue != pulseValue;
  }
}

class _RadarBeamPainter extends CustomPainter {
  final Color color;

  _RadarBeamPainter({required this.color});

  @override
  void paint(Canvas canvas, Size size) {
    final center = Offset(size.width / 2, size.height / 2);
    final radius = size.width / 2;

    final sweepGradient = SweepGradient(
      center: Alignment.center,
      startAngle: 0.0,
      endAngle: math.pi / 2,
      colors: [
        color.withValues(alpha: 0.4),
        color.withValues(alpha: 0.0),
      ],
      stops: const [0.0, 1.0],
    );

    final rect = Rect.fromCircle(center: center, radius: radius);
    final paint = Paint()
      ..shader = sweepGradient.createShader(rect)
      ..style = PaintingStyle.fill;

    canvas.drawArc(rect, 0.0, math.pi / 2, true, paint);

    final linePaint = Paint()
      ..color = color.withValues(alpha: 0.8)
      ..strokeWidth = 2.0;

    canvas.drawLine(
      center,
      Offset(center.dx + radius, center.dy),
      linePaint,
    );
  }

  @override
  bool shouldRepaint(covariant _RadarBeamPainter oldDelegate) => false;
}
