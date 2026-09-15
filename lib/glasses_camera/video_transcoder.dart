import 'dart:io';

import 'package:flutter/foundation.dart';

import 'glasses_camera_service.dart';

class VideoTranscoder {
  /// Transcodes or remuxes a raw glasses MP4 video file to standard MP4
  /// via native MediaExtractor & MediaMuxer so it plays properly on
  /// Android system players and in-app video views.
  static Future<String> transcodeVideo(
    String inputPath, {
    void Function(double progress)? onProgress,
  }) async {
    var realInputPath = inputPath;
    var inputFile = File(realInputPath);

    if (!inputFile.existsSync() && realInputPath.contains('_fixed')) {
      realInputPath = realInputPath.replaceAll('_fixed', '');
      inputFile = File(realInputPath);
    }

    if (!inputFile.existsSync()) {
      debugPrint('[Transcoder] Input video file does not exist: $inputPath');
      return inputPath;
    }

    if (realInputPath.contains('_fixed')) {
      return realInputPath;
    }

    try {
      final service = GlassesCameraService();
      final result = await service.transcodeVideo(realInputPath);
      final resultFile = File(result);
      if (resultFile.existsSync() && resultFile.lengthSync() > 0) {
        debugPrint('[Transcoder] Native video transcode/remux result: $result');
        return result;
      }
      return realInputPath;
    } catch (e) {
      debugPrint('[Transcoder] Native video transcode error: $e');
      return realInputPath;
    }
  }
}
