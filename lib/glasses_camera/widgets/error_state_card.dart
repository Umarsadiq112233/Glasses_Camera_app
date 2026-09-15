import 'package:flutter/material.dart';

import '../theme/app_theme.dart';

/// Human-friendly Error Banner/Card
class ErrorStateCard extends StatelessWidget {
  final String errorMessage;
  final VoidCallback? onRetry;
  final bool isDismissible;
  final VoidCallback? onDismiss;

  const ErrorStateCard({
    super.key,
    required this.errorMessage,
    this.onRetry,
    this.isDismissible = false,
    this.onDismiss,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: AppColors.error.withValues(alpha: 0.15),
        borderRadius: AppRadii.borderRadius16,
        border: Border.all(color: AppColors.error.withValues(alpha: 0.5)),
      ),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(8),
            decoration: BoxDecoration(
              color: AppColors.errorGlow,
              shape: BoxShape.circle,
            ),
            child: const Icon(
              Icons.warning_amber_rounded,
              color: AppColors.error,
              size: 20,
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Action Needed',
                  style: AppTypography.tag.copyWith(color: AppColors.error),
                ),
                const SizedBox(height: 2),
                Text(
                  errorMessage,
                  style: AppTypography.body2.copyWith(
                    color: AppColors.textPrimary,
                  ),
                ),
              ],
            ),
          ),
          if (onRetry != null)
            TextButton(
              onPressed: onRetry,
              child: Text(
                'Try Again',
                style: AppTypography.button.copyWith(
                  color: AppColors.primary,
                  fontSize: 13,
                ),
              ),
            ),
          if (isDismissible && onDismiss != null)
            IconButton(
              icon: const Icon(
                Icons.close,
                color: AppColors.textMuted,
                size: 18,
              ),
              onPressed: onDismiss,
            ),
        ],
      ),
    );
  }
}
