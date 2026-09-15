import 'package:flutter_test/flutter_test.dart';
import 'package:glasses_camera_app/main.dart';

void main() {
  testWidgets('Glasses Camera App smoke test', (WidgetTester tester) async {
    await tester.pumpWidget(const GlassesCameraApp());
    expect(find.text('HeyCyan Companion'), findsOneWidget);
  });
}
