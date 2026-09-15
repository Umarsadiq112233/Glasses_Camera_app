import 'package:flutter/material.dart';

import '../models.dart';
import '../theme/app_theme.dart';

/// User-friendly Media Import Progress Card
class ImportProgressCard extends StatelessWidget {
  final MediaDownloadProgress? progress;
  final String? diagnosticMessage;
  final VoidCallback? onCancel;

  const ImportProgressCard({
    super.key,
    this.progress,
    this.diagnosticMessage,
    this.onCancel,
  });

  @override
  Widget build(BuildContext context) {
    final hasProgress = progress != null && progress!.totalFiles > 0;
    final percentage = (hasProgress ? (progress!.progress * 100).toInt() : 0).clamp(0, 100);
    final currentFile = progress?.currentIndex ?? 0;
    final totalFiles = progress?.totalFiles ?? 0;
    final fileName = progress?.fileName ?? '';

    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: AppColors.surfaceElevated,
        borderRadius: AppRadii.borderRadius20,
        border: Border.all(color: AppColors.primary.withValues(alpha: 0.4)),
        boxShadow: [
          BoxShadow(
            color: AppColors.primaryGlow,
            blurRadius: 16,
            spreadRadius: 1,
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                padding: const EdgeInsets.all(8),
                decoration: const BoxDecoration(
                  color: AppColors.primaryGlow,
                  shape: BoxShape.circle,
                ),
                child: const Icon(
                  Icons.sync,
                  color: AppColors.primary,
                  size: 20,
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Importing from Glasses',
                      style: AppTypography.h3.copyWith(fontSize: 14),
                    ),
                    const SizedBox(height: 2),
                    Text(
                      hasProgress
                          ? 'Transferring media ($currentFile of $totalFiles)...'
                          : (diagnosticMessage ?? 'Connecting to glasses over Wi-Fi...'),
                      style: AppTypography.caption.copyWith(color: AppColors.primary),
                      overflow: TextOverflow.ellipsis,
                    ),
                  ],
                ),
              ),
              Text(
                '$percentage%',
                style: AppTypography.h3.copyWith(
                  color: AppColors.primary,
                  fontSize: 16,
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          ClipRRect(
            borderRadius: AppRadii.borderRadius8,
            child: LinearProgressIndicator(
              value: hasProgress ? progress!.progress : null,
              backgroundColor: AppColors.surface,
              valueColor: const AlwaysStoppedAnimation<Color>(AppColors.primary),
              minHeight: 6,
            ),
          ),
          if (hasProgress && fileName.isNotEmpty) ...[
            const SizedBox(height: 8),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Expanded(
                  child: Text(
                    fileName,
                    style: AppTypography.caption,
                    overflow: TextOverflow.ellipsis,
                  ),
                ),
                if (progress?.speed != null)
                  Text(
                    progress!.speed!,
                    style: AppTypography.caption.copyWith(color: AppColors.textSecondary),
                  ),
              ],
            ),
          ],
        ],
      ),
    );
  }
}
