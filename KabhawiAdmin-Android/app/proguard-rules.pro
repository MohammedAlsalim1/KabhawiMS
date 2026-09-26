# ---- KabhawiMS Admin ----
# نماذج البيانات (DTO) وواجهة الـ API: نحتفظ بها كما هي لأن Retrofit و kotlinx.serialization
# يعتمدان على أسماء الحقول وتوقيعات الدوال.
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*
-keep class com.kabhawi.admin.data.model.** { *; }
-keep interface com.kabhawi.admin.data.remote.KabhawiApi { *; }

# Retrofit + coroutines (R8 full mode)
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# ZXing
-dontwarn com.google.zxing.**
