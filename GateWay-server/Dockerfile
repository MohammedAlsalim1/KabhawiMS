# استخدام بيئة تشغيل جافا 21
FROM eclipse-temurin:21-jre-alpine

# تحديد مسار العمل داخل الحاوية
WORKDIR /app

# نسخ ملف الـ JAR الخاص بالبوابة
COPY target/GateWay-server-0.0.1-SNAPSHOT.jar app.jar

# كشف المنفذ الخاص بالبوابة (8080)
EXPOSE 8080

# أمر التشغيل
ENTRYPOINT ["java", "-jar", "app.jar"]